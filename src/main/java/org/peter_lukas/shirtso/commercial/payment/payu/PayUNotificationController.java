package org.peter_lukas.shirtso.commercial.payment.payu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.commercial.order.Order;
import org.peter_lukas.shirtso.commercial.order.OrderRepository;
import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.payment.Payment;
import org.peter_lukas.shirtso.commercial.payment.PaymentRepository;
import org.peter_lukas.shirtso.commercial.payment.PaymentStatus;
import org.peter_lukas.shirtso.notification.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/payu")
@ConditionalOnProperty(name = "app.payu.enabled", havingValue = "true")
public class PayUNotificationController {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public PayUNotificationController(PaymentRepository paymentRepository,
                                      OrderRepository orderRepository,
                                      NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/notify")
    @Transactional
    public ResponseEntity<Void> handleNotification(@RequestBody PayUNotification notification) {
        log.info("Received PayU notification: orderId={}, status={}",
                notification.order().orderId(), notification.order().status());

        String payuOrderId = notification.order().orderId();

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(payuOrderId);

        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for PayU orderId: {}", payuOrderId);
            return ResponseEntity.ok().build();
        }

        Payment payment = paymentOpt.get();
        Order order = payment.getOrder();

        String status = notification.order().status();

        switch (status) {
            case "COMPLETED" -> {
                log.info("Payment completed for order: {}", order.getOrderId());
                payment.setStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.PROCESSING);
                notificationService.sendOrderPaidNotification(order);
            }
            case "CANCELED" -> {
                log.info("Payment canceled for order: {}", order.getOrderId());
                payment.setStatus(PaymentStatus.FAILED);
            }
            case "REJECTED" -> {
                log.info("Payment rejected for order: {}", order.getOrderId());
                payment.setStatus(PaymentStatus.FAILED);
            }
            case "PENDING" -> log.info("Payment pending for order: {}", order.getOrderId());
            default -> log.warn("Unknown PayU status: {}", status);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return ResponseEntity.ok().build();
    }

    public record PayUNotification(
            PayUOrder order,
            @JsonProperty("localReceiptDateTime") String localReceiptDateTime,
            List<PayUProperty> properties
    ) {
    }

    public record PayUOrder(
            String orderId,
            String extOrderId,
            String orderCreateDate,
            String notifyUrl,
            String customerIp,
            String merchantPosId,
            String description,
            String currencyCode,
            String totalAmount,
            String status
    ) {
    }

    public record PayUProperty(
            String name,
            String value
    ) {
    }
}
