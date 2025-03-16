package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetabling_config_params")
public class TimeTablingConfigParams {
    public static final String MAX_DAY_SCHEDULED = "MAX_DAY_SCHEDULED";

    @Id
    @Column(name="id")
    private String id;

    @Column(name="value")
    private double value;
}
