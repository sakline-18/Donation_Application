package org.example.donation_application.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.donation_application.dao.DonationDAO;
import org.example.donation_application.models.Donation;
import org.example.donation_application.models.Donor;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class DonationReportService {

    private DonationDAO donationDAO;

    public DonationReportService() {
        this.donationDAO = new DonationDAO();
    }

    /**
     * Generate comprehensive donation report for a donor
     */
    public boolean generateDonorReport(Donor donor, String outputPath) {
        try {
            List<Donation> donations = donationDAO.getDonationsByDonor(donor.getId());

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Add header
            addReportHeader(document, donor);

            // Add summary
            addDonationSummary(document, donations);

            // Add detailed table
            addDonationTable(document, donations);

            // Add footer
            addReportFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate simple donation list report
     */
    public boolean generateDonationList(List<Donation> donations, String outputPath, String title) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);

            // Generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph dateParagraph = new Paragraph("Generated on: " + new Date().toString(), dateFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(20);
            document.add(dateParagraph);

            // Add donations table
            addDonationTable(document, donations);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error generating donation list PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void addReportHeader(Document document, Donor donor) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("DONATION REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);
        Paragraph subtitle = new Paragraph("For: " + donor.getName(), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph date = new Paragraph("Generated on: " + new Date().toString(), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20);
        document.add(date);

        // Donor information
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph donorInfo = new Paragraph("DONOR INFORMATION", sectionFont);
        donorInfo.setSpacingAfter(10);
        document.add(donorInfo);

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        document.add(new Paragraph("Name: " + donor.getName(), infoFont));
        document.add(new Paragraph("Email: " + donor.getEmail(), infoFont));
        document.add(new Paragraph("Phone: " + donor.getPhone(), infoFont));
        document.add(new Paragraph("Organization: " + donor.getOrganization(), infoFont));
        document.add(new Paragraph(" ")); // Spacing
    }

    private void addDonationSummary(Document document, List<Donation> donations) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph summaryTitle = new Paragraph("DONATION SUMMARY", sectionFont);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        // Calculate totals
        BigDecimal totalAmount = donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total fees (assuming 2% fee was deducted)
        BigDecimal totalFees = totalAmount.multiply(new BigDecimal("0.0204")); // Reverse calculate
        BigDecimal originalTotal = totalAmount.add(totalFees);

        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLUE);
        document.add(new Paragraph("Total Donations Made: " + donations.size(), summaryFont));
        document.add(new Paragraph("Total Amount Donated: $" + String.format("%.2f", originalTotal), summaryFont));
        document.add(new Paragraph("Platform Fees (2%): $" + String.format("%.2f", totalFees), summaryFont));
        document.add(new Paragraph("Net Amount to Students: $" + String.format("%.2f", totalAmount), summaryFont));
        document.add(new Paragraph(" ")); // Spacing
    }

    private void addDonationTable(Document document, List<Donation> donations) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph tableTitle = new Paragraph("DONATION DETAILS", sectionFont);
        tableTitle.setSpacingAfter(10);
        document.add(tableTitle);

        if (donations.isEmpty()) {
            document.add(new Paragraph("No donations found."));
            return;
        }

        // Create table
        PdfPTable table = new PdfPTable(5); // Date, Student, Net Amount, Original Amount, Description
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 1.5f, 1.5f, 3});

        // Add headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        addTableHeader(table, "Date", headerFont);
        addTableHeader(table, "Student", headerFont);
        addTableHeader(table, "Net Amount", headerFont);
        addTableHeader(table, "Original Amount", headerFont);
        addTableHeader(table, "Description", headerFont);

        // Add data rows
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        for (Donation donation : donations) {
            // Calculate original amount (reverse the 2% fee)
            BigDecimal netAmount = donation.getAmount();
            BigDecimal originalAmount = netAmount.divide(new BigDecimal("0.98"), 2, BigDecimal.ROUND_HALF_UP);

            table.addCell(new PdfPCell(new Phrase(donation.getDonationDate().format(formatter), cellFont)));
            table.addCell(new PdfPCell(new Phrase(donation.getStudentName(), cellFont)));
            table.addCell(new PdfPCell(new Phrase("$" + String.format("%.2f", netAmount), cellFont)));
            table.addCell(new PdfPCell(new Phrase("$" + String.format("%.2f", originalAmount), cellFont)));
            table.addCell(new PdfPCell(new Phrase(
                    donation.getDescription() != null ? donation.getDescription() : "No description", cellFont)));
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(header, font));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addReportFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footer = new Paragraph("\nThank you for your generous contributions to our students!", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }
    /**
     * Generate comprehensive report for all donations in the system
     */
    public boolean generateAllDonationsReport(List<Donation> donations, String outputPath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("ALL DONATIONS REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Add generation info
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " + new Date().toString(), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Add summary statistics
            addSystemSummary(document, donations);

            // Add detailed donations table
            addAllDonationsTable(document, donations);

            // Add footer
            addSystemFooter(document);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("Error generating all donations report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void addSystemSummary(Document document, List<Donation> donations) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph summaryTitle = new Paragraph("SYSTEM SUMMARY", sectionFont);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        // Calculate statistics
        BigDecimal totalAmount = donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long uniqueDonors = donations.stream()
                .map(Donation::getDonorName)
                .filter(name -> name != null)
                .distinct()
                .count();

        long uniqueStudents = donations.stream()
                .map(Donation::getStudentName)
                .filter(name -> name != null)
                .distinct()
                .count();

        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLUE);
        document.add(new Paragraph("Total Donations: " + donations.size(), summaryFont));
        document.add(new Paragraph("Total Amount: $" + String.format("%.2f", totalAmount), summaryFont));
        document.add(new Paragraph("Unique Donors: " + uniqueDonors, summaryFont));
        document.add(new Paragraph("Students Supported: " + uniqueStudents, summaryFont));
        document.add(new Paragraph(" ")); // Spacing
    }

    private void addAllDonationsTable(Document document, List<Donation> donations) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph tableTitle = new Paragraph("ALL DONATION TRANSACTIONS", sectionFont);
        tableTitle.setSpacingAfter(10);
        document.add(tableTitle);

        if (donations.isEmpty()) {
            document.add(new Paragraph("No donations found in the system."));
            return;
        }

        // Create table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 3, 1.5f, 3});

        // Add headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        addTableHeader(table, "Date", headerFont);
        addTableHeader(table, "Donor", headerFont);
        addTableHeader(table, "Student", headerFont);
        addTableHeader(table, "Amount", headerFont);
        addTableHeader(table, "Description", headerFont);

        // Add data rows
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        for (Donation donation : donations) {
            table.addCell(new PdfPCell(new Phrase(donation.getDonationDate().format(formatter), cellFont)));
            table.addCell(new PdfPCell(new Phrase(donation.getDonorName() != null ? donation.getDonorName() : "Unknown", cellFont)));
            table.addCell(new PdfPCell(new Phrase(donation.getStudentName() != null ? donation.getStudentName() : "Unknown", cellFont)));
            table.addCell(new PdfPCell(new Phrase("BDT" + String.format("%.2f", donation.getAmount()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(donation.getDescription() != null ? donation.getDescription() : "", cellFont)));
        }

        document.add(table);
    }

    private void addSystemFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footer = new Paragraph("\nGenerated by Donation Management System", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }

}

