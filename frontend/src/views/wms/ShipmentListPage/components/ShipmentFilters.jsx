// Note: I'm keeping the existing implementation but adding a Paper wrapper
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
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import SearchIcon from "@mui/icons-material/Search";
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import { SHIPMENT_STATUSES, SHIPMENT_TYPE_ID } from "../../common/constants/constants";

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

const ShipmentFilters = ({ 
  filters, 
  onFilterChange, 
  onApplyFilters, 
  onResetFilters,
  shipmentTypeId 
}) => {
  // Determine which statuses to show based on shipment type
  const filteredStatuses = React.useMemo(() => {
    if (shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND) {
      // For inbound shipments, exclude EXPORTED, SHIPPED, PARTIALLY_DELIVERED, DELIVERED
      return SHIPMENT_STATUSES.filter(status => 
        !['EXPORTED', 'SHIPPED', 'PARTIALLY_DELIVERED', 'DELIVERED'].includes(status.id)
      );
    } else {
      // For outbound shipments, exclude IMPORTED
      return SHIPMENT_STATUSES.filter(status => 
        status.id !== 'IMPORTED'
      );
    }
  }, [shipmentTypeId]);

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

  // Handle expected delivery date changes
  const handleDateChange = (date) => {
    onFilterChange("expectedDeliveryDate", date);
  };

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <TextField
            fullWidth
            label={shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND 
              ? "Tìm theo mã, tên lô hàng hoặc nhà cung cấp" 
              : "Tìm theo mã, tên lô hàng hoặc khách hàng"}
            name="keyword"
            value={filters.keyword || ""}
            onChange={handleKeywordChange}
            size="small"
            placeholder="Nhập từ khóa tìm kiếm"
          />
        </Grid>

        <Grid item xs={12} md={3}>
          <FormControl fullWidth size="small">
            <InputLabel id="status-select-label">Trạng thái lô hàng</InputLabel>
            <Select
              labelId="status-select-label"
              id="status-select"
              multiple
              value={filters.statusId || []}
              onChange={handleStatusChange}
              input={<OutlinedInput label="Trạng thái lô hàng" />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => {
                    const status = SHIPMENT_STATUSES.find(s => s.id === value);
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
              {filteredStatuses.map((status) => (
                <MenuItem key={status.id} value={status.id}>
                  <Checkbox checked={(filters.statusId || []).indexOf(status.id) > -1} />
                  <ListItemText primary={status.name} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <DatePicker
              label="Ngày giao hàng dự kiến"
              value={filters.expectedDeliveryDate}
              onChange={handleDateChange}
              slotProps={{ textField: { size: 'small', fullWidth: true } }}
            />
          </LocalizationProvider>
        </Grid>

        <Grid item xs={12} md={2} display="flex">
          <Button
            variant="contained"
            color="primary"
            onClick={onApplyFilters}
            startIcon={<SearchIcon />}
            fullWidth
          >
            Tìm kiếm
          </Button>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default ShipmentFilters;