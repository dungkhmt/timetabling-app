package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.dto.filter.PurchaseOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderSpecification implements Specification<OrderHeader> {
    private PurchaseOrderGetListFilter filter;

    @Override
    public Predicate toPredicate(Root<OrderHeader> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        String status = filter.getStatus();
        LocalDateTime startCreatedAt = filter.getStartCreatedAt();
        LocalDateTime endCreatedAt = filter.getEndCreatedAt();
        String orderTypeId = OrderType.PURCHASE_ORDER.name();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
        }

        if (status != null && !status.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
        }


        if (startCreatedAt != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdStamp"), startCreatedAt));
        }

        if (endCreatedAt != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdStamp"), endCreatedAt));
        }

        if (orderTypeId != null && !orderTypeId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("orderTypeId"), orderTypeId));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
