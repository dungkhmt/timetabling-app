import React from "react";
import {
  Box,
  Button,
  Typography,
  useMediaQuery,
  useTheme
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import RefreshIcon from "@mui/icons-material/Refresh";
import MapIcon from "@mui/icons-material/Map";

const FacilityListHeader = ({ onCreateFacility, onResetFilters, onViewMap }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  return (
    <Box 
      mb={3} 
      display="flex" 
      justifyContent="space-between" 
      alignItems="center"
      flexDirection={isMobile ? "column" : "row"}
    >
      <Typography variant="h5" fontWeight="bold" mb={isMobile ? 2 : 0}>
        Quản lý cơ sở
      </Typography>
      
      <Box display="flex" gap={1} flexWrap="wrap">
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={onResetFilters}
        >
          Làm mới
        </Button>
        <Button
          variant="outlined"
          startIcon={<MapIcon />}
          onClick={onViewMap}
          color="secondary"
        >
          Xem vị trí các kho
        </Button>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={onCreateFacility}
        >
          Thêm cơ sở
        </Button>
      </Box>
    </Box>
  );
};

export default FacilityListHeader;