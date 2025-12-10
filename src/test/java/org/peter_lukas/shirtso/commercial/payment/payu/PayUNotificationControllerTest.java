package org.peter_lukas.shirtso.commercial.payment.payu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.payment.PaymentService;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayUNotificationControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PayUNotificationController controller;

    private PayUNotificationController.PayUOrder payuOrder;
    private PayUNotificationController.PayUNotification notification;

    @BeforeEach
    void setUp() {
        payuOrder = new PayUNotificationController.PayUOrder(
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

        notification = new PayUNotificationController.PayUNotification(
                payuOrder,
                "2024-01-15T10:00:00",
                Collections.emptyList()
        );
    }

    @Test
    void handleNotification_CallsPaymentServiceUpdate() {
        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(paymentService).updatePaymentStatusFromPayU(
                "PAYU-ORDER-123",
                "EXT-123",
                "COMPLETED"
        );
    }

    @Test
    void handleNotification_WithNullNotification_DoesNotCallService() {
        // when
        ResponseEntity<Void> response = controller.handleNotification(null);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verifyNoInteractions(paymentService);
    }

    @Test
    void handleNotification_WithNullOrder_DoesNotCallService() {
        // given
        PayUNotificationController.PayUNotification invalidNotification =
                new PayUNotificationController.PayUNotification(null, "2024-01-15T10:00:00", Collections.emptyList());

        // when
        ResponseEntity<Void> response = controller.handleNotification(invalidNotification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verifyNoInteractions(paymentService);
    }

    @Test
    void handleNotification_WhenServiceThrows_ExceptionIsCaught() {
        // given
        doThrow(new RuntimeException("Service error")).when(paymentService)
                .updatePaymentStatusFromPayU(anyString(), anyString(), anyString());

        // when
        ResponseEntity<Void> response = controller.handleNotification(notification);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(paymentService).updatePaymentStatusFromPayU("PAYU-ORDER-123", "EXT-123", "COMPLETED");
    }

    @Test
    void handleNotification_WithProperties_CallsServiceCorrectly() {
        // given
        List<PayUNotificationController.PayUProperty> properties = List.of(
                new PayUNotificationController.PayUProperty("PAYMENT_ID", "12345"),
                new PayUNotificationController.PayUProperty("CARD_SCHEME", "VS")
        );
        PayUNotificationController.PayUNotification notificationWithProps =
                new PayUNotificationController.PayUNotification(payuOrder, "2024-01-15T10:00:00", properties);

        // when
        ResponseEntity<Void> response = controller.handleNotification(notificationWithProps);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(paymentService).updatePaymentStatusFromPayU(
                "PAYU-ORDER-123",
                "EXT-123",
                "COMPLETED"
        );
    }
}
