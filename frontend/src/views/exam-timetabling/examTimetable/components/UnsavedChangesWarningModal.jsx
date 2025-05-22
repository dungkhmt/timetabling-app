import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';
import { Warning } from '@mui/icons-material';

const UnsavedChangesWarningModal = ({ 
  open, 
  onClose, 
  onConfirm, 
  changesCount = 0
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="unsaved-warning-dialog-title"
      aria-describedby="unsaved-warning-dialog-description"
      maxWidth="sm"
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3
        }
      }}
    >
      <DialogTitle 
        id="unsaved-warning-dialog-title"
        sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: 1,
          color: 'error.main',
          pb: 1
        }}
      >
        <Warning color="error" />
        Cảnh báo: Bạn có thay đổi chưa lưu
      </DialogTitle>
      <DialogContent>
        <DialogContentText id="unsaved-warning-dialog-description" sx={{ mb: 2 }}>
          Bạn có {changesCount} thay đổi chưa được lưu. Nếu rời khỏi trang này, tất cả thay đổi sẽ bị mất.
        </DialogContentText>
        <DialogContentText color="text.secondary" variant="body2">
          Bạn có muốn lưu thay đổi trước khi rời khỏi trang không?
        </DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button 
          onClick={onClose} 
          color="primary"
          variant="text"
        >
          Đóng
        </Button>
        {/* <Button 
          onClick={() => onConfirm(false)} 
          color="error" 
          variant="outlined"
        >
          Rời đi không lưu
        </Button> */}
      </DialogActions>
    </Dialog>
  );
};

export default UnsavedChangesWarningModal;
