package com.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.entity.*;
import com.restaurant.repository.InvoiceRepository;
import com.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final AtomicLong invoiceCounter = new AtomicLong(0);

    @Transactional
    public Invoice generateInvoice(Long orderId, User generatedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        invoiceRepository.findByOrderId(orderId).ifPresent(inv -> {
            throw new IllegalArgumentException("Invoice already exists for this order");
        });

        String invoiceNumber = "INV-" + System.currentTimeMillis() + "-" + invoiceCounter.incrementAndGet();
        String billToName = order.getCustomer() != null ? order.getCustomer().getUsername() : "Guest";
        String billToContact = order.getCustomer() != null ? order.getCustomer().getEmail() : null;

        List<Map<String, Object>> itemsData = order.getItems().stream()
                .map(oi -> Map.<String, Object>of(
                        "name", oi.getMenuItem().getName(),
                        "quantity", oi.getQuantity(),
                        "unitPrice", oi.getUnitPrice(),
                        "totalPrice", oi.getTotalPrice() != null ? oi.getTotalPrice() : oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                )
                .collect(Collectors.toList());
        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(itemsData);
        } catch (JsonProcessingException e) {
            itemsJson = "[]";
        }

        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNumber(invoiceNumber)
                .billToName(billToName)
                .billToContact(billToContact)
                .itemsJson(itemsJson)
                .subtotal(order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO)
                .taxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO)
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .generatedBy(generatedBy)
                .build();

        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found for order"));
    }

    @Transactional(readOnly = true)
    public byte[] getInvoicePdfBytes(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        Order order = invoice.getOrder();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph("INVOICE", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber(), normalFont));
            document.add(new Paragraph("Date: " + invoice.getGeneratedAt().format(INVOICE_DATE_FORMAT), normalFont));
            document.add(new Paragraph("Order #: " + order.getOrderNumber(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Bill To:", smallFont));
            document.add(new Paragraph(invoice.getBillToName(), normalFont));
            if (invoice.getBillToContact() != null) {
                document.add(new Paragraph(invoice.getBillToContact(), smallFont));
            }
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{3f, 1f, 2f, 2f});
            table.addCell(createCell("Item", Element.ALIGN_LEFT, true));
            table.addCell(createCell("Qty", Element.ALIGN_RIGHT, true));
            table.addCell(createCell("Unit Price", Element.ALIGN_RIGHT, true));
            table.addCell(createCell("Total", Element.ALIGN_RIGHT, true));

            for (OrderItem oi : order.getItems()) {
                table.addCell(createCell(oi.getMenuItem().getName(), Element.ALIGN_LEFT, false));
                table.addCell(createCell(String.valueOf(oi.getQuantity()), Element.ALIGN_RIGHT, false));
                table.addCell(createCell(oi.getUnitPrice().toString(), Element.ALIGN_RIGHT, false));
                BigDecimal lineTotal = oi.getTotalPrice() != null ? oi.getTotalPrice() : oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
                table.addCell(createCell(lineTotal.toString(), Element.ALIGN_RIGHT, false));
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Subtotal: " + invoice.getSubtotal(), normalFont));
            document.add(new Paragraph("Tax: " + invoice.getTaxAmount(), normalFont));
            document.add(new Paragraph("Total: " + invoice.getTotalAmount(), titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Thank you for your order!", smallFont));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Failed to generate invoice PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private PdfPCell createCell(String text, int alignment, boolean header) {
        Font font = header ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9) : FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        return cell;
    }
}
