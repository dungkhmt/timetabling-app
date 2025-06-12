package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryBillGetListFilter;
import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryBillSpecification implements Specification<DeliveryBill> {
    private DeliveryBillGetListFilter filter;
    @Override
    public Predicate toPredicate(Root<DeliveryBill> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        String status = filter.getStatusId();
        String facilityId = filter.getFacilityId();
        LocalDateTime startCreatedAt = filter.getStartCreatedAt();
        LocalDateTime endCreatedAt = filter.getEndCreatedAt();


        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
        }

        if (facilityId != null && !facilityId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("facility").get("id"), facilityId));
        }

        if (status != null && !status.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("statusId"), status));
        }

        if (startCreatedAt != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdStamp"), startCreatedAt));
        }

        if (endCreatedAt != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdStamp"), endCreatedAt));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
