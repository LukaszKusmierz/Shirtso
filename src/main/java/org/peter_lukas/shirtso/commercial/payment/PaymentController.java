package org.peter_lukas.shirtso.commercial.payment;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.peter_lukas.shirtso.commercial.payment.dto.ProcessPaymentRequestDto;
import org.peter_lukas.shirtso.commercial.product.validation.OrderNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.PaymentException;
import org.peter_lukas.shirtso.errorhandling.ErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    @LogExecutionTime
    public ResponseEntity<?> processPayment(@Valid @RequestBody ProcessPaymentRequestDto request) {
        try {
            PaymentResponseDto response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorHandler.ErrorResponse(e.getMessage()));
        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorHandler.ErrorResponse(e.getMessage()));
        }
    }
}
