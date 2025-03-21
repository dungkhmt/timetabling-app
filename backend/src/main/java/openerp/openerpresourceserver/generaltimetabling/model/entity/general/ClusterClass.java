package openerp.openerpresourceserver.generaltimetabling.model.entity.general;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeClusterClassId;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_cluster_class")
@IdClass(CompositeClusterClassId.class)
public class ClusterClass {
    @Id
    @Column(name="class_id")
    private Long classId;

    @Id
    @Column(name="cluster_id")
    private Long clusterId;
}
