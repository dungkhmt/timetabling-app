package openerp.openerpresourceserver.generaltimetabling.model.dto.request;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RoomOccupationWithModuleCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String classRoom;
    private String classCode;

    private String semester;

    private Integer startPeriod;
    private Integer endPeriod;
    private String crew;
    private Integer dayIndex;
    private Integer weekIndex;
    private String moduleCode;
    private String status;

    public RoomOccupationWithModuleCode(){

    }
    public RoomOccupationWithModuleCode( String classRoom,  String classCode, Integer startPeriod, Integer endPeriod,
                           String crew, Integer dayIndex, Integer weekIndex, String status, String semester, String moduleCode) {
        this.classRoom = classRoom;
        this.classCode = classCode;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.crew = crew;
        this.dayIndex = dayIndex;
        this.weekIndex = weekIndex;
        this.status = status;
        this.semester = semester;
        this.moduleCode = moduleCode;
    }

    @Override
    public String toString() {
        return classCode + " " + classRoom + " " + dayIndex + " " + weekIndex + " " + startPeriod + "/" + endPeriod + " " + semester;
    }
}
