package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.ProductGetListFilter;
import openerp.openerpresourceserver.wms.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSpecification implements Specification<Product> {
    private ProductGetListFilter filter;

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        List<String> statusIds = filter.getStatusId();
        List<String> categoryIds = filter.getCategoryId();


        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("id"), "%" + keyword + "%"));
        }

        if (statusIds != null && !statusIds.isEmpty()) {
            predicates.add(root.get("statusId").in(statusIds));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            Join<Object, Object> productCategoryJoin = root.join("productCategory");
            predicates.add(productCategoryJoin.get("id").in(categoryIds));
        }


        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
