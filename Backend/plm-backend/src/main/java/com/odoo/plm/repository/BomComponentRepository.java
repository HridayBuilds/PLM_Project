package com.odoo.plm.repository;

import com.odoo.plm.entity.BomComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BomComponentRepository extends JpaRepository<BomComponent, UUID> {

    List<BomComponent> findByBomId(UUID bomId);

    void deleteByBomId(UUID bomId);

    int countByBomId(UUID bomId);

    // Find by component product
    List<BomComponent> findByComponentProductId(UUID componentProductId);

    // Check if a product is used as a component in any BOM
    boolean existsByComponentProductId(UUID productId);

    // Find all BOMs that use a specific product as component
    @Query("SELECT bc.bom.id FROM BomComponent bc WHERE bc.componentProduct.id = :productId")
    List<UUID> findBomIdsByComponentProductId(@Param("productId") UUID productId);
}
