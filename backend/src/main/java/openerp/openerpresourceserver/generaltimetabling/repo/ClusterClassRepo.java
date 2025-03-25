package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeClusterClassId;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.ClusterClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterClassRepo extends JpaRepository<ClusterClass, CompositeClusterClassId> {
    List<ClusterClass> findAllByClusterId(Long clusterId);
}
