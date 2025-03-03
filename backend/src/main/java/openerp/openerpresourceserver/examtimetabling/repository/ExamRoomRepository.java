package openerp.openerpresourceserver.examtimetabling.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import openerp.openerpresourceserver.examtimetabling.entity.ExamRoom;

@Repository
public interface ExamRoomRepository extends JpaRepository<ExamRoom, UUID> {
}
