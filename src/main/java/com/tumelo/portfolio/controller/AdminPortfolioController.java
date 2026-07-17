package com.tumelo.portfolio.controller;

import com.tumelo.portfolio.dto.FileUploadResponse;
import com.tumelo.portfolio.dto.PortfolioRequest;
import com.tumelo.portfolio.dto.PortfolioResponse;
import com.tumelo.portfolio.security.AdminKeyGuard;
import com.tumelo.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/portfolio")
public class AdminPortfolioController {

    private final PortfolioService portfolioService;
    private final AdminKeyGuard adminKeyGuard;

    public AdminPortfolioController(
            PortfolioService portfolioService,
            AdminKeyGuard adminKeyGuard
    ) {
        this.portfolioService = portfolioService;
        this.adminKeyGuard = adminKeyGuard;
    }

    @PutMapping("/admin/portfolio")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @RequestHeader(name = "X-Admin-Key", defaultValue = "") String adminKey,
            @Valid @RequestBody PortfolioRequest request
    ) {
        adminKeyGuard.verify(adminKey);
        return ResponseEntity.ok(portfolioService.upsertPortfolio(request));
    }

    /**
     * Legacy route retained for compatibility.
     */
    @PostMapping("/user/save")
    public ResponseEntity<PortfolioResponse> savePortfolioLegacy(
            @RequestHeader(name = "X-Admin-Key", defaultValue = "") String adminKey,
            @Valid @RequestBody PortfolioRequest request
    ) {
        adminKeyGuard.verify(adminKey);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioService.upsertPortfolio(request));
    }

    @PostMapping("/admin/files")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestHeader(name = "X-Admin-Key", defaultValue = "") String adminKey,
            @RequestParam("file") MultipartFile file
    ) {
        adminKeyGuard.verify(adminKey);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioService.storeFile(file));
    }

    /**
     * Legacy route retained for compatibility.
     */
    @PostMapping("/user/bsonfile")
    public ResponseEntity<FileUploadResponse> uploadFileLegacy(
            @RequestHeader(name = "X-Admin-Key", defaultValue = "") String adminKey,
            @RequestParam("file") MultipartFile file
    ) {
        return uploadFile(adminKey, file);
    }
}
