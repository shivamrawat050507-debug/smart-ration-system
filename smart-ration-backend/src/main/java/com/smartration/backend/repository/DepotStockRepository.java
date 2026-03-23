package com.smartration.backend.repository;

import com.smartration.backend.entity.Depot;
import com.smartration.backend.entity.DepotStock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepotStockRepository extends JpaRepository<DepotStock, Long> {

    List<DepotStock> findByDepotOrderByCommodityName(Depot depot);

    Optional<DepotStock> findByDepotAndCommodityNameIgnoreCase(Depot depot, String commodityName);
}
