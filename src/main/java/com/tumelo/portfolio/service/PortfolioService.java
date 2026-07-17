package com.tumelo.portfolio.service;

import com.tumelo.portfolio.dto.ContactRequest;
import com.tumelo.portfolio.dto.FileUploadResponse;
import com.tumelo.portfolio.dto.PortfolioRequest;
import com.tumelo.portfolio.dto.PortfolioResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PortfolioService {

    PortfolioResponse getPortfolio();

    PortfolioResponse upsertPortfolio(PortfolioRequest request);

    FileUploadResponse storeFile(MultipartFile file);

    FilePayload retrieveFile(String fileId);

    void sendContactEmail(ContactRequest request);
}
