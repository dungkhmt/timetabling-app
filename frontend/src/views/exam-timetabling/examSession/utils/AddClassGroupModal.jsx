import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
  Divider,
  Chip,
  Paper,
  Alert
} from '@mui/material';
import { Add as AddIcon, Close as CloseIcon } from '@mui/icons-material';

const AddClassGroupModal = ({ open, onClose, onSubmit, existingGroups = [] }) => {
  const [inputValue, setInputValue] = useState('');
  const [error, setError] = useState('');
  const [newGroups, setNewGroups] = useState([]);
  const [existingNames, setExistingNames] = useState(new Set());

  useEffect(() => {
    if (existingGroups && existingGroups.length) {
      const names = new Set(existingGroups.map(group => group.name.toLowerCase()));
      setExistingNames(names);
    }
  }, [existingGroups]);

  const handleInputChange = (event) => {
    setInputValue(event.target.value);
    if (error) setError('');
  };

  const handleKeyDown = (event) => {
    if (event.key === 'Enter' && inputValue.trim()) {
      event.preventDefault();
      addGroupName();
    }
  };

  const addGroupName = () => {
    const trimmedValue = inputValue.trim();
    if (!trimmedValue) return;

    if (existingNames.has(trimmedValue.toLowerCase())) {
      setError(`Nhóm "${trimmedValue}" đã tồn tại`);
      return;
    }

    if (newGroups.some(name => name.toLowerCase() === trimmedValue.toLowerCase())) {
      setError(`Nhóm "${trimmedValue}" đã được thêm vào danh sách`);
      return;
    }

    setNewGroups([...newGroups, trimmedValue]);
    setInputValue(''); 
  };

  const handleDeleteGroup = (index) => {
    const updatedGroups = [...newGroups];
    updatedGroups.splice(index, 1);
    setNewGroups(updatedGroups);
  };

  const handleSubmit = () => {
    if (newGroups.length === 0) {
      setError('Vui lòng thêm ít nhất một nhóm lớp');
      return;
    }

    onSubmit(newGroups.map(name => ({ name })));
    handleClose();
  };

  const handleClose = () => {
    setInputValue('');
    setNewGroups([]);
    setError('');
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Typography variant="h6">Thêm nhóm lớp mới</Typography>
      </DialogTitle>
      <Divider />
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          <TextField
            autoFocus
            fullWidth
            label="Tên nhóm lớp"
            value={inputValue}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            error={!!error}
            helperText={error}
            placeholder="Nhập tên nhóm và nhấn Enter để thêm"
            margin="normal"
            InputProps={{
              endAdornment: (
                <Button 
                  disabled={!inputValue.trim()}
                  onClick={addGroupName}
                  size="small"
                  variant="contained"
                  startIcon={<AddIcon />}
                >
                  Thêm
                </Button>
              ),
            }}
            InputLabelProps={{
              shrink: true,
            }}
          />
          
          <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mt: 2, mb: 1 }}>
            * Nhập tên nhóm lớp và nhấn Enter để thêm vào danh sách
          </Typography>
          
          {newGroups.length > 0 && (
            <Paper variant="outlined" sx={{ p: 2, mt: 2, maxHeight: '200px', overflow: 'auto' }}>
              <Typography variant="subtitle2" gutterBottom>
                Danh sách nhóm lớp mới ({newGroups.length})
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
                {newGroups.map((name, index) => (
                  <Chip
                    key={index}
                    label={name}
                    onDelete={() => handleDeleteGroup(index)}
                    deleteIcon={<CloseIcon />}
                    sx={{ margin: '4px' }}
                  />
                ))}
              </Box>
            </Paper>
          )}
          
          {newGroups.length === 0 && (
            <Alert severity="info" sx={{ mt: 2 }}>
              Chưa có nhóm lớp nào được thêm. Vui lòng nhập tên và nhấn Enter.
            </Alert>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="inherit">Hủy</Button>
        <Button 
          onClick={handleSubmit} 
          color="primary" 
          variant="contained"
          disabled={newGroups.length === 0}
        >
          Thêm {newGroups.length > 0 ? `(${newGroups.length})` : ''}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddClassGroupModal;
