// DeleteMultiClassGroupsConfirmDialog.jsx
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

const DeleteMultiClassGroupsConfirmDialog = ({ open, onClose, onConfirm, count, isLoading }) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm">
      <DialogTitle>
        <Typography variant="h6">Xác nhận xóa nhiều nhóm lớp</Typography>
      </DialogTitle>
      <DialogContent>
        <Typography>
          Bạn có chắc chắn muốn xóa {count} nhóm lớp đã chọn không?
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
          {isLoading ? 'Đang xóa...' : `Xóa (${count})`}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeleteMultiClassGroupsConfirmDialog;
