package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoProductChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EcoProductChangeRepository extends JpaRepository<EcoProductChange, UUID> {

    // Find changes for an ECO
    List<EcoProductChange> findByEcoId(UUID ecoId);

    // Find by field name
    Optional<EcoProductChange> findByEcoIdAndFieldName(UUID ecoId, String fieldName);

    // Delete all changes for an ECO
    void deleteByEcoId(UUID ecoId);

    // Count changes for an ECO
    int countByEcoId(UUID ecoId);

    // Check if field is changed in ECO
    boolean existsByEcoIdAndFieldName(UUID ecoId, String fieldName);
}
