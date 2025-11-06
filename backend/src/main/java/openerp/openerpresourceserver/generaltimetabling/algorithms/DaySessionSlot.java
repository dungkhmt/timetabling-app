package openerp.openerpresourceserver.generaltimetabling.algorithms;

import openerp.openerpresourceserver.generaltimetabling.model.Constant;

public class DaySessionSlot {
    public int day;// day 2, 3, 4, 5, 6, 7, 8
    public int session;// session 0 (morning) and 1 (afternoon)
    public int slot; // slot (tiet) 1, 2, 3, 4, 5, 6

    public DaySessionSlot(int slotIndex){
        slotIndex -= 1;
        day = slotIndex / (Constant.slotPerCrew*2); // Constant.slotPerCrew = 6 (6 tiet 1 session)
        int t = slotIndex - day*Constant.slotPerCrew*2;
        session = t / Constant.slotPerCrew; // 0 (morning) and 1 (afternoon)
        slot = t - Constant.slotPerCrew * session;
        //if(slot == 0) slot = Constant.slotPerCrew;
        slot += 1; // (tiet 1, 2, 3, 4, 5, 6)
        day = day + 2; // (2: monday; 3:tuesday; 4: wednesday)
    }
    public DaySessionSlot(int slotIndex, int nbSlotPerSession){
        slotIndex -= 1;
        day = slotIndex / (nbSlotPerSession*2); // Constant.slotPerCrew = 6 (6 tiet 1 session)
        int t = slotIndex - day*nbSlotPerSession*2;
        session = t / nbSlotPerSession; // 0 (morning) and 1 (afternoon)
        slot = t - nbSlotPerSession * session;
        //if(slot == 0) slot = Constant.slotPerCrew;
        slot += 1; // (tiet 1, 2, 3, 4, 5, 6)
        day = day + 2; // (2: monday; 3:tuesday; 4: wednesday)
    }

    public DaySessionSlot(int day, int session, int slot) {
        this.day = day;
        this.session = session;
        this.slot = slot;
    }

    public int hash(){
        return (day-2)*Constant.slotPerCrew*2 + session*Constant.slotPerCrew + slot;
    }
    public int hash(int nbSlotPerSession){
        return (day-2)*nbSlotPerSession*2 + session*nbSlotPerSession + slot;
    }
    public String toString(){
        String s = "day " + day + (session == 0 ? " Morning " : " Afternoon ") + " - slot " + slot;

        return s;
    }
    public static void main(String[] args){
        for(int slotIndex = 1; slotIndex <= 60; slotIndex++) {
            DaySessionSlot dss = new DaySessionSlot(slotIndex);
            System.out.println(slotIndex + ": " + dss);
        }
    }
}
