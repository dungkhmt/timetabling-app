import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  List,
  ListItem,
  ListItemText,
  Paper,
  Divider,
  Alert,
  AlertTitle
} from '@mui/material';
import { Warning, ErrorOutline } from '@mui/icons-material';
import { getFieldDisplayName } from '../utils/AssignmentValidation';

/**
 * Dialog component to display invalid assignments
 */
const InvalidAssignmentDialog = ({ open, onClose, invalidAssignments }) => {
  if (!invalidAssignments || invalidAssignments.length === 0) {
    return null;
  }

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3
        }
      }}
    >
      <DialogTitle sx={{ 
        bgcolor: '#f44336', 
        color: 'white',
        display: 'flex',
        alignItems: 'center',
        gap: 1
      }}>
        <ErrorOutline />
        <Typography variant="h6" fontWeight={600}>
          Yêu cầu điền đầy đủ thông tin
        </Typography>
      </DialogTitle>
      
      <DialogContent sx={{ pt: 3 }}>
        <Alert severity="warning" sx={{ mb: 3 }}>
          <AlertTitle>Một số lớp thi chưa được cung cấp đủ thông tin</AlertTitle>
          Vui lòng điền đầy đủ thông tin cho các lớp thi sau đây trước khi lưu.
        </Alert>
        
        <Paper 
          variant="outlined" 
          sx={{ 
            maxHeight: '50vh', 
            overflow: 'auto',
            borderRadius: 1
          }}
        >
          <List sx={{ py: 0 }}>
            {invalidAssignments.map((item, index) => (
              <React.Fragment key={item.assignmentId}>
                {index > 0 && <Divider />}
                <ListItem sx={{ py: 2 }}>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Warning sx={{ color: 'warning.main' }} />
                        <Typography variant="subtitle1" fontWeight={600}>
                          {item.examClassId} - {item.courseId}
                        </Typography>
                      </Box>
                    }
                    secondary={
                      <Box sx={{ mt: 1 }}>
                        <Typography variant="body2" color="text.secondary" gutterBottom>
                          {item.courseName}
                        </Typography>
                        <Typography variant="body2" sx={{ mt: 1 }}>
                          Thông tin thiếu:
                          <Box component="span" sx={{ 
                            display: 'inline-flex', 
                            flexWrap: 'wrap',
                            gap: 0.5,
                            ml: 1
                          }}>
                            {item.missingFields.map(field => (
                              <Box
                                key={field}
                                component="span"
                                sx={{
                                  bgcolor: 'error.light',
                                  color: 'error.contrastText',
                                  px: 1,
                                  py: 0.5,
                                  borderRadius: 1,
                                  fontSize: '0.8rem',
                                  fontWeight: 500
                                }}
                              >
                                {getFieldDisplayName(field)}
                              </Box>
                            ))}
                          </Box>
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
              </React.Fragment>
            ))}
          </List>
        </Paper>
      </DialogContent>
      
      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button 
          onClick={onClose} 
          variant="contained" 
          color="primary"
          sx={{ borderRadius: 1 }}
        >
          Đã hiểu
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default InvalidAssignmentDialog;
