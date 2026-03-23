package com.smartration.backend.controller;

import com.smartration.backend.dto.DashboardOverviewResponse;
import com.smartration.backend.dto.DealerScanRequest;
import com.smartration.backend.dto.DealerScanResponse;
import com.smartration.backend.dto.DistributionResponse;
import com.smartration.backend.dto.AddCityRequest;
import com.smartration.backend.dto.AddDepotRequest;
import com.smartration.backend.dto.AllocateStockRequest;
import com.smartration.backend.dto.MessageResponse;
import com.smartration.backend.dto.UpdateRationRuleRequest;
import jakarta.validation.Valid;
import com.smartration.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview/{userId}")
    public ResponseEntity<DashboardOverviewResponse> getOverview(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.getOverview(userId));
    }

    @PostMapping("/distribute/{userId}")
    public ResponseEntity<DistributionResponse> distribute(@PathVariable Long userId) {
        return ResponseEntity.ok(dashboardService.distributeRation(userId));
    }

    @PostMapping("/dealer/scan/{userId}")
    public ResponseEntity<DealerScanResponse> scanRationCard(@PathVariable Long userId, @Valid @RequestBody DealerScanRequest request) {
        return ResponseEntity.ok(dashboardService.scanRationCard(userId, request));
    }

    @PostMapping("/dealer/distribute/{userId}")
    public ResponseEntity<DistributionResponse> distributeForDealer(@PathVariable Long userId, @Valid @RequestBody DealerScanRequest request) {
        return ResponseEntity.ok(dashboardService.distributeRationForDealer(userId, request));
    }

    @PostMapping("/admin/allocate-stock")
    public ResponseEntity<MessageResponse> allocateStock(@Valid @RequestBody AllocateStockRequest request) {
        return ResponseEntity.ok(dashboardService.allocateStock(request));
    }

    @PostMapping("/admin/update-ration-rule")
    public ResponseEntity<MessageResponse> updateRationRule(@Valid @RequestBody UpdateRationRuleRequest request) {
        return ResponseEntity.ok(dashboardService.updateRationRule(request));
    }

    @PostMapping("/admin/add-city")
    public ResponseEntity<MessageResponse> addCity(@Valid @RequestBody AddCityRequest request) {
        return ResponseEntity.ok(dashboardService.addCity(request));
    }

    @PostMapping("/admin/add-depot")
    public ResponseEntity<MessageResponse> addDepot(@Valid @RequestBody AddDepotRequest request) {
        return ResponseEntity.ok(dashboardService.addDepot(request));
    }
}
