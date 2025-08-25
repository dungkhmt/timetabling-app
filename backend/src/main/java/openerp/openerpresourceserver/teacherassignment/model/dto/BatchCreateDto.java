package openerp.openerpresourceserver.teacherassignment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateDto {
    @NotBlank(message = "Batch name is required")
    private String name;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotBlank(message = "Created by user ID is required")
    private String createdByUserId;

    // Optional: danh sách class IDs nếu muốn thêm lớp ngay khi tạo batch
    private List<String> classIds;

    // Phương thức chuyển đổi sang Entity
    public Batch toEntity() {
        return Batch.builder()
                .name(this.name)
                .semester(this.semester)
                .createdByUserId(this.createdByUserId)
                .build();
    }
}
