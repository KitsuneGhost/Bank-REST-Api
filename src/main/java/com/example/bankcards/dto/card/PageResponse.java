package com.example.bankcards.dto.card;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        String sort
) {
    public static <T> PageResponse<T> from(Page<T> p, String sortString) {
        return new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(),
                p.hasNext(), p.hasPrevious(), sortString
        );
    }
}
