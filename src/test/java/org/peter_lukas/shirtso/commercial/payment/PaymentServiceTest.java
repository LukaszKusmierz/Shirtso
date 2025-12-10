package org.peter_lukas.shirtso.commercial.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.order.Order;
import org.peter_lukas.shirtso.commercial.order.OrderRepository;
import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.peter_lukas.shirtso.commercial.payment.dto.ProcessPaymentRequestDto;
import org.peter_lukas.shirtso.commercial.payment.payu.PayUApiException;
import org.peter_lukas.shirtso.commercial.product.validation.OrderNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.PaymentException;
import org.peter_lukas.shirtso.notification.NotificationService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;
    private Payment testPayment;
    private ProcessPaymentRequestDto paymentRequest;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
        testPayment = createTestPayment();
        paymentRequest = createPaymentRequest();
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setOrderId(1);
        order.setOrderStatus(OrderStatus.NEW);
        order.setTotalAmount(new BigDecimal("99.99"));
        return order;
    }

    private Payment createTestPayment() {
        Payment payment = new Payment(testOrder, new BigDecimal("99.99"), PaymentMethod.CREDIT_CARD);
        payment.setPaymentId(1);
        return payment;
    }

    private ProcessPaymentRequestDto createPaymentRequest() {
        return new ProcessPaymentRequestDto(
                1,
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );
    }

    @Nested
    @DisplayName("processPayment tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should process payment successfully with valid request")
        void processPayment_WithValidRequest_ProcessesSuccessfully() {
            // given
            String transactionId = UUID.randomUUID().toString();
            PaymentResponseDto expectedResponse = new PaymentResponseDto(
                    1, 1, new BigDecimal("99.99"), PaymentStatus.COMPLETED,
                    PaymentMethod.CREDIT_CARD, transactionId, null
            );

            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentGateway.processPayment(any(), any(), any(),any() )).thenReturn(new PaymentGateway.PaymentResult(transactionId));
            when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(expectedResponse);

            // when
            PaymentResponseDto result = paymentService.processPayment(paymentRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
            verify(paymentGateway).processPayment(
                    eq(PaymentMethod.CREDIT_CARD),
                    eq(new BigDecimal("99.99")),
                    any(PaymentDetails.class),
                    any(Integer.class)
            );
            verify(notificationService).sendOrderPaidNotification(testOrder);
            assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void processPayment_WhenOrderNotFound_ThrowsException() {
            // given
            when(orderRepository.findByOrderIdWithItems(999)).thenReturn(Optional.empty());

            ProcessPaymentRequestDto invalidRequest = new ProcessPaymentRequestDto(
                    999, PaymentMethod.CREDIT_CARD, "4111111111111111", "John Doe", "12/25", "123"
            );

            // when & then
            assertThatThrownBy(() -> paymentService.processPayment(invalidRequest))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(paymentRepository, never()).save(any());
            verify(paymentGateway, never()).processPayment(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception when order not in NEW status")
        void processPayment_WhenOrderNotInNewStatus_ThrowsException() {
            // given
            testOrder.setOrderStatus(OrderStatus.PROCESSING);
            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("NEW status");

            verify(paymentGateway, never()).processPayment(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception when payment already exists")
        void processPayment_WhenPaymentAlreadyExists_ThrowsException() {
            // given
            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.of(testPayment));

            // when & then
            assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("already exists");

            verify(paymentGateway, never()).processPayment(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should mark payment as failed when gateway fails")
        void processPayment_WhenGatewayFails_MarksPaymentAsFailed() {
            // given
            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentGateway.processPayment(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Payment declined"));

            // when & then
            assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Payment processing failed");

            verify(paymentRepository, times(2)).save(any(Payment.class));
            verify(notificationService, never()).sendOrderPaidNotification(any());
        }

        @Test
        @DisplayName("Should handle PayU API exception gracefully")
        void processPayment_WhenPayUApiExceptionThrown_MarksPaymentAsFailed() {
            // given
            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentGateway.processPayment(any(), any(), any(), any()))
                    .thenThrow(new PayUApiException("PayU service unavailable"));

            // when & then
            assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Payment processing failed")
                    .hasMessageContaining("PayU service unavailable");

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository, times(2)).save(paymentCaptor.capture());

            // Second save should mark payment as failed
            Payment savedPayment = paymentCaptor.getAllValues().get(1);
            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("Should process PayPal payment correctly")
        void processPayment_WithPayPal_ProcessesCorrectly() {
            // given
            ProcessPaymentRequestDto paypalRequest = new ProcessPaymentRequestDto(
                    1, PaymentMethod.PAYPAL, null, null, null, null
            );
            String transactionId = UUID.randomUUID().toString();

            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentGateway.processPayment(any(), any(), any(), any())).thenReturn(new PaymentGateway.PaymentResult(transactionId));
            when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(mock(PaymentResponseDto.class));

            // when
            paymentService.processPayment(paypalRequest);

            // then
            verify(paymentGateway).processPayment(
                    eq(PaymentMethod.PAYPAL),
                    any(),
                    any(PaymentDetails.class),
                    any(Integer.class)
            );
        }

        @Test
        @DisplayName("Should process bank transfer payment correctly")
        void processPayment_WithBankTransfer_ProcessesCorrectly() {
            // given
            ProcessPaymentRequestDto bankTransferRequest = new ProcessPaymentRequestDto(
                    1, PaymentMethod.BANK_TRANSFER, null, "John Doe", null, null
            );
            String transactionId = "PAYU-" + UUID.randomUUID().toString();

            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentGateway.processPayment(any(), any(), any(), any())).thenReturn(new PaymentGateway.PaymentResult(transactionId));
            when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(mock(PaymentResponseDto.class));

            // when
            paymentService.processPayment(bankTransferRequest);

            // then
            ArgumentCaptor<PaymentDetails> detailsCaptor = ArgumentCaptor.forClass(PaymentDetails.class);
            verify(paymentGateway).processPayment(
                    eq(PaymentMethod.BANK_TRANSFER),
                    eq(new BigDecimal("99.99")),
                    detailsCaptor.capture(),
                    any(Integer.class)
            );

            PaymentDetails capturedDetails = detailsCaptor.getValue();
            assertThat(capturedDetails.getCardNumber()).isNull();
            assertThat(capturedDetails.getCardHolderName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should pass correct payment details to gateway")
        void processPayment_PassesCorrectPaymentDetails() {
            // given
            String transactionId = UUID.randomUUID().toString();

            when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentGateway.processPayment(any(), any(), any(), any())).thenReturn(new PaymentGateway.PaymentResult(transactionId));
            when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(mock(PaymentResponseDto.class));

            // when
            paymentService.processPayment(paymentRequest);

            // then
            ArgumentCaptor<PaymentDetails> detailsCaptor = ArgumentCaptor.forClass(PaymentDetails.class);
            verify(paymentGateway).processPayment(
                    eq(PaymentMethod.CREDIT_CARD),
                    eq(new BigDecimal("99.99")),
                    detailsCaptor.capture(),
                    any(Integer.class)
            );

            PaymentDetails capturedDetails = detailsCaptor.getValue();
            assertThat(capturedDetails.getCardNumber()).isEqualTo("4111111111111111");
            assertThat(capturedDetails.getCardHolderName()).isEqualTo("John Doe");
            assertThat(capturedDetails.getExpiryDate()).isEqualTo("12/25");
            assertThat(capturedDetails.getCvv()).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("getPaymentByOrderId tests")
    class GetPaymentByOrderIdTests {

        @Test
        @DisplayName("Should return payment when found")
        void getPaymentByOrderId_WhenFound_ReturnsPayment() {
            // given
            PaymentResponseDto expectedResponse = new PaymentResponseDto(
                    1, 1, new BigDecimal("99.99"), PaymentStatus.COMPLETED,
                    PaymentMethod.CREDIT_CARD, "TX-123", null
            );

            when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.of(testPayment));
            when(paymentMapper.mapToDto(testPayment)).thenReturn(expectedResponse);

            // when
            PaymentResponseDto result = paymentService.getPaymentByOrderId(1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw exception when payment not found")
        void getPaymentByOrderId_WhenNotFound_ThrowsException() {
            // given
            when(paymentRepository.findByOrderOrderId(999)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentByOrderId(999))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("Payment not found");
        }
    }

    @Nested
    @DisplayName("retryFailedPayment tests")
    class RetryFailedPaymentTests {

        @Test
        @DisplayName("Should retry failed payment successfully")
        void retryFailedPayment_WithFailedPayment_RetriesSuccessfully() {
            // given
            testPayment.setStatus(PaymentStatus.FAILED);
            String newTransactionId = UUID.randomUUID().toString();
            PaymentResponseDto expectedResponse = new PaymentResponseDto(
                    1, 1, new BigDecimal("99.99"), PaymentStatus.COMPLETED,
                    PaymentMethod.CREDIT_CARD, newTransactionId, null
            );

            when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));
            when(paymentGateway.processPayment(any(), any(), any(), any())).thenReturn(new PaymentGateway.PaymentResult(newTransactionId));
            when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(expectedResponse);

            // when
            PaymentResponseDto result = paymentService.retryFailedPayment(1);

            // then
            assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
            verify(paymentGateway).processPayment(any(), any(), any(), any());
            verify(notificationService).sendOrderPaidNotification(testOrder);
        }

        @Test
        @DisplayName("Should throw exception when payment is not failed")
        void retryFailedPayment_WhenNotFailed_ThrowsException() {
            // given
            testPayment.setStatus(PaymentStatus.COMPLETED);
            when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));

            // when & then
            assertThatThrownBy(() -> paymentService.retryFailedPayment(1))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Can only retry failed payments");
        }

        @Test
        @DisplayName("Should throw exception when order is not in NEW status for retry")
        void retryFailedPayment_WhenOrderNotNew_ThrowsException() {
            // given
            testPayment.setStatus(PaymentStatus.FAILED);
            testOrder.setOrderStatus(OrderStatus.PROCESSING);
            when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));

            // when & then
            assertThatThrownBy(() -> paymentService.retryFailedPayment(1))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Order is not in NEW status");
        }

        @Test
        @DisplayName("Should mark payment as failed when retry fails")
        void retryFailedPayment_WhenRetryFails_MarksPaymentAsFailed() {
            // given
            testPayment.setStatus(PaymentStatus.FAILED);
            when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));
            when(paymentGateway.processPayment(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Payment declined again"));

            // when & then
            assertThatThrownBy(() -> paymentService.retryFailedPayment(1))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("Payment retry failed");

            verify(notificationService, never()).sendOrderPaidNotification(any());
        }
    }

    @Nested
    @DisplayName("isOrderPaid tests")
    class IsOrderPaidTests {

        @Test
        @DisplayName("Should return true when order is paid")
        void isOrderPaid_WhenPaid_ReturnsTrue() {
            // given
            Order paidOrder = mock(Order.class);
            when(paidOrder.isPaid()).thenReturn(true);
            when(orderRepository.findById(1)).thenReturn(Optional.of(paidOrder));

            // when
            boolean result = paymentService.isOrderPaid(1);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when order is not paid")
        void isOrderPaid_WhenNotPaid_ReturnsFalse() {
            // given
            Order unpaidOrder = mock(Order.class);
            when(unpaidOrder.isPaid()).thenReturn(false);
            when(orderRepository.findById(1)).thenReturn(Optional.of(unpaidOrder));

            // when
            boolean result = paymentService.isOrderPaid(1);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void isOrderPaid_WhenOrderNotFound_ThrowsException() {
            // given
            when(orderRepository.findById(999)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.isOrderPaid(999))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }
}