package com.odoo.plm.service;

import com.odoo.plm.dto.response.report.AuditLogResponse;
import com.odoo.plm.entity.AuditLog;
import com.odoo.plm.entity.Eco;
import com.odoo.plm.entity.User;
import com.odoo.plm.repository.AuditLogRepository;
import com.odoo.plm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // Audit action constants
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_ACTIVATED = "USER_ACTIVATED";
    public static final String USER_DEACTIVATED = "USER_DEACTIVATED";
    public static final String USER_ROLE_CHANGED = "USER_ROLE_CHANGED";

    public static final String PRODUCT_CREATED = "PRODUCT_CREATED";
    public static final String PRODUCT_ACTIVATED = "PRODUCT_ACTIVATED";
    public static final String PRODUCT_ARCHIVED = "PRODUCT_ARCHIVED";

    public static final String BOM_CREATED = "BOM_CREATED";
    public static final String BOM_ACTIVATED = "BOM_ACTIVATED";
    public static final String BOM_ARCHIVED = "BOM_ARCHIVED";

    public static final String ECO_CREATED = "ECO_CREATED";
    public static final String ECO_UPDATED = "ECO_UPDATED";
    public static final String ECO_SUBMITTED = "ECO_SUBMITTED";
    public static final String ECO_APPROVED = "ECO_APPROVED";
    public static final String ECO_REJECTED = "ECO_REJECTED";
    public static final String ECO_APPLIED = "ECO_APPLIED";
    public static final String ECO_STAGE_CHANGED = "ECO_STAGE_CHANGED";

    public static final String STAGE_CREATED = "STAGE_CREATED";
    public static final String STAGE_UPDATED = "STAGE_UPDATED";
    public static final String STAGE_DELETED = "STAGE_DELETED";

    public static final String APPROVAL_RULE_CREATED = "APPROVAL_RULE_CREATED";
    public static final String APPROVAL_RULE_DELETED = "APPROVAL_RULE_DELETED";

    /**
     * Log an action with ECO context
     */
    @Transactional
    public void logAction(String action, User actor, Eco eco, String affectedRecord, String oldValue, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .actorUser(actor)
                .eco(eco)
                .affectedRecord(affectedRecord)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log created: action={}, actor={}, affected={}", action, actor.getLoginId(), affectedRecord);
    }

    /**
     * Log an action without ECO context
     */
    @Transactional
    public void logAction(String action, User actor, String affectedRecord, String oldValue, String newValue) {
        logAction(action, actor, null, affectedRecord, oldValue, newValue);
    }

    /**
     * Log using current authenticated user
     */
    @Transactional
    public void logAction(String action, String affectedRecord, String oldValue, String newValue) {
        User currentUser = getCurrentUser();
        logAction(action, currentUser, null, affectedRecord, oldValue, newValue);
    }

    /**
     * Log using current authenticated user with ECO
     */
    @Transactional
    public void logAction(String action, Eco eco, String affectedRecord, String oldValue, String newValue) {
        User currentUser = getCurrentUser();
        logAction(action, currentUser, eco, affectedRecord, oldValue, newValue);
    }

    /**
     * Async logging for non-critical audit trails
     */
    @Async
    @Transactional
    public void logActionAsync(String action, UUID actorId, UUID ecoId, String affectedRecord, String oldValue, String newValue) {
        User actor = userRepository.findById(actorId).orElse(null);
        if (actor == null) {
            log.warn("Actor not found for audit log: {}", actorId);
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .actorUser(actor)
                .affectedRecord(affectedRecord)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Get all audit logs for an ECO
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsForEco(UUID ecoId) {
        return auditLogRepository.findByEcoIdOrderByTimestampDesc(ecoId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get audit logs by actor
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByActor(UUID userId, Pageable pageable) {
        return auditLogRepository.findByActorUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search audit logs with filters
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchLogs(String action, UUID actorId, LocalDateTime startDate,
                                              LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(action, actorId, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get recent activity
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getRecentActivity(Pageable pageable) {
        return auditLogRepository.findRecentActivity(pageable)
                .map(this::mapToResponse);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .ecoId(log.getEco() != null ? log.getEco().getId() : null)
                .ecoTitle(log.getEco() != null ? log.getEco().getTitle() : null)
                .actorId(log.getActorUser().getId())
                .actorName(log.getActorUser().getFirstName() + " " + log.getActorUser().getLastName())
                .actorLoginId(log.getActorUser().getLoginId())
                .action(log.getAction())
                .affectedRecord(log.getAffectedRecord())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .timestamp(log.getTimestamp())
                .build();
    }
}
