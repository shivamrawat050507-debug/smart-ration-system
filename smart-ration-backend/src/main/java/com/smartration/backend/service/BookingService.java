package com.smartration.backend.service;

import com.smartration.backend.dto.BookingRequest;
import com.smartration.backend.dto.BookingResponse;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingResponse> getBookingsByDate(LocalDate date);

    BookingResponse markAsCollected(Long bookingId);
}
