import React from "react";
import {
  Box,
  Button,
  Checkbox,
  Chip,
  FormControl,
  Grid,
  InputLabel,
  ListItemText,
  MenuItem,
  OutlinedInput,
  Paper,
  Select,
  TextField
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import RestartAltIcon from '@mui/icons-material/RestartAlt';

// Constants for shipper status
const SHIPPER_STATUSES = [
  { id: "DRIVING", name: "Đang lái xe" },
  { id: "ASSIGNED", name: "Đã phân công" },
  { id: "IN_TRIP", name: "Đang trong chuyến" },
  { id: "ACTIVE", name: "Hoạt động" },
  { id: "INACTIVE", name: "Không hoạt động" }
];

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
      width: 250,
    },
  },
};

const ShipperFilters = ({ 
  filters, 
  onFilterChange, 
  onApplyFilters, 
  onResetFilters
}) => {
  // Handle text field changes
  const handleKeywordChange = (e) => {
    onFilterChange("keyword", e.target.value);
  };

  // Handle status selection change
  const handleStatusChange = (event) => {
    const {
      target: { value },
    } = event;
    // Ensure it's always an array, even when empty
    const statusArray = typeof value === 'string' ? value.split(',') : value;
    onFilterChange("statusId", statusArray);
  };

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={6} md={6}>
          <TextField
            fullWidth
            label="Tìm theo tên, email, số điện thoại"
            name="keyword"
            value={filters.keyword || ""}
            onChange={handleKeywordChange}
            size="small"
            placeholder="Nhập từ khóa tìm kiếm"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <FormControl fullWidth size="small">
            <InputLabel id="status-select-label">Trạng thái</InputLabel>
            <Select
              labelId="status-select-label"
              id="status-select"
              multiple
              value={filters.statusId || []}
              onChange={handleStatusChange}
              input={<OutlinedInput label="Trạng thái" />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => {
                    const status = SHIPPER_STATUSES.find(s => s.id === value);
                    return (
                      <Chip 
                        key={value} 
                        label={status ? status.name : value} 
                        size="small" 
                      />
                    );
                  })}
                </Box>
              )}
              MenuProps={MenuProps}
            >
              {SHIPPER_STATUSES.map((status) => (
                <MenuItem key={status.id} value={status.id}>
                  <Checkbox checked={(filters.statusId || []).indexOf(status.id) > -1} />
                  <ListItemText primary={status.name} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        <Grid item xs={12} sm={6} md={2}>
          <Box display="flex" flexDirection="column" gap={1} height="100%">
            <Button
              variant="contained"
              color="primary"
              onClick={onApplyFilters}
              startIcon={<SearchIcon />}
              fullWidth
            >
              Tìm kiếm
            </Button>
            {/*<Button*/}
            {/*  variant="outlined"*/}
            {/*  onClick={onResetFilters}*/}
            {/*  startIcon={<RestartAltIcon />}*/}
            {/*  fullWidth*/}
            {/*>*/}
            {/*  Đặt lại*/}
            {/*</Button>*/}
          </Box>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default ShipperFilters;