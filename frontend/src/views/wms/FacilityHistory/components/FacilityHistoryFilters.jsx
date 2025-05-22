import React from "react";
import {
  Box,
  Button,
  Grid,
  Paper,
  TextField
} from "@mui/material";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import SearchIcon from "@mui/icons-material/Search";
import RestartAltIcon from '@mui/icons-material/RestartAlt';

const FacilityHistoryFilters = ({ 
  filters, 
  onFilterChange, 
  onApplyFilters, 
  onResetFilters
}) => {
  // Handle text field changes
  const handleKeywordChange = (e) => {
    onFilterChange("keyword", e.target.value);
  };

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={6} md={5}>
          <TextField
            fullWidth
            label="Tìm theo mã, tên sản phẩm, tên kho"
            name="keyword"
            value={filters.keyword || ""}
            onChange={handleKeywordChange}
            size="small"
            placeholder="Nhập từ khóa tìm kiếm"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={2}>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <DateTimePicker
              label="Từ ngày"
              value={filters.startCreatedAt}
              onChange={(date) => onFilterChange("startCreatedAt", date)}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </LocalizationProvider>
        </Grid>

        <Grid item xs={12} sm={6} md={2}>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <DateTimePicker
              label="Đến ngày"
              value={filters.endCreatedAt}
              onChange={(date) => onFilterChange("endCreatedAt", date)}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </LocalizationProvider>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Box display="flex" flexDirection="row" gap={1} height="100%">
            <Button
              variant="contained"
              color="primary"
              onClick={onApplyFilters}
              startIcon={<SearchIcon />}
              fullWidth
            >
              Tìm kiếm
            </Button>
            <Button
              variant="outlined"
              onClick={onResetFilters}
              startIcon={<RestartAltIcon />}
              fullWidth
            >
              Đặt lại
            </Button>
          </Box>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default FacilityHistoryFilters;