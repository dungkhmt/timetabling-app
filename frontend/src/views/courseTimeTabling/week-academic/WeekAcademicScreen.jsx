import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import { useState } from "react";
import { useAcademicWeeks } from "services/useAcademicWeeksData";
import GeneralSemesterAutoComplete from "views/general-time-tabling/common-components/GeneralSemesterAutoComplete";
import AddWeekAcademicSemesterDialog from "./components/AddWeekAcademicSemesterDialog";
import { Box, Button, Typography } from "@mui/material";
import WeekAcademicTable from "./components/WeekAcademicTable";

const WeekAcademicScreen = () => {
  const [selectedSemester, setSelectedSemester] = useState(null);
  const [isOpenWeekDialog, setOpenWeekDialog] = useState(false);
  const { 
    weeks, 
    isLoading, 
    deleteWeeks, 
    isDeleting,
    refetch 
  } = useAcademicWeeks(selectedSemester?.semester);

  const handleDeleteAcademicWeeks = async () => {
    if (!selectedSemester?.semester) return;
    await deleteWeeks(selectedSemester.semester);
  };

  return (
    <div className="flex flex-col justify-end gap-6 items-end">
      <Box
        style={{
          width: "100%",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Typography variant="h5">Danh sách tuần học</Typography>
      </Box>
      <AddWeekAcademicSemesterDialog
        open={isOpenWeekDialog}
        setUpdateSelectedSemester={setSelectedSemester}
        setOpen={setOpenWeekDialog}
        refetch={refetch}
        selectedSemester={selectedSemester}
      />
      <div className="flex gap-2 justify-between w-full">
        {isDeleting && <FacebookCircularProgress />}
        <GeneralSemesterAutoComplete
          selectedSemester={selectedSemester}
          setSelectedSemester={setSelectedSemester}
        />
        <div className="flex gap-2">
          <Button
            variant="contained"
            sx={{
              width: "200px",
              textTransform: "none",
              fontSize: "16px",
            }}
            color="error"
            disabled={isDeleting || selectedSemester === null}
            onClick={handleDeleteAcademicWeeks}
          >
            Xóa tuần học của kì
          </Button>
          <Button
            disabled={isLoading || selectedSemester === null}
            sx={{
              width: "220px",
              textTransform: "none",
              fontSize: "16px",
            }}
            variant="contained"
            onClick={() => {
              setOpenWeekDialog(true);
            }}
          >
            Thêm danh sách tuần học
          </Button>
        </div>
      </div>
      <WeekAcademicTable
        isLoading={isLoading}
        weeks={weeks}
        selectedSemester={selectedSemester}
      />
    </div>
  );
};

export default WeekAcademicScreen;
