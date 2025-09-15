# 🏦 Mini Banking System

Hệ thống ngân hàng mini được xây dựng với Java Spring Boot + PostgreSQL để học về Core Banking và chuẩn bị cho Blockchain.

## 🚀 Tính năng

- ✅ Quản lý khách hàng (Customer Management)
- ✅ Quản lý tài khoản (Account Management)
- ✅ Chuyển tiền (Money Transfer)
- ✅ Nạp tiền (Deposit)
- ✅ Rút tiền (Withdrawal)
- ✅ Lịch sử giao dịch (Transaction History)
- ✅ **Pessimistic Locking** (Banking Standard)
- ✅ **ACID Transactions** với Rollback
- ✅ **Connection Pooling** (HikariCP)
- ✅ **Transaction Timeout** (30 giây)
- ✅ **Custom Exceptions** cho Banking
- ✅ Database Schema hoàn chỉnh

## 🛠️ Tech Stack

- **Backend**: Java 17 + Spring Boot 3.2.0
- **Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA + Hibernate
- **Build Tool**: Maven
- **API**: RESTful APIs

## 📋 Yêu cầu hệ thống

- Java 17+
- PostgreSQL 15+
- Maven 3.6+

## 🚀 Cài đặt và chạy

### 1. Cài đặt PostgreSQL

```bash
# macOS
brew install postgresql
brew services start postgresql

# Tạo database
psql -U postgres
CREATE DATABASE mini_banking;
\q
```

### 2. Setup Database

```bash
# Chạy script setup database
psql -U postgres -d mini_banking -f database-setup.sql
```

### 3. Chạy ứng dụng

```bash
# Clone project
cd /Users/dantt1002/projects/mini-banking

# Build và chạy
mvn clean install
mvn spring-boot:run
```

### 4. Kiểm tra ứng dụng

Ứng dụng sẽ chạy tại: http://localhost:8080

## 📚 API Endpoints

### Customer Management

- `POST /api/banking/customers` - Tạo khách hàng mới
- `GET /api/banking/customers/{customerId}` - Lấy thông tin khách hàng
- `GET /api/banking/customers` - Lấy danh sách khách hàng
- `PUT /api/banking/customers/{customerId}` - Cập nhật khách hàng

### Account Management

- `POST /api/banking/accounts` - Tạo tài khoản mới
- `GET /api/banking/accounts/{accountId}` - Lấy thông tin tài khoản
- `GET /api/banking/accounts/number/{accountNumber}` - Lấy tài khoản theo số
- `GET /api/banking/customers/{customerId}/accounts` - Lấy tài khoản của khách hàng
- `GET /api/banking/accounts/{accountId}/balance` - Lấy số dư tài khoản

### Transaction Management

- `POST /api/banking/transactions/transfer` - Chuyển tiền
- `POST /api/banking/transactions/deposit` - Nạp tiền
- `POST /api/banking/transactions/withdraw` - Rút tiền
- `GET /api/banking/transactions/{transactionCode}` - Lấy thông tin giao dịch
- `GET /api/banking/accounts/{accountId}/transactions` - Lịch sử giao dịch

## 🧪 Test API với cURL

### 1. Tạo khách hàng mới

```bash
curl -X POST http://localhost:8080/api/banking/customers \
  -H "Content-Type: application/json" \
  -d '{
    "customerCode": "CUST004",
    "firstName": "Pham",
    "lastName": "Van D",
    "email": "phamvand@email.com",
    "phone": "0901234570",
    "idType": "CCCD",
    "idNumber": "123456792",
    "kycStatus": "VERIFIED"
  }'
```

### 2. Tạo tài khoản mới

```bash
curl -X POST http://localhost:8080/api/banking/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567893",
    "customer": {
      "customerId": "CUSTOMER_ID_FROM_STEP_1"
    },
    "accountType": "SAVINGS",
    "balance": 5000000.00,
    "availableBalance": 5000000.00
  }'
```

### 3. Chuyển tiền

```bash
curl -X POST http://localhost:8080/api/banking/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "FROM_ACCOUNT_ID",
    "toAccountId": "TO_ACCOUNT_ID",
    "amount": 1000000.00,
    "description": "Chuyen tien test"
  }'
```

### 4. Nạp tiền

```bash
curl -X POST http://localhost:8080/api/banking/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACCOUNT_ID",
    "amount": 2000000.00,
    "description": "Nap tien test"
  }'
```

### 5. Rút tiền

```bash
curl -X POST http://localhost:8080/api/banking/transactions/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACCOUNT_ID",
    "amount": 500000.00,
    "description": "Rut tien test"
  }'
```

## 🗄️ Database Schema

### Customers Table

- `customer_id` (UUID, Primary Key)
- `customer_code` (VARCHAR, Unique)
- `first_name`, `last_name` (VARCHAR)
- `email` (VARCHAR, Unique)
- `phone`, `address`, `city` (VARCHAR)
- `kyc_status`, `risk_level`, `status` (ENUM)
- `created_at`, `updated_at` (TIMESTAMP)

### Accounts Table

- `account_id` (UUID, Primary Key)
- `account_number` (VARCHAR, Unique)
- `customer_id` (UUID, Foreign Key)
- `account_type` (ENUM: SAVINGS, CHECKING, LOAN, INVESTMENT)
- `balance`, `available_balance` (DECIMAL)
- `credit_limit`, `interest_rate` (DECIMAL)
- `status` (ENUM: ACTIVE, INACTIVE, FROZEN, CLOSED)

### Transactions Table

- `transaction_id` (UUID, Primary Key)
- `transaction_code` (VARCHAR, Unique)
- `from_account_id`, `to_account_id` (UUID, Foreign Keys)
- `amount` (DECIMAL)
- `transaction_type` (ENUM: TRANSFER, DEPOSIT, WITHDRAWAL, PAYMENT, FEE)
- `status` (ENUM: PENDING, COMPLETED, FAILED, CANCELLED)
- `description`, `failure_reason` (TEXT)

## 🔒 Banking Standards

### **Pessimistic Locking (Banking Standard)**

- **Row-Level Locking**: Lock tài khoản trước khi cập nhật
- **FOR UPDATE**: Sử dụng `SELECT ... FOR UPDATE` để lock
- **High Reliability**: Đảm bảo 100% consistency
- **No Race Conditions**: Tránh concurrent access issues

### **ACID Properties**

- **Atomicity**: Tất cả operations trong transaction hoặc thành công hoặc rollback
- **Consistency**: Dữ liệu luôn ở trạng thái hợp lệ
- **Isolation**: Transactions độc lập với nhau
- **Durability**: Dữ liệu được lưu trữ vĩnh viễn

### **Connection Pooling**

- **HikariCP**: High-performance connection pool
- **Maximum Pool Size**: 50 connections
- **Connection Timeout**: 30 seconds
- **Leak Detection**: 60 seconds

## 🎯 Mục tiêu học tập

1. **Hiểu Core Banking**: Cách ngân hàng quản lý tài khoản và giao dịch
2. **Database Design**: Thiết kế schema cho banking system
3. **ACID Transactions**: Xử lý giao dịch với rollback mechanism
4. **RESTful APIs**: Xây dựng APIs cho banking operations
5. **Chuẩn bị cho Blockchain**: Hiểu foundation trước khi học blockchain

## 🚀 Bước tiếp theo

Sau khi hoàn thành project này, bạn sẽ:

- Hiểu rõ cách banking system hoạt động
- Có foundation vững chắc cho blockchain
- Sẵn sàng học Giai đoạn 2: Cryptography & Security

## 📞 Support

Nếu có vấn đề gì, hãy check:

1. PostgreSQL đã chạy chưa
2. Database `mini_banking` đã tạo chưa
3. Port 8080 có bị conflict không
4. Java version có đúng 17+ không
