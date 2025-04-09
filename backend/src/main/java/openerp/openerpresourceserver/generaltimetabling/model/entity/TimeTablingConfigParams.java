package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetabling_config_params")
public class TimeTablingConfigParams {
    public static final String MAX_DAY_SCHEDULED = "MAX_DAY_SCHEDULED";
    public static final String USED_ROOM_PRIORITY = "USED_ROOM_PRIORITY";

    @Id
    @Column(name="id")
    private String id;

    @Column(name="value")
    private String value;


}
