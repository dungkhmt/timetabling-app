package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class TimetableStatisticsDTO {
  private UUID timetableId;
  private String timetableName;
  private long totalClasses;
  private long assignedClasses;
  private double completionRate;
  private List<DistributionItemDTO> sessionDistribution;
  private List<DistributionItemDTO> roomDistribution;
  private List<DistributionItemDTO> buildingDistribution;
  private long smallRoomAssignments;
  private List<GroupAssignmentStatsDTO> groupAssignmentStats;
  private long totalExamDays;
  private long totalAvailableRooms;
  private long usedRoomsCount;
  private String sessionCollectionName;
  private List<DistributionItemDTO> dailyDistribution;
  private List<DailySessionDistributionDTO> dailySessionDistribution;
}
