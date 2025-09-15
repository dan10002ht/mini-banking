package com.minibanking.repository;

import com.minibanking.entity.Account;
import com.minibanking.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByCustomer(Customer customer);
    
    List<Account> findByCustomerCustomerId(UUID customerId);
    
    List<Account> findByStatus(Account.AccountStatus status);
    
    List<Account> findByAccountType(Account.AccountType accountType);
    
    @Query("SELECT a FROM Account a WHERE a.customer.customerId = :customerId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance")
    List<Account> findByBalanceGreaterThanEqual(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT a FROM Account a WHERE a.balance < :maxBalance")
    List<Account> findByBalanceLessThan(@Param("maxBalance") BigDecimal maxBalance);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountId = :accountId")
    Optional<Account> findByIdForUpdate(@Param("accountId") UUID accountId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
    
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT a FROM Account a WHERE a.accountId = :accountId")
    Optional<Account> findByIdForRead(@Param("accountId") UUID accountId);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.customer.customerId = :customerId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'ACTIVE'")
    long countActiveAccounts();
    
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.customer WHERE a.accountId = :accountId")
    Optional<Account> findByIdWithCustomer(@Param("accountId") UUID accountId);
}

