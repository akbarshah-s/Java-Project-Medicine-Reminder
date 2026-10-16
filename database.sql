CREATE DATABASE IF NOT EXISTS medicine_reminder;

USE medicine_reminder;

CREATE TABLE IF NOT EXISTS medicines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    time TIME NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_taken_date DATE DEFAULT NULL
);

INSERT INTO medicines (medicine_name, dosage, time, frequency) VALUES
('Vitamin D3', '1 tablet', '08:00:00', 'Once a day'),
('Paracetamol 650mg', '1 tablet', '10:00:00', 'Twice a day'),
('Amoxicillin 500mg', '1 capsule', '14:00:00', 'Three times a day'),
('Omeprazole 20mg', '1 capsule', '20:00:00', 'Once a day');

SELECT * FROM medicines;
