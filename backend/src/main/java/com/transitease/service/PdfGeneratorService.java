package com.transitease.service;

import com.transitease.entity.DigitalPass;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfGeneratorService {

    public byte[] generatePassPdf(DigitalPass pass) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();

            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

            // Object 1: Catalog
            offsets.add(out.size());
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Object 2: Pages
            offsets.add(out.size());
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Object 3: Page
            offsets.add(out.size());
            out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Object 4: Font Regular
            offsets.add(out.size());
            out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Object 5: Font Bold
            offsets.add(out.size());
            out.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Content Stream (Object 6)
            String streamContent = buildContentStream(pass);
            byte[] streamBytes = streamContent.getBytes(StandardCharsets.US_ASCII);

            offsets.add(out.size());
            out.write(String.format("6 0 obj\n<< /Length %d >>\nstream\n", streamBytes.length).getBytes(StandardCharsets.US_ASCII));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // XRef Table
            int xrefOffset = out.size();
            out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
            out.write(String.format("0 %d\n", offsets.size() + 1).getBytes(StandardCharsets.US_ASCII));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (int offset : offsets) {
                out.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.US_ASCII));
            }

            // Trailer
            out.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", offsets.size() + 1, xrefOffset).getBytes(StandardCharsets.US_ASCII));

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating digital pass PDF", e);
        }
    }

    private String buildContentStream(DigitalPass pass) {
        StringBuilder sb = new StringBuilder();

        // Pass card background: light neutral fill
        sb.append("0.96 0.97 0.98 rg\n");
        sb.append("50 360 512 360 re f\n");

        // Header banner: deep blue
        sb.append("0.10 0.32 0.58 rg\n");
        sb.append("50 660 512 60 re f\n");

        // Card border
        sb.append("0.10 0.32 0.58 RG\n");
        sb.append("2 w\n");
        sb.append("50 360 512 360 re S\n");

        // Header Title & Subtitle (white)
        sb.append("1 1 1 rg\n");
        sb.append("BT /F2 18 Tf 70 695 Td (TRANSITEASE DIGITAL PASS) Tj ET\n");
        sb.append("BT /F1 10 Tf 70 675 Td (Official Public Transportation Digital Pass) Tj ET\n");

        // Status Badge
        String statusText = (pass.getStatus() != null) ? pass.getStatus().name() : "ACTIVE";
        if ("ACTIVE".equals(statusText)) {
            sb.append("0.16 0.58 0.28 rg\n"); // green
        } else {
            sb.append("0.75 0.20 0.20 rg\n"); // red
        }
        sb.append("430 675 110 25 re f\n");
        sb.append("1 1 1 rg\n");
        sb.append("BT /F2 11 Tf 450 683 Td (").append(statusText).append(") Tj ET\n");

        // Extract values
        String passNum = sanitize(pass.getPassNumber());
        String passengerName = sanitize(pass.getPassenger() != null ? pass.getPassenger().getFullName() : "N/A");
        String passengerEmail = sanitize(pass.getPassenger() != null ? pass.getPassenger().getEmail() : "N/A");
        String categoryName = sanitize((pass.getApplication() != null && pass.getApplication().getPassCategory() != null)
                ? pass.getApplication().getPassCategory().getName() : "Standard Pass");
        String validFrom = pass.getValidityStartDate() != null ? pass.getValidityStartDate().toString() : "N/A";
        String validUntil = pass.getValidityEndDate() != null ? pass.getValidityEndDate().toString() : "N/A";
        String issued = pass.getIssueDate() != null ? pass.getIssueDate().toString() : "N/A";

        // Labels & Values
        sb.append("0.20 0.20 0.20 rg\n");
        addTextRow(sb, "PASS NUMBER:", passNum, 630);
        addTextRow(sb, "PASSENGER:", passengerName, 595);
        addTextRow(sb, "EMAIL:", passengerEmail, 560);
        addTextRow(sb, "CATEGORY:", categoryName, 525);
        addTextRow(sb, "VALIDITY:", validFrom + " to " + validUntil, 490);
        addTextRow(sb, "ISSUED ON:", issued, 455);

        // Divider
        sb.append("0.75 0.75 0.75 RG\n");
        sb.append("1 w\n");
        sb.append("70 435 m 540 435 l S\n");

        // Instructions footer
        sb.append("0.45 0.45 0.45 rg\n");
        sb.append("BT /F1 9 Tf 70 415 Td (Present this digital pass upon request during transit inspections.) Tj ET\n");
        sb.append("BT /F1 9 Tf 70 400 Td (Issued by TransitEase Transportation System.) Tj ET\n");

        return sb.toString();
    }

    private void addTextRow(StringBuilder sb, String label, String value, int y) {
        sb.append("BT /F2 11 Tf 70 ").append(y).append(" Td (").append(label).append(") Tj ET\n");
        sb.append("BT /F1 11 Tf 190 ").append(y).append(" Td (").append(value).append(") Tj ET\n");
    }

    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }
}
