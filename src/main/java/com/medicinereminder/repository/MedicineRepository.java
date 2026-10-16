package com.medicinereminder.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicinereminder.model.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByMedicineNameContainingIgnoreCase(String medicineName);

    List<Medicine> findAllByOrderByTimeAsc();

    List<Medicine> findByTimeBetween(LocalTime startTime, LocalTime endTime);
}
