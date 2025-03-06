package openerp.openerpresourceserver.generaltimetabling.model.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GeneralClassDto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer quantity;
    private Integer quantityMax;
    private String moduleCode;
    private String moduleName;
    private String classType;
    private String classCode;
    private String semester;
    private String studyClass;
    private String mass;
    private String state;
    private String crew;
    private String openBatch;
    private String course;
    private Long refClassId;
    private Long parentClassId;
    private Integer duration;
    private String groupName;
    private List<String> listGroupName;
    private List<RoomReservation> timeSlots = new ArrayList<RoomReservation>();
    private String learningWeeks;
    private String foreignLecturer;
}
