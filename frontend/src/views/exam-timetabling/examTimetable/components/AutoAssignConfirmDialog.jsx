import React, { useState, useEffect } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Box,
  Typography,
  Divider,
  FormControl,
  FormLabel,
  Paper,
  Chip,
  FormControlLabel,
  Grid,
  Checkbox,
  Select,
  MenuItem,
  InputLabel,
  FormHelperText
} from '@mui/material';
import { Warning, CalendarMonth,  Tune } from '@mui/icons-material';
import { format, parseISO } from 'date-fns';

const AutoAssignConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  assignedClasses,
  isProcessing,
  timetable,
  algorithms = [], 
  isLoadingAlgorithms = false 
}) => {
  const [selectedDates, setSelectedDates] = useState([]);
  const [dateOptions, setDateOptions] = useState([]);
  const [hasError, setHasError] = useState(false);
  const [selectedAlgorithm, setSelectedAlgorithm] = useState('');
  const [timeLimit, setTimeLimit] = useState('30');
  const [algorithmError, setAlgorithmError] = useState(false);

  // Time limit options in minutes
  const timeLimitOptions = [
    { value: '30', label: '30 phút' },
    { value: '60', label: '1 giờ' },
    { value: '120', label: '2 giờ' },
    { value: '180', label: '3 giờ' },
    { value: '300', label: '5 giờ' }
  ];
  
  useEffect(() => {
    if (algorithms.length > 0 && (!selectedAlgorithm || !algorithms.find(a => a.value === selectedAlgorithm))) {
      setSelectedAlgorithm(algorithms[0].value);
    }
  }, [algorithms, selectedAlgorithm]);

  useEffect(() => {
    if (!timetable) return;
    
    const options = [];
    const startDate = parseISO(timetable.planStartTime);
    const endDate = parseISO(timetable.planEndTime);
    const startWeek = timetable.planStartWeek;
    
    const startDayOfWeek = startDate.getDay(); 
    const daysFromMonday = startDayOfWeek === 0 ? 6 : startDayOfWeek - 1; 
    const firstMondayOfPlan = new Date(startDate);
    firstMondayOfPlan.setDate(startDate.getDate() - daysFromMonday);
    
    const currentWeekGroup = {
      week: startWeek,
      label: `Tuần ${startWeek}`,
      id: `week-${startWeek}`,
      dates: []
    };
      
    let currentDate = new Date(startDate);
    const endOfFirstWeek = new Date(currentDate);

    while (endOfFirstWeek.getDay() !== 0) {
      endOfFirstWeek.setDate(endOfFirstWeek.getDate() + 1);
    }

    while (currentDate <= endOfFirstWeek && currentDate <= endDate) {
      const dayOfWeek = currentDate.getDay();
      const dayName = dayOfWeek === 0 ? 'CN' : `T${dayOfWeek + 1}`;
      
      currentWeekGroup.dates.push({
        date: new Date(currentDate),
        value: format(currentDate, 'yyyy-MM-dd'),
        label: `T${startWeek} - ${dayName} ${format(currentDate, 'dd/MM/yyyy')}`
      });
      
      currentDate = new Date(currentDate);
      currentDate.setDate(currentDate.getDate() + 1);
    }
  
    if (currentWeekGroup.dates.length > 0) {
      options.push(currentWeekGroup);
    }
    
    let weekCounter = 1;
    currentDate = new Date(firstMondayOfPlan);
    currentDate.setDate(firstMondayOfPlan.getDate() + 7); 
    
    while (currentDate <= endDate) {
      const weekNumber = startWeek + weekCounter;
      
      const weekGroup = {
        week: weekNumber,
        label: `Tuần ${weekNumber}`,
        id: `week-${weekNumber}`,
        dates: []
      };
      
      for (let i = 0; i < 7; i++) {
        const dayDate = new Date(currentDate);
        dayDate.setDate(currentDate.getDate() + i);
        
        if (dayDate <= endDate) {
          const dayOfWeek = dayDate.getDay();
          const dayName = dayOfWeek === 0 ? 'CN' : `T${dayOfWeek + 1}`;
          
          weekGroup.dates.push({
            date: new Date(dayDate),
            value: format(dayDate, 'yyyy-MM-dd'),
            label: `T${weekNumber} - ${dayName} ${format(dayDate, 'dd/MM/yyyy')}`
          });
        } else {
          break;
        }
      }
      
      if (weekGroup.dates.length > 0) {
        options.push(weekGroup);
      }
      
      currentDate.setDate(currentDate.getDate() + 7);
      weekCounter++;
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
    
    const allSelected = weekDateValues.every(date => selectedDates.includes(date));
    
    if (allSelected) {
      setSelectedDates(prev => prev.filter(d => !weekDateValues.includes(d)));
    } else {
      setSelectedDates(prev => {
        const currentSet = new Set(prev);
        weekDateValues.forEach(date => currentSet.add(date));
        return Array.from(currentSet);
      });
    }
  };

  const handleSelectAll = () => {
    if (selectedDates.length === getAllDates().length) {
      setSelectedDates([]);
    } else {
      setSelectedDates(getAllDates());
    }
  };

  const getAllDates = () => {
    return dateOptions.flatMap(week => week.dates.map(date => date.value));
  };

  const handleConfirm = () => {
    let hasValidationError = false;
    
    if (selectedDates.length === 0) {
      setHasError(true);
      hasValidationError = true;
    } else {
      setHasError(false);
    }
    
    if (!selectedAlgorithm) {
      setAlgorithmError(true);
      hasValidationError = true;
    } else {
      setAlgorithmError(false);
    }
    
    if (hasValidationError) {
      return;
    }
    
    const data = {
      dates: selectedDates,
      algorithm: selectedAlgorithm,
      timeLimit: parseInt(timeLimit, 10) 
    };
    
    onConfirm(data);
  };

  useEffect(() => {
    if (!open) {
      setSelectedDates([]);
      setHasError(false);
      setSelectedAlgorithm('');
      setAlgorithmError(false);
      setTimeLimit('30'); 
    } else if (algorithms.length > 0 && !selectedAlgorithm) {
      setSelectedAlgorithm(algorithms[0].value);
    }
  }, [open, algorithms]);

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
  
  const isWeekSelected = (weekDates) => {
    return weekDates.every(date => selectedDates.includes(date.value));
  };
  
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
              Phân công tự động sẽ xóa phân công hiện tại của 
              <Box component="span" sx={{ fontWeight: 700, color: 'error.main', mx: 1 }}>
                {assignedClasses.length}
              </Box>
              lớp thi này:
            </Typography>
            <Box 
              sx={{ 
                my: 1, 
                maxHeight: '150px', 
                overflowY: 'auto', 
                p: 1, 
                border: '1px solid #eee',
                borderRadius: 1
              }}
            >
              <Typography variant="h6" fontWeight={700} color="error.main">
                {assignedClasses.map(item => item.examClassId).join(', ')}
              </Typography>
            </Box>
            <Divider sx={{ my: 2 }} />
          </>
        )}

        {/* Algorithm and Time Limit selection in a single row */}
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <Tune sx={{ mr: 1 }} color="primary" />
            <FormLabel required sx={{ color: 'text.primary', fontWeight: 500 }}>
              Cấu hình chạy thuật toán:
            </FormLabel>
          </Box>
          
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            {/* Algorithm selection */}
            <Grid item xs={8}>
              <FormControl fullWidth error={algorithmError} required size="small">
                <InputLabel id="algorithm-select-label">Thuật toán</InputLabel>
                <Select
                  labelId="algorithm-select-label"
                  id="algorithm-select"
                  value={selectedAlgorithm}
                  label="Thuật toán"
                  onChange={(e) => setSelectedAlgorithm(e.target.value)}
                  disabled={isLoadingAlgorithms || algorithms.length === 0}
                >
                  {isLoadingAlgorithms ? (
                    <MenuItem value="" disabled>
                      Đang tải danh sách thuật toán...
                    </MenuItem>
                  ) : algorithms.length === 0 ? (
                    <MenuItem value="" disabled>
                      Không có thuật toán nào
                    </MenuItem>
                  ) : (
                    algorithms.map((algorithm) => (
                      <MenuItem key={algorithm.value} value={algorithm.value}>
                        {algorithm.label}
                      </MenuItem>
                    ))
                  )}
                </Select>
                {algorithmError && <FormHelperText>Vui lòng chọn thuật toán</FormHelperText>}
              </FormControl>
            </Grid>
            
            {/* Time limit selection */}
            <Grid item xs={4}>
              <FormControl fullWidth size="small">
                <InputLabel id="time-limit-select-label">Thời gian tối đa</InputLabel>
                <Select
                  labelId="time-limit-select-label"
                  id="time-limit-select"
                  value={timeLimit}
                  label="Thời gian tối đa"
                  onChange={(e) => setTimeLimit(e.target.value)}
                >
                  {timeLimitOptions.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
          
          <Divider sx={{ my: 2 }} />
        </Box>

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
