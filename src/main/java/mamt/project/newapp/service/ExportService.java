package mamt.project.newapp.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 0, 0));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(70, 70, 70));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

    public void generateFichePaie(OutputStream outputStream,
                                  Map<String, Object> employee,
                                  Map<String, Object> salaire) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        addHeader(document, salaire);

        addEmployeeInfo(document, employee);

        addSalaryDetails(document, salaire);

        addFooter(document);

        document.close();
    }

    private void addHeader(Document document, Map<String, Object> salaire) {
        Paragraph title = new Paragraph("FICHE DE PAIE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        String periode = formatDate((String) salaire.get("start_date")) + " - " +
                formatDate((String) salaire.get("end_date"));
        Paragraph subtitle = new Paragraph("Période: " + periode, SUBTITLE_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20f);
        document.add(subtitle);
    }

    private void addEmployeeInfo(Document document, Map<String, Object> employee) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(20f);

        PdfPCell headerCell = new PdfPCell(new Phrase("INFORMATIONS EMPLOYÉ", HEADER_FONT));
        headerCell.setBackgroundColor(new Color(59, 89, 152));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        table.addCell(headerCell);

        addTableRow(table, "Matricule", (String) employee.get("name"));
        addTableRow(table, "Nom", (String) employee.get("employee_name"));
        addTableRow(table, "Département", (String) employee.get("department"));
        addTableRow(table, "Poste", (String) employee.get("designation"));
        addTableRow(table, "Date d'embauche", formatDate((String) employee.get("date_of_joining")));

        document.add(table);
    }

    private void addSalaryDetails(Document document, Map<String, Object> salaire) {
        PdfPTable earningsTable = createSalaryComponentTable("GAINS");
        List<Map<String, Object>> earnings = (List<Map<String, Object>>) salaire.get("earnings");
        if (earnings != null) {
            for (Map<String, Object> earning : earnings) {
                addSalaryComponentRow(earningsTable,
                        (String) earning.get("salary_component"),
                        (Number) earning.get("amount"));
            }
        }
        document.add(earningsTable);

        PdfPTable deductionsTable = createSalaryComponentTable("DÉDUCTIONS");
        List<Map<String, Object>> deductions = (List<Map<String, Object>>) salaire.get("deductions");
        if (deductions != null) {
            for (Map<String, Object> deduction : deductions) {
                addSalaryComponentRow(deductionsTable,
                        (String) deduction.get("salary_component"),
                        (Number) deduction.get("amount"));
            }
        }
        document.add(deductionsTable);

        // Total
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(50);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingBefore(20f);

        addTotalRow(totalTable, "Salaire Brut", (Number) salaire.get("gross_pay"));
        addTotalRow(totalTable, "Total Déductions", (Number) salaire.get("total_deduction"));
        addTotalRow(totalTable, "Salaire Net", (Number) salaire.get("net_pay"), true);

        document.add(totalTable);
    }

    private PdfPTable createSalaryComponentTable(String title) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15f);

        // En-tête du tableau
        PdfPCell headerCell = new PdfPCell(new Phrase(title, HEADER_FONT));
        headerCell.setBackgroundColor(new Color(59, 89, 152));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8f);
        table.addCell(headerCell);

        // Colonnes
        PdfPCell labelCell = new PdfPCell(new Phrase("Libellé", BOLD_FONT));
        labelCell.setPadding(5f);
        table.addCell(labelCell);

        PdfPCell amountCell = new PdfPCell(new Phrase("Montant (USD)", BOLD_FONT));
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPadding(5f);
        table.addCell(amountCell);

        return table;
    }

    private void addSalaryComponentRow(PdfPTable table, String label, Number amount) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setPadding(5f);
        table.addCell(labelCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(String.format("%,.2f", amount.doubleValue()), NORMAL_FONT));
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPadding(5f);
        table.addCell(amountCell);
    }

    private void addTotalRow(PdfPTable table, String label, Number amount, boolean isHighlighted) {
        Font font = isHighlighted ? BOLD_FONT : NORMAL_FONT;
        Color bgColor = isHighlighted ? new Color(221, 235, 247) : Color.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(5f);
        table.addCell(labelCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(String.format("%,.2f", amount.doubleValue()), font));
        amountCell.setBackgroundColor(bgColor);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPadding(5f);
        table.addCell(amountCell);
    }

    private void addTotalRow(PdfPTable table, String label, Number amount) {
        addTotalRow(table, label, amount, false);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setPadding(5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", NORMAL_FONT));
        valueCell.setPadding(5f);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(new Phrase("Date d'édition: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()), NORMAL_FONT));
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(30f);
        document.add(footer);
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            return outputFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            return dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr;
        }
    }
}