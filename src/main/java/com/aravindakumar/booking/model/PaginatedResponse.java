package com.aravindakumar.booking.model;


import lombok.*;

import java.util.List;

@Builder
@Data
public class PaginatedResponse<T> {
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private List<T> items;
    private boolean hasNext;
}
