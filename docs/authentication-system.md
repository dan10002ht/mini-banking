# Authentication System Documentation

## Overview

Hệ thống authentication cho Mini Banking System với device management, 2FA, và JWT tokens.

## Features

### 1. Device Management

- **Device Registration**: Tự động đăng ký device mới khi login
- **Device Trust**: Quản lý device tin cậy/không tin cậy
- **Device Fingerprinting**: Nhận diện device qua fingerprint
- **Device Locking**: Khóa device sau nhiều lần login thất bại

### 2. Multi-Factor Authentication (2FA)

- **OTP Generation**: Tạo OTP 6 số cho 2FA
- **OTP Verification**: Xác thực OTP với giới hạn số lần thử
- **Temporary Sessions**: Session tạm thời cho 2FA

### 3. JWT Token Management

- **Access Tokens**: Token chính cho API access (24h)
- **Refresh Tokens**: Token để refresh access token (7 ngày)
- **Temporary Tokens**: Token tạm thời cho 2FA (10 phút)

### 4. Session Management

- **Session Tracking**: Theo dõi session theo device
- **Session Invalidation**: Hủy session khi logout
- **Session Cleanup**: Tự động dọn dẹp session hết hạn

## API Endpoints

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/verify-otp` - Verify OTP for 2FA
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - User logout

### Device Management

- `GET /api/auth/devices` - Get user devices
- `POST /api/auth/devices/trust` - Trust device
- `POST /api/auth/devices/untrust` - Untrust device
- `POST /api/auth/devices/deactivate` - Deactivate device
- `GET /api/auth/devices/statistics` - Get device statistics

## Authentication Flow

### 1. Trusted Device Login

```
1. User login với PIN
2. System check device trust level
3. Generate JWT tokens
4. Create session
5. Return success response
```

### 2. Untrusted Device Login

```
1. User login với password
2. System register new device
3. Generate OTP
4. Create temporary session
5. Return 2FA required response
6. User verify OTP
7. Trust device
8. Generate JWT tokens
9. Create session
10. Return success response
```

## Database Schema

### Devices Table

```sql
CREATE TABLE devices (
    device_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_fingerprint VARCHAR(255) UNIQUE NOT NULL,
    device_name VARCHAR(100),
    device_type VARCHAR(50),
    os_name VARCHAR(50),
    os_version VARCHAR(50),
    browser_name VARCHAR(50),
    browser_version VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    is_trusted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    last_activity TIMESTAMP,
    login_count INTEGER DEFAULT 0,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Sessions Table

```sql
CREATE TABLE sessions (
    session_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_fingerprint VARCHAR(255),
    token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    is_temporary BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    last_activity TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration

### JWT Configuration

```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400 # 24 hours
  refresh-expiration: 604800 # 7 days
```

### OTP Configuration

```yaml
otp:
  length: 6
  expiration: 300 # 5 minutes
  max-attempts: 3
```

## Security Features

### 1. Device Security

- Device fingerprinting để nhận diện device
- Device locking sau 5 lần login thất bại
- Device trust management
- Device deactivation

### 2. Token Security

- JWT tokens với expiration
- Refresh token mechanism
- Token hash storage
- Session invalidation

### 3. OTP Security

- OTP expiration (5 phút)
- Max attempts (3 lần)
- OTP cleanup
- Temporary session cho 2FA

## Usage Examples

### 1. Login với Trusted Device

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user123",
    "pin": "1234",
    "deviceFingerprint": "device123",
    "deviceName": "iPhone 15"
  }'
```

### 2. Login với Untrusted Device

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user123",
    "password": "password123",
    "deviceFingerprint": "newdevice456",
    "deviceName": "Chrome Browser"
  }'
```

### 3. Verify OTP

```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "otpId": "otp123",
    "otp": "123456",
    "deviceFingerprint": "newdevice456"
  }'
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "refresh_token_here"
  }'
```

## Monitoring và Maintenance

### 1. Device Cleanup

- Tự động deactivate device không hoạt động
- Cleanup expired sessions
- Device statistics tracking

### 2. Security Monitoring

- Failed login attempts tracking
- Device lock monitoring
- Session activity tracking

### 3. Performance

- JWT token validation
- Session lookup optimization
- Device fingerprint caching

## Best Practices

### 1. Device Management

- Luôn verify device fingerprint
- Implement device trust policies
- Monitor device activity

### 2. Token Management

- Use short-lived access tokens
- Implement proper refresh token rotation
- Store token hashes, not plain tokens

### 3. Security

- Implement rate limiting
- Monitor failed login attempts
- Use secure OTP generation

## Troubleshooting

### Common Issues

1. **Device not trusted**: User cần verify OTP
2. **Token expired**: Sử dụng refresh token
3. **Device locked**: Đợi unlock hoặc contact admin
4. **OTP expired**: Request OTP mới

### Debug Logs

- Enable DEBUG logging cho `com.minibanking.security`
- Monitor authentication flow
- Check device trust levels
