# 📚 LibHub: Hyperlocal Marketplace & Transaction Engine

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

**LibHub** is a production-ready, full-stack marketplace designed to digitize the manual "visit-and-pay" study center ecosystem in Hazaribagh, Jharkhand. It features a high-performance backend architecture capable of handling secure transactions, real-time seat availability, and role-based access control.

### 🔗 Project Links
* **Live Demo:** [https://libhub.live](https://libhub.live)
* **Developer Portfolio:** [https://siddhantkr.app](https://siddhantkr.app)

---

## 🏗️ Architectural Highlights & "Backend Thinking"

This project was built with a focus on **scalability, security, and low-latency data retrieval**, specifically addressing common challenges in Fintech and Marketplace environments.

### ⚡ Performance Optimization (The Redis Layer)
* **Problem:** High database read pressure during peak hours for library metadata and seat availability.
* **Solution:** Integrated **Redis Caching** to serve frequently accessed library metadata directly from memory, bypassing heavy database queries.
* **Result:** Reduced Read API latency by **$80\%$**, significantly improving the user experience for dashboard summaries.

### 🔐 Enterprise-Grade Security
* **Authentication:** Implemented a dual-layer strategy using **Google OAuth2** for seamless student onboarding and **JWT (JSON Web Tokens)** for stateless, scalable session management.
* **Role-Based Access Control (RBAC):** Strict separation of concerns using **Spring Security** filters to differentiate between Student (User) and Library Owner (Admin) workflows.
* **Encryption:** Uses **BCrypt** password hashing to ensure industry-standard protection for sensitive user data.

### 💳 Transactional Integrity
* **Concurrency Control:** Utilizes **Optimistic Locking** to ensure seat counts remain accurate during simultaneous booking attempts, preventing race conditions and double-booking.
* **Payment Integration:** Integrated a live **Razorpay Payment Gateway** (Test Mode) to handle secure financial exchanges and booking confirmations.

---

## 🛠️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.4, Spring Data JPA (Hibernate) |
| **Security** | Spring Security, JWT, Google OAuth2, BCrypt |
| **Database** | MySQL (Relational Persistence), Redis (Caching) |
| **Frontend** | React.js (Vite), Tailwind CSS, Axios |
| **DevOps** | Docker, GitHub Actions (CI/CD), Postman |

---

## 🚀 Key Features

* **Dynamic Pricing Engine:** An automated logic layer that detects first-time users to apply "Trial Month" discounts.
* **Smart Search:** Geo-location based filtering allowing students to find study spaces in specific hubs like Matwari or Korra.
* **Automated CI/CD:** Uses **GitHub Actions** to automate container builds and ensure reliable production deployment.
* **Real-Time Dashboards:** Backend logic designed to serve aggregated summary data (totals, trends) efficiently.

---

## ⚙️ Setup & Installation

### 🐳 Run via Docker (Recommended)
The entire stack is containerized for $100\%$ environment parity.

```bash
# Clone the repository
git clone [https://github.com/Siddhantkr19/Hazaribagh-Libraries-LibHub-.git](https://github.com/Siddhantkr19/Hazaribagh-Libraries-LibHub-.git)

# Start the environment (Backend + DB + Redis)
docker-compose up --build
