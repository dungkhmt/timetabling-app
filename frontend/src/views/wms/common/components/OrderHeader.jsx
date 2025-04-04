import React from 'react';
import { Typography, Button, Stack } from '@mui/material';
import { Add, ImportExport, Refresh } from '@mui/icons-material';
import { useHistory, useLocation } from 'react-router-dom';

const OrderHeader = ({ onRefresh, onExport, type }) => {
  const history = useHistory();
  const location = useLocation();
  const baseUrl = type === "sale" ? "/wms/sales" : "/wms/purchase";

  const handleCreateOrder = () => {
    history.push(`${baseUrl}/orders/create`);
  };

  return (
    <Stack 
      direction={{ xs: 'column', sm: 'row' }} 
      spacing={2} 
      alignItems={{ xs: 'flex-start', sm: 'center' }} 
      justifyContent="space-between"
    >
      <Typography variant="h5" fontWeight="bold">
        {type === "sale" ? "Danh sách đơn hàng bán" : "Danh sách đơn đặt hàng"}
      </Typography>
      <Stack direction="row" spacing={1}>
        <Button 
          variant="outlined" 
          startIcon={<Refresh />} 
          onClick={onRefresh}
        >
          Làm mới
        </Button>
        <Button 
          variant="outlined" 
          startIcon={<ImportExport />} 
          onClick={onExport}
        >
          Xuất file
        </Button>
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<Add />}
          onClick={handleCreateOrder}
        >
          {type === "sale" ? "Tạo đơn hàng" : "Tạo đơn đặt hàng"}
        </Button>
      </Stack>
    </Stack>
  );
};

export default OrderHeader;