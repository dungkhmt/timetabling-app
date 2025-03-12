import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Grid,
  Divider,
  Paper,
  Chip,
  Stack,
  IconButton,
} from '@mui/material';
import { 
  Update as UpdateIcon, 
  Close as CloseIcon, 
  CheckCircle as CheckCircleIcon,
  School as SchoolIcon,
  Class as ClassIcon
} from '@mui/icons-material';

const UpdateConfirmDialog = ({ open, onClose, onConfirm, classData }) => {
  if (!classData) {
    return null;
  }

  const fieldLabels = {
    classCode: "Mã lớp",
    listGroupName: "Lớp liên quan tới",
    learningWeeks: "Tuần học",
    moduleCode: "Mã học phần",
    moduleName: "Tên học phần",
    quantityMax: "SL MAX",
    classType: "Loại lớp",
    mass: "Thời lượng",
    crew: "Kíp",
    openBatch: "Đợt",
    course: "Khóa",
  };

  const displayFields = [
    "classCode", "listGroupName", "learningWeeks", "moduleCode", 
    "moduleName", "quantityMax", "classType", "mass", 
    "crew", "openBatch", "course"
  ];

  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        elevation: 5,
        sx: { 
          borderRadius: 2,
          overflow: 'hidden'
        }
      }}
    >
      <DialogTitle sx={{ 
        bgcolor: 'primary.main', 
        color: 'white',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        py: 2
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <UpdateIcon sx={{ mr: 1 }} />
          <Typography variant="h6" component="span">
            Xác nhận cập nhật lớp học
          </Typography>
        </Box>
        <IconButton onClick={onClose} sx={{ color: 'white' }}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ p: 3, pt: 3 }}>
        <Typography variant="subtitle1" gutterBottom sx={{ 
          fontWeight: 500, 
          mb: 2,
          display: 'flex',
          alignItems: 'center' 
        }}>
          <ClassIcon sx={{ mr: 1, color: 'primary.main' }} />
          Thông tin cập nhật cho lớp: <Chip 
            label={classData.classCode} 
            color="primary" 
            size="small" 
            sx={{ ml: 1 }}
          />
        </Typography>

        <Divider sx={{ mb: 3 }} />
        
        <Paper elevation={0} sx={{ bgcolor: 'background.default', p: 2, borderRadius: 2 }}>
          <Grid container spacing={2}>
            {displayFields.map(field => (
              <Grid item xs={6} key={field}>
                <Box sx={{ p: 1, display: 'flex', alignItems: 'flex-start' }}>
                  <Typography variant="body2" component="span" fontWeight="bold" sx={{ minWidth: 120 }}>
                    {fieldLabels[field] || field}:
                  </Typography>
                  <Typography 
                    variant="body2" 
                    component="span" 
                    sx={{ 
                      ml: 1,
                      bgcolor: 'background.paper',
                      p: 1,
                      borderRadius: 1,
                      flexGrow: 1,
                      border: '1px solid',
                      borderColor: 'divider'
                    }}
                  >
                    {classData[field] || "—"}
                  </Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Paper>
        
        <Box sx={{ mt: 3, display: 'flex', alignItems: 'center' }}>
          <SchoolIcon color="warning" sx={{ mr: 1 }} />
          <Typography variant="body2" color="text.secondary">
            Vui lòng kiểm tra kỹ thông tin trước khi xác nhận cập nhật.
          </Typography>
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2, bgcolor: 'background.default' }}>
        <Button 
          onClick={onClose} 
          color="inherit"
          variant="outlined"
          startIcon={<CloseIcon />}
        >
          Hủy bỏ
        </Button>
        <Button 
          onClick={onConfirm} 
          color="primary" 
          variant="contained"
          startIcon={<CheckCircleIcon />}
          sx={{ ml: 2 }}
        >
          Xác nhận cập nhật
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default UpdateConfirmDialog;