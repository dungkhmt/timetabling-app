import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  Divider
} from '@mui/material';
import { Warning } from '@mui/icons-material';

const AutoAssignConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  assignedClasses,
  isProcessing 
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3,
        }
      }}
    >
      <DialogTitle sx={{ 
        bgcolor: 'warning.light', 
        display: 'flex', 
        alignItems: 'center',
        gap: 1
      }}>
        <Warning color="warning" />
        <Typography variant="h6" fontWeight={600}>
          Xác nhận phân công tự động
        </Typography>
      </DialogTitle>
      
      <DialogContent sx={{ mt: 2 }}>
        <DialogContentText>
          <Typography variant="body1" >
            Phân công tự động sẽ xóa phân công hiện tại của các lớp thi này: 
          </Typography>
          <Typography variant="body1" fontWeight={600} color="warning.main" sx={{ mb: 2 }}>
            {assignedClasses.map(item => item.examClassId).join(', ')}
          </Typography>
          <Typography variant="body1" fontWeight={600} color="error.main">
            Bạn có chắc chắn muốn phân công tự động cho các lớp đã chọn?
          </Typography>
        </DialogContentText>
      </DialogContent>
      
      <DialogActions sx={{ p: 2, bgcolor: '#f5f5f5' }}>
        <Button 
          onClick={onClose} 
          variant="outlined"
          disabled={isProcessing}
        >
          Hủy
        </Button>
        <Button 
          onClick={onConfirm} 
          variant="contained" 
          color="warning"
          disabled={isProcessing}
        >
          {isProcessing ? 'Đang xử lý...' : 'Tiếp tục phân công tự động'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AutoAssignConfirmDialog;
