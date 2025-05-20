import React, { useMemo } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
  Chip,
  Divider,
  Paper
} from '@mui/material';
import { Error, AccessTime, Room, Group } from '@mui/icons-material';

const ConflictDialog = ({ 
  open, 
  conflicts, 
  onClose
}) => {
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

  const totalConflicts = conflicts ? conflicts.length : 0;
  
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
        {totalConflicts > 0 && (
          <Typography component="span" color="error.main" fontWeight={500} sx={{ ml: 1 }}>
            ({totalConflicts} xung đột)
          </Typography>
        )}
      </DialogTitle>
      <DialogContent sx={{ py: 2 }}>
        <Typography variant="body1" sx={{ mb: 2 }}>
          Các lớp học sau đây có xung đột lịch thi (cùng phòng, cùng thời gian):
        </Typography>
        
        {/* Add fixed height and scroll for many conflicts */}
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
                        {conflict.roomId || conflict.roomName || '?'}
                      </Typography>
                    </Box>
                    
                    {/* Time info */}
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <AccessTime fontSize="small" sx={{ color: 'primary.main', mr: 0.5 }} />
                      <Typography variant="body2">
                        {conflict.date}, 
                        Ca {conflict.session || conflict.sessionName || '?'}
                      </Typography>
                    </Box>
                    
                    {/* Class info for CLASS type */}
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
                    
                    {/* For backward compatibility with examClassIds array format */}
                    {conflict.examClassIds && conflict.examClassIds.length > 0 && (
                      <>
                        <Divider orientation="vertical" flexItem sx={{ mx: 1, height: '20px' }} />
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, alignItems: 'center' }}>
                          <Typography variant="body2" fontWeight={500}>
                            Các lớp bị trùng:
                          </Typography>
                          {conflict.examClassIds.map((classId, idx) => (
                            <Chip 
                              key={idx}
                              size="small"
                              icon={<Group fontSize="small" />}
                              label={classId}
                              variant="outlined"
                              color="error"
                              sx={{ fontSize: '0.75rem' }}
                            />
                          ))}
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
