package org.peter_lukas.shirtso.commercial.payment;

import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.commercial.order.Order;
import org.peter_lukas.shirtso.commercial.order.OrderRepository;
import org.peter_lukas.shirtso.commercial.order.OrderStatus;
import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.peter_lukas.shirtso.commercial.payment.dto.ProcessPaymentRequestDto;
import org.peter_lukas.shirtso.commercial.product.validation.OrderNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.PaymentException;
import org.peter_lukas.shirtso.messages.Alerts;
import org.peter_lukas.shirtso.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.peter_lukas.shirtso.messages.Alerts.*;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          PaymentMapper paymentMapper,
                          PaymentGateway paymentGateway,
                          NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponseDto processPayment(ProcessPaymentRequestDto request) {
        log.info("Processing payment for order ID: {}", request.orderId());
        Order order = orderRepository.findByOrderIdWithItems(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(Alerts.ORDER_NOT_FOUND));
        if (order.getOrderStatus() != OrderStatus.NEW) {
            log.warn("Cannot process payment for order {} with status {}",
                    order.getOrderId(), order.getOrderStatus());
            throw new PaymentException(PAYMENT_FAILED_ORDER_STATUS);
        }
        if (paymentRepository.findByOrderOrderId(order.getOrderId()).isPresent()) {
            log.warn("Payment already exists for order {}", order.getOrderId());
            throw new PaymentException(PAYMENT_ALREADY_EXISTS);
        }
        Payment payment = new Payment(order, order.getTotalAmount(), request.paymentMethod());
        paymentRepository.save(payment);
        log.info("Payment record created with ID: {}", payment.getPaymentId());

        try {
            PaymentGateway.PaymentResult result = paymentGateway.processPayment(
                    request.paymentMethod(),
                    order.getTotalAmount(),
                    PaymentDetails.builder()
                            .cardNumber(request.cardNumber())
                            .cardHolderName(request.cardHolderName())
                            .expiryDate(request.expiryDate())
                            .cvv(request.cvv())
                            .build(),
                    order.getOrderId()
            );

            // Check if this is a redirect-based payment (PayU)
            if (result.requiresRedirect()) {
                // Payment initiated but not yet completed - user needs to be redirected
                payment.setStatus(PaymentStatus.PENDING);
                payment.setTransactionId(result.transactionId());
                paymentRepository.save(payment);

                log.info("PayU payment initiated for order {}, redirect required to: {}",
                        order.getOrderId(), result.redirectUrl());

                // Return response with redirect URL
                return new PaymentResponseDto(
                        payment.getPaymentId(),
                        order.getOrderId(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getPaymentMethod(),
                        result.transactionId(),
                        payment.getPaymentDate(),
                        result.redirectUrl()
                );
            }
            // Direct payment completed successfully (mock gateway)
            payment.markAsPaid(result.transactionId());
            order.setOrderStatus(OrderStatus.PROCESSING);
            paymentRepository.save(payment);
            orderRepository.save(order);
            log.info("Payment processed successfully for order {} with transaction ID {}",
                    order.getOrderId(), result.transactionId());
            notificationService.sendOrderPaidNotification(order);
            return paymentMapper.mapToDto(payment);
        } catch (Exception e) {
            log.error("Payment processing failed for order {} with error: {}",
                    order.getOrderId(), e.getMessage());
            payment.markAsFailed(e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentException(PAYMENT_FAILED + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Integer orderId) {
        log.debug("Fetching payment for order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Payment not found for order " + orderId));

        return paymentMapper.mapToDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Integer paymentId) {
        log.debug("Fetching payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        return paymentMapper.mapToDto(payment);
    }

    @Transactional
    public PaymentResponseDto retryFailedPayment(Integer paymentId) {
        log.info("Retrying failed payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new PaymentException("Can only retry failed payments");
        }

        Order order = payment.getOrder();

        if (order.getOrderStatus() != OrderStatus.NEW) {
            throw new PaymentException("Order is not in NEW status");
        }

        try {
            // Retry payment through gateway
            PaymentGateway.PaymentResult result = paymentGateway.processPayment(
                    payment.getPaymentMethod(),
                    payment.getAmount(),
                    PaymentDetails.builder()
                            .build(), // Note: Card details should be securely stored or re-entered
                    order.getOrderId()
            );

            // Check if redirect is required
            if (result.requiresRedirect()) {
                payment.setStatus(PaymentStatus.PENDING);
                payment.setTransactionId(result.transactionId());
                paymentRepository.save(payment);

                log.info("Payment retry initiated, redirect required");

                return new PaymentResponseDto(
                        payment.getPaymentId(),
                        order.getOrderId(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getPaymentMethod(),
                        result.transactionId(),
                        payment.getPaymentDate(),
                        result.redirectUrl()
                );
            }

            // Mark payment as successful
            payment.markAsPaid(result.transactionId());
            payment.setStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.PROCESSING);

            paymentRepository.save(payment);
            orderRepository.save(order);

            log.info("Payment retry successful. Transaction ID: {}", result.transactionId());

            // Send notification
            notificationService.sendOrderPaidNotification(order);

            return paymentMapper.mapToDto(payment);

        } catch (Exception e) {
            log.error("Payment retry failed: {}", e.getMessage());
            payment.markAsFailed(e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentException("Payment retry failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public boolean isOrderPaid(Integer orderId) {
        log.debug("Checking if order {} is paid", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for ID: " + orderId));

        boolean paid = order.isPaid();

        log.debug("Order {} payment status: {}", orderId, paid ? "PAID" : "NOT PAID");

        return paid;
    }

    @Transactional
    public void updatePaymentStatusFromPayU(String transactionId,
                                            String extOrderId,
                                            String payuStatus) {

        Integer localOrderId = null;
        try {
            localOrderId = Integer.valueOf(extOrderId);
        } catch (Exception e) {
            log.error("Invalid extOrderId '{}', cannot parse to Integer", extOrderId);
            return;
        }

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElse(null);

        if (payment == null) {
            log.warn("No payment found for PayU transactionId={}", transactionId);
            return;
        }

        Order order = payment.getOrder();

        if (!order.getOrderId().equals(localOrderId)) {
            log.error("Mismatch extOrderId={} but payment.orderId={}", extOrderId, order.getOrderId());
            return;
        }

        log.info("Updating payment {} for order {} based on PayU status {}",
                payment.getPaymentId(), order.getOrderId(), payuStatus);

        switch (payuStatus) {

            case "COMPLETED" -> {
                if (payment.getStatus() != PaymentStatus.COMPLETED) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    order.setOrderStatus(OrderStatus.PROCESSING);

                    notificationService.sendOrderPaidNotification(order);

                    log.info("Order {} marked as PAID based on PayU COMPLETED", order.getOrderId());
                } else {
                    log.info("Payment {} already COMPLETED — ignoring duplicate", payment.getPaymentId());
                }
            }

            case "PENDING" -> {
                if (payment.getStatus() == PaymentStatus.NEW) {
                    payment.setStatus(PaymentStatus.PENDING);
                }
                log.info("Payment {} set to PENDING", payment.getPaymentId());
            }

            case "CANCELED", "REJECTED" -> {
                if (payment.getStatus() != PaymentStatus.COMPLETED) {
                    payment.setStatus(PaymentStatus.FAILED);
                    log.info("Payment {} marked FAILED due to PayU status {}", payment.getPaymentId(), payuStatus);
                } else {
                    log.info("Ignoring FAILED status because payment is already COMPLETED");
                }
            }

            default -> log.warn("Unknown PayU status received: {}", payuStatus);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    @Transactional
    public PaymentResponseDto refundPayment(Integer paymentId) {
        log.info("Processing refund for payment {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Only completed payments can be refunded");
        }

        Order order = payment.getOrder();

        try {
            boolean success = paymentGateway.refundPayment(
                    payment.getTransactionId(),
                    payment.getAmount()
            );

            if (!success) {
                log.error("Refund failed for payment {}", paymentId);
                throw new PaymentException("Refund operation rejected by gateway");
            }

            // Mark refunded
            payment.setStatus(PaymentStatus.REFUNDED);
            order.setOrderStatus(OrderStatus.REFUNDED);

            paymentRepository.save(payment);
            orderRepository.save(order);

            log.info("Refund successful for payment {}", paymentId);

            notificationService.sendOrderRefundedNotification(order);

            return paymentMapper.mapToDto(payment);

        } catch (Exception e) {
            log.error("Refund exception: {}", e.getMessage());
            throw new PaymentException("Refund failed: " + e.getMessage());
        }
    }
}

//TODO: exceptions and messages
