package com.smartration.backend.repository;

import com.smartration.backend.entity.MonthlyEntitlement;
import com.smartration.backend.entity.RationCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyEntitlementRepository extends JpaRepository<MonthlyEntitlement, Long> {

    List<MonthlyEntitlement> findByRationCardAndEntitlementMonthOrderByCommodityName(RationCard rationCard, String entitlementMonth);

    List<MonthlyEntitlement> findByRationCardInAndEntitlementMonth(List<RationCard> rationCards, String entitlementMonth);
}
