package org.peter_lukas.shirtso.commercial.payment.payu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.order.Order;
import org.peter_lukas.shirtso.commercial.order.OrderRepository;
import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.payment.Payment;
import org.peter_lukas.shirtso.commercial.payment.PaymentMethod;
import org.peter_lukas.shirtso.commercial.payment.PaymentRepository;
import org.peter_lukas.shirtso.commercial.payment.PaymentStatus;
import org.peter_lukas.shirtso.notification.NotificationService;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayUNotificationControllerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PayUNotificationController controller;

    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
        testPayment = createTestPayment();
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
        payment.setTransactionId("PAYU-ORDER-123");
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    @Test
    void handleNotification_WithCompletedStatus_UpdatesPaymentAndOrder() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "COMPLETED"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);

        verify(paymentRepository).save(testPayment);
        verify(orderRepository).save(testOrder);
        verify(notificationService).sendOrderPaidNotification(testOrder);
    }

    @Test
    void handleNotification_WithCanceledStatus_MarksPaymentAsFailed() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "CANCELED"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(paymentRepository).save(testPayment);
        verify(orderRepository).save(testOrder);
        verify(notificationService, never()).sendOrderPaidNotification(any());
    }

    @Test
    void handleNotification_WithRejectedStatus_MarksPaymentAsFailed() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "REJECTED"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(paymentRepository).save(testPayment);
        verify(notificationService, never()).sendOrderPaidNotification(any());
    }

    @Test
    void handleNotification_WithPendingStatus_DoesNotChangeStatus() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "PENDING"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(testPayment);
        verify(notificationService, never()).sendOrderPaidNotification(any());
    }

    @Test
    void handleNotification_WithUnknownPayUOrderId_ReturnsOkWithoutUpdating() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "UNKNOWN-ORDER-ID",
                "EXT-999",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "COMPLETED"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        // Payment status should not change
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verify(notificationService, never()).sendOrderPaidNotification(any());
    }

    @Test
    void handleNotification_WithEmptyPaymentList_ReturnsOk() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "COMPLETED"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleNotification_WithUnknownStatus_DoesNotCrash() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "UNKNOWN_STATUS"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        // Status should remain unchanged
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(testPayment);
    }

    @Test
    void handleNotification_WithMultiplePayments_FindsCorrectOne() {
        // given
        Payment otherPayment = new Payment(testOrder, new BigDecimal("50.00"), PaymentMethod.PAYPAL);
        otherPayment.setPaymentId(2);
        otherPayment.setTransactionId("OTHER-ORDER-456");
        otherPayment.setStatus(PaymentStatus.PENDING);

        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "COMPLETED"
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );

        when(paymentRepository.findAll()).thenReturn(List.of(otherPayment, testPayment));

        // when
        controller.handleNotification(notification);

        // then
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(otherPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getTransactionId()).isEqualTo("PAYU-ORDER-123");
    }

    @Test
    void handleNotification_WithProperties_ProcessesCorrectly() {
        // given
        PayUNotificationController.PayUOrder payuOrder = new PayUNotificationController.PayUOrder(
                "PAYU-ORDER-123",
                "EXT-123",
                "2024-01-15T10:00:00",
                "https://notify.url",
                "127.0.0.1",
                "300746",
                "Order payment",
                "PLN",
                "9999",
                "COMPLETED"
        );

        List<PayUNotificationController.PayUProperty> properties = List.of(
                new PayUNotificationController.PayUProperty("PAYMENT_ID", "12345"),
                new PayUNotificationController.PayUProperty("CARD_SCHEME", "VS")
        );

        PayUNotificationController.PayUNotification notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                properties
        );

        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }
}
