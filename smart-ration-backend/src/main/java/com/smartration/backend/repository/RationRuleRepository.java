package com.smartration.backend.repository;

import com.smartration.backend.entity.RationRule;
import com.smartration.backend.entity.StateUnit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RationRuleRepository extends JpaRepository<RationRule, Long> {

    List<RationRule> findByStateAndRationCategoryIgnoreCase(StateUnit state, String rationCategory);
}
