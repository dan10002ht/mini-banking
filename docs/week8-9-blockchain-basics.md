# 📚 Week 8-9: Blockchain Basics

## 🎯 Mục tiêu

Học về cấu trúc blockchain cơ bản - Block structure, hash chain, mining.

## 📖 Nội dung học tập

### 1. Block Structure

- **Block Header**: Previous hash, Merkle root, timestamp, nonce
- **Block Body**: Transactions list
- **Block Hash**: SHA256 hash của toàn bộ block

### 2. Hash Chain

- **Chain of Blocks**: Mỗi block chứa hash của block trước
- **Immutability**: Không thể thay đổi block cũ
- **Integrity**: Verify toàn bộ chain

### 3. Mining (Proof of Work)

- **Difficulty**: Số lượng leading zeros cần thiết
- **Nonce**: Số để tìm hash phù hợp
- **Mining Process**: Thử nonce cho đến khi tìm được hash

## 🏗️ Implementation trong Mini Banking

### 1. Block Structure (Đã có)

```java
@Entity
public class Block {
    private UUID blockId;
    private Long blockNumber;
    private String previousHash;
    private String merkleRoot;
    private String blockHash;
    private Long nonce;
    private Integer difficulty;
    private List<String> transactionHashes;
    private LocalDateTime timestamp;
}
```

### 2. Hash Chain (Đã có)

```java
// Mỗi block chứa hash của block trước
String previousHash = (previousBlock != null) ? previousBlock.getBlockHash() : "0";
```

### 3. Mining Process (Đã có)

```java
// Proof of Work mining
while (!blockHash.startsWith(targetPrefix)) {
    nonce++;
    blockHash = calculateBlockHash(block, nonce);
}
```

## ✅ Đã hoàn thành

- [x] Block Entity với đầy đủ fields
- [x] Hash chain implementation
- [x] Proof of Work mining
- [x] Merkle Tree integration
- [x] Redis Streams cho transaction processing
- [x] Block verification
- [x] Transaction linking với blocks

## 🎯 Tuần 8-9: Cần làm gì?

### 1. Enhance Block Structure

- [ ] Thêm block size calculation
- [ ] Thêm block validation rules
- [ ] Thêm block statistics

### 2. Improve Mining

- [ ] Dynamic difficulty adjustment
- [ ] Mining statistics
- [ ] Mining performance optimization

### 3. Block Verification

- [ ] Complete chain verification
- [ ] Block integrity checks
- [ ] Transaction verification

### 4. Testing

- [ ] Unit tests cho blockchain
- [ ] Integration tests
- [ ] Performance tests

## 📝 Tasks cho Tuần 8-9

1. **Block Statistics Service**

   - Track mining performance
   - Block creation metrics
   - Difficulty adjustment

2. **Chain Verification Service**

   - Verify entire blockchain
   - Detect forks
   - Validate all blocks

3. **Mining Optimization**

   - Parallel mining
   - Mining pools simulation
   - Performance monitoring

4. **Block Explorer API**
   - Get block details
   - Search transactions
   - Chain statistics
