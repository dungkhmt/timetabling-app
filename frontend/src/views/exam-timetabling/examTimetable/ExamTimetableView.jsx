import { useState } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  ToggleButtonGroup,
  ToggleButton,
  CircularProgress,
  Paper,
  Button
} from '@mui/material';
import { ViewWeek, ViewModule, ArrowBack, Error } from '@mui/icons-material';
import { format } from 'date-fns';
import RoomBasedAssignmentView from './components/RoomBasedAssignmentView';
import { useExamTimetableData } from 'services/useExamTimetableData';
import { useExamRoomData } from 'services/useExamRoomData';
import { useExamTimetableAssignmentData } from 'services/useExamTimetableAssignmentData';
import ClassBasedAssignmentView from './components/ClassBasedAssignmentView'

const ExamTimeTableView = () => {
  const { id } = useParams();
  const history = useHistory();
  const [viewMode, setViewMode] = useState('room-based');

  const { timetable, isLoadingDetail } = useExamTimetableData(null, id);
  const { examRooms } = useExamRoomData();
  const { examTimetableAssignments, isLoading } = useExamTimetableAssignmentData(id);

  const handleViewModeChange = (event, newMode) => {
    if (newMode !== null) {
      setViewMode(newMode);
    }
  };

  const handleGoBack = () => {
    history.push(`/exam-time-tabling/exam-timetable/${id}`);
  };

  if (isLoadingDetail || isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!timetable) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4 }}>
        <Error sx={{ fontSize: 80, color: 'gray', mb: 2 }} />
        <Typography variant="h6" color="textSecondary">Không tìm thấy lịch thi</Typography>
        <Button 
          variant="contained" 
          sx={{ mt: 2 }} 
          onClick={() => history.push('/exam-plan')}
        >
          Quay lại danh sách
        </Button>
      </Box>
    );
  }

  const slots = [];
  timetable.dates.forEach(date => {
    timetable.slots.forEach(slot => {
      slots.push({
        week: date.weekNumber,
        date: date.date,
        dateDisplay: date.name,
        slotId: slot.id,
        slotName: slot.name,
        displayName: `${date.name} - ${slot.name}`
      });
    });
  });

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ 
        mb: 2,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: '#f5f5f5',
        borderRadius: 1,
        p: 1.5
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<ArrowBack />}
            size="small"
            onClick={handleGoBack}
            sx={{ mr: 2 }}
          >
            Quay lại
          </Button>
          <Typography variant="h6" fontWeight={600} sx={{ mr: 2 }}>
            {timetable.name}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {timetable.completedAssignments}/{timetable.totalAssignments} lớp đã xếp
          </Typography>
        </Box>

        <ToggleButtonGroup
          value={viewMode}
          exclusive
          onChange={handleViewModeChange}
          aria-label="view mode"
          size="small"
          sx={{ 
            '& .MuiToggleButton-root': {
              px: 2,
              py: 0.5,
              borderRadius: '4px !important',
              borderColor: '#dddddd',
              '&.Mui-selected': {
                backgroundColor: '#1976D2',
                color: 'white',
                '&:hover': {
                  backgroundColor: '#1565C0',
                }
              }
            }
          }}
        >
          <ToggleButton value="room-based" aria-label="room based view">
            <ViewWeek sx={{ mr: 0.5, fontSize: 18 }} />
            Theo phòng
          </ToggleButton>
          <ToggleButton value="class-based" aria-label="class based view">
            <ViewModule sx={{ mr: 0.5, fontSize: 18 }} />
            Theo lớp
          </ToggleButton>
        </ToggleButtonGroup>
        
      </Box>

      <Paper 
        elevation={2} 
        sx={{ 
          borderRadius: 2, 
          overflow: 'hidden',
          height: 'calc(100vh - 180px)',
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        <Box sx={{ 
          background: 'linear-gradient(90deg, #1976D2, #2196F3)',
          color: 'white',
          p: 1.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <Typography variant="subtitle1" fontWeight={600}>
            {viewMode === 'room-based' ? 'Lịch thi theo phòng' : 'Lịch thi theo lớp'}
          </Typography>
          <Typography variant="caption" sx={{ opacity: 0.9 }}>
            Bắt đầu: {format(new Date(timetable.planStartTime), 'dd/MM/yyyy')} (Tuần {timetable.planStartWeek})
          </Typography>
        </Box>

        <Box sx={{ flex: 1, overflow: 'auto', overflowX: 'auto', overflowY: 'hidden'  }}>
          {viewMode === 'room-based' ? (
            <RoomBasedAssignmentView 
              rooms={examRooms}
              slots={slots}
              assignments={examTimetableAssignments}
            />
          ) : (
            <ClassBasedAssignmentView 
            rooms={examRooms}
            slots={slots}
            assignments={examTimetableAssignments}
          />
          )}
        </Box>
      </Paper>
    </Container>
  );
};

export default ExamTimeTableView;
