package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoBomOperationChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EcoBomOperationChangeRepository extends JpaRepository<EcoBomOperationChange, UUID> {
    
    @Query("SELECT c FROM EcoBomOperationChange c LEFT JOIN FETCH c.bomOperation op WHERE c.eco.id = :ecoId")
    List<EcoBomOperationChange> findByEcoIdWithDetails(@Param("ecoId") UUID ecoId);

    int countByEcoId(UUID ecoId);
}
