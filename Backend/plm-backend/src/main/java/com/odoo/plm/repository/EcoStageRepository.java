package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EcoStageRepository extends JpaRepository<EcoStage, UUID> {

    // Find all stages ordered by sequence
    List<EcoStage> findAllByOrderBySequenceAsc();

    // Find by sequence
    Optional<EcoStage> findBySequence(Integer sequence);

    // Find the final stage
    Optional<EcoStage> findByIsFinalTrue();

    // Find by name
    Optional<EcoStage> findByName(String name);

    // Check if name exists
    boolean existsByName(String name);

    // Check if sequence exists
    boolean existsBySequence(Integer sequence);

    // Get the first stage (lowest sequence)
    @Query("SELECT s FROM EcoStage s ORDER BY s.sequence ASC LIMIT 1")
    Optional<EcoStage> findFirstStage();

    // Get next stage after current
    @Query("SELECT s FROM EcoStage s WHERE s.sequence > :currentSequence ORDER BY s.sequence ASC LIMIT 1")
    Optional<EcoStage> findNextStage(Integer currentSequence);

    // Get previous stage
    @Query("SELECT s FROM EcoStage s WHERE s.sequence < :currentSequence ORDER BY s.sequence DESC LIMIT 1")
    Optional<EcoStage> findPreviousStage(Integer currentSequence);

    // Get max sequence
    @Query("SELECT MAX(s.sequence) FROM EcoStage s")
    Integer findMaxSequence();

    // Count total stages
    long count();
}
