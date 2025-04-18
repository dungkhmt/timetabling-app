package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @PrePersist
    public void prePersist() {
        if(createdStamp == null) {
            createdStamp = LocalDateTime.now();
        }
        if(lastUpdatedStamp == null) {
            lastUpdatedStamp = LocalDateTime.now();
        }
        customPrePersist();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedStamp = LocalDateTime.now();
    }

    protected void customPrePersist() {

    }
}
