package com.transitease.service;

import com.transitease.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorServiceTest {

    private PdfGeneratorService pdfGeneratorService;
    private DigitalPass samplePass;
    private User passenger;
    private PassApplication application;
    private PassCategory category;

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new PdfGeneratorService();

        passenger = new User();
        passenger.setId(10L);
        passenger.setFullName("Rohan Deshmukh");
        passenger.setEmail("rohan.deshmukh@example.com");
        passenger.setRole(Role.PASSENGER);

        category = new PassCategory();
        category.setId(1L);
        category.setName("Student Monthly Pass");
        category.setValidityDays(30);

        application = new PassApplication();
        application.setId(100L);
        application.setPassenger(passenger);
        application.setPassCategory(category);
        application.setStatus(ApplicationStatus.APPROVED);

        samplePass = new DigitalPass();
        samplePass.setId(500L);
        samplePass.setPassNumber("TE-PASS-20261019-ABC12345");
        samplePass.setPassenger(passenger);
        samplePass.setApplication(application);
        samplePass.setStatus(PassStatus.ACTIVE);
        samplePass.setIssueDate(LocalDate.of(2026, 10, 19));
        samplePass.setValidityStartDate(LocalDate.of(2026, 10, 19));
        samplePass.setValidityEndDate(LocalDate.of(2026, 11, 18));
    }

    @Nested
    @DisplayName("Normal Cases (Happy Path)")
    class NormalCases {

        @Test
        @DisplayName("generatePassPdf generates valid PDF bytes with header, trailer, and metadata")
        void testGeneratePassPdf_Success() {
            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);

            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);

            // Valid PDF markers
            assertTrue(pdfContent.startsWith("%PDF-1.4"), "PDF must begin with %PDF-1.4 header");
            assertTrue(pdfContent.contains("%%EOF"), "PDF must contain %%EOF trailer");
            assertTrue(pdfContent.contains("xref"), "PDF must contain xref table");

            // Pass-specific fields
            assertTrue(pdfContent.contains("TE-PASS-20261019-ABC12345"));
            assertTrue(pdfContent.contains("Rohan Deshmukh"));
            assertTrue(pdfContent.contains("rohan.deshmukh@example.com"));
            assertTrue(pdfContent.contains("Student Monthly Pass"));
            assertTrue(pdfContent.contains("2026-10-19 to 2026-11-18"));
            assertTrue(pdfContent.contains("(ACTIVE)"));
        }
    }

    @Nested
    @DisplayName("Invalid / Missing Inputs & Graceful Fallbacks")
    class InvalidAndMissingInputCases {

        @Test
        @DisplayName("generatePassPdf with null passenger uses fallback values")
        void testGeneratePassPdf_NullPassenger() {
            samplePass.setPassenger(null);

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);
            assertTrue(pdfContent.contains("(PASSENGER:) Tj ET\nBT /F1 11 Tf 190 595 Td (N/A)"));
            assertTrue(pdfContent.contains("(EMAIL:) Tj ET\nBT /F1 11 Tf 190 560 Td (N/A)"));
        }

        @Test
        @DisplayName("generatePassPdf with null application uses Standard Pass category fallback")
        void testGeneratePassPdf_NullApplication() {
            samplePass.setApplication(null);

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);
            assertTrue(pdfContent.contains("Standard Pass"));
        }

        @Test
        @DisplayName("generatePassPdf with null dates handles N/A without crashing")
        void testGeneratePassPdf_NullDates() {
            samplePass.setIssueDate(null);
            samplePass.setValidityStartDate(null);
            samplePass.setValidityEndDate(null);

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);
            assertTrue(pdfContent.contains("N/A to N/A"));
            assertTrue(pdfContent.contains("(ISSUED ON:) Tj ET\nBT /F1 11 Tf 190 455 Td (N/A)"));
        }

        @Test
        @DisplayName("generatePassPdf with null status defaults to ACTIVE")
        void testGeneratePassPdf_NullStatus() {
            samplePass.setStatus(null);

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);
            assertTrue(pdfContent.contains("(ACTIVE)"));
        }
    }

    @Nested
    @DisplayName("Boundary Cases")
    class BoundaryCases {

        @Test
        @DisplayName("generatePassPdf with EXPIRED or SUSPENDED status renders red badge")
        void testGeneratePassPdf_ExpiredStatusRedBadge() {
            samplePass.setStatus(PassStatus.EXPIRED);

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);
            assertTrue(pdfContent.contains("(EXPIRED)"));
            assertTrue(pdfContent.contains("0.75 0.20 0.20 rg"), "Red fill color expected for expired badge");
        }

        @Test
        @DisplayName("generatePassPdf sanitizes parentheses, backslashes, and non-ASCII chars")
        void testGeneratePassPdf_SanitizationOfSpecialCharacters() {
            passenger.setFullName("Aarav (Student\\Junior) \uD83D\uDE8C Special");
            category.setName("Concessional (50% Off\\Subsidy)");

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);

            // Escaped parentheses and backslashes
            assertTrue(pdfContent.contains("Aarav \\(Student\\\\Junior\\)"), "Parentheses and backslashes must be escaped in PDF literal strings");
            assertTrue(pdfContent.contains("Concessional \\(50% Off\\\\Subsidy\\)"));
        }

        @Test
        @DisplayName("generatePassPdf with null passNumber sanitizes to empty string")
        void testGeneratePassPdf_NullPassNumber() {
            samplePass.setPassNumber(null);

            byte[] pdfBytes = pdfGeneratorService.generatePassPdf(samplePass);

            assertNotNull(pdfBytes);
            String pdfContent = new String(pdfBytes, StandardCharsets.US_ASCII);
            assertTrue(pdfContent.contains("(PASS NUMBER:) Tj ET\nBT /F1 11 Tf 190 630 Td () Tj ET"));
        }
    }
}
