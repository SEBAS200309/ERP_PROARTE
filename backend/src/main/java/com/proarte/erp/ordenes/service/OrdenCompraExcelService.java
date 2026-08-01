package com.proarte.erp.ordenes.service;

import com.proarte.erp.ordenes.entity.OrdenCompra;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class OrdenCompraExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateExcel(List<OrdenCompra> ordenes) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ordenes de Compra");

            CellStyle headerStyle = createHeaderStyle(workbook);
            createHeaderRow(sheet, headerStyle);
            fillDataRows(sheet, ordenes);
            autoSizeColumns(sheet);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            log.info("Excel generado con {} ordenes de compra", ordenes.size());
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar el archivo Excel", e);
            throw new RuntimeException("Error al generar el archivo Excel de ordenes de compra", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Código", "Descripción", "Monto", "Estado ID", "Fecha Creación"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillDataRows(Sheet sheet, List<OrdenCompra> ordenes) {
        int rowNum = 1;
        for (OrdenCompra orden : ordenes) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(orden.getCodigo() != null ? orden.getCodigo() : "");
            row.createCell(1).setCellValue(orden.getDescripcion() != null ? orden.getDescripcion() : "");
            row.createCell(2).setCellValue(orden.getMonto() != null ? orden.getMonto().doubleValue() : 0);
            row.createCell(3).setCellValue(orden.getEstadoId() != null ? orden.getEstadoId().toString() : "");
            row.createCell(4).setCellValue(
                    orden.getCreatedAt() != null ? orden.getCreatedAt().format(DATE_FORMATTER) : ""
            );
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
