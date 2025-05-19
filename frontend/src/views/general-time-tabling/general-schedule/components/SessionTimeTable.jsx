import React, { useEffect, useState } from "react";
import useDebounce from "hooks/useDebounce";
import {
  Button,
  CircularProgress,
  TablePagination,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tooltip,
  InputAdornment,
  Checkbox,
} from "@mui/material";
import { Settings, Search } from "@mui/icons-material";

const SessionTimeTable = ({
  classes,
  selectedSemester,
  selectedGroup,
  loading,
  onRowCountChange,
  selectedVersion,
}) => {
  const [classDetails, setClassDetails] = useState([]);
  const [filteredClassDetails, setFilteredClassDetails] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(100);
  const [columnVisibility, setColumnVisibility] = useState(() => {
    const savedSettings = localStorage.getItem("session-table-column-visibility");
    return savedSettings
      ? JSON.parse(savedSettings)
      : {
          classCode: true,
          studyClass: true,
          learningWeeks: true,
          moduleCode: true,
          moduleName: true,
          crew: true,
          quantityMax: true,
          classType: true,
          mass: true,
          duration: true,
          batch: true,
        };
  });

  // Get the configured number of slots per session
  const totalSlots = selectedVersion?.numberSlotsPerSession ?? 6;
  
  // Each session (morning and afternoon) has the full number of slots
  // Rather than splitting the total between them
  
  // Generate periods for morning and afternoon - each has the full number of slots
  const morningPeriods = Array.from({ length: totalSlots }, (_, i) => i + 1);
  const afternoonPeriods = Array.from({ length: totalSlots }, (_, i) => i + 1);
  // Group classes by class code and crew
  useEffect(() => {
    if (classes && classes.length > 0) {
      // First, transform the classes to a more manageable format
      const transformedClasses = classes.map(cls => ({
        weekDay: cls.weekday,
        roomReservationId: cls.roomReservationId,
        code: cls.classCode,
        crew: cls.crew,
        batch: cls.course,
        room: cls.room,
        startTime: cls.startTime,
        endTime: cls.endTime,
        studyClass: cls.studyClass,
        listGroupName: cls.listGroupName,
        learningWeeks: cls.learningWeeks,
        moduleCode: cls.moduleCode,
        moduleName: cls.moduleName,
        quantityMax: cls.quantityMax,
        classType: cls.classType,
        mass: cls.mass,
        duration: cls.duration,
        generalClassId: String(cls.id || ""),
        parentId: cls.parentClassId,
        id: cls.id
      }));

      // Group classes by classCode and crew
      const classGroups = {};
      
      transformedClasses.forEach(cls => {
        if (!cls.code || !cls.crew) return;
        
        const key = `${cls.code}_${cls.crew}`;
        
        if (!classGroups[key]) {
          classGroups[key] = {
            ...cls,
            consolidatedId: key,
            timeSlots: []
          };
        }
        
        // Add time slot if this class has schedule info
        if (cls.startTime && cls.endTime && cls.weekDay) {
          classGroups[key].timeSlots.push({
            roomReservationId: cls.roomReservationId,
            room: cls.room,
            weekDay: cls.weekDay,
            startTime: cls.startTime,
            endTime: cls.endTime,
            generalClassId: cls.generalClassId,
            crew: cls.crew // Thêm crew vào từng slot
          });
        }
      });

      // Convert groups object to array
      const consolidatedClasses = Object.values(classGroups);
      
      setClassDetails(consolidatedClasses);
      setFilteredClassDetails(consolidatedClasses);
    }
  }, [classes]);

  // Report back the consolidated count when it changes
  useEffect(() => {
    if (onRowCountChange && classDetails) {
      onRowCountChange(classDetails.length);
    }
  }, [classDetails, onRowCountChange]);

  // Use debounced search term for filtering
  useEffect(() => {
    if (debouncedSearchTerm.trim() === "") {
      setFilteredClassDetails(classDetails);
      setPage(0);
    } else {
      const lowercasedTerm = debouncedSearchTerm.toLowerCase();
      const filtered = classDetails.filter((cls) => {
        return (
          (cls.code && cls.code.toLowerCase().includes(lowercasedTerm)) ||
          (cls.moduleCode &&
            cls.moduleCode.toLowerCase().includes(lowercasedTerm)) ||
          (cls.moduleName &&
            cls.moduleName.toLowerCase().includes(lowercasedTerm)) ||
          (cls.room && cls.room.toLowerCase().includes(lowercasedTerm)) ||
          (cls.classType &&
            cls.classType.toLowerCase().includes(lowercasedTerm)) ||
          (cls.studyClass &&
            cls.studyClass.toLowerCase().includes(lowercasedTerm)) ||
          (cls.crew && cls.crew.toString().includes(lowercasedTerm))
        );
      });
      setFilteredClassDetails(filtered);
      setPage(0);
    }
  }, [debouncedSearchTerm, classDetails]);

  const days = ["Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"];

  const convertWeekdayToDay = (weekday) => {
    const dayMap = {
      2: "Thứ 2", 
      3: "Thứ 3",
      4: "Thứ 4",
      5: "Thứ 5",
      6: "Thứ 6",
      7: "Thứ 7",
      8: "Chủ nhật"
    };
    return dayMap[weekday] || "";
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSettingsOpen = () => {
    setIsSettingsOpen(true);
  };

  const handleSettingsClose = () => {
    setIsSettingsOpen(false);
  };

  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const handleColumnVisibilityChange = (column) => {
    const newVisibility = {
      ...columnVisibility,
      [column]: !columnVisibility[column],
    };
    setColumnVisibility(newVisibility);
    localStorage.setItem(
      "session-table-column-visibility",
      JSON.stringify(newVisibility)
    );
  };

  const handleSaveSettings = () => {
    handleSettingsClose();
  };

  const columnDefinitions = [
    { id: "classCode", label: "Mã lớp" },
    { id: "studyClass", label: "Nhóm" },
    { id: "learningWeeks", label: "Tuần học" },
    { id: "moduleCode", label: "Mã học phần" },
    { id: "moduleName", label: "Tên học phần" },
    { id: "crew", label: "Kíp" },
    { id: "quantityMax", label: "SL MAX" },
    { id: "classType", label: "Loại lớp" },
    { id: "mass", label: "Thời lượng" },
    { id: "duration", label: "Số tiết" },
    { id: "batch", label: "Khóa" }
  ];

  // Render time slot for a specific period and session
  const renderTimeSlotForPeriod = (classDetail, day, period, session) => {
    // Tìm slot phù hợp với day và session
    const slot = classDetail.timeSlots.find(slot => {
      const slotDay = convertWeekdayToDay(slot.weekDay);
      
      // Chỉ xét các slot có cùng ngày với day được truyền vào
      if (slotDay !== day) return false;
      
      // Xác định session dựa vào crew (S=Sáng, C=Chiều)
      const slotSession = slot.crew === "S" ? "Sáng" : (slot.crew === "C" ? "Chiều" : "");
      
      // Nếu không khớp session, bỏ qua
      if (slotSession !== session) return false;
      
      // Logic kiểm tra period có nằm trong slot không:
      // 1. Với kíp sáng (S): so sánh period trực tiếp với startTime/endTime
      // 2. Với kíp chiều (C): phải trừ totalSlots từ period để so sánh với startTime/endTime
      //    vì period được truyền vào đã là period + totalSlots (từ cột phần chiều của bảng)
      let adjustedPeriod = session === "Chiều" ? period - totalSlots : period;
      const isInRange = adjustedPeriod >= slot.startTime && adjustedPeriod <= slot.endTime;
      
      return isInRange;
    });
    
    if (!slot) {
      return (
        <td 
          key={`${classDetail.code}-${day}-${session}-${period}`}
          className="border border-gray-300 text-center px-1"
          style={{ width: "30px", minWidth: "30px" }} 
        ></td>
      );
    }
    
    // Với kíp chiều, adjustedPeriod sẽ là period - totalSlots
    // để so sánh với startTime của slot
    let adjustedPeriod = session === "Chiều" ? period - totalSlots : period;
    
    // Kiểm tra đây có phải là đầu slot không
    const isSessionStart = adjustedPeriod === slot.startTime;
    
    if (isSessionStart) {
      // Tính colspan
      const colSpan = slot.endTime - slot.startTime + 1;
      // Clean up room name
      let cleanRoomName = slot.room;
      if (cleanRoomName && cleanRoomName.includes('(Tiết')) {
        cleanRoomName = cleanRoomName.split('(Tiết')[0].trim();
      }
      return (
        <td
          key={`${classDetail.code}-${day}-${session}-${period}`}
          colSpan={colSpan}
          className="border border-gray-300 text-center px-1"
          style={{ backgroundColor: "#FFD700" }}
        >
          <span className="text-[14px]">
            {cleanRoomName}
          </span>
        </td>
      );
    }
    
    // Các period nằm giữa slot sẽ không render gì (đã được colspan che)
    return null;
  };

  return (
    <div className="h-full w-full flex flex-col justify-start">
      <div className="flex justify-end items-center gap-2 mb-1 pt-1 z-20 overflow-visible">
        <TextField
          placeholder="Tìm kiếm (mã lớp, phòng, tên học phần...)"
          variant="outlined"
          size="small"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{
            width: "300px",
            "& .MuiInputBase-root": {
              height: "36px",
            },
          }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            ),
            endAdornment: searchTerm ? (
              <InputAdornment position="end">
                <Button size="small" onClick={() => setSearchTerm("")}>
                  Xóa
                </Button>
              </InputAdornment>
            ) : null,
          }}
        />
        <Button
          variant="outlined"
          startIcon={<Settings />}
          onClick={handleSettingsOpen}
          size="small"
          sx={{
            height: "36px",
            textTransform: "none",
          }}
        >
          Cài đặt hiển thị
        </Button>
      </div>

      {loading ? (
        <table
          className="overflow-x-auto flex items-center flex-col border-separate border-spacing-0"
          style={{ flex: "1" }}
        >
          <div className="flex justify-center items-center h-full w-full ">
            <CircularProgress />
          </div>
        </table>
      ) : (
        <div className="overflow-x-auto" style={{ flex: "1" }}>
          <table
            className="min-w-full border-separate border-spacing-0"
            style={{ tableLayout: "auto" }}
          >
            <thead className="sticky top-0 z-10 bg-white">
              <tr>
                {columnVisibility.classCode && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "60px", minWidth: "60px" }}
                    rowSpan="3"
                  >
                    Mã lớp
                  </th>
                )}
                {columnVisibility.studyClass && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "80px", minWidth: "80px" }}
                    rowSpan="3"
                  >
                    Nhóm
                  </th>
                )}
                {columnVisibility.learningWeeks && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "45px", minWidth: "45px" }}
                    rowSpan="3"
                  >
                    Tuần học
                  </th>
                )}
                {columnVisibility.moduleCode && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "70px", minWidth: "70px" }}
                    rowSpan="3"
                  >
                    Mã học phần
                  </th>
                )}
                {columnVisibility.moduleName && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "100px", minWidth: "100px" }}
                    rowSpan="3"
                  >
                    Tên học phần
                  </th>
                )}
                {columnVisibility.crew && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "40px", minWidth: "40px" }}
                    rowSpan="3"
                  >
                    Kíp
                  </th>
                )}
                {columnVisibility.quantityMax && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "50px", minWidth: "50px" }}
                    rowSpan="3"
                  >
                    SL MAX
                  </th>
                )}
                {columnVisibility.classType && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "60px", minWidth: "60px" }}
                    rowSpan="3"
                  >
                    Loại lớp
                  </th>
                )}
                {columnVisibility.mass && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "60px", minWidth: "60px" }}
                    rowSpan="3"
                  >
                    Thời lượng
                  </th>
                )}
                {columnVisibility.duration && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "50px", minWidth: "50px" }}
                    rowSpan="3"
                  >
                    Số tiết
                  </th>
                )}
                {columnVisibility.batch && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "50px", minWidth: "50px" }}
                    rowSpan="3"
                  >
                    Khóa
                  </th>
                )}
                {days.map((day) => (
                  <th
                    key={day}
                    colSpan={totalSlots * 2} // Each day has both morning and afternoon
                    className="border-[1px] border-solid border-gray-300 p-2 text-center"
                  >
                    {day}
                  </th>
                ))}
              </tr>
              <tr className="bg-white">
                {days.map((day) => (
                  <React.Fragment key={`${day}-sessions`}>
                    <th
                      colSpan={totalSlots}
                      className="border-[1px] border-solid border-gray-300 text-center font-medium"
                    >
                      Sáng
                    </th>
                    <th
                      colSpan={totalSlots}
                      className="border-[1px] border-solid border-gray-300 text-center font-medium"
                    >
                      Chiều
                    </th>
                  </React.Fragment>
                ))}
              </tr>
              <tr className="bg-white">
                {days.flatMap((day) => [
                  ...morningPeriods.map((period) => (
                    <td
                      key={`${day}-morning-${period}`}
                      className="border-[1px] border-solid border-gray-300 text-center"
                      style={{ width: "20px", minWidth: "20px", padding: "2px" }} // Much smaller cells
                    >
                      {period}
                    </td>
                  )),
                  ...afternoonPeriods.map((period) => (
                    <td
                      key={`${day}-afternoon-${period}`}
                      className="border-[1px] border-solid border-gray-300 text-center"
                      style={{ width: "20px", minWidth: "20px", padding: "2px" }} // Much smaller cells
                    >
                      {period + totalSlots}
                    </td>
                  ))
                ])}
              </tr>
            </thead>
            <tbody>
              {filteredClassDetails.length > 0 ? (
                filteredClassDetails
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((classDetail, index) => {
                    let groupDisplay = "";
                    let showTooltip = false;
                    let tooltipContent = "";

                    if (classDetail.listGroupName) {
                      // Handle group name display logic
                      if (Array.isArray(classDetail.listGroupName)) {
                        const fullGroupText = classDetail.listGroupName.join(", ");
                        tooltipContent = fullGroupText;
                        groupDisplay = fullGroupText.length > 30 
                          ? fullGroupText.substring(0, 30) + "..." 
                          : fullGroupText;
                        showTooltip = fullGroupText.length > 30;
                      } else if (
                        typeof classDetail.listGroupName === "string" &&
                        classDetail.listGroupName.includes("[") &&
                        classDetail.listGroupName.includes("]")
                      ) {
                        try {
                          const groups = JSON.parse(classDetail.listGroupName);
                          if (Array.isArray(groups)) {
                            const fullGroupText = groups.join(", ");
                            tooltipContent = fullGroupText;
                            groupDisplay = fullGroupText.length > 30
                              ? fullGroupText.substring(0, 30) + "..."
                              : fullGroupText;
                            showTooltip = fullGroupText.length > 30;
                          } else {
                            groupDisplay = String(classDetail.listGroupName);
                          }
                        } catch (e) {
                          console.error("Error parsing listGroupName:", e);
                          groupDisplay = String(classDetail.listGroupName);
                        }
                      } else {
                        groupDisplay = String(classDetail.listGroupName);
                      }
                    }

                    return (
                      <tr
                        key={`${classDetail.consolidatedId}-${index}`}
                        style={{ height: "40px" }}
                      >
                        {columnVisibility.classCode && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.code}
                          </td>
                        )}
                        {columnVisibility.studyClass && (
                          <td className="border border-gray-300 text-center px-1 w-[80px] overflow-hidden text-ellipsis whitespace-nowrap">
                            {showTooltip ? (
                              <Tooltip
                                title={tooltipContent}
                                arrow
                                placement="top"
                              >
                                <span>{groupDisplay}</span>
                              </Tooltip>
                            ) : (
                              <span>{groupDisplay}</span>
                            )}
                          </td>
                        )}
                        {columnVisibility.learningWeeks && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.learningWeeks}
                          </td>
                        )}
                        {columnVisibility.moduleCode && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.moduleCode}
                          </td>
                        )}
                        {columnVisibility.moduleName && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.moduleName}
                          </td>
                        )}
                        {columnVisibility.crew && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.crew}
                          </td>
                        )}
                        {columnVisibility.quantityMax && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.quantityMax}
                          </td>
                        )}
                        {columnVisibility.classType && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.classType}
                          </td>
                        )}
                        {columnVisibility.mass && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.mass}
                          </td>
                        )}
                        {columnVisibility.duration && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.duration}
                          </td>
                        )}
                        {columnVisibility.batch && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.batch}
                          </td>
                        )}
                        
                        {/* Render all periods for each day */}
                        {days.flatMap(day => [
                          ...morningPeriods.map(period => 
                            renderTimeSlotForPeriod(classDetail, day, period, "Sáng")
                          ),
                          ...afternoonPeriods.map(period => 
                            renderTimeSlotForPeriod(classDetail, day, period + totalSlots, "Chiều")
                          )
                        ])}
                      </tr>
                    );
                  })
              ) : (
                <tr>
                  <td
                    colSpan={
                      Object.values(columnVisibility).filter(Boolean).length +
                      days.length * (morningPeriods.length + afternoonPeriods.length)
                    }
                    className="border border-gray-300 text-center py-4"
                  >
                    <div className="h-full">Không có dữ liệu</div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      <TablePagination
        className="border-y-[1px] border-solid border-gray-300"
        component="div"
        count={filteredClassDetails.length}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[25, 50, 100]}
        labelRowsPerPage="Rows per page:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}-${to} trên ${count}`
        }
      />

      {/* Settings Dialog */}
      <Dialog open={isSettingsOpen} onClose={handleSettingsClose}>
        <DialogTitle>Cài đặt hiển thị cột</DialogTitle>
        <DialogContent>
          <div className="grid grid-cols-2 gap-2">
            {columnDefinitions.map((column) => (
              <div key={column.id} className="flex items-center">
                <Checkbox
                  checked={columnVisibility[column.id]}
                  onChange={() => handleColumnVisibilityChange(column.id)}
                  id={`column-${column.id}`}
                />
                <label
                  htmlFor={`column-${column.id}`}
                  className="ml-2 cursor-pointer"
                >
                  {column.label}
                </label>
              </div>
            ))}
          </div>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleSettingsClose} color="secondary">
            Đóng
          </Button>
          <Button
            onClick={handleSaveSettings}
            color="primary"
            variant="contained"
          >
            Áp dụng
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default SessionTimeTable;
