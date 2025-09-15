package com.minibanking.repository;

import com.minibanking.entity.Account;
import com.minibanking.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Optional<Transaction> findByTransactionCode(String transactionCode);
    
    List<Transaction> findByFromAccount(Account fromAccount);
    
    List<Transaction> findByToAccount(Account toAccount);
    
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    List<Transaction> findByTransactionType(Transaction.TransactionType transactionType);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId) AND t.status = :status")
    List<Transaction> findByAccountIdAndStatus(@Param("accountId") UUID accountId, @Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.accountId = :accountId OR t.toAccount.accountId = :accountId) ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") UUID accountId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findOutgoingTransactionsByAccountIdAndDateRange(@Param("accountId") UUID accountId, 
                                                                     @Param("startDate") LocalDateTime startDate, 
                                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.toAccount.accountId = :accountId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findIncomingTransactionsByAccountIdAndDateRange(@Param("accountId") UUID accountId, 
                                                                     @Param("startDate") LocalDateTime startDate, 
                                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.accountId = :accountId AND t.status = 'COMPLETED'")
    Double getTotalOutgoingAmountByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.toAccount.accountId = :accountId AND t.status = 'COMPLETED'")
    Double getTotalIncomingAmountByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.fromAccount LEFT JOIN FETCH t.toAccount WHERE t.transactionId = :transactionId")
    Optional<Transaction> findByIdWithAccounts(@Param("transactionId") UUID transactionId);
}

