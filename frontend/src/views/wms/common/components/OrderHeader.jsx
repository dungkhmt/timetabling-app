import React from 'react';
import { Typography, Button, Stack, useMediaQuery, IconButton, Tooltip, Box } from '@mui/material';
import { Add, ImportExport, Refresh, Menu } from '@mui/icons-material';
import { useHistory, useLocation } from 'react-router-dom';
import { ORDER_TYPE_ID } from '../constants/constants';
import { useTheme } from '@mui/material/styles';

const OrderHeader = ({ onRefresh, onExport, type }) => {
  const history = useHistory();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));
  
  const baseUrl = type === ORDER_TYPE_ID.SALES_ORDER ? "/wms/sales" : "/wms/purchase";

  const handleCreateOrder = () => {
    history.push(`${baseUrl}/orders/create`);
  };

  const title = type === "sale" ? "Danh sách đơn hàng bán" : "Danh sách đơn đặt hàng";
  const createButtonText = type === "sale" ? "Tạo đơn hàng" : "Tạo đơn đặt hàng";

  return (
    <Box
      sx={{
        width: '100%',
        padding: theme.spacing(1),
        borderBottom: `1px solid ${theme.palette.divider}`,
        backgroundColor: theme.palette.background.paper,
      }}
    >
      <Stack 
        direction={{ xs: 'column', md: 'row' }} 
        spacing={{ xs: 1, sm: 2 }} 
        alignItems={{ xs: 'stretch', md: 'center' }} 
        justifyContent="space-between"
        sx={{ width: '100%' }}
      >
        <Typography 
          variant={isMobile ? "h6" : "h5"} 
          fontWeight="bold"
          sx={{ 
            flexShrink: 0, 
            whiteSpace: isTablet ? 'normal' : 'nowrap',
            marginBottom: isMobile ? 1 : 0
          }}
        >
          {title}
        </Typography>
        
        {isMobile ? (
          <Stack direction="row" spacing={1} sx={{ width: '100%' }}>
            <Tooltip title="Làm mới">
              <IconButton onClick={onRefresh} size="small" sx={{ border: `1px solid ${theme.palette.divider}` }}>
                <Refresh fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Xuất file">
              <IconButton onClick={onExport} size="small" sx={{ border: `1px solid ${theme.palette.divider}` }}>
                <ImportExport fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button 
              variant="contained" 
              color="primary" 
              startIcon={<Add />}
              onClick={handleCreateOrder}
              fullWidth
              size="small"
            >
              {isTablet ? "Tạo mới" : createButtonText}
            </Button>
          </Stack>
        ) : (
          <Stack 
            direction="row" 
            spacing={1}
            sx={{ 
              flexWrap: isTablet ? 'wrap' : 'nowrap',
              justifyContent: isTablet ? 'flex-end' : 'flex-start'
            }}
          >
            <Button 
              variant="outlined" 
              startIcon={<Refresh />} 
              onClick={onRefresh}
              size={isTablet ? "small" : "medium"}
            >
              {isTablet ? "" : "Làm mới"}
            </Button>
            <Button 
              variant="outlined" 
              startIcon={<ImportExport />} 
              onClick={onExport}
              size={isTablet ? "small" : "medium"}
            >
              {isTablet ? "" : "Xuất file"}
            </Button>
            <Button 
              variant="contained" 
              color="primary" 
              startIcon={<Add />}
              onClick={handleCreateOrder}
              size={isTablet ? "small" : "medium"}
            >
              {isTablet ? "Tạo mới" : createButtonText}
            </Button>
          </Stack>
        )}
      </Stack>
    </Box>
  );
};

export default OrderHeader;