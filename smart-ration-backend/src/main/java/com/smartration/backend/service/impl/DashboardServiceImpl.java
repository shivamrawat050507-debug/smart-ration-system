package com.smartration.backend.service.impl;

import com.smartration.backend.dto.DashboardOverviewResponse;
import com.smartration.backend.dto.DealerScanRequest;
import com.smartration.backend.dto.DealerScanResponse;
import com.smartration.backend.dto.DistributionResponse;
import com.smartration.backend.dto.AddCityRequest;
import com.smartration.backend.dto.AddDepotRequest;
import com.smartration.backend.dto.AllocateStockRequest;
import com.smartration.backend.dto.MessageResponse;
import com.smartration.backend.dto.UpdateRationRuleRequest;
import com.smartration.backend.entity.City;
import com.smartration.backend.entity.Dealer;
import com.smartration.backend.entity.Depot;
import com.smartration.backend.entity.DepotStock;
import com.smartration.backend.entity.DistributionTransaction;
import com.smartration.backend.entity.MonthlyEntitlement;
import com.smartration.backend.entity.RationCard;
import com.smartration.backend.entity.RationRule;
import com.smartration.backend.entity.Role;
import com.smartration.backend.entity.StateUnit;
import com.smartration.backend.entity.TransactionItem;
import com.smartration.backend.entity.User;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.CityRepository;
import com.smartration.backend.repository.DealerRepository;
import com.smartration.backend.repository.DepotRepository;
import com.smartration.backend.repository.DepotStockRepository;
import com.smartration.backend.repository.DistributionTransactionRepository;
import com.smartration.backend.repository.FamilyMemberRepository;
import com.smartration.backend.repository.MonthlyEntitlementRepository;
import com.smartration.backend.repository.RationCardRepository;
import com.smartration.backend.repository.RationRuleRepository;
import com.smartration.backend.repository.StateUnitRepository;
import com.smartration.backend.repository.TransactionItemRepository;
import com.smartration.backend.repository.UserRepository;
import com.smartration.backend.service.DashboardService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final UserRepository userRepository;
    private final StateUnitRepository stateUnitRepository;
    private final CityRepository cityRepository;
    private final DealerRepository dealerRepository;
    private final DepotRepository depotRepository;
    private final RationCardRepository rationCardRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final RationRuleRepository rationRuleRepository;
    private final DepotStockRepository depotStockRepository;
    private final MonthlyEntitlementRepository monthlyEntitlementRepository;
    private final DistributionTransactionRepository distributionTransactionRepository;
    private final TransactionItemRepository transactionItemRepository;

    @Override
    public DashboardOverviewResponse getOverview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String currentMonth = YearMonth.now().toString();
        String displayMonth = YearMonth.now().format(MONTH_FORMAT);
        Context context = resolveContext(user);

        List<MonthlyEntitlement> entitlements = context.rationCard() == null
                ? List.of()
                : monthlyEntitlementRepository.findByRationCardAndEntitlementMonthOrderByCommodityName(context.rationCard(), currentMonth);
        List<DepotStock> stocks = context.depot() == null
                ? List.of()
                : depotStockRepository.findByDepotOrderByCommodityName(context.depot());

        int familyMembers = context.rationCard() == null ? 0 : (int) familyMemberRepository.countByRationCardAndActiveTrue(context.rationCard());
        List<DashboardOverviewResponse.EntitlementItem> entitlementItems = buildEntitlementItems(context, entitlements, stocks, familyMembers);

        return new DashboardOverviewResponse(
                new DashboardOverviewResponse.UserPanel(
                        user.getId(),
                        user.getName(),
                        user.getRationCardNumber(),
                        user.getRole().name(),
                        context.state().getStateName(),
                        context.cityName(),
                        context.depotCode(),
                        context.depotName(),
                        user.getRole() == Role.ROLE_USER ? "Eligible this month" : "Operations access enabled"
                ),
                buildMetrics(user.getRole(), context, familyMembers, entitlements, stocks),
                new DashboardOverviewResponse.FamilyEntitlement(
                        context.rationCategory(),
                        familyMembers,
                        displayMonth,
                        entitlementItems,
                        buildPolicyNote(context.state(), context.rationCategory())
                ),
                new DashboardOverviewResponse.DepotSnapshot(
                        context.depotCode(),
                        context.depotName(),
                        context.cityName(),
                        buildStockItems(stocks),
                        "If stock is lower than entitlement, the system issues what is available and carries the remainder as pending.",
                        "Unclaimed monthly quota expires at period close and the reserved balance is digitally returned to the central pool."
                ),
                buildDistributionStatus(context, entitlements, stocks, familyMembers),
                buildAdminAnalytics(currentMonth),
                buildCityInsights(currentMonth),
                buildCityDemandInsights(currentMonth),
                buildDepotInsights(currentMonth),
                buildFraudSummary(),
                buildActivityLogs(),
                buildSmartInsights(currentMonth),
                buildRecentTransactions(),
                List.of(
                        "QR token must match an active ration card and current monthly cycle.",
                        "Collection is allowed only at the beneficiary's assigned depot.",
                        "The platform blocks repeat claims after monthly entitlement is exhausted.",
                        "Every issue, return, and stock adjustment is centrally logged for audit."
                ),
                List.of(
                        "Dealer scans QR code from the ration card or mobile pass.",
                        "System validates state, city, depot assignment, and monthly claim status.",
                        "Family size and state policy determine per-item entitlement in real time.",
                        "Depot stock is checked and full or partial issue is calculated.",
                        "Transaction, receipt, and pending balance are recorded centrally."
                ),
                buildReceiptPreview(context.rationCard()),
                buildAlerts(user.getRole(), stocks, entitlements)
        );
    }

    @Override
    public DistributionResponse distributeRation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() != Role.ROLE_USER) {
            throw new BadRequestException("Distribution action is available only for beneficiary accounts in this demo flow");
        }

        Context context = resolveContext(user);
        RationCard rationCard = context.rationCard();
        if (rationCard == null) {
            throw new BadRequestException("Depot authorization failed for this ration card");
        }
        Dealer dealer = findDealerForDepot(context.depot());
        return executeDistribution(rationCard, context, dealer);
    }

    @Override
    public DealerScanResponse scanRationCard(Long dealerUserId, DealerScanRequest request) {
        Dealer dealer = resolveDealerUser(dealerUserId);
        RationCard rationCard = rationCardRepository.findByQrCodeValue(request.qrCodeValue().trim())
                .orElseThrow(() -> new ResourceNotFoundException("QR code not recognized"));
        return buildDealerScanResponse(dealer, rationCard);
    }

    @Override
    public DistributionResponse distributeRationForDealer(Long dealerUserId, DealerScanRequest request) {
        Dealer dealer = resolveDealerUser(dealerUserId);
        RationCard rationCard = rationCardRepository.findByQrCodeValue(request.qrCodeValue().trim())
                .orElseThrow(() -> new ResourceNotFoundException("QR code not recognized"));
        Context context = buildBeneficiaryContext(rationCard);

        if (!dealer.getDepot().getId().equals(rationCard.getDepot().getId())) {
            throw new BadRequestException("This ration card is mapped to another depot");
        }

        DealerScanResponse preview = buildDealerScanResponse(dealer, rationCard);
        if (!preview.canDistribute()) {
            throw new BadRequestException(preview.reason());
        }

        return executeDistribution(rationCard, context, dealer);
    }

    @Override
    @Transactional
    public MessageResponse allocateStock(AllocateStockRequest request) {
        Depot depot = depotRepository.findByDepotCode(request.depotCode())
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found: " + request.depotCode()));
        DepotStock stock = depotStockRepository.findByDepotAndCommodityNameIgnoreCase(depot, request.commodity())
                .orElseGet(() -> DepotStock.builder()
                        .depot(depot)
                        .commodityName(request.commodity())
                        .availableQuantity(BigDecimal.ZERO)
                        .reservedPendingQuantity(BigDecimal.ZERO)
                        .monthlyRequiredQuantity(BigDecimal.ZERO)
                        .lastUpdatedAt(LocalDateTime.now())
                        .build());
        stock.setAvailableQuantity(stock.getAvailableQuantity().add(BigDecimal.valueOf(request.quantity())));
        stock.setLastUpdatedAt(LocalDateTime.now());
        depotStockRepository.save(stock);
        return new MessageResponse("Allocated " + request.quantity() + " kg of " + request.commodity() + " to " + depot.getDepotCode());
    }

    @Override
    @Transactional
    public MessageResponse updateRationRule(UpdateRationRuleRequest request) {
        StateUnit state = stateUnitRepository.findByStateCode(request.stateCode())
                .orElseThrow(() -> new ResourceNotFoundException("State not found: " + request.stateCode()));
        RationRule rule = rationRuleRepository.findByStateAndRationCategoryIgnoreCase(state, request.rationCategory()).stream()
                .filter(entry -> entry.getCommodityName().equalsIgnoreCase(request.commodityName()))
                .findFirst()
                .orElseGet(() -> RationRule.builder()
                        .state(state)
                        .rationCategory(request.rationCategory())
                        .commodityName(request.commodityName())
                        .unit(request.unit())
                        .effectiveFrom(LocalDateTime.now().toLocalDate())
                        .build());
        rule.setUnit(request.unit());
        rule.setQuantityPerPerson(BigDecimal.valueOf(request.quantityPerPerson()));
        if (rule.getEffectiveFrom() == null) {
            rule.setEffectiveFrom(LocalDateTime.now().toLocalDate());
        }
        rationRuleRepository.save(rule);
        return new MessageResponse("Updated ration rule for " + request.commodityName() + " in " + request.rationCategory());
    }

    @Override
    @Transactional
    public MessageResponse addCity(AddCityRequest request) {
        if (cityRepository.findByCityCode(request.cityCode()).isPresent()) {
            throw new BadRequestException("City code already exists");
        }
        StateUnit state = stateUnitRepository.findByStateCode(request.stateCode())
                .orElseThrow(() -> new ResourceNotFoundException("State not found: " + request.stateCode()));
        cityRepository.save(City.builder()
                .state(state)
                .cityCode(request.cityCode())
                .cityName(request.cityName())
                .population(request.population())
                .active(true)
                .build());
        return new MessageResponse("City added successfully");
    }

    @Override
    @Transactional
    public MessageResponse addDepot(AddDepotRequest request) {
        if (depotRepository.findByDepotCode(request.depotCode()).isPresent()) {
            throw new BadRequestException("Depot code already exists");
        }
        City city = cityRepository.findByCityCode(request.cityCode())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + request.cityCode()));
        depotRepository.save(Depot.builder()
                .city(city)
                .depotCode(request.depotCode())
                .depotName(request.depotName())
                .address(request.address())
                .active(true)
                .build());
        return new MessageResponse("Depot added successfully");
    }

    private Dealer resolveDealerUser(Long dealerUserId) {
        User user = userRepository.findById(dealerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dealerUserId));
        if (user.getRole() != Role.ROLE_DEALER) {
            throw new BadRequestException("Only dealer accounts can scan ration cards");
        }
        return dealerRepository.findByUsernameOrMobile(user.getRationCardNumber(), user.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer mapping not found"));
    }

    private Dealer findDealerForDepot(Depot depot) {
        return dealerRepository.findAll().stream()
                .filter(entry -> entry.getDepot().getId().equals(depot.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Dealer mapping not found for depot"));
    }

    private Context buildBeneficiaryContext(RationCard rationCard) {
        Depot depot = rationCard.getDepot();
        return new Context(rationCard.getState(), rationCard.getCity().getCityName(), depot.getDepotCode(), depot.getDepotName(), depot, rationCard, rationCard.getCategory());
    }

    private Context resolveContext(User user) {
        StateUnit state = stateUnitRepository.findByStateCode("HR")
                .orElseThrow(() -> new ResourceNotFoundException("State configuration not found"));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return new Context(state, "State Control Room", "HQ-CTRL-001", "Central Monitoring Hub", null, null, "Operational");
        }

        if (user.getRole() == Role.ROLE_DEALER) {
            Dealer dealer = dealerRepository.findByUsernameOrMobile(user.getRationCardNumber(), user.getPhone())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer mapping not found"));
            Depot depot = dealer.getDepot();
            return new Context(state, depot.getCity().getCityName(), depot.getDepotCode(), depot.getDepotName(), depot, null, "Operational");
        }

        RationCard rationCard = rationCardRepository.findByRationCardNo(user.getRationCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Ration card mapping not found"));
        return buildBeneficiaryContext(rationCard);
    }

    private List<DashboardOverviewResponse.MetricCard> buildMetrics(
            Role role,
            Context context,
            int familyMembers,
            List<MonthlyEntitlement> entitlements,
            List<DepotStock> stocks
    ) {
        BigDecimal totalIssued = entitlements.stream()
                .map(MonthlyEntitlement::getIssuedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = entitlements.stream()
                .map(MonthlyEntitlement::getPendingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long lowStockCount = stocks.stream().filter(this::isLowStock).count();

        if (role == Role.ROLE_ADMIN) {
            return List.of(
                    new DashboardOverviewResponse.MetricCard("States Configured", String.valueOf(stateUnitRepository.count()), "Policy-driven hierarchy ready for multiple states"),
                    new DashboardOverviewResponse.MetricCard("Cities Monitored", String.valueOf(cityRepository.findAllByActiveTrue().size()), "City records are database-driven, not hardcoded"),
                    new DashboardOverviewResponse.MetricCard("Active Depots", String.valueOf(depotRepository.count()), "Assigned stock and transactions tracked centrally"),
                    new DashboardOverviewResponse.MetricCard("Fraud Blocks This Month", String.valueOf(
                            distributionTransactionRepository.findTop3ByOrderByIssuedAtDesc().stream()
                                    .filter(transaction -> "BLOCKED".equalsIgnoreCase(transaction.getStatus()))
                                    .count()), "Unauthorized depots and duplicate claims stopped")
            );
        }

        if (role == Role.ROLE_DEALER) {
            String currentMonth = YearMonth.now().toString();
            long collectedPeople = distributionTransactionRepository.findAll().stream()
                    .filter(transaction -> transaction.getDepot().getId().equals(context.depot().getId()))
                    .filter(transaction -> transaction.getDistributionMonth().equals(currentMonth))
                    .filter(transaction -> !"BLOCKED".equalsIgnoreCase(transaction.getStatus()))
                    .map(transaction -> transaction.getRationCard().getId())
                    .distinct()
                    .count();
            return List.of(
                    new DashboardOverviewResponse.MetricCard("People Collected", String.valueOf(collectedPeople), "Unique beneficiaries served from this depot this month"),
                    new DashboardOverviewResponse.MetricCard("Pending Partial Claims", totalPending.stripTrailingZeros().toPlainString() + " kg", "Balances waiting for stock refill"),
                    new DashboardOverviewResponse.MetricCard("Assigned Depot", stocks.isEmpty() ? "Unavailable" : stocks.get(0).getDepot().getDepotCode(), "Dealer can issue ration only at this depot"),
                    new DashboardOverviewResponse.MetricCard("Low Stock Commodities", String.valueOf(lowStockCount), "Commodities below comfortable stock coverage")
            );
        }

        return List.of(
                new DashboardOverviewResponse.MetricCard("Family Members", String.valueOf(familyMembers), "Used for dynamic monthly entitlement"),
                new DashboardOverviewResponse.MetricCard("Assigned Depot", stocks.isEmpty() ? "Unavailable" : stocks.get(0).getDepot().getDepotCode(), "Collection restricted to mapped depot"),
                new DashboardOverviewResponse.MetricCard("Issued This Month", totalIssued.stripTrailingZeros().toPlainString() + " kg", "Full and partial issues are both tracked"),
                new DashboardOverviewResponse.MetricCard("Pending Balance", totalPending.stripTrailingZeros().toPlainString() + " kg", "Will be served automatically when stock is refilled")
        );
    }

    private List<DashboardOverviewResponse.EntitlementItem> buildEntitlementItems(
            Context context,
            List<MonthlyEntitlement> entitlements,
            List<DepotStock> stocks,
            int familyMembers
    ) {
        List<DashboardOverviewResponse.EntitlementItem> items = new ArrayList<>();
        if (context.rationCard() != null && !entitlements.isEmpty()) {
            for (MonthlyEntitlement entitlement : entitlements) {
                DepotStock stock = stocks.stream()
                        .filter(item -> item.getCommodityName().equalsIgnoreCase(entitlement.getCommodityName()))
                        .findFirst()
                        .orElse(null);
                items.add(new DashboardOverviewResponse.EntitlementItem(
                        entitlement.getCommodityName(),
                        "kg",
                        entitlement.getEntitledQuantity().doubleValue(),
                        entitlement.getIssuedQuantity().doubleValue(),
                        entitlement.getPendingQuantity().doubleValue(),
                        stock == null ? 0 : stock.getAvailableQuantity().doubleValue()
                ));
            }
            return items;
        }

        List<RationRule> rules = rationRuleRepository.findByStateAndRationCategoryIgnoreCase(context.state(), "BPL");
        for (RationRule rule : rules) {
            DepotStock stock = stocks.stream()
                    .filter(item -> item.getCommodityName().equalsIgnoreCase(rule.getCommodityName()))
                    .findFirst()
                    .orElse(null);
            items.add(new DashboardOverviewResponse.EntitlementItem(
                    rule.getCommodityName(),
                    rule.getUnit(),
                    familyMembers == 0 ? 0 : rule.getQuantityPerPerson().multiply(BigDecimal.valueOf(familyMembers)).doubleValue(),
                    0,
                    0,
                    stock == null ? 0 : stock.getAvailableQuantity().doubleValue()
            ));
        }
        return items;
    }

    private DashboardOverviewResponse.DistributionStatus buildDistributionStatus(
            Context context,
            List<MonthlyEntitlement> entitlements,
            List<DepotStock> stocks,
            int familyMembers
    ) {
        if (context.rationCard() == null) {
            return new DashboardOverviewResponse.DistributionStatus(
                    false,
                    false,
                    false,
                    "Dealer scan flow validates beneficiary depot authorization before issue.",
                    "Pending ration is calculated once a beneficiary card is selected.",
                    List.of()
            );
        }

        List<MonthlyEntitlement> effectiveEntitlements = entitlements.isEmpty()
                ? buildVirtualEntitlements(context.rationCard(), familyMembers)
                : entitlements;
        List<DashboardOverviewResponse.DistributionItem> items = calculateDistributionItems(effectiveEntitlements, stocks);
        boolean eligible = items.stream().anyMatch(item -> item.required() > 0);
        boolean partial = items.stream().anyMatch(item -> item.pending() > 0);
        DashboardOverviewResponse.DistributionItem firstWarning = items.stream()
                .filter(item -> item.warning() != null && !item.warning().isBlank())
                .findFirst()
                .orElse(null);

        return new DashboardOverviewResponse.DistributionStatus(
                eligible,
                true,
                partial,
                firstWarning == null ? "Sufficient stock available for current entitlement." : firstWarning.warning(),
                partial
                        ? "Pending: "
                        + items.stream().map(DashboardOverviewResponse.DistributionItem::pending).reduce(0.0, Double::sum)
                        + " kg (due to low stock)"
                        : "No pending balance expected after this distribution.",
                items
        );
    }

    private String buildPolicyNote(StateUnit state, String category) {
        List<RationRule> rules = rationRuleRepository.findByStateAndRationCategoryIgnoreCase(state, category);
        if (rules.isEmpty()) {
            return "Per-person allocation is derived from state policy, ration category, and assigned family size.";
        }
        return "Active " + state.getStateName() + " " + category + " rules: "
                + rules.stream()
                .sorted(Comparator.comparing(RationRule::getCommodityName))
                .map(rule -> rule.getCommodityName() + " " + rule.getQuantityPerPerson().stripTrailingZeros().toPlainString() + " " + rule.getUnit() + "/person")
                .reduce((left, right) -> left + ", " + right)
                .orElse("Policy configuration available.");
    }

    private List<DashboardOverviewResponse.StockItem> buildStockItems(List<DepotStock> stocks) {
        return stocks.stream()
                .map(stock -> new DashboardOverviewResponse.StockItem(
                        stock.getCommodityName(),
                        stock.getAvailableQuantity().doubleValue(),
                        stock.getMonthlyRequiredQuantity().doubleValue(),
                        stock.getReservedPendingQuantity().doubleValue(),
                        stockStatus(stock)
                ))
                .toList();
    }

    private List<DashboardOverviewResponse.DistributionItem> calculateDistributionItems(
            List<MonthlyEntitlement> entitlements,
            List<DepotStock> stocks
    ) {
        return entitlements.stream()
                .map(entitlement -> {
                    double required = entitlement.getEntitledQuantity().subtract(entitlement.getIssuedQuantity()).doubleValue();
                    DepotStock stock = stocks.stream()
                            .filter(item -> item.getCommodityName().equalsIgnoreCase(entitlement.getCommodityName()))
                            .findFirst()
                            .orElse(null);
                    double available = stock == null ? 0 : stock.getAvailableQuantity().doubleValue();
                    double given = Math.max(0, Math.min(required, available));
                    double pending = Math.max(0, required - given);
                    String warning = pending > 0
                            ? "Low Stock: Only " + available + " kg available for " + entitlement.getCommodityName()
                            : "";
                    return new DashboardOverviewResponse.DistributionItem(
                            entitlement.getCommodityName(),
                            "kg",
                            required,
                            available,
                            given,
                            pending,
                            warning
                    );
                })
                .toList();
    }

    private DealerScanResponse buildDealerScanResponse(Dealer dealer, RationCard rationCard) {
        Context context = buildBeneficiaryContext(rationCard);
        int familyMembers = (int) familyMemberRepository.countByRationCardAndActiveTrue(rationCard);
        String currentMonth = YearMonth.now().toString();
        List<MonthlyEntitlement> entitlements = ensureMonthlyEntitlements(rationCard, currentMonth, familyMembers);
        List<DepotStock> stocks = depotStockRepository.findByDepotOrderByCommodityName(dealer.getDepot());
        boolean assignedDepot = dealer.getDepot().getId().equals(rationCard.getDepot().getId());
        List<DashboardOverviewResponse.DistributionItem> items = assignedDepot
                ? calculateDistributionItems(entitlements, stocks)
                : List.of();
        boolean eligible = assignedDepot && items.stream().anyMatch(item -> item.required() > 0);
        boolean partial = eligible && items.stream().anyMatch(item -> item.pending() > 0);
        DashboardOverviewResponse.DistributionItem primaryItem = choosePrimaryItem(items);

        String status;
        String statusTone;
        String headline;
        String supportingText = "";
        String reason = "";
        String warningMessage = "";
        String pendingMessage = "";

        if (!assignedDepot) {
            status = "NOT_ELIGIBLE";
            statusTone = "danger";
            headline = "NOT ELIGIBLE";
            reason = "Assigned to " + rationCard.getDepot().getDepotCode() + ". Collect only from the mapped depot.";
        } else if (!eligible) {
            status = "NOT_ELIGIBLE";
            statusTone = "danger";
            headline = "NOT ELIGIBLE";
            reason = "Already collected this month";
        } else if (partial && primaryItem != null) {
            status = "LOW_STOCK";
            statusTone = "warning";
            headline = "GIVE NOW: " + formatQuantity(primaryItem.given()) + " KG";
            supportingText = "PENDING: " + formatQuantity(primaryItem.pending()) + " KG";
            reason = "Low stock at depot";
            warningMessage = primaryItem.warning();
            pendingMessage = "Pending: "
                    + formatQuantity(items.stream().map(DashboardOverviewResponse.DistributionItem::pending).reduce(0.0, Double::sum))
                    + " kg (due to low stock)";
        } else if (primaryItem != null) {
            status = "FULL_STOCK";
            statusTone = "success";
            headline = "GIVE " + primaryItem.commodity().toUpperCase(Locale.ROOT) + ": " + formatQuantity(primaryItem.given()) + " KG";
            supportingText = "Full stock available for this entitlement.";
            reason = "Monthly entitlement valid";
        } else {
            status = "NOT_ELIGIBLE";
            statusTone = "danger";
            headline = "NOT ELIGIBLE";
            reason = "No distribution instruction available for this scan";
        }

        List<DealerScanResponse.InstructionLine> instructionLines = items.stream()
                .filter(item -> item.required() > 0)
                .map(item -> new DealerScanResponse.InstructionLine(
                        item.commodity(),
                        item.pending() > 0
                                ? "Give now " + formatQuantity(item.given()) + " kg, pending " + formatQuantity(item.pending()) + " kg"
                                : "Give " + formatQuantity(item.given()) + " kg",
                        item.given(),
                        item.pending(),
                        item.unit()
                ))
                .toList();

        return new DealerScanResponse(
                dealer.getFullName(),
                dealer.getDepot().getDepotCode(),
                dealer.getDepot().getDepotName(),
                rationCard.getQrCodeValue(),
                rationCard.getHeadOfFamily(),
                rationCard.getRationCardNo(),
                familyMembers,
                rationCard.getCategory(),
                eligible,
                assignedDepot && eligible,
                status,
                statusTone,
                headline,
                supportingText,
                reason,
                warningMessage,
                pendingMessage,
                instructionLines,
                buildReceiptPreview(context.rationCard())
        );
    }

    private DashboardOverviewResponse.DistributionItem choosePrimaryItem(List<DashboardOverviewResponse.DistributionItem> items) {
        return items.stream()
                .filter(item -> item.required() > 0)
                .sorted((left, right) -> {
                    if (left.commodity().equalsIgnoreCase("Wheat") && !right.commodity().equalsIgnoreCase("Wheat")) {
                        return -1;
                    }
                    if (!left.commodity().equalsIgnoreCase("Wheat") && right.commodity().equalsIgnoreCase("Wheat")) {
                        return 1;
                    }
                    return Double.compare(right.required(), left.required());
                })
                .findFirst()
                .orElse(null);
    }

    @Transactional
    private DistributionResponse executeDistribution(RationCard rationCard, Context context, Dealer dealer) {
        String currentMonth = YearMonth.now().toString();
        int familyMembers = (int) familyMemberRepository.countByRationCardAndActiveTrue(rationCard);
        List<MonthlyEntitlement> entitlements = ensureMonthlyEntitlements(rationCard, currentMonth, familyMembers);
        List<DepotStock> stocks = depotStockRepository.findByDepotOrderByCommodityName(context.depot());
        List<DashboardOverviewResponse.DistributionItem> items = calculateDistributionItems(entitlements, stocks);

        boolean anyEligible = items.stream().anyMatch(item -> item.required() > 0);
        if (!anyEligible) {
            throw new BadRequestException("Monthly eligibility already exhausted for this ration card");
        }

        boolean partial = false;
        for (DashboardOverviewResponse.DistributionItem item : items) {
            if (item.required() <= 0) {
                continue;
            }

            MonthlyEntitlement entitlement = entitlements.stream()
                    .filter(entry -> entry.getCommodityName().equalsIgnoreCase(item.commodity()))
                    .findFirst()
                    .orElseThrow();
            DepotStock stock = stocks.stream()
                    .filter(entry -> entry.getCommodityName().equalsIgnoreCase(item.commodity()))
                    .findFirst()
                    .orElseThrow();

            stock.setAvailableQuantity(stock.getAvailableQuantity().subtract(BigDecimal.valueOf(item.given())));
            stock.setReservedPendingQuantity(BigDecimal.valueOf(item.pending()));
            stock.setLastUpdatedAt(LocalDateTime.now());
            entitlement.setIssuedQuantity(entitlement.getIssuedQuantity().add(BigDecimal.valueOf(item.given())));
            entitlement.setPendingQuantity(BigDecimal.valueOf(item.pending()));
            entitlement.setStatus(item.pending() > 0 ? "PARTIAL" : "COMPLETED");

            depotStockRepository.save(stock);
            monthlyEntitlementRepository.save(entitlement);
            partial = partial || item.pending() > 0;
        }

        LocalDateTime now = LocalDateTime.now();
        DistributionTransaction transaction = distributionTransactionRepository.save(DistributionTransaction.builder()
                .transactionNo("TXN-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .rationCard(rationCard)
                .depot(context.depot())
                .dealer(dealer)
                .distributionMonth(currentMonth)
                .status(partial ? "PARTIAL_ISSUED" : "COMPLETED")
                .verificationMode("Dealer QR Scan + Depot Authorization + Eligibility Check")
                .issuedAt(now)
                .remarks(partial ? "Partial distribution completed because of low depot stock." : "Full monthly entitlement distributed.")
                .build());

        for (DashboardOverviewResponse.DistributionItem item : items) {
            if (item.required() <= 0) {
                continue;
            }
            transactionItemRepository.save(TransactionItem.builder()
                    .transaction(transaction)
                    .commodityName(item.commodity())
                    .entitledQuantity(BigDecimal.valueOf(item.required()))
                    .issuedQuantity(BigDecimal.valueOf(item.given()))
                    .pendingQuantity(BigDecimal.valueOf(item.pending()))
                    .build());
        }

        DashboardOverviewResponse.ReceiptPreview receipt = new DashboardOverviewResponse.ReceiptPreview(
                "RCT-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                now.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                rationCard.getQrCodeValue(),
                context.depotName(),
                "Digital receipt includes name, card number, depot, quantity given, pending quantity, and timestamp."
        );

        return new DistributionResponse(
                true,
                partial
                        ? "Distribution completed with pending balance due to low stock."
                        : "Distribution completed successfully.",
                partial ? "PARTIAL" : "FULL",
                "SMS confirmation queued (demo)",
                items,
                receipt
        );
    }

    private String formatQuantity(double quantity) {
        return BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString();
    }

    private List<MonthlyEntitlement> buildVirtualEntitlements(RationCard rationCard, int familyMembers) {
        return rationRuleRepository.findByStateAndRationCategoryIgnoreCase(rationCard.getState(), rationCard.getCategory()).stream()
                .map(rule -> MonthlyEntitlement.builder()
                        .rationCard(rationCard)
                        .entitlementMonth(YearMonth.now().toString())
                        .commodityName(rule.getCommodityName())
                        .entitledQuantity(rule.getQuantityPerPerson().multiply(BigDecimal.valueOf(familyMembers)))
                        .issuedQuantity(BigDecimal.ZERO)
                        .pendingQuantity(BigDecimal.ZERO)
                        .status("OPEN")
                        .build())
                .toList();
    }

    private List<MonthlyEntitlement> ensureMonthlyEntitlements(RationCard rationCard, String month, int familyMembers) {
        List<MonthlyEntitlement> entitlements = monthlyEntitlementRepository.findByRationCardAndEntitlementMonthOrderByCommodityName(rationCard, month);
        if (!entitlements.isEmpty()) {
            return entitlements;
        }

        List<MonthlyEntitlement> generated = rationRuleRepository.findByStateAndRationCategoryIgnoreCase(rationCard.getState(), rationCard.getCategory()).stream()
                .map(rule -> monthlyEntitlementRepository.save(MonthlyEntitlement.builder()
                        .rationCard(rationCard)
                        .entitlementMonth(month)
                        .commodityName(rule.getCommodityName())
                        .entitledQuantity(rule.getQuantityPerPerson().multiply(BigDecimal.valueOf(familyMembers)))
                        .issuedQuantity(BigDecimal.ZERO)
                        .pendingQuantity(BigDecimal.ZERO)
                        .status("OPEN")
                        .build()))
                .toList();
        return generated;
    }

    private List<DashboardOverviewResponse.CityInsight> buildCityInsights(String currentMonth) {
        return cityRepository.findAllByActiveTrue().stream()
                .map(city -> {
                    List<RationCard> cards = rationCardRepository.findByCity(city);
                    List<MonthlyEntitlement> entitlements = cards.isEmpty()
                            ? List.of()
                            : monthlyEntitlementRepository.findByRationCardInAndEntitlementMonth(cards, currentMonth);
                    double riceRequirement = sumCommodity(entitlements, "Rice");
                    double wheatRequirement = sumCommodity(entitlements, "Wheat");
                    int lowStockDepots = (int) depotRepository.findByCity(city).stream()
                            .filter(depot -> depotStockRepository.findByDepotOrderByCommodityName(depot).stream().anyMatch(this::isLowStock))
                            .count();
                    return new DashboardOverviewResponse.CityInsight(
                            city.getCityName(),
                            city.getPopulation(),
                            (int) rationCardRepository.countByCity(city),
                            riceRequirement,
                            wheatRequirement,
                            lowStockDepots
                    );
                })
                .toList();
    }

    private DashboardOverviewResponse.AdminAnalytics buildAdminAnalytics(String currentMonth) {
        long totalMembers = rationCardRepository.findAll().stream()
                .mapToLong(card -> familyMemberRepository.countByRationCardAndActiveTrue(card))
                .sum();
        double totalWheatRequired = cityRepository.findAllByActiveTrue().stream()
                .mapToDouble(city -> sumCommodity(
                        monthlyEntitlementRepository.findByRationCardInAndEntitlementMonth(rationCardRepository.findByCity(city), currentMonth),
                        "Wheat"))
                .sum();
        double totalStockAvailable = depotStockRepository.findAll().stream()
                .map(DepotStock::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
        double totalDistributed = transactionItemRepository.findAll().stream()
                .map(TransactionItem::getIssuedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
        double currentMonthDemand = cityRepository.findAllByActiveTrue().stream()
                .mapToDouble(city -> monthlyEntitlementRepository.findByRationCardInAndEntitlementMonth(rationCardRepository.findByCity(city), currentMonth).stream()
                        .map(MonthlyEntitlement::getEntitledQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .doubleValue())
                .sum();
        String previousMonth = YearMonth.now().minusMonths(1).toString();
        double previousMonthDemand = cityRepository.findAllByActiveTrue().stream()
                .mapToDouble(city -> monthlyEntitlementRepository.findByRationCardInAndEntitlementMonth(rationCardRepository.findByCity(city), previousMonth).stream()
                        .map(MonthlyEntitlement::getEntitledQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .doubleValue())
                .sum();

        return new DashboardOverviewResponse.AdminAnalytics(
                totalMembers,
                totalWheatRequired,
                totalStockAvailable,
                totalDistributed,
                currentMonthDemand,
                previousMonthDemand,
                trendLabel(currentMonthDemand, previousMonthDemand, "Demand"),
                trendLabel(totalDistributed, totalStockAvailable, "Usage")
        );
    }

    private List<DashboardOverviewResponse.CityDemandInsight> buildCityDemandInsights(String currentMonth) {
        return cityRepository.findAllByActiveTrue().stream()
                .map(city -> {
                    List<RationCard> cards = rationCardRepository.findByCity(city);
                    List<MonthlyEntitlement> entitlements = cards.isEmpty()
                            ? List.of()
                            : monthlyEntitlementRepository.findByRationCardInAndEntitlementMonth(cards, currentMonth);
                    long population = cards.stream()
                            .mapToLong(card -> familyMemberRepository.countByRationCardAndActiveTrue(card))
                            .sum();
                    double wheatRequired = sumCommodity(entitlements, "Wheat");
                    double available = depotRepository.findByCity(city).stream()
                            .flatMap(depot -> depotStockRepository.findByDepotOrderByCommodityName(depot).stream())
                            .filter(stock -> stock.getCommodityName().equalsIgnoreCase("Wheat"))
                            .map(DepotStock::getAvailableQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .doubleValue();
                    double wheatPerPerson = population == 0 ? 0 : wheatRequired / population;
                    double shortageOrSurplus = available - wheatRequired;
                    return new DashboardOverviewResponse.CityDemandInsight(
                            city.getCityName(),
                            population,
                            wheatPerPerson,
                            wheatRequired,
                            available,
                            shortageOrSurplus,
                            shortageOrSurplus < 0 ? "Shortage" : "Surplus"
                    );
                })
                .toList();
    }

    private List<DashboardOverviewResponse.DepotInsight> buildDepotInsights(String currentMonth) {
        return depotRepository.findAll().stream()
                .map(depot -> {
                    double totalStock = depotStockRepository.findByDepotOrderByCommodityName(depot).stream()
                            .map(DepotStock::getAvailableQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .doubleValue();
                    double distributed = distributionTransactionRepository.findAll().stream()
                            .filter(transaction -> transaction.getDepot().getId().equals(depot.getId()))
                            .filter(transaction -> transaction.getDistributionMonth().equals(currentMonth))
                            .map(transaction -> transactionItemRepository.findByTransaction(transaction).stream()
                                    .map(TransactionItem::getIssuedQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .doubleValue();
                    return new DashboardOverviewResponse.DepotInsight(
                            depot.getDepotCode(),
                            depot.getDepotName(),
                            depot.getCity().getCityName(),
                            totalStock + distributed,
                            distributed,
                            totalStock,
                            totalStock < distributed * 0.5 ? "Watch" : "Healthy"
                    );
                })
                .toList();
    }

    private DashboardOverviewResponse.FraudSummary buildFraudSummary() {
        long blocked = distributionTransactionRepository.findAll().stream()
                .filter(transaction -> "BLOCKED".equalsIgnoreCase(transaction.getStatus()))
                .count();
        long wrongDepot = distributionTransactionRepository.findAll().stream()
                .filter(transaction -> "BLOCKED".equalsIgnoreCase(transaction.getStatus()))
                .filter(transaction -> transaction.getRemarks() != null && transaction.getRemarks().toLowerCase().contains("unauthorized"))
                .count();
        long duplicate = distributionTransactionRepository.findAll().stream()
                .filter(transaction -> "BLOCKED".equalsIgnoreCase(transaction.getStatus()))
                .filter(transaction -> transaction.getRemarks() != null && transaction.getRemarks().toLowerCase().contains("duplicate"))
                .count();
        return new DashboardOverviewResponse.FraudSummary(blocked, wrongDepot, duplicate);
    }

    private List<DashboardOverviewResponse.ActivityLog> buildActivityLogs() {
        List<DashboardOverviewResponse.ActivityLog> logs = new ArrayList<>();
        distributionTransactionRepository.findTop3ByOrderByIssuedAtDesc().forEach(transaction ->
                logs.add(new DashboardOverviewResponse.ActivityLog(
                        transaction.getIssuedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                        "BLOCKED".equalsIgnoreCase(transaction.getStatus()) ? "warning" : "success",
                        "Transaction",
                        ("BLOCKED".equalsIgnoreCase(transaction.getStatus()) ? "Unauthorized access blocked" : "Ration distributed")
                                + " - " + transaction.getDepot().getDepotCode()
                )));
        depotStockRepository.findAll().stream()
                .sorted(Comparator.comparing(DepotStock::getLastUpdatedAt).reversed())
                .limit(3)
                .forEach(stock -> logs.add(new DashboardOverviewResponse.ActivityLog(
                        stock.getLastUpdatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                        "info",
                        "Stock Update",
                        stock.getCommodityName() + " stock refreshed at " + stock.getDepot().getDepotCode()
                )));
        return logs.stream()
                .sorted(Comparator.comparing(DashboardOverviewResponse.ActivityLog::timestamp).reversed())
                .limit(6)
                .toList();
    }

    private List<String> buildSmartInsights(String currentMonth) {
        List<String> insights = new ArrayList<>();
        buildCityDemandInsights(currentMonth).stream()
                .filter(city -> city.shortageOrSurplus() < 0)
                .findFirst()
                .ifPresent(city -> insights.add("Stock shortage predicted in " + city.cityName()));
        buildDepotInsights(currentMonth).stream()
                .max(Comparator.comparing(DashboardOverviewResponse.DepotInsight::distributedQuantity))
                .ifPresent(depot -> insights.add("High demand in Depot " + depot.depotCode()));
        monthlyEntitlementRepository.findAll().stream()
                .filter(item -> item.getPendingQuantity().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .ifPresent(item -> insights.add("Unclaimed ration expiring soon for " + item.getRationCard().getRationCardNo()));
        if (insights.isEmpty()) {
            insights.add("Demand and stock are currently balanced across monitored depots.");
        }
        return insights;
    }

    private double sumCommodity(List<MonthlyEntitlement> entitlements, String commodity) {
        return entitlements.stream()
                .filter(entitlement -> entitlement.getCommodityName().equalsIgnoreCase(commodity))
                .map(MonthlyEntitlement::getEntitledQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    private List<DashboardOverviewResponse.TransactionView> buildRecentTransactions() {
        return distributionTransactionRepository.findTop3ByOrderByIssuedAtDesc().stream()
                .map(transaction -> new DashboardOverviewResponse.TransactionView(
                        transaction.getTransactionNo(),
                        transaction.getStatus().equalsIgnoreCase("BLOCKED")
                                ? "Depot mismatch blocked"
                                : transaction.getRationCard().getHeadOfFamily(),
                        transaction.getStatus().equalsIgnoreCase("BLOCKED")
                                ? "Central Monitoring Rule"
                                : transaction.getDepot().getDepotName(),
                        transaction.getStatus(),
                        transaction.getVerificationMode(),
                        transaction.getIssuedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        transaction.getRemarks()
                ))
                .toList();
    }

    private DashboardOverviewResponse.ReceiptPreview buildReceiptPreview(RationCard rationCard) {
        DistributionTransaction latest = rationCard == null
                ? distributionTransactionRepository.findTop3ByOrderByIssuedAtDesc().stream().findFirst().orElse(null)
                : distributionTransactionRepository.findTopByRationCardOrderByIssuedAtDesc(rationCard).orElse(null);
        if (latest == null) {
            return new DashboardOverviewResponse.ReceiptPreview("N/A", "N/A", "N/A", "N/A", "No distribution receipt generated yet.");
        }
        return new DashboardOverviewResponse.ReceiptPreview(
                "RCT-" + latest.getTransactionNo().substring(latest.getTransactionNo().length() - 8),
                latest.getIssuedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                latest.getRationCard().getQrCodeValue(),
                latest.getDepot().getDepotName(),
                "Digital receipt includes issued quantity, pending quantity, dealer identity, and timestamp."
        );
    }

    private List<DashboardOverviewResponse.AlertItem> buildAlerts(
            Role role,
            List<DepotStock> stocks,
            List<MonthlyEntitlement> entitlements
    ) {
        BigDecimal pending = entitlements.stream()
                .map(MonthlyEntitlement::getPendingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long lowStockCount = stocks.stream().filter(this::isLowStock).count();

        if (role == Role.ROLE_ADMIN) {
            long citiesWithRisk = cityRepository.findAllByActiveTrue().stream()
                    .filter(city -> depotRepository.findByCity(city).stream()
                            .anyMatch(depot -> depotStockRepository.findByDepotOrderByCommodityName(depot).stream().anyMatch(this::isLowStock)))
                    .count();
            return List.of(
                    new DashboardOverviewResponse.AlertItem("high", "Depot stock imbalance", citiesWithRisk + " cities have at least one depot below comfortable demand coverage."),
                    new DashboardOverviewResponse.AlertItem("medium", "Expired unclaimed entitlement", pending.stripTrailingZeros().toPlainString() + " kg remains pending and will return to city stock if unclaimed.")
            );
        }

        if (role == Role.ROLE_DEALER) {
            return List.of(
                    new DashboardOverviewResponse.AlertItem(lowStockCount > 0 ? "medium" : "low", "Sugar running low", lowStockCount > 0
                            ? lowStockCount + " commodity lines at this depot need replenishment attention."
                            : "Depot stock is currently above the configured watch threshold."),
                    new DashboardOverviewResponse.AlertItem("low", "Pending claims queue", pending.stripTrailingZeros().toPlainString() + " kg is pending and will auto-resume after the next stock inward entry.")
            );
        }

        return List.of(
                new DashboardOverviewResponse.AlertItem(pending.compareTo(BigDecimal.ZERO) > 0 ? "medium" : "low", "Partial issue recorded",
                        pending.compareTo(BigDecimal.ZERO) > 0
                                ? pending.stripTrailingZeros().toPlainString() + " kg remains pending for this month's entitlement."
                                : "No pending balance is left for the current entitlement cycle."),
                new DashboardOverviewResponse.AlertItem("low", "Depot lock active", "Your ration card can only be served at the assigned mapped depot.")
        );
    }

    private boolean isLowStock(DepotStock stock) {
        return stock.getAvailableQuantity().compareTo(stock.getMonthlyRequiredQuantity().multiply(BigDecimal.valueOf(0.6))) < 0;
    }

    private String stockStatus(DepotStock stock) {
        if (stock.getAvailableQuantity().compareTo(stock.getMonthlyRequiredQuantity().multiply(BigDecimal.valueOf(0.5))) < 0) {
            return "Critical";
        }
        if (isLowStock(stock)) {
            return "Watch";
        }
        return "Healthy";
    }

    private String trendLabel(double current, double baseline, String label) {
        if (baseline == 0 && current > 0) {
            return label + " ↑";
        }
        if (current > baseline) {
            return label + " ↑";
        }
        if (current < baseline) {
            return label + " ↓";
        }
        return label + " →";
    }

    private record Context(
            StateUnit state,
            String cityName,
            String depotCode,
            String depotName,
            Depot depot,
            RationCard rationCard,
            String rationCategory
    ) {
    }
}
