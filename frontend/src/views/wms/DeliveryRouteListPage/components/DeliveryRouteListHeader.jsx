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
import {useHandleNavigate} from "../../common/utils/functions";

const DeliveryRouteListHeader = ({ 
  onResetFilters,
  showFilters,
  onToggleFilters
}) => {

  const navigate = useHandleNavigate();

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h5" fontWeight="bold">
          Danh sách chuyến giao hàng
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
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={e => navigate("/wms/logistics/deliveryroute/create")}
          >
            Thêm mới
          </Button>
        </Box>
      </Box>
    </Paper>
  );
};

export default DeliveryRouteListHeader;