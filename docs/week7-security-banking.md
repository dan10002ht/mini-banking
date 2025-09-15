# 📚 Week 7: Security in Banking

## 🎯 Mục tiêu

Học về Security trong Banking - PKI, TLS, HSM, mã hóa dữ liệu.

## 📖 Nội dung học tập

### 1. PKI (Public Key Infrastructure)

- **Certificate Authority (CA)**: Quản lý digital certificates
- **Certificate Chain**: Trust hierarchy
- **Certificate Validation**: Verify certificate authenticity

### 2. TLS (Transport Layer Security)

- **TLS Handshake**: Secure connection establishment
- **Certificate Exchange**: Mutual authentication
- **Encrypted Communication**: Data protection in transit

### 3. HSM (Hardware Security Module)

- **Key Storage**: Secure key management
- **Cryptographic Operations**: Hardware-based security
- **Compliance**: FIPS 140-2, Common Criteria

### 4. Data Encryption

- **AES**: Advanced Encryption Standard
- **Data at Rest**: Database encryption
- **Data in Transit**: Network encryption

## 🔐 PKI (Public Key Infrastructure)

### Certificate Authority

```java
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.util.Date;

public class CertificateAuthority {
    private KeyPair caKeyPair;
    private X509Certificate caCertificate;

    public void initializeCA() throws Exception {
        // Generate CA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.caKeyPair = keyGen.generateKeyPair();

        // Create self-signed CA certificate
        this.caCertificate = createSelfSignedCertificate();
    }

    public X509Certificate issueCertificate(String subjectDN, PublicKey publicKey) throws Exception {
        // Create certificate for subject
        return createCertificate(subjectDN, publicKey, caKeyPair.getPrivate());
    }

    public boolean verifyCertificate(X509Certificate certificate) throws Exception {
        certificate.verify(caKeyPair.getPublic());
        return true;
    }

    private X509Certificate createSelfSignedCertificate() throws Exception {
        // Implementation for self-signed certificate
        // This is a simplified version
        return null; // Would need proper X.509 certificate creation
    }

    private X509Certificate createCertificate(String subjectDN, PublicKey publicKey, PrivateKey caPrivateKey) throws Exception {
        // Implementation for certificate creation
        // This is a simplified version
        return null; // Would need proper X.509 certificate creation
    }
}
```

### Certificate Validation

```java
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Date;

public class CertificateValidator {

    public boolean validateCertificate(X509Certificate certificate, X509Certificate caCertificate) throws Exception {
        try {
            // 1. Verify certificate signature
            certificate.verify(caCertificate.getPublicKey());

            // 2. Check certificate validity period
            Date now = new Date();
            if (now.before(certificate.getNotBefore()) || now.after(certificate.getNotAfter())) {
                throw new CertificateException("Certificate is not valid for current date");
            }

            // 3. Check certificate revocation (simplified)
            if (isCertificateRevoked(certificate)) {
                throw new CertificateException("Certificate has been revoked");
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCertificateRevoked(X509Certificate certificate) {
        // Simplified revocation check
        // In real implementation, would check CRL or OCSP
        return false;
    }
}
```

## 🔒 TLS (Transport Layer Security)

### TLS Client

```java
import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class TLSClient {
    private SSLSocket sslSocket;
    private SSLSocketFactory sslSocketFactory;

    public void initializeTLS() throws Exception {
        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);

        // Create socket factory
        this.sslSocketFactory = sslContext.getSocketFactory();
    }

    public void connectToServer(String host, int port) throws Exception {
        // Create SSL socket
        this.sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);

        // Enable all cipher suites
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        // Start handshake
        sslSocket.startHandshake();

        System.out.println("TLS connection established");
    }

    public void sendSecureMessage(String message) throws Exception {
        PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
        out.println(message);
    }

    public String receiveSecureMessage() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        return in.readLine();
    }

    public void closeConnection() throws Exception {
        if (sslSocket != null) {
            sslSocket.close();
        }
    }
}
```

### TLS Server

```java
import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class TLSServer {
    private SSLServerSocket sslServerSocket;
    private SSLServerSocketFactory sslServerSocketFactory;

    public void initializeTLS() throws Exception {
        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);

        // Create server socket factory
        this.sslServerSocketFactory = sslContext.getServerSocketFactory();
    }

    public void startServer(int port) throws Exception {
        // Create SSL server socket
        this.sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

        // Enable all cipher suites
        sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());

        System.out.println("TLS Server started on port " + port);
    }

    public void handleClient() throws Exception {
        SSLSocket clientSocket = (SSLSocket) sslServerSocket.accept();

        // Start handshake
        clientSocket.startHandshake();

        // Handle client communication
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String message = in.readLine();
        System.out.println("Received: " + message);

        out.println("Message received securely");

        clientSocket.close();
    }
}
```

## 🔐 HSM (Hardware Security Module)

### HSM Interface

```java
public interface HSMInterface {
    // Key management
    String generateKey(String keyId);
    String getPublicKey(String keyId);
    boolean deleteKey(String keyId);

    // Cryptographic operations
    byte[] sign(String keyId, byte[] data);
    boolean verify(String keyId, byte[] data, byte[] signature);
    byte[] encrypt(String keyId, byte[] data);
    byte[] decrypt(String keyId, byte[] data);
}

public class HSMService implements HSMInterface {
    private Map<String, KeyPair> keyStore;

    public HSMService() {
        this.keyStore = new HashMap<>();
    }

    @Override
    public String generateKey(String keyId) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        keyStore.put(keyId, keyPair);
        return "Key generated successfully";
    }

    @Override
    public String getPublicKey(String keyId) {
        KeyPair keyPair = keyStore.get(keyId);
        if (keyPair != null) {
            return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        }
        return null;
    }

    @Override
    public byte[] sign(String keyId, byte[] data) throws Exception {
        KeyPair keyPair = keyStore.get(keyId);
        if (keyPair == null) {
            throw new Exception("Key not found");
        }

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        return signature.sign();
    }

    @Override
    public boolean verify(String keyId, byte[] data, byte[] signature) throws Exception {
        KeyPair keyPair = keyStore.get(keyId);
        if (keyPair == null) {
            return false;
        }

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(keyPair.getPublic());
        sig.update(data);
        return sig.verify(signature);
    }
}
```

## 🔐 Data Encryption

### AES Encryption

```java
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESEncryption {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    public byte[] encrypt(String plainText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plainText.getBytes());
    }

    public String decrypt(byte[] cipherText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes);
    }

    public String encryptToString(String plainText, SecretKey key) throws Exception {
        byte[] encryptedBytes = encrypt(plainText, key);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptFromString(String encryptedText, SecretKey key) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        return decrypt(encryptedBytes, key);
    }
}
```

### Database Encryption

```java
public class DatabaseEncryption {
    private AESEncryption aesEncryption;
    private SecretKey encryptionKey;

    public DatabaseEncryption() throws Exception {
        this.aesEncryption = new AESEncryption();
        this.encryptionKey = aesEncryption.generateKey();
    }

    public String encryptSensitiveData(String sensitiveData) throws Exception {
        return aesEncryption.encryptToString(sensitiveData, encryptionKey);
    }

    public String decryptSensitiveData(String encryptedData) throws Exception {
        return aesEncryption.decryptFromString(encryptedData, encryptionKey);
    }

    // Example: Encrypt customer data
    public Customer encryptCustomerData(Customer customer) throws Exception {
        Customer encryptedCustomer = new Customer();
        encryptedCustomer.setCustomerId(customer.getCustomerId());
        encryptedCustomer.setFirstName(encryptSensitiveData(customer.getFirstName()));
        encryptedCustomer.setLastName(encryptSensitiveData(customer.getLastName()));
        encryptedCustomer.setEmail(encryptSensitiveData(customer.getEmail()));
        return encryptedCustomer;
    }

    public Customer decryptCustomerData(Customer encryptedCustomer) throws Exception {
        Customer customer = new Customer();
        customer.setCustomerId(encryptedCustomer.getCustomerId());
        customer.setFirstName(decryptSensitiveData(encryptedCustomer.getFirstName()));
        customer.setLastName(decryptSensitiveData(encryptedCustomer.getLastName()));
        customer.setEmail(decryptSensitiveData(encryptedCustomer.getEmail()));
        return customer;
    }
}
```

## 🏦 Banking Security Implementation

### Secure Banking Service

```java
@Service
public class SecureBankingService {

    @Autowired
    private HSMService hsmService;

    @Autowired
    private DatabaseEncryption databaseEncryption;

    @Autowired
    private CertificateValidator certificateValidator;

    public SecureTransaction processSecureTransaction(SecureTransactionRequest request) throws Exception {
        // 1. Validate client certificate
        if (!certificateValidator.validateCertificate(request.getClientCertificate(), getCACertificate())) {
            throw new SecurityException("Invalid client certificate");
        }

        // 2. Verify transaction signature
        String transactionData = request.getTransactionData();
        byte[] signature = request.getSignature();
        String clientKeyId = request.getClientKeyId();

        if (!hsmService.verify(clientKeyId, transactionData.getBytes(), signature)) {
            throw new SecurityException("Invalid transaction signature");
        }

        // 3. Process transaction with HSM signing
        String bankKeyId = "BANK_SIGNING_KEY";
        byte[] bankSignature = hsmService.sign(bankKeyId, transactionData.getBytes());

        // 4. Encrypt sensitive data
        String encryptedData = databaseEncryption.encryptSensitiveData(transactionData);

        // 5. Create secure transaction
        SecureTransaction secureTransaction = new SecureTransaction();
        secureTransaction.setTransactionData(encryptedData);
        secureTransaction.setBankSignature(bankSignature);
        secureTransaction.setTimestamp(new Date());

        return secureTransaction;
    }
}
```

## 🔧 Implementation Tasks

### Week 7: Security in Banking

- [ ] Implement PKI certificate management
- [ ] Create TLS client/server
- [ ] Implement HSM interface
- [ ] Create AES encryption system
- [ ] Implement database encryption
- [ ] Create secure banking service

## 🎯 Kết quả đạt được

### Lý thuyết

- ✅ Hiểu PKI và certificate management
- ✅ Nắm vững TLS protocol
- ✅ Biết HSM và hardware security
- ✅ Hiểu data encryption

### Thực hành

- ✅ PKI implementation
- ✅ TLS client/server
- ✅ HSM interface
- ✅ AES encryption
- ✅ Database encryption
- ✅ Secure banking service

### Kỹ năng

- ✅ Security architecture
- ✅ Certificate management
- ✅ Secure communication
- ✅ Data protection
- ✅ Compliance requirements

## 🚀 Bước tiếp theo

Sẵn sàng cho **Week 8-9: Blockchain Basics** - Học về Block structure, hash chain, mining

