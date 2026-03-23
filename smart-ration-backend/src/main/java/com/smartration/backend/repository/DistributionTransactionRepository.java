package com.smartration.backend.repository;

import com.smartration.backend.entity.DistributionTransaction;
import com.smartration.backend.entity.RationCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionTransactionRepository extends JpaRepository<DistributionTransaction, Long> {

    List<DistributionTransaction> findTop3ByOrderByIssuedAtDesc();

    Optional<DistributionTransaction> findTopByRationCardOrderByIssuedAtDesc(RationCard rationCard);

    Optional<DistributionTransaction> findByTransactionNo(String transactionNo);
}
