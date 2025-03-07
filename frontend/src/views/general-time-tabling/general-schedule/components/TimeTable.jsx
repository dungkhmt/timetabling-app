import { useEffect, useState } from "react";
import { Checkbox } from "@mui/material";
import { useClassrooms } from "views/general-time-tabling/hooks/useClassrooms";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import { Autocomplete, Box, Button, CircularProgress, FormControl, Modal, TablePagination, TextField, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Tooltip, InputAdornment } from "@mui/material";
import { Add, Remove, Settings, Search } from "@mui/icons-material";
import {toast} from "react-toastify";

const TimeTable = ({
  classes,
  selectedSemester,
  selectedGroup,
  onSaveSuccess,
  loading,
  selectedRows,
  onSelectedRowsChange,
}) => {
  const [classDetails, setClassDetails] = useState([]);
  const [filteredClassDetails, setFilteredClassDetails] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [open, setOpen] = useState(false);
  const [selectedClass, setSelectedClass] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(100);
  const [isAddSlotDialogOpen, setIsAddSlotDialogOpen] = useState(false);
  const [selectedPeriods, setSelectedPeriods] = useState("");
  const [selectedClassForSlot, setSelectedClassForSlot] = useState(null);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [columnVisibility, setColumnVisibility] = useState(() => {
    // Load from localStorage or use defaults
    const savedSettings = localStorage.getItem('timetable-column-visibility');
    return savedSettings ? JSON.parse(savedSettings) : {
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
      actions: true
    };
  });

  const { classrooms } = useClassrooms(selectedGroup?.groupName || "", null);
  const { handlers, states } = useGeneralSchedule();

  console.log(classes);
  useEffect(() => {
    if (classes && classes.length > 0) {
      const transformedClassDetails = classes
        .map((cls) => ({
          weekDay: cls.weekday,
          roomReservationId: cls.roomReservationId,
          code: cls.classCode,
          crew: cls.crew,
          batch: cls.course,
          room: cls.room,
          timetable: {
            [convertWeekdayToDay(cls.weekday)]: {
              id: cls.classCode,
              room: cls.room,
              startTime: cls.startTime,
              endTime: cls.endTime,
            },
          },
          studyClass: cls.studyClass,
          listGroupName: cls.listGroupName, // Ensure listGroupName is mapped from API response
          learningWeeks: cls.learningWeeks,
          moduleCode: cls.moduleCode,
          moduleName: cls.moduleName,
          quantityMax: cls.quantityMax,
          classType: cls.classType,
          mass: cls.mass,
          duration: cls.duration,
          generalClassId: String(cls.id || ""),
          parentId: cls.parentClassId,
        }))
        .sort((a, b) => {
          if (a.code === b.code) {
            // Safely handle splitting by checking if generalClassId exists and contains "-"
            const getIdNumber = (str) => {
              if (!str || !str.includes("-")) return parseInt(str, 10);
              return parseInt(str.split("-")[0], 10);
            };

            const idA = getIdNumber(a.generalClassId);
            const idB = getIdNumber(b.generalClassId);

            return idB - idA;
          }
          return parseInt(a.code, 10) - parseInt(b.code, 10);
        });
      setClassDetails(transformedClassDetails);
      setFilteredClassDetails(transformedClassDetails);
    }
  }, [classes]);

  // Add effect to filter classes based on search term
  useEffect(() => {
    if (searchTerm.trim() === "") {
      setFilteredClassDetails(classDetails);
      setPage(0); // Reset to first page when search term changes
    } else {
      const lowercasedTerm = searchTerm.toLowerCase();
      const filtered = classDetails.filter(cls => {
        // Search in multiple fields
        return (
          (cls.code && cls.code.toLowerCase().includes(lowercasedTerm)) ||
          (cls.moduleCode && cls.moduleCode.toLowerCase().includes(lowercasedTerm)) ||
          (cls.moduleName && cls.moduleName.toLowerCase().includes(lowercasedTerm)) ||
          (cls.room && cls.room.toLowerCase().includes(lowercasedTerm)) ||
          (cls.classType && cls.classType.toLowerCase().includes(lowercasedTerm)) ||
          (cls.studyClass && cls.studyClass.toLowerCase().includes(lowercasedTerm)) ||
          (cls.crew && cls.crew.toString().includes(lowercasedTerm))
        );
      });
      setFilteredClassDetails(filtered);
      setPage(0); // Reset to first page when search term changes
    }
  }, [searchTerm, classDetails]);

  const days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const periods = [1, 2, 3, 4, 5, 6];

  const convertWeekdayToDay = (weekday) => {
    const dayMap = {
      2: "Mon",
      3: "Tue",
      4: "Wed",
      5: "Thu",
      6: "Fri",
      7: "Sat",
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
    const { numberOfPeriods, code, ...filteredSelectedClass } = selectedClass;

    const selectedClassData = {
      ...filteredSelectedClass,
      endTime: calculatedEndTime,
    };

    await handlers.handleSaveTimeSlot(selectedSemester.semester, selectedClassData);
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

      console.log('Selected class:', selectedClassForSlot); 

      await handlers.handleAddTimeSlot({
        generalClassId: selectedClassForSlot.generalClassId,
        parentId: selectedClassForSlot.roomReservationId,
        duration: periodsToAdd,
      });
      
      handleCloseAddSlotDialog();
      onSaveSuccess();
    } catch (error) {
      console.error("Error adding time slot:", error);
      toast.error(error.response?.data || "Thêm ca học thất bại!"); // Add better error handling
    }
  };

  const handleRemoveTimeSlot = async (generalClassId, roomReservationId) => {
    try {
      if (!generalClassId || !roomReservationId) {
        throw new Error('Missing required parameters');
      }
      await handlers.handleRemoveTimeSlot({
        generalClassId: generalClassId.toString(),
        roomReservationId: roomReservationId
      });
      onSaveSuccess();
    } catch (error) {
      console.error("Error removing time slot:", error);
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
    const classInfo = classDetails[classIndex]?.timetable?.[day];

    if (!classInfo) {
      return (
        <td
          key={`${classIndex}-${day}-${period}`}
          style={{ width: "40px" }}
          className="border border-gray-300 text-center cursor-pointer px-1 "
          onClick={() => handleRowClick(classIndex, day, period)}
        ></td>
      );
    }

    if (period === classInfo.startTime) {
      const colSpan = classInfo.endTime - classInfo.startTime + 1;

      return (
        <td
          key={`${classIndex}-${day}-${period}`}
          colSpan={colSpan}
          className="border border-gray-300 bg-yellow-400 text-center cursor-pointer px-1"
          style={{ width: `${40 * colSpan}px` }}
          onClick={() => handleRowClick(classIndex, day, period)}
        >
          <span className="text-[14px]">{classInfo.room}</span>
        </td>
      );
    }

    if (period > classInfo.startTime && period <= classInfo.endTime) {
      return null;
    }

    return (
      <td
        key={`${classIndex}-${day}-${period}`}
        style={{ width: "40px" }}
        className="border border-gray-300 text-center cursor-pointer px-1"
        onClick={() => handleRowClick(classIndex, day, period)}
      ></td>
    );
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleRowClick = (classIndex, day, period) => {
    const classInfo = classDetails[classIndex]?.timetable?.[day];

    if (classInfo) {
      setSelectedClass({
        roomReservationId: Number(classDetails[classIndex].roomReservationId),
        room: classDetails[classIndex].room,
        startTime: period,
        endTime: Number(classInfo.endTime),
        weekday: Number(classDetails[classIndex].weekDay),
        code: classDetails[classIndex].code,
        numberOfPeriods: classDetails[classIndex].duration,
      });
    } else {
      setSelectedClass({
        roomReservationId: Number(classDetails[classIndex].roomReservationId),
        room: classDetails[classIndex].room,
        startTime: period,
        endTime: undefined,
        weekday: days.indexOf(day) + 2,
        code: classDetails[classIndex].code,
        numberOfPeriods: classDetails[classIndex].duration,
      });
    }
    setOpen(true);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSelectAll = (event) => {
    if (event.target.checked) {
      const newSelected = filteredClassDetails.map(row => row.generalClassId);
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
      newSelected = selectedRows.filter(id => id !== generalClassId);
    }

    onSelectedRowsChange(newSelected);
  };

  const isSelected = (generalClassId) => selectedRows.indexOf(generalClassId) !== -1;

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
      [column]: !columnVisibility[column]
    };
    setColumnVisibility(newVisibility);
    localStorage.setItem('timetable-column-visibility', JSON.stringify(newVisibility));
  };

  const handleSaveSettings = () => {
    handleSettingsClose();
  };

  const columnDefinitions = [
    { id: 'classCode', label: 'Mã lớp' },
    { id: 'studyClass', label: 'Nhóm' },
    { id: 'learningWeeks', label: 'Tuần học' },
    { id: 'moduleCode', label: 'Mã học phần' },
    { id: 'moduleName', label: 'Tên học phần' },
    { id: 'crew', label: 'Kíp' },
    { id: 'quantityMax', label: 'SL MAX' },
    { id: 'classType', label: 'Loại lớp' },
    { id: 'mass', label: 'Thời lượng' },
    { id: 'duration', label: 'Số tiết' },
    { id: 'batch', label: 'Khóa' },
    { id: 'actions', label: 'Thêm/Xóa' }
  ];

  return (
    <div className="h-full w-full flex flex-col justify-start">
      <div className="flex justify-end mb-2 items-center gap-2">
        <TextField
          placeholder="Tìm kiếm (mã lớp, phòng, tên học phần...)"
          variant="outlined"
          size="small"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ 
            width: '300px',
            '& .MuiInputBase-root': {
              height: '36px'
            }
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
            height: '36px',
            textTransform: 'none'
          }}
        >
          Cài đặt hiển thị
        </Button>
      </div>

      {loading ? (
        <table
          className="overflow-x-auto flex items-center flex-col"
          style={{ flex: "1" }}
        >
          <thead>
            <tr>
              <th className="border border-gray-300 p-1" style={{ width: "30px", minWidth: "30px" }}>
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
              {columnVisibility.classCode && <th className="border border-gray-300 p-1" style={{ width: "60px", minWidth: "60px" }}>Mã lớp</th>}
              {columnVisibility.studyClass && <th className="border border-gray-300 p-1" style={{ width: "60px", minWidth: "60px" }}>Nhóm</th>}
              {columnVisibility.learningWeeks && <th className="border border-gray-300 p-1" style={{ width: "45px", minWidth: "45px" }}>Tuần học</th>}
              {columnVisibility.moduleCode && <th className="border border-gray-300 p-1" style={{ width: "70px", minWidth: "70px" }}>Mã học phần</th>}
              {columnVisibility.moduleName && <th className="border border-gray-300 p-1" style={{ width: "100px", minWidth: "100px" }}>Tên học phần</th>}
              {columnVisibility.crew && <th className="border border-gray-300 p-1" style={{ width: "40px", minWidth: "40px" }}>Kíp</th>}
              {columnVisibility.quantityMax && <th className="border border-gray-300 p-1" style={{ width: "50px", minWidth: "50px" }}>SL MAX</th>}
              {columnVisibility.classType && <th className="border border-gray-300 p-1" style={{ width: "60px", minWidth: "60px" }}>Loại lớp</th>}
              {columnVisibility.mass && <th className="border border-gray-300 p-1" style={{ width: "60px", minWidth: "60px" }}>Thời lượng</th>}
              {columnVisibility.duration && <th className="border border-gray-300 p-1" style={{ width: "50px", minWidth: "50px" }}>Số tiết</th>}
              {columnVisibility.batch && <th className="border border-gray-300 p-1" style={{ width: "50px", minWidth: "50px" }}>Khóa</th>}
              {columnVisibility.actions && (
                <>
                  <th className="border border-gray-300 p-1" style={{ width: "35px", minWidth: "35px" }}>Thêm</th>
                  <th className="border border-gray-300 p-1" style={{ width: "35px", minWidth: "35px" }}>Xóa</th>
                </>
              )}
              {days.map((day) => (
                <th
                  key={day}
                  colSpan={6}
                  className="border border-gray-300 p-2 text-center min-w-32"
                >
                  {day}
                </th>
              ))}
            </tr>
            <tr>
              <td></td> {/* Empty cell for checkbox column */}
              <td colSpan={Object.values(columnVisibility).filter(Boolean).length + (columnVisibility.actions ? 1 : 0)} className="border"></td>
              {days.flatMap((day) =>
                periods.map((period) => (
                  <td
                    key={`${day}-${period}`}
                    className="border border-gray-300 text-center"
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
          <table className="min-w-full" style={{ tableLayout: "auto" }}>
            <thead>
              <tr>
                <th className="border border-t-0 border-l-0 p-1" style={{ width: "30px", minWidth: "30px" }}>
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
                {columnVisibility.classCode && <th className="border border-t-0 border-l-0 p-1" style={{ width: "60px", minWidth: "60px" }}>Mã lớp</th>}
                {columnVisibility.studyClass && <th className="border border-t-0 p-1" style={{ width: "80px", minWidth: "80px" }}>Nhóm</th>}
                {columnVisibility.learningWeeks && <th className="border border-t-0 p-1" style={{ width: "45px", minWidth: "45px" }}>Tuần học</th>}
                {columnVisibility.moduleCode && <th className="border border-t-0 p-1" style={{ width: "70px", minWidth: "70px" }}>Mã học phần</th>}
                {columnVisibility.moduleName && <th className="border border-t-0 p-1" style={{ width: "100px", minWidth: "100px" }}>Tên học phần</th>}
                {columnVisibility.crew && <th className="border border-t-0 p-1" style={{ width: "40px", minWidth: "40px" }}>Kíp</th>}
                {columnVisibility.quantityMax && <th className="border border-t-0 p-1" style={{ width: "50px", minWidth: "50px" }}>SL MAX</th>}
                {columnVisibility.classType && <th className="border border-t-0 p-1" style={{ width: "60px", minWidth: "60px" }}>Loại lớp</th>}
                {columnVisibility.mass && <th className="border border-t-0 p-1" style={{ width: "60px", minWidth: "60px" }}>Thời lượng</th>}
                {columnVisibility.duration && <th className="border border-t-0 p-1" style={{ width: "50px", minWidth: "50px" }}>Số tiết</th>}
                {columnVisibility.batch && <th className="border border-t-0 p-1" style={{ width: "50px", minWidth: "50px" }}>Khóa</th>}
                {columnVisibility.actions && (
                  <>
                    <th className="border border-t-0 p-1" style={{ width: "35px", minWidth: "35px" }}>Thêm</th>
                    <th className="border border-t-0 p-1">Xóa</th>
                  </>
                )}
                {days.map((day) => (
                  <th
                    key={day}
                    colSpan={6}
                    className="border border-t-0 p-2 text-center min-w-32"
                    style={{ padding: "8px 0" }}
                  >
                    {day}
                  </th>
                ))}
              </tr>
              <tr>
                <td></td> 
                <td colSpan={Object.values(columnVisibility).filter(Boolean).length + (columnVisibility.actions ? 1 : 0)} className="border"></td>
                {days.flatMap((day) =>
                  periods.map((period) => (
                    <td
                      key={`${day}-${period}`}
                      className="border border-t-0 text-center"
                      style={{ width: "40px", padding: "4px" }}
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
                    
                    let groupDisplay = '';
                    let showTooltip = false;
                    let tooltipContent = '';
                    
                    if (classDetail.listGroupName) {
                      // If it's already an array
                      if (Array.isArray(classDetail.listGroupName)) {
                        const fullGroupText = classDetail.listGroupName.join(', ');
                        tooltipContent = fullGroupText;
                        groupDisplay = fullGroupText.length > 30 
                          ? fullGroupText.substring(0, 30) + '...' 
                          : fullGroupText;
                        showTooltip = fullGroupText.length > 30;
                      }
                      else if (typeof classDetail.listGroupName === 'string' && 
                              classDetail.listGroupName.includes('[') && 
                              classDetail.listGroupName.includes(']')) {
                        try {
                          const groups = JSON.parse(classDetail.listGroupName);
                          if (Array.isArray(groups)) {
                            const fullGroupText = groups.join(', ');
                            tooltipContent = fullGroupText;
                            groupDisplay = fullGroupText.length > 30 
                              ? fullGroupText.substring(0, 30) + '...' 
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
                        <td className="border border-l-0 text-center px-1">
                          <Checkbox
                            checked={isItemSelected}
                            onChange={(event) =>
                              handleSelectRow(event, classDetail.generalClassId)
                            }
                            size="small"
                          />
                        </td>
                        {columnVisibility.classCode && <td className="border text-center px-1">{classDetail.code}</td>}
                        {columnVisibility.studyClass && (
                          <td className="border text-center px-1 w-[80px] overflow-hidden text-ellipsis whitespace-nowrap">
                            {showTooltip ? (
                              <Tooltip title={tooltipContent} arrow placement="top">
                                <span>{groupDisplay}</span>
                              </Tooltip>
                            ) : (
                              <span>{groupDisplay}</span>
                            )}
                          </td>
                        )}
                        {columnVisibility.learningWeeks && <td className="border text-center px-1">{classDetail.learningWeeks}</td>}
                        {columnVisibility.moduleCode && <td className="border text-center px-1">{classDetail.moduleCode}</td>}
                        {columnVisibility.moduleName && <td className="border text-center px-1">{classDetail.moduleName}</td>}
                        {columnVisibility.crew && <td className="border text-center px-1">{classDetail.crew}</td>}
                        {columnVisibility.quantityMax && <td className="border text-center px-1">{classDetail.quantityMax}</td>}
                        {columnVisibility.classType && <td className="border text-center px-1">{classDetail.classType}</td>}
                        {columnVisibility.mass && <td className="border text-center px-1">{classDetail.mass}</td>}
                        {columnVisibility.duration && <td className="border text-center px-1">{classDetail.duration}</td>}
                        {columnVisibility.batch && <td className="border text-center px-1">{classDetail.batch}</td>}
                        {columnVisibility.actions && (
                          <>
                            <td className="border text-center px-1">
                              {!classDetail.timeSlots && (
                                <Button
                                  onClick={() => handleOpenAddSlotDialog(classDetail)}
                                  disabled={classDetail.duration <= 0}
                                  sx={{ 
                                    minWidth: '28px', 
                                    width: '28px', 
                                    height: '28px', 
                                    padding: '2px',
                                    borderRadius: '4px'
                                  }}
                                  color="primary"
                                  size="small"
                                >
                                  <Add fontSize="small" />
                                </Button>
                              )}
                            </td>
                            <td className="border text-center px-1">
                              {classDetail.roomReservationId && (
                                <Button
                                  onClick={() =>
                                    handleRemoveTimeSlot(
                                      classDetail.generalClassId,
                                      classDetail.roomReservationId
                                    )
                                  }
                                  sx={{ 
                                    minWidth: '28px', 
                                    width: '28px', 
                                    height: '28px', 
                                    padding: '2px',
                                    borderRadius: '4px'
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
                  <td></td> {/* Empty cell for checkbox column */}
                  <td
                    colSpan={Object.values(columnVisibility).filter(Boolean).length + days.length * periods.length + (columnVisibility.actions ? 1 : 0)}
                    className="text-center py-4"
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
                <label htmlFor={`column-${column.id}`} className="ml-2 cursor-pointer">
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
          <Button onClick={handleSaveSettings} color="primary" variant="contained">
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
              sx={{ minWidth: '100px', padding: '8px 16px' }}
            >
              Đóng
            </Button>
            <Button
              onClick={handleSave}
              variant="contained"
              color="primary"
              disabled={states.isSavingTimeSlot}
              sx={{ minWidth: '100px', padding: '8px 16px' }}
            >
              {states.isSavingTimeSlot ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </Box>
      </Modal>

      <Dialog 
        open={isAddSlotDialogOpen} 
        onClose={handleCloseAddSlotDialog}
      >
        <DialogTitle>Thêm ca học</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Nhập số tiết cho ca học mới (Số tiết còn lại: {selectedClassForSlot?.duration || 0})
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
              max: selectedClassForSlot?.duration || 1
            }}
          />
        </DialogContent>
        <DialogActions sx={{ padding: '16px', gap: '8px' }}>
          <Button 
            onClick={handleCloseAddSlotDialog}
            variant="outlined" 
            sx={{ minWidth: '80px', padding: '6px 16px' }}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleAddTimeSlot} 
            variant="contained" 
            color="primary"
            sx={{ minWidth: '80px', padding: '6px 16px' }}
          >
            Thêm
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default TimeTable;
