package com.medicinereminder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.medicinereminder.model.Medicine;
import com.medicinereminder.service.MedicineExcelService;

class MedicineExcelServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void exportsMedicineRowsToExcelWorkbook() throws Exception {
        Path excelFile = tempDir.resolve("medicines.xlsx");
        Medicine medicine = new Medicine(
                12L,
                "Paracetamol",
                "1 tablet",
                LocalTime.of(9, 30),
                "Once a day",
                LocalDateTime.of(2026, 10, 16, 8, 0),
                LocalDate.of(2026, 10, 16));

        new MedicineExcelService(excelFile.toString()).exportMedicines(List.of(medicine));

        assertThat(Files.exists(excelFile)).isTrue();
        try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(excelFile))) {
            assertThat(workbook.getSheet("Medicines").getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(workbook.getSheet("Medicines").getRow(1).getCell(0).getNumericCellValue()).isEqualTo(12);
            assertThat(workbook.getSheet("Medicines").getRow(1).getCell(1).getStringCellValue()).isEqualTo("Paracetamol");
            assertThat(workbook.getSheet("Medicines").getRow(1).getCell(3).getStringCellValue()).isEqualTo("09:30");
            assertThat(workbook.getSheet("Medicines").getRow(1).getCell(6).getStringCellValue()).isEqualTo("2026-10-16");
        }
    }
}