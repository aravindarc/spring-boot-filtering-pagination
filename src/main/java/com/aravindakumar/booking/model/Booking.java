package com.aravindakumar.booking.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("booking")
@Data
public class Booking {
    @Id private String id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int room;
    private String status;
    @CreatedDate private LocalDateTime createdAt;
}
