import { useState } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Grid,
  CircularProgress,
  IconButton,
  InputAdornment
} from '@mui/material';
import { Close } from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { vi } from 'date-fns/locale';

const AddExamPlanModal = ({ open, onClose, onSave, isSaving }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startTime: null, // Match the field name used in DatePicker
    endTime: null,   // Match the field name used in DatePicker
    startWeek: '',   // Add startWeek field with empty string as initial value
  });

  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    // Special handling for startWeek to ensure it's a positive number
    if (name === 'startWeek') {
      // Only allow positive numbers or empty string (for backspace/delete)
      const regex = /^[1-9]\d*$/;
      if (value === '' || regex.test(value)) {
        setFormData({
          ...formData,
          [name]: value
        });
      }
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
    
    // Clear error when user types
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: ''
      });
    }
  };

  const handleDateChange = (name, date) => {
    setFormData({
      ...formData,
      [name]: date
    });
    
    // Clear error when user selects a date
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: ''
      });
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.name.trim()) {
      newErrors.name = 'Tên kế hoạch không được để trống';
    }
    
    if (!formData.startTime) {
      newErrors.startTime = 'Ngày bắt đầu không được để trống';
    }
    
    if (!formData.endTime) {
      newErrors.endTime = 'Ngày kết thúc không được để trống';
    }
    
    if (formData.startTime && formData.endTime && formData.startTime > formData.endTime) {
      newErrors.endTime = 'Ngày kết thúc phải sau ngày bắt đầu';
    }
    
    // Validate startWeek
    if (!formData.startWeek) {
      newErrors.startWeek = 'Tuần bắt đầu không được để trống';
    } else if (parseInt(formData.startWeek) <= 0) {
      newErrors.startWeek = 'Tuần bắt đầu phải là số dương';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (validateForm()) {
      // Convert startWeek from string to number before saving
      const formDataToSubmit = {
        ...formData,
        startWeek: parseInt(formData.startWeek)
      };
      
      onSave(formDataToSubmit);

      setFormData({
        name: '',
        description: '',
        startTime: null,
        endTime: null,
        startWeek: ''
      });
      setErrors({});
    }
  };

  const handleCloseModal = () => {
    setFormData({
      name: '',
      description: '',
      startTime: null,
      endTime: null,
      startWeek: ''
    });
    setErrors({});
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleCloseModal}
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle 
        sx={{ 
          pb: 1,
          textAlign: 'center',
          borderBottom: '1px solid #e0e0e0',
          mb: 1,
          position: 'relative',
          fontWeight: 600,
          background: 'linear-gradient(90deg, #1976D2, #2196F3)',
        }}

        color={'white'}
        fontSize={'h5.fontSize'}
      >
        Tạo Kế Hoạch Thi Mới
        <IconButton
          onClick={handleCloseModal}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: 'white',
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>
      
      <DialogContent>
        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={vi}>
          <Box sx={{ pt: 2 }}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  name="name"
                  label="Tên kế hoạch thi"
                  fullWidth
                  value={formData.name}
                  onChange={handleChange}
                  error={!!errors.name}
                  helperText={errors.name}
                  required
                />
              </Grid>
              
              {/* Start Week Field */}
              <Grid item xs={12}>
                <TextField
                  name="startWeek"
                  label="Tuần bắt đầu"
                  type="text"
                  fullWidth
                  value={formData.startWeek}
                  onChange={handleChange}
                  error={!!errors.startWeek}
                  helperText={errors.startWeek}
                  required
                  InputProps={{
                    endAdornment: <InputAdornment position="end">tuần</InputAdornment>,
                  }}
                  inputProps={{
                    inputMode: 'numeric',
                    pattern: '[1-9][0-9]*'
                  }}
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  name="description"
                  label="Mô tả (tùy chọn)"
                  fullWidth
                  multiline
                  rows={3}
                  value={formData.description}
                  onChange={handleChange}
                />
              </Grid>
              
         {/* Row with date pickers */}
              <Grid item xs={12}>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <DatePicker
                      label="Ngày bắt đầu"
                      value={formData.startTime}
                      onChange={(date) => handleDateChange('startTime', date)}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          required: true,
                          error: !!errors.startTime,
                          helperText: errors.startTime,
                          size: "small"
                        }
                      }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <DatePicker
                      label="Ngày kết thúc"
                      value={formData.endTime}
                      onChange={(date) => handleDateChange('endTime', date)}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          required: true,
                          error: !!errors.endTime,
                          helperText: errors.endTime,
                          size: "small"
                        }
                      }}
                    />
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </Box>
        </LocalizationProvider>
      </DialogContent>
      
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={handleCloseModal} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          color="primary"
          disabled={isSaving}
          startIcon={isSaving ? <CircularProgress size={20} /> : null}
        >
          {isSaving ? 'Đang lưu...' : 'Lưu kế hoạch'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddExamPlanModal;
