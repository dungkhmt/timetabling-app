import React, { useState, useEffect } from 'react';
import { 
  Button, 
  Dialog, 
  DialogActions, 
  DialogContent, 
  DialogTitle, 
  TextField,
  Grid,
  IconButton,
  Autocomplete
} from "@mui/material";
import { Close } from '@mui/icons-material'

const EditExamClassModal = ({ 
  open, 
  onClose, 
  formData, 
  onChange, 
  onSubmit,
  examCourses,
  examClassGroups,
  managementCodes,
  schools
}) => {
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [selectedDescription, setSelectedDescription] = useState(null);

  useEffect(() => {
    if (formData && examCourses) {
      const matchedCourse = examCourses.find(
        course => course.id === formData.courseId
      );
      setSelectedCourse(matchedCourse || null);
    }

    if (formData && examClassGroups) {
      const matchedGroup = examClassGroups.find(
        group => group.name === formData.description
      );
      setSelectedDescription(matchedGroup || null);
    }
  }, [formData, examCourses, examClassGroups]);

  const handleCourseChange = (event, newValue) => {
    setSelectedCourse(newValue);
    
    if (newValue) {
      const courseIdEvent = { target: { name: 'courseId', value: newValue.id } };
      const courseNameEvent = { target: { name: 'courseName', value: newValue.name } };
      
      onChange(courseIdEvent);
      onChange(courseNameEvent);
    } else {
      const courseIdEvent = { target: { name: 'courseId', value: '' } };
      const courseNameEvent = { target: { name: 'courseName', value: '' } };
      
      onChange(courseIdEvent);
      onChange(courseNameEvent);
    }
  };

  const handleDescriptionChange = (event, newValue) => {
    setSelectedDescription(newValue);
    
    if (newValue) {
      const descriptionEvent = { 
        target: { 
          name: 'description', 
          value: newValue.name 
        } 
      };
      onChange(descriptionEvent);
    } else {
      const descriptionEvent = { 
        target: { 
          name: 'description', 
          value: '' 
        } 
      };
      onChange(descriptionEvent);
    }
  };

  const handleManagementCodeChange = (event, newValue) => {
    const managementCodeEvent = { 
      target: { 
        name: 'managementCode', 
        value: newValue || ''
      } 
    };
    onChange(managementCodeEvent);
  };

  const handleSchoolChange = (event, newValue) => {
    const schoolEvent = { 
      target: { 
        name: 'school', 
        value: newValue || ''
      } 
    };
    onChange(schoolEvent);
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
          pb: 1,
          textAlign: 'center',
          borderBottom: '1px solid #e0e0e0',
          mb: 1,
          position: 'relative'  
        }}
      >
        Chỉnh sửa thông tin lớp thi
        <IconButton
          onClick={onClose}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: (theme) => theme.palette.grey[500],
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Grid container spacing={2} sx={{ pt: 1 }}>
          <Grid item xs={4}>
            <TextField
              name="examClassId"
              label="Mã lớp thi"
              fullWidth
              value={formData.examClassId}
              onChange={onChange}
              size="small"
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              name="classId"
              label="Mã lớp học"
              fullWidth
              value={formData.classId}
              onChange={onChange}
              size="small"
            />
          </Grid>
          <Grid item xs={4}>
            <Autocomplete
              options={managementCodes || []}
              getOptionLabel={(option) => option}
              value={formData.managementCode || null}
              onChange={handleManagementCodeChange}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Mã quản lý"
                  size="small"
                />
              )}
              freeSolo
              fullWidth
            />
          </Grid>

          <Grid item xs={12}>
            <Autocomplete
              options={examCourses || []}
              getOptionLabel={(option) => `${option.id} - ${option.name}`}
              value={selectedCourse}
              onChange={handleCourseChange}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Học phần"
                  size="small"
                />
              )}
              isOptionEqualToValue={(option, value) => option && value && option.id === value.id}
              fullWidth
            />
          </Grid>

          <Grid item xs={4}>
            <TextField
              name="groupId"
              label="Nhóm"
              fullWidth
              value={formData.groupId}
              onChange={onChange}
              size="small"
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              name="period"
              label="Đợt"
              fullWidth
              value={formData.period}
              onChange={onChange}
              size="small"
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              name="numberOfStudents"
              label="Số lượng SV"
              fullWidth
              type="number"
              value={formData.numberOfStudents}
              onChange={onChange}
              size="small"
            />
          </Grid>

          <Grid item xs={12}>
            <Autocomplete
              options={schools || []}
              getOptionLabel={(option) => option.name}
              value={formData.school || null}
              onChange={handleSchoolChange}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Trường/Khoa"
                  size="small"
                />
              )}
              freeSolo
              fullWidth
            />
          </Grid>

          <Grid item xs={12}>
            <Autocomplete
              options={examClassGroups || []}
              getOptionLabel={(option) => option.name}
              value={selectedDescription}
              onChange={handleDescriptionChange}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Ghi chú"
                  multiline
                  rows={2}
                />
              )}
              isOptionEqualToValue={(option, value) => option && value && option.name === value.name}
              freeSolo
              fullWidth
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button onClick={onSubmit} variant="contained" color="primary">
          Lưu thay đổi
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditExamClassModal;
