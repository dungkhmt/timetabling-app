package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamTimeTablingInput;
import java.util.*;
public interface ExamTimeTablingSolver {
    boolean solve(MapDataExamTimeTablingInput I);
    Map<Integer, Integer> getSolutionMapSlot();
    Map<Integer, Integer> getSolutionMapRoom();
}
