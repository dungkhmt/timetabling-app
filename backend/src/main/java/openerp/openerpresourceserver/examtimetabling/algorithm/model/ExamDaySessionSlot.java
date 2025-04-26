package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import openerp.openerpresourceserver.generaltimetabling.model.Constant;

public class ExamDaySessionSlot {
    public int day;// day 0,1,2,... serialization
    public int session;// session 0 (morning) and 1 (afternoon)
    public int slot; // slot (kip) 1, 2 (morning), 3, 4 (afternoon)
    public ExamDaySessionSlot(int serialSlot){// 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, ...
        day = (serialSlot-1)/(Constant.examAfternoonSlots+Constant.examMorningSlots);
        slot = serialSlot - day*(Constant.examAfternoonSlots+Constant.examMorningSlots);
        //day = day + 2;// monday, tuesday, wednesday...
        if(slot <= Constant.examMorningSlots) session = 0;
        else session = 1;
    }

    public ExamDaySessionSlot(int day, int slot) {
        this.day = day;
        if(slot <= Constant.examMorningSlots) session = 0;
        else session = 1;
        this.slot = slot;
    }

    public int hash(){
        return (day)*(Constant.examAfternoonSlots+Constant.examMorningSlots) + slot;
    }
}
