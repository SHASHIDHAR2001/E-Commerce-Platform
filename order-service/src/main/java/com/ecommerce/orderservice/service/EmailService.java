package com.ecommerce.orderservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from.address}")
    private String fromAddress;

    @Value("${app.mail.from.name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmationEmail(
            String toEmail,
            Long orderId,
            BigDecimal totalAmount,
            String status,
            List<Map<String, Object>> items
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setFrom(fromAddress, fromName);
            helper.setSubject("Order Confirmation - Order #" + orderId);

            StringBuilder html = new StringBuilder();
            html.append("<h2>Thank you for your order!</h2>");
            html.append("<p>Your order <strong>#").append(orderId)
                    .append("</strong> is currently <strong>").append(status).append("</strong>.</p>");
            html.append("<p>Total Amount: <strong>").append(totalAmount).append("</strong></p>");

            if (items != null && !items.isEmpty()) {
                html.append("<h3>Items:</h3>");
                html.append("<ul>");
                for (Map<String, Object> item : items) {
                    html.append("<li>")
                            .append("Product ID: ").append(item.get("productId"))
                            .append(", Quantity: ").append(item.get("quantity"))
                            .append(", Price: ").append(item.get("price"))
                            .append("</li>");
                }
                html.append("</ul>");
            }

            helper.setText(html.toString(), true);

            mailSender.send(message);
            logger.info("Order confirmation email sent to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Error while building order confirmation email", e);
            // you can throw here to trigger RabbitMQ retry if configured
        } catch (Exception e) {
            logger.error("Error while sending order confirmation email", e);
        }
    }
}
