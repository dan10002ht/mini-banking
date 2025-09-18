package com.minibanking.controller;

import com.minibanking.entity.Block;
import com.minibanking.entity.Validator;
import com.minibanking.service.ProofOfAuthorityService;
import com.minibanking.service.ValidatorManagementService;
import com.minibanking.blockchain.TransactionEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consensus Controller for Proof of Authority
 */
@RestController
@RequestMapping("/api/consensus")
@CrossOrigin(origins = "*")
@Tag(name = "Consensus", description = "Proof of Authority consensus management")
public class ConsensusController {

    private static final Logger logger = LoggerFactory.getLogger(ConsensusController.class);

    @Autowired
    private ProofOfAuthorityService poaService;

    @Autowired
    private ValidatorManagementService validatorService;

    /**
     * Create block using PoA consensus
     */
    @PostMapping("/poa/create-block")
    @Operation(summary = "Create block with PoA", description = "Create a new block using Proof of Authority consensus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Block created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Block.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Block> createBlockWithPoA(@RequestBody List<TransactionEvent> transactions) {
        try {
            logger.info("Creating block with PoA for {} transactions", transactions.size());

            if (transactions.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Block block = poaService.createBlockWithoutMining(transactions);
            return ResponseEntity.ok(block);

        } catch (Exception e) {
            logger.error("Failed to create block with PoA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get PoA statistics
     */
    @GetMapping("/poa/statistics")
    @Operation(summary = "Get PoA statistics", description = "Get Proof of Authority consensus statistics")
    public ResponseEntity<ProofOfAuthorityService.PoAStatistics> getPoAStatistics() {
        try {
            ProofOfAuthorityService.PoAStatistics stats = poaService.getPoAStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to get PoA statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get authorized validators
     */
    @GetMapping("/validators")
    @Operation(summary = "Get authorized validators", description = "Get list of authorized validators")
    public ResponseEntity<List<Validator>> getAuthorizedValidators() {
        try {
            List<Validator> validators = validatorService.getAuthorizedValidators();
            return ResponseEntity.ok(validators);
        } catch (Exception e) {
            logger.error("Failed to get authorized validators: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active validators
     */
    @GetMapping("/validators/active")
    @Operation(summary = "Get active validators", description = "Get list of active validators")
    public ResponseEntity<List<Validator>> getActiveValidators() {
        try {
            List<Validator> validators = validatorService.getActiveValidators();
            return ResponseEntity.ok(validators);
        } catch (Exception e) {
            logger.error("Failed to get active validators: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get online validators
     */
    @GetMapping("/validators/online")
    @Operation(summary = "Get online validators", description = "Get list of online validators")
    public ResponseEntity<List<Validator>> getOnlineValidators() {
        try {
            List<Validator> validators = validatorService.getOnlineValidators();
            return ResponseEntity.ok(validators);
        } catch (Exception e) {
            logger.error("Failed to get online validators: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get validator by ID
     */
    @GetMapping("/validators/{validatorId}")
    @Operation(summary = "Get validator by ID", description = "Get validator details by ID")
    public ResponseEntity<Validator> getValidatorById(
            @Parameter(description = "Validator ID", required = true) @PathVariable UUID validatorId) {
        try {
            Optional<Validator> validator = validatorService.getValidatorById(validatorId);
            return validator.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Failed to get validator by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add new authorized validator
     */
    @PostMapping("/validators")
    @Operation(summary = "Add authorized validator", description = "Add a new authorized validator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validator added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Validator.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Validator already exists")
    })
    public ResponseEntity<Validator> addValidator(@RequestBody AddValidatorRequest request) {
        try {
            logger.info("Adding new validator: {}", request.getValidatorName());

            Validator validator = validatorService.addAuthorizedValidator(
                    request.getValidatorName(),
                    request.getPublicKey(),
                    request.getPriority(),
                    request.getNodeUrl());

            return ResponseEntity.ok(validator);

        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add validator: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Failed to add validator: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update validator priority
     */
    @PutMapping("/validators/{validatorId}/priority")
    @Operation(summary = "Update validator priority", description = "Update validator priority")
    public ResponseEntity<Void> updateValidatorPriority(
            @Parameter(description = "Validator ID", required = true) @PathVariable UUID validatorId,
            @Parameter(description = "New priority", required = true) @RequestParam Integer priority) {
        try {
            validatorService.updateValidatorPriority(validatorId, priority);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update validator priority: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to update validator priority: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activate validator
     */
    @PutMapping("/validators/{validatorId}/activate")
    @Operation(summary = "Activate validator", description = "Activate a validator")
    public ResponseEntity<Void> activateValidator(
            @Parameter(description = "Validator ID", required = true) @PathVariable UUID validatorId) {
        try {
            validatorService.activateValidator(validatorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to activate validator: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to activate validator: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deactivate validator
     */
    @PutMapping("/validators/{validatorId}/deactivate")
    @Operation(summary = "Deactivate validator", description = "Deactivate a validator")
    public ResponseEntity<Void> deactivateValidator(
            @Parameter(description = "Validator ID", required = true) @PathVariable UUID validatorId) {
        try {
            validatorService.deactivateValidator(validatorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to deactivate validator: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to deactivate validator: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Revoke validator authority
     */
    @DeleteMapping("/validators/{validatorId}")
    @Operation(summary = "Revoke validator authority", description = "Revoke validator authority")
    public ResponseEntity<Void> revokeValidatorAuthority(
            @Parameter(description = "Validator ID", required = true) @PathVariable UUID validatorId) {
        try {
            validatorService.revokeValidatorAuthority(validatorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to revoke validator authority: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to revoke validator authority: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update validator heartbeat
     */
    @PutMapping("/validators/{validatorId}/heartbeat")
    @Operation(summary = "Update validator heartbeat", description = "Update validator heartbeat")
    public ResponseEntity<Void> updateHeartbeat(
            @Parameter(description = "Validator ID", required = true) @PathVariable UUID validatorId) {
        try {
            validatorService.updateValidatorHeartbeat(validatorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update heartbeat: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to update heartbeat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get validator statistics
     */
    @GetMapping("/validators/statistics")
    @Operation(summary = "Get validator statistics", description = "Get validator management statistics")
    public ResponseEntity<ValidatorManagementService.ValidatorStatistics> getValidatorStatistics() {
        try {
            ValidatorManagementService.ValidatorStatistics stats = validatorService.getValidatorStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to get validator statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add Validator Request DTO
     */
    public static class AddValidatorRequest {
        private String validatorName;
        private String publicKey;
        private Integer priority;
        private String nodeUrl;

        // Getters and Setters
        public String getValidatorName() {
            return validatorName;
        }

        public void setValidatorName(String validatorName) {
            this.validatorName = validatorName;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public String getNodeUrl() {
            return nodeUrl;
        }

        public void setNodeUrl(String nodeUrl) {
            this.nodeUrl = nodeUrl;
        }
    }
}
