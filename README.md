# 📚 Hazaribagh Libraries (LibConnect)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

**A hyperlocal digital marketplace connecting students with private study centers (libraries) in Hazaribagh.** *Bridging the gap between students seeking affordable study spaces and library owners seeking digital management.*

---

## 🚀 Project Overview

**Hazaribagh Libraries** is a full-stack web application designed to digitize the manual booking process of private libraries in education hubs like Hazaribagh (Matwari, Korrah, Babu Gaon). 

It replaces the traditional "visit-and-pay" model with a seamless digital experience, featuring a unique **"Customer Acquisition" engine** that offers dynamic pricing for first-time users.

### 💡 Key Features
* **Dynamic Pricing Engine:** Automatically detects first-time users to apply a "Trial Month" discount (₹350 instead of ₹400).
* **Split-Screen Transaction UI:** A modern, conversion-focused checkout experience that handles Login and Payment on a single screen.
* **Real-Time Seat Counter:** Uses optimistic locking to ensure seat counts are accurate and prevent double-booking.
* **Smart Search:** Geo-location based filtering (e.g., "Libraries in Matwari").
* **Secure Auth:** Google OAuth2 Login for students + JWT-based role protection for Admins.

---

## 🛠️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA |
| **Frontend** | React.js, Tailwind CSS, Lucide Icons, Axios |
| **Database** | MySQL 8.0 |
| **Auth** | OAuth2 (Google), JWT (JSON Web Tokens) |
| **Payment** | Razorpay Integration (Test Mode) |
| **Tools** | Docker, Maven, Postman, Git |


---

## ⚙️ Installation & Setup

Follow these steps to run the project locally.

### 1. Backend Setup (Spring Boot)
```bash
# Clone the repository
git clone [https://github.com/your-username/hazaribagh-libraries.git](https://github.com/your-username/hazaribagh-libraries.git)

# Navigate to backend
cd backend

# Configure Database
# Open src/main/resources/application.properties and update:
# spring.datasource.username=root
# spring.datasource.password=your_password

# Run the app
mvn spring-boot:run
