import { useGeneralSchedule } from "services/useGeneralScheduleData";
import AutoScheduleDialog from "./components/AutoScheduleDialog";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import GeneralGroupAutoComplete from "../common-components/GeneralGroupAutoComplete";
import { Button, Tabs, Tab, Chip, Divider, Paper } from "@mui/material";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import TimeTable from "./components/TimeTable";
import RoomOccupationScreen from "../room-occupation/RoomOccupationScreen";
import { useState, useMemo } from "react";
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import { FormControl, InputLabel, Select, MenuItem, Box } from "@mui/material";
import { FilterList, Assessment, Clear } from "@mui/icons-material";

const GeneralScheduleScreen = () => {
  const { states, setters, handlers } = useGeneralSchedule();
  const [viewTab, setViewTab] = useState(0);
  const [openResetConfirm, setOpenResetConfirm] = useState(false);
  const [isMaxDayHovered, setIsMaxDayHovered] = useState(false);

  const days = [2, 3, 4, 5, 6, 7, 8];

  // Function to map day numbers to Vietnamese day names
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

  const handleConfirmReset = () => {
    handlers.handleResetTimeTabling();
    setOpenResetConfirm(false);
  };

  const isSchedulingInProgress =
    states.isAutoSaveLoading || states.isTimeScheduleLoading || states.loading;

  // Filter unscheduled classes (where room is null)
  const unscheduledClasses = useMemo(() => {
    if (!states.classes || states.classes.length === 0) return [];
    return states.classes.filter((cls) => cls.room === null);
  }, [states.classes]);

  return (
    <div className="flex flex-col gap-3">
      <Paper elevation={1} className="p-3">
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
                  label={states.classes?.length || 0}
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
        </Tabs>

        {(viewTab === 0 || viewTab === 1) && (
          <div className="mt-3">
            <Paper variant="outlined" className="p-3">
              <div className="flex gap-3 items-center flex-wrap">
                <GeneralSemesterAutoComplete
                  selectedSemester={states.selectedSemester}
                  setSelectedSemester={setters.setSelectedSemester}
                  sx={{
                    minWidth: 200,
                    "& .MuiInputBase-root": { height: "40px" },
                  }}
                />
                <GeneralGroupAutoComplete
                  selectedGroup={states.selectedGroup}
                  setSelectedGroup={setters.setSelectedGroup}
                  sx={{
                    minWidth: 200,
                    "& .MuiInputBase-root": { height: "40px" },
                  }}
                />
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
              </div>
            </Paper>

            <div className="sticky top-0 z-10 bg-white py-3 mt-3 border-b">
              <div className="flex justify-end gap-2">
                {states.selectedRows.length > 0 ? (
                  <div className="flex-grow flex items-center px-3 bg-blue-50 rounded">
                    <span className="text-blue-700">
                      {states.selectedRows.length} lớp được chọn
                    </span>
                  </div>
                ) : null}

                <Button
                  disabled={
                    states.selectedSemester === null ||
                    states.isExportExcelLoading
                  }
                  startIcon={
                    states.isExportExcelLoading ? (
                      <FacebookCircularProgress size={20} />
                    ) : null
                  }
                  variant="contained"
                  color="success"
                  onClick={() =>
                    handlers.handleExportTimeTabling(
                      states.selectedSemester?.semester,
                      true // includeBorders parameter
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
                    !states.selectedSemester || states.isAutoSaveLoading
                  }
                  startIcon={
                    states.isAutoSaveLoading ? (
                      <FacebookCircularProgress size={20} />
                    ) : null
                  }
                  variant="contained"
                  color="primary"
                  onClick={() => setters.setOpenTimeslotDialog(true)}
                  sx={{
                    minWidth: "160px",
                    height: "40px",
                    padding: "8px 16px",
                    fontWeight: 500,
                    textTransform: "none",
                    boxShadow: 1,
                  }}
                >
                  Tự động xếp TKB
                </Button>
                <Button
                  disabled={
                    !states.selectedSemester || states.isAutoSaveLoading
                  }
                  startIcon={
                    states.isTimeScheduleLoading ? (
                      <FacebookCircularProgress size={20} />
                    ) : null
                  }
                  variant="contained"
                  color="primary"
                  onClick={() => setters.setOpenClassroomDialog(true)}
                  sx={{
                    minWidth: "160px",
                    height: "40px",
                    padding: "8px 16px",
                    fontWeight: 500,
                    textTransform: "none",
                    boxShadow: 1,
                  }}
                >
                  Tự động xếp phòng
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
            />
          </div>
        ) : (
          <div className="flex flex-row gap-4 w-full overflow-y-hidden h-[600px] rounded-[8px]">
            {viewTab === 0 && (
              <TimeTable
                selectedSemester={states.selectedSemester}
                classes={states.classes}
                selectedGroup={states.selectedGroup}
                onSaveSuccess={handlers.handleRefreshClasses}
                loading={states.loading || isSchedulingInProgress}
                selectedRows={states.selectedRows}
                onSelectedRowsChange={setters.setSelectedRows}
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
              />
            )}
          </div>
        )}
      </div>

      <div>
        <AutoScheduleDialog
          title={"Tự động xếp lịch học của kì học"}
          open={states.isOpenTimeslotDialog}
          closeDialog={() => setters.setOpenTimeslotDialog(false)}
          timeLimit={states.timeSlotTimeLimit}
          setTimeLimit={setters.setTimeSlotTimeLimit}
          submit={handlers.handleAutoScheduleTimeSlotTimeTabling}
          selectedAlgorithm={states.selectedAlgorithm}
        />
        <AutoScheduleDialog
          title={"Tự động xếp phòng học"}
          open={states.isOpenClassroomDialog}
          closeDialog={() => setters.setOpenClassroomDialog(false)}
          setTimeLimit={setters.setClassroomTimeLimit}
          timeLimit={states.classroomTimeLimit}
          submit={handlers.handleAutoScheduleClassroomTimeTabling}
          selectedAlgorithm={states.selectedAlgorithm}
        />
        <AutoScheduleDialog
          title={"Tự động xếp lịch các lớp đã chọn"}
          open={states.isOpenSelectedDialog}
          closeDialog={() => setters.setOpenSelectedDialog(false)}
          timeLimit={states.selectedTimeLimit}
          setTimeLimit={setters.setSelectedTimeLimit}
          submit={handlers.handleAutoScheduleSelected} // Already includes maxDaySchedule
          selectedAlgorithm={states.selectedAlgorithm}
          maxDaySchedule={states.maxDaySchedule} // Pass maxDaySchedule to the dialog
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
