package com.smartration.backend.repository;

import com.smartration.backend.entity.StateUnit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateUnitRepository extends JpaRepository<StateUnit, Long> {

    Optional<StateUnit> findByStateCode(String stateCode);
}
