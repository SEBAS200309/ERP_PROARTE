package com.proarte.erp.presentaciones.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.proarte.erp.presentaciones.entity.Presentacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresentacionPdfService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(75, 0, 130));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);

    public byte[] generatePdf(Presentacion presentacion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, presentacion);
            addInfo(document, presentacion);

            document.close();
            log.info("PDF generated for presentacion: {}", presentacion.getId());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF for presentacion {}: {}", presentacion.getId(), e.getMessage());
            throw new RuntimeException("Error al generar el PDF de la presentación", e);
        }
    }

    private void addHeader(Document document, Presentacion presentacion) throws DocumentException {
        Paragraph title = new Paragraph("PRESENTACIÓN", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph nombre = new Paragraph(presentacion.getNombre(), BOLD_FONT);
        nombre.setAlignment(Element.ALIGN_CENTER);
        nombre.setSpacingAfter(20);
        document.add(nombre);
    }

    private void addInfo(Document document, Presentacion presentacion) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);

        addInfoRow(infoTable, "Nombre:", presentacion.getNombre());
        addInfoRow(infoTable, "Descripción:", presentacion.getDescripcion());

        if (presentacion.getServicioId() != null) {
            addInfoRow(infoTable, "Servicio ID:",
                    presentacion.getServicioId().toString().substring(0, 8) + "...");
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
}
