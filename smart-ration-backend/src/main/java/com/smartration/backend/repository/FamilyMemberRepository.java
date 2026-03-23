package com.smartration.backend.repository;

import com.smartration.backend.entity.FamilyMember;
import com.smartration.backend.entity.RationCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    long countByRationCardAndActiveTrue(RationCard rationCard);

    List<FamilyMember> findByRationCardAndActiveTrue(RationCard rationCard);
}
