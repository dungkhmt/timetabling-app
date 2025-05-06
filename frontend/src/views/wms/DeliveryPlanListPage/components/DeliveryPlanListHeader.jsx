import React from "react";
import { useHistory } from "react-router-dom";
import { Box, Button, Typography, useTheme, useMediaQuery } from "@mui/material";
import { Refresh, Add } from "@mui/icons-material";

const DeliveryPlanListHeader = ({ onResetFilters }) => {
  const history = useHistory();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  const handleCreateDeliveryPlan = () => {
    history.push("/wms/logistics/delivery/create");
  };

  return (
    <Box mb={3} display="flex" justifyContent="space-between" alignItems="center" flexWrap={isMobile ? "wrap" : "nowrap"}>
      <Typography variant="h5" fontWeight="bold" gutterBottom={isMobile}>
        Danh sách kế hoạch giao hàng
      </Typography>
      
      <Box display="flex" gap={1} mt={isMobile ? 2 : 0} width={isMobile ? "100%" : "auto"}>
        <Button
          variant="outlined"
          color="primary"
          startIcon={<Refresh />}
          onClick={onResetFilters}
        >
          Làm mới
        </Button>
        
        <Button
          variant="contained"
          color="primary"
          startIcon={<Add />}
          onClick={handleCreateDeliveryPlan}
        >
          Tạo kế hoạch giao
        </Button>
      </Box>
    </Box>
  );
};

export default DeliveryPlanListHeader;