package com.odoo.plm.repository;

import com.odoo.plm.entity.BomOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BomOperationRepository extends JpaRepository<BomOperation, UUID> {

    List<BomOperation> findByBomId(UUID bomId);

    List<BomOperation> findByBomIdOrderBySequenceAsc(UUID bomId);

    void deleteByBomId(UUID bomId);

    int countByBomId(UUID bomId);

    // Find operations by work center
    List<BomOperation> findByWorkCenterContainingIgnoreCase(String workCenter);
}
