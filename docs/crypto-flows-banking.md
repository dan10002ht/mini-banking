# 🔐 Crypto Flows in Banking System

## 📋 Tổng quan

Tài liệu này mô tả cách sử dụng kết hợp các thuật toán mã hóa trong hệ thống banking thực tế.

## 🔑 Các thuật toán được sử dụng

- **RSA**: Mã hóa dữ liệu, ký số documents, quản lý certificates
- **ECDSA**: Ký số transactions, xác thực danh tính, mobile authentication
- **Diffie-Hellman**: Key exchange, thiết lập kênh bảo mật, session management
- **SHA256**: Hash functions, Merkle trees, data integrity

---

## 💰 Flow 1: Chuyển tiền qua Mobile Banking

### **Bước 1: Thiết lập kênh bảo mật**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. App mở kết nối
2. Diffie-Hellman: TLS handshake
3. Tạo session key cho communication
4. Kênh bảo mật được thiết lập
```

**Mục đích**: Đảm bảo toàn bộ communication được mã hóa

### **Bước 2: Xác thực danh tính**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. User nhập PIN + Fingerprint
2. App gửi PIN + Fingerprint data qua TLS
3. Server verify PIN với database
4. Server verify Fingerprint với biometric system
5. Xác thực thành công → Tạo session
```

**Mục đích**: Chứng minh user thực sự là chủ tài khoản

### **Bước 3: Tạo transaction**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. User nhập: "Chuyển 100 triệu VND từ ACC001 đến ACC002"
2. App tạo transaction object
3. User nhập PIN để xác thực
4. App gửi transaction + PIN qua TLS
5. Server verify PIN với database
6. Server ký số transaction với private key (lưu trong database)
7. Server thực hiện giao dịch
```

**Mục đích**: Đảm bảo transaction được xác thực và ký số bởi server

### **Bước 4: Xử lý transaction**

```
🏦 Banking Server
     ↓
1. Kiểm tra số dư, thông tin tài khoản
2. Thực hiện giao dịch
3. Cập nhật database
4. Gửi confirmation về app
```

**Mục đích**: Xử lý giao dịch một cách an toàn và chính xác

---

## 🔐 Server-side Signing Explanation

### **Tại sao dùng Server-side Signing:**

#### **❌ Client-side Signing (Không an toàn):**

```
App có private key → Ký số transaction → Gửi lên server
```

**Vấn đề:**

- Private key có thể bị lộ qua reverse engineering
- App không đáng tin cậy
- Khó quản lý private key

#### **✅ Server-side Signing (An toàn):**

```
App gửi PIN → Server verify → Lấy private key từ database → Ký số
```

**Lợi ích:**

- Private key được bảo vệ trong database (encrypted)
- Server đáng tin cậy hơn app
- Dễ quản lý và audit

### **Flow chi tiết:**

```
1. User nhập transaction + PIN
2. App gửi qua TLS (mã hóa)
3. Server verify PIN với database
4. Server lấy private key từ database (decrypt)
5. Server ký số transaction với private key
6. Server thực hiện giao dịch
```

---

## 💳 Flow 2: Chuyển tiền qua Internet Banking

### **Bước 1: Thiết lập HTTPS**

```
🌐 Web Browser         🏦 Banking Server
     ↓                        ↓
1. User truy cập https://vietcombank.com
2. Browser verify SSL certificate
3. Diffie-Hellman: TLS handshake
4. Kênh HTTPS được thiết lập
```

**Mục đích**: Đảm bảo communication được mã hóa qua TLS

### **Bước 2: Đăng nhập**

```
🌐 Web Browser         🏦 Banking Server
     ↓                        ↓
1. User nhập username/password
2. Browser gửi qua HTTPS (TLS encryption)
3. Server verify credentials
4. Tạo session token
5. Gửi token về browser
```

**Mục đích**: Xác thực danh tính user

### **Bước 3: Tạo transaction**

```
🌐 Web Browser         🏦 Banking Server
     ↓                        ↓
1. User nhập thông tin chuyển tiền
2. Browser gửi qua HTTPS (TLS encryption)
3. Server tạo transaction object
4. Server ký số transaction với private key của bank
5. Lưu transaction vào database
```

**Mục đích**: Tạo và lưu trữ transaction an toàn

### **Bước 4: Xác thực OTP**

```
🌐 Web Browser         🏦 Banking Server
     ↓                        ↓
1. Server gửi OTP qua SMS
2. User nhập OTP
3. Browser gửi OTP qua HTTPS
4. Server verify OTP
5. Thực hiện giao dịch
```

**Mục đích**: Xác thực bổ sung cho giao dịch

---

## 🏦 Flow 3: Tạo tài khoản mới

### **Bước 1: Thu thập thông tin KYC**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. User nhập thông tin cá nhân
2. Chụp ảnh CCCD, selfie
3. RSA: Mã hóa thông tin KYC với public key của bank
4. Gửi encrypted data qua TLS
5. Server giải mã và lưu vào database
```

**Mục đích**: Bảo vệ thông tin cá nhân nhạy cảm

### **Bước 2: Tạo key pair cho user**

```
🏦 Banking Server
     ↓
1. ECDSA: Tạo key pair cho user
2. Lưu private key vào database (encrypted)
3. Lưu public key vào database
4. Gửi public key về app
```

**Mục đích**: Tạo cặp khóa cho user để ký số giao dịch

### **Bước 3: Xác thực danh tính**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. User nhập OTP từ SMS
2. ECDSA: Ký số confirmation với private key của user
3. Gửi signed confirmation qua TLS
4. Server verify signature
5. Kích hoạt tài khoản
```

**Mục đích**: Xác thực danh tính trước khi kích hoạt tài khoản

---

## 🔐 Flow 4: Quản lý mật khẩu

### **Bước 1: Tạo mật khẩu**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. User nhập mật khẩu mới
2. SHA256: Hash mật khẩu với salt
3. RSA: Mã hóa hashed password với public key của bank
4. Gửi encrypted password qua TLS
5. Server lưu vào database
```

**Mục đích**: Bảo vệ mật khẩu ngay cả khi database bị tấn công

### **Bước 2: Xác thực mật khẩu**

```
📱 Mobile App          🏦 Banking Server
     ↓                        ↓
1. User nhập mật khẩu
2. SHA256: Hash mật khẩu với salt
3. RSA: Mã hóa hashed password với public key của bank
4. Gửi encrypted password qua TLS
5. Server so sánh với password đã lưu
```

**Mục đích**: Xác thực mật khẩu mà không lộ thông tin

---

## 📊 Flow 5: Báo cáo giao dịch

### **Bước 1: Tạo báo cáo**

```
🏦 Banking Server
     ↓
1. Truy vấn giao dịch từ database
2. Tạo báo cáo PDF
3. RSA: Ký số báo cáo với private key của bank
4. Lưu báo cáo đã ký
```

**Mục đích**: Đảm bảo tính toàn vẹn của báo cáo

### **Bước 2: Gửi báo cáo**

```
🏦 Banking Server         📧 Email System
     ↓                        ↓
1. RSA: Mã hóa báo cáo với public key của user
2. Gửi encrypted báo cáo qua email
3. User nhận email
4. RSA: Giải mã báo cáo với private key của user
5. ECDSA: Verify signature của bank
```

**Mục đích**: Gửi báo cáo an toàn và đảm bảo tính toàn vẹn

---

## 🔄 Flow 6: Đồng bộ dữ liệu giữa các hệ thống

### **Bước 1: Thiết lập kênh bảo mật**

```
🏦 Core Banking         🏦 Mobile Banking
     ↓                        ↓
1. Diffie-Hellman: Key exchange
2. Tạo shared secret key
3. Kênh bảo mật được thiết lập
```

**Mục đích**: Thiết lập kênh bảo mật giữa các hệ thống

### **Bước 2: Đồng bộ dữ liệu**

```
🏦 Core Banking         🏦 Mobile Banking
     ↓                        ↓
1. Tạo hash của dữ liệu cần đồng bộ
2. ECDSA: Ký số hash với private key của Core Banking
3. Gửi data + signature qua kênh bảo mật
4. Mobile Banking verify signature
5. Cập nhật dữ liệu
```

**Mục đích**: Đảm bảo dữ liệu được đồng bộ an toàn và chính xác

---

## 🛡️ Flow 7: Backup và Recovery

### **Bước 1: Tạo backup**

```
🏦 Banking Server         💾 Backup System
     ↓                        ↓
1. Tạo backup của database
2. SHA256: Hash backup data
3. RSA: Mã hóa backup với public key của backup system
4. ECDSA: Ký số backup với private key của bank
5. Lưu encrypted backup + signature
```

**Mục đích**: Tạo backup an toàn và đảm bảo tính toàn vẹn

### **Bước 2: Khôi phục dữ liệu**

```
💾 Backup System         🏦 Banking Server
     ↓                        ↓
1. Tải encrypted backup
2. ECDSA: Verify signature của bank
3. RSA: Giải mã backup với private key của backup system
4. SHA256: Verify hash của backup
5. Khôi phục dữ liệu
```

**Mục đích**: Khôi phục dữ liệu an toàn và đảm bảo tính toàn vẹn

---

## 📈 Flow 8: Monitoring và Audit

### **Bước 1: Thu thập logs**

```
🏦 Banking Server         📊 Monitoring System
     ↓                        ↓
1. Tạo log entries cho mọi hoạt động
2. SHA256: Hash mỗi log entry
3. ECDSA: Ký số log entry với private key của server
4. Gửi signed logs qua TLS
5. Lưu logs vào audit database
```

**Mục đích**: Tạo audit trail không thể bị thay đổi

### **Bước 2: Phân tích logs**

```
📊 Monitoring System
     ↓
1. Truy vấn logs từ audit database
2. ECDSA: Verify signature của mỗi log entry
3. SHA256: Verify hash của mỗi log entry
4. Phân tích patterns và anomalies
5. Tạo alerts cho suspicious activities
```

**Mục đích**: Phát hiện các hoạt động bất thường và tấn công

---

## 🔧 Implementation Details

### **Key Management**

```java
// RSA Key Generation
KeyPair rsaKeyPair = RSAUtils.generateKeyPair(2048);

// ECDSA Key Generation
KeyPair ecdsaKeyPair = DigitalSignature.generateKeyPair();

// Diffie-Hellman Key Exchange
KeyPair dhKeyPair = DiffieHellmanUtils.generateKeyPair();
```

### **Encryption/Decryption**

```java
// RSA Encryption
String encrypted = RSAUtils.encrypt(data, publicKey);
String decrypted = RSAUtils.decrypt(encrypted, privateKey);

// ECDSA Signing (Server-side)
String signature = DigitalSignature.sign(data, privateKey); // Server ký
boolean isValid = DigitalSignature.verify(data, signature, publicKey); // Verify
```

### **Key Exchange**

```java
// Diffie-Hellman Key Agreement
byte[] sharedSecret = DiffieHellmanUtils.performKeyAgreement(
    privateKey, publicKey);
```

---

## 🎯 Kết luận

### **Tại sao cần kết hợp nhiều thuật toán:**

- **RSA**: Mạnh mẽ, phù hợp với encryption và certificate management
- **ECDSA**: Nhanh, hiệu quả, phù hợp với transaction signing
- **Diffie-Hellman**: Chuyên nghiệp, phù hợp với key exchange
- **SHA256**: Cơ bản, phù hợp với hashing và data integrity

### **Nguyên tắc bảo mật:**

1. **Defense in Depth**: Nhiều lớp bảo vệ
2. **Least Privilege**: Chỉ cấp quyền tối thiểu cần thiết
3. **Zero Trust**: Không tin tưởng bất kỳ ai
4. **Audit Everything**: Ghi lại mọi hoạt động

### **Best Practices:**

- Bảo vệ private keys trong database (encrypted)
- Implement certificate pinning
- Regular key rotation
- Monitor và audit mọi hoạt động
- Test security regularly

**Kết hợp các thuật toán mã hóa đúng cách là nền tảng của bảo mật banking!** 🚀
