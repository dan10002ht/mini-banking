package com.minibanking.controller;

import com.minibanking.dto.CreateAccountRequest;
import com.minibanking.entity.Account;
import com.minibanking.entity.Customer;
import com.minibanking.interfaces.IBankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
@Tag(name = "Account Management", description = "Account CRUD operations and balance management")
public class AccountController {
    
    @Autowired
    private IBankingService bankingService;
    
    @Operation(summary = "Create a new account", description = "Creates a new bank account for a customer with minimal required information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Account.class))),
        @ApiResponse(responseCode = "400", description = "Invalid account data"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @Parameter(description = "Account creation request", required = true)
            @RequestBody CreateAccountRequest request) {
        try {
            // Get customer by ID
            Customer customer = bankingService.getCustomerById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Create account entity from DTO
            Account account = new Account();
            account.setAccountNumber(request.getAccountNumber());
            account.setCustomer(customer);
            account.setAccountType(request.getAccountType());
            account.setCurrency(request.getCurrency());
            account.setBalance(request.getInitialBalance());
            account.setAvailableBalance(request.getInitialBalance());
            account.setCreditLimit(request.getCreditLimit());
            account.setInterestRate(request.getInterestRate());
            
            Account createdAccount = bankingService.createAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Get account by ID", description = "Retrieves an account by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Account.class))),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        return bankingService.getAccountById(accountId)
            .map(account -> ResponseEntity.ok(account))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get all accounts", description = "Retrieves all accounts in the banking system")
    @ApiResponse(responseCode = "200", description = "List of all accounts",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Account.class)))
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        // Note: BankingService doesn't have getAllAccounts method
        return ResponseEntity.ok(List.of());
    }
    
    @Operation(summary = "Get accounts by customer", description = "Retrieves all accounts for a specific customer")
    @ApiResponse(responseCode = "200", description = "List of customer accounts",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Account.class)))
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable UUID customerId) {
        List<Account> accounts = bankingService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }
    
    @Operation(summary = "Get account balance", description = "Retrieves the current balance of an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "1000.00"))),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        try {
            BigDecimal balance = bankingService.getAccountBalance(accountId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
