# 📚 Week 6: Public Key Cryptography

## 🎯 Mục tiêu

Học về Public Key Cryptography - RSA, ECDSA, Diffie-Hellman.

## 📖 Nội dung học tập

### 1. Public Key Cryptography

- **RSA**: Rivest-Shamir-Adleman algorithm
- **ECDSA**: Elliptic Curve Digital Signature Algorithm
- **Diffie-Hellman**: Key exchange protocol

### 2. Key Management

- **Key Generation**: Tạo private/public key pairs
- **Key Storage**: Bảo mật private keys
- **Key Exchange**: Secure key distribution

### 3. Digital Certificates

- **X.509 Certificates**: Standard format
- **Certificate Authority (CA)**: Trust management
- **PKI**: Public Key Infrastructure

## 🔐 RSA Algorithm

### RSA Key Generation

```java
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;

public class RSAKeyGenerator {
    private KeyPair keyPair;

    public void generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4);
        keyGen.initialize(spec);
        this.keyPair = keyGen.generateKeyPair();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public String getPrivateKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }
}
```

### RSA Encryption/Decryption

```java
import javax.crypto.Cipher;

public class RSAEncryption {
    private Cipher cipher;

    public RSAEncryption() throws Exception {
        this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }

    public byte[] encrypt(String message, PublicKey publicKey) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    public String decrypt(byte[] encryptedMessage, PrivateKey privateKey) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }
}
```

## 🔐 ECDSA Algorithm

### ECDSA Key Generation

```java
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class ECDSAKeyGenerator {
    private KeyPair keyPair;

    public void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec);
        this.keyPair = keyGen.generateKeyPair();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public String getPrivateKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }
}
```

### ECDSA Signature

```java
public class ECDSASignature {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
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

## 🤝 Diffie-Hellman Key Exchange

### Diffie-Hellman Implementation

```java
import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class DiffieHellman {
    private KeyPair keyPair;
    private KeyAgreement keyAgreement;

    public void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec);
        this.keyPair = keyGen.generateKeyPair();

        this.keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(keyPair.getPrivate());
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public byte[] generateSharedSecret(PublicKey otherPublicKey) throws Exception {
        keyAgreement.doPhase(otherPublicKey, true);
        return keyAgreement.generateSecret();
    }

    public String getSharedSecretAsString(PublicKey otherPublicKey) throws Exception {
        byte[] sharedSecret = generateSharedSecret(otherPublicKey);
        return Base64.getEncoder().encodeToString(sharedSecret);
    }
}
```

## 🏦 Banking Applications

### Secure Communication

```java
public class SecureBankingCommunication {
    private RSAEncryption rsaEncryption;
    private ECDSASignature signature;
    private DiffieHellman keyExchange;

    public SecureBankingCommunication() throws Exception {
        this.rsaEncryption = new RSAEncryption();
        this.signature = new ECDSASignature();
        this.keyExchange = new DiffieHellman();
    }

    public EncryptedMessage encryptAndSign(String message, PublicKey recipientPublicKey, PrivateKey senderPrivateKey) throws Exception {
        // 1. Encrypt message with recipient's public key
        byte[] encryptedMessage = rsaEncryption.encrypt(message, recipientPublicKey);

        // 2. Sign encrypted message with sender's private key
        byte[] signature = this.signature.sign(Base64.getEncoder().encodeToString(encryptedMessage));

        return new EncryptedMessage(encryptedMessage, signature);
    }

    public String decryptAndVerify(EncryptedMessage encryptedMessage, PrivateKey recipientPrivateKey, PublicKey senderPublicKey) throws Exception {
        // 1. Verify signature
        String encryptedMessageString = Base64.getEncoder().encodeToString(encryptedMessage.getEncryptedData());
        boolean isValid = signature.verify(encryptedMessageString, encryptedMessage.getSignature());

        if (!isValid) {
            throw new SecurityException("Invalid signature");
        }

        // 2. Decrypt message
        return rsaEncryption.decrypt(encryptedMessage.getEncryptedData(), recipientPrivateKey);
    }
}
```

### Key Management System

```java
public class BankingKeyManagement {
    private Map<String, KeyPair> customerKeys;
    private Map<String, PublicKey> bankKeys;

    public BankingKeyManagement() {
        this.customerKeys = new HashMap<>();
        this.bankKeys = new HashMap<>();
    }

    public void generateCustomerKeys(String customerId) throws Exception {
        ECDSAKeyGenerator keyGen = new ECDSAKeyGenerator();
        keyGen.generateKeyPair();
        customerKeys.put(customerId, new KeyPair(keyGen.getPublicKey(), keyGen.getPrivateKey()));
    }

    public void addBankPublicKey(String bankId, PublicKey publicKey) {
        bankKeys.put(bankId, publicKey);
    }

    public KeyPair getCustomerKeys(String customerId) {
        return customerKeys.get(customerId);
    }

    public PublicKey getBankPublicKey(String bankId) {
        return bankKeys.get(bankId);
    }
}
```

## 🔐 Digital Certificates

### X.509 Certificate

```java
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;

public class CertificateManager {

    public X509Certificate loadCertificate(byte[] certificateBytes) throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream in = new ByteArrayInputStream(certificateBytes);
        return (X509Certificate) certFactory.generateCertificate(in);
    }

    public boolean verifyCertificate(X509Certificate certificate, PublicKey caPublicKey) throws Exception {
        certificate.verify(caPublicKey);
        return true;
    }

    public String getCertificateInfo(X509Certificate certificate) {
        return String.format(
            "Subject: %s\nIssuer: %s\nValid From: %s\nValid To: %s",
            certificate.getSubjectDN(),
            certificate.getIssuerDN(),
            certificate.getNotBefore(),
            certificate.getNotAfter()
        );
    }
}
```

## 🔧 Implementation Tasks

### Week 6: Public Key Cryptography

- [ ] Implement RSA key generation và encryption
- [ ] Implement ECDSA key generation và signature
- [ ] Implement Diffie-Hellman key exchange
- [ ] Create secure banking communication system
- [ ] Implement key management system
- [ ] Handle digital certificates

## 🎯 Kết quả đạt được

### Lý thuyết

- ✅ Hiểu RSA algorithm
- ✅ Nắm vững ECDSA algorithm
- ✅ Biết Diffie-Hellman key exchange
- ✅ Hiểu digital certificates

### Thực hành

- ✅ RSA encryption/decryption
- ✅ ECDSA signature/verification
- ✅ Diffie-Hellman key exchange
- ✅ Secure communication system
- ✅ Key management system

### Kỹ năng

- ✅ Public key cryptography
- ✅ Key management
- ✅ Secure communication
- ✅ Certificate handling

## 💻 Implementation hoàn chỉnh

### RSA Implementation

```java
// Key generation
KeyPair keyPair = RSAUtils.generateKeyPair(2048);

// Encryption/Decryption
String encrypted = RSAUtils.encrypt("Hello World", keyPair.getPublic());
String decrypted = RSAUtils.decrypt(encrypted, keyPair.getPrivate());

// Digital signature
String signature = RSAUtils.sign("Hello World", keyPair.getPrivate());
boolean isValid = RSAUtils.verify("Hello World", signature, keyPair.getPublic());
```

### Diffie-Hellman Implementation

```java
// Key generation
KeyPair keyPairA = DiffieHellmanUtils.generateKeyPair();
KeyPair keyPairB = DiffieHellmanUtils.generateKeyPair();

// Key exchange
byte[] sharedSecret = DiffieHellmanUtils.performKeyAgreement(
    keyPairA.getPrivate(), keyPairB.getPublic());
```

### Test Results

- ✅ RSAUtilsTest: 8/8 tests passed
- ✅ DiffieHellmanUtilsTest: 9/9 tests passed
- ✅ DigitalSignatureTest: 12/12 tests passed
- ✅ HashUtilsTest: 9/9 tests passed
- ✅ **Total: 38/38 tests passed**

## 🚀 Bước tiếp theo

Sẵn sàng cho **Week 7: Security in Banking** - Học về PKI, TLS, HSM
