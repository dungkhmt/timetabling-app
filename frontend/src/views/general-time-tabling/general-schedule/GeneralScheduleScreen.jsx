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
        
        const clusterClasses = await handlers.getClassesByCluster(selectedCluster.id, selectedVersion?.id);
        setFilteredClasses(clusterClasses);
      } else {
        setFilteredClasses(states.classes);
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
    states.isAutoSaveLoading || states.isTimeScheduleLoading || states.loading;

  const unscheduledClasses = useMemo(() => {
    if (selectedCluster) {
      return filteredClasses.filter((cls) => cls.room === null);
    }
    if (!states.classes || states.classes.length === 0) return [];
    return states.classes.filter((cls) => cls.room === null);
  }, [states.classes, filteredClasses, selectedCluster]);

  const displayClasses = selectedCluster ? filteredClasses : states.classes;
  
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

  // Version selection UI
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
        createVersion={createVersion} // Truyền createVersion từ useTimeTablingVersionData
        onCreateSuccess={(createdVersion) => {
          // Khi tạo version thành công, refresh danh sách
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
    </div>
  );
};

export default GeneralScheduleScreen;
