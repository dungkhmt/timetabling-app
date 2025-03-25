import React, { useState, useEffect } from 'react';
import { 
  Box, 
  TextField, 
  MenuItem, 
  Select, 
  Button, 
  FormControl, 
  InputLabel,
  Chip,
  OutlinedInput,
  Checkbox,
  ListItemText
} from '@mui/material';
import { SALE_CHANNELS } from 'views/wms/common/constants/constants';
import dayjs from 'dayjs';

const SaleOrderFilters = ({ filters, setFilters, onApplyFilters }) => {
  
  // Khởi tạo state ban đầu nếu chưa có
  useEffect(() => {
    if (!filters.saleChannelId && filters.saleChannelId !== null) {
      setFilters(prev => ({ ...prev, saleChannelId: [] }));
    }
  }, []);

  const handleSearchChange = (e) => {
    if(e.key === 'Enter') 
    setFilters(prev => ({ ...prev, keyword: e.target.value }));
  };

  const handleChannelChange = (e) => {
    const { value } = e.target;
    
    // Kiểm tra nếu "ALL" được chọn
    if (value.includes('ALL')) {
      // Nếu chọn "Tất cả", đặt saleChannelId thành null
      setFilters(prev => ({ ...prev, saleChannelId: null }));
    } 
    else {
      // Ngược lại, gán danh sách các channel được chọn
      setFilters(prev => ({ ...prev, saleChannelId: value }));
    }
  };

  const handleDateCreatedChange = (e) => {
    const value = e.target.value;

    
    let startDate, endDate;
    const now = dayjs();

    switch (value) {
      case 'today':
        startDate = now.startOf('day');
        endDate = now.endOf('day');
        break;
      case 'yesterday':
        startDate = now.subtract(1, 'day').startOf('day');
        endDate = now.subtract(1, 'day').endOf('day');
        break;
      case 'last7days':
        startDate = now.subtract(6, 'day').startOf('day');
        endDate = now.endOf('day');
        break;
      case 'thisMonth':
        startDate = now.startOf('month');
        endDate = now.endOf('month');
        break;
      default:
        startDate = null;
        endDate = null;
    }

    // Định dạng ngày theo chuẩn LocalDateTime
    const formatToLocalDateTime = (dayjsDate) => {
      if (!dayjsDate) return null;
      return dayjsDate.format('YYYY-MM-DDTHH:mm:ss');
    };

    setFilters(prev => ({ 
      ...prev, 
      dateCreated: value,
      startCreatedAt: formatToLocalDateTime(startDate),
      endCreatedAt: formatToLocalDateTime(endDate)
    }));
  };

  // Cấu hình hiển thị menu cho select
  const ITEM_HEIGHT = 48;
  const ITEM_PADDING_TOP = 8;
  const MenuProps = {
    PaperProps: {
      style: {
        maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
        width: 250,
      },
    },
  };

  // Kiểm tra nếu "Tất cả" được chọn
  const isAllSelected = filters.saleChannelId === null;

  // Hàm lấy giá trị channels đã chọn
  const getChannelValue = () => {
    if (isAllSelected) return [];
    return Array.isArray(filters.saleChannelId) ? filters.saleChannelId : [filters.saleChannelId];
  };

  return (
    <Box display="flex" gap={2} mt={2} flexWrap="wrap" alignItems="center">
      <TextField
        label="Tìm kiếm"
        variant="outlined"
        size="small"
        value={filters.keyword || ''}
        onChange={handleSearchChange}
        placeholder="Mã đơn hàng, tên khách hàng..."
        sx={{ flexGrow: 1, minWidth: '200px' }}
      />
      
      <FormControl size="small" sx={{ minWidth: 200 }}>
        <InputLabel id="channel-select-label">Kênh bán hàng</InputLabel>
        <Select
          labelId="channel-select-label"
          multiple
          value={getChannelValue()}
          onChange={handleChannelChange}
          input={<OutlinedInput label="Kênh bán hàng" />}
          renderValue={(selected) => {
            if (isAllSelected) return <Chip label="Tất cả" size="small" />;
            
            return (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {selected.map((value) => (
                  <Chip 
                    key={value} 
                    label={SALE_CHANNELS[value]} 
                    size="small" 
                  />
                ))}
              </Box>
            );
          }}
          MenuProps={MenuProps}
        >
          <MenuItem value="ALL">
            <Checkbox checked={isAllSelected} />
            <ListItemText primary="Tất cả" />
          </MenuItem>
          
          {Object.entries(SALE_CHANNELS).map(([channelId, label]) => (
            <MenuItem key={channelId} value={channelId}>
              <Checkbox 
                checked={getChannelValue().includes(channelId)} 
                disabled={isAllSelected}
              />
              <ListItemText primary={label} />
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      
      <FormControl size="small" sx={{ minWidth: 150 }}>
        <InputLabel>Ngày tạo</InputLabel>
        <Select
          label="Ngày tạo"
          onChange={handleDateCreatedChange}
        >
          <MenuItem value="date">Tất cả</MenuItem>
          <MenuItem value="today">Hôm nay</MenuItem>
          <MenuItem value="yesterday">Hôm qua</MenuItem>
          <MenuItem value="last7days">7 ngày qua</MenuItem>
          <MenuItem value="thisMonth">Tháng này</MenuItem>
        </Select>
      </FormControl>
      
      <Button 
        variant="contained" 
        onClick={onApplyFilters}
        color="primary"
      >
        Áp dụng
      </Button>
    </Box>
  );
};

export default SaleOrderFilters;