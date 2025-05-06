import { useState } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useExamPlanData } from 'services/useExamPlanData';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Grid,
  Typography
} from '@mui/material';
import {
  Delete,
  Edit,
  SentimentDissatisfied
} from '@mui/icons-material';
import TimetableList from './utils/TimetableList';
import StatisticsPanel from './utils/StatisticsPanel';
import { format } from 'date-fns';
import EditPlanModal from './utils/UpdateExamPlanModel'
import DeleteConfirmModal from './utils/DeleteExamPlanModal'
import { useExamTimetableData } from 'services/useExamTimetableData'

const ExamPlanDetailPage = () => {
  const history = useHistory();
  const { id } = useParams();
  const { examPlan: {
    examPlan,
    semester
  }, isLoading, deleteExamPlan, updateExamPlan, planStatistics, isLoadingPlanStatistics } = useExamPlanData(id);
  const {
    examTimetables,
    isLoading: isLoadingTimetables,
    createExamTimetable,
  } = useExamTimetableData(id);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!examPlan) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4 }}>
        <SentimentDissatisfied sx={{ fontSize: 80, color: 'gray', mb: 2 }} />
        <Typography variant="h6" color="textSecondary">Không tìm thấy kế hoạch thi</Typography>
        <Button 
          variant="contained" 
          sx={{ mt: 2 }} 
          onClick={() => history.push('/exam-plans')}
        >
          Quay lại danh sách
        </Button>
      </Box>
    );
  }

  const handleEditPlan = () => {
    setIsEditModalOpen(true);
  };
  
  const handleDeletePlan = () => {
    setIsDeleteModalOpen(true);
  };
  
  const handleSavePlan = async (formData) => {
    try {
      setIsSaving(true);
      await updateExamPlan(formData);
      setIsSaving(false);
      setIsEditModalOpen(false);
      window.location.reload();
    } catch (error) {
      console.error('Error updating plan:', error);
      setIsSaving(false);
    }
  };
  
  const handleConfirmDelete = async () => {
    try {
      setIsDeleting(true);
      await deleteExamPlan(id);
      setIsDeleting(false);
      setIsDeleteModalOpen(false);
      history.push('/exam-time-tabling/exam-plan');
    } catch (error) {
      console.error('Error deleting plan:', error);
      setIsDeleting(false);
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Grid container spacing={3} sx={{ minHeight: 'calc(100vh - 200px)' }}> {/* Set a minimum height */}
        <Grid item xs={12} md={8} sx={{ display: 'flex', flexDirection: 'column' }}>
          {/* Plan Information Card */}
          <Card sx={{ 
            mb: 3, 
            boxShadow: 2, 
            borderRadius: 2,
            overflow: 'hidden',
            border: '1px solid #ddd',
          }}>
            <Box sx={{ 
              background: 'linear-gradient(90deg, #1976D2, #2196F3)', 
              color: 'white',
              p: 2,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <Typography variant="h5" fontWeight={600}>
                {examPlan.name}
              </Typography>
              <Box>
              <Button 
                variant="contained" 
                color="secondary"
                startIcon={<Edit />} 
                size="small"
                sx={{ 
                  mr: 1, 
                  backgroundColor: 'secondary.main', 
                  '&:hover': { backgroundColor: '#FFB74D' } // Softer orange on hover
                }}
                onClick={handleEditPlan}
              >
                SỬA
              </Button>

              <Button 
                variant="contained" 
                color="error" 
                startIcon={<Delete />} 
                size="small"
                sx={{ 
                  backgroundColor: 'error.main', 
                  '&:hover': { backgroundColor: '#E57373' } // Softer red on hover
                }}
                onClick={handleDeletePlan}
              >
                XÓA
              </Button>

              </Box>
            </Box>
            <CardContent sx={{ p: 2 }}>
              <Typography variant="body1" fontWeight={600} color="#1976D2" marginBottom={1}>
              📅 Kì {semester.semester} 
              </Typography>
              <Typography variant="body1" fontWeight={600} color="#1976D2" marginBottom={1}>
              📅 {new Date(examPlan.startTime).toLocaleDateString('en-GB')} (Tuần {examPlan.startWeek}) → {new Date(examPlan.endTime).toLocaleDateString('en-GB')}
              </Typography>
              <Typography variant="body1" color="text.secondary">
                Mô tả: {examPlan.description || 'Chưa có mô tả'}
              </Typography>
            </CardContent>
          </Card>


          {/* Timetable List */}
          <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', width: '100%' }}>
            <TimetableList 
              planId={id}
              timetables={examTimetables}
              isLoading={isLoadingTimetables}
              onCreateTimetable={createExamTimetable}
            />
          </Box>
        </Grid>

        {/* Right Column - Statistics Panel */}
        <Grid item xs={12} md={4}>
          <StatisticsPanel 
            planId={id} 
            statistics={planStatistics} 
            isLoading={isLoadingPlanStatistics} 
          />
        </Grid>
      </Grid>

      <EditPlanModal
        open={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onSave={handleSavePlan}
        examPlan={examPlan}
        isSaving={isSaving}
      />

      <DeleteConfirmModal
        open={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleConfirmDelete}
        planName={examPlan?.name}
        isDeleting={isDeleting}
      />
    </Container>
  )
};

export default ExamPlanDetailPage;
