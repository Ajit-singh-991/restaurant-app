package com.restaurant.controller;

import com.restaurant.entity.Invoice;
import com.restaurant.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Invoice> getInvoiceByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.getInvoiceByOrderId(orderId));
    }

    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdf = invoiceService.getInvoicePdfBytes(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
