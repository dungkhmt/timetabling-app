import { useState } from "react";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { useAcademicWeeks } from "services/useAcademicWeeksData";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography, Box } from "@mui/material";
import { DatePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { FacebookCircularProgress } from "components/common/progressBar/CustomizedCircularProgress";
import dayjs from "dayjs";

const AddWeekAcademicSemesterDialog = ({
  open,
  setOpen,
  setUpdateSelectedSemester,
  refetch,
  selectedSemester
}) => {
  const [numberOfWeeks, setNumOfWeeks] = useState("");
  const [startDate, setStartDate] = useState(dayjs());
  const [startWeek, setStartWeek] = useState(1);
  const { createWeeks, isCreating } = useAcademicWeeks();

  const handleClose = () => {
    setOpen(false);
  };

  const handleSubmit = async () => {
    if (!selectedSemester?.semester) return;
    
    await createWeeks({
      semester: selectedSemester.semester,
      startDate: formatDateToString(startDate.toDate()),
      startWeek: Number(startWeek),
      numberOfWeeks: Number(numberOfWeeks),
    });
    
    // Chỉ refetch một lần ở đây sau khi tạo thành công
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
          {/* Hiển thị kỳ học đã chọn dưới dạng text box với styling giống dropdown */}
          <Box sx={{ mb: 2 }}>
            <TextField
              fullWidth
              label="Kỳ học"
              value={selectedSemester?.semester || ""}
              disabled
              InputProps={{
                readOnly: true,
              }}
            />
          </Box>
          
          <TextField
            value={startWeek}
            onChange={(e) => {
              setStartWeek(e.target.value);
            }}
            label={"Tuần bắt đầu"}
            type="number"
            inputProps={{ min: 1 }}
          />
          
          <TextField
            value={numberOfWeeks}
            onChange={(e) => {
              setNumOfWeeks(e.target.value);
            }}
            label={"Nhập số tuần học"}
            type="number"
            inputProps={{ min: 1 }}
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
            isNaN(Number(numberOfWeeks)) ||
            !startWeek ||
            isNaN(Number(startWeek))
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
