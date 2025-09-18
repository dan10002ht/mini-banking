# 📚 Week 10-11: Consensus Mechanisms

## 🎯 Mục tiêu

Học về các cơ chế consensus - PoW, PoS, PBFT, Raft, HotStuff.

## 📖 Nội dung học tập

### 1. Proof of Work (PoW) - Đã implement

- **Mining**: Tìm nonce để tạo hash phù hợp
- **Difficulty**: Điều chỉnh độ khó
- **Energy Intensive**: Tốn nhiều năng lượng

### 2. Proof of Authority (PoA) - **QUAN TRỌNG CHO BANKING**

- **Authority-based**: Chọn validator dựa trên quyền hạn
- **No Mining**: Không cần mining, tiết kiệm năng lượng
- **Fast**: Xử lý nhanh, phù hợp banking
- **Centralized**: Kiểm soát tập trung

### 3. Proof of Stake (PoS)

- **Staking**: Khóa coin để tham gia consensus
- **Validator Selection**: Chọn validator dựa trên stake
- **Energy Efficient**: Tiết kiệm năng lượng

### 4. PBFT (Practical Byzantine Fault Tolerance)

- **3f+1 Nodes**: Cần ít nhất 3f+1 nodes
- **Byzantine Faults**: Chống lỗi malicious nodes
- **Finality**: Giao dịch có tính finality

### 5. Raft Consensus

- **Leader Election**: Chọn leader
- **Log Replication**: Replicate logs
- **Split Brain Prevention**: Tránh split brain

### 6. HotStuff

- **Linear View Change**: Thay đổi view tuyến tính
- **Optimistic Responsiveness**: Phản hồi nhanh
- **BFT Properties**: Byzantine fault tolerance

## 🏗️ Implementation trong Mini Banking

### 1. Current: PoW (Đã có)

```java
// Proof of Work mining
while (!blockHash.startsWith(targetPrefix)) {
    nonce++;
    blockHash = calculateBlockHash(block, nonce);
}
```

### 2. Cần implement: PoA (Ưu tiên cho Banking)

```java
@Service
public class ProofOfAuthorityService {

    public Validator selectAuthorizedValidator() {
        // Chọn validator dựa trên authority (quyền hạn)
        List<Validator> authorizedValidators = getAuthorizedValidators();
        return authorizedValidators.get(0); // Chọn validator đầu tiên
    }

    public boolean validateAuthority(Validator validator) {
        // Kiểm tra validator có quyền tạo block không
        return authorizedValidators.contains(validator);
    }

    public Block createBlockWithoutMining(List<TransactionEvent> transactions) {
        // Tạo block không cần mining (nhanh hơn PoW)
        Block block = new Block();
        block.setBlockNumber(getLastBlockNumber() + 1);
        block.setPreviousHash(getLastBlockHash());
        block.setMerkleRoot(calculateMerkleRoot(transactions));
        block.setBlockHash(calculateBlockHash(block));
        return block;
    }
}
```

### 3. Cần implement: PoS

```java
@Service
public class ProofOfStakeService {

    public Validator selectValidator(List<Validator> validators) {
        // Select validator based on stake
        return validators.stream()
            .max(Comparator.comparing(Validator::getStake))
            .orElse(null);
    }

    public boolean validateStake(Validator validator, BigDecimal stake) {
        return validator.getStake().compareTo(stake) >= 0;
    }
}
```

### 4. Cần implement: PBFT

```java
@Service
public class PBFTService {

    public boolean proposeBlock(Block block, List<Node> nodes) {
        // Send proposal to all nodes
        int votes = 0;
        for (Node node : nodes) {
            if (node.vote(block)) {
                votes++;
            }
        }
        return votes >= (2 * nodes.size() / 3) + 1;
    }
}
```

## ✅ Đã hoàn thành

- [x] Proof of Work implementation
- [x] Mining process
- [x] Difficulty adjustment
- [x] Block creation

## 🎯 Tuần 10-11: Cần làm gì?

### 1. Proof of Authority (Ưu tiên cho Banking)

- [ ] Authorized validator management
- [ ] Authority validation
- [ ] Block creation without mining
- [ ] Performance optimization

### 2. Proof of Stake

- [ ] Validator selection
- [ ] Staking mechanism
- [ ] Stake validation

### 3. PBFT Implementation

- [ ] Node communication
- [ ] Vote collection
- [ ] Consensus decision

### 4. Raft Consensus

- [ ] Leader election
- [ ] Log replication
- [ ] Split brain prevention

### 5. Consensus Testing

- [ ] Unit tests
- [ ] Integration tests
- [ ] Fault tolerance tests

## 📝 Tasks cho Tuần 10-11

1. **PoA Service (Ưu tiên cho Banking)**

   - Authorized validator management
   - Authority validation
   - Block creation without mining
   - Performance optimization

2. **PoS Service**

   - Validator management
   - Staking mechanism
   - Validator selection

3. **PBFT Service**

   - Node communication
   - Vote collection
   - Consensus decision

4. **Raft Service**

   - Leader election
   - Log replication
   - Split brain prevention

5. **Consensus Testing**
   - Unit tests
   - Integration tests
   - Fault tolerance tests
