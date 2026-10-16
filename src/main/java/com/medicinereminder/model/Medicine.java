package com.medicinereminder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Medicine name is required")
    @Column(name = "medicine_name", nullable = false, length = 100)
    private String medicineName;

    @NotBlank(message = "Dosage is required")
    @Column(name = "dosage", nullable = false, length = 100)
    private String dosage;

    @NotNull(message = "Scheduled time is required")
    @Column(name = "time", nullable = false)
    private LocalTime time;

    @NotBlank(message = "Frequency is required")
    @Column(name = "frequency", nullable = false, length = 50)
    private String frequency;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_taken_date")
    private LocalDate lastTakenDate;

    public Medicine() {
        this.createdAt = LocalDateTime.now();
    }

    public Medicine(String medicineName, String dosage, LocalTime time, String frequency) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.time = time;
        this.frequency = frequency;
        this.createdAt = LocalDateTime.now();
        this.lastTakenDate = null;
    }

    public Medicine(Long id, String medicineName, String dosage, LocalTime time, String frequency, LocalDateTime createdAt, LocalDate lastTakenDate) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.time = time;
        this.frequency = frequency;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastTakenDate = lastTakenDate;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @JsonProperty("isTakenToday")
    public boolean isTakenToday() {
        return lastTakenDate != null && lastTakenDate.equals(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getLastTakenDate() {
        return lastTakenDate;
    }

    public void setLastTakenDate(LocalDate lastTakenDate) {
        this.lastTakenDate = lastTakenDate;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", medicineName='" + medicineName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", time=" + time +
                ", frequency='" + frequency + '\'' +
                ", createdAt=" + createdAt +
                ", lastTakenDate=" + lastTakenDate +
                '}';
    }
}
