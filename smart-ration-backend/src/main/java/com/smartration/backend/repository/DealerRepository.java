package com.smartration.backend.repository;

import com.smartration.backend.entity.Dealer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    Optional<Dealer> findByUsernameOrMobile(String username, String mobile);
}
