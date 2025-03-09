import React, { useState } from 'react';
import { 
  Box, 
  Typography, 
  Checkbox, 
  FormControlLabel,
  Button,
  Divider
} from '@mui/material';

const statuses = [
  { value: 'NEW', label: 'Mới tạo' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'DELIVERING', label: 'Đang giao' },
  { value: 'COMPLETED', label: 'Hoàn thành' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

const StatusFilterPopup = ({ onClose, onApply, currentStatuses = [] }) => {
  const [selectedStatuses, setSelectedStatuses] = useState(
    currentStatuses || []
  );

  const handleChange = (status) => {
    setSelectedStatuses(prev => {
      if (prev.includes(status)) {
        return prev.filter(s => s !== status);
      } else {
        return [...prev, status];
      }
    });
  };

  const handleApply = () => {
    onApply(selectedStatuses.length > 0 ? selectedStatuses : null);
  };

  return (
    <Box sx={{ width: 300, p: 2 }}>
      <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
        Lọc theo trạng thái
      </Typography>
      
      <Divider sx={{ my: 1 }} />
      
      <Box sx={{ maxHeight: 300, overflow: 'auto', my: 2 }}>
        {statuses.map(status => (
          <FormControlLabel
            key={status.value}
            control={
              <Checkbox 
                checked={selectedStatuses.includes(status.value)} 
                onChange={() => handleChange(status.value)} 
              />
            }
            label={status.label}
          />
        ))}
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
        >
          Áp dụng
        </Button>
      </Box>
    </Box>
  );
};

export default React.memo(StatusFilterPopup);