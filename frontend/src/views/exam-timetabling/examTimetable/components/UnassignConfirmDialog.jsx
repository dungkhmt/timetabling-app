import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Box,
  Typography,
} from '@mui/material';
import { Warning } from '@mui/icons-material';

const UnassignConfirmDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  assignmentsToUnassign,
  isProcessing
}) => {
  const handleConfirm = () => {
    onConfirm();
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3,
          width: '100%'
        }
      }}
    >
      <DialogTitle sx={{ 
        bgcolor: 'error.light', 
        display: 'flex', 
        alignItems: 'center',
        gap: 1
      }}>
        <Warning color="error" />
        <Typography variant="h6" fontWeight={600}>
          Xác nhận hủy phân công
        </Typography>
      </DialogTitle>
      
      <DialogContent sx={{ mt: 2 }}>
        {assignmentsToUnassign.length > 0 && (
          <>
            <Typography variant="body1">
              Bạn sắp hủy phân công của 
              <Box component="span" sx={{ fontWeight: 700, color: 'error.main', mx: 1 }}>
                {assignmentsToUnassign.length}
              </Box>
              lớp thi này:
            </Typography>
            <Box 
              sx={{ 
                my: 1, 
                maxHeight: '150px', 
                overflowY: 'auto', 
                p: 1, 
                border: '1px solid #eee',
                borderRadius: 1
              }}
            >
              <Typography variant="h6" fontWeight={700} color="error.main">
                {assignmentsToUnassign.map(item => item.examClassId).join(', ')}
              </Typography>
            </Box>
            
            <Typography variant="body1" color="text.secondary" sx={{ mt: 2 }}>
              Thao tác này sẽ xóa phân công phòng, ngày thi và ca thi của các lớp thi đã chọn. Bạn có chắc chắn muốn tiếp tục?
            </Typography>
          </>
        )}
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
          onClick={handleConfirm} 
          variant="contained" 
          color="error"
          disabled={isProcessing || assignmentsToUnassign.length === 0}
        >
          {isProcessing ? 'Đang xử lý...' : 'Xác nhận hủy phân công'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default UnassignConfirmDialog;
