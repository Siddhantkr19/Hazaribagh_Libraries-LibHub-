package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.PaymentHistory;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.PaymentHistoryRepository;
import com.HazaribaghLibraries.repository.UserRepository;

import java.awt.Color;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentReportService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final EmailService emailService;

    public PaymentReportService(UserRepository userRepository,
                                LibraryRepository libraryRepository,
                                PaymentHistoryRepository paymentHistoryRepository,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.emailService = emailService;
    }

    // ================= MAIN METHOD =================
    public ByteArrayOutputStream generatePaymentReport(
            String email, Long libraryId, String sendTo) {

        List<PaymentHistory> history;
        String reportTitle = "Full Payment History";

        if (email != null && libraryId != null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new RuntimeException("Library not found"));

            // Use the new N+1 safe method
            history = paymentHistoryRepository.findByUserAndLibraryWithDetails(user, library);

            reportTitle = "Payment Report for " + user.getName()
                    + " (" + library.getName() + ")";

        } else if (libraryId != null) {
            // We don't need to fetch the library here anymore, the query does it.
            // Use the new N+1 safe method
            history = paymentHistoryRepository.findByLibraryIdWithDetails(libraryId);

            // Get the library name from the first result (if it exists)
            String libraryName = history.isEmpty() ? "Unknown Library" : history.get(0).getLibrary().getName();
            reportTitle = "Full Payment History - " + libraryName;

        } else {
            history = paymentHistoryRepository.findAllWithDetails();
        }

        ByteArrayOutputStream pdfStream = createPdf(history, reportTitle);

        // Optional Email Sending
        if (sendTo != null && !sendTo.isBlank()) {
            emailService.sendEmailWithAttachment(
                    sendTo,
                    "Payment Report",
                    "Please find the attached payment report.",
                    pdfStream,
                    "payment-report.pdf"
            );
        }

        return pdfStream;
    }

    // ================= PDF CREATION =================
    private ByteArrayOutputStream createPdf(List<PaymentHistory> history, String title) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 10);

            document.add(new Paragraph(title, titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Generated on: " + LocalDateTime.now(), bodyFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{2, 3, 3, 2, 3, 2});

            addHeader(table, "User", headerFont);
            addHeader(table, "Email", headerFont);
            addHeader(table, "Library", headerFont);
            addHeader(table, "Amount", headerFont);
            addHeader(table, "Payment Date", headerFont);
            addHeader(table, "Status", headerFont);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            for (PaymentHistory p : history) {
                table.addCell(new Phrase(p.getUser().getName(), bodyFont));
                table.addCell(new Phrase(p.getUser().getEmail(), bodyFont));
                table.addCell(new Phrase(p.getLibrary().getName(), bodyFont));
                table.addCell(new Phrase("₹" + p.getAmount(), bodyFont));
                table.addCell(new Phrase(
                        p.getPaymentDate().format(formatter), bodyFont));
                table.addCell(new Phrase(p.getStatus(), bodyFont));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating payment PDF", e);
        }

        return outputStream;
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }


}
