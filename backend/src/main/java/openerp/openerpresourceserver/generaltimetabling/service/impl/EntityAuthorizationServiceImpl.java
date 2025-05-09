package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.entity.EntityAuthorization;
import openerp.openerpresourceserver.generaltimetabling.repo.EntityAuthorizationRepo;
import openerp.openerpresourceserver.generaltimetabling.service.EntityAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class EntityAuthorizationServiceImpl implements EntityAuthorizationService {

    private EntityAuthorizationRepo entityAuthorizationRepo;

    @Override
    public Set<String> getEntityAuthorization(String id, List<String> roleIds) {
        List<EntityAuthorization> entityAuthorizations = entityAuthorizationRepo.findAllByIdStartingWithAndRoleIdIn(id, roleIds);
        return entityAuthorizations != null ? entityAuthorizations.stream().map(EntityAuthorization::getId).collect(Collectors.toSet()) : Collections.emptySet();
    }
}
