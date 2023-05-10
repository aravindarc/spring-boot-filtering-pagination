package com.aravindakumar.booking.repository;

import com.aravindakumar.booking.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingRepo extends MongoRepository<Booking, String>, FilterableRepository<Booking> {

}
