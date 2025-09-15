# 📚 Week 3-4: Payment Systems

## 🎯 Mục tiêu

Tìm hiểu các hệ thống thanh toán và xử lý transaction atomicity.

## 📖 Nội dung học tập

### 1. ACH (Automated Clearing House)

- **Định nghĩa**: Hệ thống chuyển tiền nội địa
- **Đặc điểm**: Batch processing, low cost, high volume
- **Sử dụng**: Direct deposit, bill payment, P2P transfer

#### ACH Flow

```
1. Originator → ODFI (Originating Depository Financial Institution)
2. ODFI → ACH Network
3. ACH Network → RDFI (Receiving Depository Financial Institution)
4. RDFI → Receiver
```

#### ACH Transaction Types

- **PPD**: Prearranged Payment and Deposit (Direct deposit, bill payment)
- **CCD**: Corporate Credit or Debit (Business-to-business)
- **WEB**: Internet-Initiated Entry (Online payments)
- **TEL**: Telephone-Initiated Entry (Phone payments)

### 2. SWIFT (Society for Worldwide Interbank Financial Telecommunication)

- **Định nghĩa**: Hệ thống chuyển tiền quốc tế
- **Đặc điểm**: Real-time messaging, high cost, secure
- **Sử dụng**: Cross-border payments, trade finance

#### SWIFT Flow

```
1. Sender Bank → SWIFT Network
2. SWIFT Network → Correspondent Bank
3. Correspondent Bank → Receiver Bank
4. Receiver Bank → Beneficiary
```

#### SWIFT Message Types

- **MT103**: Single Customer Credit Transfer
- **MT202**: General Financial Institution Transfer
- **MT700**: Documentary Credit (Letter of Credit)
- **MT760**: Bank Guarantee

### 3. VISA/Mastercard

- **Định nghĩa**: Credit/Debit card processing networks
- **Đặc điểm**: Real-time authorization, high fees, instant settlement
- **Sử dụng**: POS, online payments, ATM

#### Card Transaction Flow

```
1. Cardholder → Merchant
2. Merchant → Acquiring Bank
3. Acquiring Bank → Card Network (VISA/Mastercard)
4. Card Network → Issuing Bank
5. Issuing Bank → Cardholder
```

## 🔄 Transaction Atomicity (ACID)

### ACID Properties trong Banking

- **Atomicity**: Tất cả operations hoặc thành công hoặc rollback
- **Consistency**: Dữ liệu luôn ở trạng thái hợp lệ
- **Isolation**: Transactions độc lập với nhau
- **Durability**: Dữ liệu được lưu trữ vĩnh viễn

### Banking Transaction Example

```java
@Transactional
public void transferMoney(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
    // Atomicity: Tất cả operations trong 1 transaction
    Account fromAccount = accountRepository.findByIdForUpdate(fromAccountId);
    Account toAccount = accountRepository.findByIdForUpdate(toAccountId);

    // Consistency: Validate business rules
    if (!fromAccount.hasSufficientBalance(amount)) {
        throw new InsufficientFundsException();
    }

    // Isolation: Pessimistic locking
    fromAccount.debit(amount);
    toAccount.credit(amount);

    // Durability: Database commit
    accountRepository.save(fromAccount);
    accountRepository.save(toAccount);
}
```

## 💻 Payment Service Implementation

### Payment Service Structure

```java
@Service
public class PaymentService {

    @Autowired
    private BankingService bankingService;

    @Autowired
    private PaymentNetworkService paymentNetworkService;

    // ACH Payment
    @Transactional
    public PaymentResult processACHPayment(ACHPaymentRequest request) {
        // 1. Validate request
        // 2. Process payment
        // 3. Send to ACH network
        // 4. Update accounts
    }

    // SWIFT Payment
    @Transactional
    public PaymentResult processSWIFTPayment(SWIFTPaymentRequest request) {
        // 1. Validate request
        // 2. Process payment
        // 3. Send to SWIFT network
        // 4. Update accounts
    }

    // Card Payment
    @Transactional
    public PaymentResult processCardPayment(CardPaymentRequest request) {
        // 1. Validate request
        // 2. Authorize with card network
        // 3. Process payment
        // 4. Update accounts
    }
}
```

### Payment Network Services

```java
@Service
public class ACHNetworkService {
    public ACHResponse sendACHTransaction(ACHTransaction transaction) {
        // 1. Format ACH message
        // 2. Send to ACH network
        // 3. Handle response
        // 4. Update status
    }
}

@Service
public class SWIFTNetworkService {
    public SWIFTResponse sendSWIFTMessage(SWIFTMessage message) {
        // 1. Format SWIFT message
        // 2. Send to SWIFT network
        // 3. Handle response
        // 4. Update status
    }
}
```

## 📊 So sánh Payment Systems

### ACH vs SWIFT vs VISA/Mastercard

| Feature      | ACH               | SWIFT           | VISA/Mastercard |
| ------------ | ----------------- | --------------- | --------------- |
| **Cost**     | Low (0.1-0.5 USD) | High (5-50 USD) | High (1-3%)     |
| **Speed**    | Slow (1-3 days)   | Slow (1-5 days) | Real-time       |
| **Scope**    | Domestic          | International   | Global          |
| **Volume**   | High              | Medium          | High            |
| **Security** | Medium            | High            | High            |

### Use Cases

- **ACH**: Direct deposit, bill payment, domestic transfers
- **SWIFT**: International wire transfers, trade finance
- **VISA/Mastercard**: POS, online payments, ATM withdrawals

## 🔧 Implementation Tasks

### Week 3: Payment Systems Theory

- [ ] Học ACH, SWIFT, VISA/Mastercard
- [ ] Hiểu payment flows
- [ ] So sánh các payment systems
- [ ] ACID properties trong banking

### Week 4: Payment Systems Practice

- [ ] Implement Payment Service
- [ ] Integrate với payment networks
- [ ] Test transaction atomicity
- [ ] Handle payment failures

## 🎯 Kết quả đạt được

### Lý thuyết

- ✅ Hiểu ACH, SWIFT, VISA/Mastercard
- ✅ Nắm vững payment flows
- ✅ So sánh các payment systems
- ✅ ACID properties trong banking

### Thực hành

- ✅ Payment Service implementation
- ✅ Payment network integration
- ✅ Transaction atomicity testing
- ✅ Error handling và rollback

### Kỹ năng

- ✅ Payment system design
- ✅ Network integration
- ✅ Transaction management
- ✅ Error handling

## 🚀 Bước tiếp theo

Sẵn sàng cho **Week 5: Hash & Signature** - Học về SHA256, Merkle Tree

