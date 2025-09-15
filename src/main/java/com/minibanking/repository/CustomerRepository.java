package com.minibanking.repository;

import com.minibanking.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    Optional<Customer> findByCustomerCode(String customerCode);
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByIdNumber(String idNumber);
    
    List<Customer> findByStatus(Customer.CustomerStatus status);
    
    List<Customer> findByKycStatus(Customer.KycStatus kycStatus);
    
    @Query("SELECT c FROM Customer c WHERE c.firstName LIKE %:name% OR c.lastName LIKE %:name%")
    List<Customer> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Customer c WHERE c.riskLevel = :riskLevel")
    List<Customer> findByRiskLevel(@Param("riskLevel") Customer.RiskLevel riskLevel);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = 'ACTIVE'")
    long countActiveCustomers();
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.accounts WHERE c.customerId = :customerId")
    Optional<Customer> findByIdWithAccounts(@Param("customerId") UUID customerId);
}

