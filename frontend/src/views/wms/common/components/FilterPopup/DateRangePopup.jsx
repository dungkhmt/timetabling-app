import React, { useState } from 'react';
import { 
  Box, 
  Typography, 
  Button, 
  Divider,
  TextField
} from '@mui/material';

const DateRangePopup = ({ onClose, onApply, startDate, endDate }) => {
  const [dateRange, setDateRange] = useState({
    startDate: startDate || '',
    endDate: endDate || ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setDateRange(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleApply = () => {
    if (dateRange.startDate && dateRange.endDate) {
      onApply(dateRange);
    }
  };

  return (
    <Box sx={{ width: 300, p: 2 }}>
      <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
        Lọc theo ngày tạo
      </Typography>
      
      <Divider sx={{ my: 1 }} />
      
      <Box sx={{ my: 2 }}>
        <TextField
          label="Từ ngày"
          type="date"
          name="startDate"
          value={dateRange.startDate}
          onChange={handleChange}
          fullWidth
          margin="normal"
          InputLabelProps={{
            shrink: true,
          }}
        />
        
        <TextField
          label="Đến ngày"
          type="date"
          name="endDate"
          value={dateRange.endDate}
          onChange={handleChange}
          fullWidth
          margin="normal"
          InputLabelProps={{
            shrink: true,
          }}
          error={dateRange.endDate && dateRange.startDate > dateRange.endDate}
          helperText={
            dateRange.endDate && dateRange.startDate > dateRange.endDate
              ? "Ngày kết thúc phải sau ngày bắt đầu"
              : ""
          }
        />
      </Box>
      
      <Divider sx={{ my: 1 }} />
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
        <Button onClick={onClose}>
          Hủy
        </Button>
        <Button 
          variant="contained" 
          color="primary" 
          onClick={handleApply}
          disabled={!dateRange.startDate || !dateRange.endDate || dateRange.startDate > dateRange.endDate}
        >
          Áp dụng
        </Button>
      </Box>
    </Box>
  );
};

export default React.memo(DateRangePopup);