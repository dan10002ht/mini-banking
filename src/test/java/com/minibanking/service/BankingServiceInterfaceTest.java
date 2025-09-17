package com.minibanking.service;

import com.minibanking.entity.Account;
import com.minibanking.entity.Customer;
import com.minibanking.entity.Transaction;
import com.minibanking.exception.AccountNotFoundException;
import com.minibanking.exception.InsufficientFundsException;
import com.minibanking.interfaces.IBankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class demonstrating Interface usage
 * Shows how Interface makes testing easier
 */
@ExtendWith(MockitoExtension.class)
class BankingServiceInterfaceTest {
    
    @Mock
    private IBankingService bankingService;
    
    @Test
    void testTransferMoney_WithInterface() {
        // Given
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Test transfer";
        
        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionCode("TXN123");
        mockTransaction.setAmount(amount);
        
        // Mock the interface method
        when(bankingService.transferMoney(fromAccountId, toAccountId, amount, description))
            .thenReturn(mockTransaction);
        
        // When
        Transaction result = bankingService.transferMoney(fromAccountId, toAccountId, amount, description);
        
        // Then
        assertNotNull(result);
        assertEquals("TXN123", result.getTransactionCode());
        assertEquals(amount, result.getAmount());
        
        // Verify the method was called
        verify(bankingService).transferMoney(fromAccountId, toAccountId, amount, description);
    }
    
    @Test
    void testTransferMoney_InsufficientFunds() {
        // Given
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.00");
        String description = "Test transfer";
        
        // Mock the interface to throw exception
        when(bankingService.transferMoney(fromAccountId, toAccountId, amount, description))
            .thenThrow(new InsufficientFundsException("Insufficient balance"));
        
        // When & Then
        assertThrows(InsufficientFundsException.class, () -> {
            bankingService.transferMoney(fromAccountId, toAccountId, amount, description);
        });
        
        // Verify the method was called
        verify(bankingService).transferMoney(fromAccountId, toAccountId, amount, description);
    }
    
    @Test
    void testGetAccountById_WithInterface() {
        // Given
        UUID accountId = UUID.randomUUID();
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setAccountNumber("ACC123");
        mockAccount.setBalance(new BigDecimal("500.00"));
        
        // Mock the interface method
        when(bankingService.getAccountById(accountId))
            .thenReturn(Optional.of(mockAccount));
        
        // When
        Optional<Account> result = bankingService.getAccountById(accountId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(accountId, result.get().getAccountId());
        assertEquals("ACC123", result.get().getAccountNumber());
        assertEquals(new BigDecimal("500.00"), result.get().getBalance());
        
        // Verify the method was called
        verify(bankingService).getAccountById(accountId);
    }
    
    @Test
    void testGetAccountById_NotFound() {
        // Given
        UUID accountId = UUID.randomUUID();
        
        // Mock the interface to return empty
        when(bankingService.getAccountById(accountId))
            .thenReturn(Optional.empty());
        
        // When
        Optional<Account> result = bankingService.getAccountById(accountId);
        
        // Then
        assertFalse(result.isPresent());
        
        // Verify the method was called
        verify(bankingService).getAccountById(accountId);
    }
    
    @Test
    void testCreateCustomer_WithInterface() {
        // Given
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        
        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerId(UUID.randomUUID());
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");
        savedCustomer.setEmail("john.doe@example.com");
        
        // Mock the interface method
        when(bankingService.createCustomer(any(Customer.class)))
            .thenReturn(savedCustomer);
        
        // When
        Customer result = bankingService.createCustomer(customer);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getCustomerId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        
        // Verify the method was called
        verify(bankingService).createCustomer(any(Customer.class));
    }
}
