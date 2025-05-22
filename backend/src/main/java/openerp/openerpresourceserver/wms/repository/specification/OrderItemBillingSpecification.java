package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.dto.filter.OrderItemBillingGetListFilter;
import openerp.openerpresourceserver.wms.entity.OrderItemBilling;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemBillingSpecification implements Specification<OrderItemBilling> {
    private OrderItemBillingGetListFilter filter;
    @Override
    public Predicate toPredicate(Root<OrderItemBilling> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        LocalDateTime startCreatedAt = filter.getStartCreatedAt();
        LocalDateTime endCreatedAt = filter.getEndCreatedAt();
        String orderTypeId = OrderType.PURCHASE_ORDER.name();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
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
