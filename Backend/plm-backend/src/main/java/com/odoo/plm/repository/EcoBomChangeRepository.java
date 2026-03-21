package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoBomChange;
import com.odoo.plm.enums.ChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EcoBomChangeRepository extends JpaRepository<EcoBomChange, UUID> {

    // Find changes for an ECO
    List<EcoBomChange> findByEcoId(UUID ecoId);

    // Find by change type
    List<EcoBomChange> findByEcoIdAndChangeType(UUID ecoId, ChangeType changeType);

    // Delete all changes for an ECO
    void deleteByEcoId(UUID ecoId);

    // Count changes for an ECO
    int countByEcoId(UUID ecoId);

    // Check if component is changed in ECO
    boolean existsByEcoIdAndBomComponentId(UUID ecoId, UUID bomComponentId);

    // Find change for specific component
    List<EcoBomChange> findByEcoIdAndBomComponentId(UUID ecoId, UUID bomComponentId);
}
