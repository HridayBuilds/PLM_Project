package com.ecotracker.service;

import com.ecotracker.dto.*;
import com.ecotracker.model.*;
import com.ecotracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final EcoTypeRepository ecoTypeRepository;
    private final EcoStageRepository ecoStageRepository;
    private final EcoTagRepository ecoTagRepository;
    private final UserRepository userRepository;

    // ==================== ECO Types ====================

    public EcoTypeDTO createEcoType(EcoTypeCreateDTO dto) {
        EcoType type = new EcoType();
        type.setName(dto.getName());
        type.setDescription(dto.getDescription());
        type.setPrefix(dto.getPrefix());
        type.setActive(true);

        type = ecoTypeRepository.save(type);

        // Create default stages if provided
        if (dto.getStages() != null && !dto.getStages().isEmpty()) {
            int sequence = 1;
            for (StageCreateDTO stageDto : dto.getStages()) {
                createStageForType(type, stageDto, sequence++);
            }
        }

        log.info("Created ECO Type: {}", type.getName());
        return mapTypeToDTO(type);
    }

    public EcoTypeDTO updateEcoType(Long id, EcoTypeUpdateDTO dto) {
        EcoType type = ecoTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ECO Type not found"));

        if (dto.getName() != null) type.setName(dto.getName());
        if (dto.getDescription() != null) type.setDescription(dto.getDescription());
        if (dto.getPrefix() != null) type.setPrefix(dto.getPrefix());
        if (dto.getActive() != null) type.setActive(dto.getActive());

        type = ecoTypeRepository.save(type);
        return mapTypeToDTO(type);
    }

    public void deleteEcoType(Long id) {
        EcoType type = ecoTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ECO Type not found"));

        // Soft delete - just deactivate
        type.setActive(false);
        ecoTypeRepository.save(type);
        log.info("Deactivated ECO Type: {}", type.getName());
    }

    @Transactional(readOnly = true)
    public List<EcoTypeDTO> getAllEcoTypes(boolean activeOnly) {
        List<EcoType> types;
        if (activeOnly) {
            types = ecoTypeRepository.findByActiveTrue();
        } else {
            types = ecoTypeRepository.findAll();
        }
        return types.stream().map(this::mapTypeToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EcoTypeDTO getEcoType(Long id) {
        EcoType type = ecoTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ECO Type not found"));
        return mapTypeToDTO(type);
    }

    // ==================== ECO Stages ====================

    public EcoStageDTO createStage(StageCreateDTO dto) {
        EcoType type = ecoTypeRepository.findById(dto.getEcoTypeId())
                .orElseThrow(() -> new RuntimeException("ECO Type not found"));

        // Get max sequence for this type
        int maxSequence = ecoStageRepository.findMaxSequenceByTypeId(type.getId()).orElse(0);

        return mapStageToDTO(createStageForType(type, dto, maxSequence + 1));
    }

    private EcoStage createStageForType(EcoType type, StageCreateDTO dto, int sequence) {
        EcoStage stage = new EcoStage();
        stage.setName(dto.getName());
        stage.setDescription(dto.getDescription());
        stage.setSequence(dto.getSequence() != null ? dto.getSequence() : sequence);
        stage.setEcoType(type);
        stage.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : false);

        // Set approvers
        if (dto.getApproverIds() != null && !dto.getApproverIds().isEmpty()) {
            Set<User> approvers = new HashSet<>(userRepository.findAllById(dto.getApproverIds()));
            stage.setApprovers(approvers);
        }

        stage = ecoStageRepository.save(stage);
        log.info("Created stage {} for type {}", stage.getName(), type.getName());
        return stage;
    }

    public EcoStageDTO updateStage(Long id, StageUpdateDTO dto) {
        EcoStage stage = ecoStageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stage not found"));

        if (dto.getName() != null) stage.setName(dto.getName());
        if (dto.getDescription() != null) stage.setDescription(dto.getDescription());
        if (dto.getSequence() != null) stage.setSequence(dto.getSequence());
        if (dto.getRequiresApproval() != null) stage.setRequiresApproval(dto.getRequiresApproval());

        if (dto.getApproverIds() != null) {
            Set<User> approvers = new HashSet<>(userRepository.findAllById(dto.getApproverIds()));
            stage.setApprovers(approvers);
        }

        stage = ecoStageRepository.save(stage);
        return mapStageToDTO(stage);
    }

    public void deleteStage(Long id) {
        EcoStage stage = ecoStageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stage not found"));
        ecoStageRepository.delete(stage);
        log.info("Deleted stage: {}", stage.getName());
    }

    @Transactional(readOnly = true)
    public List<EcoStageDTO> getStagesByType(Long typeId) {
        List<EcoStage> stages = ecoStageRepository.findByEcoTypeIdOrderBySequenceAsc(typeId);
        return stages.stream().map(this::mapStageToDTO).collect(Collectors.toList());
    }

    public void reorderStages(Long typeId, List<Long> stageIds) {
        int sequence = 1;
        for (Long stageId : stageIds) {
            EcoStage stage = ecoStageRepository.findById(stageId)
                    .orElseThrow(() -> new RuntimeException("Stage not found: " + stageId));

            if (!stage.getEcoType().getId().equals(typeId)) {
                throw new RuntimeException("Stage does not belong to specified type");
            }

            stage.setSequence(sequence++);
            ecoStageRepository.save(stage);
        }
        log.info("Reordered stages for type {}", typeId);
    }

    // ==================== ECO Tags ====================

    public EcoTagDTO createTag(TagCreateDTO dto) {
        EcoTag tag = new EcoTag();
        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#6366f1");
        tag.setDescription(dto.getDescription());

        tag = ecoTagRepository.save(tag);
        log.info("Created tag: {}", tag.getName());
        return mapTagToDTO(tag);
    }

    public EcoTagDTO updateTag(Long id, TagUpdateDTO dto) {
        EcoTag tag = ecoTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        if (dto.getName() != null) tag.setName(dto.getName());
        if (dto.getColor() != null) tag.setColor(dto.getColor());
        if (dto.getDescription() != null) tag.setDescription(dto.getDescription());

        tag = ecoTagRepository.save(tag);
        return mapTagToDTO(tag);
    }

    public void deleteTag(Long id) {
        ecoTagRepository.deleteById(id);
        log.info("Deleted tag: {}", id);
    }

    @Transactional(readOnly = true)
    public List<EcoTagDTO> getAllTags() {
        return ecoTagRepository.findAll().stream()
                .map(this::mapTagToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Mapping Methods ====================

    private EcoTypeDTO mapTypeToDTO(EcoType type) {
        EcoTypeDTO dto = new EcoTypeDTO();
        dto.setId(type.getId());
        dto.setName(type.getName());
        dto.setDescription(type.getDescription());
        dto.setPrefix(type.getPrefix());
        dto.setActive(type.isActive());

        if (type.getStages() != null) {
            dto.setStages(type.getStages().stream()
                    .map(this::mapStageToDTO)
                    .sorted((a, b) -> a.getSequence() - b.getSequence())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private EcoStageDTO mapStageToDTO(EcoStage stage) {
        EcoStageDTO dto = new EcoStageDTO();
        dto.setId(stage.getId());
        dto.setName(stage.getName());
        dto.setDescription(stage.getDescription());
        dto.setSequence(stage.getSequence());
        dto.setRequiresApproval(stage.isRequiresApproval());

        if (stage.getApprovers() != null) {
            dto.setApprovers(stage.getApprovers().stream()
                    .map(user -> {
                        UserDTO userDTO = new UserDTO();
                        userDTO.setId(user.getId());
                        userDTO.setFirstName(user.getFirstName());
                        userDTO.setLastName(user.getLastName());
                        userDTO.setEmail(user.getEmail());
                        return userDTO;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private EcoTagDTO mapTagToDTO(EcoTag tag) {
        EcoTagDTO dto = new EcoTagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setDescription(tag.getDescription());
        return dto;
    }
}
