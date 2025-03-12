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
} from '@mui/material';

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
    >
      <DialogTitle>
        Xác nhận cập nhật lớp học
      </DialogTitle>
      <DialogContent>
        <Typography variant="subtitle1" gutterBottom>
          Xác nhận cập nhật thông tin lớp học sau:
        </Typography>
        <Box sx={{ mt: 2 }}>
          <Grid container spacing={2}>
            {displayFields.map(field => (
              <Grid item xs={6} key={field}>
                <Typography variant="body2" component="span" fontWeight="bold">
                  {fieldLabels[field] || field}: 
                </Typography>
                <Typography variant="body2" component="span" sx={{ ml: 1 }}>
                  {classData[field] || ""}
                </Typography>
              </Grid>
            ))}
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          Hủy
        </Button>
        <Button onClick={onConfirm} color="primary" variant="contained">
          Xác nhận
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default UpdateConfirmDialog;
