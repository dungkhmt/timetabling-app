package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleOrderSpecification implements Specification<OrderHeader> {
    private SaleOrderGetListFilter filter;

    @Override
    public Predicate toPredicate(Root<OrderHeader> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        String statusId = filter.getStatus();
        List<String> saleChannelId = filter.getSaleChannelId();
        LocalDateTime startCreatedAt = filter.getStartCreatedAt();
        LocalDateTime endCreatedAt = filter.getEndCreatedAt();
        String orderTypeId = OrderType.SALES_ORDER.name();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
        }

        if (statusId != null && !statusId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
        }

        if (saleChannelId != null && !saleChannelId.isEmpty()) {
            predicates.add(root.get("saleChannelId").in(saleChannelId));
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
