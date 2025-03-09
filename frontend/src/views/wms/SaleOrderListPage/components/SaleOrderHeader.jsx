import React from 'react';
import { Typography, Button, Stack } from '@mui/material';
import { Add, Refresh } from '@mui/icons-material';
import { useHistory, useLocation } from 'react-router-dom';

const SaleOrderHeader = ({ onRefresh }) => {
  const history = useHistory();
  const location = useLocation();

  const handleCreateOrder = () => {
    history.push(location.pathname + '/create');
  };

  return (
    <Stack 
      direction={{ xs: 'column', sm: 'row' }} 
      spacing={2} 
      alignItems={{ xs: 'flex-start', sm: 'center' }} 
      justifyContent="space-between"
    >
      <Typography variant="h5" fontWeight="bold">
        Danh sách đơn hàng
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
          variant="contained" 
          color="primary" 
          startIcon={<Add />}
          onClick={handleCreateOrder}
        >
          Tạo đơn hàng
        </Button>
      </Stack>
    </Stack>
  );
};

export default SaleOrderHeader;