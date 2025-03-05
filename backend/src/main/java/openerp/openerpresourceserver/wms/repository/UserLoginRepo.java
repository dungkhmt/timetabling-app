package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLoginRepo extends JpaRepository<UserLogin, String> {
}
