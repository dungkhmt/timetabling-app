package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

public class RoomDaySlotSession {
    public int room;
    public int day;
    public int slot;
    public int session;

    public RoomDaySlotSession(int room, int day, int slot, int session) {
        this.room = room;
        this.day = day;
        this.slot = slot;
        this.session = session;
    }
}
