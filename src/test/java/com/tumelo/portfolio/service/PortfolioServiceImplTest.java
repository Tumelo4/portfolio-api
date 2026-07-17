package com.tumelo.portfolio.service;

import java.util.List;
import java.util.Optional;

import com.tumelo.portfolio.dto.ContactRequest;
import com.tumelo.portfolio.dto.PortfolioRequest;
import com.tumelo.portfolio.dto.ProjectDto;
import com.tumelo.portfolio.dto.SkillDto;
import com.tumelo.portfolio.exception.PortfolioNotFoundException;
import com.tumelo.portfolio.model.PortfolioDocument;
import com.tumelo.portfolio.repository.PortfolioRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock
    private PortfolioRepository repository;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private JavaMailSender mailSender;

    private PortfolioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PortfolioServiceImpl(
                repository,
                gridFsTemplate,
                mailSender,
                "portfolio@example.com",
                "owner@example.com",
                5_242_880
        );
    }

    @Test
    void returnsTheFirstPortfolioDocument() {
        PortfolioDocument document = new PortfolioDocument();
        document.setId(new ObjectId());
        document.setName("Tumelo");
        document.setSurname("Mosomane");
        document.setOccupation("Software Engineer");
        document.setHomeDescription("Home");
        document.setAboutDescription("About");

        when(repository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(document));

        var result = service.getPortfolio();

        assertEquals("Tumelo", result.name());
        assertEquals("Mosomane", result.surname());
    }

    @Test
    void reportsWhenNoPortfolioExists() {
        when(repository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.empty());

        assertThrows(
                PortfolioNotFoundException.class,
                service::getPortfolio
        );
    }

    @Test
    void createsThePortfolioWhenNoDocumentExists() {
        PortfolioRequest request = new PortfolioRequest(
                "Tumelo",
                "Mosomane",
                "Software Engineer",
                "https://linkedin.example",
                "https://github.example",
                "",
                "resume-id",
                "image-id",
                "Home description",
                "About description",
                List.of(new SkillDto("Java", "java-image")),
                List.of(new ProjectDto(
                        "Portfolio",
                        "Developer portfolio",
                        "project-image",
                        "https://project.example"
                ))
        );

        when(repository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.empty());

        when(repository.save(any(PortfolioDocument.class)))
                .thenAnswer(invocation -> {
                    PortfolioDocument saved = invocation.getArgument(0);
                    saved.setId(new ObjectId());
                    return saved;
                });

        var result = service.upsertPortfolio(request);

        assertEquals("Tumelo", result.name());
        assertEquals(1, result.skills().size());
        assertEquals(1, result.projects().size());
    }

    @Test
    void sendsContactEmailUsingReplyTo() {
        ContactRequest request = new ContactRequest(
                "Jane",
                "Recruiter",
                "jane@example.com",
                "https://company.example",
                "Engineering Manager",
                "+27 10 000 0000",
                "I would like to discuss an opportunity."
        );

        service.sendContactEmail(request);

        ArgumentCaptor<SimpleMailMessage> message =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(message.capture());

        assertEquals("jane@example.com", message.getValue().getReplyTo());
        assertEquals(
                "owner@example.com",
                message.getValue().getTo()[0]
        );
    }
}
