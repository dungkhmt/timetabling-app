import { useRef, useState } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Container,
  CircularProgress,
  Grid,
  Paper,
  Typography,
} from '@mui/material';
import {
  Edit,
  Delete,
  Visibility,
  AssignmentTurnedIn,
  School,
  CheckCircle,
  Error
} from '@mui/icons-material';
import ClassesTable from './components/ClassAssignTable';
import { format } from 'date-fns';
import DeleteConfirmModal from './components/DeleteExamTimetableModal'
import UpdateTimetableModal from './components/UpdateTimeTableModal'
import ConflictDialog from './components/ConflictSaveDialog'
import { useExamTimetableData } from 'services/useExamTimetableData'
import { time } from 'echarts'

const useOptionsData = () => {
  return {
    rooms: [
      { id: "R101", name: "P.101" },
      { id: "R102", name: "P.102" },
      { id: "R103", name: "P.103" },
      { id: "R104", name: "P.104" }
    ],
  };
};


const TimetableDetailPage = () => {
  const { id } = useParams();
  const history = useHistory();
  const {
    timetable,
    isLoadingDetail,
    errorDetail,
    deleteExamTimetable,
    updateExamTimetable,
  } = useExamTimetableData(null, id);

  const optionsData = useOptionsData();
  
  const [isRenameDialogOpen, setIsRenameDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isConflictModalOpen, setIsConflictModalOpen] = useState(false);
  const [conflicts, setConflicts] = useState([]);

  // Use ref to access ClassesTable methods
  const classesTableRef = useRef();

  const handleSaveAndCheck = () => {
    // Get the current assignments and check for conflicts
    const assignmentChanges = classesTableRef.current.getAssignmentChanges();
    const foundConflicts = classesTableRef.current.checkForAssignmentConflicts();

    if (foundConflicts.length > 0) {
      // We have conflicts, show the dialog
      setConflicts(foundConflicts);
      setIsConflictModalOpen(true);
    } else {
      // No conflicts, save directly
      saveAssignments(assignmentChanges);
    }
  };

  const handleContinueSaveWithConflicts = () => {
    setIsConflictModalOpen(false);
    const assignmentChanges = classesTableRef.current.getAssignmentChanges();
    saveAssignments(assignmentChanges, conflicts);
  };

  const saveAssignments = async (assignments, conflictsToLog = []) => {
    try {
      // Call your API to save assignments
     
      
      // Handle success
    } catch (error) {
      // Handle error
      console.error('Error saving assignments:', error);
    }
  };
  
  if (isLoadingDetail) {
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

  const handleRenameTimetable = async (name) => {
    const payload = {
      name,
      id: timetable.id,
    };
    await updateExamTimetable(payload);
    setIsRenameDialogOpen(false);
    window.location.reload();
  };

  const handleDeleteTimetable = () => {
    setIsDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async (timetableId, examPlanId) => {
    await deleteExamTimetable(timetableId);
    setIsDeleteDialogOpen(false);
    history.push(`/exam-time-tabling/exam-plan/${examPlanId}`);
    window.location.reload();
  };

  const handleViewTimetable = () => {
    history.push(`/exam-timetable/${id}/view`);
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Timetable Information Card */}
      <Card sx={{ 
        mb: 4, 
        boxShadow: 2, 
        borderRadius: 2,
        overflow: 'hidden',
        border: '1px solid #ddd',
      }}>
        {/* Header with Gradient */}
        <Box sx={{ 
          background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
          color: 'white',
          p: 2,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <Typography variant="h5" fontWeight={600}>
            {timetable.name}
          </Typography>
          <Box>
            <Button 
              variant="contained" 
              color="secondary"
              startIcon={<Visibility />} 
              size="small"
              sx={{ 
                mr: 1, 
                backgroundColor: 'secondary.main', 
                '&:hover': { backgroundColor: '#FFB74D' }
              }}
              onClick={handleViewTimetable}
            >
              Xem
            </Button>

            <Button 
              variant="contained" 
              color="info" 
              startIcon={<Edit />} 
              size="small"
              sx={{ 
                mr: 1, 
                backgroundColor: 'info.main', 
                '&:hover': { backgroundColor: '#4FC3F7' }
              }}
              onClick={() => setIsRenameDialogOpen(true)}
            >
              Đổi tên
            </Button>

            <Button 
              variant="contained" 
              color="error" 
              startIcon={<Delete />} 
              size="small"
              sx={{ 
                backgroundColor: 'error.main', 
                '&:hover': { backgroundColor: '#E57373' }
              }}
              onClick={handleDeleteTimetable}
            >
              Xóa
            </Button>
          </Box>
        </Box>

        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Typography variant="body1" fontWeight={600} color="text.primary">
                  📅 Tạo ngày: {format(new Date(timetable.createdAt), 'dd/MM/yyyy HH:mm')}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body1" fontWeight={500} color="text.primary">
                📅 Bắt đầu: {format(new Date(timetable.planStartTime), 'dd/MM/yyyy')}(Tuần {timetable.planStartWeek}) đến {format(new Date(timetable.planEndTime), 'dd/MM/yyyy')}
                </Typography>
              </Box>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', height: '100%' }}>
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center',
                  mr: 4
                }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <School sx={{ color: 'primary.main', mr: 1 }} />
                    <Typography variant="h6" fontWeight={600} color="primary.main">
                      {timetable.completedAssignments}/{timetable.assignments.length}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    Lớp đã xếp
                  </Typography>
                </Box>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Classes Table */}
      <Paper 
        elevation={2} 
        sx={{ 
          borderRadius: 2, 
          overflow: 'hidden',
          height: 'calc(100vh - 100px)', // Adjustable based on your layout
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          background: 'linear-gradient(90deg, #1976D2, #2196F3)',
          color: 'white',
          p: 2
        }}>
          <Typography variant="h6" fontWeight={600}>
            Danh sách lớp thi
          </Typography>
          
          <Button
            variant="contained"
            color="success"
            startIcon={<AssignmentTurnedIn />}
            onClick={handleSaveAndCheck}
            sx={{ 
              bgcolor: 'success.main',
              '&:hover': { bgcolor: '#2E7D32' }
            }}
          >
            Lưu và kiểm tra xung đột
          </Button>
        </Box>

        <Box sx={{ flex: 1, overflow: 'hidden' }}>
          <ClassesTable 
            ref={classesTableRef}
            classesData={timetable.assignments} 
            isLoading={isLoadingDetail}
            rooms={optionsData.rooms}
            weeks={timetable.weeks}
            dates={timetable.dates}
            slots={timetable.slots}
          />

          <ConflictDialog
            open={isConflictModalOpen}
            conflicts={conflicts}
            onClose={() => setIsConflictModalOpen(false)}
            onContinue={handleContinueSaveWithConflicts}
          />  
        </Box>
      </Paper>

      <DeleteConfirmModal
        open={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={() => handleConfirmDelete(id, timetable.examPlanId)}
        timetableName={timetable.name}
        isDeleting={false}
      ></DeleteConfirmModal>

      <UpdateTimetableModal
        open={isRenameDialogOpen}
        onClose={() => setIsRenameDialogOpen(false)}
        tiemtableId={timetable.id}
        onUpdateTimetable={handleRenameTimetable}
        timetableName={timetable.name}
        timetableId={timetable.id}
      ></UpdateTimetableModal>
    </Container>
  );
};

export default TimetableDetailPage;
