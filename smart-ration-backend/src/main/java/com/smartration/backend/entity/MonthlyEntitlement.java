package com.smartration.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monthly_entitlements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ration_card_id")
    private RationCard rationCard;

    @Column(nullable = false)
    private String entitlementMonth;

    @Column(nullable = false)
    private String commodityName;

    @Column(nullable = false)
    private BigDecimal entitledQuantity;

    @Column(nullable = false)
    private BigDecimal issuedQuantity;

    @Column(nullable = false)
    private BigDecimal pendingQuantity;

    @Column(nullable = false)
    @Builder.Default
    private String status = "OPEN";
}
