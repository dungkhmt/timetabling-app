import React from "react";
import { 
  Paper, 
  Grid, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Button 
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";

const InventoryReportControls = ({
  facilities,
  selectedFacility,
  dateRange,
  onFacilityChange,
  onDateRangeChange,
  onApplyDateRange
}) => {
  return (
    <Paper elevation={1} sx={{ p: 3, mb: 3 }}>
      <Grid container spacing={3} alignItems="center">
        <Grid item xs={12} md={4}>
          <FormControl fullWidth>
            <InputLabel>Chọn kho</InputLabel>
            <Select
              value={selectedFacility}
              label="Chọn kho"
              onChange={onFacilityChange}
            >
              <MenuItem value="">Tất cả các kho</MenuItem>
              {facilities.map(facility => (
                <MenuItem key={facility.id} value={facility.id}>
                  {facility.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
        
        <Grid item xs={12} md={3}>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <DatePicker
              label="Từ ngày"
              value={dateRange.startDate}
              onChange={(date) => onDateRangeChange("startDate", date)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </LocalizationProvider>
        </Grid>
        
        <Grid item xs={12} md={3}>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <DatePicker
              label="Đến ngày"
              value={dateRange.endDate}
              onChange={(date) => onDateRangeChange("endDate", date)}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </LocalizationProvider>
        </Grid>

        <Grid item xs={12} md={2} textAlign="right">
          <Button
              variant="contained"
              color="primary"
              fullWidth
              onClick={onApplyDateRange}
              sx={{
                height: "56px", // Standard MUI input height with label
                // or use padding instead
                // py: 2,
              }}
          >
            Áp dụng
          </Button>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default InventoryReportControls;