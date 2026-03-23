package com.smartration.backend.controller;

import com.smartration.backend.dto.BookingResponse;
import com.smartration.backend.dto.ShopkeeperLoginRequest;
import com.smartration.backend.dto.ShopkeeperLoginResponse;
import com.smartration.backend.service.ShopkeeperService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopkeepers")
@RequiredArgsConstructor
public class ShopkeeperController {

    private final ShopkeeperService shopkeeperService;

    @PostMapping("/login")
    public ResponseEntity<ShopkeeperLoginResponse> login(@Valid @RequestBody ShopkeeperLoginRequest request) {
        return ResponseEntity.ok(shopkeeperService.login(request));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> viewDailyBookings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(shopkeeperService.viewDailyBookings(date));
    }

    @PatchMapping("/bookings/{bookingId}/verify")
    public ResponseEntity<BookingResponse> verifyUser(@PathVariable Long bookingId) {
        return ResponseEntity.ok(shopkeeperService.verifyUser(bookingId));
    }
}
