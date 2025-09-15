# 📚 Week 1-2: Ledger & Transaction

## 🎯 Mục tiêu

Hiểu cách ngân hàng vận hành và quản lý giao dịch.

## 📖 Nội dung học tập

### 1. Ledger (Sổ cái)

- **Account-based model**: Mỗi tài khoản có số dư, giao dịch cập nhật số dư
- **UTXO model**: Mỗi giao dịch tạo ra output mới, input được "tiêu thụ"
- **Double-entry bookkeeping**: Mỗi giao dịch phải cân bằng (debit = credit)

### 2. Database cho Banking

- **PostgreSQL**: ACID properties, transaction isolation levels, indexing strategies
- **Oracle DB**: Enterprise features, partitioning, advanced security

### 3. Thực hành xây dựng

- App quản lý tài khoản (tạo, xóa, cập nhật)
- Chức năng chuyển tiền với validation
- Logging và audit trail

## 🏗️ Kiến trúc Banking System

### Core Banking Nodes

- **Account Management Node**: Quản lý tài khoản khách hàng
- **Transaction Processing Node**: Xử lý giao dịch, validation và authorization
- **Payment Gateway Node**: Kết nối với payment networks

### Regulatory & Compliance Nodes

- **Central Bank Node**: Monetary policy, currency management
- **Regulatory Reporting Node**: Basel III compliance, Anti-money laundering
- **Credit Bureau Node**: Credit scoring, risk assessment

### Network & Infrastructure Nodes

- **SWIFT Network Node**: Cross-border messaging, payment instructions
- **ACH Network Node**: Domestic payments, batch processing
- **Card Network Node**: Card transactions, authorization

## 🔄 Transaction Processing

### ACID Properties

- **Atomicity**: Tất cả operations hoặc thành công hoặc rollback
- **Consistency**: Dữ liệu luôn ở trạng thái hợp lệ
- **Isolation**: Transactions độc lập với nhau
- **Durability**: Dữ liệu được lưu trữ vĩnh viễn

### Pessimistic Locking (Banking Standard)

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.accountId = :accountId")
Optional<Account> findByIdForUpdate(@Param("accountId") UUID accountId);
```

### Rollback Mechanism

- **Spring Transaction Management**: Tự động rollback khi có Exception
- **Database WAL**: Write-Ahead Logging để đảm bảo consistency
- **Connection Pooling**: HikariCP với 50 connections

## 💻 Banking App Implementation

### Entity Layer

- **Customer**: Thông tin khách hàng
- **Account**: Tài khoản với balance và status
- **Transaction**: Giao dịch với status và audit trail

### Repository Layer

- **Custom Methods**: `findByIdForUpdate`, `findByIdForRead`
- **Pessimistic Locking**: Đảm bảo concurrent safety
- **Type-safe Queries**: JPQL với parameter binding

### Service Layer

- **BankingService**: Core banking operations
- **Transaction Processing**: Transfer, deposit, withdraw
- **Error Handling**: Custom exceptions cho banking

### Controller Layer

- **RESTful APIs**: Customer, Account, Transaction endpoints
- **Error Handling**: Proper HTTP status codes
- **Request/Response DTOs**: API data transfer objects

## 🎯 Kết quả đạt được

### Lý thuyết

- ✅ Hiểu rõ Account-based vs UTXO model
- ✅ Nắm vững Double-entry bookkeeping
- ✅ Hiểu banking nodes architecture
- ✅ Biết T+2 settlement và lý do

### Thực hành

- ✅ PostgreSQL fundamentals
- ✅ Database schema design
- ✅ ACID transaction processing
- ✅ Pessimistic locking implementation
- ✅ Banking app hoàn chỉnh

### Kỹ năng

- ✅ Spring Boot + JPA
- ✅ Custom repository methods
- ✅ Transaction management
- ✅ Error handling
- ✅ Banking standards

## 🚀 Bước tiếp theo

Sẵn sàng cho **Week 3-4: Payment System** - Học về ACH, SWIFT, VISA/Mastercard

