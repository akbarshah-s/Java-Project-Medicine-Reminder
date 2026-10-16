package com.medicinereminder.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medicinereminder.exception.ResourceNotFoundException;
import com.medicinereminder.model.Medicine;
import com.medicinereminder.repository.MedicineRepository;

@Service
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineExcelService medicineExcelService;

    @Autowired
    public MedicineService(MedicineRepository medicineRepository, MedicineExcelService medicineExcelService) {
        this.medicineRepository = medicineRepository;
        this.medicineExcelService = medicineExcelService;
    }

    public Medicine addMedicine(Medicine medicine) {
        if (medicine == null) {
            throw new IllegalArgumentException("Medicine details cannot be null.");
        }
        if (medicine.getMedicineName() == null || medicine.getMedicineName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be empty.");
        }
        if (medicine.getDosage() == null || medicine.getDosage().trim().isEmpty()) {
            throw new IllegalArgumentException("Dosage cannot be empty.");
        }
        if (medicine.getTime() == null) {
            throw new IllegalArgumentException("Scheduled time cannot be null.");
        }
        if (medicine.getFrequency() == null || medicine.getFrequency().trim().isEmpty()) {
            throw new IllegalArgumentException("Frequency cannot be empty.");
        }
        Medicine savedMedicine = medicineRepository.save(medicine);
        synchronizeExcelFile();
        return savedMedicine;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = medicineRepository.findAllByOrderByTimeAsc();
        return new ArrayList<>(list);
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));
    }

    public Medicine updateMedicine(Long id, Medicine updatedDetails) {
        if (updatedDetails == null) {
            throw new IllegalArgumentException("Updated medicine details cannot be null.");
        }

        Medicine existingMedicine = getMedicineById(id);

        if (updatedDetails.getMedicineName() != null && !updatedDetails.getMedicineName().trim().isEmpty()) {
            existingMedicine.setMedicineName(updatedDetails.getMedicineName().trim());
        }
        if (updatedDetails.getDosage() != null && !updatedDetails.getDosage().trim().isEmpty()) {
            existingMedicine.setDosage(updatedDetails.getDosage().trim());
        }
        if (updatedDetails.getTime() != null) {
            existingMedicine.setTime(updatedDetails.getTime());
        }
        if (updatedDetails.getFrequency() != null && !updatedDetails.getFrequency().trim().isEmpty()) {
            existingMedicine.setFrequency(updatedDetails.getFrequency().trim());
        }

        Medicine savedMedicine = medicineRepository.save(existingMedicine);
        synchronizeExcelFile();
        return savedMedicine;
    }

    public void deleteMedicine(Long id) {
        Medicine existingMedicine = getMedicineById(id);
        medicineRepository.delete(existingMedicine);
        synchronizeExcelFile();
    }

    public List<Medicine> searchMedicines(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllMedicines();
        }
        return new ArrayList<>(medicineRepository.findByMedicineNameContainingIgnoreCase(name.trim()));
    }

    public List<Medicine> getDueMedicines() {
        LocalTime now = LocalTime.now();
        List<Medicine> allMedicines = getAllMedicines();
        List<Medicine> dueList = new ArrayList<>();

        for (Medicine med : allMedicines) {
            if (!med.isTakenToday()) {
                LocalTime scheduledTime = med.getTime();
                long diffMinutes = java.time.Duration.between(scheduledTime, now).toMinutes();
                if (Math.abs(diffMinutes) <= 2) {
                    dueList.add(med);
                }
            }
        }
        return dueList;
    }

    public Medicine markAsTaken(Long id) {
        Medicine medicine = getMedicineById(id);
        medicine.setLastTakenDate(LocalDate.now());
        Medicine savedMedicine = medicineRepository.save(medicine);
        synchronizeExcelFile();
        return savedMedicine;
    }

    public Medicine resetTakenStatus(Long id) {
        Medicine medicine = getMedicineById(id);
        medicine.setLastTakenDate(null);
        Medicine savedMedicine = medicineRepository.save(medicine);
        synchronizeExcelFile();
        return savedMedicine;
    }

    private void synchronizeExcelFile() {
        medicineExcelService.exportMedicines(medicineRepository.findAllByOrderByTimeAsc());
    }
}
