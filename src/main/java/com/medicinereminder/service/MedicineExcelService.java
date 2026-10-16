package com.medicinereminder.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.medicinereminder.model.Medicine;

@Service
public class MedicineExcelService {

    private static final String[] HEADERS = {
            "ID", "Medicine Name", "Dosage", "Scheduled Time", "Frequency", "Created At", "Last Taken Date"
    };
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Path excelFile;

    public MedicineExcelService(@Value("${app.excel.file:medicines.xlsx}") String excelFile) {
        this.excelFile = Paths.get(excelFile);
    }

    public synchronized void exportMedicines(List<Medicine> medicines) {
        Path destination = excelFile.toAbsolutePath();
        Path temporaryFile = null;

        try {
            Files.createDirectories(destination.getParent());
            temporaryFile = Files.createTempFile(destination.getParent(), "medicines-", ".xlsx");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Medicines");
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(0);
                for (int column = 0; column < HEADERS.length; column++) {
                    Cell cell = headerRow.createCell(column);
                    cell.setCellValue(HEADERS[column]);
                    cell.setCellStyle(headerStyle);
                }

                int rowIndex = 1;
                for (Medicine medicine : medicines) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(medicine.getId());
                    row.createCell(1).setCellValue(medicine.getMedicineName());
                    row.createCell(2).setCellValue(medicine.getDosage());
                    row.createCell(3).setCellValue(medicine.getTime().format(TIME_FORMAT));
                    row.createCell(4).setCellValue(medicine.getFrequency());
                    row.createCell(5).setCellValue(medicine.getCreatedAt() == null ? "" : medicine.getCreatedAt().toString());
                    row.createCell(6).setCellValue(medicine.getLastTakenDate() == null ? "" : medicine.getLastTakenDate().toString());
                }

                sheet.createFreezePane(0, 1);
                if (!medicines.isEmpty()) {
                    sheet.setAutoFilter(new CellRangeAddress(0, medicines.size(), 0, HEADERS.length - 1));
                }
                for (int column = 0; column < HEADERS.length; column++) {
                    sheet.autoSizeColumn(column);
                }

                try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                    workbook.write(output);
                }
            }

            try {
                Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw new IllegalStateException("Could not write the medicine Excel file: " + destination, exception);
        }
    }
}