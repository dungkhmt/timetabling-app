import React from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
  Chip,
  Divider
} from '@mui/material';
import { Error, AccessTime, Room, Group } from '@mui/icons-material';

const ConflictDialog = ({ 
  open, 
  conflicts, 
  onClose
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
    >
      <DialogTitle sx={{ 
        backgroundColor: '#ffebee',
        display: 'flex',
        alignItems: 'center'
      }}>
        <Error color="error" sx={{ mr: 1 }} />
        Phát hiện xung đột
      </DialogTitle>
      <DialogContent sx={{ py: 2 }}>
        <Typography variant="body1" sx={{ mb: 2 }}>
          Các lớp học sau đây có xung đột lịch thi (cùng phòng, cùng thời gian):
          {conflicts.length > 1 && (
            <Typography component="span" color="error.main" fontWeight={500}>
              {` (${conflicts.length} xung đột)`}
            </Typography>
          )}
        </Typography>
        
        {/* Add fixed height and scroll for many conflicts */}
        <Box sx={{ 
          height: conflicts.length > 5 ? '300px' : 'auto', 
          maxHeight: '50vh',  
          overflowY: 'auto',  
          pr: 1  
        }}>
          {conflicts.map((conflict, index) => (
            <Box 
              key={index} 
              sx={{ 
                mb: 1.5, 
                p: 1.5, 
                border: '1px solid #ffccbc', 
                borderRadius: 1,
                backgroundColor: '#fff8e1'
              }}
            >
              <Box sx={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                mb: 1
              }}>
                <Typography variant="subtitle2" fontWeight={600}>
                  Xung đột {index + 1} / {conflicts.length}
                </Typography>
                <Chip 
                  size="small" 
                  color="error" 
                  label="Trùng lịch" 
                  sx={{ fontSize: '0.75rem' }}
                />
              </Box>
              
              {/* One line for room and time information */}
              <Box sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                mb: 1,
                pb: 1,
                borderBottom: '1px dashed #ffccbc'
              }}>
                <Room fontSize="small" sx={{ color: 'primary.main', mr: 0.5 }} />
                <Typography variant="body2" sx={{ mr: 2 }}>
                  {conflict.roomName || '?'}
                </Typography>
                
                <AccessTime fontSize="small" sx={{ color: 'primary.main', mr: 0.5 }} />
                <Typography variant="body2">
                  {conflict.weekName || '?'}, {conflict.sessionName || '?'}
                </Typography>
              </Box>
              
              {/* Compact list of class IDs */}
              <Typography variant="body2" fontWeight={500} sx={{ mb: 0.5 }}>
                Các lớp bị trùng:
              </Typography>
              
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {conflict.examClassIds && conflict.examClassIds.map((classId, idx) => (
                  <Chip 
                    key={idx}
                    size="small"
                    icon={<Group fontSize="small" />}
                    label={classId}
                    variant="outlined"
                    sx={{ fontSize: '0.75rem' }}
                  />
                ))}
              </Box>
              
              <Typography variant="body2" color="error" sx={{ mt: 1, fontSize: '0.75rem', fontStyle: 'italic' }}>
                Vui lòng cập nhật lại lịch thi cho một hoặc nhiều lớp để giải quyết xung đột.
              </Typography>
            </Box>
          ))}
        </Box>
        
        {conflicts.length > 8 && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1, fontStyle: 'italic', fontSize: '0.75rem' }}>
            * Hiển thị {conflicts.length} xung đột. Cuộn để xem thêm.
          </Typography>
        )}
        
        <Typography variant="body1" sx={{ mt: 1.5, fontWeight: 500, color: 'error.main' }}>
          Không thể lưu khi còn xung đột. Vui lòng giải quyết các xung đột trước khi lưu.
        </Typography>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button 
          onClick={onClose} 
          variant="contained"
          color="primary"
        >
          Đóng và giải quyết xung đột
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConflictDialog;
