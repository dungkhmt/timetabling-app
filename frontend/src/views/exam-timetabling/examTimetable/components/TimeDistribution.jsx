import React, { useState } from 'react';
import {
  Box,
  Divider,
  FormControl,
  MenuItem,
  Paper,
  Select,
  Typography
} from '@mui/material';
import { PieChart } from '@mui/x-charts/PieChart';

const TimeDistribution = ({ title, data }) => {
  const [selectedMode, setSelectedMode] = useState('session');
  const [selectedDate, setSelectedDate] = useState('general');

  if (!data) {
    return (
      <Paper elevation={1} sx={{ height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Typography variant="body1" color="text.secondary">
          Không có dữ liệu
        </Typography>
      </Paper>
    );
  }

  const chartColors = [
    '#1976d2', '#388e3c', '#f57c00', '#d32f2f', '#7b1fa2', 
    '#fbc02d', '#00796b', '#5d4037', '#c2185b', '#455a64',
    '#64b5f6', '#2196f3', '#ff9800', '#e53935', '#8b9467', 
    '#66bb6a', '#1e88e5', '#4caf50', '#9c27b0', '#03a9f4'
  ];

  let chartData = [];
  
  if (selectedMode === 'session') {
    if (selectedDate === 'general') {
      // Use general session distribution
      const sessionData = data.session || [];
      chartData = sessionData.map((item, index) => ({
        id: index,
        value: item.count,
        label: item.name,
        color: chartColors[index % chartColors.length]
      }));
    } else {
      const dailySessionData = data.dailySession || [];
      const selectedDayData = dailySessionData.find(day => day.date === selectedDate);
      if (selectedDayData && selectedDayData.sessions) {
        chartData = selectedDayData.sessions.map((item, index) => ({
          id: index,
          value: item.count,
          label: `Kíp ${item.name}`,
          color: chartColors[index % chartColors.length]
        }));
      }
    }
  } else if (selectedMode === 'day') {
    // Use day distribution
    const dayData = data.day || [];
    chartData = dayData.map((item, index) => ({
      id: index,
      value: item.count,
      label: item.name,
      color: chartColors[index % chartColors.length]
    }));
  }

  const availableDates = data.dailySession ? data.dailySession.map(item => item.date) : [];

  const modes = [
    { value: 'session', label: 'Theo kíp thi' },
    { value: 'day', label: 'Theo ngày thi' }
  ];

  return (
    <Paper elevation={1} sx={{ height: '100%' }}>
      <Box sx={{
        p: 1.5,
        borderBottom: '1px solid #e0e0e0', 
        background: 'linear-gradient(90deg, #1976D2, #2196F3)',
        color: 'white',
        borderRadius: '4px 4px 0 0',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: 1
      }}>
        <Typography variant="h6" fontWeight={500}>
          {title}
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          {/* Mode Selection */}
          <FormControl variant="outlined" size="small" sx={{ minWidth: 120, backgroundColor: 'white', borderRadius: 1 }}>
            <Select
              value={selectedMode}
              onChange={(e) => {
                setSelectedMode(e.target.value);
                if (e.target.value !== 'session') {
                  setSelectedDate('general');
                }
              }}
              sx={{ color: '#1976D2', '& .MuiSelect-icon': { color: '#1976D2' } }}
            >
              {modes.map((mode) => (
                <MenuItem key={mode.value} value={mode.value}>
                  {mode.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {/* Date Selection (only show when session mode is selected) */}
          {selectedMode === 'session' && (
            <FormControl variant="outlined" size="small" sx={{ minWidth: 120, backgroundColor: 'white', borderRadius: 1 }}>
              <Select
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                sx={{ color: '#1976D2', '& .MuiSelect-icon': { color: '#1976D2' } }}
              >
                <MenuItem value="general">Tổng quan</MenuItem>
                {availableDates.map((date) => (
                  <MenuItem key={date} value={date}>
                    {date}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}
        </Box>
      </Box>
      
      <Box sx={{ p: 1.5, height: 'calc(100% - 70px)' }}>
        {chartData.length > 0 ? (
          <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
            <Box sx={{ height: '100%', width: '55%' }}>
              <PieChart
                series={[
                  {
                    data: chartData,
                    highlightScope: { faded: 'global', highlighted: 'item' },
                    faded: { innerRadius: 30, additionalRadius: -30, color: 'gray' },
                  },
                ]}
                height={220}
                margin={{ top: 0, bottom: 0, left: 0, right: 0 }}
                slotProps={{
                  legend: { hidden: true },
                }}
              />
            </Box>
            
            <Divider orientation="vertical" sx={{ height: '90%', mx: 1 }} />
            
            <Box 
              sx={{ 
                display: 'flex', 
                flexDirection: 'column', 
                alignItems: 'stretch',
                width: '45%',
                height: '90%',
                overflowY: 'auto',
                pl: 1,
                '&::-webkit-scrollbar': {
                  width: '8px',
                },
                '&::-webkit-scrollbar-thumb': {
                  backgroundColor: '#bdbdbd',
                  borderRadius: '4px',
                },
                '&::-webkit-scrollbar-track': {
                  backgroundColor: '#f5f5f5',
                }
              }}
            >
              {chartData.map((item, index) => (
                <Box key={index} sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                  <Box
                    sx={{
                      width: 12,
                      height: 12,
                      backgroundColor: item.color,
                      mr: 1,
                      flexShrink: 0
                    }}
                  />
                  <Typography variant="body2" noWrap sx={{ flexGrow: 1 }}>
                    {item.label} ({item.value})
                  </Typography>
                </Box>
              ))}
            </Box>
          </Box>
        ) : (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <Typography variant="body1" color="text.secondary">
              Không có dữ liệu hiển thị
            </Typography>
          </Box>
        )}
      </Box>
    </Paper>
  );
};

export default TimeDistribution;
