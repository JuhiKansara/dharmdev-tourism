package com.dharmdev.tourism_backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class PdfGenerationService {

    private final Font titleFont;
    private final Font headerFont;
    private final Font normalFont;
    private final Font boldFont;

    public PdfGenerationService() {
        try {
            InputStream fontStream = new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream();
            byte[] fontBytes = fontStream.readAllBytes();
            BaseFont baseFont = BaseFont.createFont(
                    "NotoSans-Regular.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null
            );
            titleFont = new Font(baseFont, 18, Font.BOLD);
            headerFont = new Font(baseFont, 10, Font.BOLD);
            normalFont = new Font(baseFont, 10, Font.NORMAL);
            boldFont = new Font(baseFont, 10, Font.BOLD);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NotoSans font for PDF generation", e);
        }
    }

    public byte[] generateEstimatePdf(GstCalculationService.EstimateCalculation data) throws DocumentException {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Paragraph title = new Paragraph("Estimate", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15);
        document.add(title);

        // Business + Estimate For/Details block
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);

        PdfPCell businessCell = new PdfPCell();
        businessCell.setPadding(8);
        businessCell.addElement(new Paragraph("Dharmdev Tourism and Travels", headerFont));
        businessCell.addElement(new Paragraph("31 Ashadip Society, Modhera Road, Mahesana", normalFont));
        businessCell.addElement(new Paragraph("Phone: 8469405720", normalFont));
        businessCell.addElement(new Paragraph("GSTIN: 24EVOPK9257C1Z9", normalFont));
        headerTable.addCell(businessCell);

        PdfPCell contactCell = new PdfPCell();
        contactCell.setPadding(8);
        contactCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        contactCell.addElement(new Paragraph("Email: jayshreek762@gmail.com", normalFont));
        contactCell.addElement(new Paragraph("State: 24-Gujarat", normalFont));
        headerTable.addCell(contactCell);
        document.add(headerTable);

        // Estimate For / Details row
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        PdfPCell forCell = new PdfPCell();
        forCell.setPadding(8);
        forCell.addElement(new Paragraph("Estimate For:", headerFont));
        forCell.addElement(new Paragraph(data.customerName(), normalFont));
        infoTable.addCell(forCell);

        PdfPCell detailsCell = new PdfPCell();
        detailsCell.setPadding(8);
        detailsCell.addElement(new Paragraph("Estimate Details:", headerFont));
        detailsCell.addElement(new Paragraph("No: " + data.estimateNo(), normalFont));
        detailsCell.addElement(new Paragraph("Date: " + data.estimateDate(), normalFont));
        detailsCell.addElement(new Paragraph("Place of Supply: " + data.placeOfSupplyStateCode() + "-Gujarat", normalFont));
        infoTable.addCell(detailsCell);
        document.add(infoTable);

        // Line items table
        PdfPTable itemsTable = new PdfPTable(new float[]{0.5f, 3f, 1.5f, 1f, 1.2f, 1.5f, 1.3f});
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingBefore(0);
        addHeaderCell(itemsTable, "#");
        addHeaderCell(itemsTable, "Item Name");
        addHeaderCell(itemsTable, "HSN/SAC");
        addHeaderCell(itemsTable, "Qty");
        addHeaderCell(itemsTable, "Price/Unit");
        addHeaderCell(itemsTable, "GST(₹)");
        addHeaderCell(itemsTable, "Amount(₹)");

        int i = 1;
        for (var li : data.lineItems()) {
            addCell(itemsTable, String.valueOf(i++), Element.ALIGN_LEFT);
            addCell(itemsTable, li.lineItem().getItemName(), Element.ALIGN_LEFT);
            addCell(itemsTable, li.lineItem().getHsnSac() == null ? "" : li.lineItem().getHsnSac(), Element.ALIGN_LEFT);
            addCell(itemsTable, li.lineItem().getQuantity().toString(), Element.ALIGN_RIGHT);
            addCell(itemsTable, "₹" + li.lineItem().getPricePerUnit(), Element.ALIGN_RIGHT);
            addCell(itemsTable, "₹" + li.totalTax() + " (" + li.lineItem().getGstRate() + "%)", Element.ALIGN_RIGHT);
            addCell(itemsTable, "₹" + li.lineAmount(), Element.ALIGN_RIGHT);
        }
        document.add(itemsTable);

        // Totals
        Paragraph totals = new Paragraph();
        totals.add(new Chunk("Sub Total: ₹" + data.subTotal() + "\n", normalFont));
        totals.add(new Chunk("Total Tax: ₹" + data.totalTax() + "\n", normalFont));
        totals.add(new Chunk("Grand Total: ₹" + data.grandTotal() + "\n", boldFont));
        totals.add(new Chunk("Amount in Words: " + data.grandTotalInWords(), normalFont));
        totals.setSpacingBefore(15);
        document.add(totals);

        // Terms
        if (data.termsAndConditions() != null && !data.termsAndConditions().isBlank()) {
            Paragraph terms = new Paragraph();
            terms.add(new Chunk("Terms And Conditions: ", headerFont));
            terms.add(new Chunk(data.termsAndConditions(), normalFont));
            terms.setSpacingBefore(15);
            document.add(terms);
        }

        document.close();
        return out.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, normalFont));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }
}