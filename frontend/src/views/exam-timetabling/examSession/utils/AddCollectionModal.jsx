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

const AddCollectionModal = ({ open, onClose, onSubmit }) => {
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  const handleNameChange = (event) => {
    setName(event.target.value);
    if (error) setError('');
  };

  const handleSubmit = () => {
    // Validate
    if (!name.trim()) {
      setError('Vui lòng nhập tên bộ kíp thi');
      return;
    }

    // Submit form
    onSubmit({ 
      name: name.trim() 
    });

    // Reset form
    handleClose();
  };

  const handleClose = () => {
    setName('');
    setError('');
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Typography variant="h6">Thêm bộ kíp thi mới</Typography>
      </DialogTitle>
      <Divider />
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          <TextField
            autoFocus
            fullWidth
            label="Tên bộ kíp thi"
            value={name}
            onChange={handleNameChange}
            error={!!error}
            helperText={error}
            placeholder="Ví dụ: Bộ kíp thi sáng"
            margin="normal"
            InputLabelProps={{
              shrink: true,
            }}
          />
          <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mt: 2 }}>
            * Tên bộ kíp thi là bắt buộc
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="inherit">Hủy</Button>
        <Button 
          onClick={handleSubmit} 
          color="primary" 
          variant="contained"
        >
          Thêm
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddCollectionModal;
