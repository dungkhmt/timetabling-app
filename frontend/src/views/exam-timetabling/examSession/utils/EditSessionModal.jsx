import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Grid,
  Typography,
  Divider
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider, TimePicker } from '@mui/x-date-pickers';
import { vi } from 'date-fns/locale';

const EditSessionModal = ({ open, onClose, formData, onChange, onSubmit }) => {
  const [errors, setErrors] = useState({
    name: '',
    startTime: '',
    endTime: ''
  });
  
  const [startTimeDate, setStartTimeDate] = useState(null);
  const [endTimeDate, setEndTimeDate] = useState(null);
  
  useEffect(() => {
    if (formData.startTime) {
      setStartTimeDate(new Date(formData.startTime));
    }
    if (formData.endTime) {
      setEndTimeDate(new Date(formData.endTime));
    }
  }, [formData.startTime, formData.endTime]);

  // Handle text input changes
  const handleChange = (event) => {
    const { name, value } = event.target;
    onChange({
      target: {
        name,
        value
      }
    });
    // Clear error when user types
    setErrors({
      ...errors,
      [name]: ''
    });
  };

  // Handle time changes
  const handleTimeChange = (type, newValue) => {
    if (type === 'startTime') {
      setStartTimeDate(newValue);
      if (newValue) {
        const date = new Date(formData.startTime || new Date());
        date.setHours(newValue.getHours());
        date.setMinutes(newValue.getMinutes());
        onChange({
          target: {
            name: 'startTime',
            value: new Date.toISOString()
          }
        });
      }
    } else if (type === 'endTime') {
      setEndTimeDate(newValue);
      if (newValue) {
        const date = new Date(formData.endTime || new Date());
        date.setHours(newValue.getHours());
        date.setMinutes(newValue.getMinutes());
        onChange({
          target: {
            name: 'endTime',
            value: date.toISOString()
          }
        });
      }
    }
    
    setErrors({
      ...errors,
      [type]: ''
    });
  };

  const validateForm = () => {
    let isValid = true;
    const newErrors = { name: '', startTime: '', endTime: '' };

    if (!formData.name.trim()) {
      newErrors.name = 'Vui lòng nhập tên kíp thi';
      isValid = false;
    }

    if (!formData.startTime) {
      newErrors.startTime = 'Vui lòng chọn giờ bắt đầu';
      isValid = false;
    }

    if (!formData.endTime) {
      newErrors.endTime = 'Vui lòng chọn giờ kết thúc';
      isValid = false;
    }

    if (formData.startTime && formData.endTime) {
      const start = new Date(formData.startTime);
      const end = new Date(formData.endTime);
      
      const startTime = new Date();
      startTime.setHours(start.getHours());
      startTime.setMinutes(start.getMinutes());
      
      const endTime = new Date();
      endTime.setHours(end.getHours());
      endTime.setMinutes(end.getMinutes());
      
      if (startTime >= endTime) {
        newErrors.endTime = 'Giờ kết thúc phải sau giờ bắt đầu';
        isValid = false;
      }
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleSubmit = () => {
    if (validateForm()) {
      const start = new Date(formData.startTime);
      const end = new Date(formData.endTime);
      
      const formatTimeString = (date) => {
        return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
      };
      
      const displayName = `${formData.name} (${formatTimeString(start)} - ${formatTimeString(end)})`;
      
      onSubmit({
        ...formData,
        displayName
      });
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Typography variant="h6">Chỉnh sửa kíp thi</Typography>
      </DialogTitle>
      <Divider />
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Tên kíp thi"
                name="name"
                value={formData.name}
                onChange={handleChange}
                error={!!errors.name}
                helperText={errors.name}
              />
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={vi}>
                <TimePicker
                  label="Giờ bắt đầu"
                  value={startTimeDate}
                  onChange={(newValue) => handleTimeChange('startTime', newValue)}
                  renderInput={(params) => (
                    <TextField 
                      {...params} 
                      fullWidth 
                      error={!!errors.startTime}
                      helperText={errors.startTime}
                    />
                  )}
                  ampm={false}
                  views={['hours', 'minutes']}
                />
              </LocalizationProvider>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={vi}>
                <TimePicker
                  label="Giờ kết thúc"
                  value={endTimeDate}
                  onChange={(newValue) => handleTimeChange('endTime', newValue)}
                  renderInput={(params) => (
                    <TextField 
                      {...params} 
                      fullWidth 
                      error={!!errors.endTime}
                      helperText={errors.endTime}
                    />
                  )}
                  ampm={false}
                  views={['hours', 'minutes']}
                />
              </LocalizationProvider>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="caption" color="textSecondary">
                ID: {formData.id}
              </Typography>
            </Grid>
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="inherit">Hủy</Button>
        <Button onClick={handleSubmit} color="primary" variant="contained">Lưu</Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditSessionModal;
