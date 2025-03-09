import React from 'react';
import { Box, TextField, MenuItem, Select, Button, FormControl, InputLabel } from '@mui/material';

const SaleOrderFilters = ({ filters, setFilters, onApplyFilters }) => {
  const handleSearchChange = (e) => {
    setFilters(prev => ({ ...prev, search: e.target.value }));
  };

  const handleDeliveryStatusChange = (e) => {
    setFilters(prev => ({ ...prev, deliveryStatus: e.target.value }));
  };

  const handleDateCreatedChange = (e) => {
    setFilters(prev => ({ ...prev, dateCreated: e.target.value }));
  };

  return (
    <Box display="flex" gap={2} mt={2} flexWrap="wrap" alignItems="center">
      <TextField
        label="Tìm kiếm"
        variant="outlined"
        size="small"
        value={filters.search}
        onChange={handleSearchChange}
        placeholder="Mã đơn hàng, tên khách hàng..."
        sx={{ flexGrow: 1, minWidth: '200px' }}
      />
      
      <FormControl size="small" sx={{ minWidth: 150 }}>
        <InputLabel>Trạng thái giao hàng</InputLabel>
        <Select
          value={filters.deliveryStatus}
          label="Trạng thái giao hàng"
          onChange={handleDeliveryStatusChange}
        >
          <MenuItem value="all">Tất cả</MenuItem>
          <MenuItem value="waiting">Chờ xử lý</MenuItem>
          <MenuItem value="shipping">Đang giao hàng</MenuItem>
          <MenuItem value="delivered">Đã giao</MenuItem>
          <MenuItem value="canceled">Đã hủy</MenuItem>
        </Select>
      </FormControl>
      
      <FormControl size="small" sx={{ minWidth: 150 }}>
        <InputLabel>Ngày tạo</InputLabel>
        <Select
          value={filters.dateCreated}
          label="Ngày tạo"
          onChange={handleDateCreatedChange}
        >
          <MenuItem value="date">Ngày tạo</MenuItem>
          <MenuItem value="today">Hôm nay</MenuItem>
          <MenuItem value="yesterday">Hôm qua</MenuItem>
          <MenuItem value="last7days">7 ngày qua</MenuItem>
          <MenuItem value="thisMonth">Tháng này</MenuItem>
        </Select>
      </FormControl>
      
      <Button variant="contained" onClick={onApplyFilters}>
        Áp dụng
      </Button>
    </Box>
  );
};

export default SaleOrderFilters;
