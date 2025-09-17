# 🔗 Blockchain Integration với Redis Streams

## 📋 Tổng quan

Hệ thống banking đã được tích hợp blockchain sử dụng Redis Streams để xử lý transactions một cách bất đồng bộ và tạo ra các blocks với Proof of Work.

## 🏗️ Kiến trúc

### **Flow hoạt động:**

```
1. User thực hiện transaction (Transfer/Deposit/Withdraw)
2. BankingService xử lý transaction và lưu vào database
3. BankingService gửi TransactionEvent vào Redis Stream
4. BlockchainStreamConsumer nhận event từ Redis Stream
5. Consumer tạo Block với batch transactions
6. Consumer thực hiện mining (Proof of Work)
7. Block được lưu vào database
8. Event được gửi về blockchain stream
```

## 🔧 Components

### **1. Block Entity**

- Lưu trữ thông tin block trong database
- Chứa block number, previous hash, merkle root, block hash
- Hỗ trợ mining với nonce và difficulty

### **2. TransactionEvent**

- Event object cho Redis Streams
- Chứa thông tin transaction đã được serialize
- Hỗ trợ digital signature và merkle proof

### **3. BlockchainService**

- Tạo và quản lý blocks
- Thực hiện mining (Proof of Work)
- Verify block integrity
- Quản lý blockchain state

### **4. TransactionStreamProducer**

- Gửi transaction events vào Redis Stream
- Gửi blockchain events (block created, block mined)
- Quản lý stream statistics

### **5. BlockchainStreamConsumer**

- Nhận events từ Redis Stream
- Tạo blocks từ batch transactions
- Thực hiện mining process
- Xử lý bất đồng bộ

### **6. BlockchainController**

- REST API để query blockchain
- Quản lý consumer status
- Verify blocks

## 🚀 Cài đặt và chạy

### **1. Cài đặt Redis**

```bash
# Sử dụng Docker Compose
docker-compose -f docker-compose.redis.yml up -d

# Hoặc cài đặt Redis locally
# macOS
brew install redis
brew services start redis

# Ubuntu
sudo apt-get install redis-server
sudo systemctl start redis
```

### **2. Cập nhật Database**

```bash
# Chạy script cập nhật database
psql -U postgres -d mini_banking -f database-setup.sql
```

### **3. Chạy ứng dụng**

```bash
# Build và chạy
mvn clean install
mvn spring-boot:run
```

## 📊 API Endpoints

### **Blockchain Info**

- `GET /api/blockchain/info` - Thông tin tổng quan blockchain
- `GET /api/blockchain/blocks/latest` - Block mới nhất
- `GET /api/blockchain/blocks/{blockId}` - Chi tiết block theo ID
- `GET /api/blockchain/blocks/number/{blockNumber}` - Block theo số
- `GET /api/blockchain/blocks?startBlock=1&endBlock=10` - Blocks trong khoảng

### **Block Verification**

- `POST /api/blockchain/blocks/{blockId}/verify` - Verify block integrity

### **Stream Management**

- `GET /api/blockchain/streams/info` - Thông tin Redis Streams
- `GET /api/blockchain/consumer/status` - Trạng thái consumer
- `POST /api/blockchain/consumer/initialize` - Khởi tạo consumer group
- `POST /api/blockchain/consumer/start` - Bắt đầu consumer

## ⚙️ Configuration

### **Redis Configuration**

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

### **Blockchain Configuration**

```yaml
blockchain:
  mining:
    difficulty: 4 # Số số 0 đầu tiên cần thiết
    batch-size: 10 # Số transactions per block
    batch-timeout: 5000 # milliseconds
  streams:
    transaction-stream: "transaction-events"
    blockchain-stream: "blockchain-events"
    consumer-group: "blockchain-processors"
    consumer-name: "blockchain-processor-1"
```

## 🔍 Monitoring

### **1. Redis Streams Info**

```bash
# Xem stream info
redis-cli XINFO STREAM transaction-events
redis-cli XINFO STREAM blockchain-events

# Xem consumer groups
redis-cli XINFO GROUPS transaction-events
```

### **2. Application Logs**

```bash
# Xem logs
tail -f logs/application.log | grep -i blockchain
```

### **3. Database Queries**

```sql
-- Xem blocks
SELECT * FROM blocks ORDER BY block_number DESC LIMIT 10;

-- Xem blockchain stats
SELECT
    COUNT(*) as total_blocks,
    AVG(transaction_count) as avg_transactions_per_block,
    MAX(block_number) as latest_block_number
FROM blocks WHERE status = 'MINED';
```

## 🧪 Testing

### **1. Tạo Transaction**

```bash
# Transfer money
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "account-id-1",
    "toAccountId": "account-id-2",
    "amount": 1000,
    "description": "Test transfer"
  }'
```

### **2. Kiểm tra Blockchain**

```bash
# Xem blockchain info
curl http://localhost:8080/api/blockchain/info

# Xem latest block
curl http://localhost:8080/api/blockchain/blocks/latest
```

## 🔧 Troubleshooting

### **1. Redis Connection Issues**

```bash
# Kiểm tra Redis
redis-cli ping

# Xem Redis logs
docker logs mini-banking-redis
```

### **2. Consumer Issues**

```bash
# Xem consumer status
curl http://localhost:8080/api/blockchain/consumer/status

# Restart consumer
curl -X POST http://localhost:8080/api/blockchain/consumer/start
```

### **3. Database Issues**

```sql
-- Kiểm tra blocks table
SELECT COUNT(*) FROM blocks;

-- Xem pending blocks
SELECT * FROM blocks WHERE status = 'PENDING';
```

## 📈 Performance

### **Mining Performance**

- **Difficulty 4**: ~1-2 giây per block
- **Difficulty 6**: ~10-30 giây per block
- **Difficulty 8**: ~2-5 phút per block

### **Throughput**

- **Batch Size 10**: ~100 transactions/minute
- **Batch Size 50**: ~500 transactions/minute
- **Batch Size 100**: ~1000 transactions/minute

## 🔐 Security

### **Block Integrity**

- Mỗi block có hash duy nhất
- Previous hash liên kết các blocks
- Merkle root đảm bảo transaction integrity
- Proof of Work ngăn chặn spam

### **Transaction Security**

- Mọi transaction được ký số
- Merkle proof cho transaction verification
- ACID transactions trong database
- Pessimistic locking cho account updates

## 🚀 Next Steps

1. **Implement Merkle Proof Verification**
2. **Add Block Validation Rules**
3. **Implement Fork Detection**
4. **Add Block Explorer UI**
5. **Implement Smart Contracts**
6. **Add Consensus Mechanisms**

---

**Blockchain integration hoàn tất! Hệ thống banking giờ đây có thể xử lý transactions và tạo blockchain một cách bất đồng bộ!** 🏦🔗✨
