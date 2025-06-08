package openerp.openerpresourceserver.generaltimetabling.algorithms;


public class AlgorithmConfigParams {
    public static final String TIME_SLOT_ORDER_FROM_EARLIEST = "TIME_SLOT_ORDER_FROM_EARLIEST";
    public static final String TIME_SLOT_ORDER_FROM_PRIORITY_SETTINGS = "TIME_SLOT_ORDER_FROM_PRIORITY_SETTINGS";

    public String TIME_SLOT_ORDER = TIME_SLOT_ORDER_FROM_EARLIEST;// from 1, 2, . . .
    public int SLOT_PER_SESSION = 6; // each session consists of 6 time-slots, 1, 2, 3, 4, 5, 6
    public int MAX_DAY_SCHEDULED = 5; // by defalt: days 2, 3, 4, 5, 6

    public String USED_ROOM_PRIORITY = "Y";

    public double roomCapRate = 1.15;
}
