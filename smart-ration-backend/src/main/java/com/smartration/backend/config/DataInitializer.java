package com.smartration.backend.config;

import com.smartration.backend.entity.City;
import com.smartration.backend.entity.Dealer;
import com.smartration.backend.entity.Depot;
import com.smartration.backend.entity.DepotStock;
import com.smartration.backend.entity.DistributionTransaction;
import com.smartration.backend.entity.FamilyMember;
import com.smartration.backend.entity.Inventory;
import com.smartration.backend.entity.MonthlyEntitlement;
import com.smartration.backend.entity.Product;
import com.smartration.backend.entity.RationCard;
import com.smartration.backend.entity.RationRule;
import com.smartration.backend.entity.Role;
import com.smartration.backend.entity.Shopkeeper;
import com.smartration.backend.entity.StateUnit;
import com.smartration.backend.entity.TransactionItem;
import com.smartration.backend.entity.User;
import com.smartration.backend.repository.CityRepository;
import com.smartration.backend.repository.DealerRepository;
import com.smartration.backend.repository.DepotRepository;
import com.smartration.backend.repository.DepotStockRepository;
import com.smartration.backend.repository.DistributionTransactionRepository;
import com.smartration.backend.repository.FamilyMemberRepository;
import com.smartration.backend.repository.InventoryRepository;
import com.smartration.backend.repository.MonthlyEntitlementRepository;
import com.smartration.backend.repository.ProductRepository;
import com.smartration.backend.repository.RationCardRepository;
import com.smartration.backend.repository.RationRuleRepository;
import com.smartration.backend.repository.ShopkeeperRepository;
import com.smartration.backend.repository.StateUnitRepository;
import com.smartration.backend.repository.TransactionItemRepository;
import com.smartration.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ShopkeeperRepository shopkeeperRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StateUnitRepository stateUnitRepository;
    private final CityRepository cityRepository;
    private final DepotRepository depotRepository;
    private final DealerRepository dealerRepository;
    private final RationCardRepository rationCardRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final RationRuleRepository rationRuleRepository;
    private final DepotStockRepository depotStockRepository;
    private final MonthlyEntitlementRepository monthlyEntitlementRepository;
    private final DistributionTransactionRepository distributionTransactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedLegacyDemoData();
        seedUsers();
        seedRationDomain();
        backfillRegisteredUsersIntoRationDomain();
    }

    private void seedLegacyDemoData() {
        if (shopkeeperRepository.count() == 0) {
            shopkeeperRepository.save(Shopkeeper.builder()
                    .name("Default Shopkeeper")
                    .username("shopkeeper1")
                    .password(passwordEncoder.encode("password123"))
                    .build());
        }

        if (inventoryRepository.count() == 0) {
            inventoryRepository.save(Inventory.builder()
                    .rice(100)
                    .wheat(80)
                    .sugar(50)
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder()
                    .name("Rice")
                    .description("Essential ration rice")
                    .stockQuantity(100)
                    .unit("kg")
                    .managedByRole(Role.ROLE_ADMIN)
                    .build());
            productRepository.save(Product.builder()
                    .name("Wheat")
                    .description("Essential ration wheat")
                    .stockQuantity(80)
                    .unit("kg")
                    .managedByRole(Role.ROLE_ADMIN)
                    .build());
            productRepository.save(Product.builder()
                    .name("Sugar")
                    .description("Essential ration sugar")
                    .stockQuantity(50)
                    .unit("kg")
                    .managedByRole(Role.ROLE_ADMIN)
                    .build());
        }
    }

    private void seedUsers() {
        saveUserIfAbsent("ADMIN001", "System Admin", "9999999999", "admin123", Role.ROLE_ADMIN);
        saveUserIfAbsent("HR-GRG-4421", "Ravi Kumar", "9876543210", "password123", Role.ROLE_USER);
        saveUserIfAbsent("DLR-GRG-014", "Meena Sharma", "9123456780", "password123", Role.ROLE_DEALER);
    }

    private void saveUserIfAbsent(String rationCardNumber, String name, String phone, String password, Role role) {
        if (!userRepository.existsByRationCardNumber(rationCardNumber) && !userRepository.existsByPhone(phone)) {
            userRepository.save(User.builder()
                    .name(name)
                    .rationCardNumber(rationCardNumber)
                    .phone(phone)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .build());
        }
    }

    private void seedRationDomain() {
        StateUnit haryana = stateUnitRepository.findByStateCode("HR")
                .orElseGet(() -> stateUnitRepository.save(StateUnit.builder()
                        .stateCode("HR")
                        .stateName("Haryana")
                        .active(true)
                        .build()));

        City gurugram = saveCityIfAbsent("GRG", "Gurugram", 184500, haryana);
        City faridabad = saveCityIfAbsent("FBD", "Faridabad", 213300, haryana);
        City hisar = saveCityIfAbsent("HSR", "Hisar", 126800, haryana);

        Depot gurugramDepot = saveDepotIfAbsent("GRG-D014", "Sector 14 Fair Price Depot", "Ward 14, Gurugram", gurugram);
        Depot faridabadDepot = saveDepotIfAbsent("FBD-D022", "NIT Fair Price Depot", "NIT 2, Faridabad", faridabad);
        Depot hisarDepot = saveDepotIfAbsent("HSR-D009", "Model Town Fair Price Depot", "Model Town, Hisar", hisar);

        Dealer gurugramDealer = saveDealerIfAbsent(gurugramDepot, "Meena Sharma", "9123456780", "DLR-GRG-014");
        Dealer faridabadDealer = saveDealerIfAbsent(faridabadDepot, "Sandeep Yadav", "9123456781", "DLR-FBD-022");
        Dealer hisarDealer = saveDealerIfAbsent(hisarDepot, "Asha Malik", "9123456782", "DLR-HSR-009");

        RationCard gurugramCard = saveCardIfAbsent(gurugramDepot, gurugram, haryana, "HR-GRG-4421", "QR-GRG-D014-RC4421", "Ravi Kumar", "BPL", "9876543210");
        RationCard faridabadCard = saveCardIfAbsent(faridabadDepot, faridabad, haryana, "HR-FBD-2188", "QR-FBD-D022-RC2188", "Sunita Devi", "BPL", "9876543211");
        RationCard hisarCard = saveCardIfAbsent(hisarDepot, hisar, haryana, "HR-HSR-8765", "QR-HSR-D009-RC8765", "Mohan Lal", "APL", "9876543212");

        saveFamilyIfAbsent(gurugramCard, List.of(
                member("Ravi Kumar", "HEAD", 41),
                member("Pooja Kumari", "SPOUSE", 37),
                member("Anjali", "DAUGHTER", 16),
                member("Rohan", "SON", 12),
                member("Kamla Devi", "MOTHER", 66)
        ));
        saveFamilyIfAbsent(faridabadCard, List.of(
                member("Sunita Devi", "HEAD", 44),
                member("Mahesh", "SPOUSE", 48),
                member("Kiran", "DAUGHTER", 18),
                member("Vivek", "SON", 14),
                member("Sarla", "MOTHER", 70)
        ));
        saveFamilyIfAbsent(hisarCard, List.of(
                member("Mohan Lal", "HEAD", 46),
                member("Seema", "SPOUSE", 39),
                member("Deepak", "SON", 17)
        ));

        saveRuleIfAbsent(haryana, "BPL", "Rice", "kg", 5);
        saveRuleIfAbsent(haryana, "BPL", "Wheat", "kg", 2);
        saveRuleIfAbsent(haryana, "BPL", "Sugar", "kg", 1);
        saveRuleIfAbsent(haryana, "APL", "Rice", "kg", 3);
        saveRuleIfAbsent(haryana, "APL", "Wheat", "kg", 2);
        saveRuleIfAbsent(haryana, "APL", "Sugar", "kg", 0.5);

        saveStockIfAbsent(gurugramDepot, "Rice", 1260, 120, 1480);
        saveStockIfAbsent(gurugramDepot, "Wheat", 980, 95, 1325);
        saveStockIfAbsent(gurugramDepot, "Sugar", 245, 60, 410);
        saveStockIfAbsent(faridabadDepot, "Rice", 980, 90, 1065);
        saveStockIfAbsent(faridabadDepot, "Wheat", 720, 50, 640);
        saveStockIfAbsent(faridabadDepot, "Sugar", 180, 70, 360);
        saveStockIfAbsent(hisarDepot, "Rice", 540, 20, 634);
        saveStockIfAbsent(hisarDepot, "Wheat", 400, 15, 380);
        saveStockIfAbsent(hisarDepot, "Sugar", 140, 5, 160);

        String month = YearMonth.now().toString();
        saveEntitlementIfAbsent(gurugramCard, month, "Rice", 25, 20, 5);
        saveEntitlementIfAbsent(gurugramCard, month, "Wheat", 10, 10, 0);
        saveEntitlementIfAbsent(gurugramCard, month, "Sugar", 5, 1, 4);
        saveEntitlementIfAbsent(faridabadCard, month, "Rice", 25, 12, 13);
        saveEntitlementIfAbsent(faridabadCard, month, "Wheat", 10, 7, 3);
        saveEntitlementIfAbsent(faridabadCard, month, "Sugar", 5, 0, 5);
        saveEntitlementIfAbsent(hisarCard, month, "Rice", 9, 9, 0);
        saveEntitlementIfAbsent(hisarCard, month, "Wheat", 6, 6, 0);
        saveEntitlementIfAbsent(hisarCard, month, "Sugar", 1.5, 1.5, 0);

        DistributionTransaction t1 = saveTransactionIfAbsent("TXN-20260323-001", gurugramCard, gurugramDepot, gurugramDealer, month, "PARTIAL_ISSUED", "QR Scan",
                LocalDateTime.of(2026, 3, 23, 9, 20), "Rice and wheat issued, sugar pending because depot stock fell below monthly demand.");
        DistributionTransaction t2 = saveTransactionIfAbsent("TXN-20260322-118", hisarCard, hisarDepot, hisarDealer, month, "COMPLETED", "QR + OTP",
                LocalDateTime.of(2026, 3, 22, 16, 5), "Full monthly entitlement issued and receipt generated.");
        DistributionTransaction t3 = saveTransactionIfAbsent("TXN-20260322-094", faridabadCard, faridabadDepot, faridabadDealer, month, "BLOCKED", "Fraud Guard",
                LocalDateTime.of(2026, 3, 22, 11, 42), "Attempted collection from an unauthorized depot was rejected.");

        saveTransactionItemIfAbsent(t1, "Rice", 25, 20, 5);
        saveTransactionItemIfAbsent(t1, "Wheat", 10, 10, 0);
        saveTransactionItemIfAbsent(t1, "Sugar", 5, 1, 4);
        saveTransactionItemIfAbsent(t2, "Rice", 9, 9, 0);
        saveTransactionItemIfAbsent(t2, "Wheat", 6, 6, 0);
        saveTransactionItemIfAbsent(t2, "Sugar", 1.5, 1.5, 0);
        saveTransactionItemIfAbsent(t3, "Rice", 25, 0, 25);
    }

    private City saveCityIfAbsent(String code, String name, int population, StateUnit state) {
        return cityRepository.findByCityCode(code)
                .orElseGet(() -> cityRepository.save(City.builder()
                        .cityCode(code)
                        .cityName(name)
                        .population(population)
                        .state(state)
                        .active(true)
                        .build()));
    }

    private Depot saveDepotIfAbsent(String code, String name, String address, City city) {
        return depotRepository.findByDepotCode(code)
                .orElseGet(() -> depotRepository.save(Depot.builder()
                        .depotCode(code)
                        .depotName(name)
                        .address(address)
                        .city(city)
                        .active(true)
                        .build()));
    }

    private Dealer saveDealerIfAbsent(Depot depot, String fullName, String mobile, String username) {
        return dealerRepository.findByUsernameOrMobile(username, mobile)
                .orElseGet(() -> dealerRepository.save(Dealer.builder()
                        .depot(depot)
                        .fullName(fullName)
                        .mobile(mobile)
                        .username(username)
                        .passwordHash(passwordEncoder.encode("password123"))
                        .status("ACTIVE")
                        .build()));
    }

    private RationCard saveCardIfAbsent(Depot depot, City city, StateUnit state, String cardNo, String qr, String head, String category, String mobile) {
        return rationCardRepository.findByRationCardNo(cardNo)
                .orElseGet(() -> rationCardRepository.save(RationCard.builder()
                        .depot(depot)
                        .city(city)
                        .state(state)
                        .rationCardNo(cardNo)
                        .qrCodeValue(qr)
                        .headOfFamily(head)
                        .category(category)
                        .mobile(mobile)
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .build()));
    }

    private void saveFamilyIfAbsent(RationCard rationCard, List<FamilyMember> members) {
        if (familyMemberRepository.countByRationCardAndActiveTrue(rationCard) == 0) {
            familyMemberRepository.saveAll(members.stream()
                    .map(member -> FamilyMember.builder()
                            .rationCard(rationCard)
                            .memberName(member.getMemberName())
                            .relationType(member.getRelationType())
                            .age(member.getAge())
                            .active(member.isActive())
                            .build())
                    .toList());
        }
    }

    private FamilyMember member(String name, String relation, int age) {
        return FamilyMember.builder()
                .memberName(name)
                .relationType(relation)
                .age(age)
                .active(true)
                .build();
    }

    private void saveRuleIfAbsent(StateUnit state, String category, String commodity, String unit, double quantity) {
        boolean exists = rationRuleRepository.findByStateAndRationCategoryIgnoreCase(state, category).stream()
                .anyMatch(rule -> rule.getCommodityName().equalsIgnoreCase(commodity));
        if (!exists) {
            rationRuleRepository.save(RationRule.builder()
                    .state(state)
                    .rationCategory(category)
                    .commodityName(commodity)
                    .unit(unit)
                    .quantityPerPerson(BigDecimal.valueOf(quantity))
                    .effectiveFrom(LocalDate.of(2026, 1, 1))
                    .build());
        }
    }

    private void saveStockIfAbsent(Depot depot, String commodity, double available, double pending, double required) {
        if (depotStockRepository.findByDepotAndCommodityNameIgnoreCase(depot, commodity).isEmpty()) {
            depotStockRepository.save(DepotStock.builder()
                    .depot(depot)
                    .commodityName(commodity)
                    .availableQuantity(BigDecimal.valueOf(available))
                    .reservedPendingQuantity(BigDecimal.valueOf(pending))
                    .monthlyRequiredQuantity(BigDecimal.valueOf(required))
                    .lastUpdatedAt(LocalDateTime.now())
                    .build());
        }
    }

    private void saveEntitlementIfAbsent(RationCard rationCard, String month, String commodity, double entitled, double issued, double pending) {
        boolean exists = monthlyEntitlementRepository.findByRationCardAndEntitlementMonthOrderByCommodityName(rationCard, month).stream()
                .anyMatch(item -> item.getCommodityName().equalsIgnoreCase(commodity));
        if (!exists) {
            monthlyEntitlementRepository.save(MonthlyEntitlement.builder()
                    .rationCard(rationCard)
                    .entitlementMonth(month)
                    .commodityName(commodity)
                    .entitledQuantity(BigDecimal.valueOf(entitled))
                    .issuedQuantity(BigDecimal.valueOf(issued))
                    .pendingQuantity(BigDecimal.valueOf(pending))
                    .status(pending > 0 ? "PARTIAL" : "OPEN")
                    .build());
        }
    }

    private DistributionTransaction saveTransactionIfAbsent(
            String transactionNo,
            RationCard rationCard,
            Depot depot,
            Dealer dealer,
            String month,
            String status,
            String mode,
            LocalDateTime issuedAt,
            String remarks
    ) {
        return distributionTransactionRepository.findByTransactionNo(transactionNo)
                .orElseGet(() -> distributionTransactionRepository.save(DistributionTransaction.builder()
                        .transactionNo(transactionNo)
                        .rationCard(rationCard)
                        .depot(depot)
                        .dealer(dealer)
                        .distributionMonth(month)
                        .status(status)
                        .verificationMode(mode)
                        .issuedAt(issuedAt)
                        .remarks(remarks)
                        .build()));
    }

    private void saveTransactionItemIfAbsent(DistributionTransaction transaction, String commodity, double entitled, double issued, double pending) {
        boolean exists = transactionItemRepository.findByTransaction(transaction).stream()
                .anyMatch(item -> item.getCommodityName().equalsIgnoreCase(commodity));
        if (!exists) {
            transactionItemRepository.save(TransactionItem.builder()
                    .transaction(transaction)
                    .commodityName(commodity)
                    .entitledQuantity(BigDecimal.valueOf(entitled))
                    .issuedQuantity(BigDecimal.valueOf(issued))
                    .pendingQuantity(BigDecimal.valueOf(pending))
                    .build());
        }
    }

    private void backfillRegisteredUsersIntoRationDomain() {
        Depot defaultDepot = depotRepository.findByDepotCode("GRG-D014").orElse(null);
        StateUnit defaultState = stateUnitRepository.findByStateCode("HR").orElse(null);
        if (defaultDepot == null || defaultState == null) {
            return;
        }

        userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ROLE_USER)
                .filter(user -> rationCardRepository.findByRationCardNo(user.getRationCardNumber()).isEmpty())
                .forEach(user -> {
                    RationCard rationCard = rationCardRepository.save(RationCard.builder()
                            .state(defaultState)
                            .city(defaultDepot.getCity())
                            .depot(defaultDepot)
                            .rationCardNo(user.getRationCardNumber())
                            .qrCodeValue("QR-" + user.getRationCardNumber())
                            .headOfFamily(user.getName())
                            .category("BPL")
                            .mobile(user.getPhone())
                            .status("ACTIVE")
                            .createdAt(LocalDateTime.now())
                            .build());

                    if (familyMemberRepository.countByRationCardAndActiveTrue(rationCard) == 0) {
                        familyMemberRepository.save(FamilyMember.builder()
                                .rationCard(rationCard)
                                .memberName(user.getName())
                                .relationType("HEAD")
                                .age(30)
                                .active(true)
                                .build());
                    }
                });
    }
}
