package com.smartration.backend.service.impl;

import com.smartration.backend.dto.InventoryRequest;
import com.smartration.backend.dto.InventoryResponse;
import com.smartration.backend.entity.Inventory;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.InventoryRepository;
import com.smartration.backend.service.InventoryService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public InventoryResponse addStock(InventoryRequest request) {
        Inventory inventory = Inventory.builder()
                .rice(request.getRice())
                .wheat(request.getWheat())
                .sugar(request.getSugar())
                .updatedAt(LocalDateTime.now())
                .build();
        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse updateStock(Long inventoryId, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        inventory.setRice(request.getRice());
        inventory.setWheat(request.getWheat());
        inventory.setSugar(request.getSugar());
        inventory.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse getCurrentStock() {
        Inventory inventory = inventoryRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record found"));
        return mapToResponse(inventory);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .rice(inventory.getRice())
                .wheat(inventory.getWheat())
                .sugar(inventory.getSugar())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
