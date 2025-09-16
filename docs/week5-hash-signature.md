# 🔐 Week 5: Hash & Signature

## 🎯 Mục tiêu

Nắm vững nền tảng cryptography để hiểu blockchain:

- SHA256 hash function
- Merkle Tree structure
- Digital signatures
- Hash chains

## 📖 Nội dung học tập

### 1. Hash Functions

#### SHA256 (Secure Hash Algorithm 256-bit)

**Định nghĩa**: Hàm băm một chiều tạo ra output 256-bit từ input bất kỳ.

**Đặc điểm**:

- **Deterministic**: Cùng input → cùng output
- **One-way**: Không thể reverse từ hash về input
- **Avalanche effect**: Thay đổi nhỏ → hash hoàn toàn khác
- **Fixed length**: Luôn 256-bit (64 hex characters)

#### SHA256 Properties

```
Input: "Hello World"
SHA256: a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146

Input: "Hello World!"
SHA256: 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069
```

**Avalanche Effect Example**:

```
"Hello"     → 2ef7bde608ce5404e97d5f042f95f89f1c232871
"Hello!"    → 334d016f755cd6dc58c53a86e183882f8ec14f52fb05345887c8a5edd42c87b7
```

### 2. Merkle Tree

#### Cấu trúc Merkle Tree

**Định nghĩa**: Cây nhị phân chứa hash của tất cả transactions.

```
        Root Hash
       /         \
   Hash(AB)    Hash(CD)
   /     \     /     \
Hash(A) Hash(B) Hash(C) Hash(D)
   |       |       |       |
  TX1     TX2     TX3     TX4
```

#### Merkle Tree Properties

- **Leaf nodes**: Hash của individual transactions
- **Internal nodes**: Hash của 2 child nodes
- **Root hash**: Đại diện cho toàn bộ tree
- **Proof**: Có thể verify transaction mà không cần toàn bộ tree

### 3. Digital Signatures

#### ECDSA (Elliptic Curve Digital Signature Algorithm)

**Components**:

- **Private Key**: 256-bit random number
- **Public Key**: Derived from private key
- **Signature**: (r, s) pair created with private key

#### Signature Process

```
1. Hash message: H = SHA256(message)
2. Generate random k
3. Calculate r = (k * G).x mod n
4. Calculate s = k^(-1) * (H + r * private_key) mod n
5. Signature = (r, s)
```

#### Verification Process

```
1. Hash message: H = SHA256(message)
2. Calculate u1 = s^(-1) * H mod n
3. Calculate u2 = s^(-1) * r mod n
4. Calculate P = u1 * G + u2 * public_key
5. Verify: P.x mod n == r
```

## 💻 Implementation

### SHA256 Implementation

```java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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

### Merkle Tree Implementation

```java
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {

    private List<String> transactions;
    private String rootHash;

    public MerkleTree(List<String> transactions) {
        this.transactions = transactions;
        this.rootHash = buildTree();
    }

    private String buildTree() {
        List<String> currentLevel = new ArrayList<>();

        // Hash all transactions
        for (String tx : transactions) {
            currentLevel.add(sha256(tx));
        }

        // Build tree bottom-up
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size())
                    ? currentLevel.get(i + 1)
                    : left; // Duplicate last element if odd

                nextLevel.add(sha256(left + right));
            }

            currentLevel = nextLevel;
        }

        return currentLevel.get(0);
    }

    public String getRootHash() {
        return rootHash;
    }

    public boolean verifyTransaction(String transaction, List<String> proof) {
        String hash = sha256(transaction);

        for (String sibling : proof) {
            hash = sha256(hash + sibling);
        }

        return hash.equals(rootHash);
    }
}
```

### Digital Signature Implementation

```java
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.math.BigInteger;

public class DigitalSignature {

    private KeyPair keyPair;

    public DigitalSignature() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            keyGen.initialize(ecSpec);
            this.keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sign(String message) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(message.getBytes());
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(String message, byte[] signature) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(keyPair.getPublic());
            sig.update(message.getBytes());
            return sig.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}
```

## 🔧 Banking Use Cases

### 1. Transaction Integrity

```java
public class Transaction {
    private String from;
    private String to;
    private BigDecimal amount;
    private long timestamp;
    private String hash;
    private byte[] signature;

    public Transaction(String from, String to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    private String calculateHash() {
        String data = from + to + amount.toString() + timestamp;
        return HashUtils.sha256(data);
    }

    public void sign(PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(this.hash.getBytes());
            this.signature = signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifySignature(PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(this.hash.getBytes());
            return sig.verify(this.signature);
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 2. Block Structure

```java
public class Block {
    private String previousHash;
    private List<Transaction> transactions;
    private String merkleRoot;
    private long timestamp;
    private int nonce;
    private String hash;

    public Block(String previousHash, List<Transaction> transactions) {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = System.currentTimeMillis();
        this.merkleRoot = calculateMerkleRoot();
        this.hash = calculateHash();
    }

    private String calculateMerkleRoot() {
        List<String> txHashes = transactions.stream()
            .map(Transaction::getHash)
            .collect(Collectors.toList());

        MerkleTree tree = new MerkleTree(txHashes);
        return tree.getRootHash();
    }

    private String calculateHash() {
        String data = previousHash + merkleRoot + timestamp + nonce;
        return HashUtils.sha256(data);
    }

    public void mine(int difficulty) {
        String target = "0".repeat(difficulty);

        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }
}
```

## 🧪 Testing

### Hash Function Tests

```java
@Test
public void testSHA256() {
    String input = "Hello World";
    String expected = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146";
    String actual = HashUtils.sha256(input);
    assertEquals(expected, actual);
}

@Test
public void testAvalancheEffect() {
    String input1 = "Hello";
    String input2 = "Hello!";

    String hash1 = HashUtils.sha256(input1);
    String hash2 = HashUtils.sha256(input2);

    // Hashes should be completely different
    assertNotEquals(hash1, hash2);
}
```

### Merkle Tree Tests

```java
@Test
public void testMerkleTree() {
    List<String> transactions = Arrays.asList(
        "TX1: Alice -> Bob 100",
        "TX2: Bob -> Charlie 50",
        "TX3: Charlie -> Alice 25",
        "TX4: Alice -> David 75"
    );

    MerkleTree tree = new MerkleTree(transactions);
    String rootHash = tree.getRootHash();

    assertNotNull(rootHash);
    assertEquals(64, rootHash.length()); // 256-bit = 64 hex chars
}

@Test
public void testMerkleProof() {
    List<String> transactions = Arrays.asList("TX1", "TX2", "TX3", "TX4");
    MerkleTree tree = new MerkleTree(transactions);

    // Verify TX1 is in the tree
    List<String> proof = Arrays.asList(
        "hash(TX2)",
        "hash(hash(TX3) + hash(TX4))"
    );

    assertTrue(tree.verifyTransaction("TX1", proof));
}
```

## 🎯 Kết quả đạt được

### Lý thuyết

- ✅ Hiểu SHA256 hash function
- ✅ Nắm vững Merkle Tree structure
- ✅ Digital signatures với ECDSA
- ✅ Hash chains trong blockchain

### Thực hành

- ✅ Implement SHA256
- ✅ Build Merkle Tree
- ✅ Digital signature generation/verification
- ✅ Transaction integrity checking

### Kỹ năng

- ✅ Cryptography fundamentals
- ✅ Hash function implementation
- ✅ Digital signature systems
- ✅ Blockchain data structures

## 🚀 Bước tiếp theo

Sẵn sàng cho **Week 6: Public Key Cryptography** - Học RSA, ECDSA, Diffie-Hellman
