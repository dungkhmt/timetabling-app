import React from "react";
import {
  Box,
  Button,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import RestartAltIcon from '@mui/icons-material/RestartAlt';

// Constants for vehicle status - same as in the parent
const VEHICLE_STATUSES = [
  { id: "", name: "Tất cả" },
  { id: "AVAILABLE", name: "Có sẵn" },
  { id: "ASSIGNED", name: "Đã phân công" },
  { id: "IN_USE", name: "Đang sử dụng" },
  { id: "UNDER_MAINTENANCE", name: "Đang bảo trì" }
];

const VehicleFilters = ({ 
  filters, 
  onFilterChange, 
  onApplyFilters, 
  onResetFilters,
  hideStatusFilter = false
}) => {
  // Handle text field changes
  const handleKeywordChange = (e) => {
    onFilterChange("keyword", e.target.value);
  };

  // Handle status selection change
  const handleStatusChange = (event) => {
    onFilterChange("statusId", event.target.value);
  };

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={hideStatusFilter ? 8 : 6} md={hideStatusFilter ? 8 : 6}>
          <TextField
            fullWidth
            label="Tìm theo mã, tên phương tiện"
            name="keyword"
            value={filters.keyword || ""}
            onChange={handleKeywordChange}
            size="small"
            placeholder="Nhập từ khóa tìm kiếm"
          />
        </Grid>

        {!hideStatusFilter && (
          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth size="small">
              <InputLabel id="status-select-label">Trạng thái</InputLabel>
              <Select
                labelId="status-select-label"
                id="status-select"
                value={filters.statusId || ""}
                onChange={handleStatusChange}
                label="Trạng thái"
              >
                {VEHICLE_STATUSES.map((status) => (
                  <MenuItem key={status.id} value={status.id}>
                    {status.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        )}

        <Grid item xs={12} sm={6} md={hideStatusFilter ? 4 : 2}>
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

export default VehicleFilters;