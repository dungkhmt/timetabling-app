import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  CircularProgress
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';

const DeleteClassGroupConfirmDialog = ({ open, onClose, onConfirm, classGroupName, isLoading }) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm">
      <DialogTitle>
        <Typography variant="h6">Xác nhận xóa nhóm lớp</Typography>
      </DialogTitle>
      <DialogContent>
        <Typography>
          Bạn có chắc chắn muốn xóa nhóm lớp "{classGroupName}" không?
        </Typography>
        <Typography variant="caption" color="error" sx={{ display: 'block', mt: 2 }}>
          * Hành động này không thể hoàn tác.
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="inherit" disabled={isLoading}>
          Hủy
        </Button>
        <Button
          onClick={onConfirm}
          color="error"
          variant="contained"
          startIcon={isLoading ? <CircularProgress size={20} color="inherit" /> : <DeleteIcon />}
          disabled={isLoading}
        >
          {isLoading ? 'Đang xóa...' : 'Xóa'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeleteClassGroupConfirmDialog;
