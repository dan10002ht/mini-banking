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
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
@Tag(name = "Transfer Operations", description = "Money transfer, deposit, and withdrawal operations")
public class TransferController {
    
    @Autowired
    private IBankingService bankingService;
    
    @Operation(summary = "Transfer money between accounts", description = "Transfers money from one account to another")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer successful",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "422", description = "Insufficient funds")
    })
    @PostMapping
    public ResponseEntity<Transaction> transferMoney(
            @Parameter(description = "Source account ID", required = true)
            @RequestParam UUID fromAccountId,
            @Parameter(description = "Destination account ID", required = true)
            @RequestParam UUID toAccountId,
            @Parameter(description = "Transfer amount", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "Transfer description", required = false)
            @RequestParam(required = false) String description) {
        try {
            Transaction transaction = bankingService.transferMoney(fromAccountId, toAccountId, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Deposit money to account", description = "Deposits money to a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/deposit")
    public ResponseEntity<Transaction> depositMoney(
            @Parameter(description = "Account ID", required = true)
            @RequestParam UUID accountId,
            @Parameter(description = "Deposit amount", required = true)
            @RequestParam BigDecimal amount) {
        try {
            Transaction transaction = bankingService.deposit(accountId, amount, "Deposit");
            return ResponseEntity.ok(transaction);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Withdraw money from account", description = "Withdraws money from a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "422", description = "Insufficient funds")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdrawMoney(
            @Parameter(description = "Account ID", required = true)
            @RequestParam UUID accountId,
            @Parameter(description = "Withdrawal amount", required = true)
            @RequestParam BigDecimal amount) {
        try {
            Transaction transaction = bankingService.withdraw(accountId, amount, "Withdrawal");
            return ResponseEntity.ok(transaction);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
