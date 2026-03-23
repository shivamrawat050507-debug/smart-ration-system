package com.smartration.backend.repository;

import com.smartration.backend.entity.City;
import com.smartration.backend.entity.Depot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepotRepository extends JpaRepository<Depot, Long> {

    Optional<Depot> findByDepotCode(String depotCode);

    List<Depot> findByCity(City city);
}
