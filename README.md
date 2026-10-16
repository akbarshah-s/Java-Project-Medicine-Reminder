# MEDICINE REMINDER APP — JAVA SPRING BOOT & MYSQL WEB APPLICATION

A complete, responsive, full-stack **Medicine Reminder Web Application** developed for a college Java project.

---

## 1. DEFINITION & PURPOSE

The **Medicine Reminder App** helps users remember medicines that need to be taken at specific times throughout the day. It provides an intuitive web interface backed by a persistent Java Spring Boot REST API and MySQL database.

> **IMPORTANT MEDICAL COMPLIANCE DISCLAIMER:**
> The application is ONLY a reminder system. It does NOT recommend medicines, prescribe treatments, alter prescriptions, diagnose diseases, or suggest dosages. All medicine information is entered strictly by the user according to their official medical prescription.

---

## 2. PROBLEM STATEMENT

Many patients and elderly individuals struggle to take their prescribed medications on time due to busy daily schedules or memory oversights. Missing doses or taking medicines at incorrect times can lead to reduced treatment efficacy or health complications. This project addresses the challenge by providing a centralized, automated digital reminder system that alerts users exactly when their medicines are due.

---

## 3. PROJECT OBJECTIVES

* Allow users to store medicine details permanently in a database.
* View total medicines, today's schedule, and upcoming doses chronologically.
* Provide CRUD operations (Create, Read, Update, Delete) for medicine records.
* Offer instant search capabilities for quick access to medicine records.
* Continuously compare current time (`LocalTime`) with scheduled database entries.
* Trigger live web alerts and audio chimes when a medicine is due.
* Allow users to mark medicines as taken for today.

---

## 4. TECHNOLOGIES USED

| Component | Technology |
| :--- | :--- |
| **Backend Framework** | Java 17, Spring Boot 3.2.3, Spring Web |
| **Data Access / ORM** | Spring Data JPA, Hibernate |
| **Database** | MySQL (Database: `medicine_reminder`) |
| **Spreadsheet Export** | Apache POI (`medicines.xlsx`) |
| **Build Tool** | Apache Maven (`pom.xml`) |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES6+), Font Awesome |
| **API Format** | JSON / RESTful Web Services |

---

## 5. FOUR PROJECT MODULES & TEAM RESPONSIBILITIES

The application is structured into four logical modules, divided among team members for college submission:

### MODULE 1 — MEDICINE ENTRY AND MEDICINE DETAILS
* **Handled by:** Akbarsha
* **Responsibilities:** Designed the `Medicine` class/JPA Entity, medicine creation form, field validations (`medicineName`, `dosage`, `time`, `frequency`), encapsulation, `LocalTime` mapping, and MySQL JPA mappings.

### MODULE 2 — MEDICINE LIST AND MANAGEMENT
* **Handled by:** Alen Binoy
* **Responsibilities:** Implemented viewing all medicines, search queries by name, editing medicine details, deleting records, `MedicineRepository` interface, and list processing using `ArrayList`.

### MODULE 3 — MEDICINE REMINDER AND TIME CHECKING
* **Handled by:** Harsha Vardhini
* **Responsibilities:** Built `ReminderService.java`, background `@Scheduled` database time checking, `LocalTime` comparisons, due medicine detection logic, and "Mark as Taken" status handlers.

### MODULE 4 — MAIN MENU, USER INTERACTION AND TESTING
* **Handled by:** Anjana
* **Responsibilities:** Developed main navigation sidebar, responsive dashboard layout, REST API Controllers (`MedicineController.java`), client-side fetch API handlers (`app.js`), global error handling (`GlobalExceptionHandler.java`), and full-stack integration testing.

---

## 6. DATABASE STRUCTURE (MYSQL)

Database Name: `medicine_reminder`  
Table Name: `medicines`

### SQL Table Schema (`database.sql`)

```sql
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
```

### Field Details
* `id`: BIGINT, Primary Key, Auto Increment
* `medicine_name`: VARCHAR(100), Required
* `dosage`: VARCHAR(100), Required
* `time`: TIME, Required (Mapped to Java `LocalTime`)
* `frequency`: VARCHAR(50), Required ("Once a day", "Twice a day", etc.)
* `created_at`: TIMESTAMP, Automatically set
* `last_taken_date`: DATE, Stores date when dose was last marked as taken

---

## 7. JAVA CONCEPTS DEMONSTRATED

* **Class & Object:** Encapsulated domain entities like `Medicine` and service objects (`MedicineService`, `ReminderService`).
* **Constructors:** Default No-Arg constructors for JPA and parameterized constructors for manual object instantiation.
* **Encapsulation:** Private entity fields accessed safely via standard Getters and Setters.
* **ArrayList / Collections:** Used throughout service methods for sorting and filtering medicine lists.
* **Methods:** Modular business methods (`addMedicine`, `getAllMedicines`, `updateMedicine`, `deleteMedicine`, `getDueMedicines`).
* **If/Else Conditions & Loops:** Applied in validation checks, chronological schedule sorting, and time window comparisons.
* **Switch Statement:** Used in `MedicineReminderConsoleDemo.java` menu and `app.js` UI section renderer.
* **LocalTime & Date API:** Uses `java.time.LocalTime` and `java.time.LocalDate` for precision timing.
* **Exception Handling:** Centralized `@ControllerAdvice` (`GlobalExceptionHandler.java`) returning clean JSON errors.

---

## 8. REST API ENDPOINTS

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/medicines` | Retrieve all medicines sorted by time |
| `GET` | `/api/medicines/{id}` | Retrieve a single medicine by ID |
| `POST` | `/api/medicines` | Add a new medicine record |
| `PUT` | `/api/medicines/{id}` | Update existing medicine details |
| `DELETE` | `/api/medicines/{id}` | Delete a medicine record |
| `GET` | `/api/medicines/search?name=Para` | Search medicines by name |
| `GET` | `/api/medicines/due` | Retrieve medicines currently due |
| `POST` | `/api/medicines/{id}/take` | Mark a medicine as taken for today |
| `POST` | `/api/medicines/{id}/reset-taken` | Reset taken status for testing |

---

## 9. HOW TO RUN THE APPLICATION

### Step 1: Install Prerequisites
* Java Development Kit (JDK 17 or higher)
* MySQL Community Server (v8.0+)
* Apache Maven (or use included `mvnw`)

### Step 2: Database Setup
1. Open MySQL Command Line Client or MySQL Workbench.
2. Run the script provided in `database.sql`:
   ```sql
   CREATE DATABASE medicine_reminder;
   ```
3. Update `src/main/resources/application.properties` with your MySQL username and password if required:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```

### Step 3: Run the Spring Boot Backend
Navigate to the project root directory in PowerShell or Command Prompt:

```bash
.\mvnw.cmd spring-boot:run

mvn spring-boot:run
```

Medicine changes are synchronized to `medicines.xlsx` in the application working directory. Set `MEDICINE_EXCEL_FILE` to choose a different output path.

*(Note: To run with zero-config H2 in-memory database without MySQL installed, run: `mvn spring-boot:run -Dspring-boot.run.profiles=h2`)*

### Step 4: Access Web Application
Open your web browser and navigate to:
```text
http://localhost:8080
```

---

## 10. TESTING & VERIFICATION CHECKLIST

1. **Database Persistence Test:**
   * Add a medicine (e.g. Paracetamol, 1 tablet, 10:00 AM, Once a day).
   * Stop the Spring Boot application server.
   * Restart the application server.
   * Verify Paracetamol remains present in the medicines list.

2. **CRUD Operations Test:**
   * **Create:** Add new medicine via Add Medicine form.
   * **Read:** View medicine in All Medicines table and Today's Schedule.
   * **Update:** Edit medicine time from 10:00 AM to 11:00 AM.
   * **Delete:** Click Delete and confirm in modal dialog.

3. **Automated Reminder Test:**
   * Add a test medicine with scheduled time set to 1 minute ahead of current local time.
   * Wait for scheduled time to arrive.
   * Observe active modal alert popup and chime audio notification automatically appearing.
   * Click "Mark as Taken" to mark dose complete.
