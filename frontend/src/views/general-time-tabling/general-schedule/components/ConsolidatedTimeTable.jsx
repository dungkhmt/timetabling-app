import { useEffect, useState } from "react";
import { Checkbox } from "@mui/material";
import { useClassrooms } from "views/general-time-tabling/hooks/useClassrooms";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
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
} from "@mui/material";
import { Settings, Search } from "@mui/icons-material";
import React from "react";

const ConsolidatedTimeTable = ({
  classes,
  selectedSemester,
  selectedGroup,
  loading,
  selectedRows,
  onSelectedRowsChange,
}) => {
  const [classDetails, setClassDetails] = useState([]);
  const [filteredClassDetails, setFilteredClassDetails] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const debouncedSearchTerm = useDebounce(searchTerm, 300); // 300ms delay
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(100);
  const [columnVisibility, setColumnVisibility] = useState(() => {
    const savedSettings = localStorage.getItem("consolidated-table-column-visibility");
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

  const { classrooms } = useClassrooms(selectedGroup?.groupName || "", null);
  const { handlers, states } = useGeneralSchedule();

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
            generalClassId: cls.generalClassId
          });
        }
      });

      // Convert groups object to array
      const consolidatedClasses = Object.values(classGroups);
      
      setClassDetails(consolidatedClasses);
      setFilteredClassDetails(consolidatedClasses);
    }
  }, [classes]);

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

  const days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
  const periods = [1, 2, 3, 4, 5, 6];

  const convertWeekdayToDay = (weekday) => {
    const dayMap = {
      2: "Mon", 
      3: "Tue",
      4: "Wed",
      5: "Thu",
      6: "Fri",
      7: "Sat",
      8: "Sun"
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

  const handleSelectAll = (event) => {
    if (event.target.checked) {
      // For consolidated view, we need to extract all generalClassIds from timeSlots
      const newSelected = [];
      filteredClassDetails.forEach(classGroup => {
        classGroup.timeSlots.forEach(slot => {
          if (slot.generalClassId && !newSelected.includes(slot.generalClassId)) {
            newSelected.push(slot.generalClassId);
          }
        });
      });
      onSelectedRowsChange(newSelected);
    } else {
      onSelectedRowsChange([]);
    }
  };

  const handleSelectRow = (event, consolidatedId) => {
    // Find the class group
    const classGroup = filteredClassDetails.find(cls => cls.consolidatedId === consolidatedId);
    
    if (!classGroup || !classGroup.timeSlots || classGroup.timeSlots.length === 0) return;
    
    // Get all generalClassIds from this group's time slots
    const groupIds = classGroup.timeSlots.map(slot => slot.generalClassId).filter(Boolean);
    
    // Check if any of the group's ids are already selected
    const hasSelected = groupIds.some(id => selectedRows.includes(id));
    
    let newSelected = [...selectedRows];
    
    if (hasSelected) {
      // Remove all ids from this group
      newSelected = newSelected.filter(id => !groupIds.includes(id));
    } else {
      // Add all ids from this group
      groupIds.forEach(id => {
        if (!newSelected.includes(id)) {
          newSelected.push(id);
        }
      });
    }
    
    onSelectedRowsChange(newSelected);
  };

  const isSelected = (consolidatedId) => {
    // Find the class group
    const classGroup = filteredClassDetails.find(cls => cls.consolidatedId === consolidatedId);
    
    if (!classGroup || !classGroup.timeSlots || classGroup.timeSlots.length === 0) return false;
    
    // Check if any of the time slots' generalClassIds are in selectedRows
    return classGroup.timeSlots.some(slot => 
      slot.generalClassId && selectedRows.includes(slot.generalClassId)
    );
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
      "consolidated-table-column-visibility",
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

  // Render time slots for a class
  const renderTimeSlots = (classDetail, day) => {
    // Filter time slots for this day
    const daySlotsMap = {};
    
    // Group time slots by period to combine consecutive periods
    classDetail.timeSlots.forEach(slot => {
      if (convertWeekdayToDay(slot.weekDay) === day) {
        for (let period = slot.startTime; period <= slot.endTime; period++) {
          daySlotsMap[period] = {
            ...slot,
            isStart: period === slot.startTime,
            isEnd: period === slot.endTime
          };
        }
      }
    });
    
    // Create array of consecutive periods
    const consecutiveSlots = [];
    let currentSlot = null;
    
    // Process periods 1-6 in order
    for (let period = 1; period <= 6; period++) {
      const slot = daySlotsMap[period];
      
      if (slot) {
        if (slot.isStart) {
          // Start a new slot
          currentSlot = {
            startPeriod: period,
            endPeriod: period,
            room: slot.room,
            roomReservationId: slot.roomReservationId,
            generalClassId: slot.generalClassId
          };
        } else if (currentSlot) {
          // Extend current slot
          currentSlot.endPeriod = period;
        }
        
        if (slot.isEnd && currentSlot) {
          // End current slot and add to list
          consecutiveSlots.push(currentSlot);
          currentSlot = null;
        }
      } else if (currentSlot) {
        // Gap in periods, end current slot
        consecutiveSlots.push(currentSlot);
        currentSlot = null;
      }
    }
    
    // Make sure we don't leave a slot open
    if (currentSlot) {
      consecutiveSlots.push(currentSlot);
    }
    
    // Render time slots
    return periods.map((period) => {
      // Check if this period is the start of a slot
      const slotStart = consecutiveSlots.find(slot => slot.startPeriod === period);
      
      if (slotStart) {
        const colSpan = slotStart.endPeriod - slotStart.startPeriod + 1;
        
        return (
          <td
            key={`${classDetail.code}-${day}-${period}`}
            colSpan={colSpan}
            className="border border-gray-300 text-center px-1"
            style={{ backgroundColor: "#FFD700" }}
          >
            <span className="text-[14px]">{slotStart.room}</span>
          </td>
        );
      }
      
      // Check if this period is part of an ongoing slot (not the start)
      const isPartOfSlot = consecutiveSlots.some(
        slot => period > slot.startPeriod && period <= slot.endPeriod
      );
      
      // If it's part of an ongoing slot, don't render anything
      if (isPartOfSlot) {
        return null;
      }
      
      // Otherwise, render an empty cell
      return (
        <td
          key={`${classDetail.code}-${day}-${period}`}
          className="border border-gray-300 text-center px-1"
          style={{ width: "70px" }}
        ></td>
      );
    });
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
                <th
                  className="border-[1px] border-solid border-gray-300 p-1"
                  style={{ width: "30px", minWidth: "30px" }}
                >
                  <Checkbox
                    indeterminate={
                      selectedRows.length > 0 &&
                      selectedRows.length < classDetails.length
                    }
                    checked={
                      classDetails.length > 0 &&
                      selectedRows.length === classDetails.length
                    }
                    onChange={handleSelectAll}
                    size="small"
                  />
                </th>
                {columnVisibility.classCode && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "60px", minWidth: "60px" }}
                  >
                    Mã lớp
                  </th>
                )}
                {columnVisibility.studyClass && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "80px", minWidth: "80px" }}
                  >
                    Nhóm
                  </th>
                )}
                {columnVisibility.learningWeeks && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "45px", minWidth: "45px" }}
                  >
                    Tuần học
                  </th>
                )}
                {columnVisibility.moduleCode && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "70px", minWidth: "70px" }}
                  >
                    Mã học phần
                  </th>
                )}
                {columnVisibility.moduleName && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "100px", minWidth: "100px" }}
                  >
                    Tên học phần
                  </th>
                )}
                {columnVisibility.crew && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "40px", minWidth: "40px" }}
                  >
                    Kíp
                  </th>
                )}
                {columnVisibility.quantityMax && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "50px", minWidth: "50px" }}
                  >
                    SL MAX
                  </th>
                )}
                {columnVisibility.classType && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "60px", minWidth: "60px" }}
                  >
                    Loại lớp
                  </th>
                )}
                {columnVisibility.mass && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "60px", minWidth: "60px" }}
                  >
                    Thời lượng
                  </th>
                )}
                {columnVisibility.duration && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "50px", minWidth: "50px" }}
                  >
                    Số tiết
                  </th>
                )}
                {columnVisibility.batch && (
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "50px", minWidth: "50px" }}
                  >
                    Khóa
                  </th>
                )}
                {days.map((day) => (
                  <th
                    key={day}
                    colSpan={6}
                    className="border-[1px] border-solid border-gray-300 p-2 text-center min-w-32"
                    style={{ padding: "8px 0" }}
                  >
                    {day}
                  </th>
                ))}
              </tr>
              <tr className="bg-white">
                <td className="border-[1px] border-solid border-gray-300"></td>
                <td
                  colSpan={
                    Object.values(columnVisibility).filter(Boolean).length
                  }
                  className="border-[1px] border-solid border-gray-300"
                ></td>
                {days.flatMap((day) =>
                  periods.map((period) => (
                    <td
                      key={`${day}-${period}`}
                      className="border-[1px] border-solid border-gray-300 text-center"
                      style={{ width: "60px", padding: "4px" }}
                    >
                      {period}
                    </td>
                  ))
                )}
              </tr>
            </thead>
            <tbody>
              {filteredClassDetails.length > 0 ? (
                filteredClassDetails
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((classDetail, index) => {
                    const isItemSelected = isSelected(classDetail.consolidatedId);

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
                        className={isItemSelected ? "bg-blue-50" : ""}
                      >
                        <td className="border border-gray-300 text-center px-1">
                          <Checkbox
                            checked={isItemSelected}
                            onChange={(event) =>
                              handleSelectRow(event, classDetail.consolidatedId)
                            }
                            size="small"
                          />
                        </td>
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
                        
                        {/* Render time slots for each day */}
                        {days.map(day => (
                          <React.Fragment key={`${classDetail.code}-${day}`}>
                            {renderTimeSlots(classDetail, day)}
                          </React.Fragment>
                        ))}
                      </tr>
                    );
                  })
              ) : (
                <tr>
                  <td className="border border-gray-300"></td>
                  <td
                    colSpan={
                      Object.values(columnVisibility).filter(Boolean).length +
                      days.length * periods.length
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

export default ConsolidatedTimeTable;