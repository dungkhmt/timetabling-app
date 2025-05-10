import React, { useState } from 'react';
import { 
  Button, 
  Dialog, 
  DialogActions, 
  DialogContent, 
  DialogTitle, 
  Grid,
  IconButton,
  Typography,
  FormControl,
  FormHelperText,
  Autocomplete,
  TextField,
  Box,
  CircularProgress,
  Tooltip
} from "@mui/material";
import { Close, CloudUpload, HelpOutline } from '@mui/icons-material';

const UploadExamClassesModal = ({ 
  open, 
  onClose, 
  onSubmit,
  examClassGroups,
  isUploading = false,
  handleDownloadSample
}) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [errors, setErrors] = useState({
    file: '',
    group: ''
  });

  // Handle file selection
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    setSelectedFile(file || null);
    
    // Clear file error if a file is selected
    if (file) {
      setErrors(prev => ({
        ...prev,
        file: ''
      }));
    }
  };

  // Handle group selection
  const handleGroupChange = (event, newValue) => {
    setSelectedGroup(newValue);
    
    // Clear group error if a group is selected
    if (newValue) {
      setErrors(prev => ({
        ...prev,
        group: ''
      }));
    }
  };

  // Validate form before submission
  const validateForm = () => {
    const newErrors = {};
    
    if (!selectedFile) {
      newErrors.file = 'Vui lòng chọn tệp tin để tải lên';
    }
    
    if (!selectedGroup) {
      newErrors.group = 'Vui lòng chọn nhóm lớp thi';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = () => {
    if (validateForm()) {
      onSubmit({
        file: selectedFile,
        groupId: selectedGroup.id,
        groupName: selectedGroup.name
      });
    }
  };

  // Reset form state
  const resetForm = () => {
    setSelectedFile(null);
    setSelectedGroup(null);
    setErrors({
      file: '',
      group: ''
    });
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={resetForm}
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle 
        sx={{ 
          pb: 1,
          textAlign: 'center',
          borderBottom: '1px solid #e0e0e0',
          mb: 1,
          position: 'relative',
          fontWeight: 600,
          background: 'linear-gradient(90deg, #1976D2, #2196F3)',
          color: 'white'
        }}
      >
        Tải lên danh sách lớp thi
        <IconButton
          onClick={resetForm}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: 'white',
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Grid container spacing={3} sx={{ pt: 2 }}>
          {/* Group selection */}
          <Grid item xs={12}>
            <Autocomplete
              options={examClassGroups || []}
              getOptionLabel={(option) => `${option.id} - ${option.name}`}
              value={selectedGroup}
              onChange={handleGroupChange}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Nhóm lớp thi"
                  required
                  error={!!errors.group}
                  helperText={errors.group}
                />
              )}
              isOptionEqualToValue={(option, value) => option && value && option.id === value.id}
              fullWidth
            />
          </Grid>

          {/* File upload */}
          <Grid item xs={12}>
            <FormControl 
              fullWidth 
              error={!!errors.file}
              required
            >
              <Box
                sx={{
                  border: '1px dashed',
                  borderColor: errors.file ? 'error.main' : 'grey.400',
                  borderRadius: 1,
                  p: 2,
                  textAlign: 'center',
                  cursor: 'pointer',
                  '&:hover': {
                    borderColor: 'primary.main',
                    bgcolor: 'rgba(25, 118, 210, 0.04)'
                  }
                }}
                component="label"
              >
                <input
                  type="file"
                  accept=".xlsx,.xls,.csv"
                  hidden
                  onChange={handleFileChange}
                />
                <CloudUpload sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
                <Typography variant="body1" gutterBottom>
                  {selectedFile ? selectedFile.name : 'Chọn hoặc kéo thả tệp tin vào đây'}
                </Typography>
                <Typography variant="caption" color="textSecondary">
                  Hỗ trợ định dạng: .xlsx, .xls, .csv
                </Typography>
              </Box>
              {errors.file && <FormHelperText>{errors.file}</FormHelperText>}
            </FormControl>
          </Grid>

          {/* Instructions with sample file link */}
          <Grid item xs={12}>
            <Typography variant="body2" color="textSecondary">
              File phải có các thông tin và bố cục giống{' '}
              <Box
                component="span"
                sx={{
                  color: 'primary.main',
                  cursor: 'pointer',
                  textDecoration: 'underline',
                  '&:hover': {
                    textDecoration: 'none',
                  }
                }}
                onClick={handleDownloadSample}
              >
                file mẫu
              </Box>.
            </Typography>
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={resetForm} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          color="primary"
          disabled={isUploading}
          startIcon={isUploading ? <CircularProgress size={20} /> : null}
        >
          {isUploading ? 'Đang tải lên...' : 'Tải lên'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default UploadExamClassesModal;
