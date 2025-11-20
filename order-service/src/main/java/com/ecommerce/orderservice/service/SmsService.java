package com.ecommerce.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final RestTemplate restTemplate;

    @Value("${brevo.sms.apiKey}")
    private String apiKey;

    @Value("${brevo.sms.url}")
    private String smsUrl;

    @Value("${brevo.sms.sender}")
    private String sender;

//    @Value("${brevo.sms.sendermobile}")
//    private String sendermobile;


    @Value("${brevo.sms.type}")
    private String smsType;

    @Value("${brevo.sms.tag}")
    private String smsTag;

    @Value("${brevo.sms.callbackUrl:}")
    private String callbackUrl;

    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends an order confirmation SMS via Brevo Transactional SMS API.
     */
    public void sendOrderConfirmationSms(
            String phoneNumber,
            Long orderId,
            BigDecimal totalAmount,
            String status
    ) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            logger.warn("No phone number provided for order {}, skipping SMS", orderId);
            return;
        }

        try {
            // SMS text (keep it short; 160 chars per SMS segment)
            String content = String.format(
                    "Hi! Your order #%d is %s. Total: %s. Thank you!",
                    orderId,
                    status,
                    totalAmount
            );

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("recipient", phoneNumber);  // e.g. 91XXXXXXXXXX (check Brevo format for your country)
            body.put("content", content);
            body.put("type", smsType);          // transactional / marketing
            body.put("tag", smsTag);

            if (callbackUrl != null && !callbackUrl.isBlank()) {
                body.put("webUrl", callbackUrl);  // Brevo will call this URL with SMS events :contentReference[oaicite:1]{index=1}
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");
            headers.set("api-key", apiKey);      // Brevo auth header :contentReference[oaicite:2]{index=2}

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(smsUrl, request, String.class);

            logger.info("Brevo SMS response for order {}: status={}, body={}",
                    orderId, response.getStatusCode(), response.getBody());

        } catch (Exception e) {
            logger.error("Error while sending SMS for order {}", orderId, e);
            // You can throw a runtime exception here to let RabbitMQ retry if you configure retries
        }
    }
}
