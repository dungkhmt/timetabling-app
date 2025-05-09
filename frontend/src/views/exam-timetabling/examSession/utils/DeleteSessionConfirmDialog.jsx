import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box
} from '@mui/material';
import { Warning as WarningIcon } from '@mui/icons-material';

const DeleteSessionConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  sessionName, 
  isMultiple = false,
  isLoading = false 
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm">
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <WarningIcon color="error" />
        <Typography variant="h6">Xác nhận xóa</Typography>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ p: 1 }}>
          {isMultiple ? (
            <Typography>
              Bạn có chắc chắn muốn xóa các kíp thi đã chọn không?
            </Typography>
          ) : (
            <Typography>
              Bạn có chắc chắn muốn xóa kíp thi "<strong>{sessionName}</strong>" không?
            </Typography>
          )}
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Hành động này không thể hoàn tác.
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button 
          onClick={onClose} 
          color="inherit" 
          disabled={isLoading}
        >
          Hủy
        </Button>
        <Button 
          onClick={onConfirm} 
          color="error" 
          variant="contained" 
          autoFocus
          disabled={isLoading}
        >
          {isLoading ? 'Đang xóa...' : 'Xóa'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeleteSessionConfirmDialog;
