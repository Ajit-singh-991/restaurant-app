package com.restaurant.service;

import com.restaurant.entity.Invoice;
import com.restaurant.entity.Order;
import com.restaurant.entity.Payment;
import com.restaurant.repository.InvoiceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReceiptEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    public ReceiptEmailService(InvoiceService invoiceService, InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
    }

    @Value("${app.mail.from:noreply@restaurant.com}")
    private String fromAddress;

    /**
     * Send receipt email with PDF attachment when mail is enabled and recipient is provided.
     */
    public void sendReceiptIfEnabled(Order order, Payment payment, String recipientEmail) {
        if (!enabled || mailSender == null || recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        try {
            Invoice invoice = invoiceRepository.findByOrderId(order.getId()).orElse(null);
            if (invoice == null) {
                return;
            }
            byte[] pdfBytes = invoiceService.getInvoicePdfBytes(invoice.getId());
            String subject = "Your receipt – Order " + order.getOrderNumber();
            String body = "Thank you for your order. Please find your invoice attached.";
            sendWithAttachment(recipientEmail, subject, body, pdfBytes, "invoice-" + order.getOrderNumber() + ".pdf");
        } catch (Exception e) {
            log.warn("Failed to send receipt email: {}", e.getMessage());
        }
    }

    private void sendWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName) throws MessagingException {
        if (mailSender == null) return;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
        mailSender.send(message);
    }
}
