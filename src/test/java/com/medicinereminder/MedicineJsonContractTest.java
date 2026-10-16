package com.medicinereminder;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medicinereminder.model.Medicine;

class MedicineJsonContractTest {

    @Test
    void isTakenTodayShouldSerializeAsExpectedJsonProperty() throws Exception {
        Medicine medicine = new Medicine();
        medicine.setMedicineName("Paracetamol");
        medicine.setDosage("1 tablet");
        medicine.setTime(LocalTime.of(9, 0));
        medicine.setFrequency("Once a day");
        medicine.setLastTakenDate(LocalDate.now());

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(medicine);

        assertThat(json).contains("\"isTakenToday\":true");
    }
}
