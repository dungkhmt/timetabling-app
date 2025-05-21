package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.VehicleGetListFilter;
import openerp.openerpresourceserver.wms.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleSpecification implements Specification<Vehicle> {
    private VehicleGetListFilter filter;
    @Override
    public Predicate toPredicate(Root<Vehicle> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        String statusId = filter.getStatusId();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
            predicates.add(criteriaBuilder.like(root.get("vehicleName"), "%" + keyword + "%"));
        }

        if (statusId != null && !statusId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
