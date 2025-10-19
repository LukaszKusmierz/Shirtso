package org.peter_lukas.shirtso.commercial.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.order.Order;
import org.peter_lukas.shirtso.commercial.order.OrderRepository;
import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.peter_lukas.shirtso.commercial.payment.dto.ProcessPaymentRequestDto;
import org.peter_lukas.shirtso.commercial.product.validation.OrderNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.PaymentException;
import org.peter_lukas.shirtso.notification.NotificationService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
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
        when(paymentGateway.processPayment(any(), any(), any())).thenReturn(transactionId);
        when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(expectedResponse);

        // when
        PaymentResponseDto result = paymentService.processPayment(paymentRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentGateway).processPayment(
                eq(PaymentMethod.CREDIT_CARD),
                eq(new BigDecimal("99.99")),
                any(PaymentDetails.class)
        );
        verify(notificationService).sendOrderPaidNotification(testOrder);
        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
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
        verify(paymentGateway, never()).processPayment(any(), any(), any());
    }

    @Test
    void processPayment_WhenOrderNotInNewStatus_ThrowsException() {
        // given
        testOrder.setOrderStatus(OrderStatus.PROCESSING);
        when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("NEW status");

        verify(paymentGateway, never()).processPayment(any(), any(), any());
    }

    @Test
    void processPayment_WhenPaymentAlreadyExists_ThrowsException() {
        // given
        when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.of(testPayment));

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("already exists");

        verify(paymentGateway, never()).processPayment(any(), any(), any());
    }

    @Test
    void processPayment_WhenGatewayFails_MarksPaymentAsFailed() {
        // given
        when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentGateway.processPayment(any(), any(), any()))
                .thenThrow(new RuntimeException("Payment declined"));

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Payment processing failed");

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(notificationService, never()).sendOrderPaidNotification(any());
    }

    @Test
    void processPayment_WithDifferentPaymentMethods_ProcessesCorrectly() {
        // given
        ProcessPaymentRequestDto paypalRequest = new ProcessPaymentRequestDto(
                1, PaymentMethod.PAYPAL, null, null, null, null
        );
        String transactionId = UUID.randomUUID().toString();

        when(orderRepository.findByOrderIdWithItems(1)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderOrderId(1)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentGateway.processPayment(any(), any(), any())).thenReturn(transactionId);
        when(paymentMapper.mapToDto(any(Payment.class))).thenReturn(mock(PaymentResponseDto.class));

        // when
        paymentService.processPayment(paypalRequest);

        // then
        verify(paymentGateway).processPayment(
                eq(PaymentMethod.PAYPAL),
                any(),
                any(PaymentDetails.class)
        );
    }
}