package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.ShipperGetListFilter;
import openerp.openerpresourceserver.wms.entity.Shipper;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipperSpecification implements Specification<Shipper> {
    private ShipperGetListFilter filter;
    @Override
    public Predicate toPredicate(Root<Shipper> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        String statusId = filter.getStatusId();
        String deliveryStatusId = filter.getDeliveryStatusId();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
        }

        if (statusId != null && !statusId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
        }

        if (deliveryStatusId != null && !deliveryStatusId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("deliveryStatusId"), deliveryStatusId));
        }


        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
