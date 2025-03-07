import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider, TimePicker } from '@mui/x-date-pickers';
import { vi } from 'date-fns/locale';

const AddSessionModal = ({ open, onClose, onSubmit, examTimetableSessionCollectionId }) => {
  const [formData, setFormData] = useState({
    name: '',
    startTime: null,
    endTime: null,
    examTimetableSessionCollectionId: examTimetableSessionCollectionId || ''
  });

  const [formErrors, setFormErrors] = useState({
    name: '',
    startTime: '',
    endTime: ''
  });

  // Handle input changes
  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value
    });
    // Clear error when user types
    setFormErrors({
      ...formErrors,
      [name]: ''
    });
  };

  // Handle time picker changes
  const handleTimeChange = (type, newValue) => {
    setFormData({
      ...formData,
      [type]: newValue
    });
    // Clear error when user sets time
    setFormErrors({
      ...formErrors,
      [type]: ''
    });
  };

  // Handle form submission
  const handleSubmit = () => {
    // Validate form
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

    if (formData.startTime && formData.endTime && formData.startTime >= formData.endTime) {
      newErrors.endTime = 'Giờ kết thúc phải sau giờ bắt đầu';
      isValid = false;
    }

    setFormErrors(newErrors);

    if (isValid) {
      // Format data for submission
      // Create ISO strings but keep today's date for both start and end times
      const now = new Date();
      const startTime = new Date(formData.startTime);
      const endTime = new Date(formData.endTime);
      
      // Set year, month, day to today
      startTime.setFullYear(now.getFullYear());
      startTime.setMonth(now.getMonth());
      startTime.setDate(now.getDate());
      
      endTime.setFullYear(now.getFullYear());
      endTime.setMonth(now.getMonth());
      endTime.setDate(now.getDate());
      
      // Create displayName with formatted times
      const formatTimeString = (date) => {
        return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
      };
      
      const displayName = `${formData.name} (${formatTimeString(startTime)} - ${formatTimeString(endTime)})`;
      
      const submissionData = {
        name: formData.name,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        displayName: displayName,
        examTimetableSessionCollectionId: examTimetableSessionCollectionId
      };
      
      onSubmit(submissionData);
    }
  };

  const handleClose = () => {
    // Reset form when closing
    setFormData({
      name: '',
      startTime: null,
      endTime: null,
      examTimetableSessionCollectionId: examTimetableSessionCollectionId || ''
    });
    setFormErrors({
      name: '',
      startTime: '',
      endTime: ''
    });
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Thêm kíp thi mới</DialogTitle>
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
                error={!!formErrors.name}
                helperText={formErrors.name}
                placeholder="Ví dụ: Kíp 1"
              />
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={vi}>
                <TimePicker
                  label="Giờ bắt đầu"
                  value={formData.startTime}
                  onChange={(newValue) => handleTimeChange('startTime', newValue)}
                  renderInput={(params) => (
                    <TextField 
                      {...params} 
                      fullWidth 
                      error={!!formErrors.startTime}
                      helperText={formErrors.startTime}
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
                  value={formData.endTime}
                  onChange={(newValue) => handleTimeChange('endTime', newValue)}
                  renderInput={(params) => (
                    <TextField 
                      {...params} 
                      fullWidth 
                      error={!!formErrors.endTime}
                      helperText={formErrors.endTime}
                    />
                  )}
                  ampm={false}
                  views={['hours', 'minutes']}
                />
              </LocalizationProvider>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="caption" color="textSecondary">
                * Tất cả các trường là bắt buộc
              </Typography>
            </Grid>
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="inherit">Hủy</Button>
        <Button onClick={handleSubmit} color="primary" variant="contained">Thêm</Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddSessionModal;
