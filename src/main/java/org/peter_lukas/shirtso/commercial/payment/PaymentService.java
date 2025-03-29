package org.peter_lukas.shirtso.commercial.payment;

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
        Order order = orderRepository.findByOrderIdWithItems(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(Alerts.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.NEW) {
            throw new PaymentException(PAYMENT_FAILED_ORDER_STATUS);
        }

        if (paymentRepository.findByOrderOrderId(order.getOrderId()).isPresent()) {
            throw new PaymentException(PAYMENT_ALREADY_EXISTS);
        }

        Payment payment = new Payment(order, order.getTotalAmount(), request.paymentMethod());
        paymentRepository.save(payment);

        try {
            String transactionId = paymentGateway.processPayment(
                    request.paymentMethod(),
                    order.getTotalAmount(),
                    PaymentDetails.builder()
                            .cardNumber(request.cardNumber())
                            .cardHolderName(request.cardHolderName())
                            .expiryDate(request.expiryDate())
                            .cvv(request.cvv())
                            .build()
            );

            payment.markAsPaid(transactionId);
            order.setOrderStatus(OrderStatus.PROCESSING);
            paymentRepository.save(payment);
            orderRepository.save(order);
            notificationService.sendOrderPaidNotification(order);
            return paymentMapper.mapToDto(payment);
        } catch (Exception e) {
            payment.markAsFailed(e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentException(PAYMENT_FAILED + e.getMessage());
        }
    }
}
