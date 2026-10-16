package com.medicinereminder;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import com.medicinereminder.model.Medicine;

public class MedicineReminderConsoleDemo {

    private static final ArrayList<Medicine> medicineList = new ArrayList<>();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        seedInitialData();

        boolean running = true;
        System.out.println("=================================================");
        System.out.println("   MEDICINE REMINDER - JAVA CONSOLE DEMO         ");
        System.out.println("=================================================");

        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-6): ");
            String choiceInput = scanner.nextLine();

            switch (choiceInput) {
                case "1":
                    addMedicineConsole(scanner);
                    break;
                case "2":
                    viewMedicinesConsole();
                    break;
                case "3":
                    editMedicineConsole(scanner);
                    break;
                case "4":
                    deleteMedicineConsole(scanner);
                    break;
                case "5":
                    checkReminderConsole();
                    break;
                case "6":
                    running = false;
                    System.out.println("\nExiting Console Demo. Goodbye!");
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please enter a number from 1 to 6.");
            }
        }
        scanner.close();
    }

    private static void seedInitialData() {
        medicineList.add(new Medicine(1L, "Paracetamol", "1 tablet", LocalTime.of(10, 0), "Once a day", null, null));
        medicineList.add(new Medicine(2L, "Vitamin D3", "1 tablet", LocalTime.of(20, 0), "Once a day", null, null));
    }

    private static void printMenu() {
        System.out.println("\n-------------------------------------------------");
        System.out.println("MAIN MENU");
        System.out.println("1. Add Medicine");
        System.out.println("2. View All Medicines");
        System.out.println("3. Edit Medicine");
        System.out.println("4. Delete Medicine");
        System.out.println("5. Check Current Reminder");
        System.out.println("6. Exit Console Demo");
        System.out.println("-------------------------------------------------");
    }

    private static void addMedicineConsole(Scanner scanner) {
        System.out.println("\n--- ADD NEW MEDICINE ---");
        System.out.print("Enter Medicine Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Dosage (e.g., 1 tablet): ");
        String dosage = scanner.nextLine().trim();

        LocalTime time = null;
        while (time == null) {
            System.out.print("Enter Time (HH:mm 24-hr format, e.g., 10:00 or 14:30): ");
            String timeStr = scanner.nextLine().trim();
            try {
                time = LocalTime.parse(timeStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format! Please use HH:mm (e.g. 09:30).");
            }
        }

        System.out.print("Enter Frequency (e.g., Once a day): ");
        String frequency = scanner.nextLine().trim();

        long newId = medicineList.size() + 1;
        Medicine medicine = new Medicine(newId, name, dosage, time, frequency, null, null);
        medicineList.add(medicine);

        System.out.println("[✓] Medicine added successfully: " + name);
    }

    private static void viewMedicinesConsole() {
        System.out.println("\n--- ALL MEDICINES ---");
        if (medicineList.isEmpty()) {
            System.out.println("No medicines added yet.");
            return;
        }

        for (int i = 0; i < medicineList.size(); i++) {
            Medicine m = medicineList.get(i);
            System.out.printf("[%d] ID: %d | Name: %s | Dosage: %s | Time: %s | Frequency: %s\n",
                    (i + 1), m.getId(), m.getMedicineName(), m.getDosage(), m.getTime(), m.getFrequency());
        }
    }

    private static void editMedicineConsole(Scanner scanner) {
        viewMedicinesConsole();
        if (medicineList.isEmpty()) return;

        System.out.print("\nEnter ID of Medicine to Edit: ");
        String idStr = scanner.nextLine();
        try {
            long id = Long.parseLong(idStr);
            Medicine target = null;
            for (Medicine m : medicineList) {
                if (m.getId() == id) {
                    target = m;
                    break;
                }
            }

            if (target == null) {
                System.out.println("[!] Medicine ID not found.");
                return;
            }

            System.out.print("Enter New Name (press Enter to keep '" + target.getMedicineName() + "'): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) target.setMedicineName(newName);

            System.out.print("Enter New Dosage (press Enter to keep '" + target.getDosage() + "'): ");
            String newDosage = scanner.nextLine().trim();
            if (!newDosage.isEmpty()) target.setDosage(newDosage);

            System.out.print("Enter New Time HH:mm (press Enter to keep '" + target.getTime() + "'): ");
            String newTimeStr = scanner.nextLine().trim();
            if (!newTimeStr.isEmpty()) {
                target.setTime(LocalTime.parse(newTimeStr, TIME_FORMATTER));
            }

            System.out.print("Enter New Frequency (press Enter to keep '" + target.getFrequency() + "'): ");
            String newFreq = scanner.nextLine().trim();
            if (!newFreq.isEmpty()) target.setFrequency(newFreq);

            System.out.println("[✓] Medicine updated successfully.");

        } catch (Exception e) {
            System.out.println("[!] Invalid input error.");
        }
    }

    private static void deleteMedicineConsole(Scanner scanner) {
        viewMedicinesConsole();
        if (medicineList.isEmpty()) return;

        System.out.print("\nEnter ID of Medicine to Delete: ");
        String idStr = scanner.nextLine();
        try {
            long id = Long.parseLong(idStr);
            boolean removed = medicineList.removeIf(m -> m.getId() == id);
            if (removed) {
                System.out.println("[✓] Medicine deleted successfully.");
            } else {
                System.out.println("[!] Medicine ID not found.");
            }
        } catch (Exception e) {
            System.out.println("[!] Invalid input format.");
        }
    }

    private static void checkReminderConsole() {
        LocalTime now = LocalTime.now();
        System.out.println("\n--- CURRENT TIME CHECK: " + now.format(TIME_FORMATTER) + " ---");
        boolean foundDue = false;

        for (Medicine m : medicineList) {
            if (m.getTime().getHour() == now.getHour() && m.getTime().getMinute() == now.getMinute()) {
                System.out.println("********************************");
                System.out.println("        MEDICINE REMINDER       ");
                System.out.println("********************************");
                System.out.println("It is time to take your medicine!");
                System.out.println("Medicine: " + m.getMedicineName());
                System.out.println("Dosage:   " + m.getDosage());
                System.out.println("Time:     " + m.getTime());
                System.out.println("********************************");
                foundDue = true;
            }
        }

        if (!foundDue) {
            System.out.println("No medicine is due at this exact time (" + now.format(TIME_FORMATTER) + ").");
        }
    }
}
