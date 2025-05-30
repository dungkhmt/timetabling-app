// EditClassGroupModal.jsx
import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
  Divider
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';

const EditClassGroupModal = ({ open, onClose, formData, onChange, onSubmit, onDelete }) => {
  const [error, setError] = useState('');
  
  const handleNameChange = (event) => {
    onChange({
      target: {
        name: 'name',
        value: event.target.value
      }
    });
    if (error) setError('');
  };

  const handleSubmit = () => {
    // Validate
    if (!formData.name?.trim()) {
      setError('Vui lòng nhập tên nhóm lớp');
      return;
    }

    // Submit form
    onSubmit({
      id: formData.id,
      name: formData.name.trim()
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Typography variant="h6">Chỉnh sửa nhóm lớp</Typography>
      </DialogTitle>
      <Divider />
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          <TextField
            autoFocus
            fullWidth
            label="Tên nhóm lớp"
            name="name"
            value={formData.name || ''}
            onChange={handleNameChange}
            error={!!error}
            helperText={error}
            margin="normal"
            InputLabelProps={{
              shrink: true,
            }}
          />
          <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mt: 2 }}>
            ID: {formData.id || ''}
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="inherit">Hủy</Button>
        <Button 
          onClick={handleSubmit} 
          color="primary" 
          variant="contained"
        >
          Lưu
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditClassGroupModal;
