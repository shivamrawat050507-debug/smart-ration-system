package com.smartration.backend.repository;

import com.smartration.backend.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByRationCardNumber(String rationCardNumber);

    boolean existsByPhone(String phone);

    Optional<User> findByRationCardNumber(String rationCardNumber);

    Optional<User> findByPhone(String phone);
}
