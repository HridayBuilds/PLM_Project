package com.odoo.plm.repository;

import com.odoo.plm.entity.Eco;
import com.odoo.plm.enums.EcoStatus;
import com.odoo.plm.enums.EcoType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EcoRepository extends JpaRepository<Eco, UUID>, JpaSpecificationExecutor<Eco> {

    // Find by status
    Page<Eco> findByStatus(EcoStatus status, Pageable pageable);

    List<Eco> findByStatus(EcoStatus status);

    // Find by type
    Page<Eco> findByEcoType(EcoType type, Pageable pageable);

    // Find by creator
    Page<Eco> findByCreatedById(UUID userId, Pageable pageable);

    // Find by product
    Page<Eco> findByProductId(UUID productId, Pageable pageable);

    List<Eco> findByProductId(UUID productId);

    // Find by BOM
    Page<Eco> findByBomId(UUID bomId, Pageable pageable);

    List<Eco> findByBomId(UUID bomId);

    // Find by current stage
    Page<Eco> findByCurrentStageId(UUID stageId, Pageable pageable);

    // Find ECOs pending approval at a specific stage
    @Query("SELECT e FROM Eco e WHERE e.currentStage.id = :stageId AND e.status = 'IN_PROGRESS'")
    Page<Eco> findPendingByStageId(@Param("stageId") UUID stageId, Pageable pageable);

    // Find ECOs pending approval for a specific approver
    @Query("SELECT DISTINCT e FROM Eco e " +
            "JOIN EcoApprovalRule r ON r.ecoStage.id = e.currentStage.id " +
            "WHERE r.approverUser.id = :approverId AND e.status = 'IN_PROGRESS'")
    Page<Eco> findPendingForApprover(@Param("approverId") UUID approverId, Pageable pageable);

    // Search ECOs with filters
    @Query("SELECT e FROM Eco e WHERE " +
            "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:type IS NULL OR e.ecoType = :type) AND " +
            "(:productId IS NULL OR e.product.id = :productId)")
    Page<Eco> searchEcos(
            @Param("title") String title,
            @Param("status") EcoStatus status,
            @Param("type") EcoType type,
            @Param("productId") UUID productId,
            Pageable pageable
    );

    // Find ECOs by date range
    Page<Eco> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find applied ECOs for a product (for history)
    @Query("SELECT e FROM Eco e WHERE e.product.id = :productId AND e.status = 'APPLIED' ORDER BY e.effectiveDate DESC")
    List<Eco> findAppliedByProductId(@Param("productId") UUID productId);

    // Count ECOs by status
    long countByStatus(EcoStatus status);

    // Count ECOs by type
    long countByEcoType(EcoType type);

    // Find ECOs not created by a specific user (for approval - can't approve own ECO)
    @Query("SELECT e FROM Eco e WHERE e.currentStage.id = :stageId AND e.status = 'IN_PROGRESS' AND e.createdBy.id != :userId")
    Page<Eco> findPendingByStageIdExcludingCreator(
            @Param("stageId") UUID stageId,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    // ============ JOIN FETCH queries to avoid lazy loading ============

    @Query("SELECT e FROM Eco e " +
            "LEFT JOIN FETCH e.product " +
            "LEFT JOIN FETCH e.bom " +
            "LEFT JOIN FETCH e.createdBy " +
            "LEFT JOIN FETCH e.currentStage " +
            "WHERE e.id = :id")
    Optional<Eco> findByIdWithDetails(@Param("id") UUID id);

    @Query(value = "SELECT e FROM Eco e " +
            "LEFT JOIN FETCH e.product " +
            "LEFT JOIN FETCH e.bom " +
            "LEFT JOIN FETCH e.createdBy " +
            "LEFT JOIN FETCH e.currentStage " +
            "WHERE e.createdBy.id = :userId",
            countQuery = "SELECT COUNT(e) FROM Eco e WHERE e.createdBy.id = :userId")
    Page<Eco> findByCreatedByIdWithDetails(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = "SELECT DISTINCT e FROM Eco e " +
            "LEFT JOIN FETCH e.product " +
            "LEFT JOIN FETCH e.bom " +
            "LEFT JOIN FETCH e.createdBy " +
            "LEFT JOIN FETCH e.currentStage " +
            "JOIN EcoApprovalRule r ON r.ecoStage.id = e.currentStage.id " +
            "WHERE r.approverUser.id = :approverId AND e.status = 'IN_PROGRESS'",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Eco e " +
                    "JOIN EcoApprovalRule r ON r.ecoStage.id = e.currentStage.id " +
                    "WHERE r.approverUser.id = :approverId AND e.status = 'IN_PROGRESS'")
    Page<Eco> findPendingForApproverWithDetails(@Param("approverId") UUID approverId, Pageable pageable);

    @Query(value = "SELECT e FROM Eco e " +
            "LEFT JOIN FETCH e.product " +
            "LEFT JOIN FETCH e.bom " +
            "LEFT JOIN FETCH e.createdBy " +
            "LEFT JOIN FETCH e.currentStage " +
            "WHERE (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:type IS NULL OR e.ecoType = :type) AND " +
            "(:productId IS NULL OR e.product.id = :productId)",
            countQuery = "SELECT COUNT(e) FROM Eco e WHERE " +
                    "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
                    "(:status IS NULL OR e.status = :status) AND " +
                    "(:type IS NULL OR e.ecoType = :type) AND " +
                    "(:productId IS NULL OR e.product.id = :productId)")
    Page<Eco> searchEcosWithDetails(
            @Param("title") String title,
            @Param("status") EcoStatus status,
            @Param("type") EcoType type,
            @Param("productId") UUID productId,
            Pageable pageable
    );

    // Get all ECOs with details (for admin/general listing)
    @Query(value = "SELECT e FROM Eco e " +
            "LEFT JOIN FETCH e.product " +
            "LEFT JOIN FETCH e.bom " +
            "LEFT JOIN FETCH e.createdBy " +
            "LEFT JOIN FETCH e.currentStage",
            countQuery = "SELECT COUNT(e) FROM Eco e")
    Page<Eco> findAllWithDetails(Pageable pageable);
}
