package openerp.openerpresourceserver.generaltimetabling.model.entity.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekValidator;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_class")
public class TimeTablingClass {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String classCode;
    private String classType;
    private String course;
    private String crew;
    private String groupName;
    private String learningWeeks;
    private String mass;
    private String moduleCode;
    private String moduleName;
    private String openBatch;
    private Long parentClassId;
    private Integer quantity;
    private Integer quantityMax;
    private Long refClassId;
    private String semester;
    private String state;
    private String studyClass;
    private String foreignLecturer;
    private Integer duration;
    private Long batchId;
    private String createdByUserId;
    @Override
    public int hashCode() {
        return Objects.hash(moduleName, moduleCode, classCode);
    }


    @Override
    public String toString() {
        return refClassId +"/"+ parentClassId +"/"+ classCode + " " + moduleCode + " " + moduleName;
    }

    public void setLearningWeeks(String learningWeeks) {
        if (learningWeeks != null) {
            List<String> weekStringList = List.of(learningWeeks.split(","));
            weekStringList.forEach(weekString -> {
                if (!LearningWeekValidator.isCorrectFormat(weekString))
                    throw new InvalidFieldException("Tuần học không đúng định dạng!");
            });
        }
        this.learningWeeks = learningWeeks;
    }

    public List<Integer> extractLearningWeeks(){
        String[] terms = learningWeeks.split(",");
        List<Integer> W = new ArrayList();
        try {
            if (terms != null) {
                for (String t : terms) {
                    if (!t.contains("-")) {
                        int w = Integer.valueOf(t);
                    }else{
                        String[] s = t.split("-");
                        if(s != null && s.length == 2){
                            int start = Integer.valueOf(s[0]);
                            int end = Integer.valueOf(s[1]);
                            for(int w = start; w <= end; w++) W.add(w);
                        }else{
                            return new ArrayList<>();// not correct format
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>(); // not correct format
        }
        return W;
    }

    public static List<Integer> extractLearningWeeks(String learningWeeks){
        String[] terms = learningWeeks.split(",");
        List<Integer> W = new ArrayList();
        try {
            if (terms != null) {
                for (String t : terms) {
                    if (!t.contains("-")) {
                        int w = Integer.valueOf(t);
                    }else{
                        String[] s = t.split("-");
                        if(s != null && s.length == 2){
                            int start = Integer.valueOf(s[0]);
                            int end = Integer.valueOf(s[1]);
                            for(int w = start; w <= end; w++) W.add(w);
                        }else{
                            return new ArrayList<>();// not correct format
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>(); // not correct format
        }
        return W;
    }
}
