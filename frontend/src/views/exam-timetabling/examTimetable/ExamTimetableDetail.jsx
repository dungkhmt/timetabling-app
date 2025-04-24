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
  Tooltip
} from '@mui/material';
import {
  Edit,
  Delete,
  Visibility,
  AssignmentTurnedIn,
  School,
  Error,
  FileDownload,
  AutoFixHigh, // Added for auto-assign icon
  InsertChart
} from '@mui/icons-material';
import ClassesTable from './components/ClassAssignTable';
import { format } from 'date-fns';
import DeleteConfirmModal from './components/DeleteExamTimetableModal';
import UpdateTimetableModal from './components/UpdateTimeTableModal';
import ConflictDialog from './components/ConflictSaveDialog';
import InvalidAssignmentDialog from './components/InvalidAssignmentDialog';
import AutoAssignConfirmDialog from './components/AutoAssignConfirmDialog'; // Added import
import { validateAssignmentChanges } from './utils/AssignmentValidation';
import { useExamTimetableData } from 'services/useExamTimetableData';
import { useExamRoomData } from 'services/useExamRoomData';
import { useExamTimetableAssignmentData } from 'services/useExamTimetableAssignmentData';

const convertDateFormat = (dateStr) =>{
  const [year, month, day] = dateStr.split('-');
  return `${day}-${month}-${year}`;
}

const TimetableDetailPage = () => {
  const { id } = useParams();
  const history = useHistory();
  const {
    timetable,
    isLoadingDetail,
    deleteExamTimetable,
    updateExamTimetable,
    updateExamTimetableAssignments,
    getAssignmentConflicts,
  } = useExamTimetableData(null, id);

  const {
    examRooms,
  } = useExamRoomData();

  const {
    examTimetableAssignments,
    isLoading,
    exportTimetable,
    error,
    autoAssign,
    algorithms,
    isLoadingAlgorithm,
  } = useExamTimetableAssignmentData(id);

  const [isRenameDialogOpen, setIsRenameDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isConflictModalOpen, setIsConflictModalOpen] = useState(false);
  const [isInvalidModalOpen, setIsInvalidModalOpen] = useState(false);
  const [conflicts, setConflicts] = useState([]);
  const [invalidAssignments, setInvalidAssignments] = useState([]);
  const [selectedAssignments, setSelectedAssignments] = useState([]); // Track selected assignments
  const [isExporting, setIsExporting] = useState(false); // Track export status
  
  // Added state variables for auto-assign functionality
  const [isAutoAssignDialogOpen, setIsAutoAssignDialogOpen] = useState(false);
  const [isAutoAssigning, setIsAutoAssigning] = useState(false);
  const [alreadyAssignedClasses, setAlreadyAssignedClasses] = useState([]);

  const classesTableRef = useRef();

  const handleSelectionChange = (selectedIds) => {
    setSelectedAssignments(selectedIds);
  };

  const handleExport = async () => {
    if (selectedAssignments.length === 0) {
      return;
    }

    try {
      setIsExporting(true);
      await exportTimetable(selectedAssignments);
      setIsExporting(false);
    } catch (error) {
      console.error('Error exporting assignments:', error);
      setIsExporting(false);
    }
  };

  // Added function to handle auto-assign button click
  const handleAutoAssign = () => {
    if (selectedAssignments.length === 0) {
      return;
    }

    // Check if any selected assignments are already assigned
    const assignedClasses = examTimetableAssignments.filter(assignment => 
      selectedAssignments.includes(assignment.id) && 
      (assignment.room || assignment.week || assignment.date || assignment.slot)
    );

    setAlreadyAssignedClasses(assignedClasses);
    setIsAutoAssignDialogOpen(true);
  };

  const performAutoAssign = async (data) => {
    const {
      dates: selectedDates,
      timeLimit,
      algorithm,
    } = data;
    try {
      setIsAutoAssigning(true);
      const examClassAssignmentLookup = new Map(
        examTimetableAssignments.map(assignment => [assignment.id, assignment.examTimetableClassId])
      );
      const result = await autoAssign({
        examTimetableId: timetable.id,
        classIds: selectedAssignments.map(id => examClassAssignmentLookup.get(id)),
        examDates: selectedDates.map(date => convertDateFormat(date)),
        algorithm,
        timeLimit,
      });
      console.log(result);
      setIsAutoAssigning(false);
      setIsAutoAssignDialogOpen(false);
    } catch (error) {
      console.error('Error auto-assigning:', error);
      setIsAutoAssigning(false);
      setIsAutoAssignDialogOpen(false);
    }
  };

  const handleSaveAndCheck = async () => {
    const assignmentChanges = classesTableRef.current.getAssignmentChanges();
    
    const validationResult = validateAssignmentChanges(
      classesTableRef.current.getRawAssignmentChanges(), 
      examTimetableAssignments
    );
    
    if (!validationResult.isValid) {
      setInvalidAssignments(validationResult.invalidAssignments);
      setIsInvalidModalOpen(true);
      return;
    }

    const {
      data: foundConflicts,
    } = await getAssignmentConflicts(
      assignmentChanges.map((assignment) => ({
        timetableId: timetable.id,
        ...assignment,
      }))
    );

    if (foundConflicts.length > 0) {
      setConflicts(foundConflicts);
      setIsConflictModalOpen(true);
    } else {
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
      await updateExamTimetableAssignments(assignments, conflictsToLog);
    } catch (error) {
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

  const handleViewTimetable = () => {
    history.push(`/exam-time-tabling/exam-timetable/view/${id}`);
  };

  const handleViewTimetableStatistics = () => {
    history.push(`/exam-time-tabling/exam-timetable/statistic/${id}`);
  };

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

  const handleCloseInvalidModal = () => {
    setIsInvalidModalOpen(false);
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
              color="secondary"
              startIcon={<InsertChart />} 
              size="small"
              sx={{ 
                mr: 1, 
                backgroundColor: 'success.main', 
                '&:hover': { backgroundColor: '#FFB74D' }
              }}
              onClick={handleViewTimetableStatistics}
            >
              Thống kê
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
                mr: 1,
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
                      {timetable.completedAssignments}/{examTimetableAssignments.length || 100}
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
          height: 'calc(100vh - 100px)',
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
          
          <Box sx={{ display: 'flex', gap: 2 }}>
            {/* Auto Assign Button - Added */}
            <Tooltip title={selectedAssignments.length === 0 ? "Chọn ít nhất một lớp để phân công tự động" : "Phân công tự động cho lớp đã chọn"}>
              <span>
                <Button
                  variant="contained"
                  color="warning"
                  startIcon={<AutoFixHigh />}
                  onClick={handleAutoAssign}
                  disabled={selectedAssignments.length === 0 || isAutoAssigning}
                  sx={{ 
                    bgcolor: 'warning.light',
                    '&:hover': { bgcolor: '#FFA726' }
                  }}
                >
                  {isAutoAssigning ? 'Đang xử lý...' : `Phân công tự động (${selectedAssignments.length})`}
                </Button>
              </span>
            </Tooltip>

            <Tooltip title={selectedAssignments.length === 0 ? "Chọn ít nhất một lớp để xuất" : "Xuất danh sách đã chọn"}>
              <span>
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<FileDownload />}
                  onClick={handleExport}
                  disabled={selectedAssignments.length === 0 || isExporting}
                  sx={{ 
                    bgcolor: 'success.light',
                    '&:hover': { bgcolor: '#66BB6A' }
                  }}
                >
                  {isExporting ? 'Đang xuất...' : `Xuất Excel (${selectedAssignments.length})`}
                </Button>
              </span>
            </Tooltip>
            
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
        </Box>

        <Box sx={{ flex: 1, overflow: 'hidden' }}>
          <ClassesTable 
            ref={classesTableRef}
            classesData={examTimetableAssignments} 
            isLoading={isLoading}
            rooms={examRooms}
            weeks={timetable.weeks}
            dates={timetable.dates}
            slots={timetable.slots}
            onSelectionChange={handleSelectionChange}
          />

          {/* Conflict Dialog */}
          <ConflictDialog
            open={isConflictModalOpen}
            conflicts={conflicts}
            onClose={() => setIsConflictModalOpen(false)}
            onContinue={handleContinueSaveWithConflicts}
          />
          
          {/* Invalid Assignment Dialog */}
          <InvalidAssignmentDialog
            open={isInvalidModalOpen}
            invalidAssignments={invalidAssignments}
            onClose={handleCloseInvalidModal}
          />
          
          {/* Auto Assign Confirm Dialog */}
          <AutoAssignConfirmDialog
            open={isAutoAssignDialogOpen}
            onClose={() => setIsAutoAssignDialogOpen(false)}
            onConfirm={performAutoAssign}
            assignedClasses={alreadyAssignedClasses}
            isProcessing={isAutoAssigning}
            timetable={timetable}
            algorithms={algorithms.map(algorithmName => ({
              value: algorithmName,
              label: algorithmName
            }))}
            isLoadingAlgorithms={isLoadingAlgorithm}
          />
        </Box>
      </Paper>

      <DeleteConfirmModal
        open={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={() => handleConfirmDelete(id, timetable.examPlanId)}
        timetableName={timetable.name}
        isDeleting={false}
      />

      <UpdateTimetableModal
        open={isRenameDialogOpen}
        onClose={() => setIsRenameDialogOpen(false)}
        tiemtableId={timetable.id}
        onUpdateTimetable={handleRenameTimetable}
        timetableName={timetable.name}
        timetableId={timetable.id}
      />
    </Container>
  );
};

export default TimetableDetailPage;
