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
import {ImportExport} from "@mui/icons-material";

const ProductListHeader = ({ onCreateProduct, onResetFilters }) => {
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
        Quản lý sản phẩm
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
            variant="outlined"
            startIcon={<ImportExport />}
            size="medium"
        >
            Xuất file
        </Button>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={onCreateProduct}
        >
          Thêm sản phẩm
        </Button>
      </Box>
    </Box>
  );
};

export default ProductListHeader;