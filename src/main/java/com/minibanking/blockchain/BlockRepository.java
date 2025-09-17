package com.minibanking.blockchain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Block entities
 */
@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {
    
    /**
     * Find the latest block by block number
     */
    @Query("SELECT b FROM Block b ORDER BY b.blockNumber DESC")
    Optional<Block> findLatestBlock();
    
    /**
     * Find block by block number
     */
    Optional<Block> findByBlockNumber(Long blockNumber);
    
    /**
     * Find block by block hash
     */
    Optional<Block> findByBlockHash(String blockHash);
    
    /**
     * Find blocks by status
     */
    List<Block> findByStatus(Block.BlockStatus status);
    
    /**
     * Find blocks in range
     */
    @Query("SELECT b FROM Block b WHERE b.blockNumber BETWEEN :startBlock AND :endBlock ORDER BY b.blockNumber")
    List<Block> findBlocksInRange(@Param("startBlock") Long startBlock, @Param("endBlock") Long endBlock);
    
    /**
     * Count total blocks
     */
    @Query("SELECT COUNT(b) FROM Block b")
    Long countTotalBlocks();
    
    /**
     * Find blocks with pending status
     */
    @Query("SELECT b FROM Block b WHERE b.status = 'PENDING' ORDER BY b.timestamp")
    List<Block> findPendingBlocks();
    
    /**
     * Find the last mined block
     */
    @Query("SELECT b FROM Block b WHERE b.status = 'MINED' ORDER BY b.blockNumber DESC")
    Optional<Block> findLastMinedBlock();
}
