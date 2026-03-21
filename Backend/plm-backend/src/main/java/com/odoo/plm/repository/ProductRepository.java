package com.odoo.plm.repository;

import com.odoo.plm.entity.Product;
import com.odoo.plm.enums.ProductStatus;
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
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    // Find by status
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    List<Product> findByStatus(ProductStatus status);

    // Find active products
    Page<Product> findByStatusIn(List<ProductStatus> statuses, Pageable pageable);

    // Find by name (case insensitive search)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find active product by name (latest version)
    @Query("SELECT p FROM Product p WHERE p.name = :name AND p.status = 'ACTIVE' ORDER BY p.version DESC")
    Optional<Product> findActiveByName(@Param("name") String name);

    // Find all versions of a product by name
    @Query("SELECT p FROM Product p WHERE p.name = :name ORDER BY p.version DESC")
    List<Product> findAllVersionsByName(@Param("name") String name);

    // Find all versions of a product by name with pagination
    @Query("SELECT p FROM Product p WHERE p.name = :name ORDER BY p.version DESC")
    Page<Product> findAllVersionsByName(@Param("name") String name, Pageable pageable);

    // Check if product name exists in ACTIVE status
    boolean existsByNameAndStatus(String name, ProductStatus status);

    // Find latest version of a product
    @Query("SELECT p FROM Product p WHERE p.name = :name ORDER BY p.version DESC LIMIT 1")
    Optional<Product> findLatestVersionByName(@Param("name") String name);

    // Find by name and version
    Optional<Product> findByNameAndVersion(String name, Integer version);

    // Search products with multiple filters
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    // Get max version for a product name
    @Query("SELECT MAX(p.version) FROM Product p WHERE p.name = :name")
    Integer findMaxVersionByName(@Param("name") String name);

    // Find products created by user
    Page<Product> findByCreatedById(UUID userId, Pageable pageable);
}
