package com.minibanking.controller;

import com.minibanking.entity.Account;
import com.minibanking.entity.Customer;
import com.minibanking.entity.Transaction;
import com.minibanking.exception.AccountNotFoundException;
import com.minibanking.exception.InsufficientFundsException;
import com.minibanking.service.BankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banking")
@CrossOrigin(origins = "*")
public class BankingController {
    
    @Autowired
    private BankingService bankingService;
    
    // Customer Endpoints
    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        try {
            Customer createdCustomer = bankingService.createCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable UUID customerId) {
        return bankingService.getCustomerById(customerId)
            .map(customer -> ResponseEntity.ok(customer))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = bankingService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    @PutMapping("/customers/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable UUID customerId, @RequestBody Customer customer) {
        try {
            customer.setCustomerId(customerId);
            Customer updatedCustomer = bankingService.updateCustomer(customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Account Endpoints
    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        try {
            Account createdAccount = bankingService.createAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID accountId) {
        return bankingService.getAccountById(accountId)
            .map(account -> ResponseEntity.ok(account))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/accounts/number/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        return bankingService.getAccountByNumber(accountNumber)
            .map(account -> ResponseEntity.ok(account))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customers/{customerId}/accounts")
    public ResponseEntity<List<Account>> getCustomerAccounts(@PathVariable UUID customerId) {
        List<Account> accounts = bankingService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        try {
            BigDecimal balance = bankingService.getAccountBalance(accountId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/accounts/{accountId}/available-balance")
    public ResponseEntity<BigDecimal> getAvailableBalance(@PathVariable UUID accountId) {
        try {
            BigDecimal availableBalance = bankingService.getAvailableBalance(accountId);
            return ResponseEntity.ok(availableBalance);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Transaction Endpoints
    @PostMapping("/transactions/transfer")
    public ResponseEntity<Transaction> transferMoney(@RequestBody TransferRequest request) {
        try {
            Transaction transaction = bankingService.transferMoney(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/transactions/deposit")
    public ResponseEntity<Transaction> deposit(@RequestBody DepositRequest request) {
        try {
            Transaction transaction = bankingService.deposit(
                request.getAccountId(),
                request.getAmount(),
                request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/transactions/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestBody WithdrawRequest request) {
        try {
            Transaction transaction = bankingService.withdraw(
                request.getAccountId(),
                request.getAmount(),
                request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/transactions/{transactionCode}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionCode) {
        return bankingService.getTransactionByCode(transactionCode)
            .map(transaction -> ResponseEntity.ok(transaction))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable UUID accountId) {
        List<Transaction> transactions = bankingService.getTransactionHistory(accountId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        try {
            Transaction.TransactionStatus transactionStatus = Transaction.TransactionStatus.valueOf(status.toUpperCase());
            List<Transaction> transactions = bankingService.getTransactionsByStatus(transactionStatus);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Request DTOs
    public static class TransferRequest {
        private UUID fromAccountId;
        private UUID toAccountId;
        private BigDecimal amount;
        private String description;
        
        // Getters and Setters
        public UUID getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }
        
        public UUID getToAccountId() { return toAccountId; }
        public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class DepositRequest {
        private UUID accountId;
        private BigDecimal amount;
        private String description;
        
        // Getters and Setters
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class WithdrawRequest {
        private UUID accountId;
        private BigDecimal amount;
        private String description;
        
        // Getters and Setters
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

