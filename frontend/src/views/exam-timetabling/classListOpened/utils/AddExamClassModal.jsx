import React, { useState } from 'react';
import { 
  Button, 
  Dialog, 
  DialogActions, 
  DialogContent, 
  DialogTitle, 
  Grid, 
  IconButton, 
  TextField,
  Autocomplete
} from "@mui/material";
import { Close } from '@mui/icons-material'

const AddExamClassModal = ({ 
  open, 
  onClose, 
  onSubmit,
  examCourses,
  examClassGroups,
  managementCodes,
  schools,
}) => {
  const [formData, setFormData] = useState({
    examClassId: '',
    classId: '',
    courseId: '',
    courseName: '',
    description: '',
    groupId: '',
    numberOfStudents: '',
    school: '',
    period: '',
    managementCode: ''
  });

  const [errors, setErrors] = useState({});
  
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [selectedDescription, setSelectedDescription] = useState(null);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleCourseChange = (event, newValue) => {
    setSelectedCourse(newValue);
    
    if (newValue) {
      setFormData(prev => ({
        ...prev,
        courseId: newValue.id,
        courseName: newValue.name
      }));
      
      if (errors.courseId || errors.courseName) {
        setErrors(prev => ({
          ...prev,
          courseId: '',
          courseName: ''
        }));
      }
    } else {
      setFormData(prev => ({
        ...prev,
        courseId: '',
        courseName: ''
      }));
    }
  };

  const handleDescriptionChange = (event, selectedGroup) => {
    setSelectedDescription(selectedGroup);
    
    if (selectedGroup) {
      setFormData(prev => ({
        ...prev,
        description: selectedGroup.name,
        examClassGroupId: selectedGroup.id
      }));
      
      if (errors.description) {
        setErrors(prev => ({
          ...prev,
          description: ''
        }));
      }
    } else {
      setFormData(prev => ({
        ...prev,
        description: ''
      }));
    }
  };

  const handleManagementCodeChange = (event, newValue) => {
    if (newValue) {
      setFormData(prev => ({
        ...prev,
        managementCode: newValue
      }));
      
      if (errors.managementCode) {
        setErrors(prev => ({
          ...prev,
          managementCode: ''
        }));
      }
    } else {
      setFormData(prev => ({
        ...prev,
        managementCode: ''
      }));
    }
  };

  const handleSchoolChange = (event, newValue) => {
    if (newValue) {
      setFormData(prev => ({
        ...prev,
        school: newValue
      }));
      
      if (errors.school) {
        setErrors(prev => ({
          ...prev,
          school: ''
        }));
      }
    } else {
      setFormData(prev => ({
        ...prev,
        school: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    Object.keys(formData).forEach(key => {
      if (!formData[key].toString().trim()) {
        newErrors[key] = 'Trường này không được để trống';
      }
    });
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (validateForm()) {
      onSubmit(formData);
      
      setFormData({
        examClassId: '',
        classId: '',
        courseId: '',
        courseName: '',
        description: '',
        groupId: '',
        numberOfStudents: '',
        school: '',
        period: '',
        managementCode: ''
      });
      
      setSelectedCourse(null);
      setSelectedDescription(null);
    }
  };

  const resetForm = () => {
    setFormData({
      examClassId: '',
      classId: '',
      courseId: '',
      courseName: '',
      description: '',
      groupId: '',
      numberOfStudents: '',
      school: '',
      period: '',
      managementCode: ''
    });
    setErrors({});
    setSelectedCourse(null);
    setSelectedDescription(null);
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={resetForm}
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
        Thêm lớp thi mới
        <IconButton
          onClick={resetForm}
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
              onChange={handleChange}
              error={!!errors.examClassId}
              helperText={errors.examClassId}
              required
              size="small"
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              name="classId"
              label="Mã lớp học"
              fullWidth
              value={formData.classId}
              onChange={handleChange}
              error={!!errors.classId}
              helperText={errors.classId}
              required
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
                  required
                  error={!!errors.managementCode}
                  helperText={errors.managementCode}
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
                  required
                  error={!!errors.courseId || !!errors.courseName}
                  helperText={errors.courseId || errors.courseName}
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
              onChange={handleChange}
              error={!!errors.groupId}
              helperText={errors.groupId}
              required
              size="small"
            />
          </Grid>
          <Grid item xs={4}>
            <TextField
              name="period"
              label="Đợt"
              fullWidth
              value={formData.period}
              onChange={handleChange}
              error={!!errors.period}
              helperText={errors.period}
              required
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
              onChange={handleChange}
              error={!!errors.numberOfStudents}
              helperText={errors.numberOfStudents}
              required
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
                  required
                  error={!!errors.school}
                  helperText={errors.school}
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
                  required
                  error={!!errors.description}
                  helperText={errors.description}
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
        <Button onClick={resetForm} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          Thêm lớp thi
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddExamClassModal;
