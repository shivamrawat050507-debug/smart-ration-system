package com.smartration.backend.service.impl;

import com.smartration.backend.dto.BookingRequest;
import com.smartration.backend.dto.BookingResponse;
import com.smartration.backend.entity.Booking;
import com.smartration.backend.entity.User;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.BookingRepository;
import com.smartration.backend.repository.UserRepository;
import com.smartration.backend.service.BookingService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (request.getDate().equals(LocalDate.now()) && request.getTime().isBefore(LocalTime.now())) {
            throw new BadRequestException("Booking time cannot be in the past");
        }

        Booking booking = Booking.builder()
                .bookingDate(request.getDate())
                .bookingTime(request.getTime())
                .collected(false)
                .user(user)
                .build();

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingsByDate(LocalDate date) {
        return bookingRepository.findByBookingDate(date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BookingResponse markAsCollected(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        booking.setCollected(true);
        return mapToResponse(bookingRepository.save(booking));
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .collected(booking.getCollected())
                .build();
    }
}
