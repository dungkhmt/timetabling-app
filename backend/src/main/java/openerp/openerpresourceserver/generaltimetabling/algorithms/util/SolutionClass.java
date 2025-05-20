package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.List;

public class SolutionClass {
    public AClass cls;
    public List<int[]> periods;// start slots of class-segments: p[0] is start-slot, p[1] is end-slot, p[2] is session

    public SolutionClass(AClass cls, List<int[]> periods) {
        this.cls = cls;
        this.periods = periods;
    }

    public String toString() {
        String s = cls.course + "[" + cls.id + "]: ";
        for (int[] p : periods) s = s + "(" + p[0] + "," + p[1] + "," + p[2] + ") ";
        return s;
    }

    public static boolean overLap(int startSlot1, int duration1, int startSlot2, int duration2) {
        if (startSlot1 + duration1 <= startSlot2 || startSlot2 + duration2 <= startSlot1) return false;
        return true;
    }

    public boolean overlap(SolutionClass sci) {
        for (int[] p : periods) {
            for (int[] pi : sci.periods) {
                if (p[2] != pi[2]) continue;// sessions are different
                if (SolutionClass.overLap(p[0], p[1] + 1 - p[0], pi[0], pi[1] + 1 - pi[0])) return true;
            }
        }
        return false;
    }

    public boolean overlap(int start, int end, int session) {
        for (int[] p : periods) {
            if (p[2] != session) continue;
            if (SolutionClass.overLap(p[0], p[1] + 1 - p[0], start, end - start + 1)) return true;
        }
        return false;
    }
}
