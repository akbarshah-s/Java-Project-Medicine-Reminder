package com.medicinereminder.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medicinereminder.model.Medicine;
import com.medicinereminder.service.MedicineService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {

    private final MedicineService medicineService;

    @Autowired
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<List<Medicine>> getAllMedicines() {
        List<Medicine> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(medicine);
    }

    @PostMapping
    public ResponseEntity<Medicine> addMedicine(@Valid @RequestBody Medicine medicine) {
        Medicine savedMedicine = medicineService.addMedicine(medicine);
        return new ResponseEntity<>(savedMedicine, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id, @Valid @RequestBody Medicine medicineDetails) {
        Medicine updatedMedicine = medicineService.updateMedicine(id, medicineDetails);
        return ResponseEntity.ok(updatedMedicine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Medicine deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Medicine>> searchMedicines(@RequestParam(required = false, defaultValue = "") String name) {
        List<Medicine> result = medicineService.searchMedicines(name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/due")
    public ResponseEntity<List<Medicine>> getDueMedicines() {
        List<Medicine> dueMedicines = medicineService.getDueMedicines();
        return ResponseEntity.ok(dueMedicines);
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<Medicine> markAsTaken(@PathVariable Long id) {
        Medicine medicine = medicineService.markAsTaken(id);
        return ResponseEntity.ok(medicine);
    }

    @PostMapping("/{id}/reset-taken")
    public ResponseEntity<Medicine> resetTakenStatus(@PathVariable Long id) {
        Medicine medicine = medicineService.resetTakenStatus(id);
        return ResponseEntity.ok(medicine);
    }
}
