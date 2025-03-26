import { useState } from "react";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { useAcademicWeeks } from "services/useAcademicWeeksData";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from "@mui/material";
import GeneralSemesterAutoComplete from "views/general-time-tabling/common-components/GeneralSemesterAutoComplete";
import { DatePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import dayjs from "dayjs";

const AddWeekAcademicSemesterDialog = ({
  open,
  setOpen,
  setUpdateSelectedSemester,
  refetch
}) => {
  const [numberOfWeeks, setNumOfWeeks] = useState("");
  const [startDate, setStartDate] = useState(dayjs());
  const [selectedSemester, setSelectedSemester] = useState(null);
  const { createWeeks, isCreating } = useAcademicWeeks();

  const handleClose = () => {
    setOpen(false);
  };

  const handleSubmit = async () => {
    await createWeeks({
      semester: selectedSemester.semester,
      startDate: formatDateToString(startDate.toDate()),
      numberOfWeeks: Number(numberOfWeeks),
    });
    setUpdateSelectedSemester(selectedSemester);
    await refetch();
    handleClose();
  };

  const formatDateToString = (date) => {
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    const formattedDay = String(day).padStart(2, "0");
    const formattedMonth = String(month).padStart(2, "0");

    return `${formattedDay}/${formattedMonth}/${year}`;
  };

  return (
    <Dialog open={open} onClose={handleClose}>
      <DialogTitle>Tạo tuần học</DialogTitle>
      <DialogContent dividers={true}>
        <div className="flex gap-2 flex-col py-2">
          <GeneralSemesterAutoComplete
            selectedSemester={selectedSemester}
            setSelectedSemester={setSelectedSemester}
          />
          <TextField
            value={numberOfWeeks}
            onChange={(e) => {
              setNumOfWeeks(e.target.value);
            }}
            label={"Nhập số tuần"}
            type="number"
          />
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DatePicker
              label="Chọn ngày bắt đầu kỳ học"
              value={startDate}
              onChange={(newValue) => {
                setStartDate(newValue);
              }}
            />
          </LocalizationProvider>
        </div>
      </DialogContent>
      <DialogActions>
        {isCreating ? <FacebookCircularProgress /> : null}
        <Button
          variant="outlined"
          disabled={
            isCreating ||
            selectedSemester === null ||
            !numberOfWeeks ||
            isNaN(Number(numberOfWeeks))
          }
          onClick={handleSubmit}
          type="submit"
        >
          Xác nhận
        </Button>
        <Button variant="outlined" onClick={handleClose}>
          Hủy
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddWeekAcademicSemesterDialog;
