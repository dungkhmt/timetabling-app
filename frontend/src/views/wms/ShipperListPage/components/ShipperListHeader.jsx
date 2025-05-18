import React from "react";
import {
  Box,
  Button,
  Typography,
  Paper
} from "@mui/material";
import RefreshIcon from "@mui/icons-material/Refresh";
import FilterAltIcon from "@mui/icons-material/FilterAlt";
import AddIcon from "@mui/icons-material/Add";
import { useHistory } from "react-router-dom";

const ShipperListHeader = ({ 
  onResetFilters,
  showFilters,
  onToggleFilters
}) => {
  const history = useHistory();

  const handleCreateShipper = () => {
    history.push("/wms/logistics/shipper/create");
  };

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h5" fontWeight="bold">
          Danh sách nhân viên giao hàng
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

          <Button
            variant="contained"
            color="success"
            startIcon={<AddIcon />}
            onClick={handleCreateShipper}
          >
            Thêm mới
          </Button>
        </Box>
      </Box>
    </Paper>
  );
};

export default ShipperListHeader;