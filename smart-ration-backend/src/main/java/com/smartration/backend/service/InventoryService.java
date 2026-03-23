package com.smartration.backend.service;

import com.smartration.backend.dto.InventoryRequest;
import com.smartration.backend.dto.InventoryResponse;

public interface InventoryService {

    InventoryResponse addStock(InventoryRequest request);

    InventoryResponse updateStock(Long inventoryId, InventoryRequest request);

    InventoryResponse getCurrentStock();
}
