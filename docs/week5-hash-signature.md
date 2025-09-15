# 📚 Week 5: Hash & Signature

## 🎯 Mục tiêu

Học về Hash functions và Digital signatures - nền tảng cho blockchain.

## 📖 Nội dung học tập

### 1. Hash Functions

- **SHA256**: Secure Hash Algorithm 256-bit
- **Merkle Tree**: Cấu trúc dữ liệu để verify integrity
- **Hash Properties**: Deterministic, one-way, collision-resistant

### 2. Digital Signatures

- **RSA**: Rivest-Shamir-Adleman algorithm
- **ECDSA**: Elliptic Curve Digital Signature Algorithm
- **Signature Process**: Sign, Verify, Key Management

### 3. Merkle Tree

- **Cấu trúc**: Binary tree với hash values
- **Ứng dụng**: Blockchain, Git, File systems
- **Benefits**: Efficient verification, tamper detection

## 🔐 Hash Functions

### SHA256

```java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashExample {
    public static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
```

### Hash Properties

- **Deterministic**: Cùng input → cùng output
- **One-way**: Không thể reverse từ hash về input
- **Collision-resistant**: Khó tìm 2 inputs có cùng hash
- **Fixed length**: SHA256 luôn tạo ra 256-bit (64 hex characters)

## 🌳 Merkle Tree

### Cấu trúc Merkle Tree

```
        Root Hash
       /         \
   Hash AB      Hash CD
   /     \      /     \
Hash A  Hash B Hash C Hash D
  |       |      |       |
Data A  Data B Data C Data D
```

### Merkle Tree Implementation

```java
public class MerkleTree {
    private List<String> transactions;
    private String rootHash;

    public MerkleTree(List<String> transactions) {
        this.transactions = transactions;
        this.rootHash = buildMerkleTree();
    }

    private String buildMerkleTree() {
        List<String> currentLevel = new ArrayList<>(transactions);

        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left;
                String combined = left + right;
                nextLevel.add(sha256(combined));
            }

            currentLevel = nextLevel;
        }

        return currentLevel.get(0);
    }

    public boolean verifyTransaction(String transaction) {
        // Verify transaction is in Merkle tree
        return transactions.contains(transaction);
    }
}
```

## ✍️ Digital Signatures

### RSA Signature

```java
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSASignature {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public byte[] sign(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        return signature.sign();
    }

    public boolean verify(String message, byte[] signature) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(message.getBytes());
        return sig.verify(signature);
    }
}
```

### ECDSA Signature

```java
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class ECDSASignature {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public byte[] sign(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        return signature.sign();
    }

    public boolean verify(String message, byte[] signature) throws Exception {
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(publicKey);
        sig.update(message.getBytes());
        return sig.verify(signature);
    }
}
```

## 🏦 Banking Applications

### Transaction Signing

```java
public class BankingTransaction {
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String signature;

    public void sign(PrivateKey privateKey) throws Exception {
        String message = transactionId + fromAccount + toAccount + amount.toString();
        ECDSASignature signer = new ECDSASignature();
        signer.setPrivateKey(privateKey);
        this.signature = Base64.getEncoder().encodeToString(signer.sign(message));
    }

    public boolean verify(PublicKey publicKey) throws Exception {
        String message = transactionId + fromAccount + toAccount + amount.toString();
        ECDSASignature verifier = new ECDSASignature();
        verifier.setPublicKey(publicKey);
        return verifier.verify(message, Base64.getDecoder().decode(signature));
    }
}
```

### Merkle Tree for Transaction Batch

```java
public class TransactionBatch {
    private List<BankingTransaction> transactions;
    private MerkleTree merkleTree;

    public TransactionBatch(List<BankingTransaction> transactions) {
        this.transactions = transactions;
        List<String> transactionHashes = transactions.stream()
            .map(t -> sha256(t.toString()))
            .collect(Collectors.toList());
        this.merkleTree = new MerkleTree(transactionHashes);
    }

    public String getRootHash() {
        return merkleTree.getRootHash();
    }

    public boolean verifyTransaction(BankingTransaction transaction) {
        return merkleTree.verifyTransaction(sha256(transaction.toString()));
    }
}
```

## 🔧 Implementation Tasks

### Week 5: Hash & Signature

- [ ] Implement SHA256 hash function
- [ ] Build Merkle Tree structure
- [ ] Create RSA signature system
- [ ] Create ECDSA signature system
- [ ] Apply to banking transactions

## 🎯 Kết quả đạt được

### Lý thuyết

- ✅ Hiểu SHA256 hash function
- ✅ Nắm vững Merkle Tree structure
- ✅ Biết RSA và ECDSA signatures
- ✅ Ứng dụng trong banking

### Thực hành

- ✅ Hash function implementation
- ✅ Merkle Tree implementation
- ✅ Digital signature system
- ✅ Transaction signing và verification

### Kỹ năng

- ✅ Cryptographic operations
- ✅ Data integrity verification
- ✅ Digital signature management
- ✅ Security best practices

## 🚀 Bước tiếp theo

Sẵn sàng cho **Week 6: Public Key Cryptography** - Học về RSA, ECDSA, Diffie-Hellman

