package com.smartration.backend.service;

import com.smartration.backend.dto.BookingResponse;
import com.smartration.backend.dto.ShopkeeperLoginRequest;
import com.smartration.backend.dto.ShopkeeperLoginResponse;
import java.time.LocalDate;
import java.util.List;

public interface ShopkeeperService {

    ShopkeeperLoginResponse login(ShopkeeperLoginRequest request);

    List<BookingResponse> viewDailyBookings(LocalDate date);

    BookingResponse verifyUser(Long bookingId);
}
