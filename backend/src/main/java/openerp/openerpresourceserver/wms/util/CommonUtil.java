package openerp.openerpresourceserver.wms.util;

import org.springframework.data.domain.PageRequest;

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
}
