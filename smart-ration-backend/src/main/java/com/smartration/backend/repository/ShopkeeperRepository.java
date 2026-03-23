package com.smartration.backend.repository;

import com.smartration.backend.entity.Shopkeeper;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopkeeperRepository extends JpaRepository<Shopkeeper, Long> {

    Optional<Shopkeeper> findByUsername(String username);
}
