package openerp.openerpresourceserver.generaltimetabling.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapDataScheduleTimeSlotRoomWrapper {
    MapDataScheduleTimeSlotRoom data;
    Map<Integer, GeneralClass> mClassSegment2Class;
    Map<Integer, RoomReservation> mClassSegment2RoomReservation;
}
