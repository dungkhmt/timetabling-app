import { useEffect, useState } from "react"
import { useExamPlanData } from "services/useExamPlanData"
import { COLUMNS } from "./utils/gridColumns"
import {
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  TextField,
  Tooltip,
  Typography
} from "@mui/material"
import { Add, Delete, Download, HelpOutline, Upload } from "@mui/icons-material"
import { DataGrid, GridToolbar } from "@mui/x-data-grid"
import { useExamClassData } from "services/useExamClassData"
import EditExamClassModal from './utils/EditExamClassModal'
import AddExamClassModal from "./utils/AddExamClassModal"
import UploadExamClassesModal from "./utils/UploadExamClassesModal" // Import the new modal
import localText from "./utils/LocalText"
import { useExamCourseData } from "services/useExamCourseData"
import { useExamClassGroupData } from "services/useExamClassGroupData"
import { useExamFacultyData } from "services/useExamFacultyData"
import { toast } from "react-toastify"

export default function ExamClassListPage() {
  const [selectedExamPlan, setSelectedExamPlan] = useState(null)
  const [successDialogOpen, setSuccessDialogOpen] = useState(false)
  const [conflictDialogOpen, setConflictDialogOpen] = useState(false)
  const [conflictList, setConflictList] = useState([])
  const [selectedRow, setSelectedRow] = useState(null)
  const [selectedRows, setSelectedRows] = useState([])
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false) // New state for upload modal
  const [editFormData, setEditFormData] = useState({
    examClassId: '',
    classId: '',
    courseId: '',
    courseName: '',
    description: '',
    groupId: '',
    numberOfStudents: '',
    school: '',
    period: '',
    managementCode: '',
    id: '',
    examPlanId: '',
  })

  let {
    examClasses,
    isLoading: isLoadingClasses,
    importExcel,
    exportClasses,
    exportConflicts,
    isExportingClasses,
    isExportingConflicts,
    deleteExamClasses,
    isClearing,
    updateExamClass,
    createExamClass,
    downloadSample,
    isImporting,
  } = useExamClassData(selectedExamPlan?.id)

  let {
    examCourses,
    isLoading: isLoadingCourses,
  } = useExamCourseData()

  let {
    examClassGroups
  } = useExamClassGroupData()

  let {
    examFaculties,
  } = useExamFacultyData()

  const managementCodes = [
    'Elitech',
    'CT Chuẩn',
    'CT Tài năng',
  ]

  const { examPlans } = useExamPlanData();

  useEffect(() => {
    if (examPlans && examPlans.length > 0 && !selectedExamPlan) {
      setSelectedExamPlan(examPlans[0]);
    }
  }, [examPlans, selectedExamPlan]);

  const handleOpenUploadModal = () => {
    setIsUploadModalOpen(true);
  }

  const handleCloseUploadModal = () => {
    setIsUploadModalOpen(false);
  }

  const handleUploadSubmit = async (uploadData) => {
    if (uploadData.file && selectedExamPlan) {
      const formData = new FormData();
      formData.append("file", uploadData.file);
      formData.append("groupId", uploadData.groupId); 
      formData.append("groupName", uploadData.groupName); 

      console.log("Form data:", formData.get("file"), formData.get("groupId"));
      try {
        const result = await importExcel(formData);
        handleCloseUploadModal();
        if (result.data.length === 0) {
          setSuccessDialogOpen(true);
        } else {
          setConflictList(result.data.map(examClass => examClass.id));
          setConflictDialogOpen(true);
        }
      } catch (error) {
        console.error("Error uploading file", error);
        toast.error("Có lỗi xảy ra khi tải lên danh sách lớp");
        handleCloseUploadModal();
      }
    }
  }

  const handleSelectExamPlan = (event, examPlan) => {
    setSelectedExamPlan(examPlan);
    setSelectedRows([]);
  }

  const handleDialogClose = () => {
    setSuccessDialogOpen(false)
    setConflictDialogOpen(false)
    // window.location.reload()
  }

  const handleDownloadConflictList = async () => {
    try {
      await exportConflicts(conflictList)
      setSuccessDialogOpen(false)
    } catch (error) {
      console.error("Error exporting conflicts:", error)
    }
  }

  const handleExportExamClasses = async () => {
    try {
      await exportClasses(selectedRows)
    } catch (error) {
      console.error("Error exporting conflicts:", error)
    }
  }

  const handleRowClick = (params) => {
    setSelectedRow(params.row)
    setEditFormData({
      id: params.row.id || '',
      examClassId: params.row.examClassId || '',
      classId: params.row.classId || '',
      courseId: params.row.courseId || '',
      courseName: params.row.courseName || '',
      description: params.row.description || '',
      groupId: params.row.groupId || '',
      numberOfStudents: params.row.numberOfStudents || '',
      school: params.row.school || '',
      period: params.row.period || '',
      managementCode: params.row.managementCode || '',
      examPlanId: params.row.examPlanId || '',
    })
    setIsEditModalOpen(true)
  }

  const handleCloseEditModal = () => {
    setIsEditModalOpen(false)
    setSelectedRow(null)
    setEditFormData({
      id: '',
      examClassId: '',
      classId: '',
      courseId: '',
      courseName: '',
      description: '',
      groupId: '',
      numberOfStudents: '',
      school: '',
      period: '',
      managementCode: '',
      examPlanId: '',
    })
  }

  const handleFormChange = (event) => {
    const { name, value } = event.target
    setEditFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmitEdit = async () => {
    await updateExamClass({
      ...editFormData,
      school: editFormData.school,
    })
    handleCloseEditModal()
  }

  const handleDeleteClick = () => {
    setIsDeleteConfirmOpen(true)
  }

  const handleConfirmDelete = async () => {
    try {
      setIsDeleteConfirmOpen(false)
      await deleteExamClasses(selectedRows)
      setSelectedRows([])
    } catch (error) {
      console.error("Error deleting classes:", error)
      setIsDeleteConfirmOpen(false)
    }
  }

  const handleCancelDelete = () => {
    setIsDeleteConfirmOpen(false)
  }

  const handleAddClick = () => {
    setIsAddModalOpen(true)
  }

  const handleCloseAddModal = () => {
    setIsAddModalOpen(false)
  }

  const handleAddSubmit = async (formData) => {
    try {
      setIsAddModalOpen(false) 

      await createExamClass({
        ...formData,
        school: formData.school.name,
      })
    } catch (error) {
      console.error("Error adding exam class:", error)
      setIsAddModalOpen(false) 
    }
  }

  const handleDownloadSample = () => {
    try {
      downloadSample()
    } catch (error) {
      console.error("Error downloading sample file:", error)
    }
  }

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
          Danh Sách Lớp Thi
        </Typography>
      </Box>
    )
  }

  return (
    <div style={{ height: 600, width: "100%" }}>
      {(isLoadingClasses || isClearing || isExportingConflicts || isExportingClasses || isImporting) && (
        <CircularProgress
          style={{ position: "absolute", top: "50%", left: "50%" }}
        />
      )}
      
      {/* Title */}
      <DataGridTitle />
      
      {/* Control Panel */}
      <Box sx={{ px: 2, pb: 2 }}>
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
        <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 2 }}>
          <Button
            variant="contained"
            color="error"
            onClick={handleDeleteClick}
            size="small"
            startIcon={<Delete />} 
            disabled={selectedRows.length === 0 || isClearing || isImporting || isExportingClasses || isExportingConflicts || !selectedExamPlan}
          >
            Xóa lớp
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleAddClick}
            disabled={isClearing || isImporting || isExportingClasses || isExportingConflicts || !selectedExamPlan}
            startIcon={<Add />}
            size="small"
          >
            Thêm lớp
          </Button>

          <Button
            onClick={handleExportExamClasses}
            color="success"
            variant="contained"
            startIcon={<Download />}
            size="small"
            disabled={selectedRows.length === 0 || isClearing || isImporting || isExportingClasses || isExportingConflicts || !selectedExamPlan}
          >
            Tải xuống DS lớp
          </Button>
          
          {/* Updated Upload Button */}
          <Button
            color="success"
            variant="contained"
            startIcon={<Upload />}
            size="small"
            onClick={handleOpenUploadModal}
            disabled={!selectedExamPlan || isClearing || isImporting || isExportingClasses || isExportingConflicts}
          >
            Tải lên DS lớp
          </Button>
        </Box>
      </Box>

      {/* DataGrid */}
      <DataGrid
        localeText={localText}
        autoHeight
        rows={examClasses}
        columns={COLUMNS}
        pageSizeOptions={[10, 20, 50, 100]}
        onRowClick={handleRowClick}
        initialState={{
          pagination: { paginationModel: { pageSize: 10 } },
        }}
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
        checkboxSelection
        disableRowSelectionOnClick
        onRowSelectionModelChange={(ids) => setSelectedRows(ids)}
        sx={{
          '& .MuiDataGrid-columnHeaders': {
            backgroundColor: '#5495e8',  // Theme primary color
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

      {/* Edit Modal */}
      <EditExamClassModal
        open={isEditModalOpen}
        onClose={handleCloseEditModal}
        formData={editFormData}
        onChange={handleFormChange}
        onSubmit={handleSubmitEdit}
        examCourses={examCourses}
        examClassGroups={examClassGroups}
        schools={examFaculties}
        managementCodes={managementCodes}
      />

      {/* Add Modal */}
      <AddExamClassModal
        open={isAddModalOpen}
        onClose={handleCloseAddModal}
        examCourses={examCourses}
        examClassGroups={examClassGroups}
        schools={examFaculties}
        managementCodes={managementCodes}
        onSubmit={(editFormData) => {
          handleAddSubmit({
            ...editFormData,
            examPlanId: selectedExamPlan.id
          })
        }}
      />
      
      {/* New Upload Modal */}
      <UploadExamClassesModal
        open={isUploadModalOpen}
        onClose={handleCloseUploadModal}
        onSubmit={handleUploadSubmit}
        examClassGroups={examClassGroups}
        isUploading={isImporting}
        handleDownloadSample={handleDownloadSample}
      />
      
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={isDeleteConfirmOpen}
        onClose={handleCancelDelete}
      >
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          <Typography>
            Bạn có chắc chắn muốn xóa các lớp đã chọn không?
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

      {/* Success/Conflict Dialog */}
      <Dialog
        open={successDialogOpen || conflictDialogOpen}
        onClose={handleDialogClose}
      >
        <DialogTitle>
          {successDialogOpen
            ? "Tải lên danh sách thành công"
            : "Danh sách lớp bị trùng"}
        </DialogTitle>
        <DialogContent>
          {conflictDialogOpen ? (
            <ul>
              {conflictList.map((conflict) => (
                <li key={conflict.id}>{conflict.moduleName}</li>
              ))}
            </ul>
          ) : null}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDialogClose} color="primary" autoFocus>
            OK
          </Button>
          {conflictDialogOpen && (
            <Button onClick={handleDownloadConflictList} color="primary">
              Tải xuống
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </div>
  )
}
