package com.smartration.backend.service;

import com.smartration.backend.dto.DashboardOverviewResponse;
import com.smartration.backend.dto.DealerScanRequest;
import com.smartration.backend.dto.DealerScanResponse;
import com.smartration.backend.dto.DistributionResponse;
import com.smartration.backend.dto.AddCityRequest;
import com.smartration.backend.dto.AddDepotRequest;
import com.smartration.backend.dto.AllocateStockRequest;
import com.smartration.backend.dto.MessageResponse;
import com.smartration.backend.dto.UpdateRationRuleRequest;

public interface DashboardService {

    DashboardOverviewResponse getOverview(Long userId);

    DistributionResponse distributeRation(Long userId);

    DealerScanResponse scanRationCard(Long dealerUserId, DealerScanRequest request);

    DistributionResponse distributeRationForDealer(Long dealerUserId, DealerScanRequest request);

    MessageResponse allocateStock(AllocateStockRequest request);

    MessageResponse updateRationRule(UpdateRationRuleRequest request);

    MessageResponse addCity(AddCityRequest request);

    MessageResponse addDepot(AddDepotRequest request);
}
