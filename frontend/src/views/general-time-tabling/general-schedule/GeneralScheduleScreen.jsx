import { useGeneralSchedule } from "services/useGeneralScheduleData";
import { useTimeTablingVersionData } from "services/useTimeTablingVersionData";
import AutoScheduleDialog from "./components/AutoScheduleDialog";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import GeneralGroupAutoComplete from "../common-components/GeneralGroupAutoComplete";
import GeneralClusterAutoComplete from "../common-components/GeneralClusterAutoComplete";
import VersionSelectionScreen from "../version-selection/VersionSelectionScreen";
import { Button, Tabs, Tab, Chip, Divider, Paper, Typography, Box, TextField } from "@mui/material";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import TimeTable from "./components/TimeTable";
import ConsolidatedTimeTable from "./components/ConsolidatedTimeTable";
import SessionTimeTable from "./components/SessionTimeTable";
import RoomOccupationScreen from "../room-occupation/RoomOccupationScreen";
import { useState, useMemo, useEffect } from "react";
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import { Clear, ArrowBack } from "@mui/icons-material";
import {toast} from "react-toastify";
import { request } from "api";
import { StandardTable } from "erp-hust/lib/StandardTable";


const GeneralScheduleScreen = () => {
  const { states, setters, handlers } = useGeneralSchedule();
  const [viewTab, setViewTab] = useState(0);
  const [openResetConfirm, setOpenResetConfirm] = useState(false);
  const [isMaxDayHovered, setIsMaxDayHovered] = useState(false);
  const [selectedCluster, setSelectedCluster] = useState(null);
  const [filteredClasses, setFilteredClasses] = useState([]);
  const [consolidatedCount, setConsolidatedCount] = useState(0);
  const [sessionViewCount, setSessionViewCount] = useState(0);
  
  const [selectedVersion, setSelectedVersion] = useState(null);
  const [showVersionSelection, setShowVersionSelection] = useState(true);
  const [openNewVersionDialog, setOpenNewVersionDialog] = useState(false);
  const [newVersionName, setNewVersionName] = useState("");
  const [newVersionStatus, setNewVersionStatus] = useState("DRAFT");
  const[isDeletingBySemester, setIsDeletingBySemester] = useState(false);
  const[isCreateBySemester, setIsCreateBySemester] = useState(false);  
  const[openSearchRoom, setOpenSearchRoom] = useState(false);
  const[searchRoomCapacity, setSearchRoomCapacity] = useState(90);
  const[searchRoomData, setSearchRoomData] = useState([]);
  const[roomNameFilter, setRoomNameFilter] = useState("");
  const[filteredRoomData, setFilteredRoomData] = useState([]);

  const[openAdvancedFilter, setOpenAdvancedFilter] = useState(false);
  const[filterMinQty, setFilterMinQty] = useState(0);
  const[filterMaxQty, setFilterMaxQty] = useState(0);
  const[filterCourseCodes, setFilterCourseCodes] = useState("");
  const[filterClassTypes, setFilterClassTypes] = useState("");
  const[isFilterApplied, setIsFilterApplied] = useState(false);
  const[originalClasses, setOriginalClasses] = useState([]);
  
  
  const[currentTimeSlot, setCurrentTimeSlot] = useState({
    session: '',
    day: '',
    startPeriod: '',
    duration: '',
    logic: 'OR' 
  });
  const[timeSlotList, setTimeSlotList] = useState([]);

  const processClassData = (data) => {
    if (!Array.isArray(data)) {
      return [];
    }
  
    let generalClasses = [];
    data.forEach((classObj) => {
      if (classObj.timeSlots) {
        classObj.timeSlots.forEach((timeSlot, index) => {
          if (timeSlot.duration !== null) {
            const cloneObj = JSON.parse(
              JSON.stringify({
                ...classObj,
                ...timeSlot,
                classCode: classObj.classCode,
                roomReservationId: timeSlot.id,
                id: classObj.id + `-${index + 1}`,
                crew: classObj.crew,
                duration: timeSlot.duration,
                isChild: true,
                parentId: classObj.id,
              })
            );
            delete cloneObj.timeSlots;
            generalClasses.push(cloneObj);
          }
        });
      }
    });
  
    return generalClasses;
  };

  const searchRoomColumns = [
    {
      title: "ID",
      field: "id"
    },
    {
      title: "Qty",
      field: "quantityMax"
    }
  ];

  const days = [2, 3, 4, 5, 6, 7, 8];

  const getDayName = (day) => {
    switch (day) {
      case 2:
        return "Thứ 2";
      case 3:
        return "Thứ 3";
      case 4:
        return "Thứ 4";
      case 5:
        return "Thứ 5";
      case 6:
        return "Thứ 6";
      case 7:
        return "Thứ 7";
      case 8:
        return "Chủ nhật";
      default:
        return `Ngày ${day}`;
    }
  };

  const { 
    states: { versions, isLoading: isVersionsLoading, isCreating, searchName, selectedSemester: versionSelectedSemester },
    setters: { setSearchName, setSelectedSemester: setVersionSelectedSemester },
    handlers: { fetchVersions, createVersion }
  } = useTimeTablingVersionData();

  const handleVersionSelect = (version) => {
    setSelectedVersion(version);
    setters.setVersionId(version.id);
    
    if (version && version.semester) {
      const versionSemester = { semester: version.semester };
      setters.setSelectedSemester(versionSemester);
    }
    
    setShowVersionSelection(false);
  };

  const handleBackToVersionSelection = () => {
    setShowVersionSelection(true);
  };
  useEffect(() => {
    const filterClassesByCluster = async () => {
      if (selectedCluster) {
        setters.setSelectedGroup(null);
        
        if (isFilterApplied) {
          setIsFilterApplied(false);
          setFilterMinQty(0);
          setFilterMaxQty(0);
          setFilterCourseCodes("");
          setFilterClassTypes("");
          setOriginalClasses([]); 
        }
        
        const clusterClasses = await handlers.getClassesByCluster(selectedCluster.id, selectedVersion?.id);
        setFilteredClasses(clusterClasses);
      } else {
        if (!isFilterApplied) {
          setFilteredClasses([]);
        }
      }
    };
    filterClassesByCluster();
  }, [selectedCluster, states.classes]);

  const handleConfirmReset = async () => {
    await handlers.handleResetTimeTabling();
    setOpenResetConfirm(false);
    setters.setSelectedRows([]);
  };

  const handleAutoScheduleSelectedWithClear = async () => {
    const currentVersionId = selectedVersion?.id;
    
    console.log("Xếp lịch với version_id:", currentVersionId);
    
    await handlers.handleAutoScheduleSelected(currentVersionId);
    setters.setSelectedRows([]);
  };

  const handleSetConsolidatedCount = (count) => {
    setConsolidatedCount(count);
  };

  const handleSetSessionViewCount = (count) => {
    setSessionViewCount(count);
  };

  const isSchedulingInProgress =
    states.isAutoSaveLoading || states.isTimeScheduleLoading || states.loading;  const unscheduledClasses = useMemo(() => {
    if (isFilterApplied) {
      return filteredClasses.filter((cls) => cls.room === null);
    }
    if (selectedCluster) {
      return filteredClasses.filter((cls) => cls.room === null);
    }
    if (!states.classes || states.classes.length === 0) return [];
    return states.classes.filter((cls) => cls.room === null);
  }, [states.classes, filteredClasses, selectedCluster, isFilterApplied]);
  
  const displayClasses = useMemo(() => {
    const result = isFilterApplied ? filteredClasses : (selectedCluster ? filteredClasses : states.classes);
    console.log('displayClasses updated:', {
      isFilterApplied,
      filteredClassesLength: filteredClasses?.length || 0,
      selectedCluster: selectedCluster?.name || null,
      statesClassesLength: states.classes?.length || 0,
      resultLength: result?.length || 0
    });
    return result;
  
  }, [isFilterApplied, filteredClasses, selectedCluster, states.classes]);
  
  function handleCreateSegment() {
    if (!states.selectedSemester || !states.selectedSemester.semester) {
      toast.error("Vui lòng chọn học kỳ trước khi tạo ca học");
      return;
    }
    
    let body = {
      semester: states.selectedSemester.semester,
      versionId: selectedVersion?.id || null
    };

    setIsCreateBySemester(true);
    
    request(
      "post",
      "/general-classes/create-class-segments",
      (res) => {
        console.log('create class-segments returned ', res.data);
        setters.setSelectedRows([]);
        toast.success("Đã tạo ca học thành công");
        setTimeout(() => {
          handlers.handleRefreshClasses();
          setIsCreateBySemester(false);
        }, 500);
      },
      {
        onError: (e) => {
          setters.setSelectedRows([]);
          toast.error("Có lỗi khi tạo ca học: " + (e.response?.data || e.message));
          setIsCreateBySemester(false);
        }
      },
      body
    );
  }

  function handleRemoveSegment() {
    if (!states.selectedSemester || !states.selectedSemester.semester) {
      toast.error("Vui lòng chọn học kỳ trước khi xóa ca học");
      return;
    }
    
    let body = {
      semester: states.selectedSemester.semester,
      versionId: selectedVersion?.id || null
    };

    setIsDeletingBySemester(true);
    
    request(
      "post",
      "/general-classes/remove-class-segments",
      (res) => {
        console.log('remove class-segments returned ', res.data);
        setters.setSelectedRows([]);
        toast.success("Đã xóa ca học thành công");
        setTimeout(() => {
          handlers.handleRefreshClasses();
          setIsDeletingBySemester(false);
        }, 500);
      },
      {
        onError: (e) => {
          setters.setSelectedRows([]);
          toast.error("Có lỗi khi xóa ca học: " + (e.response?.data || e.message));
          setIsDeletingBySemester(false);
        }
      },
      body
    );  
}
  const handleChangeSearchRoomCapacity = (event) => {
    setSearchRoomCapacity(event.target.value);
  };
  
  // New handlers for single form approach
  const handleCurrentTimeSlotChange = (field, value) => {
    setCurrentTimeSlot(prev => ({ ...prev, [field]: value }));
  };

  const handleAddToTimeSlotList = () => {
    // Validate current time slot
    if (!currentTimeSlot.session || !currentTimeSlot.day || !currentTimeSlot.startPeriod || !currentTimeSlot.duration) {
      toast.error("Vui lòng điền đầy đủ thông tin ca học");
      return;
    }

    // Validate time slot
    const validationError = validateCurrentTimeSlot();
    if (validationError) {
      toast.error(validationError);
      return;
    }

    // Add to list
    const newTimeSlot = {
      id: Date.now(),
      ...currentTimeSlot
    };
    setTimeSlotList(prev => [...prev, newTimeSlot]);

    // Clear form (keep logic for next slot)
    setCurrentTimeSlot({
      session: '',
      day: '',
      startPeriod: '',
      duration: '',
      logic: currentTimeSlot.logic // Keep the same logic for convenience
    });
  };

  const handleRemoveLastTimeSlot = () => {
    if (timeSlotList.length > 0) {
      setTimeSlotList(prev => prev.slice(0, -1));
    }
  };

  const handleClearAllTimeSlots = () => {
    setTimeSlotList([]);
    setCurrentTimeSlot({
      session: '',
      day: '',
      startPeriod: '',
      duration: '',
      logic: 'OR'
    });
  };  const generateTimeSlotString = () => {
    if (timeSlotList.length === 0) return '';
    
    let result = '';
    timeSlotList.forEach((slot, index) => {
      const slotString = `${slot.session}-${slot.day}-${slot.startPeriod}-${slot.duration}`;
      
      if (index === 0) {
        result = slotString;
      } else {
        const separator = slot.logic === 'AND' ? ':' : ';';
        result += separator + slotString;
      }
    });
    
    return result;
  };

  const getTimeSlotMeaning = () => {
    if (timeSlotList.length === 0) return 'Chưa có ca học nào được thêm vào';
    
    let result = '';
    timeSlotList.forEach((slot, index) => {
      const sessionText = slot.session === 'S' ? 'Sáng' : 'Chiều';
      const dayText = getDayName(parseInt(slot.day));
      const endPeriod = parseInt(slot.startPeriod) + parseInt(slot.duration) - 1;
      const meaning = `${sessionText} ${dayText}, tiết ${slot.startPeriod}-${endPeriod} (${slot.duration} tiết)`;
      
      if (index === 0) {
        result = meaning;
      } else {
        const connector = slot.logic === 'AND' ? ' VÀ ' : ' HOẶC ';
        result += connector + meaning;
      }
    });
    
    return result;
  };

  const validateCurrentTimeSlot = () => {
    if (!currentTimeSlot.session || !currentTimeSlot.day || !currentTimeSlot.startPeriod || !currentTimeSlot.duration) {
      return "Vui lòng điền đầy đủ thông tin";
    }
    
    const numberSlotsPerSession = selectedVersion?.numberSlotsPerSession || 6;
    const startPeriod = parseInt(currentTimeSlot.startPeriod);
    const duration = parseInt(currentTimeSlot.duration);
    
    if (startPeriod + duration - 1 > numberSlotsPerSession) {
      return `Tiết bắt đầu (${startPeriod}) + Số tiết (${duration}) > Tối đa (${numberSlotsPerSession})`;
    }
    
    return null;
  };  
    const handleClickSearchRoom = () => {
    // Reset form when opening dialog
    setCurrentTimeSlot({
      session: '',
      day: '',
      startPeriod: '',
      duration: '',
      logic: 'OR'
    });
    setTimeSlotList([]);
    setSearchRoomData([]);
    setRoomNameFilter("");
    setFilteredRoomData([]);
    setOpenSearchRoom(true);
  };  const handleClickAdvancedFilter = () => {
      setOpenAdvancedFilter(true);
  }
  
  const handleRoomNameFilterChange = (event) => {
    setRoomNameFilter(event.target.value);
  };
  
  useEffect(() => {
    if (!searchRoomData || searchRoomData.length === 0) {
      setFilteredRoomData([]);
      return;
    }
    
    if (!roomNameFilter.trim()) {
      setFilteredRoomData(searchRoomData);
      return;
    }
    
    const filtered = searchRoomData.filter(room => 
      room.id && room.id.toLowerCase().includes(roomNameFilter.toLowerCase().trim())
    );
    setFilteredRoomData(filtered);
  }, [roomNameFilter, searchRoomData]);
  
  const handleChangeFilterMaxQty = (e) => {
    setFilterMaxQty(e.target.value);
  }
  const handleChangeFilterMinQty = (e) => {
    setFilterMinQty(e.target.value);
}  
  const handleChangeCourseCodes = (e) => {
    setFilterCourseCodes(e.target.value);
  };
  const handleChangeClassTypes = (e) => {
    setFilterClassTypes(e.target.value);
  };
  
  const performAdvancedFilter = () => {
    if (!isFilterApplied && states.classes?.length > 0) {
      setOriginalClasses([...states.classes]);
    }

    let body = {
      filterMinQty: filterMinQty,
      filterMaxQty: filterMaxQty,
      filterCourseCodes: filterCourseCodes,
      filterClassTypes: filterClassTypes,
      versionId: selectedVersion.id
    };      
    request(
      "post",
      "/general-classes/advanced-filter",
      (res) => {
        console.log('Advanced Filter Result: ', res.data);
        if (res.data && Array.isArray(res.data)) {
          const processedData = processClassData(res.data);
          setFilteredClasses(processedData);
          setIsFilterApplied(true);
          setSelectedCluster(null); 
          
          setOpenAdvancedFilter(false);
          setters.setSelectedRows([]);
          
          toast.success(`Đã lọc thành công! Tìm thấy ${processedData.length} lớp học phù hợp.`);
        } else {
          toast.warning("Không tìm thấy lớp học nào phù hợp với điều kiện lọc!");
        }
      },
      {
        onError: (e) => {
          console.error("Advanced Filter Error:", e);
          toast.error("Có lỗi khi thực hiện bộ lọc nâng cao: " + (e.response?.data?.message || e.message));
        }
      },
      body
    );
  }
  const resetAdvancedFilter = () => {
    if (originalClasses.length > 0) {
      setFilteredClasses([...originalClasses]);
      setIsFilterApplied(false);
      setFilterMinQty(0);
      setFilterMaxQty(0);
      setFilterCourseCodes("");
      setFilterClassTypes("");
      setters.setSelectedRows([]);
      setOriginalClasses([]); 
      toast.success("Đã reset bộ lọc và hiển thị tất cả lớp học!");
    }
  }

  const performSearchRoom = () => {
    const generatedTimeSlots = generateTimeSlotString();
    
    if (!generatedTimeSlots) {
      toast.error("Vui lòng thêm ít nhất một ca học vào chuỗi tìm kiếm");
      return;
    }
    
    let body = {
      searchRoomCapacity: searchRoomCapacity,
      timeSlots: generatedTimeSlots,
      versionId: selectedVersion.id
    };
      request(
      "post",
      "/general-classes/search-rooms",
      (res) => {
        console.log('Search Room: ', res.data);
        setSearchRoomData(res.data);
        setFilteredRoomData(res.data); 
        toast.success("Tìm phòng thành công");
      },
      {
        onError: (e) => {
          toast.error("Có lỗi khi tìm phòng");
        }
      },
      body
    );
  };
  if (showVersionSelection) {
    return (
      <VersionSelectionScreen
        selectedSemester={versionSelectedSemester}
        setSelectedSemester={setVersionSelectedSemester}
        searchName={searchName}
        setSearchName={setSearchName}
        versions={versions}
        isLoading={isVersionsLoading}
        handleVersionSelect={handleVersionSelect}
        openNewVersionDialog={openNewVersionDialog}
        setOpenNewVersionDialog={setOpenNewVersionDialog}
        newVersionName={newVersionName}
        setNewVersionName={setNewVersionName}
        newVersionStatus={newVersionStatus}
        setNewVersionStatus={setNewVersionStatus}
        isCreating={isCreating}
        createVersion={createVersion} 
        onCreateSuccess={(createdVersion) => {
          fetchVersions();
        }}
      />
    );
  }

  // Main scheduling UI (original content)
  const numberSlotsToDisplay = selectedVersion?.numberSlotsPerSession ?? 6;

  return (
    <div className="flex flex-col gap-3">
      <Paper elevation={1} className="p-3">
        <Box className="flex justify-between items-center mb-3">
          <Button
            startIcon={<ArrowBack />}
            onClick={handleBackToVersionSelection}
            variant="outlined"
            size="small"
            sx={{
              textTransform: 'none',
              fontSize: { xs: '0.75rem', sm: '0.875rem' }
            }}
          >
            Quay lại
          </Button>
          
          {selectedVersion && (
            <Box className="flex flex-col items-end">
              <Box className="flex items-center gap-2 flex-wrap">
                <Typography 
                  variant="h6" 
                  className="font-semibold"
                  sx={{
                    fontSize: { xs: '0.9rem', sm: '1.1rem', md: '1.25rem' },
                    maxWidth: { xs: '120px', sm: '200px', md: '300px' },
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap'
                  }}
                >
                  {selectedVersion.name}
                </Typography>
                <Chip 
                  label={selectedVersion.status} 
                  color={selectedVersion.status === "DRAFT" ? "default" : "success"} 
                  size="small" 
                />
                <Typography 
                  variant="body2" 
                  color="text.secondary"
                  sx={{
                    fontSize: { xs: '0.7rem', sm: '0.75rem', md: '0.875rem' }
                  }}
                >
                  Học kỳ: {selectedVersion.semester}
                </Typography>
              </Box>
            </Box>
          )}
        </Box>
        
        <Divider className="mb-3" />
        
        <Tabs
          value={viewTab}
          onChange={(e, newVal) => setViewTab(newVal)}
          sx={{
            borderBottom: 1,
            borderColor: "divider",
            "& .MuiTab-root": {
              minWidth: "140px",
              fontWeight: 500,
              textTransform: "none",
              fontSize: "15px",
              py: 1.5,
            },
          }}
        >
          <Tab
            label={
              <div className="flex items-center gap-2">
                <span>Tất cả lớp học</span>
                <Chip
                  size="small"
                  label={displayClasses?.length || 0}
                  color="default"
                />
              </div>
            }
          />
          <Tab
            label={
              <div className="flex items-center gap-2">
                <span>Lớp chưa xếp</span>
                <Chip
                  size="small"
                  label={unscheduledClasses.length}
                  color="error"
                />
              </div>
            }
          />
          <Tab label="Xem theo phòng" />
          <Tab
            label={
              <div className="flex items-center gap-2">
                <span>Gộp lớp học</span>
                <Chip
                  size="small"
                  label={consolidatedCount || displayClasses?.length || 0}
                  color="default"
                />
              </div>
            }
          />
          <Tab
            label={
              <div className="flex items-center gap-2">
                <span>Xem theo buổi</span>
                <Chip
                  size="small"
                  label={sessionViewCount || displayClasses?.length || 0}
                  color="default"
                />
              </div>
            }
          />
        </Tabs>

        {(viewTab === 0 || viewTab === 1 || viewTab === 3 || viewTab === 4) && (
          <div className="mt-3">
            <Paper variant="outlined" className="p-3">
              <div className="flex gap-3 items-center flex-wrap">
              <TextField
                width="200px"
                size="small"
                height="36px"
                label="Kỳ học"
                value={selectedVersion?.semester || ""}
                disabled
                InputProps={{
                  readOnly: true,
                }}
              />

                <GeneralGroupAutoComplete
                  selectedGroup={states.selectedGroup}
                  setSelectedGroup={(group) => {
                    setters.setSelectedGroup(group);
                    setSelectedCluster(null);
                  }}
                  sx={{
                    minWidth: 200,
                    "& .MuiInputBase-root": { height: "40px" },
                  }}
                  disabled={selectedCluster !== null}
                />

                {(viewTab === 3 || viewTab === 4) && (
                  <GeneralClusterAutoComplete
                    selectedCluster={selectedCluster}
                    setSelectedCluster={setSelectedCluster}
                    selectedSemester={states.selectedSemester}
                    sx={{
                      minWidth: 200,
                      "& .MuiInputBase-root": { height: "40px" },
                    }}
                  />
                )}

                {viewTab !== 3 && viewTab !== 4 && (
                  <>
                    <FormControl
                      sx={{
                        minWidth: 200,
                        "& .MuiInputBase-root": { height: "40px" },
                      }}
                      size="small"
                      disabled={states.isAlgorithmsLoading}
                    >
                      <InputLabel id="algorithm-select-label">
                        Chọn thuật toán
                      </InputLabel>
                      <Select
                        labelId="algorithm-select-label"
                        id="algorithm-select"
                        value={states.selectedAlgorithm}
                        onChange={(e) =>
                          setters.setSelectedAlgorithm(e.target.value)
                        }
                        label="Chọn thuật toán"
                      >
                        {states.algorithms.map((algorithm, index) => (
                          <MenuItem key={index} value={algorithm}>
                            {algorithm}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>

                    <FormControl
                      sx={{
                        minWidth: 200,
                        "& .MuiInputBase-root": { height: "40px" },
                      }}
                      size="small"
                      disabled={states.isAlgorithmsLoading}
                      onMouseEnter={() => setIsMaxDayHovered(true)}
                      onMouseLeave={() => setIsMaxDayHovered(false)}
                    >
                      <InputLabel id="max-day-schedule-label">
                        Chọn ngày muộn nhất
                      </InputLabel>
                      <Select
                        labelId="max-day-schedule-label"
                        id="max-day-schedule-select"
                        value={states.maxDaySchedule || ""}
                        onChange={(e) =>
                          setters.setMaxDaySchedule(e.target.value || null)
                        }
                        label="Chọn ngày muộn nhất"
                        renderValue={(selected) =>
                          selected ? getDayName(selected) : "Không chọn"
                        }
                        endAdornment={
                          states.maxDaySchedule &&
                          isMaxDayHovered && (
                            <Button
                              sx={{
                                position: "absolute",
                                right: 36,
                                top: "50%",
                                transform: "translateY(-50%)",
                                minWidth: "auto",
                                p: 0.5,
                                borderRadius: "50%",
                                width: "22px",
                                height: "22px",
                                color: "gray",
                              }}
                              onClick={(e) => {
                                e.stopPropagation();
                                setters.setMaxDaySchedule(null);
                              }}
                            >
                              <Clear fontSize="small" />
                            </Button>
                          )
                        }
                      >
                        {days.map((day) => (
                          <MenuItem key={day} value={day}>
                            {getDayName(day)}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </>
                )}
              </div>
            </Paper>

            <div className="sticky top-0 z-10 bg-white py-3 mt-3 border-b">
              <div className="flex justify-between gap-2">
                {viewTab !== 3 && viewTab !== 4 && (
                  <GeneralClusterAutoComplete
                    selectedCluster={selectedCluster}
                    setSelectedCluster={setSelectedCluster}
                    selectedSemester={states.selectedSemester}
                    sx={{
                      minWidth: 200,
                      "& .MuiInputBase-root": { height: "40px" },
                    }}
                  />
                )}
                <div className={`flex md:flex-row flex-col ${viewTab !== 3 && viewTab !== 4 ? 'justify-end' : 'justify-end w-full'} gap-2`}>                 
                {states.selectedRows.length > 0 ? (
                    <div className="flex-grow flex items-center px-3 bg-blue-50 rounded">
                      <span className="text-blue-700">
                        {states.selectedRows.length} lớp được chọn
                      </span>
                    </div>
                  ) : null}
                <Button
                  startIcon={isCreateBySemester ? <FacebookCircularProgress size={20} /> : null}
                  sx={{ 
                    minWidth: '120px',
                    textTransform: 'none',
                    fontSize: '14px'
                  }}
                  disabled={isCreateBySemester || !states.selectedSemester || states.loading}
                  onClick={handleClickAdvancedFilter}
                  variant="contained"
                  color="secondary"
                >
                  Tìm kiếm nâng cao
                </Button>

                {isFilterApplied && (
                  <Button
                    sx={{ 
                      minWidth: '120px',
                      textTransform: 'none',
                      fontSize: '14px'
                    }}
                    onClick={resetAdvancedFilter}
                    variant="outlined"
                    color="warning"
                  >
                    🔄 Reset Filter
                  </Button>
                )}

                <Button
                  startIcon={isCreateBySemester ? <FacebookCircularProgress size={20} /> : null}
                  sx={{ 
                    minWidth: '120px',
                    textTransform: 'none',
                    fontSize: '14px'
                  }}
                  disabled={isCreateBySemester || !states.selectedSemester || states.loading}
                  onClick={handleClickSearchRoom}
                  variant="contained"
                  color="secondary"
                >Tìm kiếm phòng</Button>

                <Button
                  startIcon={isCreateBySemester ? <FacebookCircularProgress size={20} /> : null}
                  sx={{ 
                    minWidth: '120px',
                    textTransform: 'none',
                    fontSize: '14px'
                  }}
                  disabled={isCreateBySemester || !states.selectedSemester || states.loading}
                  onClick={handleCreateSegment}
                  variant="contained"
                  color="secondary"
                >
                  {isCreateBySemester ? "Đang tạo..." : "Tạo ca học"}
                </Button>
                <Button
                  startIcon={isDeletingBySemester ? <FacebookCircularProgress size={20} /> : null}
                  sx={{ 
                    minWidth: '120px',
                    textTransform: 'none',
                    fontSize: '14px'
                  }}
                  disabled={isDeletingBySemester || !states.selectedSemester || states.loading}
                  onClick={handleRemoveSegment}
                  variant="contained"
                  color="secondary"
                >
                  {isDeletingBySemester ? "Đang xóa..." : "Xóa ca học"}
                </Button>
                <Button
                    disabled={
                      states.selectedSemester === null ||
                      states.isExportExcelLoading
                    }                    startIcon={
                      states.isExportExcelLoading ? (
                        <FacebookCircularProgress size={20} />
                      ) : null
                    }
                    variant="contained"
                    color="success"
                    onClick={() =>
                      viewTab === 4
                      ? handlers.handleExportTimeTablingWithAllSession(
                          states.selectedSemester?.semester,
                          selectedVersion?.id,
                          selectedVersion?.numberSlotsPerSession || 6 
                        )
                      : handlers.handleExportTimeTabling(
                          states.selectedSemester?.semester,
                          selectedVersion?.id,
                          selectedVersion?.numberSlotsPerSession || 6 
                        )
                    }
                    sx={{
                      minWidth: "100px",
                      height: "40px",
                      padding: "8px 16px",
                      fontWeight: 500,
                      textTransform: "none",
                      boxShadow: 1,
                    }}
                  >
                    Xuất File Excel
                  </Button>
                  
                  {viewTab !== 3 && viewTab !== 4 && (
                    <>
                      <Button
                        disabled={
                          states.selectedRows.length === 0 || states.isResetLoading
                        }
                        startIcon={
                          states.isResetLoading ? (
                            <FacebookCircularProgress size={20} />
                          ) : null
                        }
                        variant="contained"
                        color="error"
                        onClick={() => setOpenResetConfirm(true)}
                        sx={{
                          minWidth: "150px",
                          height: "40px",
                          padding: "8px 16px",
                          fontWeight: 500,
                          textTransform: "none",
                          boxShadow: 1,
                        }}
                      >
                        Xóa lịch học TKB
                      </Button>
                      <Button
                        disabled={
                          states.selectedRows.length === 0 || states.isAutoSaveLoading
                        }
                        startIcon={
                          states.isAutoSaveLoading ? (
                            <FacebookCircularProgress size={20} />
                          ) : null
                        }
                        variant="contained"
                        color="primary"
                        onClick={() => setters.setOpenSelectedDialog(true)}
                        sx={{
                          minWidth: "200px",
                          height: "40px",
                          padding: "8px 16px",
                          fontWeight: 500,
                          textTransform: "none",
                          boxShadow: 1,
                        }}
                      >
                        Xếp lịch lớp đã chọn
                      </Button>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </Paper>

      <div className="flex-1 min-h-0">
        {states.loading ? (
          <div className="h-full flex items-center justify-center">
            <FacebookCircularProgress />
          </div>
        ) : viewTab === 2 ? (
          <div className="flex-grow overflow-y-hidden">
            <RoomOccupationScreen
              selectedSemester={states.selectedSemester}
              setSelectedSemester={setters.setSelectedSemester}
              selectedVersion={selectedVersion}
              numberSlotsPerSession={selectedVersion?.numberSlotsPerSession ?? 6}
            />
          </div>
        ) :
         viewTab === 3 ? (
          <div className="flex flex-row gap-4 w-full overflow-y-hidden h-[600px] rounded-[8px]">
              <ConsolidatedTimeTable
                selectedSemester={states.selectedSemester}
                classes={displayClasses}
                selectedGroup={states.selectedGroup}
                onSaveSuccess={handlers.handleRefreshClasses}
                loading={states.loading || isSchedulingInProgress}
                onRowCountChange={handleSetConsolidatedCount}
                selectedVersion={selectedVersion}
              />
          </div>
        ) : 
        viewTab === 4 ? (
          <div className="flex flex-row gap-4 w-full overflow-y-hidden h-[600px] rounded-[8px]">
              <SessionTimeTable
                selectedSemester={states.selectedSemester}
                classes={displayClasses}
                selectedGroup={states.selectedGroup}
                onSaveSuccess={handlers.handleRefreshClasses}
                loading={states.loading || isSchedulingInProgress}
                onRowCountChange={handleSetSessionViewCount}
                selectedVersion={selectedVersion}
              />
          </div>
        ) : (
          <div className="flex flex-row gap-4 w-full overflow-y-hidden h-[600px] rounded-[8px]">
            {viewTab === 0 && (
              <TimeTable
                selectedSemester={states.selectedSemester}
                classes={displayClasses}
                selectedGroup={states.selectedGroup}
                onSaveSuccess={handlers.handleRefreshClasses}
                loading={states.loading || isSchedulingInProgress}
                selectedRows={states.selectedRows}
                onSelectedRowsChange={setters.setSelectedRows}
                selectedVersion={selectedVersion}
                numberSlotsToDisplay={numberSlotsToDisplay}
              />
            )}
            {viewTab === 1 && (
              <TimeTable
                selectedSemester={states.selectedSemester}
                classes={unscheduledClasses}
                selectedGroup={states.selectedGroup}
                onSaveSuccess={handlers.handleRefreshClasses}
                loading={states.loading || isSchedulingInProgress}
                selectedRows={states.selectedRows}
                onSelectedRowsChange={setters.setSelectedRows}
                selectedVersion={selectedVersion}
                numberSlotsToDisplay={numberSlotsToDisplay}
              />
            )}
          </div>
        )}
      </div>

      <div>
        <AutoScheduleDialog
          title={"Tự động xếp lịch các lớp đã chọn"}
          open={states.isOpenSelectedDialog}
          closeDialog={() => setters.setOpenSelectedDialog(false)}
          timeLimit={states.selectedTimeLimit}
          setTimeLimit={setters.setSelectedTimeLimit}
          submit={handleAutoScheduleSelectedWithClear}
          selectedAlgorithm={states.selectedAlgorithm}
          maxDaySchedule={states.maxDaySchedule}
        />
      </div>

      <Dialog
        open={openResetConfirm}
        onClose={() => setOpenResetConfirm(false)}
      >
        <DialogTitle>Xác nhận xóa lịch học</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Bạn có chắc chắn muốn xóa {states.selectedRows.length} lịch học đã
            chọn không?
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ padding: "16px", gap: "8px" }}>
          <Button
            onClick={() => setOpenResetConfirm(false)}
            variant="outlined"
            sx={{ minWidth: "80px", padding: "6px 16px" }}
          >
            Hủy
          </Button>
          <Button
            onClick={handleConfirmReset}
            color="error"
            variant="contained"
            autoFocus
            sx={{ minWidth: "80px", padding: "6px 16px" }}
          >
            Xóa
          </Button>
        </DialogActions>
      </Dialog>        
      <Dialog
        open={openAdvancedFilter}
        onClose={() => setOpenAdvancedFilter(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle sx={{ 
          borderBottom: '1px solid #e0e0e0',
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          fontWeight: 600,
          pb: 2
        }}>
          🔍 Bộ lọc nâng cao
        </DialogTitle>
        
        <DialogContent sx={{ p: 3 }}>
          <Box sx={{ mb: 2 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: '#1976d2' }}>
              📊 Lọc theo số lượng sinh viên
            </Typography>
            
            <Paper variant="outlined" sx={{ p: 2, mb: 3, backgroundColor: '#f8f9fa' }}>
              <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
                <TextField
                  label="Số lượng tối thiểu"
                  value={filterMinQty}
                  onChange={handleChangeFilterMinQty}
                  variant="outlined"
                  type="number"
                  size="small"
                  sx={{ 
                    minWidth: 200,
                    '& .MuiOutlinedInput-root': {
                      height: '44px'
                    }
                  }}
                  InputProps={{
                    inputProps: { min: 0 },
                    startAdornment: <span style={{ marginRight: '8px', color: '#666' }}>≥</span>
                  }}
                />
                <TextField
                  label="Số lượng tối đa"
                  value={filterMaxQty}
                  onChange={handleChangeFilterMaxQty}
                  variant="outlined"
                  type="number"
                  size="small"
                  sx={{ 
                    minWidth: 200,
                    '& .MuiOutlinedInput-root': {
                      height: '44px'
                    }
                  }}
                  InputProps={{
                    inputProps: { min: 0 },
                    startAdornment: <span style={{ marginRight: '8px', color: '#666' }}>≤</span>
                  }}
                />
              </Box>
              
              {(filterMinQty > 0 || filterMaxQty > 0) && (
                <Box sx={{ mt: 1, p: 1, backgroundColor: '#e3f2fd', borderRadius: 1 }}>
                  <Typography variant="body2" sx={{ fontSize: '0.8rem', color: '#1976d2' }}>
                    💡 {filterMinQty > 0 && filterMaxQty > 0 
                      ? `Hiển thị lớp có từ ${filterMinQty} đến ${filterMaxQty} sinh viên`
                      : filterMinQty > 0 
                        ? `Hiển thị lớp có ít nhất ${filterMinQty} sinh viên`
                        : `Hiển thị lớp có tối đa ${filterMaxQty} sinh viên`
                    }
                  </Typography>
                </Box>
              )}
            </Paper>
          </Box>

          <Box sx={{ mb: 2 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: '#1976d2' }}>
              📚 Lọc theo mã và loại lớp
            </Typography>
            
            <Paper variant="outlined" sx={{ p: 2, backgroundColor: '#f8f9fa' }}>
              <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
                <TextField
                  label="Mã khóa học (Course Codes)"
                  value={filterCourseCodes}
                  onChange={handleChangeCourseCodes}
                  variant="outlined"
                  size="small"
                  placeholder="VD: IT3080, IT4409"
                  sx={{ 
                    flex: 1,
                    minWidth: 250,
                    '& .MuiOutlinedInput-root': {
                      height: '44px'
                    }
                  }}
                  helperText="Nhập nhiều mã cách nhau bằng dấu ;"
                />
                <TextField
                  label="Loại lớp (Class Types)"
                  value={filterClassTypes}
                  onChange={handleChangeClassTypes}
                  variant="outlined"
                  size="small"
                  placeholder="VD: LT, BT, TH"
                  sx={{ 
                    flex: 1,
                    minWidth: 250,
                    '& .MuiOutlinedInput-root': {
                      height: '44px'
                    }
                  }}
                  helperText="Nhập nhiều loại cách nhau bằng dấu ;"
                />
              </Box>
              
              {(filterCourseCodes || filterClassTypes) && (
                <Box sx={{ mt: 1, p: 1, backgroundColor: '#e8f5e8', borderRadius: 1 }}>
                  <Typography variant="body2" sx={{ fontSize: '0.8rem', color: '#2e7d32' }}>
                    ✅ Bộ lọc đang áp dụng: 
                    {filterCourseCodes && ` Mã khóa học: "${filterCourseCodes}"`}
                    {filterCourseCodes && filterClassTypes && ' • '}
                    {filterClassTypes && ` Loại lớp: "${filterClassTypes}"`}
                  </Typography>
                </Box>
              )}
            </Paper>
          </Box>
        </DialogContent>
        
        <DialogActions sx={{ 
          padding: "16px", 
          gap: "8px", 
          borderTop: '1px solid #e0e0e0',
          backgroundColor: '#fafafa'
        }}>
          <Button
            onClick={() => setOpenAdvancedFilter(false)}
            variant="outlined"
            sx={{ 
              minWidth: "100px", 
              padding: "8px 16px",
              textTransform: 'none'
            }}
          >
            Hủy bỏ
          </Button>
          <Button
            onClick={performAdvancedFilter}
            color="primary"
            variant="contained"
            autoFocus
            sx={{ 
              minWidth: "120px", 
              padding: "8px 16px",
              textTransform: 'none'
            }}
          >
            🔍 Áp dụng bộ lọc
          </Button>
        </DialogActions>
      </Dialog>
      
      <Dialog
        open={openSearchRoom}
        onClose={() => setOpenSearchRoom(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle sx={{ 
          borderBottom: '1px solid #e0e0e0',
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          fontWeight: 600
        }}>
          🔍 Tìm kiếm phòng
        </DialogTitle>         
        
         <DialogContent sx={{ p: 3 }}>
          <Box sx={{ my: 1, display: 'flex', gap: 3, alignItems: 'stretch' }}>            
            
            <TextField
              label="Số lượng tối đa sinh viên"
              value={searchRoomCapacity}
              onChange={handleChangeSearchRoomCapacity}
              variant="outlined"
              type="number"
              size="small"
              sx={{ 
                minWidth: 200,
                '& .MuiOutlinedInput-root': {
                  height: '44px'
                }
              }}
              InputProps={{
                inputProps: { min: 1 }
              }}
            />
              {timeSlotList.length > 0 && (                
                <Paper variant="outlined" sx={{ 
                  px: 2,
                  py: 1, 
                  backgroundColor: '#f8f9fa', 
                  flex: 1, 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: 1,
                  minHeight: '40px'
                }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flex: 1 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 500, fontSize: '0.9rem', whiteSpace: 'nowrap' }}>
                    📋 Danh sách ca học đã thêm:
                  </Typography>
                  <code style={{ 
                    backgroundColor: '#e3f2fd', 
                    padding: '4px 12px', 
                    borderRadius: 6, 
                    fontSize: '0.8rem',
                    fontFamily: 'monospace',
                    border: '1px solid #ccc'
                  }}>
                    {generateTimeSlotString()}
                  </code>
                </Box>
                
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={handleRemoveLastTimeSlot}
                    color="warning"
                    sx={{ textTransform: 'none', fontSize: '0.75rem', py: 0.5, px: 1 }}
                  >
                    ↶ Xóa ca cuối cùng
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={handleClearAllTimeSlots}
                    color="error"
                    sx={{ textTransform: 'none', fontSize: '0.75rem', py: 0.5, px: 1 }}
                  >
                    🗑️ Xóa tất cả
                  </Button>
                </Box>
              </Paper>
            )}
          </Box>

          <Box sx={{ mb: 1 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
              🕐 Nhập thông tin ca học
            </Typography>
            
            <Paper variant="outlined" sx={{ p: 1, px: 2, mb: 1 }}>
              <Typography variant="subtitle2" sx={{ mb: 1, color: '#666' }}>
                Ca học {timeSlotList.length + 1}:
              </Typography>                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 1, alignItems: 'center', justifyContent: 'space-between' }}>
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>                  {timeSlotList.length > 0 && (
                    <FormControl size="small" sx={{ 
                      minWidth: 100,
                      '& .MuiOutlinedInput-root': {
                        height: '40px'
                      }
                    }}>
                      <InputLabel>Kết hợp</InputLabel>
                      <Select
                        value={currentTimeSlot.logic}
                        onChange={(e) => handleCurrentTimeSlotChange('logic', e.target.value)}
                        label="Kết hợp"
                        sx={{
                          backgroundColor: currentTimeSlot.logic === 'AND' ? '#e8f5e8' : '#fff3e0',
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: currentTimeSlot.logic === 'AND' ? '#4caf50' : '#ff9800'
                          }
                        }}
                      >
                        <MenuItem value="OR">HOẶC</MenuItem>
                        <MenuItem value="AND">VÀ</MenuItem>
                      </Select>
                    </FormControl>
                  )}

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Buổi</InputLabel>
                    <Select
                      value={currentTimeSlot.session}
                      onChange={(e) => handleCurrentTimeSlotChange('session', e.target.value)}
                      label="Buổi"
                    >
                      <MenuItem value="S">Sáng</MenuItem>
                      <MenuItem value="C">Chiều</MenuItem>
                    </Select>
                  </FormControl>

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Thứ</InputLabel>
                    <Select
                      value={currentTimeSlot.day}
                      onChange={(e) => handleCurrentTimeSlotChange('day', e.target.value)}
                      label="Thứ"
                    >
                      {[2, 3, 4, 5, 6, 7, 8].map(day => (
                        <MenuItem key={day} value={day.toString()}>
                          {getDayName(day)}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Tiết BĐ</InputLabel>
                    <Select
                      value={currentTimeSlot.startPeriod}
                      onChange={(e) => handleCurrentTimeSlotChange('startPeriod', e.target.value)}
                      label="Tiết BĐ"
                    >
                      {Array.from({ length: selectedVersion?.numberSlotsPerSession || 6 }, (_, i) => i + 1).map(period => (
                        <MenuItem key={period} value={period.toString()}>
                          {period}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Số tiết</InputLabel>
                    <Select
                      value={currentTimeSlot.duration}
                      onChange={(e) => handleCurrentTimeSlotChange('duration', e.target.value)}
                      label="Số tiết"
                    >
                      {Array.from({ length: selectedVersion?.numberSlotsPerSession || 6 }, (_, i) => i + 1).map(duration => (
                        <MenuItem key={duration} value={duration.toString()}>
                          {duration}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Box>

                <Button
                  variant="contained"
                  onClick={handleAddToTimeSlotList}
                  disabled={!currentTimeSlot.session || !currentTimeSlot.day || !currentTimeSlot.startPeriod || !currentTimeSlot.duration || !!validateCurrentTimeSlot()}
                  size="small"
                  sx={{ 
                    textTransform: 'none',
                    backgroundColor: '#1976d2',
                    minWidth: 180,
                    height: 40,
                    '&:hover': {
                      backgroundColor: '#1565c0'
                    }
                  }}
                >
                  ➕ Thêm vào chuỗi tìm kiếm
                </Button>
              </Box>

              {/* Validation Error */}
              {currentTimeSlot.session && currentTimeSlot.day && currentTimeSlot.startPeriod && currentTimeSlot.duration && validateCurrentTimeSlot() && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="error" sx={{ fontSize: '0.75rem' }}>
                    ❌ {validateCurrentTimeSlot()}
                  </Typography>
                </Box>
              )}</Paper>          </Box>

          {/* Room Name Filter */}
          {searchRoomData.length > 0 && (
            <Box sx={{ mb: 2 }}>
              <TextField
                label="Lọc theo tên phòng"
                value={roomNameFilter}
                onChange={handleRoomNameFilterChange}
                variant="outlined"
                size="small"
                placeholder="VD: TC-101, D3-301..."
                sx={{ 
                  minWidth: 300,
                  '& .MuiOutlinedInput-root': {
                    height: '40px'
                  }
                }}
                helperText={`Hiển thị ${filteredRoomData.length}/${searchRoomData.length} phòng`}
              />
            </Box>
          )}

          {/* Results Table */}
          <Box sx={{ 
            maxHeight: '400px', 
            overflow: 'auto',
            border: '1px solid #e0e0e0',
            borderRadius: 1,
            '& .MuiTableContainer-root': {
              maxHeight: 'none'
            },
            '& .MuiTable-root thead th': {
              position: 'sticky',
              top: 0,
              backgroundColor: '#f5f5f5',
              zIndex: 10,
              borderBottom: '2px solid #e0e0e0'
            }
          }}>            <StandardTable
              columns={searchRoomColumns}
              data={filteredRoomData}
              hideCommandBar
              options={{
                selection: false,
                search: true,
                paging: false,
                toolbar: false
              }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ padding: "16px", gap: "8px", borderTop: '1px solid #e0e0e0' }}>
          <Button
            onClick={() => setOpenSearchRoom(false)}
            variant="outlined"
            sx={{ minWidth: "80px", padding: "6px 16px" }}
          >
            Hủy
          </Button>
          <Button
            onClick={performSearchRoom}
            color="primary"
            variant="contained"
            autoFocus
            disabled={timeSlotList.length === 0}
            sx={{ minWidth: "120px", padding: "6px 16px" }}
          >
            🔍 Tìm phòng
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default GeneralScheduleScreen;
