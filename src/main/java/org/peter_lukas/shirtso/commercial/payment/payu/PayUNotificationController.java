package org.peter_lukas.shirtso.commercial.payment.payu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.commercial.payment.PaymentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payu")
@ConditionalOnProperty(name = "app.payu.enabled", havingValue = "true")
public class PayUNotificationController {

    private final PaymentService paymentService;

    public PayUNotificationController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/notify")
    @Transactional
    public ResponseEntity<Void> handleNotification(@RequestBody PayUNotification notification) {

        if (notification == null || notification.order() == null) {
            log.warn("Received invalid PayU notification: missing order payload");
            return ResponseEntity.ok().build();
        }

        PayUOrder payuOrder = notification.order();
        String transactionId = payuOrder.orderId();       // PayU orderId = transactionId
        String extOrderId = payuOrder.extOrderId();       // your local orderId
        String status = payuOrder.status();

        log.info("Received PayU notification: payuOrderId={}, extOrderId={}, status={}",
                transactionId, extOrderId, status);

        try {
            paymentService.updatePaymentStatusFromPayU(
                    transactionId,
                    extOrderId,
                    status
            );
        } catch (Exception e) {
            log.error("Error processing PayU notification: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // ----- PayU DTOs -----

    public record PayUNotification(
            PayUOrder order,
            @JsonProperty("localReceiptDateTime") String localReceiptDateTime,
            List<PayUProperty> properties
    ) { }

    public record PayUOrder(
            String orderId,      // PayU order ID (transactionId)
            String extOrderId,   // your orderId
            String orderCreateDate,
            String notifyUrl,
            String customerIp,
            String merchantPosId,
            String description,
            String currencyCode,
            String totalAmount,
            String status
    ) { }

    public record PayUProperty(
            String name,
            String value
    ) { }
}
