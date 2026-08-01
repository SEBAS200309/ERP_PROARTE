package com.proarte.erp.cotizaciones.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.proarte.erp.cotizaciones.entity.Cotizacion;
import com.proarte.erp.cotizaciones.entity.CotizacionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotizacionPdfService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(75, 0, 130));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(Cotizacion cotizacion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, cotizacion);
            addInfo(document, cotizacion);
            addItemsTable(document, cotizacion);
            addTotal(document, cotizacion);

            document.close();
            log.info("PDF generado para cotizacion: {}", cotizacion.getCodigo());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF para cotizacion {}: {}", cotizacion.getCodigo(), e.getMessage());
            throw new RuntimeException("Error al generar el PDF de la cotizacion", e);
        }
    }

    private void addHeader(Document document, Cotizacion cotizacion) throws DocumentException {
        Paragraph title = new Paragraph("COTIZACIÓN", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph codigo = new Paragraph(cotizacion.getCodigo(), BOLD_FONT);
        codigo.setAlignment(Element.ALIGN_CENTER);
        codigo.setSpacingAfter(20);
        document.add(codigo);
    }

    private void addInfo(Document document, Cotizacion cotizacion) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);

        addInfoRow(infoTable, "Código:", cotizacion.getCodigo());

        if (cotizacion.getCreatedAt() != null) {
            addInfoRow(infoTable, "Fecha de creación:",
                    cotizacion.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        if (cotizacion.getFechaVencimiento() != null) {
            addInfoRow(infoTable, "Fecha de vencimiento:",
                    cotizacion.getFechaVencimiento().format(DATE_FORMATTER));
        }

        document.add(infoTable);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addItemsTable(Document document, Cotizacion cotizacion) throws DocumentException {
        Paragraph subtitle = new Paragraph("Detalle de Ítems", BOLD_FONT);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 1.5f, 2f, 2f});

        addTableHeader(table, "#");
        addTableHeader(table, "Servicio ID");
        addTableHeader(table, "Cantidad");
        addTableHeader(table, "Precio Unit.");
        addTableHeader(table, "Subtotal");

        int index = 1;
        for (CotizacionItem item : cotizacion.getItems()) {
            addTableCell(table, String.valueOf(index++));
            addTableCell(table, item.getServicioId() != null ? item.getServicioId().toString().substring(0, 8) + "..." : "-");
            addTableCell(table, String.valueOf(item.getCantidad()));
            addTableCell(table, formatCurrency(item.getPrecioUnitario()));
            addTableCell(table, formatCurrency(item.getSubtotal()));
        }

        table.setSpacingAfter(15);
        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(75, 0, 130));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTotal(Document document, Cotizacion cotizacion) throws DocumentException {
        Paragraph total = new Paragraph(
                "TOTAL: " + formatCurrency(cotizacion.getTotal()),
                new Font(Font.HELVETICA, 14, Font.BOLD, new Color(75, 0, 130))
        );
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "$0.00";
        return String.format("$%,.2f", value);
    }
}
