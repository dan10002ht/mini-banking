package com.minibanking.repository;

import com.minibanking.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Session entities
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    
    /**
     * Find session by token hash
     */
    Optional<Session> findByTokenHash(String tokenHash);
    
    /**
     * Find session by refresh token hash
     */
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);
    
    /**
     * Find active sessions for a user
     */
    @Query("SELECT s FROM Session s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :now")
    List<Session> findActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    /**
     * Find session by user ID and device fingerprint
     */
    @Query("SELECT s FROM Session s WHERE s.userId = :userId AND s.deviceFingerprint = :deviceFingerprint AND s.isActive = true AND s.expiresAt > :now")
    Optional<Session> findByUserIdAndDeviceFingerprint(@Param("userId") UUID userId, @Param("deviceFingerprint") String deviceFingerprint, @Param("now") LocalDateTime now);
    
    /**
     * Find temporary sessions
     */
    @Query("SELECT s FROM Session s WHERE s.isTemporary = true AND s.isActive = true AND s.expiresAt > :now")
    List<Session> findActiveTemporarySessions(@Param("now") LocalDateTime now);
    
    /**
     * Find expired sessions
     */
    @Query("SELECT s FROM Session s WHERE s.expiresAt < :now")
    List<Session> findExpiredSessions(@Param("now") LocalDateTime now);
    
    /**
     * Find sessions by IP address
     */
    List<Session> findByIpAddress(String ipAddress);
    
    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :now")
    Long countActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    /**
     * Find sessions by user agent
     */
    @Query("SELECT s FROM Session s WHERE s.userAgent LIKE %:userAgent%")
    List<Session> findByUserAgentContaining(@Param("userAgent") String userAgent);
    
    /**
     * Find sessions created after a specific time
     */
    @Query("SELECT s FROM Session s WHERE s.createdAt > :afterTime")
    List<Session> findSessionsCreatedAfter(@Param("afterTime") LocalDateTime afterTime);
    
    /**
     * Find sessions with no activity for specified time
     */
    @Query("SELECT s FROM Session s WHERE s.lastActivity < :cutoffTime AND s.isActive = true")
    List<Session> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Invalidate sessions by user ID
     */
    @Query("UPDATE Session s SET s.isActive = false WHERE s.userId = :userId")
    int invalidateSessionsByUserId(@Param("userId") UUID userId);
    
    /**
     * Invalidate sessions by user ID and device fingerprint
     */
    @Query("UPDATE Session s SET s.isActive = false WHERE s.userId = :userId AND s.deviceFingerprint = :deviceFingerprint")
    int invalidateSessionsByUserIdAndDevice(@Param("userId") UUID userId, @Param("deviceFingerprint") String deviceFingerprint);
    
    /**
     * Delete expired sessions
     */
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);
    
    /**
     * Delete inactive sessions
     */
    @Query("DELETE FROM Session s WHERE s.lastActivity < :cutoffTime AND s.isActive = false")
    int deleteInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}
