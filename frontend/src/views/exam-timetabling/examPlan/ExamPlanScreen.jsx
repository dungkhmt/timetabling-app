import { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { useExamPlanData } from 'services/useExamPlanData';
import {
  Box,
  Button,
  Container,
  Paper,
  TextField,
  Typography,
  InputAdornment,
  CircularProgress,
  Stack,
  TablePagination
} from '@mui/material';
import {
  Add,
  Search,
  SentimentDissatisfied
} from '@mui/icons-material';
import ExamPlanRow from './utils/ExamPlanRow';
import AddExamPlanModal from './utils/AddExamPlanModal';
import { useExamSemesterData } from 'services/useExamSemesterData'

const ExamPlanListPage = () => {
  const history = useHistory();
  const { examPlans, isLoading, createExamPlan } = useExamPlanData();
  const { examSemesters } = useExamSemesterData();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  
  const handleSearchChange = (e) => {
    setSearch(e.target.value);
    setPage(1);
  };

  const handleCreatePlan = () => {
    setIsAddModalOpen(true);
  };

  const handleCloseAddModal = () => {
    setIsAddModalOpen(false);
  };

  const handleSaveNewPlan = async (formData) => {
    try {
      setIsSaving(true);
      const result = await createExamPlan(formData);
      setIsSaving(false);
      setIsAddModalOpen(false);

      if (result && result.data.id) {
        history.push(`/exam-time-tabling/exam-plan/${result.data.id}`);
      }
    } catch (error) {
      console.error("Error creating exam plan:", error);
      setIsSaving(false);
    }
  };

  const handlePlanClick = (planId) => {
    history.push(`/exam-time-tabling/exam-plan/${planId}`);
  };

  const filteredPlans = examPlans ? examPlans.filter(plan => 
    plan.name.toLowerCase().includes(search.toLowerCase())
  ) : [];

  const totalPages = Math.ceil(filteredPlans.length / rowsPerPage);
  const startIndex = (page - 1) * rowsPerPage;
  const endIndex = startIndex + rowsPerPage;
  const paginatedPlans = filteredPlans.slice(startIndex, endIndex);

  useEffect(() => {
    if (page > totalPages && totalPages > 0) {
      setPage(1);
    }
  }, [filteredPlans.length, page, totalPages]);

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ textAlign: 'center', mb: 5 }}>
        <Typography variant="h4" fontWeight={700} color="primary">
          Kế Hoạch Thi
        </Typography>
      </Box>

      <Paper elevation={1} sx={{ mb: 4 }}>
        <Box
          sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            background: 'linear-gradient(90deg, #1976D2, #2196F3)',
            color: 'white',
            padding: '16px 24px',
            borderRadius: '4px',
            overflow: 'hidden'
          }}
        >
          <TextField
            placeholder="Tìm kiếm theo tên..."
            value={search}
            onChange={handleSearchChange}
            variant="outlined"
            size="small"
            sx={{ 
              width: '50%',
              '& .MuiOutlinedInput-root': {
                '& fieldset': {
                  borderColor: '#e0e0e0',
                },
                '&:hover fieldset': {
                  borderColor: '#bdbdbd',
                },
              },
              backgroundColor: 'white',
              borderRadius: '4px'
            }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search />
                </InputAdornment>
              ),
            }}
          />

          <Button
            variant="contained"
            sx={{ 
              backgroundColor: '#9c27b0', // Purple color
              '&:hover': {
                backgroundColor: '#7b1fa2' // Darker purple on hover
              },
              textTransform: 'uppercase',
              borderRadius: '4px',
              px: 2
            }}
            startIcon={<Add />}
            onClick={handleCreatePlan}
          >
            Tạo kế hoạch mới
          </Button>
        </Box>
      </Paper>

      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        filteredPlans.length > 0 ? (
          <>
            <Paper elevation={1}>
              {paginatedPlans.map((plan, index) => (
                <ExamPlanRow
                  key={plan.id}
                  plan={plan} 
                  index={index}
                  onClick={handlePlanClick}
                  isLast={index === paginatedPlans.length - 1}
                />
              ))}
            </Paper>
            
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
              <Stack spacing={2}>
                <TablePagination
                  component="div"
                  count={filteredPlans.length}
                  page={page - 1}
                  onPageChange={(e, newPage) => setPage(newPage + 1)}
                  rowsPerPage={rowsPerPage}
                  onRowsPerPageChange={(e) => {
                    setRowsPerPage(parseInt(e.target.value, 10));
                    setPage(1);
                  }}
                  labelRowsPerPage="Hiển thị:"
                />
              </Stack>
            </Box>
          </>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4 }}>
            <SentimentDissatisfied sx={{ fontSize: 80, color: 'gray', mb: 2 }} />
            <Typography variant="h6" color="textSecondary">Không tìm thấy kế hoạch thi</Typography>
            <Button variant="contained" sx={{ mt: 2 }} startIcon={<Add />} onClick={handleCreatePlan}>
              Tạo kế hoạch mới
            </Button>
          </Box>
        )
      )}

      {/* Add the modal */}
      <AddExamPlanModal
        open={isAddModalOpen}
        semesters={examSemesters}
        onClose={handleCloseAddModal}
        onSave={handleSaveNewPlan}
        isSaving={isSaving}
      />
    </Container>
  );
};

export default ExamPlanListPage;
