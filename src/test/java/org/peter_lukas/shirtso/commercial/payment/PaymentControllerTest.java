package org.peter_lukas.shirtso.commercial.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.commercial.payment.dto.PaymentResponseDto;
import org.peter_lukas.shirtso.commercial.payment.dto.ProcessPaymentRequestDto;
import org.peter_lukas.shirtso.commercial.product.validation.OrderNotFoundException;
import org.peter_lukas.shirtso.commercial.product.validation.PaymentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = org.peter_lukas.shirtso.config.WebConfig.class)
})
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private ProcessPaymentRequestDto validRequest;
    private PaymentResponseDto successResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ProcessPaymentRequestDto(
                1,
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "John Doe",
                "12/25",
                "123"
        );

        successResponse = new PaymentResponseDto(
                1,
                1,
                new BigDecimal("99.99"),
                PaymentStatus.COMPLETED,
                PaymentMethod.CREDIT_CARD,
                UUID.randomUUID().toString(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /api/payments/methods")
    class GetPaymentMethodsTests {

        @Test
        @DisplayName("Should return all payment methods")
        void getPaymentMethods_ReturnsAllMethods() throws Exception {
            mockMvc.perform(get("/api/payments/methods"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(PaymentMethod.values().length)))
                    .andExpect(jsonPath("$", containsInAnyOrder(
                            Stream.of(PaymentMethod.values())
                            .map(Enum::name)
                            .toArray(String[]::new)
                    )));
        }
    }

    @Nested
    @DisplayName("POST /api/payments/process")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should process payment successfully")
        void processPayment_WithValidRequest_ReturnsOk() throws Exception {
            when(paymentService.processPayment(any(ProcessPaymentRequestDto.class)))
                    .thenReturn(successResponse);

            mockMvc.perform(post("/api/payments/process")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1))
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void processPayment_WhenOrderNotFound_Returns404() throws Exception {
            when(paymentService.processPayment(any(ProcessPaymentRequestDto.class)))
                    .thenThrow(new OrderNotFoundException("Order not found"));

            mockMvc.perform(post("/api/payments/process")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when payment fails")
        void processPayment_WhenPaymentFails_Returns400() throws Exception {
            when(paymentService.processPayment(any(ProcessPaymentRequestDto.class)))
                    .thenThrow(new PaymentException("Payment failed"));

            mockMvc.perform(post("/api/payments/process")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when order ID is null")
        void processPayment_WithNullOrderId_Returns400() throws Exception {
            ProcessPaymentRequestDto invalidRequest = new ProcessPaymentRequestDto(
                    null, PaymentMethod.CREDIT_CARD, null, null, null, null
            );

            mockMvc.perform(post("/api/payments/process")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when payment method is null")
        void processPayment_WithNullPaymentMethod_Returns400() throws Exception {
            ProcessPaymentRequestDto invalidRequest = new ProcessPaymentRequestDto(
                    1, null, null, null, null, null
            );

            mockMvc.perform(post("/api/payments/process")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/payments/order/{orderId}")
    class GetPaymentByOrderIdTests {

        @Test
        @DisplayName("Should return payment for order")
        void getPaymentByOrderId_WhenFound_ReturnsOk() throws Exception {
            when(paymentService.getPaymentByOrderId(1)).thenReturn(successResponse);

            mockMvc.perform(get("/api/payments/order/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Should return 404 when payment not found")
        void getPaymentByOrderId_WhenNotFound_Returns404() throws Exception {
            when(paymentService.getPaymentByOrderId(999))
                    .thenThrow(new OrderNotFoundException("Payment not found"));

            mockMvc.perform(get("/api/payments/order/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/payments/{paymentId}")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("Should return payment by ID")
        void getPaymentById_WhenFound_ReturnsOk() throws Exception {
            when(paymentService.getPaymentById(1)).thenReturn(successResponse);

            mockMvc.perform(get("/api/payments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1));
        }

        @Test
        @DisplayName("Should return 404 when payment not found by ID")
        void getPaymentById_WhenNotFound_Returns404() throws Exception {
            when(paymentService.getPaymentById(999))
                    .thenThrow(new PaymentException("Payment not found"));

            mockMvc.perform(get("/api/payments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/payments/{paymentId}/retry")
    class RetryFailedPaymentTests {

        @Test
        @DisplayName("Should retry failed payment successfully")
        void retryFailedPayment_WhenSuccessful_ReturnsOk() throws Exception {
            when(paymentService.retryFailedPayment(1)).thenReturn(successResponse);

            mockMvc.perform(post("/api/payments/1/retry")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Should return 400 when retry fails")
        void retryFailedPayment_WhenFails_Returns400() throws Exception {
            when(paymentService.retryFailedPayment(1))
                    .thenThrow(new PaymentException("Cannot retry"));

            mockMvc.perform(post("/api/payments/1/retry")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/payments/order/{orderId}/status")
    class IsOrderPaidTests {

        @Test
        @DisplayName("Should return true when order is paid")
        void isOrderPaid_WhenPaid_ReturnsTrue() throws Exception {
            when(paymentService.isOrderPaid(1)).thenReturn(true);

            mockMvc.perform(get("/api/payments/order/1/status"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("Should return false when order is not paid")
        void isOrderPaid_WhenNotPaid_ReturnsFalse() throws Exception {
            when(paymentService.isOrderPaid(1)).thenReturn(false);

            mockMvc.perform(get("/api/payments/order/1/status"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void isOrderPaid_WhenOrderNotFound_Returns404() throws Exception {
            when(paymentService.isOrderPaid(999))
                    .thenThrow(new OrderNotFoundException("Order not found"));

            mockMvc.perform(get("/api/payments/order/999/status"))
                    .andExpect(status().isNotFound());
        }
    }
}
