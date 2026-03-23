package com.smartration.backend.repository;

import com.smartration.backend.entity.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByActiveTrue();

    Optional<City> findByCityCode(String cityCode);
}
