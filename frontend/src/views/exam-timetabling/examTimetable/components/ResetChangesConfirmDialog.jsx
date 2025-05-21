import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  CircularProgress
} from '@mui/material';
import { Warning } from '@mui/icons-material';

/**
 * Confirmation dialog for resetting unsaved assignment changes
 */
const ResetChangesConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  changesCount = 0,
  isProcessing = false 
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="reset-dialog-title"
      aria-describedby="reset-dialog-description"
      maxWidth="sm"
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3
        }
      }}
    >
      <DialogTitle 
        id="reset-dialog-title"
        sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: 1,
          color: 'warning.main',
          pb: 1
        }}
      >
        <Warning color="warning" />
        Xác nhận hoàn tác thay đổi
      </DialogTitle>
      <DialogContent>
        <DialogContentText id="reset-dialog-description" sx={{ mb: 2 }}>
          Bạn có chắc chắn muốn hoàn tác tất cả thay đổi chưa lưu?
          {changesCount > 0 && (
            <strong>{` (${changesCount} thay đổi)`}</strong>
          )}
        </DialogContentText>
        <DialogContentText color="text.secondary" variant="body2">
          Tất cả các thông tin phân công chưa lưu sẽ bị mất và không thể khôi phục.
        </DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button 
          onClick={onClose} 
          variant="outlined"
          disabled={isProcessing}
        >
          Hủy
        </Button>
        <Button 
          onClick={onConfirm} 
          color="warning" 
          variant="contained"
          disabled={isProcessing}
          startIcon={isProcessing ? <CircularProgress size={16} color="inherit" /> : null}
        >
          {isProcessing ? 'Đang xử lý...' : 'Hoàn tác thay đổi'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ResetChangesConfirmDialog;
