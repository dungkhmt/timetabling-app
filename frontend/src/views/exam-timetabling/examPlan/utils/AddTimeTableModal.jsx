import { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  IconButton,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Typography,
  Divider,
  Chip,
  FormHelperText
} from '@mui/material';
import { Close, AccessTime } from '@mui/icons-material';
import { useExamSessionData } from 'services/useExamSessionData';

const AddTimetableModal = ({ open, onClose, planId, onCreateTimetable }) => {
  const { examSessions, isLoading } = useExamSessionData();
  const [name, setName] = useState('');
  const [selectedCollection, setSelectedCollection] = useState('');
  const [error, setError] = useState('');
  const [collectionError, setCollectionError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const history = useHistory();

  // Instead of managing a separate state, use the data directly from the hook
  const sessionCollections = examSessions || [];

  const handleChange = (e) => {
    setName(e.target.value);
    if (error) setError('');
  };

  const handleCollectionChange = (e) => {
    setSelectedCollection(e.target.value);
    if (collectionError) setCollectionError('');
  };

  const handleSubmit = async () => {
    // Validate inputs
    let hasError = false;

    if (!name.trim()) {
      setError('Tên lịch thi không được để trống');
      hasError = true;
    }

    if (!selectedCollection) {
      setCollectionError('Vui lòng chọn bộ ca thi');
      hasError = true;
    }

    if (hasError) return;

    try {
      setIsSubmitting(true);

      // Call API to create timetable with selected collection
      const result = await onCreateTimetable({
        name,
        examPlanId: planId,
        examTimetableSessionCollectionId: selectedCollection // Add the selected collection ID
      });

      setIsSubmitting(false);

      // Reset form and close modal
      setName('');
      setSelectedCollection('');
      onClose();

      // Redirect to timetable detail page
      if (result && result.id) {
        history.push(`/exam-timetables/${result.id}`);
      }
    } catch (error) {
      setIsSubmitting(false);
      setError('Có lỗi xảy ra khi tạo lịch thi');
      console.error('Error creating timetable:', error);
    }
  };

  const handleClose = () => {
    setName('');
    setSelectedCollection('');
    setError('');
    setCollectionError('');
    onClose();
  };

  // Find the selected collection object
  const selectedCollectionData = sessionCollections.find(
    collection => collection.id === selectedCollection
  );

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle
        sx={{
          pb: 1,
          textAlign: 'center',
          borderBottom: '1px solid #e0e0e0',
          mb: 1,
          position: 'relative'
        }}
      >
        Tạo lịch thi mới
        <IconButton
          onClick={handleClose}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: (theme) => theme.palette.grey[500],
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <TextField
          autoFocus
          margin="dense"
          label="Tên lịch thi"
          fullWidth
          value={name}
          onChange={handleChange}
          error={!!error}
          helperText={error}
          required
          sx={{ mt: 2, mb: 2 }}
        />

        <FormControl fullWidth error={!!collectionError} sx={{ mb: 2 }}>
          <InputLabel id="session-collection-label">Bộ ca thi</InputLabel>
          <Select
            labelId="session-collection-label"
            value={selectedCollection}
            onChange={handleCollectionChange}
            label="Bộ ca thi"
            disabled={isLoading || sessionCollections.length === 0}
          >
            {sessionCollections.map((collection) => (
              <MenuItem key={collection.id} value={collection.id}>
                {collection.name}
              </MenuItem>
            ))}
          </Select>
          {collectionError && <FormHelperText>{collectionError}</FormHelperText>}
        </FormControl>

        {selectedCollectionData && (
          <Box sx={{ mt: 2, bgcolor: '#f5f5f5', p: 2, borderRadius: 1 }}>
            <Typography variant="subtitle2" gutterBottom>
              Danh sách ca thi trong bộ <strong>{selectedCollectionData.name}</strong>:
            </Typography>
            <Divider sx={{ mb: 1 }} />
            
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {selectedCollectionData.sessions.map((session) => (
                <Box 
                  key={session.id} 
                  sx={{ 
                    display: 'flex', 
                    alignItems: 'center',
                    p: 1,
                    borderRadius: 1,
                    bgcolor: 'white'
                  }}
                >
                  <AccessTime fontSize="small" sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="body2">
                    {session.displayName}
                  </Typography>
                </Box>
              ))}
            </Box>
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={handleClose} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="primary"
          disabled={isSubmitting}
          startIcon={isSubmitting ? <CircularProgress size={20} /> : null}
        >
          {isSubmitting ? 'Đang tạo...' : 'Tạo lịch thi'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddTimetableModal;
