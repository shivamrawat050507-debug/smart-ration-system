package com.smartration.backend.repository;

import com.smartration.backend.entity.City;
import com.smartration.backend.entity.RationCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RationCardRepository extends JpaRepository<RationCard, Long> {

    Optional<RationCard> findByRationCardNo(String rationCardNo);

    Optional<RationCard> findByQrCodeValue(String qrCodeValue);

    List<RationCard> findByCity(City city);

    long countByCity(City city);
}
