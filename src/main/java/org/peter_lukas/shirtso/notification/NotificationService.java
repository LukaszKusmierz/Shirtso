package org.peter_lukas.shirtso.notification;

import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.commercial.order.Order;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendOrderConfirmationNotification(Order order) {
        User user = order.getUser();
        String subject = "Order Confirmation - Order #" + order.getOrderId();
        String message = buildOrderConfirmationEmail(order);
        emailService.sendEmail(user.getEmail(), subject, message);
    }

    public void sendOrderPaidNotification(Order order) {
        User user = order.getUser();
        String subject = "Payment Confirmation - Order #" + order.getOrderId();
        String message = buildPaymentConfirmationEmail(order);
        emailService.sendEmail(user.getEmail(), subject, message);
    }

    public void sendOrderStatusChangeNotification(Order order, String previousStatus) {
        User user = order.getUser();
        String subject = "Order Status Update - Order #" + order.getOrderId();
        String message = buildOrderStatusChangeEmail(order, previousStatus);
        emailService.sendEmail(user.getEmail(), subject, message);
    }

    private String buildOrderConfirmationEmail(Order order) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(order.getUser().getUserName()).append(",\n\n");
        emailBody.append("Thank you for your order. Your order #").append(order.getOrderId())
                .append(" has been received and is currently being processed.\n\n");
        emailBody.append("Order Details:\n");
        emailBody.append("Date: ").append(order.getCreatedAt()).append("\n");
        emailBody.append("Total Amount: ").append(order.getTotalAmount()).append(" ").append(order.getCurrency()).append("\n\n");
        emailBody.append("Items:\n");
        order.getItems().forEach(item -> {
            emailBody.append("- ").append(item.getProduct().getProductName())
                    .append(" (Qty: ").append(item.getQuantity())
                    .append(") - ").append(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                    .append(" ").append(order.getCurrency())
                    .append("\n");
        });
        emailBody.append("\nPlease proceed to payment to complete your order.\n\n");
        emailBody.append("Thank you for shopping with us!\n");
        emailBody.append("Shirtso Team");
        return emailBody.toString();
    }

    private String buildPaymentConfirmationEmail(Order order) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(order.getUser().getUserName()).append(",\n\n");
        emailBody.append("We're happy to confirm that your payment for order #").append(order.getOrderId())
                .append(" has been successfully processed.\n\n");
        emailBody.append("Order Details:\n");
        emailBody.append("Date: ").append(order.getCreatedAt()).append("\n");
        emailBody.append("Total Amount: ").append(order.getTotalAmount()).append(" ").append(order.getCurrency()).append("\n\n");
        emailBody.append("Your order is now being prepared for shipping. You will receive another notification when your order ships.\n\n");
        emailBody.append("Thank you for shopping with us!\n");
        emailBody.append("Shirtso Team");
        return emailBody.toString();
    }

    private String buildOrderStatusChangeEmail(Order order, String previousStatus) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(order.getUser().getUserName()).append(",\n\n");
        emailBody.append("The status of your order #").append(order.getOrderId())
                .append(" has been updated from ").append(previousStatus)
                .append(" to ").append(order.getOrderStatus()).append(".\n\n");
        switch (order.getOrderStatus()) {
            case SHIPPED:
                emailBody.append("Your order is on its way! You should receive your items within the next few business days.\n");
                break;
            case DELIVERED:
                emailBody.append("Your order has been delivered. We hope you enjoy your purchase!\n");
                break;
            case CANCELLED:
                emailBody.append("Your order has been cancelled. If you did not request this cancellation, please contact our customer support.\n");
                break;
            default:
                emailBody.append("If you have any questions about your order, please contact our customer support.\n");
        }
        emailBody.append("\nThank you for shopping with us!\n");
        emailBody.append("Shirtso Team");
        return emailBody.toString();
    }
}
