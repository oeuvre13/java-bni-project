# Java BNI Projecto

This is a simple Spring Boot API that provides user **registration** and **login** functionality using **Oracle SQL** as the database.

---

## 🚀 Features

- User registration (`/api/auth/register`)hggfhhgfhgkfh
- User login (`/api/auth/login`)
- Password encryption with BCrypt
- Spring Data JPA for database interaction
- Spring Security (only for password encoding)
- Oracle Database integration

---

## 🧱 Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- Spring Security (minimal config)
- Oracle Database XE
- Maven

---

## 📦 Project Structure

```
src/main/java/com/bni/bni/
├── config/           # Security config
├── controller/       # API endpoints
├── entity/           # JPA entities
├── repository/       # Spring Data JPA interfaces
├── service/          # Business logic
```

---

## ⚙️ Setup & Run

### 1. Prerequisites

- Java 17+
- Maven
- Oracle Database (XE or standard)
- Postman (for testing)

### 2. Clone the Repository

```bash
git clone https://github.com/frammawiliansyah/java-bni-project.git
cd java-bni-project
```

### 3. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=auth_user
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```

> Make sure your Oracle DB is running and the user has sufficient privileges.

### 4. Create Database Table

```sql
-- Connect as admin (e.g., SYS or SYSTEM) and run:
CREATE USER auth_user IDENTIFIED BY your_password;
GRANT CONNECT, RESOURCE TO auth_user;
ALTER USER auth_user QUOTA UNLIMITED ON USERS;

-- Login as auth_user and run:
CREATE TABLE users (
  username VARCHAR2(50) PRIMARY KEY,
  password_hash VARCHAR2(255),
  role VARCHAR2(20)
);
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

---

## 🧪 API Endpoints (Test with Postman)

### Register
`POST /api/auth/register`  
**Body (JSON):**
```json
{
  "username": "demo",
  "password": "123456"
}
```

### Login
`POST /api/auth/login`  
**Body (JSON):**
```json
{
  "username": "demo",
  "password": "123456"
}
```

---

## 📚 Resources

- Spring Boot Docs: https://spring.io/projects/spring-boot  
- Oracle JDBC Docs: https://docs.oracle.com/en/database/oracle  
- Spring Security Password Encoding: https://www.baeldung.com/spring-security-registration-password-encoding  

---

hai2

## 📌 Notes

- The project uses Spring Security only for password encryption; it does not enforce authentication on protected routes.
- No JWT implementation yet (can be added as next step).
