package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeVersionRoomId;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.VersionRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VersionRoomRepo extends JpaRepository <VersionRoom, CompositeVersionRoomId>{
    List<VersionRoom> findAllByVersionId(Long versionId);
    List<VersionRoom> findAllByVersionIdAndRoomId(Long versionId, String roomId);
    List<VersionRoom> findAllByVersionIdAndRoomIdIn(Long versionId, List<String> roomIds);
}
