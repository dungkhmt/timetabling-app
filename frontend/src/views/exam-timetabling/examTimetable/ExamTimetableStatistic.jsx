import React, { useState } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Divider,
  FormControl,
  Grid,
  MenuItem,
  Paper,
  Select,
  Typography,
  Button
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { PieChart } from '@mui/x-charts/PieChart';
import { useExamTimetableData } from 'services/useExamTimetableData';

// General Information Component
const GeneralInfoCard = ({ timetableInfo }) => {
  if (!timetableInfo) return null;

  return (
    <Card sx={{ 
      height: '100%', 
      boxShadow: 2, 
      borderRadius: 2,
      overflow: 'hidden',
      border: '1px solid #ddd',
    }}>
      <Box sx={{ 
        background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
        color: 'white',
        p: 1.5,
      }}>
        <Typography variant="h6" fontWeight={600}>
          Thông tin lịch thi
        </Typography>
      </Box>
      <CardContent>
        <Typography variant="h5" fontWeight={600} color="primary" gutterBottom>
          {timetableInfo.timetableName}
        </Typography>
        
        <Grid container justifyContent={'space-between'} spacing={1} sx={{ mt: 0.5 }}>
          <Grid item xs={4}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Tổng số lớp:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.totalClasses}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={8}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Lớp đã xếp:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.assignedClasses} ({timetableInfo.completionRate}%)
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={4}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Tổng số phòng:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.totalAvailableRooms}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={8}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Phòng đã sử dụng:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.usedRoomsCount}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={4}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Tổng số ngày thi:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.totalExamDays}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={8}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Bộ kíp thi:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.sessionCollectionName}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

// Group Statistics Component
const GroupStatisticsCard = ({ groupStats }) => {
  const [selectedGroup, setSelectedGroup] = useState(
    groupStats && groupStats.length > 0 ? groupStats[0].groupName : ''
  );

  if (!groupStats || groupStats.length === 0) {
    return (
      <Card sx={{ height: '100%', boxShadow: 2, borderRadius: 2 }}>
        <Box sx={{ 
          background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
          color: 'white',
          p: 2,
        }}>
          <Typography variant="h6" fontWeight={600}>
            Thống kê theo nhóm lớp
          </Typography>
        </Box>
        <CardContent sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80%' }}>
          <Typography variant="body1" color="text.secondary">
            Không có dữ liệu nhóm lớp
          </Typography>
        </CardContent>
      </Card>
    );
  }

  const selectedGroupData = groupStats.find(group => group.groupName === selectedGroup);

  return (
    <Card sx={{ 
      height: '100%', 
      boxShadow: 2, 
      borderRadius: 2,
      overflow: 'hidden',
      border: '1px solid #ddd',
    }}>
      <Box sx={{ 
        background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
        color: 'white',
        p: 1.5,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <Typography variant="h6" fontWeight={600}>
          Thống kê theo nhóm lớp
        </Typography>
        <FormControl variant="outlined" size="small" sx={{ minWidth: 200, backgroundColor: 'white', borderRadius: 1 }}>
          <Select
            value={selectedGroup}
            onChange={(e) => setSelectedGroup(e.target.value)}
            sx={{ color: '#1976D2', '& .MuiSelect-icon': { color: '#1976D2' } }}
          >
            {groupStats.map((group) => (
              <MenuItem key={group.groupName} value={group.groupName}>
                {group.groupName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>
      <CardContent>
        {selectedGroupData && (
          <Grid container spacing={1}>
            <Grid item xs={6}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                  Tổng số lớp:
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {selectedGroupData.totalClasses}
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={6}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                  Đã xếp lịch:
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {selectedGroupData.assignedClasses} ({selectedGroupData.completionRate}%)
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={6}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                  Chưa xếp lịch:
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {selectedGroupData.unassignedClasses}
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={6}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                  Ngày thi nhiều môn:
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {selectedGroupData.daysWithMultipleExams}
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                  Thời gian nghỉ trung bình:
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {selectedGroupData.averageRelaxTimeBetweenCourses} ngày
                </Typography>
              </Box>
            </Grid>
          </Grid>
        )}
      </CardContent>
    </Card>
  );
};

// Distribution Chart Component
const DistributionChart = ({ title, data, chartType, onModeChange, modes }) => {
  const [selectedMode, setSelectedMode] = useState(modes[0].value);

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
    '#1976d2', // Blue
    '#388e3c', // Green
    '#f57c00', // Orange
    '#d32f2f', // Red
    '#7b1fa2', // Purple
    '#fbc02d', // Yellow
    '#00796b', // Teal
    '#5d4037', // Brown
    '#c2185b', // Pink
    '#455a64',  // Blue Grey
    '#64b5f6', // Light Blue
    '#2196f3', // Dark Blue
    '#ff9800', // Deep Orange
    '#e53935', // Red
    '#8b9467', // Light Green
    '#66bb6a', // Light Teal
    '#1e88e5', // Dark Blue Grey
    '#4caf50', // Light Green
    '#9c27b0', // Deep Purple
    '#03a9f4'  // Cyan
  ];

  const selectedData = data[selectedMode] || [];
  const chartData = selectedData.map((item, index) => ({
    id: index,
    value: item.count,
    label: item.name,
    color: chartColors[index % chartColors.length]
  }));

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
        alignItems: 'center'
      }}>
        <Typography variant="h6" fontWeight={500}>
          {title}
        </Typography>
        <FormControl variant="outlined" size="small" sx={{ minWidth: 150, backgroundColor: 'white', borderRadius: 1 }}>
          <Select
            value={selectedMode}
            onChange={(e) => {
              setSelectedMode(e.target.value);
              if (onModeChange) onModeChange(e.target.value);
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
      </Box>
      
      <Box sx={{ p: 1.5, height: 'calc(100% - 50px)' }}>
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
                height: '100%',
                overflow: 'auto',
                pl: 1
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

// Main Component
const TimetableStatisticsPanel = () => {
  const { id } = useParams();
  const history = useHistory(); 
  const { 
    statistics: timetableStatistics,
    isLoadingStatistic: isLoading,
    errorStatistic: error,
  } = useExamTimetableData(null, id);

  const handleGoBack = () => {
    history.push(`/exam-time-tabling/exam-timetable/${id}`);
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Typography variant="h6" color="error">
          Lỗi: {error.message || 'Không thể tải dữ liệu thống kê'}
        </Typography>
      </Box>
    );
  }

  if (!timetableStatistics) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Typography variant="h6" color="textSecondary">
          Không có dữ liệu thống kê
        </Typography>
      </Box>
    );
  }

  const timeDistributionData = {
    session: timetableStatistics.sessionDistribution,
    day: timetableStatistics.dailyDistribution
  };

  const locationDistributionData = {
    room: timetableStatistics.roomDistribution,
    building: timetableStatistics.buildingDistribution
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Add Back Button */}
      <Box sx={{ display: 'flex', mb: 2 }}>
        <Button 
          variant="contained" 
          sx={{ mt: 2 }} 
          onClick={handleGoBack}
        >
          <ArrowBackIcon sx={{ mr: 1 }} />
          Quay lại danh sách
        </Button>
      </Box>

      <Grid container spacing={3} sx={{ minHeight: 'calc(100vh - 200px)' }}>
        {/* Region A: General Information */}
        <Grid item xs={12} md={6} sx={{ height: '250px' }}>
          <GeneralInfoCard timetableInfo={timetableStatistics} />
        </Grid>

        {/* Region B: Group Statistics */}
        <Grid item xs={12} md={6} sx={{ height: '250px' }}>
          <GroupStatisticsCard groupStats={timetableStatistics.groupAssignmentStats} />
        </Grid>

        {/* Region C: Time Distribution Chart */}
        <Grid item xs={12} md={6} sx={{ height: '370px' }}>
          <DistributionChart 
            title="Phân bố lớp theo thời gian"
            data={timeDistributionData}
            chartType="pie"
            modes={[
              { value: 'session', label: 'Theo kíp thi' },
              { value: 'day', label: 'Theo ngày thi' }
            ]}
          />
        </Grid>

        {/* Region D: Location Distribution Chart */}
        <Grid item xs={12} md={6} sx={{ height: '370px' }}>
          <DistributionChart 
            title="Phân bố lớp theo địa điểm"
            data={locationDistributionData}
            chartType="pie"
            modes={[
              { value: 'room', label: 'Theo phòng' },
              { value: 'building', label: 'Theo tòa nhà' }
            ]}
          />
        </Grid>
      </Grid>
    </Container>
  );
};

export default TimetableStatisticsPanel;
