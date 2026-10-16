package com.medicinereminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedicineReminderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicineReminderApplication.class, args);
        System.out.println("==========================================================================");
        System.out.println("  MEDICINE REMINDER APPLICATION IS RUNNING SUCCESSFULLY!                ");
        System.out.println("  Access Web Dashboard at: http://localhost:8080                         ");
        System.out.println("==========================================================================");
    }
}
