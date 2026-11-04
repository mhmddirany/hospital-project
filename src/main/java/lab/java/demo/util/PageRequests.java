package lab.java.demo.util;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Step 2: makes a client-supplied {@link Pageable} safe to run.
 *
 * <ol>
 *   <li><b>Sort whitelist.</b> {@code ?sort=...} comes straight from the
 *       URL. An unknown property would otherwise blow up deep inside
 *       Spring Data as a 500; a known-but-unintended one (say, a column
 *       with no index) could be slow. Only the listed fields are allowed,
 *       and anything else is a 400 with a clear message.</li>
 *   <li><b>Stable ordering.</b> If two rows have the same name, the
 *       database may return them in any order, so the same row could show
 *       up on page 1 <i>and</i> page 2. Adding {@code id} as a final
 *       tie-breaker makes the order -- and therefore the pages --
 *       deterministic.</li>
 * </ol>
 *
 * <p>The maximum page size is enforced separately by
 * {@code spring.data.web.pageable.max-page-size} in application.properties.
 */
public final class PageRequests {

    private PageRequests() {
    }

    public static Pageable safe(Pageable requested, Set<String> sortableFields) {
        for (Sort.Order order : requested.getSort()) {
            if (!sortableFields.contains(order.getProperty())) {
                throw new IllegalArgumentException("Cannot sort by '" + order.getProperty()
                        + "'. Allowed sort fields: " + new TreeSet<>(sortableFields));
            }
        }
        Sort sort = requested.getSort().getOrderFor("id") == null
                ? requested.getSort().and(Sort.by("id"))
                : requested.getSort();
        return PageRequest.of(requested.getPageNumber(), requested.getPageSize(), sort);
    }
}
