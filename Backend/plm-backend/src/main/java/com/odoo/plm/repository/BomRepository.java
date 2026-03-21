package com.odoo.plm.repository;

import com.odoo.plm.entity.Bom;
import com.odoo.plm.enums.BomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BomRepository extends JpaRepository<Bom, UUID>, JpaSpecificationExecutor<Bom> {

    // Find by product
    List<Bom> findByProductId(UUID productId);

    Page<Bom> findByProductId(UUID productId, Pageable pageable);

    // Find by status
    Page<Bom> findByStatus(BomStatus status, Pageable pageable);

    List<Bom> findByStatus(BomStatus status);

    // Find active BOM for a product
    @Query("SELECT b FROM Bom b WHERE b.product.id = :productId AND b.status = 'ACTIVE' ORDER BY b.version DESC")
    Optional<Bom> findActiveByProductId(@Param("productId") UUID productId);

    // Find by reference
    Optional<Bom> findByReference(String reference);

    Page<Bom> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);

    // Find all versions of a BOM for a product
    @Query("SELECT b FROM Bom b WHERE b.product.id = :productId ORDER BY b.version DESC")
    List<Bom> findAllVersionsByProductId(@Param("productId") UUID productId);

    // Find by product and version
    Optional<Bom> findByProductIdAndVersion(UUID productId, Integer version);

    // Get max version for a product's BOM
    @Query("SELECT MAX(b.version) FROM Bom b WHERE b.product.id = :productId")
    Integer findMaxVersionByProductId(@Param("productId") UUID productId);

    // Search BOMs with filters
    @Query("SELECT b FROM Bom b WHERE " +
            "(:reference IS NULL OR LOWER(b.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:productId IS NULL OR b.product.id = :productId)")
    Page<Bom> searchBoms(
            @Param("reference") String reference,
            @Param("status") BomStatus status,
            @Param("productId") UUID productId,
            Pageable pageable
    );

    // Check reference exists
    boolean existsByReference(String reference);

    // Find BOMs created by user
    Page<Bom> findByCreatedById(UUID userId, Pageable pageable);
}
