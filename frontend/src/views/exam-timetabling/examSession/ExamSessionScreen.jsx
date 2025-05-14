import { useEffect, useState } from "react"
import {
  Box,
  Grid,
  Typography
} from "@mui/material"
import { useExamSessionData } from "services/useExamSessionData"
import { useExamClassGroupData } from "services/useExamClassGroupData"
import AddSessionModal from "./utils/AddSessionModal"
import EditSessionModal from "./utils/EditSessionModal"
import DeleteSessionConfirmDialog from "./utils/DeleteSessionConfirmDialog"
import AddCollectionModal from "./utils/AddCollectionModal"
import EditCollectionModal from "./utils/EditCollectionModal"
import DeleteCollectionConfirmDialog from "./utils/DeleteCollectionConfrimDialog"
import ExamSessionTable from "./utils/ExamSessionTable"
import ExamClassGroupTable from "./utils/ExamClassGroupTable"
import AddClassGroupModal from "./utils/AddClassGroupModal"
import DeleteClassGroupConfirmDialog from "./utils/DeleteClassGroupConfirmDialog"
import EditClassGroupModal from "./utils/EditClassGroupModal"
import DeleteMultiClassGroupsConfirmDialog from "./utils/DeleteMultiClassGroupsConfirmDialog"

export default function ExamSessionListPage() {
  const [selectedCollection, setSelectedCollection] = useState(null)
  const [isAddCollectionModalOpen, setIsAddCollectionModalOpen] = useState(false)
  const [isEditCollectionModalOpen, setIsEditCollectionModalOpen] = useState(false)
  const [isDeleteCollectionConfirmOpen, setIsDeleteCollectionConfirmOpen] = useState(false)
  const [collectionFormData, setCollectionFormData] = useState({
    id: '',
    name: ''
  })

  const [selectedRow, setSelectedRow] = useState(null)
  const [isAddSessionModalOpen, setIsAddSessionModalOpen] = useState(false)
  const [isEditSessionModalOpen, setIsEditSessionModalOpen] = useState(false)
  const [isDeleteSessionConfirmOpen, setIsDeleteSessionConfirmOpen] = useState(false)
  const [sessionToDelete, setSessionToDelete] = useState(null)
  const [sessionFormData, setSessionFormData] = useState({
    id: '',
    name: '',
    startTime: '',
    endTime: '',
    displayName: ''
  })

  const [selectedClassGroup, setSelectedClassGroup] = useState(null)
  const [isAddClassGroupModalOpen, setIsAddClassGroupModalOpen] = useState(false)
  const [isEditClassGroupModalOpen, setIsEditClassGroupModalOpen] = useState(false)
  const [isDeleteClassGroupConfirmOpen, setIsDeleteClassGroupConfirmOpen] = useState(false)
  const [isDeleteMultiClassGroupsConfirmOpen, setIsDeleteMultiClassGroupsConfirmOpen] = useState(false)
  const [classGroupsToDelete, setClassGroupsToDelete] = useState([])
  const [classGroupFormData, setClassGroupFormData] = useState({
    id: '',
    name: ''
  })

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

  const {
    examClassGroups,
    isLoading: isLoadingClassGroup,
    createExamClassGroups,
    deleteExamClassGroups,
    updateExamClassGroup,
  } = useExamClassGroupData()

  // // Initialize selected collection
  // useEffect(() => {
  //   if (sessionCollections && sessionCollections.length > 0 && !selectedCollection) {
  //     setSelectedCollection(sessionCollections[0]);
  //   }
  // }, [sessionCollections, selectedCollection]);

  // const currentSessions = selectedCollection ? selectedCollection.sessions : []

  // Collection handlers
  const handleSelectCollection = (event, collection) => {
    setSelectedCollection(collection);
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

  const handleCollectionFormChange = (event) => {
    const { name, value } = event.target;
    setCollectionFormData(prev => ({
      ...prev,
      [name]: value
    }));
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

  const handleClassGroupFormChange = (event) => {
    const { name, value } = event.target;
    setClassGroupFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  const handleSubmitAddClassGroup = async (groupsToAdd) => {
    try {
      await createExamClassGroups({
        groupNames: groupsToAdd.map(group => group.name)
      });
      setIsAddClassGroupModalOpen(false);
    } catch (error) {
      console.error("Error adding class groups:", error);
    }
  };

  const handleSubmitEditClassGroup = async () => {
    try {
      await updateExamClassGroup(classGroupFormData);
      setIsEditClassGroupModalOpen(false);
    } catch (error) {
      console.error("Error updating class group:", error);
    }
  };

  const handleConfirmDeleteClassGroup = async () => {
    try {
      await deleteExamClassGroups(selectedClassGroup.id);
      setIsDeleteClassGroupConfirmOpen(false);
    } catch (error) {
      console.error("Error deleting class group:", error);
      setIsDeleteClassGroupConfirmOpen(false);
    }
  };

  const handleConfirmDeleteMultiClassGroups = async () => {
    try {
      await deleteExamClassGroups(classGroupsToDelete);
      setIsDeleteMultiClassGroupsConfirmOpen(false);
    } catch (error) {
      console.error("Error deleting multiple class groups:", error);
      setIsDeleteMultiClassGroupsConfirmOpen(false);
    }
  };

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
  }

  const handleDeleteSingleSession = (params) => {
    setSessionToDelete(params.row);
    setSelectedRow(params.row);
    setIsDeleteSessionConfirmOpen(true);
  }

  const handleConfirmDeleteSession = async () => {
    try {
      await deleteExamSession(selectedRow.id);
      setIsDeleteSessionConfirmOpen(false);
    } catch (error) {
      setIsDeleteSessionConfirmOpen(false);
    }
  }

  const handleSessionFormChange = (event) => {
    const { name, value } = event.target;
    setSessionFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }

  const handleCloseAddSession = () => setIsAddSessionModalOpen(false);
  const handleCloseEditSession = () => {
    setIsEditSessionModalOpen(false);
    setSelectedRow(null);
  }
  const handleCloseAddCollection = () => setIsAddCollectionModalOpen(false);
  const handleCloseEditCollection = () => setIsEditCollectionModalOpen(false);

  const handleSubmitAddSession = async (formData) => {
    try {
      await createExamSession({
        ...formData,
        startTime: new Date(new Date(formData.startTime).getTime() + 7 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(new Date(formData.endTime).getTime() + 7 * 60 * 60 * 1000).toISOString(),
        examTimetableSessionCollectionId: sessionCollections[0].id
      });
      setSessionFormData({
        id: '',
        name: '',
        startTime: '',
        endTime: '',
        displayName: ''
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
        startTime: new Date(new Date(sessionFormData.startTime).getTime() + 7 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(new Date(sessionFormData.endTime).getTime() + 7 * 60 * 60 * 1000).toISOString(),
        examTimetableSessionCollectionId: sessionCollections[0].id
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

  const handleAddClassGroup = () => {
    setClassGroupFormData({
      id: '',
      name: ''
    });
    setIsAddClassGroupModalOpen(true);
  };

  const handleEditClassGroup = (params) => {
    setSelectedClassGroup(params.row);
    setClassGroupFormData({
      id: params.row.id || '',
      name: params.row.name || '',
    });
    setIsEditClassGroupModalOpen(true);
  };

  const handleDeleteClassGroup = (params) => {
    setSelectedClassGroup(params.row);
    setIsDeleteClassGroupConfirmOpen(true);
  };

  const handleDeleteMultiClassGroups = (selectedIds) => {
    setClassGroupsToDelete(selectedIds);
    setIsDeleteMultiClassGroupsConfirmOpen(true);
  };

  return (
    <Box sx={{ p: 2 }}>
      <Box sx={{ display: "flex", justifyContent: "center", mb: 5, alignItems: "center" }}>
        <Typography variant="h4" sx={{ fontWeight: 700, color: '#1976d2' }}>
          Cài Đặt Lịch Thi
        </Typography>
        
        {/* <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
          <Autocomplete
            options={sessionCollections || []}
            getOptionLabel={(option) => option.name}
            style={{ width: 230 }}
            value={selectedCollection}
            onChange={handleSelectCollection}
            renderInput={(params) => (
              <TextField {...params} label="Chọn bộ kíp thi" variant="outlined" size="small" />
            )}
          />
          
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
              backgroundColor: 'secondary.main', 
              '&:hover': { backgroundColor: '#FFB74D' }
            }}
            onClick={handleEditCollection}
            disabled={!selectedCollection || isLoading}
          >
            Sửa bộ kíp
          </Button>
        </Box> */}
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={7}>
          <ExamClassGroupTable
            classGroups={examClassGroups || []}
            isLoading={isLoadingClassGroup}
            onAddGroup={handleAddClassGroup}
            onEditGroup={handleEditClassGroup}
            onDeleteGroup={handleDeleteClassGroup}
            onDeleteMultiGroups={handleDeleteMultiClassGroups}
          />
        </Grid>

        <Grid item xs={12} md={5}>
          <ExamSessionTable
            sessions={sessionCollections && sessionCollections[0] ? sessionCollections[0].sessions : []}
            isLoading={isLoading}
            onAddSession={handleAddSession}
            onEditSession={handleEditSession}
            onDeleteSession={handleDeleteSingleSession}
            selectedCollection={sessionCollections[0]}
          />
        </Grid>
      </Grid>

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
        onConfirm={handleConfirmDeleteSession}
        sessionName={sessionToDelete?.name || sessionToDelete?.displayName || ''}
        isLoading={isLoading}
      />

      {/* Collection Modals */}
      {/* <AddCollectionModal
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
      /> */}

      <AddClassGroupModal
        open={isAddClassGroupModalOpen}
        onClose={() => setIsAddClassGroupModalOpen(false)}
        onSubmit={handleSubmitAddClassGroup}
      />

      <EditClassGroupModal
        open={isEditClassGroupModalOpen}
        onClose={() => setIsEditClassGroupModalOpen(false)}
        formData={classGroupFormData}
        onChange={handleClassGroupFormChange}
        onSubmit={handleSubmitEditClassGroup}
      />

      <DeleteClassGroupConfirmDialog
        open={isDeleteClassGroupConfirmOpen}
        onClose={() => setIsDeleteClassGroupConfirmOpen(false)}
        onConfirm={handleConfirmDeleteClassGroup}
        classGroupName={selectedClassGroup?.name || ''}
        isLoading={isLoadingClassGroup}
      />

      <DeleteMultiClassGroupsConfirmDialog
        open={isDeleteMultiClassGroupsConfirmOpen}
        onClose={() => setIsDeleteMultiClassGroupsConfirmOpen(false)}
        onConfirm={handleConfirmDeleteMultiClassGroups}
        count={classGroupsToDelete.length}
        isLoading={isLoadingClassGroup}
      />
    </Box>
  );
}
