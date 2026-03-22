package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EcoAttachmentRepository extends JpaRepository<EcoAttachment, UUID> {
    List<EcoAttachment> findByEcoId(UUID ecoId);
}
