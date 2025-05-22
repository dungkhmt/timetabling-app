package openerp.openerpresourceserver.generaltimetabling.helper;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Helper class to set column widths for Excel sheets
 */
public class ColumnWidthHelper {
    
    /**
     * Sets column widths for timetabling Excel sheets
     * 
     * @param sheet The Excel sheet to modify
     * @param infoEndColumn End column index of info section
     * @param scheduleStartColumn Start column index of schedule section
     * @param numberSlotsPerDay Number of slots per day
     * @param days Number of days to include (typically 7 for Mon-Sun)
     */
    public static void setTimeTableColumnWidths(Sheet sheet, int infoEndColumn, int scheduleStartColumn, 
                                              int numberSlotsPerDay, int days) {
        // Set normal width for info columns (standard width)
        for (int i = 0; i <= infoEndColumn; i++) {
            sheet.setColumnWidth(i, 256 * 12); // 12 characters width (standard)
        }
        
        // Set half width for schedule columns
        int totalScheduleColumns = days * numberSlotsPerDay;
        for (int i = scheduleStartColumn; i < scheduleStartColumn + totalScheduleColumns; i++) {
            sheet.setColumnWidth(i, 256 * 6); // 6 characters width (half of standard)
        }
    }
}
