import { useCallback, useEffect, useRef, useState } from 'react';
import { useParams, useHistory, Prompt } from 'react-router-dom';
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
  AutoFixHigh, 
  InsertChart,
  PublishedWithChanges,
  Redo
} from '@mui/icons-material';
import ClassesTable from './components/ClassAssignTable';
import { format } from 'date-fns';
import DeleteConfirmModal from './components/DeleteExamTimetableModal';
import UpdateTimetableModal from './components/UpdateTimeTableModal';
import ConflictDialog from './components/ConflictSaveDialog';
import InvalidAssignmentDialog from './components/InvalidAssignmentDialog';
import AutoAssignConfirmDialog from './components/AutoAssignConfirmDialog'; 
import ConflictCheckDialog from './components/ConflictCheckDialog'; 
import { validateAssignmentChanges } from './utils/AssignmentValidation';
import { useExamTimetableData } from 'services/useExamTimetableData';
import { useExamRoomData } from 'services/useExamRoomData';
import { useExamTimetableAssignmentData } from 'services/useExamTimetableAssignmentData';
import UnassignConfirmDialog from './components/UnassignConfirmDialog'
import UnsavedChangesWarningModal from './components/UnsavedChangesWarningModal'

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
    unassignAssignments,
    autoAssign,
    algorithms,
    isLoadingAlgorithm,
    checkConflictForFullExamTimetableAssignment,
  } = useExamTimetableAssignmentData(id);

  const [isRenameDialogOpen, setIsRenameDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isConflictModalOpen, setIsConflictModalOpen] = useState(false);
  const [isInvalidModalOpen, setIsInvalidModalOpen] = useState(false);
  const [conflicts, setConflicts] = useState([]);
  const [invalidAssignments, setInvalidAssignments] = useState([]);
  const [selectedAssignments, setSelectedAssignments] = useState([]); 
  const [isExporting, setIsExporting] = useState(false); 
  
  const [isCheckingConflicts, setIsCheckingConflicts] = useState(false);
  const [isConflictCheckDialogOpen, setIsConflictCheckDialogOpen] = useState(false);
  const [checkConflicts, setCheckConflicts] = useState([]);
  
  const [isAutoAssignDialogOpen, setIsAutoAssignDialogOpen] = useState(false);
  const [isAutoAssigning, setIsAutoAssigning] = useState(false);
  const [alreadyAssignedClasses, setAlreadyAssignedClasses] = useState([]);

  const [isUnassignDialogOpen, setIsUnassignDialogOpen] = useState(false);
  const [isUnassigning, setIsUnassigning] = useState(false);
  const [assignmentsToUnassign, setAssignmentsToUnassign] = useState([]);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [isNavigatingAway, setIsNavigatingAway] = useState(false);
  const [pendingNavigation, setPendingNavigation] = useState(null);
  const [isWarningModalOpen, setIsWarningModalOpen] = useState(false);

  const classesTableRef = useRef();

  useEffect(() => {
    if (classesTableRef.current) {
      const unsavedChangesCount = Object.keys(classesTableRef.current.getRawAssignmentChanges() || {}).length;
      setHasUnsavedChanges(unsavedChangesCount > 0);
    }
  }, [examTimetableAssignments]); 

  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (hasUnsavedChanges) {
        e.preventDefault();
        e.returnValue = '';
        return ''; 
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [hasUnsavedChanges]);

  const checkUnsavedChanges = useCallback(() => {
    if (classesTableRef.current) {
      const changes = classesTableRef.current.getRawAssignmentChanges() || {};
      return Object.keys(changes).length;
    }
    return 0;
  }, []);

  const handleNavigation = useCallback((nextLocation) => {
    const changesCount = checkUnsavedChanges();
    
    if (changesCount > 0) {
      setPendingNavigation(nextLocation);
      setIsWarningModalOpen(true);
      return false;
    }
    
    return true;
  }, [checkUnsavedChanges]);

  const handleSaveAndCheck = useCallback(async () => {
    const assignmentChanges = classesTableRef.current.getAssignmentChanges();
    
    const validationResult = validateAssignmentChanges(
      classesTableRef.current.getRawAssignmentChanges(), 
      examTimetableAssignments
    );
    
    if (!validationResult.isValid) {
      setInvalidAssignments(validationResult.invalidAssignments);
      setIsInvalidModalOpen(true);
      return Promise.reject(new Error('Invalid assignments'));
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
      return Promise.reject(new Error('Conflicts found'));
    } else {
      return saveAssignments(assignmentChanges);
    }
  });


  const handleNavigationDecision = useCallback((shouldSave) => {
    setIsWarningModalOpen(false);

    
    if (shouldSave) {
      handleSaveAndCheck().then(() => {
        if (pendingNavigation) {
          history.push(pendingNavigation.pathname);
        }
      });
    } else {
      if (pendingNavigation) {
        setIsNavigatingAway(true);
        history.push(pendingNavigation.pathname);
      }
    }
    
    setPendingNavigation(null);
  }, [pendingNavigation, history, handleSaveAndCheck]);

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

  const handleAutoAssign = () => {
    if (selectedAssignments.length === 0) {
      return;
    }

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
      setIsAutoAssigning(false);
      setIsAutoAssignDialogOpen(false);
    } catch (error) {
      console.error('Error auto-assigning:', error);
      setIsAutoAssigning(false);
      setIsAutoAssignDialogOpen(false);
    }
  };

  const handleUnassign = () => {
    if (selectedAssignments.length === 0) {
      return;
    }
  
    const assignedClasses = examTimetableAssignments.filter(assignment => 
      selectedAssignments.includes(assignment.id) && 
      (assignment.room || assignment.week || assignment.date || assignment.slot)
    );
  
    if (assignedClasses.length === 0) {
      return;
    }
  
    setAssignmentsToUnassign(assignedClasses);
    setIsUnassignDialogOpen(true);
  };
  
  const performUnassign = async () => {
    try {
      setIsUnassigning(true);
      await unassignAssignments(selectedAssignments);
      setIsUnassigning(false);
      setIsUnassignDialogOpen(false);
    } catch (error) {
      console.error('Error unassigning assignments:', error);
      setIsUnassigning(false);
      setIsUnassignDialogOpen(false);
    }
  };

  
  const handleCheck = async () => {
    try {
      setIsCheckingConflicts(true);
      const {
        data: result,
      } = await checkConflictForFullExamTimetableAssignment(timetable.id);
      
      setIsCheckingConflicts(false);
      
      setCheckConflicts(result);
      setIsConflictCheckDialogOpen(true);
    } catch (error) {
      console.error('Error checking conflicts:', error);
      setIsCheckingConflicts(false);
    }
  };

  const handleCloseConflictCheckDialog = () => {
    setIsConflictCheckDialogOpen(false);
  };

  const handleContinueSaveWithConflicts = () => {
    setIsConflictModalOpen(false);
    const assignmentChanges = classesTableRef.current.getAssignmentChanges();
    return saveAssignments(assignmentChanges, conflicts);
  };

  const saveAssignments = async (assignments, conflictsToLog = []) => {
    try {
      await updateExamTimetableAssignments(assignments, conflictsToLog);
      setHasUnsavedChanges(false);
      if (classesTableRef.current) {
        classesTableRef.current.resetAssignmentChanges();
      }
      return Promise.resolve();
    } catch (error) {
      console.error('Error saving assignments:', error);
      return Promise.reject(error);
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

  const handleBackToTimeplan = () => {
    history.push(`/exam-time-tabling/exam-plan/${timetable.examPlanId}`);
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
    window.location.replace(`/exam-time-tabling/exam-plan/${examPlanId}`);
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
              startIcon={<Redo />} 
              size="small"
              sx={{ 
                mr: 1, 
                backgroundColor: 'warning.main', 
                '&:hover': { backgroundColor: '#FFB74D' }
              }}
              onClick={handleBackToTimeplan}
            >
              Về kế hoạch
            </Button>

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
          
          <Box sx={{ display: 'flex', gap: 1 }}>
            {/* Auto Assign Button */}
            <Tooltip title={selectedAssignments.length === 0 ? "Chọn ít nhất một lớp để xếp tự động" : "Xếp tự động cho lớp đã chọn"}>
              <span>
                <Button
                  variant="contained"
                  color="warning"
                  size="small"
                  startIcon={<AutoFixHigh />}
                  onClick={handleAutoAssign}
                  disabled={selectedAssignments.length === 0 || isAutoAssigning}
                  sx={{ 
                    bgcolor: 'warning.light',
                    '&:hover': { bgcolor: '#FFA726' }
                  }}
                >
                  {isAutoAssigning ? 'Đang xử lý...' : `Xếp tự động (${selectedAssignments.length})`}
                </Button>
              </span>
            </Tooltip>

            <Tooltip title={selectedAssignments.length === 0 ? "Chọn ít nhất một lớp để hủy phân công" : "Hủy phân công cho lớp đã chọn"}>
              <span>
                <Button
                  variant="contained"
                  size="small"
                  color="error"
                  startIcon={<Delete />}
                  onClick={handleUnassign}
                  disabled={selectedAssignments.length === 0 || isUnassigning}
                  sx={{ 
                    bgcolor: 'error.light',
                    '&:hover': { bgcolor: '#EF5350' }
                  }}
                >
                  {isUnassigning ? 'Đang xử lý...' : `Hủy (${selectedAssignments.length})`}
                </Button>
              </span>
            </Tooltip>

            <Tooltip title={selectedAssignments.length === 0 ? "Chọn ít nhất một lớp để xuất" : "Xuất danh sách đã chọn"}>
              <span>
                <Button
                  variant="contained"
                  color="success"
                  size="small"
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
              color="warning"
              size="small"
              startIcon={<PublishedWithChanges />}
              onClick={handleCheck}
              disabled={isCheckingConflicts}
              sx={{ 
                bgcolor: 'warning.main',
                '&:hover': { bgcolor: '#FFC107' }
              }}
            >
              {isCheckingConflicts ? 'Đang kiểm tra...' : 'Kiểm tra'}
            </Button>

            <Button
              size="small"
              variant="contained"
              color="success"
              startIcon={<AssignmentTurnedIn />}
              onClick={handleSaveAndCheck}
              sx={{ 
                bgcolor: 'success.main',
                '&:hover': { bgcolor: '#2E7D32' }
              }}
            >
              Lưu
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
            onChangeStatusUpdate={setHasUnsavedChanges} 
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

          <UnassignConfirmDialog
            open={isUnassignDialogOpen}
            onClose={() => setIsUnassignDialogOpen(false)}
            onConfirm={performUnassign}
            assignmentsToUnassign={assignmentsToUnassign}
            isProcessing={isUnassigning}
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
          
          {/* Conflict Check Dialog */}
          <ConflictCheckDialog
            open={isConflictCheckDialogOpen}
            conflicts={checkConflicts}
            onClose={handleCloseConflictCheckDialog}
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

      <Prompt
        when={hasUnsavedChanges && !isNavigatingAway}
        message={handleNavigation}
      />
      
      {/* Unsaved Changes Warning Modal */}
      <UnsavedChangesWarningModal
        open={isWarningModalOpen}
        onClose={() => setIsWarningModalOpen(false)}
        onConfirm={handleNavigationDecision}
        changesCount={checkUnsavedChanges()}
      />
    </Container>
  );
};

export default TimetableDetailPage;
