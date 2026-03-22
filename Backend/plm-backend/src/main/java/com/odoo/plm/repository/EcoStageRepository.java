package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EcoStageRepository extends JpaRepository<EcoStage, UUID> {

    List<EcoStage> findAllByProductIdOrderBySequenceAsc(UUID productId);

    Optional<EcoStage> findByProductIdAndSequence(UUID productId, Integer sequence);

    @Query("SELECT s FROM EcoStage s WHERE s.product.id = :productId AND s.isFinal = true ORDER BY s.sequence DESC LIMIT 1")
    Optional<EcoStage> findByProductIdAndIsFinalTrue(@Param("productId") UUID productId);

    Optional<EcoStage> findByProductIdAndName(UUID productId, String name);

    boolean existsByProductIdAndName(UUID productId, String name);

    boolean existsByProductIdAndSequence(UUID productId, Integer sequence);

    Optional<EcoStage> findFirstByProductIdOrderBySequenceAsc(UUID productId);

    Optional<EcoStage> findFirstByProductIdAndSequenceGreaterThanOrderBySequenceAsc(UUID productId, Integer currentSequence);

    Optional<EcoStage> findFirstByProductIdAndSequenceLessThanOrderBySequenceDesc(UUID productId, Integer currentSequence);

    @Query("SELECT MAX(s.sequence) FROM EcoStage s WHERE s.product.id = :productId")
    Integer findMaxSequenceByProductId(@Param("productId") UUID productId);

    // Global stages (product_id is NULL) - apply to all products
    @Query("SELECT s FROM EcoStage s WHERE s.product IS NULL ORDER BY s.sequence ASC")
    List<EcoStage> findAllGlobalStagesOrderBySequenceAsc();

    @Query("SELECT s FROM EcoStage s WHERE s.product IS NULL ORDER BY s.sequence ASC LIMIT 1")
    Optional<EcoStage> findFirstGlobalStageOrderBySequenceAsc();

    @Query("SELECT s FROM EcoStage s WHERE s.product IS NULL AND s.isFinal = true ORDER BY s.sequence DESC LIMIT 1")
    Optional<EcoStage> findGlobalFinalStage();

    @Query("SELECT s FROM EcoStage s WHERE s.product IS NULL AND s.sequence > :currentSequence ORDER BY s.sequence ASC LIMIT 1")
    Optional<EcoStage> findNextGlobalStage(@Param("currentSequence") Integer currentSequence);
}
