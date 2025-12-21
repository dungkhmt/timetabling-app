import { useEffect, useState } from "react";
import { Checkbox,MenuItem, InputLabel, Select } from "@mui/material";
import { useClassrooms } from "views/general-time-tabling/hooks/useClassrooms";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import useDebounce from "hooks/useDebounce";
import {request} from "api";

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
import { set } from "react-hook-form";

const TimeTableClassSegmentNew = ({
  classes,
  //getClasses,
  versionId,
  selectedSemester,
  selectedVersion, 
  selectedGroup,
  onSaveSuccess,
  //loading,
  //selectedRows,
  //onSelectedRowsChange,
  numberSlotsToDisplay, 
  searchCourseCode,
  searchClassCode,
  searchCourseName,
  searchGroupName
}) => {
  const [classWithClassSegments, setClassWithClassSegments] = useState([]);
  const [activePageClassWithClassSegments, setActivePageClassWithClassSegments] = useState([]);
  
  const [classDetails, setClassDetails] = useState([]);
  const [filteredClassDetails, setFilteredClassDetails] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const debouncedSearchTerm = useDebounce(searchTerm, 300); // 300ms delay
  const [open, setOpen] = useState(false);
  const [selectAll, setSelectAll] = useState(false);
  const [selectedClass, setSelectedClass] = useState(null);
  const [selectedClassSegmentIds, setSelectedClassSegmentIds] = useState([]);
  const [rowIndexSelected, setRowIndexSelected] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRows, setTotalRows] = useState(0);
  const [isAddSlotDialogOpen, setIsAddSlotDialogOpen] = useState(false);
  const [selectedPeriods, setSelectedPeriods] = useState("");
  const [selectedClassForSlot, setSelectedClassForSlot] = useState(null);
  
  const [openManualAssign,setOpenManualAssign] = useState(false);
  const [classrooms, setClassrooms] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [selectedDay, setSelectedDay] = useState(null);
  const [selectedStartTime, setSelectedStartTime] = useState(null);

  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const [draggedClass, setDraggedClass] = useState(null);
  const [moveConfirmOpen, setMoveConfirmOpen] = useState(false);
  const [moveTarget, setMoveTarget] = useState(null);

      const [scheduleTimeLimit, setScheduleTimeLimit] = useState(5);
      const [algorithm, setAlgorithm] = useState("");
      const [algorithms, setAlgorithms] = useState([]);
      const [daysSchedule, setDaysSchedule] = useState('2,3,4,5,6');
      const [slotsSchedule, setSlotsSchedule] = useState('1,2,3,4,5,6');
      const [openScheduleDialog, setOpenScheduleDialog] = useState(false);
      const [openClearScheduleDialog,setOpenClearScheduleDialog] = useState(false);
      const [errors, setErrors] = useState({});

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

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


  const { handlers, states } = useGeneralSchedule();

  const [selectedIds, setSelectedIds] = useState(new Set());
  
    // Derived state for the "Select All" logic
    const isAllSelected = classWithClassSegments.length > 0 && selectedIds.size === classWithClassSegments.length;
    const isAnySelected = selectedIds.size > 0;
    const isIndeterminate = isAnySelected && !isAllSelected;
  
    // Toggle a single row
    const toggleRow = (id) => {
      const newSelected = new Set(selectedIds);
      if (newSelected.has(id)) {
        newSelected.delete(id);
      } else {
        newSelected.add(id);
      }
      setSelectedIds(newSelected);
    };
  
    // Toggle all rows
    const toggleAll = () => {
      if (isAllSelected) {
        setSelectedIds(new Set());
      } else {
        setSelectedIds(new Set(classWithClassSegments.map((item) => item.classId)));
      }
    };

  function getAlgorithms(){
              request("get", 
                  "/general-classes/get-list-algorithm-names",
                  (res) => {
                      setAlgorithms(res.data);
                  }
              );
          }

          function performSchedule(){
              let ids = [];
              selectedIds.forEach(id => ids.push(id));
              let payLoadSchedule = {                  
                  timeLimit: scheduleTimeLimit,
                  ids: ids,
                  //ids: selectedIds,
                  //ids: selectedClassSegmentIds,
                  algorithm: algorithm,
                  versionId: Number(versionId),
                  days: daysSchedule,
                  slots: slotsSchedule
                  
              };
              setLoading(true);
             // alert('performSchdule, payload = ' + JSON.stringify(payLoadSchedule));
              request(
                  "post",
                  "/general-classes/auto-schedule-timeslot-room",
                  (res) => {
                      getClasses();
                      setOpenScheduleDialog(false);
                      setLoading(false);
                  },
                  null,
                  payLoadSchedule
              );
          }
          function performClearSchedule(){
              let ids = [];
              //for(let i=0;i<rowIndexSelected.length;i++){
              //    if(rowIndexSelected[i]){ ids.push(classWithClassSegments[i].classId); }
              //}
              selectedIds.forEach(id => ids.push(id));
              let payLoad = {
                  //ids: setSelectedClassSegmentIds//selectedRows
                  ids: ids
                  //ids: selectedIds
                };
              //alert('performClearSchedule, payload = ' + JSON.stringify(rowIndexSelected));
              request(
                         "post",
                         `/general-classes/reset-schedule?semester=`,
                         (res) => {
                            if(res.data == 'ok'){
                              getClasses();
                              //getAllClasses();
                            }else{
                            //  alert(res.data.message);
                            }
                         },
                         null,
                         payLoad,
                         {},
                         null,
                         null
                  );
              setOpenClearScheduleDialog(false);
          }
          function handleScheduleDialogClose(){
              setOpenScheduleDialog(false);
          }
  
  useEffect(() => {
      getClasses();
      getRooms();
      getAlgorithms();
      console.log('useEffect, classes = ',classes);
    
  }, []);

  // Use debounced search term for filtering
  useEffect(() => {
    
    
  }, []);

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

  function getRooms(){
    let body = {};
    request(
      "get",
      "/classroom/get-classrooms-of-version/"+ versionId,
      (res) => {
        console.log(res);
        setClassrooms(res.data || []);
        setLoading(false);
      },
      (error) => {
        console.error(error);
        setError(error);
        setLoading(false);
      }
    );
  }
  function handleViewUnscheduledClasses(){
      setLoading(true);
          request(
                              "get",
                              //"/general-classes/get-classes-with-class-segments-of-version?versionId=" + versionId
                              "/general-classes/get-unscheduled-class-segments-of-version-new?versionId=" + versionId
                              + "&searchCourseCode=" + searchCourseCode + "&searchCourseName=" + searchCourseName
                              + "&searchClassCode=" + searchClassCode + "&searchGroupName=" + searchGroupName,
                              (res)=>{
                                  console.log('get-classes-with-class-segments-of-version, res = ' + res.data);
                                  setClassWithClassSegments(res.data || []);
                                  setLoading(false);
                                  setTotalRows(res.data.length);
                                  setActivePageClassWithClassSegments(res.data.slice(0*rowsPerPage,0*rowsPerPage + rowsPerPage));
                                  setSelectedClassSegmentIds([]);
                                  let newRowIndexSelected = [];
                                  for(let i=0;i<res.data.length;i++){ newRowIndexSelected.push(false); }
                                  setRowIndexSelected(newRowIndexSelected);
                              },
                              (error)=>{
                                  console.error(error);
                                  setError(error);
                              },
                          );
  

  }

      function getClasses(){
          setLoading(true);
          request(
                              "get",
                              //"/general-classes/get-classes-with-class-segments-of-version?versionId=" + versionId
                              "/general-classes/get-class-segments-of-version-new?versionId=" + versionId
                              + "&searchCourseCode=" + searchCourseCode + "&searchCourseName=" + searchCourseName
                              + "&searchClassCode=" + searchClassCode + "&searchGroupName=" + searchGroupName,
                              (res)=>{
                                  console.log('get-classes-with-class-segments-of-version, res = ' + res.data);
                                  setClassWithClassSegments(res.data || []);
                                  setLoading(false);
                                  setTotalRows(res.data.length);
                                  setActivePageClassWithClassSegments(res.data.slice(0*rowsPerPage,0*rowsPerPage + rowsPerPage));
                                  setSelectedClassSegmentIds([]);
                                  let newRowIndexSelected = [];
                                  for(let i=0;i<res.data.length;i++){ newRowIndexSelected.push(false); }
                                  setRowIndexSelected(newRowIndexSelected);
                              },
                              (error)=>{
                                  console.error(error);
                                  setError(error);
                              },
                          );
  
      }
  
  const handleSaveManualAssign = async () => {
    /*
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
    */
    let payLoad = {
      versionId: versionId,
      classSegmentId: selectedClass.classId,
      session: selectedClass.session,
      //day: days.indexOf(selectedClass.day) + 2,
      day: selectedDay,
      startTime: selectedStartTime,
      duration: selectedClass.duration,
      roomCode: selectedRoom
    }; 
    request(
           "post",
           `/general-classes/manual-assign-timetable-class-segment`,
           (res) => {
              if(res.data.status == 'SUCCESS'){
                getClasses();
              }else{
                alert(res.data.message);
              }
           },
           null,
           payLoad,
           {},
           null,
           null
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
    //setSelectedRoom(e.target.value);

    const newValue =
      name === "startTime" || name === "endTime" || name === "duration"
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
  
  const classInfo = classes[actualIndex];
  //const timetable = classInfo?.timetable?.[day];
  //console.log('renderCellContent, index ',classIndex,' classInfo ',classInfo);
  if (!classInfo || !classInfo.startTime || !classInfo.endTime) {
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
  let dayIndex = days.indexOf(day)+2;
  if (dayIndex != classInfo.day || period < classInfo.startTime || period > classInfo.endTime) {
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

  if (period === classInfo.startTime) {
    const colSpan = classInfo.endTime - classInfo.startTime + 1;

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
        onDragStart={(e) => handleDragStart(e, actualIndex, day, {startTime:classInfo.startTime,endTime:classInfo.endTime})}
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
        <span className="text-[14px]">{classInfo.roomCode}</span>
      </td>
    );
  }

  return null;
};

const handleRowClick = (classIndex, day, period) => {
  //alert('click classIndex = ' + classIndex + ' day = ' + day + ' period = '  + period);
  setSelectedClass({
      id: Number(classes[classIndex].id),
      code: classes[classIndex].classCode,
      room: classes[classIndex].room,
      startTime: period,
      //endTime: Number(classInfo.endTime),
      day: days.indexOf(day) + 2,
      //code: classes[classIndex].classCode,
      duration: classes[classIndex].duration,
      session: classes[classIndex].session
    });
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
  const sourceStartTime = draggedClass.startTime;

  // Only suppress dialog if dropped on exactly the same position (same day AND same starting period)
  if (sourceIndex === targetIndex && sourceDay === targetDay && sourceStartTime === targetPeriod) {
    return;
  }
  
  const sourceClassInfo = classes[sourceIndex];
  const targetClassInfo = classes[targetIndex];

  // Set the move target with necessary information for the save operation
  
  
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
    let P = classWithClassSegments.slice(newPage*rowsPerPage,newPage*rowsPerPage + rowsPerPage);
    P.map((cls,index) =>{
      console.log('class ' + cls.classCode + ': ');
      cls.classSegments.map((cs,i) => {
        console.log(cs.day + '-' + cs.session + '-' + cs.startTime + '-' + cs.duration);
      })
    })
    setActivePageClassWithClassSegments(classWithClassSegments.slice(newPage*rowsPerPage,newPage*rowsPerPage + rowsPerPage));

  };

  const handleChangeRowsPerPage = (event) => {
    let rpp = parseInt(event.target.value, 10);
    setRowsPerPage(rpp);
    setPage(0);
    setActivePageClassWithClassSegments(classWithClassSegments.slice(0*rpp,0*rpp + rpp));
    
  };

  const 
  handleSelectAll = (event) => {
    //if (event.target.checked) {
    if(selectAll === false){
      setSelectAll(true);
      const newSelected = classWithClassSegments.map((row) => row.classId);
      //onSelectedRowsChange(newSelected);
      setSelectedClassSegmentIds(newSelected)
      let newRowIndexSelected = [];
      classWithClassSegments.map((row,index) => { newRowIndexSelected.push(true); });
      setRowIndexSelected(newRowIndexSelected);
      console.log('handleSelectAll, CHECKED -> rowIndexSelected = ' + JSON.stringify(rowIndexSelected));
    } else {
      setSelectAll(false);
      //onSelectedRowsChange([]);
      setSelectedClassSegmentIds([]);
      let newRowIndexSelected = [];
      classWithClassSegments.map((row,index) => { newRowIndexSelected.push(false); });
      setRowIndexSelected(newRowIndexSelected);
      console.log('handleSelectAll, UN-CHECKED -> rowIndexSelected = ' + JSON.stringify(rowIndexSelected));
    }


  };

  const handleSelectRow = (event, index, csId) => {
    let classSegmentId = activePageClassWithClassSegments[index].classId;
    //alert('select row csId = ' + classSegmentId + ' index = ' + index + ' checked = ' + event.target.checked);
    const selectedIndex = selectedClassSegmentIds.indexOf(classSegmentId);
    let newSelected = [];

    if (selectedIndex === -1) {
      newSelected = [...selectedClassSegmentIds, classSegmentId];
    } else {
      newSelected = selectedClassSegmentIds.filter((id) => id !== classSegmentId);
    }
    if(rowIndexSelected[index] === false){
      let newRowIndexSelected = [...rowIndexSelected];
      newRowIndexSelected[index] = true;
      setRowIndexSelected(newRowIndexSelected);
    }else{
      let newRowIndexSelected = [...rowIndexSelected];
      newRowIndexSelected[index] = false;
      setRowIndexSelected(newRowIndexSelected);
    }
    setSelectedClassSegmentIds(newSelected);
    //alert('selectedClassSegmentIds = ' + JSON.stringify(newSelected));

    /*
    const selectedIndex = selectedRows.indexOf(csId);
    let newSelected = [];

    if (selectedIndex === -1) {
      newSelected = [...selectedRows, csId];
    } else {
      newSelected = selectedRows.filter((id) => id !== csId);
    }

    onSelectedRowsChange(newSelected);
    */
  };

  const isSelected = (csId) => 
    //selectedRows.indexOf(csId) !== -1;
    selectedClassSegmentIds.indexOf(csId) !== -1;
  
  function handleConfirmManualAssign(){
    handleSaveManualAssign();
  }
  function handleCancelManualAssign(){
    setOpenManualAssign(false);
  }
  
  function handleCellClick(index, day, period){
    //let classSegmentId = classWithClassSegments[index].classId;
    setSelectedDay(day);
    setSelectedStartTime(period);
    setSelectedClass(classWithClassSegments[index]);
    //alert('click index = ' + index + ' classSgmentId = ' + classSegmentId + ' day = ' + day + ' period = '  + period);
    setOpenManualAssign(true);
  }
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
        <Button
          variant="outlined"
          onClick={handleViewUnscheduledClasses}
          size="small"
          sx={{
            height: "36px",
            textTransform: "none",
          }}
        >
          Unscheduled classes
        </Button>
        <Button
          variant="outlined"
          onClick={() => {setOpenScheduleDialog(true);}}
          size="small"
          sx={{
            height: "36px",
            textTransform: "none",
          }}
        >
          Auto Schedule
        </Button>
        <Button
          variant="outlined"
          onClick = {() =>{ setOpenClearScheduleDialog(true); }}
          size="small"
          sx={{
            height: "36px",
            textTransform: "none",
          }}
        >
          Clear Schedule
        </Button>
                    
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
              <input
                type="checkbox"
                checked={isAllSelected}
                ref={(el) => el && (el.indeterminate = isIndeterminate)}
                onChange={toggleAll}
              />
                {/*
                <Checkbox
                  indeterminate={
                    selectedClassSegmentIds.length > 0 &&
                    selectedClassSegmentIds.length < classWithClassSegments.length
                  }
                  checked={
                    selectAll
                    //classWithClassSegments.length > 0 &&
                    //selectedClassSegmentIds.length === classWithClassSegments.length
                  }
                  onChange={handleSelectAll}
                  size="small"
                />
                */}
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
              {days.flatMap((day) => [
                periods.map((period) => (
                  <td
                    key={`${day}-${period}`}
                    className="border-[1px] border-solid border-gray-300 text-center"
                    style={{ width: "60px", padding: "4px" }}
                  >
                    {period}
                  </td>
                ))
              ]
              )}
            </tr>
          </thead>
          <div className="flex justify-center items-center h-full w-full ">
            <CircularProgress />
          </div>
        </table>
      ) : (
        <div className="overflow-auto" style={{ flex: "1", maxHeight: "calc(100vh - 200px)" }}>
          <table
            className="min-w-full border-separate border-spacing-0"
            style={{ tableLayout: "auto" }}
          >
            <thead className="sticky top-0 z-10 bg-white" style={{ position: "sticky", top: 0 }}>
              <tr>
                <th
                  className="border-[1px] border-solid border-gray-300 p-1"
                  style={{ width: "30px", minWidth: "30px" }}
                >
                <input
                   type="checkbox"
                   checked={isAllSelected}
                   ref={(el) => el && (el.indeterminate = isIndeterminate)}
                  onChange={toggleAll}
                />
                  {/*
                  <Checkbox
                    indeterminate={
                      selectedClassSegmentIds.length > 0 &&
                      selectedClassSegmentIds.length < classWithClassSegments.length
                    }
                    checked={
                      selectAll
                      //classWithClassSegments.length > 0 &&
                      //selectedClassSegmentIds.length === classWithClassSegments.length
                    }
                    onChange={handleSelectAll}
                    size="small"
                  />
                  */}
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
                {days.flatMap((day) => [
                  periods.map((period) => (
                    <td
                      key={`${day}-${period}`}
                      className="border-[1px] border-solid border-gray-300 text-center"
                      style={{ width: "60px", padding: "4px" }} // Increased width from 40px to 60px
                    >
                      {period}
                    </td>
                  ))
                ]
                )}
              </tr>
            </thead>
            <tbody>
              {classWithClassSegments && classWithClassSegments.length > 0 ? (
                //classWithClassSegments
                //  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                activePageClassWithClassSegments
                  .map((classDetail, index) => {
                    const isItemSelected = isSelected(
                      classDetail.id
                    );

                    let groupDisplay = "";
                    let showTooltip = false;
                    let tooltipContent = "";

                    
                    return (
                      <tr
                        key={`${classDetail.classId}`}
                        style={{ height: "40px" }} // Reduced row height from 52px
                        className={isItemSelected ? "bg-blue-50" : ""}
                      >
                        <td style={{ padding: '12px' }}>
                          <input
                            type="checkbox"
                            checked={selectedIds.has(classDetail.classId)}
                            onChange={() => toggleRow(classDetail.classId)}
                          />
                        </td>
                        {/*
                        <td className="border border-gray-300 text-center px-1">
                          <Checkbox
                            //checked={isItemSelected}
                            checked = {rowIndexSelected[index]}
                            onChange={(event) =>
                              handleSelectRow(event, index, classDetail.id)
                            }
                            size="small"
                          />
                        </td>
                        */}
                        {columnVisibility.classCode && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.classCode}
                          </td>
                        )}
                        {columnVisibility.studyClass && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.groupNames}
                          </td>
                        )}
                        {columnVisibility.learningWeeks && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.learningWeeks}
                          </td>
                        )}
                        {columnVisibility.moduleCode && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.courseCode}
                          </td>
                        )}
                        {columnVisibility.moduleName && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.courseName}
                          </td>
                        )}
                        {columnVisibility.crew && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.session}
                          </td>
                        )}
                        {columnVisibility.quantityMax && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.maxNbStudents}
                            
                          </td>
                        )}
                        {columnVisibility.classType && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.classType}
                          </td>
                        )}
                        {columnVisibility.mass && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.volumn}
                          </td>
                        )}
                        {columnVisibility.duration && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.duration}
                          </td>
                        )}
                        {columnVisibility.batch && (
                          <td className="border border-gray-300 text-center px-1">
                            {classDetail.promotion}
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
                              {classDetail.id && (
                                <Button
                                  onClick={() =>
                                    handleRemoveTimeSlot(
                                      classDetail.classId,
                                      classDetail.id
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
                        {days.flatMap((day) => {
                          const dayIndex = days.indexOf(day) + 2;
                          //const allPeriods = [...periods, ...periods.map(p => p + effectiveSlots)];
                          const allPeriods = [...periods];
                          const renderedPeriods = new Set();
                          const cells = [];
                          
                          allPeriods.forEach((period) => {
                            // Skip if this period was already rendered as part of a colspan
                            if (renderedPeriods.has(period)) {
                              return;
                            }
                            
                            // Find matching class segment for this day and period
                            const matchingSegment = classDetail.classSegments?.find(
                              cs => cs.day === dayIndex && cs.startTime === period
                            );
                            
                            if (matchingSegment) {
                              // Mark all periods covered by this segment as rendered
                              for (let i = 0; i < matchingSegment.duration; i++) {
                                renderedPeriods.add(matchingSegment.startTime + i);
                              }
                              
                              cells.push(
                                <td
                                  key={`${classDetail.id}-${day}-${period}`}
                                  colSpan={matchingSegment.duration}
                                  style={{ width: `${70 * matchingSegment.duration}px`,backgroundColor: `${matchingSegment.color}` }}
                                  className="border border-gray-300 text-center cursor-pointer px-1"
                                >
                                  <span className="text-[14px]">{matchingSegment.roomCode}</span>
                                </td>
                              );
                            } else {
                              // Render empty cell
                              cells.push(
                                <td
                                  key={`${classDetail.id}-${day}-${period}`}
                                  style={{ width: "70px" }}
                                  className="border border-gray-300 text-center cursor-pointer px-1"
                                  onClick={() => handleCellClick(index, dayIndex, period)}

                                ></td>
                              );
                            }
                          });
                          
                          return cells;
                        })}
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
        count={totalRows}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[5, 25, 50, 100]}
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

      <Dialog open={openManualAssign} >
        <DialogTitle>Gan phong thu cong</DialogTitle>
        <DialogContent>
          <DialogContentText>
              <FormControl fullWidth margin="normal">
                <Autocomplete
                  options={classrooms.map((classroom) => classroom.classroom)}
                  value={selectedRoom || ""}
                  onChange={(event, newValue) => {
                    //alert('slected room ' + newValue);
                    //console.log('seleted room change, event',event);
                    setSelectedRoom(newValue);
                    //handleInputChange({
                    //  target: { name: "room", value: newValue },
                    //});
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
                value={selectedStartTime || ""}
                onChange={handleInputChange}
                fullWidth
                margin="normal"
              />
              
              <TextField
                label="Ngày"
                name="day"
                type="number"
                value={selectedDay || ""}
                
                fullWidth
                margin="normal"
                InputProps={{
                  readOnly: true,
                }}
              />
            
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelManualAssign} color="secondary">
            Hủy
          </Button>
          <Button onClick={handleConfirmManualAssign} color="primary" variant="contained">
            Xác nhận
          </Button>
        </DialogActions>
      </Dialog>


      <Dialog
                      open={openScheduleDialog}
                      onClose={handleScheduleDialogClose}
                              
                  >
                  <DialogTitle>Schedule settings</DialogTitle>
      
                      <DialogContent>
                          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                              <TextField
                                  label="Time Limit "
                                  name="scheduleTimeLimit"
                                  value={scheduleTimeLimit}
                                  onChange={(e) => {setScheduleTimeLimit(e.target.value)}}
                                  size="small"
                                  sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                  required
                                  error={!!errors.scheduleTimeLimit}
                                  helperText={errors.scheduleTimeLimit}
                              />
                              <FormControl fullWidth>
                                  <InputLabel id="algo">Thuật toán</InputLabel>
                                  <Select
                                      labelId="algo"
                                      id="algo"
                                      value={algorithm}
                                      label="algoritm"
                                      onChange={(e) => {setAlgorithm(e.target.value)}}
                                  >
                                  {algorithms.map((algo) => (
                                      <MenuItem key={algo} value={algo}>
                                          {algo}
                                      </MenuItem>
                                  ))}   
                                  
                                  </Select>
                              </FormControl>
                          </Box> 
                          
                          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                              <TextField
                                  label="days"
                                  name="days"
                                  value={daysSchedule}
                                  onChange={(e) => {setDaysSchedule(e.target.value)}}
                                  size="small"
                                  sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                  required
                                  error={!!errors.days}
                                  helperText={errors.days}
                              />
                              <TextField
                                  label="slots"
                                  name="slots"
                                  value={slotsSchedule}
                                  onChange={(e) => {setSlotsSchedule(e.target.value)}}
                                  size="small"
                                  sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                  required
                                  error={!!errors.slots}
                                  helperText={errors.slots}
                              />
                          </Box> 
      
                      </DialogContent>
                      <DialogActions sx={{
                              padding: "16px",
                              gap: "8px",
                              borderTop: '1px solid #e0e0e0',
                              backgroundColor: '#fafafa'
                          }}>
                          <Button
                              onClick={() =>{
                                  //setOpenScheduleDialog(false);
                                  performSchedule();
                              }}
                              variant="outlined"
                              sx={{
                                  minWidth: "100px",
                                  padding: "8px 16px",
                                  textTransform: 'none'
                              }}
                          >
                              RUN
                          </Button>
                      </DialogActions>
                  </Dialog>

            <Dialog
                open={openClearScheduleDialog}
                
                        
            >
            <DialogTitle>Clear Schedule </DialogTitle>

                <DialogContent>
                    

                </DialogContent>
                <DialogActions sx={{
                        padding: "16px",
                        gap: "8px",
                        borderTop: '1px solid #e0e0e0',
                        backgroundColor: '#fafafa'
                    }}>
                    <Button
                        onClick={() =>{
                            performClearSchedule();
                        }}
                        variant="outlined"
                        sx={{
                            minWidth: "100px",
                            padding: "8px 16px",
                            textTransform: 'none'
                        }}
                    >
                        YES
                    </Button>
                    <Button
                        onClick={() =>{
                            setOpenClearScheduleDialog(false);
                        }}
                        variant="outlined"
                        sx={{
                            minWidth: "100px",
                            padding: "8px 16px",
                            textTransform: 'none'
                        }}
                    >
                        NO
                    </Button>
                    
                </DialogActions>
            </Dialog>

      
    </div>
  );
};

export default TimeTableClassSegmentNew;
