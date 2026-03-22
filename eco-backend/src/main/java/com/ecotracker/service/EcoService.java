package com.ecotracker.service;

import com.ecotracker.dto.*;
import com.ecotracker.model.*;
import com.ecotracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EcoService {

    private final EcoRepository ecoRepository;
    private final EcoStageRepository ecoStageRepository;
    private final EcoTypeRepository ecoTypeRepository;
    private final EcoApprovalRepository ecoApprovalRepository;
    private final EcoTagRepository ecoTagRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BomRepository bomRepository;
    private final BomLineRepository bomLineRepository;
    private final EcoStageHistoryRepository stageHistoryRepository;
    private final NotificationService notificationService;
    private final FileService fileService;

    /**
     * Create a new ECO
     */
    public EcoResponseDTO createEco(EcoCreateDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EcoType type = ecoTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new RuntimeException("ECO Type not found"));

        // Get the first stage for this type
        EcoStage initialStage = ecoStageRepository
                .findByEcoTypeIdOrderBySequenceAsc(type.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No stages defined for ECO type"));

        // Generate reference number
        String reference = generateReference(type);

        Eco eco = new Eco();
        eco.setReference(reference);
        eco.setName(dto.getName());
        eco.setDescription(dto.getDescription());
        eco.setType(type);
        eco.setCurrentStage(initialStage);
        eco.setCreatedBy(user);
        eco.setAssignedTo(user);
        eco.setPriority(dto.getPriority() != null ? dto.getPriority() : Eco.Priority.NORMAL);
        eco.setEffectivity(dto.getEffectivity() != null ? dto.getEffectivity() : Eco.Effectivity.AS_SOON_AS_POSSIBLE);
        eco.setEffectiveDate(dto.getEffectiveDate());
        eco.setState(Eco.EcoState.IN_PROGRESS);

        // Add tags
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<EcoTag> tags = new HashSet<>(ecoTagRepository.findAllById(dto.getTagIds()));
            eco.setTags(tags);
        }

        eco = ecoRepository.save(eco);

        // Create stage history entry
        createStageHistory(eco, null, initialStage, user, "ECO Created");

        // Create approvals for this stage
        createApprovalsForStage(eco, initialStage);

        log.info("Created ECO: {} by user: {}", reference, user.getEmail());

        return mapToResponseDTO(eco);
    }

    /**
     * Get ECO by ID
     */
    @Transactional(readOnly = true)
    public EcoResponseDTO getEcoById(Long id) {
        Eco eco = ecoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ECO not found with id: " + id));
        return mapToResponseDTO(eco);
    }

    /**
     * List ECOs with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<EcoResponseDTO> listEcos(EcoFilterDTO filter, Pageable pageable) {
        Page<Eco> ecos;

        if (filter != null && hasFilters(filter)) {
            ecos = ecoRepository.findWithFilters(
                    filter.getTypeId(),
                    filter.getStageId(),
                    filter.getState(),
                    filter.getAssignedToId(),
                    filter.getCreatedById(),
                    filter.getPriority(),
                    filter.getSearchTerm(),
                    pageable
            );
        } else {
            ecos = ecoRepository.findAll(pageable);
        }

        return ecos.map(this::mapToResponseDTO);
    }

    /**
     * Update ECO
     */
    public EcoResponseDTO updateEco(Long id, EcoUpdateDTO dto, Long userId) {
        Eco eco = ecoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ECO not found"));

        if (eco.getState() == Eco.EcoState.DONE) {
            throw new RuntimeException("Cannot update a completed ECO");
        }

        if (dto.getName() != null) eco.setName(dto.getName());
        if (dto.getDescription() != null) eco.setDescription(dto.getDescription());
        if (dto.getPriority() != null) eco.setPriority(dto.getPriority());
        if (dto.getEffectivity() != null) eco.setEffectivity(dto.getEffectivity());
        if (dto.getEffectiveDate() != null) eco.setEffectiveDate(dto.getEffectiveDate());

        if (dto.getAssignedToId() != null) {
            User assignee = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            eco.setAssignedTo(assignee);
        }

        if (dto.getTagIds() != null) {
            Set<EcoTag> tags = new HashSet<>(ecoTagRepository.findAllById(dto.getTagIds()));
            eco.setTags(tags);
        }

        eco = ecoRepository.save(eco);
        log.info("Updated ECO: {} by user: {}", eco.getReference(), userId);

        return mapToResponseDTO(eco);
    }

    /**
     * Approve current stage
     */
    public EcoResponseDTO approveStage(Long ecoId, ApprovalDTO dto, Long userId) {
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new RuntimeException("ECO not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find pending approval for this user at current stage
        EcoApproval approval = ecoApprovalRepository
                .findByEcoIdAndStageIdAndApproverId(ecoId, eco.getCurrentStage().getId(), userId)
                .orElseThrow(() -> new RuntimeException("No pending approval found for this user"));

        if (approval.getStatus() != EcoApproval.ApprovalStatus.PENDING) {
            throw new RuntimeException("Approval already processed");
        }

        approval.setStatus(EcoApproval.ApprovalStatus.APPROVED);
        approval.setComment(dto.getComment());
        approval.setApprovedAt(LocalDateTime.now());
        ecoApprovalRepository.save(approval);

        // Check if all approvals for current stage are approved
        boolean allApproved = checkAllApprovalsForStage(eco, eco.getCurrentStage());

        if (allApproved) {
            // Move to next stage
            moveToNextStage(eco, user);
        }

        eco = ecoRepository.findById(ecoId).get();
        return mapToResponseDTO(eco);
    }

    /**
     * Reject current stage
     */
    public EcoResponseDTO rejectStage(Long ecoId, ApprovalDTO dto, Long userId) {
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new RuntimeException("ECO not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EcoApproval approval = ecoApprovalRepository
                .findByEcoIdAndStageIdAndApproverId(ecoId, eco.getCurrentStage().getId(), userId)
                .orElseThrow(() -> new RuntimeException("No pending approval found"));

        approval.setStatus(EcoApproval.ApprovalStatus.REJECTED);
        approval.setComment(dto.getComment());
        approval.setApprovedAt(LocalDateTime.now());
        ecoApprovalRepository.save(approval);

        // Optionally move back to previous stage or require fixes
        createStageHistory(eco, eco.getCurrentStage(), eco.getCurrentStage(), user,
                "Stage rejected: " + dto.getComment());

        // Notify creator
        notificationService.notifyEcoRejected(eco, user, dto.getComment());

        return mapToResponseDTO(eco);
    }

    /**
     * Apply BOM changes
     */
    public EcoResponseDTO applyChanges(Long ecoId, Long userId) {
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new RuntimeException("ECO not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify all approvals are complete
        List<EcoApproval> pendingApprovals = ecoApprovalRepository.findByEcoIdAndStatus(
                ecoId, EcoApproval.ApprovalStatus.PENDING);

        if (!pendingApprovals.isEmpty()) {
            throw new RuntimeException("Cannot apply changes: pending approvals exist");
        }

        // Apply each BOM change
        for (EcoBomChange change : eco.getBomChanges()) {
            applyBomChange(change);
        }

        // Mark ECO as done
        eco.setState(Eco.EcoState.DONE);
        eco.setCompletedAt(LocalDateTime.now());
        eco = ecoRepository.save(eco);

        log.info("Applied changes for ECO: {} by user: {}", eco.getReference(), userId);

        // Notify stakeholders
        notificationService.notifyEcoCompleted(eco);

        return mapToResponseDTO(eco);
    }

    /**
     * Get ECOs assigned to user
     */
    @Transactional(readOnly = true)
    public List<EcoResponseDTO> getMyEcos(Long userId) {
        List<Eco> ecos = ecoRepository.findByAssignedToIdAndStateIn(
                userId,
                List.of(Eco.EcoState.IN_PROGRESS, Eco.EcoState.CHANGE_REQUEST)
        );
        return ecos.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Get ECOs pending approval by user
     */
    @Transactional(readOnly = true)
    public List<EcoResponseDTO> getPendingApprovals(Long userId) {
        List<EcoApproval> approvals = ecoApprovalRepository.findPendingByApproverId(userId);
        return approvals.stream()
                .map(a -> mapToResponseDTO(a.getEco()))
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private String generateReference(EcoType type) {
        String prefix = type.getPrefix() != null ? type.getPrefix() : "ECO";
        long count = ecoRepository.count() + 1;
        return String.format("%s-%06d", prefix, count);
    }

    private void createStageHistory(Eco eco, EcoStage fromStage, EcoStage toStage,
                                    User user, String comment) {
        EcoStageHistory history = new EcoStageHistory();
        history.setEco(eco);
        history.setFromStage(fromStage);
        history.setToStage(toStage);
        history.setChangedBy(user);
        history.setComment(comment);
        stageHistoryRepository.save(history);
    }

    private void createApprovalsForStage(Eco eco, EcoStage stage) {
        if (stage.getApprovers() != null) {
            for (User approver : stage.getApprovers()) {
                EcoApproval approval = new EcoApproval();
                approval.setEco(eco);
                approval.setStage(stage);
                approval.setApprover(approver);
                approval.setStatus(EcoApproval.ApprovalStatus.PENDING);
                approval.setRequired(true);
                ecoApprovalRepository.save(approval);

                // Notify approver
                notificationService.notifyApprovalRequired(eco, approver);
            }
        }
    }

    private boolean checkAllApprovalsForStage(Eco eco, EcoStage stage) {
        List<EcoApproval> approvals = ecoApprovalRepository
                .findByEcoIdAndStageId(eco.getId(), stage.getId());

        return approvals.stream()
                .filter(EcoApproval::isRequired)
                .allMatch(a -> a.getStatus() == EcoApproval.ApprovalStatus.APPROVED);
    }

    private void moveToNextStage(Eco eco, User user) {
        EcoStage currentStage = eco.getCurrentStage();

        // Find next stage
        List<EcoStage> stages = ecoStageRepository
                .findByEcoTypeIdOrderBySequenceAsc(eco.getType().getId());

        EcoStage nextStage = null;
        boolean foundCurrent = false;

        for (EcoStage stage : stages) {
            if (foundCurrent) {
                nextStage = stage;
                break;
            }
            if (stage.getId().equals(currentStage.getId())) {
                foundCurrent = true;
            }
        }

        if (nextStage != null) {
            eco.setCurrentStage(nextStage);
            ecoRepository.save(eco);

            createStageHistory(eco, currentStage, nextStage, user, "Stage approved, moving to next stage");
            createApprovalsForStage(eco, nextStage);

            log.info("ECO {} moved from {} to {}", eco.getReference(),
                    currentStage.getName(), nextStage.getName());
        } else {
            // No more stages - ECO is ready for final apply
            log.info("ECO {} completed all approval stages", eco.getReference());
        }
    }

    private void applyBomChange(EcoBomChange change) {
        Bom bom = change.getBom();

        switch (change.getChangeType()) {
            case ADD_COMPONENT:
                BomLine newLine = new BomLine();
                newLine.setBom(bom);
                newLine.setProduct(change.getNewProduct());
                newLine.setQuantity(change.getNewQuantity());
                newLine.setOperation(change.getNewOperation());
                bomLineRepository.save(newLine);
                break;

            case REMOVE_COMPONENT:
                if (change.getBomLine() != null) {
                    bomLineRepository.delete(change.getBomLine());
                }
                break;

            case UPDATE_COMPONENT:
                BomLine line = change.getBomLine();
                if (line != null) {
                    if (change.getNewProduct() != null) {
                        line.setProduct(change.getNewProduct());
                    }
                    if (change.getNewQuantity() != null) {
                        line.setQuantity(change.getNewQuantity());
                    }
                    if (change.getNewOperation() != null) {
                        line.setOperation(change.getNewOperation());
                    }
                    bomLineRepository.save(line);
                }
                break;

            case REPLACE_COMPONENT:
                BomLine oldLine = change.getBomLine();
                if (oldLine != null && change.getNewProduct() != null) {
                    oldLine.setProduct(change.getNewProduct());
                    if (change.getNewQuantity() != null) {
                        oldLine.setQuantity(change.getNewQuantity());
                    }
                    bomLineRepository.save(oldLine);
                }
                break;
        }

        // Update BOM version
        bom.setVersion(bom.getVersion() + 1);
        bomRepository.save(bom);
    }

    private boolean hasFilters(EcoFilterDTO filter) {
        return filter.getTypeId() != null ||
               filter.getStageId() != null ||
               filter.getState() != null ||
               filter.getAssignedToId() != null ||
               filter.getCreatedById() != null ||
               filter.getPriority() != null ||
               (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty());
    }

    private EcoResponseDTO mapToResponseDTO(Eco eco) {
        EcoResponseDTO dto = new EcoResponseDTO();
        dto.setId(eco.getId());
        dto.setReference(eco.getReference());
        dto.setName(eco.getName());
        dto.setDescription(eco.getDescription());
        dto.setState(eco.getState());
        dto.setPriority(eco.getPriority());
        dto.setEffectivity(eco.getEffectivity());
        dto.setEffectiveDate(eco.getEffectiveDate());
        dto.setCreatedAt(eco.getCreatedAt());
        dto.setUpdatedAt(eco.getUpdatedAt());
        dto.setCompletedAt(eco.getCompletedAt());

        // Map type
        if (eco.getType() != null) {
            EcoTypeDTO typeDTO = new EcoTypeDTO();
            typeDTO.setId(eco.getType().getId());
            typeDTO.setName(eco.getType().getName());
            typeDTO.setPrefix(eco.getType().getPrefix());
            dto.setType(typeDTO);
        }

        // Map current stage
        if (eco.getCurrentStage() != null) {
            EcoStageDTO stageDTO = new EcoStageDTO();
            stageDTO.setId(eco.getCurrentStage().getId());
            stageDTO.setName(eco.getCurrentStage().getName());
            stageDTO.setSequence(eco.getCurrentStage().getSequence());
            dto.setCurrentStage(stageDTO);
        }

        // Map created by
        if (eco.getCreatedBy() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(eco.getCreatedBy().getId());
            userDTO.setFirstName(eco.getCreatedBy().getFirstName());
            userDTO.setLastName(eco.getCreatedBy().getLastName());
            userDTO.setEmail(eco.getCreatedBy().getEmail());
            dto.setCreatedBy(userDTO);
        }

        // Map assigned to
        if (eco.getAssignedTo() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(eco.getAssignedTo().getId());
            userDTO.setFirstName(eco.getAssignedTo().getFirstName());
            userDTO.setLastName(eco.getAssignedTo().getLastName());
            userDTO.setEmail(eco.getAssignedTo().getEmail());
            dto.setAssignedTo(userDTO);
        }

        // Map tags
        if (eco.getTags() != null) {
            dto.setTags(eco.getTags().stream()
                    .map(tag -> {
                        EcoTagDTO tagDTO = new EcoTagDTO();
                        tagDTO.setId(tag.getId());
                        tagDTO.setName(tag.getName());
                        tagDTO.setColor(tag.getColor());
                        return tagDTO;
                    })
                    .collect(Collectors.toList()));
        }

        // Map approvals
        List<EcoApproval> approvals = ecoApprovalRepository.findByEcoId(eco.getId());
        dto.setApprovals(approvals.stream()
                .map(this::mapApprovalToDTO)
                .collect(Collectors.toList()));

        // Map BOM changes count
        dto.setBomChangesCount(eco.getBomChanges() != null ? eco.getBomChanges().size() : 0);

        // Map attachments count
        dto.setAttachmentsCount(eco.getAttachments() != null ? eco.getAttachments().size() : 0);

        return dto;
    }

    private ApprovalResponseDTO mapApprovalToDTO(EcoApproval approval) {
        ApprovalResponseDTO dto = new ApprovalResponseDTO();
        dto.setId(approval.getId());
        dto.setStatus(approval.getStatus());
        dto.setComment(approval.getComment());
        dto.setRequired(approval.isRequired());
        dto.setApprovedAt(approval.getApprovedAt());

        if (approval.getApprover() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(approval.getApprover().getId());
            userDTO.setFirstName(approval.getApprover().getFirstName());
            userDTO.setLastName(approval.getApprover().getLastName());
            userDTO.setEmail(approval.getApprover().getEmail());
            dto.setApprover(userDTO);
        }

        if (approval.getStage() != null) {
            EcoStageDTO stageDTO = new EcoStageDTO();
            stageDTO.setId(approval.getStage().getId());
            stageDTO.setName(approval.getStage().getName());
            dto.setStage(stageDTO);
        }

        return dto;
    }
}
