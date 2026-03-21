package com.odoo.plm.repository;

import com.odoo.plm.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    // Find by ECO
    List<AuditLog> findByEcoId(UUID ecoId);

    List<AuditLog> findByEcoIdOrderByTimestampDesc(UUID ecoId);

    // Find by actor
    Page<AuditLog> findByActorUserId(UUID userId, Pageable pageable);

    // Find by action
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find by timestamp range
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find by affected record
    Page<AuditLog> findByAffectedRecordContaining(String recordIdentifier, Pageable pageable);

    // Search audit logs
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:actorId IS NULL OR a.actorUser.id = :actorId) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> searchAuditLogs(
            @Param("action") String action,
            @Param("actorId") UUID actorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Get recent activity
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecentActivity(Pageable pageable);

    // Get activity summary by action type
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action")
    List<Object[]> countByAction();
}
