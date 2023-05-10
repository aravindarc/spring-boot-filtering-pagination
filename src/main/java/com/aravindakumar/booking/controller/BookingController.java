package com.aravindakumar.booking.controller;

import com.aravindakumar.booking.model.Booking;
import com.aravindakumar.booking.model.PaginatedResponse;
import com.aravindakumar.booking.repository.BookingRepo;
import com.aravindakumar.booking.repository.Filtering;
import com.aravindakumar.booking.repository.FilteringFactory;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingRepo bookingRepo;

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingRepo.save(booking);
    }

    @GetMapping
    public PaginatedResponse<Booking> getBookings(@RequestParam int page, @RequestParam int size, @RequestParam List<String> filter) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> all = bookingRepo.findAllWithFilter(Booking.class, FilteringFactory.parseFromParams(filter, Booking.class), pageable);
        return PaginatedResponse.<Booking>builder()
                .currentPage(all.getNumber())
                .totalItems(all.getTotalElements())
                .totalPages(all.getTotalPages())
                .items(all.getContent())
                .hasNext(all.hasNext())
                .build();
    }
}
