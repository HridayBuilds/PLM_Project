package com.odoo.plm.repository;

import com.odoo.plm.entity.BomAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BomAttachmentRepository extends JpaRepository<BomAttachment, UUID> {
    List<BomAttachment> findByBomId(UUID bomId);
    void deleteByBomId(UUID bomId);
}
