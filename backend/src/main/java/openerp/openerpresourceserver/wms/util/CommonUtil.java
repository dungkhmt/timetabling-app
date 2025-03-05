package openerp.openerpresourceserver.wms.util;

import java.util.UUID;

public class CommonUtil {
    //this funtion to get UUID
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getSequenceId(String prefix, int length, int increment) {
        return prefix + String.format("%0" + length + "d", increment);
    }
}
