import React, { useMemo } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
  Divider,
  Paper
} from '@mui/material';
import { Error, AccessTime, Room, Group, CheckCircle } from '@mui/icons-material';

const ConflictCheckDialog = ({ open, conflicts, onClose }) => {
  const groupedConflicts = useMemo(() => {
    if (!conflicts || conflicts.length === 0) return {};
    
    return conflicts.reduce((acc, conflict) => {
      const type = conflict.conflictType || 'UNKNOWN';
      if (!acc[type]) {
        acc[type] = [];
      }
      acc[type].push(conflict);
      return acc;
    }, {});
  }, [conflicts]);

  const hasConflicts = conflicts && conflicts.length > 0;
  
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
    >
      <DialogTitle sx={{ 
        backgroundColor: hasConflicts ? '#ffebee' : '#e8f5e9',
        display: 'flex',
        alignItems: 'center'
      }}>
        {hasConflicts ? (
          <>
            <Error color="error" sx={{ mr: 1 }} />
            Phát hiện xung đột
          </>
        ) : (
          <>
            <CheckCircle color="success" sx={{ mr: 1 }} />
            Kiểm tra hoàn tất
          </>
        )}
      </DialogTitle>
      <DialogContent sx={{ py: 2 }}>
        {hasConflicts ? (
          <>
            <Typography variant="body1" sx={{ mb: 2 }}>
              Các xung đột sau đây được phát hiện trong lịch thi:
              {conflicts.length > 1 && (
                <Typography component="span" color="error.main" fontWeight={500}>
                  {` (${conflicts.length} xung đột)`}
                </Typography>
              )}
            </Typography>
            
            <Box sx={{ 
              height: conflicts.length > 5 ? '300px' : 'auto', 
              maxHeight: '50vh',  
              overflowY: 'auto',  
              pr: 1  
            }}>
              {Object.entries(groupedConflicts).map(([type, typeConflicts]) => (
                <Paper key={type} elevation={1} sx={{ mb: 2, overflow: 'hidden' }}>
                  <Box sx={{ 
                    p: 1.5, 
                    backgroundColor: type === 'ROOM' ? '#fff3e0' : '#fff8e1',
                    borderBottom: '1px solid #ffe0b2'
                  }}>
                    <Typography variant="subtitle1" fontWeight={600}>
                      {type === 'ROOM' ? 'Xung đột Phòng' : type === 'CLASS' ? 'Xung đột Lớp' : `Xung đột ${type}`}
                      <Typography component="span" color="error.main" sx={{ ml: 1 }}>
                        ({typeConflicts.length})
                      </Typography>
                    </Typography>
                  </Box>
                  
                  {typeConflicts.map((conflict, index) => (
                    <Box 
                      key={index} 
                      sx={{ 
                        p: 1.5, 
                        borderBottom: index < typeConflicts.length - 1 ? '1px dashed #ffe0b2' : 'none',
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                        {/* Room info */}
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <Room fontSize="small" sx={{ color: 'primary.main', mr: 0.5 }} />
                          <Typography variant="body2" sx={{ mr: 1 }}>
                            {conflict.roomId || '?'}
                          </Typography>
                        </Box>
                        
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <AccessTime fontSize="small" sx={{ color: 'primary.main', mr: 0.5 }} />
                          <Typography variant="body2">
                            {conflict.date ? new Date(conflict.date).toLocaleDateString('vi-VN') : '?'}, Ca {conflict.session || '?'}
                          </Typography>
                        </Box>
                        
                        {type === 'CLASS' && (
                          <>
                            <Divider orientation="vertical" flexItem sx={{ mx: 1, height: '20px' }} />
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <Group fontSize="small" sx={{ color: 'error.main', mr: 0.5 }} />
                              <Typography variant="body2" color="error.main">
                                Lớp {conflict.examClassId1 || '?'} và Lớp {conflict.examClassId2 || '?'}
                              </Typography>
                            </Box>
                          </>
                        )}
                      </Box>
                    </Box>
                  ))}
                </Paper>
              ))}
            </Box>
            
            {conflicts.length > 8 && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1, fontStyle: 'italic', fontSize: '0.75rem' }}>
                * Hiển thị {conflicts.length} xung đột. Cuộn để xem thêm.
              </Typography>
            )}
            
            <Typography variant="body1" sx={{ mt: 1.5, fontWeight: 500, color: 'error.main' }}>
              Vui lòng giải quyết các xung đột trước khi lưu lịch thi.
            </Typography>
          </>
        ) : (
          <Box sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center', 
            justifyContent: 'center',
            py: 3
          }}>
            <CheckCircle color="success" sx={{ fontSize: 64, mb: 2 }} />
            <Typography variant="h6" align="center" color="success.main" gutterBottom>
              Không tìm thấy xung đột trong lịch thi
            </Typography>
            <Typography variant="body1" align="center" color="text.secondary">
              Lịch thi hiện tại không có xung đột. Bạn có thể tiếp tục sử dụng an toàn.
            </Typography>
          </Box>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button 
          onClick={onClose} 
          variant="contained"
          color={hasConflicts ? "primary" : "success"}
        >
          Đóng
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConflictCheckDialog;
