package com.minibanking.interfaces;

import com.minibanking.entity.Account;
import com.minibanking.entity.Customer;
import com.minibanking.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Banking Service Interface
 * Defines contract for banking operations
 */
public interface IBankingService {
    
    // ==================== CUSTOMER MANAGEMENT ====================
    
    /**
     * Create a new customer
     * @param customer Customer entity
     * @return Created customer
     */
    Customer createCustomer(Customer customer);
    
    /**
     * Get customer by ID
     * @param customerId Customer ID
     * @return Customer if found
     */
    Optional<Customer> getCustomerById(UUID customerId);
    
    /**
     * Get customer by customer code
     * @param customerCode Customer code
     * @return Customer if found
     */
    Optional<Customer> getCustomerByCode(String customerCode);
    
    /**
     * Get all customers
     * @return List of all customers
     */
    List<Customer> getAllCustomers();
    
    /**
     * Update customer information
     * @param customer Customer entity
     * @return Updated customer
     */
    Customer updateCustomer(Customer customer);
    
    // ==================== ACCOUNT MANAGEMENT ====================
    
    /**
     * Create a new account
     * @param account Account entity
     * @return Created account
     */
    Account createAccount(Account account);
    
    /**
     * Get account by ID
     * @param accountId Account ID
     * @return Account if found
     */
    Optional<Account> getAccountById(UUID accountId);
    
    /**
     * Get account by account number
     * @param accountNumber Account number
     * @return Account if found
     */
    Optional<Account> getAccountByNumber(String accountNumber);
    
    /**
     * Get accounts by customer ID
     * @param customerId Customer ID
     * @return List of accounts for customer
     */
    List<Account> getAccountsByCustomerId(UUID customerId);
    
    /**
     * Update account information
     * @param account Account entity
     * @return Updated account
     */
    Account updateAccount(Account account);
    
    // ==================== TRANSACTION PROCESSING ====================
    
    /**
     * Transfer money between accounts
     * @param fromAccountId Source account ID
     * @param toAccountId Destination account ID
     * @param amount Transfer amount
     * @param description Transfer description
     * @return Transaction record
     */
    Transaction transferMoney(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description);
    
    /**
     * Deposit money to account
     * @param accountId Account ID
     * @param amount Deposit amount
     * @param description Deposit description
     * @return Transaction record
     */
    Transaction deposit(UUID accountId, BigDecimal amount, String description);
    
    /**
     * Withdraw money from account
     * @param accountId Account ID
     * @param amount Withdrawal amount
     * @param description Withdrawal description
     * @return Transaction record
     */
    Transaction withdraw(UUID accountId, BigDecimal amount, String description);
    
    // ==================== TRANSACTION HISTORY ====================
    
    /**
     * Get transaction history for account
     * @param accountId Account ID
     * @return List of transactions
     */
    List<Transaction> getTransactionHistory(UUID accountId);
    
    /**
     * Get transaction by transaction code
     * @param transactionCode Transaction code
     * @return Transaction if found
     */
    Optional<Transaction> getTransactionByCode(String transactionCode);
    
    /**
     * Get transactions by status
     * @param status Transaction status
     * @return List of transactions with status
     */
    List<Transaction> getTransactionsByStatus(Transaction.TransactionStatus status);
    
    // ==================== ACCOUNT BALANCE ====================
    
    /**
     * Get account balance
     * @param accountId Account ID
     * @return Account balance
     */
    BigDecimal getAccountBalance(UUID accountId);
    
    /**
     * Get available balance (considering holds)
     * @param accountId Account ID
     * @return Available balance
     */
    BigDecimal getAvailableBalance(UUID accountId);
    
    /**
     * Get account for read operations
     * @param accountId Account ID
     * @return Account entity
     */
    Account getAccountForRead(UUID accountId);
}
