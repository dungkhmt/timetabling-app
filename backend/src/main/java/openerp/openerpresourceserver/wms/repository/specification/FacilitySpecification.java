package openerp.openerpresourceserver.wms.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.wms.dto.filter.FacilityGetListFilter;
import openerp.openerpresourceserver.wms.entity.Facility;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilitySpecification implements Specification<Facility> {
    private FacilityGetListFilter filter;


    @Override
    public Predicate toPredicate(Root<Facility> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        String keyword = filter.getKeyword();
        List<String> statusId = filter.getStatusId();

        if (keyword != null && !keyword.isEmpty()) {
            Predicate idLike = criteriaBuilder.like(root.get("id").as(String.class), "%" + keyword + "%");
            Predicate nameLike = criteriaBuilder.like(root.get("name"), "%" + keyword + "%");
            predicates.add(criteriaBuilder.or(idLike, nameLike));
        }

        if (statusId != null && !statusId.isEmpty()) {
            predicates.add(root.get("statusId").in(statusId));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
