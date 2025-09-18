# 📚 Proof of Authority (PoA) cho Banking

## 🎯 Tại sao PoA quan trọng cho Banking?

### **1. So sánh với PoW**
| Aspect | PoW | PoA |
|--------|-----|-----|
| **Energy** | Cao (150 TWh/năm) | Thấp |
| **Speed** | Chậm (10 phút/block) | Nhanh (< 1 giây) |
| **Cost** | Cao (mining) | Thấp |
| **Control** | Phi tập trung | Tập trung |
| **Banking** | ❌ Không phù hợp | ✅ Phù hợp |

### **2. Ưu điểm cho Banking**
- ✅ **Nhanh**: Xử lý transaction < 1 giây
- ✅ **Rẻ**: Không cần mining, tiết kiệm chi phí
- ✅ **Kiểm soát**: Ngân hàng kiểm soát validators
- ✅ **Bảo mật**: Chỉ authorized validators mới tạo block
- ✅ **Compliance**: Tuân thủ quy định ngân hàng

## 🏗️ Implementation trong Mini Banking

### **1. Validator Entity**
```java
@Entity
@Table(name = "validators")
public class Validator {
    @Id
    private UUID validatorId;
    
    @Column(unique = true)
    private String validatorName;
    
    @Column
    private String publicKey;
    
    @Column
    private Boolean isAuthorized = false;
    
    @Column
    private Integer priority = 0; // Ưu tiên tạo block
    
    @Column
    private LocalDateTime lastBlockTime;
    
    @Column
    private Integer blocksCreated = 0;
    
    @Column
    private Boolean isActive = true;
}
```

### **2. PoA Service**
```java
@Service
public class ProofOfAuthorityService {
    
    @Autowired
    private ValidatorRepository validatorRepository;
    
    @Autowired
    private BlockRepository blockRepository;
    
    public Validator selectAuthorizedValidator() {
        // Lấy danh sách validators được ủy quyền
        List<Validator> authorizedValidators = validatorRepository
            .findByIsAuthorizedTrueAndIsActiveTrueOrderByPriorityDesc();
        
        if (authorizedValidators.isEmpty()) {
            throw new BlockchainException("No authorized validators available");
        }
        
        // Chọn validator có priority cao nhất
        return authorizedValidators.get(0);
    }
    
    public Block createBlockWithoutMining(List<TransactionEvent> transactions) {
        // Chọn validator
        Validator validator = selectAuthorizedValidator();
        
        // Tạo block mới
        Block block = new Block();
        block.setBlockNumber(getLastBlockNumber() + 1);
        block.setPreviousHash(getLastBlockHash());
        block.setMerkleRoot(calculateMerkleRoot(transactions));
        block.setTimestamp(LocalDateTime.now());
        block.setValidatorId(validator.getValidatorId());
        
        // Tính hash (không cần mining)
        String blockHash = calculateBlockHash(block);
        block.setBlockHash(blockHash);
        
        // Lưu block
        block = blockRepository.save(block);
        
        // Cập nhật thống kê validator
        updateValidatorStats(validator);
        
        return block;
    }
    
    private void updateValidatorStats(Validator validator) {
        validator.setBlocksCreated(validator.getBlocksCreated() + 1);
        validator.setLastBlockTime(LocalDateTime.now());
        validatorRepository.save(validator);
    }
}
```

### **3. Validator Management**
```java
@Service
public class ValidatorManagementService {
    
    public Validator addAuthorizedValidator(String validatorName, String publicKey, Integer priority) {
        Validator validator = new Validator();
        validator.setValidatorName(validatorName);
        validator.setPublicKey(publicKey);
        validator.setIsAuthorized(true);
        validator.setPriority(priority);
        validator.setIsActive(true);
        validator.setBlocksCreated(0);
        
        return validatorRepository.save(validator);
    }
    
    public void revokeValidatorAuthority(UUID validatorId) {
        Validator validator = validatorRepository.findById(validatorId)
            .orElseThrow(() -> new ValidatorNotFoundException("Validator not found"));
        
        validator.setIsAuthorized(false);
        validator.setIsActive(false);
        validatorRepository.save(validator);
    }
    
    public List<Validator> getAuthorizedValidators() {
        return validatorRepository.findByIsAuthorizedTrueAndIsActiveTrueOrderByPriorityDesc();
    }
}
```

### **4. PoA Controller**
```java
@RestController
@RequestMapping("/api/consensus/poa")
public class ProofOfAuthorityController {
    
    @Autowired
    private ProofOfAuthorityService poaService;
    
    @Autowired
    private ValidatorManagementService validatorService;
    
    @PostMapping("/create-block")
    public ResponseEntity<Block> createBlock(@RequestBody List<TransactionEvent> transactions) {
        try {
            Block block = poaService.createBlockWithoutMining(transactions);
            return ResponseEntity.ok(block);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/validators")
    public ResponseEntity<Validator> addValidator(@RequestBody AddValidatorRequest request) {
        Validator validator = validatorService.addAuthorizedValidator(
            request.getValidatorName(),
            request.getPublicKey(),
            request.getPriority()
        );
        return ResponseEntity.ok(validator);
    }
    
    @GetMapping("/validators")
    public ResponseEntity<List<Validator>> getValidators() {
        List<Validator> validators = validatorService.getAuthorizedValidators();
        return ResponseEntity.ok(validators);
    }
    
    @DeleteMapping("/validators/{validatorId}")
    public ResponseEntity<Void> revokeValidator(@PathVariable UUID validatorId) {
        validatorService.revokeValidatorAuthority(validatorId);
        return ResponseEntity.ok().build();
    }
}
```

## 🔄 Migration từ PoW sang PoA

### **1. Configuration**
```yaml
# application.yml
blockchain:
  consensus:
    type: poa # poa, pow, pos, pbft
  poa:
    enabled: true
    authorized-validators:
      - name: "Bank-Validator-1"
        public-key: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
        priority: 100
      - name: "Bank-Validator-2"
        public-key: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEB..."
        priority: 90
```

### **2. Consensus Factory**
```java
@Service
public class ConsensusFactory {
    
    @Value("${blockchain.consensus.type}")
    private String consensusType;
    
    public ConsensusService getConsensusService() {
        switch (consensusType.toLowerCase()) {
            case "poa":
                return new ProofOfAuthorityService();
            case "pow":
                return new ProofOfWorkService();
            case "pos":
                return new ProofOfStakeService();
            case "pbft":
                return new PBFTService();
            default:
                throw new IllegalArgumentException("Unsupported consensus type: " + consensusType);
        }
    }
}
```

## 📊 Performance Comparison

### **1. Block Creation Time**
```
PoW:  ~10 phút (Bitcoin)
PoA:  < 1 giây
PoS:  ~1-2 phút
PBFT: ~1-5 giây
```

### **2. Energy Consumption**
```
PoW:  ~150 TWh/năm
PoA:  ~0.001 TWh/năm
PoS:  ~0.01 TWh/năm
PBFT: ~0.005 TWh/năm
```

### **3. Transaction Throughput**
```
PoW:  ~7 TPS
PoA:  ~1000+ TPS
PoS:  ~100+ TPS
PBFT: ~1000+ TPS
```

## 🎯 Kết luận

**PoA là lựa chọn tốt nhất cho banking vì:**
- ✅ Nhanh, rẻ, tiết kiệm năng lượng
- ✅ Kiểm soát tập trung phù hợp với banking
- ✅ Compliance với quy định ngân hàng
- ✅ Dễ implement và maintain

**Nên chuyển từ PoW sang PoA trong Mini Banking!**
