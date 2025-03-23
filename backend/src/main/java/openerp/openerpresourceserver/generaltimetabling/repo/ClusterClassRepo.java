package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeClusterClassId;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.ClusterClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterClassRepo extends JpaRepository<ClusterClass, CompositeClusterClassId> {

}
