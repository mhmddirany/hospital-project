package lab.java.demo.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Step 2: one page of results plus what a client needs to show "page 2 of
 * 7" or fetch the next page.
 *
 * <p>Why not return Spring's {@code Page} directly? Its JSON shape is an
 * implementation detail (Spring itself warns that it isn't stable), and it
 * would expose internal fields like the raw sort object. This record is
 * our public contract instead -- the same reason the controllers return
 * XxxResponse DTOs instead of entities (Issue 23).
 *
 * @param content       the items on this page
 * @param page          zero-based page number
 * @param size          requested page size
 * @param totalElements number of matching items across all pages
 * @param totalPages    number of pages at this size
 * @param first         whether this is the first page
 * @param last          whether this is the last page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    /** Converts a page of entities into a page of response DTOs. */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<? super E, ? extends T> mapper) {
        Page<T> mapped = page.map(mapper);
        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages(),
                mapped.isFirst(),
                mapped.isLast());
    }
}
