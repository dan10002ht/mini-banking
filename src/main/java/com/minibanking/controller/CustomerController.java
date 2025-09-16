package com.minibanking.controller;

import com.minibanking.entity.Customer;
import com.minibanking.service.BankingService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Management", description = "Customer CRUD operations and management")
public class CustomerController {
    
    @Autowired
    private BankingService bankingService;
    
    @Operation(summary = "Create a new customer", description = "Creates a new customer in the banking system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class))),
        @ApiResponse(responseCode = "400", description = "Invalid customer data")
    })
    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @Parameter(description = "Customer information", required = true)
            @RequestBody Customer customer) {
        try {
            Customer createdCustomer = bankingService.createCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(summary = "Get customer by ID", description = "Retrieves a customer by their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable UUID customerId) {
        return bankingService.getCustomerById(customerId)
            .map(customer -> ResponseEntity.ok(customer))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get all customers", description = "Retrieves all customers in the banking system")
    @ApiResponse(responseCode = "200", description = "List of all customers",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)))
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = bankingService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    @Operation(summary = "Update customer", description = "Updates an existing customer's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "400", description = "Invalid customer data")
    })
    @PutMapping("/{customerId}")
    public ResponseEntity<Customer> updateCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable UUID customerId,
            @Parameter(description = "Updated customer information", required = true)
            @RequestBody Customer customer) {
        try {
            customer.setCustomerId(customerId);
            Customer updatedCustomer = bankingService.updateCustomer(customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Delete customer", description = "Deletes a customer from the banking system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable UUID customerId) {
        // Note: BankingService doesn't have deleteCustomer method
        return ResponseEntity.notFound().build();
    }
}
