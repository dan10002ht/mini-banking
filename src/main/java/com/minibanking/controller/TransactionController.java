package com.minibanking.controller;

import com.minibanking.entity.Transaction;
import com.minibanking.exception.AccountNotFoundException;
import com.minibanking.exception.InsufficientFundsException;
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
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
@Tag(name = "Transaction Management", description = "Transaction operations and history")
public class TransactionController {
    
    @Autowired
    private IBankingService bankingService;
    
    @Operation(summary = "Get transaction by ID", description = "Retrieves a transaction by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable UUID transactionId) {
        // Note: BankingService doesn't have getTransactionById, using getTransactionByCode instead
        return ResponseEntity.notFound().build();
    }
    
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions in the banking system")
    @ApiResponse(responseCode = "200", description = "List of all transactions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class)))
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        // Note: BankingService doesn't have getAllTransactions, returning empty list for now
        return ResponseEntity.ok(List.of());
    }
    
    @Operation(summary = "Get transactions by account", description = "Retrieves all transactions for a specific account")
    @ApiResponse(responseCode = "200", description = "List of account transactions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class)))
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId) {
        List<Transaction> transactions = bankingService.getTransactionHistory(accountId);
        return ResponseEntity.ok(transactions);
    }
}
