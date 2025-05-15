package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.ShipmentGetListFilter;
import openerp.openerpresourceserver.wms.entity.Shipment;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentSpecification implements Specification<Shipment> {
    private ShipmentGetListFilter filter;

    @Override
    public Predicate toPredicate(Root<Shipment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        List<String> statusId = filter.getStatusId();
        String shipmentTypeId = filter.getShipmentTypeId();
        LocalDate expectedDeliveryDate = filter.getExpectedDeliveryDate();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
            predicates.add(criteriaBuilder.like(root.get("name"), "%" + keyword + "%"));
        }

        if (statusId != null && !statusId.isEmpty()) {
            predicates.add(root.get("statusId").in(statusId));
        }

        if (shipmentTypeId != null && !shipmentTypeId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("shipmentTypeId"), shipmentTypeId));
        }

        if (expectedDeliveryDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expectedDeliveryDate"), expectedDeliveryDate));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
