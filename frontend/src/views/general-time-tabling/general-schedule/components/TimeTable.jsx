import { useEffect, useState } from "react";
import { Checkbox } from "@mui/material";
import { useClassrooms } from "views/general-time-tabling/hooks/useClassrooms";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import useDebounce from "hooks/useDebounce";
import {
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  FormControl,
  Modal,
  TablePagination,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Tooltip,
  InputAdornment,
} from "@mui/material";
import { Add, Remove, Settings, Search } from "@mui/icons-material";
import { toast } from "react-toastify";

const TimeTable = ({
  classes,
  selectedSemester,
  selectedVersion, 
  selectedGroup,
  onSaveSuccess,
  loading,
  selectedRows,
  onSelectedRowsChange,
  numberSlotsToDisplay, 
}) => {
  const [classDetails, setClassDetails] = useState([]);
  const [filteredClassDetails, setFilteredClassDetails] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const debouncedSearchTerm = useDebounce(searchTerm, 300); // 300ms delay
  const [open, setOpen] = useState(false);
  const [selectedClass, setSelectedClass] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(100);
  const [isAddSlotDialogOpen, setIsAddSlotDialogOpen] = useState(false);
  const [selectedPeriods, setSelectedPeriods] = useState("");
  const [selectedClassForSlot, setSelectedClassForSlot] = useState(null);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const [draggedClass, setDraggedClass] = useState(null);
  const [moveConfirmOpen, setMoveConfirmOpen] = useState(false);
  const [moveTarget, setMoveTarget] = useState(null);
  const [columnVisibility, setColumnVisibility] = useState(() => {
    const savedSettings = localStorage.getItem("timetable-column-visibility");
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
          actions: true,
        };
  });

  const { classrooms } = useClassrooms(selectedGroup?.groupName || "", null);
  const { handlers, states } = useGeneralSchedule();

  useEffect(() => {
    if (classes && classes.length > 0) {
      const transformedClassDetails = classes.map((cls) => ({
        weekDay: cls.weekday,
        roomReservationId: cls.roomReservationId,
        code: cls.classCode,
        crew: cls.crew,
        batch: cls.course,
        room: cls.room,
        timetable: cls.startTime && cls.endTime ? {
          [convertWeekdayToDay(cls.weekday)]: {
            id: cls.classCode,
            room: cls.room,
            startTime: cls.startTime,
            endTime: cls.endTime,
          },
        } : null,
        studyClass: cls.studyClass,
        listGroupName: cls.listGroupName,
        learningWeeks: cls.learningWeeks,
        moduleCode: cls.moduleCode,
        moduleName: cls.moduleName,
        quantityMax: cls.quantityMax,
        classType: cls.classType,
        mass: cls.mass,
        duration: cls.duration,
        generalClassId: cls.id,
        parentId: cls.parentClassId,
      }));
      setClassDetails(transformedClassDetails);
      setFilteredClassDetails(transformedClassDetails);
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
  // Generate periods dynamically based on numberSlotsToDisplay
  // If numberSlotsToDisplay is undefined or null, default to 6 for safety, though GeneralScheduleScreen should provide a default.
  const effectiveSlots = typeof numberSlotsToDisplay === 'number' && numberSlotsToDisplay > 0 ? numberSlotsToDisplay : 6;
  const periods = Array.from({ length: effectiveSlots }, (_, i) => i + 1);

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

  const handleClose = () => {
    setOpen(false);
  };

  const handleSave = async () => {
    if (!selectedClass || !selectedSemester) return;

    const calculatedEndTime =
      selectedClass.startTime + selectedClass.numberOfPeriods - 1;

    if (calculatedEndTime > effectiveSlots) {
      toast.error(`Tiết kết thúc không thể vượt quá tổng số tiết cho phép (Tiết ${effectiveSlots}).`);
      return;
    }

    const { numberOfPeriods, code, ...filteredSelectedClass } = selectedClass;

    const selectedClassData = {
      ...filteredSelectedClass,
      endTime: calculatedEndTime,
    };

    await handlers.handleSaveTimeSlot(
      selectedSemester.semester,
      selectedClassData
    );
    onSaveSuccess();
    setOpen(false);
  };

  const handleAddTimeSlot = async () => {
    try {
      if (!selectedClassForSlot || !selectedPeriods) return;

      const periodsToAdd = parseInt(selectedPeriods, 10);
      if (periodsToAdd > selectedClassForSlot.duration) {
        toast.error("Số tiết không được lớn hơn số tiết còn lại!");
        return;
      }


      await handlers.handleAddTimeSlot({
        generalClassId: selectedClassForSlot.generalClassId,
        parentId: selectedClassForSlot.roomReservationId, // Đây là ID của segment cha (nếu là tách từ segment đã có lịch) hoặc null/undefined
        duration: periodsToAdd,
      });

      handleCloseAddSlotDialog();
      onSaveSuccess();
    } catch (error) {
      console.error("Error adding time slot:", error);
      toast.error(error.response?.data || "Thêm ca học thất bại!");
    }
  };

  const handleRemoveTimeSlot = async (generalClassId, roomReservationId) => {
    try {
      if (!generalClassId || !roomReservationId) {
        throw new Error("Missing required parameters");
      }
      await handlers.handleRemoveTimeSlot({
        generalClassId: generalClassId.toString(),
        roomReservationId: roomReservationId,
        versionId: selectedVersion?.id, // Sử dụng selectedVersion.id
      });
      onSaveSuccess();
    } catch (error) {
      console.error("Error removing time slot:", error);
      toast.error(error.response?.data || "Xóa ca học thất bại!");
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;

    const newValue =
      name === "startTime" || name === "endTime" || name === "numberOfPeriods"
        ? Number(value)
        : value;

    setSelectedClass((prevClass) => ({
      ...prevClass,
      [name]: newValue,
    }));
  };

const renderCellContent = (classIndex, day, period) => {
  // Tính toán index thực từ index hiện tại và trang hiện tại
  const actualIndex = page * rowsPerPage + classIndex;
  
  const classInfo = filteredClassDetails[actualIndex];
  const timetable = classInfo?.timetable?.[day];
  
  if (!timetable || !timetable.startTime || !timetable.endTime) {
    // Không có startTime hoặc endTime, hiển thị ô trống
    return (
      <td
        key={`${actualIndex}-${day}-${period}`}
        style={{ width: "70px" }}
        className="border border-gray-300 text-center cursor-pointer px-1"
        onClick={() => handleRowClick(actualIndex, day, period)}
        onDragOver={(e) => {
          e.preventDefault();
          if (draggedClass) {
            e.currentTarget.style.backgroundColor = "#EEC82C"; 
          }
        }}
        onDragLeave={(e) => {
          e.currentTarget.style.backgroundColor = "";
        }}
        onDrop={(e) => {
          e.preventDefault();
          e.currentTarget.style.backgroundColor = "";
          if (draggedClass) {
            handleDrop(actualIndex, day, period);
          }
        }}
      ></td>
    );
  }
  
  if (period < timetable.startTime || period > timetable.endTime) {
    // Không nằm trong khoảng thời gian của lớp, hiển thị ô trống
    return (
      <td
        key={`${actualIndex}-${day}-${period}`}
        style={{ width: "70px" }}
        className="border border-gray-300 text-center cursor-pointer px-1"
        onClick={() => handleRowClick(actualIndex, day, period)}
        onDragOver={(e) => {
          e.preventDefault();
          if (draggedClass) {
            e.currentTarget.style.backgroundColor = "#EEC82C"; // Green highlight for drop target
          }
        }}
        onDragLeave={(e) => {
          e.currentTarget.style.backgroundColor = "";
        }}
        onDrop={(e) => {
          e.preventDefault();
          e.currentTarget.style.backgroundColor = "";
          if (draggedClass) {
            handleDrop(actualIndex, day, period);
          }
        }}
      ></td>
    );
  }

  if (period === timetable.startTime) {
    const colSpan = timetable.endTime - timetable.startTime + 1;

    return (
      <td
        key={`${actualIndex}-${day}-${period}`}
        colSpan={colSpan}
        className="border border-gray-300 text-center cursor-pointer px-1"
        style={{ 
          width: `${70 * colSpan}px`, 
          backgroundColor: isDragging && draggedClass?.index === actualIndex && draggedClass?.day === day ? "#b3e0ff" : "#FFD700", 
          cursor: "grab"
        }}
        onClick={() => handleRowClick(actualIndex, day, period)}
        draggable={true}
        onDragStart={(e) => handleDragStart(e, actualIndex, day, timetable)}
        onDragEnd={(e) => {
          setIsDragging(false);
          e.currentTarget.style.opacity = "1";
          // Only clear background color for empty cells, not for class cells
          document.querySelectorAll('td').forEach(td => {
            // If it's a drop target (empty cell), reset its background
            if (td.style.backgroundColor === 'rgb(76, 175, 80)') { // Reset green background
              td.style.backgroundColor = "";
            }
            // Don't reset background for class cells (which have #FFD700 color)
          });
        }}
      >
        <span className="text-[14px]">{timetable.room}</span>
      </td>
    );
  }

  return null;
};

const handleRowClick = (classIndex, day, period) => {
  const classInfo = filteredClassDetails[classIndex]?.timetable?.[day];

  if (classInfo) {
    setSelectedClass({
      roomReservationId: Number(filteredClassDetails[classIndex].roomReservationId),
      room: filteredClassDetails[classIndex].room,
      startTime: period,
      endTime: Number(classInfo.endTime),
      weekday: Number(filteredClassDetails[classIndex].weekDay),
      code: filteredClassDetails[classIndex].code,
      numberOfPeriods: filteredClassDetails[classIndex].duration,
    });
  } else {
    setSelectedClass({
      roomReservationId: Number(filteredClassDetails[classIndex].roomReservationId),
      room: filteredClassDetails[classIndex].room,
      startTime: period,
      endTime: undefined,
      weekday: days.indexOf(day) + 2,
      code: filteredClassDetails[classIndex].code,
      numberOfPeriods: filteredClassDetails[classIndex].duration,
    });
  }
  setOpen(true);
};

const handleDragStart = (e, index, day, timetable) => {
  // Store the dragged class information
  setDraggedClass({
    index,
    day,
    timetable,
    classInfo: filteredClassDetails[index]
  });
  
  setIsDragging(true);
  e.currentTarget.style.opacity = "0.4";

  // Create a drag image
  const dragImg = document.createElement('div');
  dragImg.textContent = timetable.room;
  dragImg.style.backgroundColor = "#FFD700";
  dragImg.style.padding = "5px 10px";
  dragImg.style.borderRadius = "4px";
  dragImg.style.position = "absolute";
  dragImg.style.top = "-1000px";
  document.body.appendChild(dragImg);
  
  // Set drag image
  e.dataTransfer.setDragImage(dragImg, 20, 20);
  
  // Clean up the element after drag ends
  setTimeout(() => {
    document.body.removeChild(dragImg);
  }, 0);
};

const handleDrop = (targetIndex, targetDay, targetPeriod) => {
  if (!draggedClass) return;
  
  const sourceIndex = draggedClass.index;
  const sourceDay = draggedClass.day;
  const sourceStartTime = draggedClass.timetable.startTime;

  // Only suppress dialog if dropped on exactly the same position (same day AND same starting period)
  if (sourceIndex === targetIndex && sourceDay === targetDay && sourceStartTime === targetPeriod) {
    return;
  }
  
  const sourceClassInfo = filteredClassDetails[sourceIndex];
  const targetClassInfo = filteredClassDetails[targetIndex];

  // Set the move target with necessary information for the save operation
  setMoveTarget({
    roomReservationId: Number(sourceClassInfo.roomReservationId),
    generalClassId: sourceClassInfo.generalClassId,
    sourceDay: sourceDay,
    sourceStartTime: draggedClass.timetable.startTime,
    sourceEndTime: draggedClass.timetable.endTime,
    targetDay: targetDay,
    targetStartTime: targetPeriod,
    targetRoom: targetClassInfo.room || sourceClassInfo.room,
    numberOfPeriods: draggedClass.timetable.endTime - draggedClass.timetable.startTime + 1
  });
  
  // Open confirmation dialog
  setMoveConfirmOpen(true);
};

const handleConfirmMove = async () => {
  if (!moveTarget || !draggedClass) return;

  const { targetRoom, targetStartTime, targetDay, originalClassInfo, targetClassId, originalRoomReservationId } = moveTarget;
  const duration = draggedClass.classInfo.duration; // Assuming duration is on draggedClass.classInfo

  const endTime = targetStartTime + duration - 1;

  if (endTime > effectiveSlots) {
    toast.error(`Tiết kết thúc sau khi di chuyển (Tiết ${endTime}) không thể vượt quá tổng số tiết cho phép (Tiết ${effectiveSlots}).`);
    setMoveConfirmOpen(false);
    setDraggedClass(null);
    setIsDragging(false);
    return;
  }

  // If validation passes, proceed with the move operation
  try {
    // Calculate the end time based on the starting period and number of periods
    const endTime = moveTarget.targetStartTime + moveTarget.numberOfPeriods - 1;

    // Call the API to update the time slot
    await handlers.handleSaveTimeSlot(
      selectedSemester.semester, 
      {
        roomReservationId: moveTarget.roomReservationId,
        room: moveTarget.targetRoom,
        startTime: moveTarget.targetStartTime,
        endTime: endTime,
        weekday: days.indexOf(moveTarget.targetDay) + 2,
      }
    );

    onSaveSuccess();
  } catch (error) {
    console.error('Error moving time slot:', error);
  } finally {
    setMoveConfirmOpen(false);
    setMoveTarget(null);
    setDraggedClass(null);
  }
};

const handleCancelMove = () => {
  setMoveConfirmOpen(false);
  setMoveTarget(null);
  setDraggedClass(null);
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
      const newSelected = filteredClassDetails.map((row) => row.generalClassId);
      onSelectedRowsChange(newSelected);
    } else {
      onSelectedRowsChange([]);
    }
  };

  const handleSelectRow = (event, generalClassId) => {
    const selectedIndex = selectedRows.indexOf(generalClassId);
    let newSelected = [];

    if (selectedIndex === -1) {
      newSelected = [...selectedRows, generalClassId];
    } else {
      newSelected = selectedRows.filter((id) => id !== generalClassId);
    }

    onSelectedRowsChange(newSelected);
  };

  const isSelected = (generalClassId) =>
    selectedRows.indexOf(generalClassId) !== -1;

  const handleOpenAddSlotDialog = (classDetail) => {
    setSelectedClassForSlot(classDetail);
    setIsAddSlotDialogOpen(true);
  };

  const handleCloseAddSlotDialog = () => {
    setIsAddSlotDialogOpen(false);
    setSelectedPeriods("");
    setSelectedClassForSlot(null);
  };

  const handleSettingsOpen = () => {
    setIsSettingsOpen(true);
  };

  const handleSettingsClose = () => {
    setIsSettingsOpen(false);
  };

  const handleColumnVisibilityChange = (column) => {
    const newVisibility = {
      ...columnVisibility,
      [column]: !columnVisibility[column],
    };
    setColumnVisibility(newVisibility);
    localStorage.setItem(
      "timetable-column-visibility",
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
    { id: "batch", label: "Khóa" },
    { id: "actions", label: "Thêm/Xóa" },
  ];

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
                  style={{ width: "60px", minWidth: "60px" }}
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
              {columnVisibility.actions && (
                <>
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "35px", minWidth: "35px" }}
                  >
                    Thêm
                  </th>
                  <th
                    className="border-[1px] border-solid border-gray-300 p-1"
                    style={{ width: "35px", minWidth: "35px" }}
                  >
                    Xóa
                  </th>
                </>
              )}
              {days.map((day) => (
                <th
                  key={day}
                  colSpan={effectiveSlots} // Sử dụng effectiveSlots
                  className="border-[1px] border-solid border-gray-300 p-2 text-center min-w-40"
                >
                  {day}
                </th>
              ))}
            </tr>
            <tr className="bg-white">
              <td className="border-[1px] border-solid border-gray-300"></td> 
              <td
                colSpan={
                  Object.values(columnVisibility).filter(Boolean).length +
                  (columnVisibility.actions ? 1 : 0)
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
                {columnVisibility.actions && (
                  <>
                    <th
                      className="border-[1px] border-solid border-gray-300 p-1"
                      style={{ width: "35px", minWidth: "35px" }}
                    >
                      Thêm
                    </th>
                    <th
                      className="border-[1px] border-solid border-gray-300 p-1"
                      style={{ width: "35px", minWidth: "35px" }}
                    >
                      Xóa
                    </th>
                  </>
                )}
                {days.map((day) => (
                  <th
                    key={day}
                    colSpan={effectiveSlots} // Sử dụng effectiveSlots
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
                    Object.values(columnVisibility).filter(Boolean).length +
                    (columnVisibility.actions ? 1 : 0)
                  }
                  className="border-[1px] border-solid border-gray-300"
                ></td>
                {days.flatMap((day) =>
                  periods.map((period) => (
                    <td
                      key={`${day}-${period}`}
                      className="border-[1px] border-solid border-gray-300 text-center"
                      style={{ width: "60px", padding: "4px" }} // Increased width from 40px to 60px
                    >
                      {period}
                    </td>
                  ))
                )}
              </tr>
            </thead>
            <tbody>
              {classes && classes.length > 0 ? (
                filteredClassDetails
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((classDetail, index) => {
                    const isItemSelected = isSelected(
                      classDetail.generalClassId
                    );

                    let groupDisplay = "";
                    let showTooltip = false;
                    let tooltipContent = "";

                    if (classDetail.listGroupName) {
                      // If it's already an array
                      if (Array.isArray(classDetail.listGroupName)) {
                        const fullGroupText =
                          classDetail.listGroupName.join(", ");
                        tooltipContent = fullGroupText;
                        groupDisplay =
                          fullGroupText.length > 30
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
                            groupDisplay =
                              fullGroupText.length > 30
                                ? fullGroupText.substring(0, 30) + "..."
                                : fullGroupText;
                            showTooltip = fullGroupText.length > 30;
                          } else {
                            groupDisplay = String(classDetail.listGroupName);
                          }
                        } catch (e) {
                          // If parsing fails, use as is
                          console.error("Error parsing listGroupName:", e);
                          groupDisplay = String(classDetail.listGroupName);
                        }
                      }
                      // If it's any other type, convert to string
                      else {
                        groupDisplay = String(classDetail.listGroupName);
                      }
                    }

                    return (
                      <tr
                        key={`${classDetail.code}-${index}`}
                        style={{ height: "40px" }} // Reduced row height from 52px
                        className={isItemSelected ? "bg-blue-50" : ""}
                      >
                        <td className="border border-gray-300 text-center px-1">
                          <Checkbox
                            checked={isItemSelected}
                            onChange={(event) =>
                              handleSelectRow(event, classDetail.generalClassId)
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
                        {columnVisibility.actions && (
                          <>
                            <td className="border border-gray-300 text-center px-1">
                              {!classDetail.timeSlots && (
                                <Button
                                  onClick={() =>
                                    handleOpenAddSlotDialog(classDetail)
                                  }
                                  disabled={classDetail.duration <= 0}
                                  sx={{
                                    minWidth: "28px",
                                    width: "28px",
                                    height: "28px",
                                    padding: "2px",
                                    borderRadius: "4px",
                                  }}
                                  color="primary"
                                  size="small"
                                >
                                  <Add fontSize="small" />
                                </Button>
                              )}
                            </td>
                            <td className="border border-gray-300 text-center px-1">
                              {classDetail.roomReservationId && (
                                <Button
                                  onClick={() =>
                                    handleRemoveTimeSlot(
                                      classDetail.generalClassId,
                                      classDetail.roomReservationId
                                    )
                                  }
                                  sx={{
                                    minWidth: "28px",
                                    width: "28px",
                                    height: "28px",
                                    padding: "2px",
                                    borderRadius: "4px",
                                  }}
                                  color="error"
                                  size="small"
                                >
                                  <Remove fontSize="small" />
                                </Button>
                              )}
                            </td>
                          </>
                        )}
                        {days.flatMap((day) =>
                          periods.map((period) =>
                            renderCellContent(index, day, period)
                          )
                        )}
                      </tr>
                    );
                  })
              ) : (
                <tr>
                  <td className="border border-gray-300"></td>
                  <td
                    colSpan={
                      Object.values(columnVisibility).filter(Boolean).length +
                      days.length * periods.length +
                      (columnVisibility.actions ? 1 : 0)
                    }
                    className="border border-gray-300 text-center py-4"
                  >
                    <div className="h-full ">Không có dữ liệu</div>
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

      {/* Existing modals */}
      <Modal
        open={open}
        disableEscapeKeyDown
        disableBackdropClick
        onClose={(_, reason) => {
          if (reason !== "backdropClick") {
            handleClose();
          }
        }}
        style={{ position: "fixed" }}
      >
        <Box
          onClick={(e) => e.stopPropagation()}
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            width: 400,
            bgcolor: "background.paper",
            borderRadius: 2,
            boxShadow: 24,
            p: 4,
            outline: "none",
            zIndex: 1000,
          }}
        >
          <h2 id="modal-title">Thông tin lớp học</h2>
          {selectedClass ? (
            <div>
              <TextField
                label="Mã lớp"
                name="code"
                value={selectedClass.code || ""}
                onChange={handleInputChange}
                fullWidth
                disabled
                margin="normal"
                InputProps={{
                  readOnly: true,
                }}
              />
              <FormControl fullWidth margin="normal">
                <Autocomplete
                  options={classrooms.map((classroom) => classroom.classroom)}
                  value={selectedClass.room || ""}
                  onChange={(event, newValue) => {
                    handleInputChange({
                      target: { name: "room", value: newValue },
                    });
                  }}
                  renderInput={(params) => (
                    <TextField {...params} label="Phòng học" />
                  )}
                  freeSolo
                />
              </FormControl>
              <TextField
                label="Tiết bắt đầu"
                name="startTime"
                type="number"
                value={selectedClass.startTime || ""}
                onChange={handleInputChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Số tiết học"
                name="numberOfPeriods"
                type="number"
                disabled
                value={selectedClass.numberOfPeriods || ""}
                onChange={handleInputChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Ngày"
                name="weekday"
                type="number"
                value={selectedClass.weekday || ""}
                onChange={handleInputChange}
                fullWidth
                margin="normal"
                InputProps={{
                  readOnly: true,
                }}
              />
            </div>
          ) : (
            <p>Không có thông tin lớp học</p>
          )}
          <div className="flex justify-between mt-[20px] gap-4">
            <Button
              onClick={handleClose}
              variant="outlined"
              color="secondary"
              disabled={states.isSavingTimeSlot}
              sx={{ minWidth: "100px", padding: "8px 16px" }}
            >
              Đóng
            </Button>
            <Button
              onClick={handleSave}
              variant="contained"
              color="primary"
              disabled={states.isSavingTimeSlot}
              sx={{ minWidth: "100px", padding: "8px 16px" }}
            >
              {states.isSavingTimeSlot ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </Box>
      </Modal>

      <Dialog open={isAddSlotDialogOpen} onClose={handleCloseAddSlotDialog}>
        <DialogTitle>Thêm ca học</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Nhập số tiết cho ca học mới (Số tiết còn lại:{" "}
            {selectedClassForSlot?.duration || 0})
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            label="Số tiết"
            type="number"
            fullWidth
            value={selectedPeriods}
            onChange={(e) => setSelectedPeriods(e.target.value)}
            inputProps={{
              min: 1,
              max: selectedClassForSlot?.duration || 1,
            }}
          />
        </DialogContent>
        <DialogActions sx={{ padding: "16px", gap: "8px" }}>
          <Button
            onClick={handleCloseAddSlotDialog}
            variant="outlined"
            sx={{ minWidth: "80px", padding: "6px 16px" }}
          >
            Hủy
          </Button>
          <Button
            onClick={handleAddTimeSlot}
            variant="contained"
            color="primary"
            sx={{ minWidth: "80px", padding: "6px 16px" }}
          >
            Thêm
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={moveConfirmOpen} onClose={handleCancelMove}>
        <DialogTitle>Xác nhận di chuyển ca học</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Bạn có chắc chắn muốn di chuyển ca học này sang tiết {moveTarget?.targetStartTime} {moveTarget?.targetDay}?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelMove} color="secondary">
            Hủy
          </Button>
          <Button onClick={handleConfirmMove} color="primary" variant="contained">
            Xác nhận
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default TimeTable;
