package com.tumelo.portfolio.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.tumelo.portfolio.dto.ContactRequest;
import com.tumelo.portfolio.dto.FileUploadResponse;
import com.tumelo.portfolio.dto.PortfolioRequest;
import com.tumelo.portfolio.dto.PortfolioResponse;
import com.tumelo.portfolio.dto.ProjectDto;
import com.tumelo.portfolio.dto.SkillDto;
import com.tumelo.portfolio.exception.FileStorageException;
import com.tumelo.portfolio.exception.InvalidFileException;
import com.tumelo.portfolio.exception.MailDeliveryException;
import com.tumelo.portfolio.exception.MailNotConfiguredException;
import com.tumelo.portfolio.exception.PortfolioFileNotFoundException;
import com.tumelo.portfolio.exception.PortfolioNotFoundException;
import com.tumelo.portfolio.model.PortfolioDocument;
import com.tumelo.portfolio.model.Project;
import com.tumelo.portfolio.model.Skill;
import com.tumelo.portfolio.repository.PortfolioRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private static final MediaType PDF = MediaType.APPLICATION_PDF;
    private static final MediaType PNG = MediaType.IMAGE_PNG;
    private static final MediaType JPEG = MediaType.IMAGE_JPEG;

    private final PortfolioRepository portfolioRepository;
    private final GridFsTemplate gridFsTemplate;
    private final JavaMailSender emailSender;
    private final String mailFrom;
    private final String mailTo;
    private final long maxUploadBytes;

    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            GridFsTemplate gridFsTemplate,
            JavaMailSender emailSender,
            @Value("${app.mail.from:}") String mailFrom,
            @Value("${app.mail.to:}") String mailTo,
            @Value("${app.upload.max-bytes:5242880}") long maxUploadBytes
    ) {
        this.portfolioRepository = portfolioRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.emailSender = emailSender;
        this.mailFrom = mailFrom == null ? "" : mailFrom.trim();
        this.mailTo = mailTo == null ? "" : mailTo.trim();
        this.maxUploadBytes = maxUploadBytes;
    }

    @Override
    public PortfolioResponse getPortfolio() {
        PortfolioDocument document = portfolioRepository
                .findFirstByOrderByIdAsc()
                .orElseThrow(() -> new PortfolioNotFoundException(
                        "Portfolio information is unavailable"
                ));

        return toResponse(document);
    }

    @Override
    public PortfolioResponse upsertPortfolio(PortfolioRequest request) {
        PortfolioDocument document = portfolioRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(PortfolioDocument::new);

        applyRequest(document, request);

        PortfolioDocument saved = portfolioRepository.save(document);
        return toResponse(saved);
    }

    @Override
    public FileUploadResponse storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("A non-empty file is required");
        }

        if (file.getSize() > maxUploadBytes) {
            throw new InvalidFileException(
                    "File exceeds the configured maximum size of "
                            + maxUploadBytes + " bytes"
            );
        }

        String filename = sanitizeFilename(file.getOriginalFilename());

        try {
            byte[] content = file.getBytes();
            MediaType mediaType = detectMediaType(content, filename);

            if (!isAllowed(mediaType)) {
                throw new InvalidFileException(
                        "Only PDF, PNG and JPEG files are allowed"
                );
            }

            Document metadata = new Document()
                    .append("contentType", mediaType.toString())
                    .append("size", content.length);

            ObjectId fileId;
            try (InputStream input = new ByteArrayInputStream(content)) {
                fileId = gridFsTemplate.store(
                        input,
                        filename,
                        mediaType.toString(),
                        metadata
                );
            }

            return new FileUploadResponse(
                    fileId.toHexString(),
                    filename,
                    mediaType.toString(),
                    content.length,
                    "File stored successfully"
            );
        } catch (InvalidFileException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new FileStorageException(
                    "The uploaded file could not be read",
                    exception
            );
        }
    }

    @Override
    public FilePayload retrieveFile(String fileId) {
        if (!ObjectId.isValid(fileId)) {
            throw new InvalidFileException("Invalid file identifier");
        }

        ObjectId objectId = new ObjectId(fileId);
        GridFSFile gridFsFile = gridFsTemplate.findOne(
                Query.query(Criteria.where("_id").is(objectId))
        );

        if (gridFsFile == null) {
            throw new PortfolioFileNotFoundException(
                    "No file exists for the supplied identifier"
            );
        }

        try (InputStream input = gridFsTemplate
                .getResource(gridFsFile)
                .getInputStream()) {

            byte[] content = input.readAllBytes();
            String filename = Optional.ofNullable(gridFsFile.getFilename())
                    .filter(name -> !name.isBlank())
                    .orElse("portfolio-file");

            MediaType mediaType = parseMediaType(gridFsFile, content, filename);

            return new FilePayload(content, mediaType, filename);
        } catch (IOException exception) {
            throw new FileStorageException(
                    "The stored file could not be read",
                    exception
            );
        }
    }

    @Override
    public void sendContactEmail(ContactRequest request) {
        if (mailFrom.isBlank() || mailTo.isBlank()) {
            throw new MailNotConfiguredException(
                    "Contact email is not configured"
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(mailTo);
        message.setReplyTo(request.workEmail());
        message.setSubject("Portfolio inquiry from " + request.firstName()
                + " " + request.lastName());
        message.setText(buildEmailBody(request));

        try {
            emailSender.send(message);
        } catch (MailException exception) {
            throw new MailDeliveryException(
                    "The contact message could not be delivered",
                    exception
            );
        }
    }

    private void applyRequest(
            PortfolioDocument document,
            PortfolioRequest request
    ) {
        document.setName(request.name());
        document.setSurname(request.surname());
        document.setOccupation(request.occupation());
        document.setLinkedinLink(request.linkedinLink());
        document.setGithubLink(request.githubLink());
        document.setInstagramLink(request.instagramLink());
        document.setPdfResumeId(request.pdfResumeId());
        document.setImageId(request.imageId());
        document.setHomeDescription(request.homeDescription());
        document.setAboutDescription(request.aboutDescription());

        List<Skill> skills = request.skills()
                .stream()
                .map(skill -> new Skill(skill.name(), skill.imageId()))
                .toList();

        List<Project> projects = request.projects()
                .stream()
                .map(project -> new Project(
                        project.title(),
                        project.description(),
                        project.imageId(),
                        project.link()
                ))
                .toList();

        document.setSkills(skills);
        document.setProjects(projects);
    }

    private PortfolioResponse toResponse(PortfolioDocument document) {
        List<SkillDto> skills = document.getSkills()
                .stream()
                .map(skill -> new SkillDto(
                        skill.getName(),
                        skill.getImageId()
                ))
                .toList();

        List<ProjectDto> projects = document.getProjects()
                .stream()
                .map(project -> new ProjectDto(
                        project.getTitle(),
                        project.getDescription(),
                        project.getImageId(),
                        project.getLink()
                ))
                .toList();

        return new PortfolioResponse(
                document.getId() == null
                        ? null
                        : document.getId().toHexString(),
                document.getName(),
                document.getSurname(),
                document.getOccupation(),
                document.getLinkedinLink(),
                document.getGithubLink(),
                document.getInstagramLink(),
                document.getPdfResumeId(),
                document.getImageId(),
                document.getHomeDescription(),
                document.getAboutDescription(),
                skills,
                projects
        );
    }

    private String buildEmailBody(ContactRequest request) {
        return """
                First name: %s
                Last name: %s
                Work email: %s
                Work URL: %s
                Job title: %s
                Contact number: %s

                Message:
                %s
                """.formatted(
                request.firstName(),
                request.lastName(),
                request.workEmail(),
                request.workUrl(),
                request.jobTitle(),
                Optional.ofNullable(request.contactNumber()).orElse(""),
                request.message()
        );
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "portfolio-file";
        }

        String filename = Path.of(originalFilename)
                .getFileName()
                .toString()
                .replaceAll("[\\r\\n]", "_");

        if (filename.length() > 180) {
            filename = filename.substring(filename.length() - 180);
        }

        return filename;
    }

    private MediaType parseMediaType(
            GridFSFile file,
            byte[] content,
            String filename
    ) {
        if (file.getMetadata() != null) {
            String metadataType = file.getMetadata()
                    .getString("contentType");

            if (metadataType != null && !metadataType.isBlank()) {
                try {
                    return MediaType.parseMediaType(metadataType);
                } catch (IllegalArgumentException ignored) {
                    // Fall back to signature detection.
                }
            }
        }

        return detectMediaType(content, filename);
    }

    private MediaType detectMediaType(byte[] content, String filename) {
        if (isPdf(content)) {
            return PDF;
        }

        if (isPng(content)) {
            return PNG;
        }

        if (isJpeg(content)) {
            return JPEG;
        }

        String lower = filename.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".pdf")) {
            return PDF;
        }

        if (lower.endsWith(".png")) {
            return PNG;
        }

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return JPEG;
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private boolean isAllowed(MediaType mediaType) {
        return PDF.equals(mediaType)
                || PNG.equals(mediaType)
                || JPEG.equals(mediaType);
    }

    private boolean isPdf(byte[] content) {
        return content.length >= 4
                && content[0] == 0x25
                && content[1] == 0x50
                && content[2] == 0x44
                && content[3] == 0x46;
    }

    private boolean isPng(byte[] content) {
        return content.length >= 8
                && (content[0] & 0xFF) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A;
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF;
    }
}
