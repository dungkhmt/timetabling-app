package openerp.openerpresourceserver.wms.util;

import org.springframework.data.domain.PageRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        LocalDate current = from.with(DayOfWeek.MONDAY);
        while (!current.isAfter(to)) {
            dates.add(current);
            current = current.plusWeeks(1);
        }
        return dates;
    }
}
