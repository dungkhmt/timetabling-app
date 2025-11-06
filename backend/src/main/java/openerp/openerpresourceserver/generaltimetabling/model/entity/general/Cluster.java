package openerp.openerpresourceserver.generaltimetabling.model.entity.general;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_cluster")
public class Cluster {
    @Id
    @Column(name="id")
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name="semester")
    private String semester;
    @Column(name="batch_id")
    private Long batchId;

}
