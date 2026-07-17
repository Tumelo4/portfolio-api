package com.tumelo.portfolio.controller;

import java.util.Base64;

import com.tumelo.portfolio.dto.ContactRequest;
import com.tumelo.portfolio.dto.ContactResponse;
import com.tumelo.portfolio.dto.PortfolioResponse;
import com.tumelo.portfolio.service.FilePayload;
import com.tumelo.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/user/details")
    public ResponseEntity<PortfolioResponse> getPortfolio() {
        return ResponseEntity.ok(portfolioService.getPortfolio());
    }

    @PostMapping("/user/submit")
    public ResponseEntity<ContactResponse> submitContact(
            @Valid @RequestBody ContactRequest request
    ) {
        portfolioService.sendContactEmail(request);
        return ResponseEntity.ok(new ContactResponse("Email sent successfully!"));
    }

    /**
     * Backward-compatible endpoint for the existing Next.js frontend.
     * It returns Base64 text because the current frontend decodes files that way.
     */
    @GetMapping("/retrieve/{fileId}")
    public ResponseEntity<String> retrieveLegacyFile(@PathVariable String fileId) {
        FilePayload file = portfolioService.retrieveFile(fileId);

        return ResponseEntity.ok()
                .header("X-Content-Type", file.mediaType().toString())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(file.filename())
                                .build()
                                .toString()
                )
                .body(Base64.getEncoder().encodeToString(file.content()));
    }

    /**
     * Preferred endpoint for new clients. It returns the original binary file.
     */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> retrieveFile(@PathVariable String fileId) {
        FilePayload file = portfolioService.retrieveFile(fileId);

        return ResponseEntity.ok()
                .contentType(file.mediaType())
                .contentLength(file.content().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(file.filename())
                                .build()
                                .toString()
                )
                .body(file.content());
    }
}
