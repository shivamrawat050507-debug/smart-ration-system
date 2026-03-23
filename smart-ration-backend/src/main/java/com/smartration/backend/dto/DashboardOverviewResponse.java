package com.smartration.backend.dto;

import java.util.List;

public record DashboardOverviewResponse(
        UserPanel profile,
        List<MetricCard> metrics,
        FamilyEntitlement entitlement,
        DepotSnapshot depotSnapshot,
        DistributionStatus distributionStatus,
        AdminAnalytics adminAnalytics,
        List<CityInsight> cityInsights,
        List<CityDemandInsight> cityDemandInsights,
        List<DepotInsight> depotInsights,
        FraudSummary fraudSummary,
        List<ActivityLog> activityLogs,
        List<String> smartInsights,
        List<TransactionView> recentTransactions,
        List<String> fraudControls,
        List<String> workflowSteps,
        ReceiptPreview receiptPreview,
        List<AlertItem> alerts
) {

    public record UserPanel(
            Long userId,
            String name,
            String rationCardNumber,
            String role,
            String stateName,
            String cityName,
            String depotCode,
            String depotName,
            String beneficiaryStatus
    ) {
    }

    public record MetricCard(
            String label,
            String value,
            String helper
    ) {
    }

    public record FamilyEntitlement(
            String rationCategory,
            int familyMembers,
            String entitlementMonth,
            List<EntitlementItem> items,
            String policyNote
    ) {
    }

    public record EntitlementItem(
            String commodity,
            String unit,
            double entitlement,
            double issued,
            double pending,
            double depotAvailable
    ) {
    }

    public record DepotSnapshot(
            String depotCode,
            String depotName,
            String cityName,
            List<StockItem> stockItems,
            String partialDistributionRule,
            String unclaimedRule
    ) {
    }

    public record StockItem(
            String commodity,
            double available,
            double monthlyDemand,
            double reservedPending,
            String status
    ) {
    }

    public record CityInsight(
            String cityName,
            int population,
            int rationCards,
            double riceRequirement,
            double wheatRequirement,
            int lowStockDepots
    ) {
    }

    public record AdminAnalytics(
            long totalMembers,
            double totalWheatRequired,
            double totalStockAvailable,
            double totalDistributed,
            double currentMonthDemand,
            double previousMonthDemand,
            String demandTrend,
            String stockUsageTrend
    ) {
    }

    public record CityDemandInsight(
            String cityName,
            long totalPopulation,
            double wheatPerPerson,
            double totalRequiredStock,
            double availableStock,
            double shortageOrSurplus,
            String status
    ) {
    }

    public record DepotInsight(
            String depotCode,
            String depotName,
            String cityName,
            double totalStock,
            double distributedQuantity,
            double remainingStock,
            String status
    ) {
    }

    public record FraudSummary(
            long totalBlockedAttempts,
            long wrongDepotAccess,
            long duplicateClaims
    ) {
    }

    public record ActivityLog(
            String timestamp,
            String severity,
            String type,
            String message
    ) {
    }

    public record DistributionStatus(
            boolean eligible,
            boolean depotAuthorized,
            boolean partialDistributionExpected,
            String warningMessage,
            String pendingMessage,
            List<DistributionItem> items
    ) {
    }

    public record DistributionItem(
            String commodity,
            String unit,
            double required,
            double available,
            double given,
            double pending,
            String warning
    ) {
    }

    public record TransactionView(
            String transactionId,
            String beneficiary,
            String depotName,
            String status,
            String mode,
            String timestamp,
            String summary
    ) {
    }

    public record ReceiptPreview(
            String receiptNumber,
            String issueDate,
            String qrReference,
            String collectionDepot,
            String note
    ) {
    }

    public record AlertItem(
            String severity,
            String title,
            String detail
    ) {
    }
}
