package com.smartration.backend.config;

import com.smartration.backend.entity.Inventory;
import com.smartration.backend.entity.Product;
import com.smartration.backend.entity.Role;
import com.smartration.backend.entity.Shopkeeper;
import com.smartration.backend.entity.User;
import com.smartration.backend.repository.InventoryRepository;
import com.smartration.backend.repository.ProductRepository;
import com.smartration.backend.repository.ShopkeeperRepository;
import com.smartration.backend.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ShopkeeperRepository shopkeeperRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (shopkeeperRepository.count() == 0) {
            Shopkeeper shopkeeper = Shopkeeper.builder()
                    .name("Default Shopkeeper")
                    .username("shopkeeper1")
                    .password(passwordEncoder.encode("password123"))
                    .build();
            shopkeeperRepository.save(shopkeeper);
        }

        if (inventoryRepository.count() == 0) {
            Inventory inventory = Inventory.builder()
                    .rice(100)
                    .wheat(80)
                    .sugar(50)
                    .updatedAt(LocalDateTime.now())
                    .build();
            inventoryRepository.save(inventory);
        }

        if (!userRepository.existsByRationCardNumber("ADMIN001")) {
            User admin = User.builder()
                    .name("System Admin")
                    .rationCardNumber("ADMIN001")
                    .phone("9999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder()
                    .name("Rice")
                    .description("Essential ration rice")
                    .stockQuantity(100)
                    .unit("kg")
                    .managedByRole(Role.ROLE_ADMIN)
                    .build());
            productRepository.save(Product.builder()
                    .name("Wheat")
                    .description("Essential ration wheat")
                    .stockQuantity(80)
                    .unit("kg")
                    .managedByRole(Role.ROLE_ADMIN)
                    .build());
            productRepository.save(Product.builder()
                    .name("Sugar")
                    .description("Essential ration sugar")
                    .stockQuantity(50)
                    .unit("kg")
                    .managedByRole(Role.ROLE_ADMIN)
                    .build());
        }
    }
}
