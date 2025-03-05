import { useEffect, useState } from "react"
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
  Typography
} from "@mui/material"
import { DataGrid } from "@mui/x-data-grid"
import { useExamSessionData } from "services/useExamSessionData"
import localText from "./utils/LocalText"
import { Add, Delete, Edit,  } from "@mui/icons-material"
import AddSessionModal from "./utils/AddSessionModal"
import EditSessionModal from "./utils/EditSessionModal"
import DeleteSessionConfirmDialog from "./utils/DeleteSessionConfirmDialog"
import AddCollectionModal from "./utils/AddCollectionModal"
import EditCollectionModal from "./utils/EditCollectionModal"
import DeleteCollectionConfirmDialog from "./utils/DeleteCollectionConfrimDialog"


export default function ExamSessionListPage() {
  const [selectedCollection, setSelectedCollection] = useState(null)
  const [selectedRow, setSelectedRow] = useState(null)
  const [selectedRows, setSelectedRows] = useState([])
  const [isAddSessionModalOpen, setIsAddSessionModalOpen] = useState(false)
  const [isEditSessionModalOpen, setIsEditSessionModalOpen] = useState(false)
  const [isAddCollectionModalOpen, setIsAddCollectionModalOpen] = useState(false)
  const [isEditCollectionModalOpen, setIsEditCollectionModalOpen] = useState(false)
  const [isDeleteSessionConfirmOpen, setIsDeleteSessionConfirmOpen] = useState(false)
  const [isDeleteCollectionConfirmOpen, setIsDeleteCollectionConfirmOpen] = useState(false)
  const [sessionToDelete, setSessionToDelete] = useState(null);
  

  // Form data state
  const [sessionFormData, setSessionFormData] = useState({
    id: '',
    name: '',
    startTime: '',
    endTime: '',
    displayName: ''
  })

  const [collectionFormData, setCollectionFormData] = useState({
    id: '',
    name: ''
  })

  // Fetch data using the hook
  const {
    sessionCollections,
    isLoading,
    createCollectionSession,
    updateCollectionSession,
    deleteCollectionSession,
    createExamSession,
    updateExamSession,
    deleteExamSession,
  } = useExamSessionData()

  // Set initial selected collection
  useEffect(() => {
    if (sessionCollections && sessionCollections.length > 0 && !selectedCollection) {
      setSelectedCollection(sessionCollections[0]);
    }
  }, [sessionCollections, selectedCollection]);

  // Get current sessions from selected collection
  const currentSessions = selectedCollection ? selectedCollection.sessions : []

  // Collection handlers
  const handleSelectCollection = (event, collection) => {
    setSelectedCollection(collection);
    // Clear selected rows when changing collection
    setSelectedRows([]);
  }

  const handleAddCollection = () => {
    setCollectionFormData({
      id: '',
      name: ''
    });
    setIsAddCollectionModalOpen(true);
  }

  const handleEditCollection = () => {
    if (selectedCollection) {
      setCollectionFormData({
        id: selectedCollection.id,
        name: selectedCollection.name
      });
      setIsEditCollectionModalOpen(true);
    }
  }

  const handleConfirmDeleteCollection = async () => {
    try {
      await deleteCollectionSession(selectedCollection.id);
      setIsDeleteCollectionConfirmOpen(false);
      setSelectedCollection(null);
    } catch (error) {
      console.error("Error deleting collection:", error);
      setIsDeleteCollectionConfirmOpen(false);
    }
  }

  // Session handlers
  const handleAddSession = () => {
    setSessionFormData({
      id: '',
      name: '',
      startTime: '',
      endTime: '',
      displayName: ''
    });
    setIsAddSessionModalOpen(true);
  }

  const handleSessionFormChange = (event) => {
    const { name, value } = event.target;
    setSessionFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }

  const handleCollectionFormChange = (event) => {
    const { name, value } = event.target;
    setCollectionFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }

  // Modal close handlers
  const handleCloseAddSession = () => {
    setIsAddSessionModalOpen(false);
  }

  const handleCloseEditSession = () => {
    setIsEditSessionModalOpen(false);
    setSelectedRow(null);
  }

  const handleCloseAddCollection = () => {
    setIsAddCollectionModalOpen(false);
  }

  const handleCloseEditCollection = () => {
    setIsEditCollectionModalOpen(false);
  }

  // Submit handlers
  const handleSubmitAddSession = async (formData) => {
    try {
      await createExamSession({
        ...formData,
        examTimetableSessionCollectionId: selectedCollection.id
      });
      setIsAddSessionModalOpen(false);
    } catch (error) {
      console.error("Error adding session:", error);
    }
  }

  const handleSubmitEditSession = async () => {
    try {
      await updateExamSession({
        ...sessionFormData,
        examTimetableSessionCollectionId: selectedCollection.id
      });
      setIsEditSessionModalOpen(false);
    } catch (error) {
      console.error("Error updating session:", error);
    }
  }

  const handleSubmitAddCollection = async (formData) => {
    try {
      await createCollectionSession(formData);
      setIsAddCollectionModalOpen(false);
    } catch (error) {
      console.error("Error adding collection:", error);
    }
  }

  const handleSubmitEditCollection = async () => {
    try {
      await updateCollectionSession(collectionFormData);
      setIsEditCollectionModalOpen(false);
    } catch (error) {
      console.error("Error updating collection:", error);
    }
  }

  const handleEditSession = (params) => {
    setSelectedRow(params.row);
    setSessionFormData({
      id: params.row.id || '',
      name: params.row.name || '',
      startTime: params.row.startTime || '',
      endTime: params.row.endTime || '',
      displayName: params.row.displayName || ''
    });
    setIsEditSessionModalOpen(true);
  };
  
  const handleDeleteSingleSession = (params) => {
    setSessionToDelete(params.row);
    setSelectedRow(params.row);
    setIsDeleteSessionConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    try {
      await deleteExamSession(selectedRow.id);
      setIsDeleteSessionConfirmOpen(false);
    } catch (error) {
      console.error("Error deleting session:", error);
      setIsDeleteSessionConfirmOpen(false);
    }
  };

  // Custom DataGrid toolbar component
  function DataGridToolbar() {
    return (
      <Box sx={{ px: 2, pb: 2 }}>
        {/* Collection selection and actions */}
        <Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
          <Box sx={{ display: "flex", gap: 2 }}>
            
          </Box>
          <Autocomplete
            options={sessionCollections}
            getOptionLabel={(option) => option.name}
            style={{ width: 230 }}
            value={selectedCollection}
            onChange={handleSelectCollection}
            renderInput={(params) => (
              <TextField {...params} label="Chọn bộ kíp thi" variant="outlined" size="small" />
            )}
          />
        </Box>

        {/* Session actions */}
        <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 2 }}>
          <Button
            variant="contained"
            color="primary"
            size="small"
            onClick={handleAddSession}
            disabled={!selectedCollection || isLoading}
            startIcon={<Add />}
          >
            Thêm kíp thi
          </Button>
          
          <Button
              variant="contained"
              color="success"
              size="small"
              startIcon={<Add />}
              sx={{ 
                bgcolor: 'success.light',
                '&:hover': { bgcolor: '#66BB6A' }
              }}
              onClick={handleAddCollection}
              disabled={isLoading}
            >
              Thêm bộ kíp
            </Button>
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
              onClick={handleEditCollection}
              disabled={!selectedCollection || isLoading}
            >
              Sửa bộ kíp
            </Button>
          
        </Box>

        {/* Confirmation dialogs */}
        <Dialog
          open={isDeleteCollectionConfirmOpen}
          onClose={() => setIsDeleteCollectionConfirmOpen(false)}
        >
          <DialogTitle>Xác nhận xóa</DialogTitle>
          <DialogContent>
            <Typography>
              Bạn có chắc chắn muốn xóa bộ kíp thi "{selectedCollection?.name}" không?
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setIsDeleteCollectionConfirmOpen(false)} color="primary">
              Hủy
            </Button>
            <Button onClick={handleConfirmDeleteCollection} color="error" variant="contained" autoFocus>
              Xóa
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    );
  }

  // DataGrid title component
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
          Danh Sách Kíp Thi
        </Typography>
      </Box>
    );
  }

  // Inside your component, before returning JSX
  const actionColumn = {
    headerName: "Thao tác",
    field: "actions",
    width: 120,
    sortable: false,
    filterable: false,
    headerAlign: 'center',
    align: 'center',
    renderCell: (params) => {
      return (
        <Box sx={{ display: 'flex', gap: 1 }}>
          <IconButton 
            size="small" 
            color="primary"
            onClick={(event) => {
              event.stopPropagation();
              handleEditSession(params);
            }}
          >
            <Edit fontSize="small" />
          </IconButton>
          <IconButton 
            size="small" 
            color="error"
            onClick={(event) => {
              event.stopPropagation();
              // Pass the full row data to your handler
              handleDeleteSingleSession(params);
            }}
          >
            <Delete fontSize="small" />
          </IconButton>
        </Box>
      );
    }
  };
  
  return (
    <div style={{ height: 600, width: "100%" }}>
      {isLoading && (
        <CircularProgress
          style={{ position: "absolute", top: "50%", left: "50%" }}
        />
      )}
      <DataGrid
        onCellClick={(params) => {
          // This prevents row clicks from triggering when clicking action buttons
          if (params.field === 'actions') {
            return;
          }
        }}
        componentsEvents={{
          editSession: handleEditSession,
          deleteSession: handleDeleteSingleSession
        }}
        localeText={localText}
        components={{
          Toolbar: () => (
            <>
              <DataGridTitle />
              <DataGridToolbar />
            </>
          ),
        }}
        autoHeight
        rows={currentSessions}
        columns={[...COLUMNS, actionColumn]}
        pageSizeOptions={[10, 20, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 10 } },
        }}
        disableRowSelectionOnClick
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

      <AddSessionModal
        open={isAddSessionModalOpen}
        onClose={handleCloseAddSession}
        formData={sessionFormData}
        onChange={handleSessionFormChange}
        onSubmit={handleSubmitAddSession}
      />

      <EditSessionModal
        open={isEditSessionModalOpen}
        onClose={handleCloseEditSession}
        formData={sessionFormData}
        onChange={handleSessionFormChange}
        onSubmit={handleSubmitEditSession}
      />

      <DeleteSessionConfirmDialog
        open={isDeleteSessionConfirmOpen}
        onClose={() => {
          setIsDeleteSessionConfirmOpen(false);
          setSessionToDelete(null);
        }}
        onConfirm={handleConfirmDelete}
        sessionName={sessionToDelete?.name || sessionToDelete?.displayName || ''}
        isLoading={isLoading}
      />

      <AddCollectionModal
        open={isAddCollectionModalOpen}
        onClose={handleCloseAddCollection}
        onSubmit={handleSubmitAddCollection}
      />

      <EditCollectionModal
        open={isEditCollectionModalOpen}
        onClose={handleCloseEditCollection}
        formData={collectionFormData}
        onChange={handleCollectionFormChange}
        onSubmit={handleSubmitEditCollection}
        onDelete={() => {
          setIsDeleteCollectionConfirmOpen(true);
          setIsEditCollectionModalOpen(false);
        }}
      />

      <DeleteCollectionConfirmDialog
        open={isDeleteCollectionConfirmOpen}
        onClose={() => setIsDeleteCollectionConfirmOpen(false)}
        onConfirm={handleConfirmDeleteCollection}
        collectionName={selectedCollection?.name || ''}
        isLoading={isLoading}
      />
    </div>
  );
}
