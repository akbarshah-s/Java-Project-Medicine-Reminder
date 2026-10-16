package com.medicinereminder.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.medicinereminder.model.Medicine;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private final MedicineService medicineService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    @Autowired
    public ReminderService(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Scheduled(fixedRate = 10000)
    public void checkForDueMedicines() {
        LocalTime currentTime = LocalTime.now();
        List<Medicine> dueMedicines = medicineService.getDueMedicines();

        if (!dueMedicines.isEmpty()) {
            logger.info("=================================================");
            logger.info("  [MEDICINE REMINDER ALERT] Current Time: {}", currentTime.format(timeFormatter));
            for (Medicine med : dueMedicines) {
                logger.info("  -> DUE NOW: {} | Dosage: {} | Scheduled: {}", 
                        med.getMedicineName(), med.getDosage(), med.getTime().format(timeFormatter));
            }
            logger.info("=================================================");
        }
    }

    public List<Medicine> getCurrentDueMedicines() {
        return medicineService.getDueMedicines();
    }
}
