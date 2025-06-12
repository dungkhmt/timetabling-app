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
import { BarChart } from '@mui/x-charts/BarChart';
import { useExamTimetableData } from 'services/useExamTimetableData';
import TimeDistribution from './components/TimeDistribution.jsx';

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
          <Grid item xs={6}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Tổng số lớp:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.totalClasses}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={6}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Lớp đã xếp:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.assignedClasses} ({timetableInfo.completionRate}%)
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={6}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Tổng số phòng:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.totalAvailableRooms}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={6}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Phòng đã sử dụng:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.usedRoomsCount}
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={5}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                Tổng số ngày thi:
              </Typography>
              <Typography variant="body1" fontWeight={500}>
                {timetableInfo.totalExamDays}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

// Group Multiple Exams Distribution Card 
const GroupMultipleExamsCard = ({ groupStats }) => {
  if (!groupStats || groupStats.length === 0) {
    return (
      <Card sx={{ height: '100%', boxShadow: 2, borderRadius: 2 }}>
        <Box sx={{ 
          background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
          color: 'white',
          p: 1.5,
        }}>
          <Typography variant="h6" fontWeight={600}>
            Phân bố ngày thi nhiều môn
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

  const top5Groups = [...groupStats]
    .sort((a, b) => b.daysWithMultipleExams - a.daysWithMultipleExams)
    .filter(group => group.daysWithMultipleExams > 0)
    .slice(0, 5);

  const countByRange = groupStats.reduce((acc, group) => {
    const count = group.daysWithMultipleExams;
    if (count === 0) acc['0'] = (acc['0'] || 0) + 1;
    else if (count <= 5) acc['1-5'] = (acc['1-5'] || 0) + 1;
    else if (count <= 10) acc['6-10'] = (acc['6-10'] || 0) + 1;
    else if (count <= 15) acc['11-15'] = (acc['11-15'] || 0) + 1;
    else acc['16+'] = (acc['16+'] || 0) + 1;
    return acc;
  }, {});

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
          Phân bố ngày thi nhiều môn
        </Typography>
      </Box>
      <CardContent>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary">
              Top 5 nhóm có nhiều ngày thi nhiều môn
            </Typography>
            <Box sx={{ height: 150, px: 2 }}>
              {top5Groups.length > 0 ? (
                <BarChart
                  dataset={top5Groups}
                  yAxis={[{ 
                    label: null, 
                  }]}
                  xAxis={[{ 
                    scaleType: 'band',
                    dataKey: 'groupName',
                    tickLabelStyle: {
                      angle: 0,
                      textAnchor: 'middle',
                      fontSize: 12
                    }
                  }]}
                  series={[{
                    dataKey: 'daysWithMultipleExams',
                    valueFormatter: (value) => `${value}`,
                  }]}
                  height={130}
                  width={top5Groups.length <= 3 ? 300 : undefined} 
                  margin={{ left: 10, right: 10, top: 20, bottom: 30 }}
                  layout="vertical" 
                  barProps={{
                    paddingInner: 0.4, 
                  }}
                  sx={{
                    '.MuiBarElement-root': {
                      maxWidth: '80px',
                    },
                  }}
                />
              ) : (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                  <Typography variant="body1" color="text.secondary">
                    Không có nhóm nào có ngày thi nhiều môn
                  </Typography>
                </Box>
              )}
            </Box>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 1 }}>
              Phân bố theo khoảng
            </Typography>
            <Grid container spacing={1} sx={{ mt: 0.5 }}>
              {Object.entries(countByRange).map(([range, count]) => (
                <Grid item xs={2} key={range}>
                  <Box sx={{ 
                    border: '1px solid #e0e0e0', 
                    borderRadius: 1, 
                    p: 1, 
                    textAlign: 'center',
                    backgroundColor: '#f5f5f5'
                  }}>
                    <Typography variant="h6" color="primary">{count}</Typography>
                    <Typography variant="caption">{range} ngày</Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

// Group Detail Statistics Card 
const GroupDetailStatisticsCard = ({ groupStats }) => {
  const [selectedGroup, setSelectedGroup] = useState(
    groupStats && groupStats.length > 0 ? groupStats[0].groupName : ''
  );

  if (!groupStats || groupStats.length === 0) {
    return (
      <Card sx={{ height: '100%', boxShadow: 2, borderRadius: 2 }}>
        <Box sx={{ 
          background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
          color: 'white',
          p: 1.5,
        }}>
          <Typography variant="h6" fontWeight={600}>
            Thống kê chi tiết theo nhóm lớp
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
  
  const dailyExamsData = selectedGroupData && selectedGroupData.examsPerDayDistribution ? 
    Object.entries(selectedGroupData.examsPerDayDistribution)
      .map(([date, count]) => ({
        date: date.substring(0, 5), 
        count,
        fullDate: date
      }))
      .sort((a, b) => {
        const [dayA, monthA] = a.fullDate.split('/');
        const [dayB, monthB] = b.fullDate.split('/');
        const dateA = new Date(2025, parseInt(monthA) - 1, parseInt(dayA));
        const dateB = new Date(2025, parseInt(monthB) - 1, parseInt(dayB));
        return dateA - dateB;
      }) : [];

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
          Thống kê chi tiết theo nhóm lớp
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
          <>
            <Grid container spacing={0.5} sx={{ mb: 0.5 }}>
              <Grid item xs={4}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                    Tổng số lớp:
                  </Typography>
                  <Typography variant="body1" fontWeight={500}>
                    {selectedGroupData.totalClasses}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={4}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                    Đã xếp lịch:
                  </Typography>
                  <Typography variant="body1" fontWeight={500}>
                    {selectedGroupData.assignedClasses} ({selectedGroupData.completionRate}%)
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={4}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                    Chưa xếp lịch:
                  </Typography>
                  <Typography variant="body1" fontWeight={500}>
                    {selectedGroupData.unassignedClasses}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={4}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                    Ngày thi nhiều môn:
                  </Typography>
                  <Typography variant="body1" fontWeight={500}>
                    {selectedGroupData.daysWithMultipleExams}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={4}>
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
            
            <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 0.5 }}>
              Phân bố số lượng bài thi theo ngày
            </Typography>
            
            <Box sx={{ height: 180, width: '100%', px: 2 }}>
              <BarChart
                dataset={dailyExamsData}
                yAxis={[{ 
                  label: null, 
                  max: Math.max(...dailyExamsData.map(d => d.count)) * 1.2,
                  min: 0,
                  tickNumber: Math.min(
                    Math.max(...dailyExamsData.map(d => d.count)) + 1,
                    5
                  ),
                  valueFormatter: (value) => {
                    if (Math.max(...dailyExamsData.map(d => d.count)) < 5) {
                      return value % 1 === 0 ? value.toString() : '';
                    }
                    return Math.round(value).toString();
                  }
                }]}
                xAxis={[{ 
                  scaleType: 'band',
                  dataKey: 'date',
                  tickLabelStyle: {
                    angle: 0,
                    textAnchor: 'middle',
                    fontSize: 12
                  }
                }]}
                series={[{
                  dataKey: 'count',
                  valueFormatter: (value) => `${value}`,
                  color: '#ef9f5e'
                }]}
                height={180}
                margin={{ left: 40, right: 10, top: 20, bottom: 30 }}
                barProps={{
                  paddingInner: 2, 
                }}
                sx={{
                  '.MuiBarElement-root': {
                    maxWidth: '20px', 
                  },
                }}
              />
            </Box>
          </>
        )}
      </CardContent>
    </Card>
  );
};

// Distribution Chart Component (for location distribution only)
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
    '#1976d2', '#388e3c', '#f57c00', '#d32f2f', '#7b1fa2', 
    '#fbc02d', '#00796b', '#5d4037', '#c2185b', '#455a64',
    '#64b5f6', '#2196f3', '#ff9800', '#e53935', '#8b9467', 
    '#66bb6a', '#1e88e5', '#4caf50', '#9c27b0', '#03a9f4'
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
    day: timetableStatistics.dailyDistribution,
    dailySession: timetableStatistics.dailySessionDistribution
  };

  const locationDistributionData = {
    room: timetableStatistics.roomDistribution,
    building: timetableStatistics.buildingDistribution
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: "flex", justifyContent: "center", mb: 3, alignItems: "center" }}>
        <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
          Thống Kê Lịch Thi
        </Typography>
      </Box>

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
        {/* General Information Card */}
        <Grid item xs={12} md={4.5} sx={{ height: '250px' }}>
          <GeneralInfoCard timetableInfo={timetableStatistics} />
        </Grid>

        {/* Group Multiple Exams Distribution Card */}
        <Grid item xs={12} md={7.5} sx={{ height: '250px' }}>
          <GroupMultipleExamsCard groupStats={timetableStatistics.groupAssignmentStats} />
        </Grid>

        {/* Group Detail Statistics Card */}
        <Grid item xs={12} sx={{ height: '370px' }}>
          <GroupDetailStatisticsCard groupStats={timetableStatistics.groupAssignmentStats} />
        </Grid>

        {/* Time Distribution Chart - Using the new component */}
        <Grid item xs={12} md={6} sx={{ height: '370px' }}>
          <TimeDistribution 
            title="Phân bố lớp theo thời gian"
            data={timeDistributionData}
          />
        </Grid>

        {/* Location Distribution Chart */}
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
