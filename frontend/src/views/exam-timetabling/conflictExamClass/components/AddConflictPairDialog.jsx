import React, { useState, useEffect } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  TextField,
  Typography,
  Box,
  Paper,
  Autocomplete,
  Chip,
  Grid,
  Divider,
  Alert
} from "@mui/material";
import { Close, Search, Error } from '@mui/icons-material';

const ClassInfoBox = ({ classData, colorTheme }) => {
  if (!classData) return null;
  
  return (
    <Paper 
      elevation={2} 
      sx={{ 
        p: 1.5, 
        mb: 1, 
        borderLeft: `4px solid ${colorTheme}`,
        bgcolor: `${colorTheme}10`,
        fontSize: '0.9rem'
      }}
    >
      <Typography variant="subtitle1" sx={{ color: colorTheme, fontWeight: 'bold', mb: 0.5 }}>
        {classData.examClassId} - {classData.courseName}
      </Typography>
      
      <Grid container spacing={1} sx={{ fontSize: '0.85rem' }}>
        <Grid item xs={6}>
          <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã lớp thi:</Typography>
          <Typography variant="body2">{classData.examClassId}</Typography>
        </Grid>
        <Grid item xs={6}>
          <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã lớp học:</Typography>
          <Typography variant="body2">{classData.classId}</Typography>
        </Grid>
        
        <Grid item xs={6}>
          <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Mã học phần:</Typography>
          <Typography variant="body2">{classData.courseId}</Typography>
        </Grid>
        <Grid item xs={6}>
          <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Nhóm:</Typography>
          <Typography variant="body2">{classData.groupId}</Typography>
        </Grid>
        
        <Grid item xs={12}>
          <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Trường/Khoa:</Typography>
          <Typography variant="body2" noWrap>{classData.school}</Typography>
        </Grid>
        
        <Grid item xs={12}>
          <Typography variant="caption" sx={{ fontWeight: 'bold', display: 'block' }}>Ghi chú:</Typography>
          <Typography variant="body2">{classData.description}</Typography>
        </Grid>
      </Grid>
    </Paper>
  );
};

const AddConflictPairDialog = ({ 
  open, 
  onClose, 
  onSubmit,
  examClasses = [],
  examPlanId
}) => {
  const [selectedClasses, setSelectedClasses] = useState([]);
  const [searchText, setSearchText] = useState('');
  const [error, setError] = useState('');
  
  useEffect(() => {
    if (open) {
      setSelectedClasses([]);
      setError('');
    }
  }, [open]);
  
  const handleClassSelect = (event, value) => {
    if (value && selectedClasses.length < 2 && !selectedClasses.find(c => c.id === value.id)) {
      setSelectedClasses([...selectedClasses, value]);
    }
    
    setSearchText('');
  };
  
  const handleRemoveClass = (classId) => {
    setSelectedClasses(selectedClasses.filter(c => c.id !== classId));
  };
  
  const handleSubmit = () => {
    if (selectedClasses.length !== 2) {
      setError('Vui lòng chọn đúng 2 lớp để tạo xung đột');
      return;
    }
    
    onSubmit({
      classId1: selectedClasses[0].id,
      classId2: selectedClasses[1].id,
      examPlanId
    });
  };
  
  const availableClasses = examClasses.filter(
    c => !selectedClasses.find(sc => sc.id === c.id)
  );
  
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
        Thêm xung đột lớp mới
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
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            Chọn 2 lớp thi xung đột:
          </Typography>
          
          <Autocomplete
            options={availableClasses}
            getOptionLabel={(option) => `${option.examClassId} - ${option.courseName} (${option.courseId})`}
            onChange={handleClassSelect}
            inputValue={searchText}
            onInputChange={(event, value) => setSearchText(value)}
            value={null}
            disabled={selectedClasses.length >= 2}
            renderInput={(params) => (
              <TextField 
                {...params} 
                label="Tìm kiếm lớp" 
                variant="outlined" 
                fullWidth
                placeholder={selectedClasses.length >= 2 ? "Đã chọn đủ 2 lớp" : "Nhập mã lớp, mã học phần hoặc tên lớp..."}
                InputProps={{
                  ...params.InputProps,
                  startAdornment: (
                    <>
                      <Search color="action" sx={{ mr: 1 }} />
                      {params.InputProps.startAdornment}
                    </>
                  )
                }}
              />
            )}
          />
        </Box>
        
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} icon={<Error />}>
            {error}
          </Alert>
        )}
        
        <Box sx={{ mb: 2 }}>
          <Typography variant="subtitle1" gutterBottom>
            Đã chọn ({selectedClasses.length}/2):
          </Typography>
          
          {selectedClasses.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
              Chưa có lớp nào được chọn
            </Typography>
          ) : (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {selectedClasses.map((classItem, index) => (
                <Chip
                  key={classItem.id}
                  label={`${classItem.examClassId} - ${classItem.courseName}`}
                  onDelete={() => handleRemoveClass(classItem.id)}
                  color={index === 0 ? "primary" : "error"}
                  variant="outlined"
                  sx={{ fontWeight: 'medium' }}
                />
              ))}
            </Box>
          )}
        </Box>
        
        <Divider sx={{ my: 2 }} />
        
        <Box>
          <Typography variant="subtitle1" gutterBottom>
            Thông tin lớp đã chọn:
          </Typography>
          
          {selectedClasses.length > 0 ? (
            selectedClasses.map((classItem, index) => (
              <ClassInfoBox 
                key={classItem.id} 
                classData={classItem} 
                colorTheme={index === 0 ? '#1976d2' : '#d32f2f'} 
              />
            ))
          ) : (
            <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
              Vui lòng chọn lớp để xem thông tin
            </Typography>
          )}
        </Box>
      </DialogContent>
      
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} variant="outlined" color="inherit">
          Hủy
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          color="primary" 
          disabled={selectedClasses.length !== 2}
        >
          Thêm xung đột
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddConflictPairDialog;
