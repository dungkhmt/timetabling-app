import React from "react";
import {
  TextField,
  InputAdornment,
  Grid,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Box
} from "@mui/material";
import { Search } from "@mui/icons-material";

const DeliveryBillFilters = ({
  filters,
  onFilterChange,
  onApplyFilters,
  statuses,
  priorityLevels
}) => {
  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Grid container spacing={2}>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              name="keyword"
              label="Tìm kiếm"
              value={filters.keyword}
              onChange={onFilterChange}
              placeholder="Tên phiếu giao, mã phiếu, khách hàng..."
              size="small"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Trạng thái</InputLabel>
              <Select
                name="status"
                value={filters.status}
                onChange={onFilterChange}
                label="Trạng thái"
              >
                <MenuItem value="">Tất cả</MenuItem>
                {Object.entries(statuses).map(([value, { label }]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Mức độ ưu tiên</InputLabel>
              <Select
                name="priority"
                value={filters.priority}
                onChange={onFilterChange}
                label="Mức độ ưu tiên"
              >
                <MenuItem value="">Tất cả</MenuItem>
                {Object.entries(priorityLevels).map(([value, { label }]) => (
                  <MenuItem key={value} value={Number(value)}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} md={2}>
            <TextField
              fullWidth
              label="Từ ngày"
              type="date"
              name="startDate"
              value={filters.startDate || ""}
              onChange={onFilterChange}
              size="small"
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          
          <Grid item xs={12} md={2}>
            <Box display="flex" gap={1}>
              <TextField
                fullWidth
                label="Đến ngày"
                type="date"
                name="endDate"
                value={filters.endDate || ""}
                onChange={onFilterChange}
                size="small"
                InputLabelProps={{ shrink: true }}
              />
              
              <Button
                variant="contained"
                color="primary"
                onClick={onApplyFilters}
              >
                Lọc
              </Button>
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default DeliveryBillFilters;