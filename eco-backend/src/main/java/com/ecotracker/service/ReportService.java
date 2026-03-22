package com.ecotracker.service;

import com.ecotracker.dto.*;
import com.ecotracker.model.*;
import com.ecotracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final EcoRepository ecoRepository;
    private final EcoApprovalRepository approvalRepository;
    private final EcoStageHistoryRepository stageHistoryRepository;
    private final UserRepository userRepository;
    private final EcoTypeRepository typeRepository;

    /**
     * Get dashboard summary statistics
     */
    public DashboardDTO getDashboardStats(Long userId) {
        DashboardDTO dashboard = new DashboardDTO();

        // Total counts by state
        dashboard.setTotalEcos(ecoRepository.count());
        dashboard.setInProgressCount(ecoRepository.countByState(Eco.EcoState.IN_PROGRESS));
        dashboard.setChangeRequestCount(ecoRepository.countByState(Eco.EcoState.CHANGE_REQUEST));
        dashboard.setCompletedCount(ecoRepository.countByState(Eco.EcoState.DONE));

        // My ECOs
        if (userId != null) {
            dashboard.setMyEcosCount(ecoRepository.countByAssignedToId(userId));
            dashboard.setPendingApprovalsCount(
                    approvalRepository.countPendingByApproverId(userId));
        }

        // ECOs by priority
        Map<String, Long> byPriority = new HashMap<>();
        for (Eco.Priority priority : Eco.Priority.values()) {
            byPriority.put(priority.name(), ecoRepository.countByPriority(priority));
        }
        dashboard.setEcosByPriority(byPriority);

        // ECOs by type
        List<Object[]> typeStats = ecoRepository.countByTypeGrouped();
        Map<String, Long> byType = new HashMap<>();
        for (Object[] row : typeStats) {
            byType.put((String) row[0], (Long) row[1]);
        }
        dashboard.setEcosByType(byType);

        // Recent ECOs
        List<Eco> recentEcos = ecoRepository.findTop10ByOrderByCreatedAtDesc();
        dashboard.setRecentEcos(recentEcos.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList()));

        // Average cycle time (in days)
        Double avgCycleTime = calculateAverageCycleTime();
        dashboard.setAverageCycleTimeDays(avgCycleTime != null ? avgCycleTime : 0.0);

        return dashboard;
    }

    /**
     * Get ECO activity report
     */
    public ActivityReportDTO getActivityReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        ActivityReportDTO report = new ActivityReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // ECOs created in period
        List<Eco> createdEcos = ecoRepository.findByCreatedAtBetween(start, end);
        report.setEcosCreated(createdEcos.size());

        // ECOs completed in period
        List<Eco> completedEcos = ecoRepository.findByCompletedAtBetween(start, end);
        report.setEcosCompleted(completedEcos.size());

        // Activity by day
        Map<LocalDate, Integer> createdByDay = createdEcos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCreatedAt().toLocalDate(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        report.setCreatedByDay(createdByDay);

        Map<LocalDate, Integer> completedByDay = completedEcos.stream()
                .filter(e -> e.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCompletedAt().toLocalDate(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        report.setCompletedByDay(completedByDay);

        // Top contributors
        Map<String, Long> contributors = createdEcos.stream()
                .filter(e -> e.getCreatedBy() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCreatedBy().getFirstName() + " " + e.getCreatedBy().getLastName(),
                        Collectors.counting()
                ));
        report.setTopContributors(contributors);

        return report;
    }

    /**
     * Get cycle time report
     */
    public CycleTimeReportDTO getCycleTimeReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        CycleTimeReportDTO report = new CycleTimeReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Get completed ECOs in period
        List<Eco> completedEcos = ecoRepository.findByCompletedAtBetween(start, end);

        if (completedEcos.isEmpty()) {
            report.setAverageCycleTimeDays(0.0);
            report.setMinCycleTimeDays(0.0);
            report.setMaxCycleTimeDays(0.0);
            report.setMedianCycleTimeDays(0.0);
            return report;
        }

        // Calculate cycle times
        List<Double> cycleTimes = completedEcos.stream()
                .filter(e -> e.getCompletedAt() != null)
                .map(e -> (double) ChronoUnit.HOURS.between(e.getCreatedAt(), e.getCompletedAt()) / 24)
                .sorted()
                .collect(Collectors.toList());

        double avg = cycleTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double min = cycleTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = cycleTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        // Median
        double median;
        int size = cycleTimes.size();
        if (size % 2 == 0) {
            median = (cycleTimes.get(size / 2 - 1) + cycleTimes.get(size / 2)) / 2;
        } else {
            median = cycleTimes.get(size / 2);
        }

        report.setAverageCycleTimeDays(Math.round(avg * 100.0) / 100.0);
        report.setMinCycleTimeDays(Math.round(min * 100.0) / 100.0);
        report.setMaxCycleTimeDays(Math.round(max * 100.0) / 100.0);
        report.setMedianCycleTimeDays(Math.round(median * 100.0) / 100.0);

        // By ECO type
        Map<String, Double> byType = completedEcos.stream()
                .filter(e -> e.getCompletedAt() != null && e.getType() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getType().getName(),
                        Collectors.averagingDouble(e ->
                                (double) ChronoUnit.HOURS.between(e.getCreatedAt(), e.getCompletedAt()) / 24)
                ));
        report.setCycleTimeByType(byType);

        // By priority
        Map<String, Double> byPriority = completedEcos.stream()
                .filter(e -> e.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getPriority().name(),
                        Collectors.averagingDouble(e ->
                                (double) ChronoUnit.HOURS.between(e.getCreatedAt(), e.getCompletedAt()) / 24)
                ));
        report.setCycleTimeByPriority(byPriority);

        return report;
    }

    /**
     * Get approval performance report
     */
    public ApprovalReportDTO getApprovalReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        ApprovalReportDTO report = new ApprovalReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Get approvals in period
        List<EcoApproval> approvals = approvalRepository.findByApprovedAtBetween(start, end);

        report.setTotalApprovals(approvals.size());
        report.setApprovedCount((int) approvals.stream()
                .filter(a -> a.getStatus() == EcoApproval.ApprovalStatus.APPROVED).count());
        report.setRejectedCount((int) approvals.stream()
                .filter(a -> a.getStatus() == EcoApproval.ApprovalStatus.REJECTED).count());

        // Average response time
        double avgResponseTime = approvals.stream()
                .filter(a -> a.getApprovedAt() != null)
                .mapToDouble(a -> ChronoUnit.HOURS.between(a.getCreatedAt(), a.getApprovedAt()) / 24.0)
                .average()
                .orElse(0);
        report.setAverageResponseTimeDays(Math.round(avgResponseTime * 100.0) / 100.0);

        // By approver
        Map<String, ApprovalReportDTO.ApproverStats> approverStats = new HashMap<>();
        approvals.stream()
                .filter(a -> a.getApprover() != null)
                .forEach(a -> {
                    String name = a.getApprover().getFirstName() + " " + a.getApprover().getLastName();
                    ApprovalReportDTO.ApproverStats stats = approverStats.computeIfAbsent(
                            name, k -> new ApprovalReportDTO.ApproverStats());
                    stats.setTotalReviews(stats.getTotalReviews() + 1);
                    if (a.getStatus() == EcoApproval.ApprovalStatus.APPROVED) {
                        stats.setApprovals(stats.getApprovals() + 1);
                    } else if (a.getStatus() == EcoApproval.ApprovalStatus.REJECTED) {
                        stats.setRejections(stats.getRejections() + 1);
                    }
                    if (a.getApprovedAt() != null) {
                        double days = ChronoUnit.HOURS.between(a.getCreatedAt(), a.getApprovedAt()) / 24.0;
                        stats.addResponseTime(days);
                    }
                });
        report.setApproverStats(approverStats);

        return report;
    }

    /**
     * Get stage bottleneck analysis
     */
    public BottleneckReportDTO getBottleneckReport() {
        BottleneckReportDTO report = new BottleneckReportDTO();

        // Get all stage history
        List<EcoStageHistory> history = stageHistoryRepository.findAll();

        // Calculate time spent in each stage
        Map<String, List<Double>> stageTimesMap = new HashMap<>();

        // Group by ECO to calculate time between transitions
        Map<Long, List<EcoStageHistory>> byEco = history.stream()
                .collect(Collectors.groupingBy(h -> h.getEco().getId()));

        for (List<EcoStageHistory> ecoHistory : byEco.values()) {
            List<EcoStageHistory> sorted = ecoHistory.stream()
                    .sorted(Comparator.comparing(EcoStageHistory::getCreatedAt))
                    .collect(Collectors.toList());

            for (int i = 0; i < sorted.size() - 1; i++) {
                EcoStageHistory current = sorted.get(i);
                EcoStageHistory next = sorted.get(i + 1);

                if (current.getToStage() != null) {
                    String stageName = current.getToStage().getName();
                    double hours = ChronoUnit.HOURS.between(current.getCreatedAt(), next.getCreatedAt());
                    double days = hours / 24;

                    stageTimesMap.computeIfAbsent(stageName, k -> new ArrayList<>()).add(days);
                }
            }
        }

        // Calculate averages
        Map<String, Double> avgTimeByStage = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : stageTimesMap.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            avgTimeByStage.put(entry.getKey(), Math.round(avg * 100.0) / 100.0);
        }
        report.setAverageTimeByStage(avgTimeByStage);

        // Identify bottlenecks (stages with above-average time)
        double overallAvg = avgTimeByStage.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        List<String> bottlenecks = avgTimeByStage.entrySet().stream()
                .filter(e -> e.getValue() > overallAvg * 1.5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        report.setBottleneckStages(bottlenecks);

        // Current ECOs by stage
        Map<String, Long> currentByStage = ecoRepository.findByStateIn(
                        List.of(Eco.EcoState.IN_PROGRESS, Eco.EcoState.CHANGE_REQUEST))
                .stream()
                .filter(e -> e.getCurrentStage() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCurrentStage().getName(),
                        Collectors.counting()
                ));
        report.setCurrentEcosByStage(currentByStage);

        return report;
    }

    /**
     * Export report data
     */
    public byte[] exportReport(String reportType, LocalDate startDate, LocalDate endDate, String format) {
        // This would generate CSV or Excel exports
        // For now, return placeholder
        StringBuilder csv = new StringBuilder();

        switch (reportType) {
            case "activity":
                csv.append("Date,Created,Completed\n");
                ActivityReportDTO activity = getActivityReport(startDate, endDate);
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    csv.append(date)
                            .append(",")
                            .append(activity.getCreatedByDay().getOrDefault(date, 0))
                            .append(",")
                            .append(activity.getCompletedByDay().getOrDefault(date, 0))
                            .append("\n");
                }
                break;

            case "cycle-time":
                csv.append("Type,Average Cycle Time (Days)\n");
                CycleTimeReportDTO cycleTime = getCycleTimeReport(startDate, endDate);
                for (Map.Entry<String, Double> entry : cycleTime.getCycleTimeByType().entrySet()) {
                    csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
                }
                break;
        }

        return csv.toString().getBytes();
    }

    // ==================== Helper Methods ====================

    private Double calculateAverageCycleTime() {
        List<Eco> completedEcos = ecoRepository.findByState(Eco.EcoState.DONE);

        if (completedEcos.isEmpty()) return null;

        double totalDays = completedEcos.stream()
                .filter(e -> e.getCompletedAt() != null)
                .mapToDouble(e -> ChronoUnit.HOURS.between(e.getCreatedAt(), e.getCompletedAt()) / 24.0)
                .sum();

        long count = completedEcos.stream()
                .filter(e -> e.getCompletedAt() != null)
                .count();

        return count > 0 ? Math.round((totalDays / count) * 100.0) / 100.0 : null;
    }

    private EcoSummaryDTO mapToSummary(Eco eco) {
        EcoSummaryDTO dto = new EcoSummaryDTO();
        dto.setId(eco.getId());
        dto.setReference(eco.getReference());
        dto.setName(eco.getName());
        dto.setState(eco.getState().name());
        dto.setPriority(eco.getPriority().name());
        dto.setCreatedAt(eco.getCreatedAt());

        if (eco.getType() != null) {
            dto.setTypeName(eco.getType().getName());
        }
        if (eco.getCurrentStage() != null) {
            dto.setStageName(eco.getCurrentStage().getName());
        }
        if (eco.getAssignedTo() != null) {
            dto.setAssignedToName(eco.getAssignedTo().getFirstName() + " " + eco.getAssignedTo().getLastName());
        }

        return dto;
    }
}
