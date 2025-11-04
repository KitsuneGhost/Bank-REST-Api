package com.example.bankcards.dto.card;

import org.springframework.data.domain.Page;

import java.util.List;


/**
 * Generic Data Transfer Object representing a paginated API response.
 * <p>
 * Wraps the content and metadata of a {@link org.springframework.data.domain.Page}
 * to provide a consistent pagination structure for REST responses.
 * <p>
 * This DTO allows clients to easily interpret pagination details such as
 * the current page, total pages, element counts, and sort order.
 *
 * <p><b>Example JSON response:</b>
 * <pre>
 * {
 *   "items": [ ... ],
 *   "page": 0,
 *   "size": 12,
 *   "totalElements": 57,
 *   "totalPages": 5,
 *   "hasNext": true,
 *   "hasPrevious": false,
 *   "sort": "createdAt,desc"
 * }
 * </pre>
 *
 * @param <T>           the type of the elements in the page content
 * @param items         list of items on the current page
 * @param page          current page index (zero-based)
 * @param size          number of elements per page
 * @param totalElements total number of elements across all pages
 * @param totalPages    total number of available pages
 * @param hasNext       {@code true} if a next page exists
 * @param hasPrevious   {@code true} if a previous page exists
 * @param sort          sorting criteria used for this page (e.g., {@code "createdAt,desc"})
 *
 * @see org.springframework.data.domain.Page
 * @see org.springframework.data.domain.Pageable
 * @see com.example.bankcards.service.CardService#filterAll(String, Long, java.math.BigDecimal,
 * java.math.BigDecimal, java.time.LocalDate, java.time.LocalDate, String, org.springframework.data.domain.Pageable)
 */
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
