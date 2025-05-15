import React from "react";
import {
  Box,
  Button,
  Typography,
  Paper
} from "@mui/material";
import RefreshIcon from "@mui/icons-material/Refresh";
import FilterAltIcon from "@mui/icons-material/FilterAlt";
import { SHIPMENT_TYPE_ID } from "../../common/constants/constants";

const ShipmentListHeader = ({ 
  shipmentTypeId,
  onResetFilters,
  showFilters,
  onToggleFilters
}) => {
  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h5" fontWeight="bold">
          Danh sách {shipmentTypeId === SHIPMENT_TYPE_ID.INBOUND ? "nhập" : "xuất"} hàng
        </Typography>
        
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={onResetFilters}
          >
            Làm mới
          </Button>
          
          <Button
            variant={showFilters ? "contained" : "outlined"}
            color="primary"
            startIcon={<FilterAltIcon />}
            onClick={onToggleFilters}
          >
            Bộ lọc
          </Button>
        </Box>
      </Box>
    </Paper>
  );
};

export default ShipmentListHeader;