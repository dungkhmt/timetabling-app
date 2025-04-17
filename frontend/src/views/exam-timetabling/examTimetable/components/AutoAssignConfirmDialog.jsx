import React, { useState, useEffect } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  Divider,
  FormControl,
  FormLabel,
  Paper,
  Chip,
  IconButton,
  FormControlLabel,
  Grid,
  Checkbox
} from '@mui/material';
import { Warning, CalendarMonth, ExpandMore } from '@mui/icons-material';
import { format, addDays, parseISO } from 'date-fns';

const AutoAssignConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  assignedClasses,
  isProcessing,
  timetable
}) => {
  const [selectedDates, setSelectedDates] = useState([]);
  const [dateOptions, setDateOptions] = useState([]);
  const [hasError, setHasError] = useState(false);

  // Generate date options based on timetable plan dates and weeks
  useEffect(() => {
    if (!timetable) return;
    
    const options = [];
    const startDate = parseISO(timetable.planStartTime);
    const endDate = parseISO(timetable.planEndTime);
    const startWeek = timetable.planStartWeek;
    
    // Calculate the number of days between start and end dates
    const daysDiff = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24));
    
    // Generate all dates in the range, grouped by week
    for (let week = 0; week < Math.ceil(daysDiff / 7) + 1; week++) {
      const weekNumber = startWeek + week;
      const weekStart = addDays(startDate, week * 7);
      
      // Create week group
      const weekGroup = {
        week: weekNumber,
        label: `Tuần ${weekNumber}`,
        id: `week-${weekNumber}`,
        dates: []
      };
      
      // Add days for this week (max 7 days or until end date)
      for (let day = 0; day < 7; day++) {
        const currentDate = addDays(weekStart, day);
        if (currentDate <= endDate) {
          const dayOfWeek = currentDate.getDay();
          // Convert to Vietnamese day naming (T2, T3, etc., CN for Sunday)
          const dayName = dayOfWeek === 0 ? 'CN' : `T${dayOfWeek + 1}`;
          
          weekGroup.dates.push({
            date: currentDate,
            value: format(currentDate, 'yyyy-MM-dd'),
            label: `T${weekNumber} - ${dayName} ${format(currentDate, 'dd/MM/yyyy')}`
          });
        }
      }
      
      // Only add week if it has dates
      if (weekGroup.dates.length > 0) {
        options.push(weekGroup);
      }
    }
    
    setDateOptions(options);
  }, [timetable]);

  const handleDateToggle = (dateValue) => {
    setSelectedDates(prev => {
      if (prev.includes(dateValue)) {
        return prev.filter(d => d !== dateValue);
      } else {
        return [...prev, dateValue];
      }
    });
  };

  const handleWeekToggle = (weekDates) => {
    const weekDateValues = weekDates.map(d => d.value);
    
    // Check if all dates in this week are already selected
    const allSelected = weekDateValues.every(date => selectedDates.includes(date));
    
    if (allSelected) {
      // If all selected, remove them all
      setSelectedDates(prev => prev.filter(d => !weekDateValues.includes(d)));
    } else {
      // Otherwise, add all missing dates
      setSelectedDates(prev => {
        const currentSet = new Set(prev);
        weekDateValues.forEach(date => currentSet.add(date));
        return Array.from(currentSet);
      });
    }
  };

  const handleSelectAll = () => {
    if (selectedDates.length === getAllDates().length) {
      // If all are selected, deselect all
      setSelectedDates([]);
    } else {
      // Otherwise select all
      setSelectedDates(getAllDates());
    }
  };

  const getAllDates = () => {
    return dateOptions.flatMap(week => week.dates.map(date => date.value));
  };

  const handleConfirm = () => {
    if (selectedDates.length === 0) {
      setHasError(true);
      return;
    }
    onConfirm(selectedDates);
  };

  // Reset selected dates when dialog closes
  useEffect(() => {
    if (!open) {
      setSelectedDates([]);
      setHasError(false);
    }
  }, [open]);

  // Helper function to find date label by value
  const getDateLabel = (dateValue) => {
    for (const week of dateOptions) {
      for (const date of week.dates) {
        if (date.value === dateValue) {
          return date.label;
        }
      }
    }
    return dateValue;
  };
  
  // Check if all dates in a week are selected
  const isWeekSelected = (weekDates) => {
    return weekDates.every(date => selectedDates.includes(date.value));
  };
  
  // Check if some but not all dates in a week are selected
  const isWeekPartiallySelected = (weekDates) => {
    return weekDates.some(date => selectedDates.includes(date.value)) && 
           !weekDates.every(date => selectedDates.includes(date.value));
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3,
          width: '100%'
        }
      }}
    >
      <DialogTitle sx={{ 
        bgcolor: 'warning.light', 
        display: 'flex', 
        alignItems: 'center',
        gap: 1
      }}>
        <Warning color="warning" />
        <Typography variant="h6" fontWeight={600}>
          Xác nhận phân công tự động
        </Typography>
      </DialogTitle>
      
      <DialogContent sx={{ mt: 2 }}>
        {assignedClasses.length > 0 && (
          <>
            <Typography variant="body1">
              Phân công tự động sẽ xóa phân công hiện tại của các lớp thi này:
            </Typography>
            <Typography variant="h6" fontWeight={700} color="error.main" sx={{ my: 1 }}>
              {assignedClasses.map(item => item.examClassId).join(', ')}
            </Typography>
            <Divider sx={{ my: 2 }} />
          </>
        )}

        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <CalendarMonth sx={{ mr: 1 }} color="primary" />
            <FormLabel required sx={{ color: 'text.primary', fontWeight: 500 }}>
              Chọn ngày thi để phân công tự động:
            </FormLabel>
          </Box>
          
          <FormControl fullWidth error={hasError} required component={Paper} 
            variant="outlined" sx={{ p: 2, borderRadius: 1 }}>
            
            {/* Select All Option */}
            <FormControlLabel
              control={
                <Checkbox 
                  checked={selectedDates.length > 0 && selectedDates.length === getAllDates().length}
                  indeterminate={selectedDates.length > 0 && selectedDates.length < getAllDates().length}
                  onChange={handleSelectAll}
                />
              }
              label={<Typography fontWeight={600}>Chọn tất cả</Typography>}
            />
            
            <Divider sx={{ my: 1 }} />
            
            {/* Week Groups */}
            {dateOptions.map((week) => (
              <Box key={week.id}>
                <FormControlLabel
                  control={
                    <Checkbox 
                      checked={isWeekSelected(week.dates)}
                      indeterminate={isWeekPartiallySelected(week.dates)}
                      onChange={() => handleWeekToggle(week.dates)}
                    />
                  }
                  label={<Typography fontWeight={600}>{week.label}</Typography>}
                />
                
                <Box sx={{ ml: 3, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {week.dates.map((date) => (
                    <Chip
                      key={date.value}
                      label={date.label}
                      clickable
                      onClick={() => handleDateToggle(date.value)}
                      color={selectedDates.includes(date.value) ? "primary" : "default"}
                      variant={selectedDates.includes(date.value) ? "filled" : "outlined"}
                      sx={{ 
                        my: 0.5,
                        '&.MuiChip-filled': { fontWeight: 600 }
                      }}
                    />
                  ))}
                </Box>
                
                {week !== dateOptions[dateOptions.length - 1] && <Divider sx={{ my: 1 }} />}
              </Box>
            ))}
            
            {hasError && (
              <Typography color="error" variant="caption" sx={{ mt: 1 }}>
                Vui lòng chọn ít nhất một ngày
              </Typography>
            )}
          </FormControl>
        </Box>
      </DialogContent>
      
      <DialogActions sx={{ p: 2, bgcolor: '#f5f5f5' }}>
        <Button 
          onClick={onClose} 
          variant="outlined"
          disabled={isProcessing}
        >
          Hủy
        </Button>
        <Button 
          onClick={handleConfirm} 
          variant="contained" 
          color="warning"
          disabled={isProcessing || selectedDates.length === 0}
        >
          {isProcessing ? 'Đang xử lý...' : 'Tiếp tục phân công tự động'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AutoAssignConfirmDialog;
