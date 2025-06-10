package openerp.openerpresourceserver.wms.dto.delivery;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDeliveryPlan {
    private String id;
    private String deliveryPlanName;
    private String description;
    @NotNull
    private LocalDate deliveryDate;
    @Size(min = 1, message = "At least one delivery bill ID is required")
    private List<String> deliveryBillIds;
    @Size(min = 1, message = "At least one shipper ID is required")
    private List<String> shipperIds;
    @Size(min = 1, message = "At least one vehicle ID is required")
    private List<String> vehicleIds;
    @NotNull
    private String facilityId;
}
