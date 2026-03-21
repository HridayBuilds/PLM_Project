package com.odoo.plm.repository;

import com.odoo.plm.entity.ProductAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductAttachmentRepository extends JpaRepository<ProductAttachment, UUID> {

    List<ProductAttachment> findByProductId(UUID productId);

    void deleteByProductId(UUID productId);

    int countByProductId(UUID productId);
}
