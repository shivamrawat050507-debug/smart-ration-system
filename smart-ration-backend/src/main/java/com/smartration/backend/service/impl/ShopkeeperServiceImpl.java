package com.smartration.backend.service.impl;

import com.smartration.backend.dto.BookingResponse;
import com.smartration.backend.dto.ShopkeeperLoginRequest;
import com.smartration.backend.dto.ShopkeeperLoginResponse;
import com.smartration.backend.entity.Shopkeeper;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.repository.ShopkeeperRepository;
import com.smartration.backend.service.BookingService;
import com.smartration.backend.service.ShopkeeperService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopkeeperServiceImpl implements ShopkeeperService {

    private final ShopkeeperRepository shopkeeperRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingService bookingService;

    @Override
    public ShopkeeperLoginResponse login(ShopkeeperLoginRequest request) {
        Shopkeeper shopkeeper = shopkeeperRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), shopkeeper.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        return ShopkeeperLoginResponse.builder()
                .message("Shopkeeper login successful")
                .shopkeeperId(shopkeeper.getId())
                .name(shopkeeper.getName())
                .build();
    }

    @Override
    public List<BookingResponse> viewDailyBookings(LocalDate date) {
        return bookingService.getBookingsByDate(date);
    }

    @Override
    public BookingResponse verifyUser(Long bookingId) {
        return bookingService.markAsCollected(bookingId);
    }
}
