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

const DeleteCollectionConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  collectionName,
  isLoading = false 
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm">
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <WarningIcon color="error" />
        <Typography variant="h6">Xác nhận xóa bộ kíp thi</Typography>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ p: 1 }}>
          <Typography>
            Bạn có chắc chắn muốn xóa bộ kíp thi "<strong>{collectionName}</strong>" không?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Tất cả các kíp thi thuộc bộ này sẽ bị xóa. Hành động này không thể hoàn tác.
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

export default DeleteCollectionConfirmDialog;
