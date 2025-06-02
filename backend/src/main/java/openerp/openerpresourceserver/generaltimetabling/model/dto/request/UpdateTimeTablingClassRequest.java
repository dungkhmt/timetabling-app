package openerp.openerpresourceserver.generaltimetabling.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateTimeTablingClassRequest {
    private TimeTablingClass timetablingClass;
}
