import React from 'react';
import { 
  Button, 
  Dialog, 
  DialogActions, 
  DialogContent, 
  DialogTitle, 
  Grid,
  IconButton,
  Typography,
  Paper,
  Divider
} from "@mui/material";
import { Close } from '@mui/icons-material';

const ConflictDetailDialog = ({ 
  open, 
  onClose, 
  conflictPair,
  examClasses 
}) => {
  const class1 = examClasses.find(c => c.id === conflictPair?.classId1);
  const class2 = examClasses.find(c => c.id === conflictPair?.classId2);

  if (!conflictPair || !class1 || !class2) {
    return null;
  }

  const renderClassDetails = (classData, colorTheme) => {
    return (
      <Paper 
        elevation={2} 
        sx={{ 
          p: 1.5, 
          mb: 1, 
          borderLeft: `4px solid ${colorTheme}`,
          bgcolor: `${colorTheme}10`,
        }}
      >
        <Typography variant="subtitle1" sx={{ color: colorTheme, fontWeight: 'bold', mb: 0.5, fontSize: '0.95rem' }}>
          {classData.examClassId} - {classData.courseName}
        </Typography>
        
        <Grid container spacing={0.5} sx={{ fontSize: '1.2rem' }}>
          {/* First row */}
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã lớp thi:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.examClassId}</Typography>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã lớp học:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.classId}</Typography>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã học phần:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.courseId}</Typography>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Nhóm:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.groupId}</Typography>
          </Grid>
          
          {/* Second row */}
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Đợt:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.period}</Typography>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã quản lý:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.managementCode}</Typography>
          </Grid>
          <Grid item xs={6} sm={3}>
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Số lượng SV:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.numberOfStudents}</Typography>
          </Grid>
          
          {/* Description in a separate row */}
          <Grid item xs={6} sx={{ mt: 0.5 }}>
            <Divider sx={{ my: 0.5 }} />
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Ghi chú:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }}>{classData.description}</Typography>
          </Grid>
          <Grid item xs={6} sx={{ mt: 0.5 }}>
            <Divider sx={{ my: 0.5 }} />
            <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Trường/Khoa:</Typography>
            <Typography variant="body" sx={{ fontSize: '1rem' }} noWrap>{classData.school}</Typography>
          </Grid>
        </Grid>
      </Paper>
    );
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
    >
      <DialogTitle
        sx={{
          py: 1,
          textAlign: 'center',
          borderBottom: '1px solid #e0e0e0',
          position: 'relative',
          fontSize: '1.1rem'
        }}
      >
        Thông tin chi tiết lớp xung đột
        <IconButton
          onClick={onClose}
          sx={{
            position: 'absolute',
            right: 8,
            top: 4,
            color: (theme) => theme.palette.grey[500],
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>
      
      <DialogContent sx={{ py: 1 }}>
        {renderClassDetails(class1, '#1976d2')} 
        {renderClassDetails(class2, '#d32f2f')} 
      </DialogContent>
      
      <DialogActions sx={{ px: 2, py: 1 }}>
        <Button onClick={onClose} variant="contained" color="primary" size="small">
          Đóng
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConflictDetailDialog;
