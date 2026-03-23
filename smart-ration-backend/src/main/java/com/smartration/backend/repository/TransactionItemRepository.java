package com.smartration.backend.repository;

import com.smartration.backend.entity.DistributionTransaction;
import com.smartration.backend.entity.TransactionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {

    List<TransactionItem> findByTransaction(DistributionTransaction transaction);
}
