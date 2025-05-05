package openerp.openerpresourceserver.examtimetabling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, String> {
   default List<ExamRoom> findAllAsExamRoomDTO() {
        return findAll().stream()
            .map(classroom -> ExamRoom.builder()
                .id(classroom.getId())
                .name(classroom.getClassroom())
                .numberSeat(classroom.getQuantityMax() != null ? classroom.getQuantityMax().intValue() : 0)
                .numberComputer(0)
                .build())
            .collect(Collectors.toList());
    }
}
