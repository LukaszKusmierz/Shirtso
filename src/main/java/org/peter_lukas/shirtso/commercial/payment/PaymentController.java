package org.peter_lukas.shirtso.commercial.payment;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.peter_lukas.shirtso.commercial.payment.dto.ProcessPaymentRequestDto;
import org.peter_lukas.shirtso.commercial.product.validation.OrderNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.PaymentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/methods")
    public List<PaymentMethod> getPaymentMethods() {
        return List.of(PaymentMethod.values());
    }

    @PostMapping("/process")
    @LogExecutionTime
    public ResponseEntity<PaymentResponseDto> processPayment(@Valid @RequestBody ProcessPaymentRequestDto request) {
        try {
            PaymentResponseDto response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/order/{orderId}")
    @LogExecutionTime
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable Integer orderId) {
        try {
            PaymentResponseDto response = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{paymentId}")
    @LogExecutionTime
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Integer paymentId) {
        try {
            PaymentResponseDto response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(response);
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{paymentId}/retry")
    @LogExecutionTime
    public ResponseEntity<PaymentResponseDto> retryFailedPayment(@PathVariable Integer paymentId) {
        try {
            PaymentResponseDto response = paymentService.retryFailedPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/order/{orderId}/status")
    @LogExecutionTime
    public ResponseEntity<Boolean> isOrderPaid(@PathVariable Integer orderId) {
        try {
            boolean isPaid = paymentService.isOrderPaid(orderId);
            return ResponseEntity.ok(isPaid);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{paymentId}/refund")
    @LogExecutionTime
    public ResponseEntity<PaymentResponseDto> refundPayment(@PathVariable Integer paymentId) {
        log.info("Refund request received for payment {}", paymentId);
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
}
