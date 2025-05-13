package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.SupplierGetListFilter;
import openerp.openerpresourceserver.wms.entity.Supplier;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierSpecification implements Specification<Supplier> {
    private SupplierGetListFilter filter;
    @Override
    public Predicate toPredicate(Root<Supplier> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        List<String> statusId = filter.getStatusId();

        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
            predicates.add(criteriaBuilder.like(root.get("name"), "%" + keyword + "%"));
        }

        if (statusId != null && !statusId.isEmpty()) {
            predicates.add(root.get("statusId").in(statusId));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
