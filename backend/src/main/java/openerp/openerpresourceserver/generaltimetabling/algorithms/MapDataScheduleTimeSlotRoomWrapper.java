package openerp.openerpresourceserver.generaltimetabling.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingRoom;
import openerp.openerpresourceserver.labtimetabling.entity.Room;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapDataScheduleTimeSlotRoomWrapper {
    public MapDataScheduleTimeSlotRoom data;
    public Map<Integer, ModelResponseTimeTablingClass> mClassSegment2Class;
    public Map<Integer, TimeTablingClassSegment> mClassSegment2RoomReservation;
    public Map<Integer, Classroom> mIndex2Room;
}
