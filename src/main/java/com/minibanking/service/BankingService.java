package com.minibanking.service;

import com.minibanking.interfaces.IBankingService;
import com.minibanking.entity.Account;
import com.minibanking.entity.Customer;
import com.minibanking.entity.Transaction;
import com.minibanking.exception.AccountNotFoundException;
import com.minibanking.exception.InsufficientFundsException;
import com.minibanking.repository.AccountRepository;
import com.minibanking.repository.CustomerRepository;
import com.minibanking.repository.TransactionRepository;
import com.minibanking.blockchain.TransactionEvent;
import com.minibanking.blockchain.TransactionStreamProducer;
import com.minibanking.security.logging.SecureLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BankingService implements IBankingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankingService.class);
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionStreamProducer streamProducer;
    
    @Autowired
    private SecureLoggingService secureLoggingService;
    
    // Customer Management
    public Customer createCustomer(Customer customer) {
        logger.info("Creating customer: {}", customer.getEmail());
        return customerRepository.save(customer);
    }
    
    public Optional<Customer> getCustomerById(UUID customerId) {
        return customerRepository.findById(customerId);
    }
    
    public Optional<Customer> getCustomerByCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode);
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Customer updateCustomer(Customer customer) {
        logger.info("Updating customer: {}", customer.getCustomerId());
        return customerRepository.save(customer);
    }
    
    // Account Management
    public Account createAccount(Account account) {
        logger.info("Creating account: {} for customer: {}", account.getAccountNumber(), account.getCustomer().getCustomerId());
        return accountRepository.save(account);
    }
    
    public Optional<Account> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }
    
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    public List<Account> getAccountsByCustomerId(UUID customerId) {
        return accountRepository.findByCustomerCustomerId(customerId);
    }
    
    public Account updateAccount(Account account) {
        logger.info("Updating account: {}", account.getAccountId());
        return accountRepository.save(account);
    }
    
    // Transaction Processing with Pessimistic Locking (Banking Standard)
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Transaction transferMoney(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description) {
        logger.info("Processing transfer: {} from {} to {}", amount, fromAccountId, toAccountId);
        
        // 1. Lock accounts for update to prevent concurrent access (Banking Standard)
        Account fromAccount = accountRepository.findByIdForUpdate(fromAccountId)
            .orElseThrow(() -> new AccountNotFoundException("From account not found"));
        
        Account toAccount = accountRepository.findByIdForUpdate(toAccountId)
            .orElseThrow(() -> new AccountNotFoundException("To account not found"));
        
        // 2. Validate accounts
        if (!fromAccount.getStatus().equals(Account.AccountStatus.ACTIVE)) {
            throw new IllegalStateException("From account is not active");
        }
        
        if (!toAccount.getStatus().equals(Account.AccountStatus.ACTIVE)) {
            throw new IllegalStateException("To account is not active");
        }
        
        // 3. Check sufficient balance
        if (!fromAccount.hasSufficientBalance(amount)) {
            throw new InsufficientFundsException("Insufficient balance in from account");
        }
        
        // 4. Create transaction record
        String transactionCode = generateTransactionCode();
        Transaction transaction = new Transaction();
        transaction.setTransactionCode(transactionCode);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        try {
            // 5. Process the transfer
            fromAccount.debit(amount);
            toAccount.credit(amount);
            
            // 6. Save accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            // 7. Mark transaction as completed
            transaction.markAsCompleted();
            transaction = transactionRepository.save(transaction);
            
            // 8. Send transaction to blockchain stream
            try {
                TransactionEvent transactionEvent = new TransactionEvent(transaction);
                streamProducer.sendTransactionEvent(transactionEvent);
                logger.info("Transaction event sent to blockchain stream: {}", transactionCode);
            } catch (Exception e) {
                logger.warn("Failed to send transaction to blockchain stream: {}", e.getMessage());
                // Don't fail the transaction if blockchain fails
            }
            
            // 9. Log transfer securely
            secureLoggingService.logTransfer(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                amount,
                description,
                "SUCCESS"
            );
            
            logger.info("Transfer completed successfully: {}", transactionCode);
            return transaction;
            
        } catch (Exception e) {
            // 8. Mark transaction as failed
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            // Log error securely
            secureLoggingService.logError("Transfer", e.getMessage(),
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                amount.toString()
            );
            
            throw e;
        }
    }
    
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Transaction deposit(UUID accountId, BigDecimal amount, String description) {
        logger.info("Processing deposit: {} to account: {}", amount, accountId);
        
        // 1. Lock account for update (Banking Standard - Pessimistic Locking)
        Account account = accountRepository.findByIdForUpdate(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        
        // 2. Validate account
        if (!account.getStatus().equals(Account.AccountStatus.ACTIVE)) {
            throw new IllegalStateException("Account is not active");
        }
        
        // 3. Create transaction record
        String transactionCode = generateTransactionCode();
        Transaction transaction = new Transaction();
        transaction.setTransactionCode(transactionCode);
        transaction.setToAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        try {
            // 4. Process the deposit
            account.credit(amount);
            
            // 5. Save account
            accountRepository.save(account);
            
            // 6. Mark transaction as completed
            transaction.markAsCompleted();
            transaction = transactionRepository.save(transaction);
            
            // 7. Send transaction to blockchain stream
            try {
                TransactionEvent transactionEvent = new TransactionEvent(transaction);
                streamProducer.sendTransactionEvent(transactionEvent);
                logger.info("Transaction event sent to blockchain stream: {}", transactionCode);
            } catch (Exception e) {
                logger.warn("Failed to send transaction to blockchain stream: {}", e.getMessage());
                // Don't fail the transaction if blockchain fails
            }
            
            logger.info("Deposit completed successfully: {}", transactionCode);
            return transaction;
            
        } catch (Exception e) {
            // 7. Mark transaction as failed
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            logger.error("Deposit failed: {}", e.getMessage());
            throw e;
        }
    }
    
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Transaction withdraw(UUID accountId, BigDecimal amount, String description) {
        logger.info("Processing withdrawal: {} from account: {}", amount, accountId);
        
        // 1. Lock account for update (Banking Standard - Pessimistic Locking)
        Account account = accountRepository.findByIdForUpdate(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        
        // 2. Validate account
        if (!account.getStatus().equals(Account.AccountStatus.ACTIVE)) {
            throw new IllegalStateException("Account is not active");
        }
        
        // 3. Check sufficient balance
        if (!account.hasSufficientBalance(amount)) {
            throw new InsufficientFundsException("Insufficient balance");
        }
        
        // 4. Create transaction record
        String transactionCode = generateTransactionCode();
        Transaction transaction = new Transaction();
        transaction.setTransactionCode(transactionCode);
        transaction.setFromAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        try {
            // 5. Process the withdrawal
            account.debit(amount);
            
            // 6. Save account
            accountRepository.save(account);
            
            // 7. Mark transaction as completed
            transaction.markAsCompleted();
            transaction = transactionRepository.save(transaction);
            
            // 8. Send transaction to blockchain stream
            try {
                TransactionEvent transactionEvent = new TransactionEvent(transaction);
                streamProducer.sendTransactionEvent(transactionEvent);
                logger.info("Transaction event sent to blockchain stream: {}", transactionCode);
            } catch (Exception e) {
                logger.warn("Failed to send transaction to blockchain stream: {}", e.getMessage());
                // Don't fail the transaction if blockchain fails
            }
            
            logger.info("Withdrawal completed successfully: {}", transactionCode);
            return transaction;
            
        } catch (Exception e) {
            // 8. Mark transaction as failed
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            logger.error("Withdrawal failed: {}", e.getMessage());
            throw e;
        }
    }
    
    // Transaction History
    public List<Transaction> getTransactionHistory(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
    
    public Optional<Transaction> getTransactionByCode(String transactionCode) {
        return transactionRepository.findByTransactionCode(transactionCode);
    }
    
    public List<Transaction> getTransactionsByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }
    
    // Utility Methods
    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Account Balance (Read Operations - No Lock Needed)
    public BigDecimal getAccountBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        return account.getBalance();
    }
    
    public BigDecimal getAvailableBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        return account.getAvailableBalance();
    }
    
    // Read Operations with Pessimistic Read Lock (for consistency)
    public Account getAccountForRead(UUID accountId) {
        return accountRepository.findByIdForRead(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }
}

