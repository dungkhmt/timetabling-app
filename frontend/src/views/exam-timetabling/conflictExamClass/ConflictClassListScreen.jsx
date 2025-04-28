import React, { useState, useEffect, useMemo } from 'react';
import {
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Typography,
} from "@mui/material";
import { Add, Delete } from "@mui/icons-material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import ConflictDetailDialog from './components/ConflictDetailDialog';
import AddConflictPairDialog from './components/AddConflictPairDialog';

import { useExamPlanData } from "services/useExamPlanData";
import { useExamClassConflictData } from "services/useExamClassConflictData";

export default function ConflictClassListScreen() {
  const [selectedExamPlan, setSelectedExamPlan] = useState(null);
  const [selectedConflict, setSelectedConflict] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [selectedRows, setSelectedRows] = useState([]);
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
  
  const { 
    examClassConflicts: conflictData, 
    isLoading, 
    error, 
    createConflict, 
    deleteConflicts
  } = useExamClassConflictData(selectedExamPlan?.id);

  const { examPlans } = useExamPlanData();

  useEffect(() => {
    if (examPlans.length > 0 && !selectedExamPlan) {
      setSelectedExamPlan(examPlans[0]);
    }
  }, [examPlans, selectedExamPlan]);

  const handleSelectExamPlan = (event, examPlan) => {
    setSelectedExamPlan(examPlan);
    setSelectedRows([]);
  };

  const handleRowClick = (params) => {
    const conflictRow = params.row;
    setSelectedConflict(conflictRow);
    setIsDetailModalOpen(true);
  };

  const handleCloseDetailModal = () => {
    setIsDetailModalOpen(false);
    setSelectedConflict(null);
  };

  const handleDeleteClick = () => {
    if (selectedRows.length > 0) {
      setIsDeleteConfirmOpen(true);
    }
  };

  const handleConfirmDelete = async () => {
    try {
      await deleteConflicts(selectedRows);
      setSnackbar({
        open: true,
        message: 'Xóa xung đột thành công',
        severity: 'success'
      });
      setIsDeleteConfirmOpen(false);
      setSelectedRows([]);
    } catch (error) {
      setSnackbar({
        open: true,
        message: 'Lỗi khi xóa xung đột: ' + (error.message || 'Đã xảy ra lỗi'),
        severity: 'error'
      });
    }
  };

  const handleCancelDelete = () => {
    setIsDeleteConfirmOpen(false);
  };

  const handleAddClick = () => {
    setIsAddModalOpen(true);
  };

  const handleCloseAddModal = () => {
    setIsAddModalOpen(false);
  };

  const handleAddConflict = async (conflictData) => {
    try {
      await createConflict({
        examTimetablingClassId1: conflictData.classId1,
        examTimetablingClassId2: conflictData.classId2,
      });
      setIsAddModalOpen(false);
    } catch (error) {
    }
  };

  const columns = [
    { 
      field: 'examClassId1', 
      headerName: 'Mã lớp thi 1', 
      width: 120,
      renderCell: (params) => (
        <Box >
          {params.value}
        </Box>
      )
    },
    { 
      field: 'classId1', 
      headerName: 'Mã lớp học 1', 
      width: 120,
      renderCell: (params) => (
        <Box >
          {params.value}
        </Box>
      )
    },
    { 
      field: 'courseId1', 
      headerName: 'Mã học phần 1', 
      width: 120,
      renderCell: (params) => (
        <Box >
          {params.value}
        </Box>
      )
    },
    { 
      field: 'description1', 
      headerName: 'Ghi chú 1', 
      width: 300,
      renderCell: (params) => (
        <Box >
          {params.value}
        </Box>
      )
    },
    
    { 
      field: 'examClassId2', 
      headerName: 'Mã lớp thi 2', 
      width: 120,
      renderCell: (params) => (
        <Box >
          {params.value}
        </Box>
      )
    },
    { 
      field: 'classId2', 
      headerName: 'Mã lớp học 2', 
      width: 120,
      renderCell: (params) => (
        <Box >
          {params.value}
        </Box>
      )
    },
    { 
      field: 'courseId2', 
      headerName: 'Mã học phần 2', 
      width: 120,
      renderCell: (params) => (
        <Box>
          {params.value}
        </Box>
      )
    },
    { 
      field: 'description2', 
      headerName: 'Ghi chú 2', 
      width: 300,
      renderCell: (params) => (
        <Box>
          {params.value}
        </Box>
      )
    },
  ];

  const rows = useMemo(() => {
    if (!conflictData?.conflicts || !conflictData?.examClasses) {
      return [];
    }
    
    return conflictData.conflicts.map(conflict => {
      const class1 = conflictData.examClasses.find(c => c.id === conflict.classId1) || {};
      const class2 = conflictData.examClasses.find(c => c.id === conflict.classId2) || {};
      
      return {
        id: conflict.conflictId,
        conflict: conflict,
        examClassId1: class1.examClassId || "",
        classId1: class1.classId || "",
        courseId1: class1.courseId || "",
        description1: class1.description || "",
        examClassId2: class2.examClassId || "",
        classId2: class2.classId || "",
        courseId2: class2.courseId || "",
        description2: class2.description || "",
      };
    });
  }, [conflictData]);

  function DataGridTitle() {
    return (
      <Box
        sx={{
          width: "100%",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          pt: 2,
        }}
      >
        <Typography
          variant="h4"
          sx={{
            fontWeight: 700,
            color: '#1976d2',
            position: 'relative',
          }}
        >
          Danh Sách Lớp Xung Đột
        </Typography>
      </Box>
    );
  }

  return (
    <div style={{ height: 600, width: "100%" }}>
      {isLoading && (
        <CircularProgress
          style={{ position: "absolute", top: "50%", left: "50%" }}
        />
      )}
      
      {/* Title */}
      <DataGridTitle />
      
      {/* Control Panel */}
      <Box sx={{ px: 2, pb: 2, display: "flex", flexDirection: "column" }}>
        {/* ExamPlan selection */}
        <Box sx={{ display: "flex", justifyContent: "flex-end", mb: 2 }}>
          <Autocomplete
            options={examPlans}
            getOptionLabel={(option) => option.name}
            style={{ width: 230 }}
            value={selectedExamPlan}
            onChange={handleSelectExamPlan}
            renderInput={(params) => (
              <TextField {...params} label="Chọn kế hoạch thi" variant="outlined" size="small" />
            )}
          />
        </Box>
        
        {/* Action buttons */}
        <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 2, mb: 2 }}>
          <Button
            variant="contained"
            color="error"
            onClick={handleDeleteClick}
            size="small"
            startIcon={<Delete />} 
            disabled={selectedRows.length === 0 || isLoading || !selectedExamPlan}
          >
            Xóa xung đột
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleAddClick}
            disabled={isLoading || !selectedExamPlan}
            startIcon={<Add />}
            size="small"
          >
            Thêm xung đột
          </Button>
        </Box>
      </Box>
      
      {/* DataGrid */}
      <DataGrid
        rows={rows}
        columns={columns}
        pageSizeOptions={[10, 20, 50, 100]}
        onRowClick={handleRowClick}
        initialState={{
          pagination: { paginationModel: { pageSize: 10 } },
        }}
        checkboxSelection
        slots={{ toolbar: GridToolbar }}
        slotProps={{
          toolbar: {
            showQuickFilter: true,
            quickFilterProps: { debounceMs: 500 },
            printOptions: { disableToolbarButton: true },
            csvOptions: { disableToolbarButton: true },
            disableColumnFilter: true,
            disableDensitySelector: true,
            disableColumnSelector: true,
          },
        }}
        disableRowSelectionOnClick
        onRowSelectionModelChange={(ids) => setSelectedRows(ids)}
        sx={{
          '& .MuiDataGrid-columnHeaders': {
            backgroundColor: '#5495e8',
            color: '#fff',
            fontSize: '15px',
            fontWeight: 'bold',
          },
          '& .MuiDataGrid-row:nth-of-type(even)': {
            backgroundColor: '#f9f9f9',
          },
          '& .MuiDataGrid-columnHeader': {
            '&:focus': {
              outline: 'none',
            },
          },
          '& .MuiDataGrid-columnHeaderTitle': {
            fontWeight: 'bold',
          },
        }}
      />

      {/* Detail Dialog */}
      <ConflictDetailDialog
        open={isDetailModalOpen}
        onClose={handleCloseDetailModal}
        conflictPair={selectedConflict?.conflict}
        examClasses={conflictData?.examClasses || []}
      />
      
      {/* Add Conflict Dialog */}
      <AddConflictPairDialog
        open={isAddModalOpen}
        onClose={handleCloseAddModal}
        onSubmit={handleAddConflict}
        examClasses={conflictData?.examClasses || []}
        examPlanId={selectedExamPlan?.id}
      />
      
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={isDeleteConfirmOpen}
        onClose={handleCancelDelete}
      >
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          <Typography>
            Bạn có chắc chắn muốn xóa các xung đột đã chọn không?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete} color="primary">
            Hủy
          </Button>
          <Button onClick={handleConfirmDelete} color="error" variant="contained" autoFocus>
            Xóa
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
