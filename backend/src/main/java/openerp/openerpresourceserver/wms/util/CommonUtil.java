package openerp.openerpresourceserver.wms.util;

import org.springframework.data.domain.PageRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CommonUtil {
    //this funtion to get UUID
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getSequenceId(String prefix, int length, int increment) {
        return prefix + String.format("%0" + length + "d", increment);
    }

    // this function to get PageRequest from page and limit
    public static PageRequest getPageRequest(Integer page, Integer limit) {
        return PageRequest.of(page, limit);
    }

    public static List<LocalDate> getAllWeeklyStartDates(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = to.with(DayOfWeek.MONDAY);
        while (current.isAfter(from)) {
            dates.add(current);
            current = current.minusWeeks(1);
        }
        return dates;
    }

    public static <T> List<T> getRandomElements(List<T> list, int from, int to) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Cannot select from empty or null list");
        }

        if (to > list.size()) {
            throw new IllegalArgumentException("Requested count exceeds list size");
        }

        // Create a copy to avoid modifying the original list
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled.subList(from, to);
    }

    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Cannot select random element from empty or null list");
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
