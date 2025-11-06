import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  FormHelperText,
  CircularProgress,
  IconButton,
  Box,
  Divider,
  Paper,
  Autocomplete,
  Alert,
} from "@mui/material";
import ClearIcon from "@mui/icons-material/Clear";
import { toast } from "react-toastify";
import { useGroupData } from "services/useGroupData";
import { useCourseData } from "services/useCourseData";
import { request } from "api";
import { validateClassForm, prepareClassPayload, extractErrorMessage } from "../utils/formValidate";

const AddNewClassDialog = ({ open, onClose, semester, onSuccess, selectedGroup }) => {
  // Khởi tạo formData với giá trị từ props ngay từ đầu
  const [formData, setFormData] = useState({
    groupId: selectedGroup?.id || "", 
    programName: selectedGroup?.groupName || "", 
    moduleCode: "", 
    moduleName: "", 
    classType: "LT+BT",
    numberOfClasses: "", // Số lượng lớp học
    learningWeeks: "", // Tuần học
    weekType: "0", // Tuần chẵn/lẻ
    crew: "", // Kíp
    duration: "", // Số tiết
    promotion: "", // Khóa
    lectureMaxQuantity: "", // Số SV Max (LT)
    exerciseMaxQuantity: "", // Số SV Max (BT)
    lectureExerciseMaxQuantity: "", // Số SV Max (LT+BT)
    mass: "", // Khối lượng X(a-b-c-d)
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [course, setCourse] = useState(null);
  // Fetch groups and courses
  const { allGroups, isLoadingGroups } = useGroupData();
  const { courses, isLoading: isLoadingCourses } = useCourseData();
  
  // Cập nhật formData mỗi khi dialog mở hoặc selectedGroup thay đổi
  useEffect(() => {
    // Chỉ cập nhật khi dialog đang mở để tránh cập nhật không cần thiết
    if (open && selectedGroup) {
      setFormData(prev => ({
        ...prev,
        groupId: selectedGroup.id || "",
        programName: selectedGroup.groupName || ""
      }));
    }
  }, [open, selectedGroup]);
  
  // Cập nhật selectedCourse đồng thời cập nhật formData tương ứng
  const handleCourseChange = (newSelectedCourse) => {
    setSelectedCourse(newSelectedCourse);
    setLoading(true);
    request(
      "get",
      "/course-v2/" + newSelectedCourse.id,
      (res) => {
        console.log("Success response:", res.data);
        setLoading(false);
        if (onSuccess && res.data) {
          onSuccess(res.data);
          setCourse(res.data);
          setFormData(prev => ({
            ...prev,        
            mass: res.data.volumn
          }));
        }
        //toast.success("get Course Detail thành công!");
        
      },
      null
      
    ).catch((err) => {
      // This will catch any other errors that might occur
      console.error("Unexpected error:", err);
      setLoading(false);
      toast.error(err.response.data);
    });

    if (newSelectedCourse) {
      setFormData(prev => ({
        ...prev,
        
        moduleCode: newSelectedCourse.id,
        moduleName: newSelectedCourse.courseName
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        moduleCode: "",
        moduleName: ""
      }));
    }
  };

  const handleChangeClassType = (e) => {
    let classType = e.target.value
    setFormData({
      ...formData,
      classType: classType
    });
    if(course){
      if(classType === 'LT+BT'){
        setFormData({
          ...formData,
          duration: course.durationLtBt,
          lectureMaxQuantity: "", // Số SV Max (LT)
          exerciseMaxQuantity: "", // Số SV Max (BT)
          lectureExerciseMaxQuantity: course.maxStudentLTBT, // Số SV Max (LT+BT)
        });
      }else if(classType === "LT"){
        setFormData({
          ...formData,
          duration: course.durationLt,
          lectureMaxQuantity: course.maxStudentLT, // Số SV Max (LT)
          exerciseMaxQuantity: "", // Số SV Max (BT)
          lectureExerciseMaxQuantity: "", // Số SV Max (LT+BT)
        });
      }else if(classType === "BT"){
        setFormData({
          ...formData,
          duration: course.durationBt,
          lectureMaxQuantity: "", // Số SV Max (LT)
          exerciseMaxQuantity: course.maxStudentBT, // Số SV Max (BT)
          lectureExerciseMaxQuantity: "", // Số SV Max (LT+BT)
        });  
      }
    }
  }
  const handleChange = (e) => {
    const { name, value } = e.target;
    // Trim string values to remove leading and trailing whitespace
    const trimmedValue = typeof value === 'string' ? value.trim() : value;
    
    setFormData({
      ...formData,
      [name]: trimmedValue,
    });

    // Clear error for the field being edited
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: null,
      });
    }
  };

  const handleNumberChange = (e) => {
    const { name, value } = e.target;
    // Trim the value before checking if it's empty
    const trimmedValue = typeof value === 'string' ? value.trim() : value;
    
    setFormData({
      ...formData,
      [name]: trimmedValue === "" ? "" : Number(trimmedValue),
    });

    // Clear error for the field being edited
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: null,
      });
    }
  };

  const handleClearField = (fieldName) => {
    if (fieldName === "groupId") {
      // Clear both groupId and programName when clearing the group
      setFormData({
        ...formData,
        groupId: "",
        programName: ""
      });
      
      // Clear any related errors
      if (errors.groupId || errors.programName) {
        setErrors({
          ...errors,
          groupId: null,
          programName: null
        });
      }
    } else {
      setFormData({
        ...formData,
        [fieldName]: "",
      });
    }
  };
  
  const handleSubmit = () => {
    const trimmedFormData = Object.keys(formData).reduce((acc, key) => {
      // If the value is a string, trim it
      if (typeof formData[key] === 'string') {
        acc[key] = formData[key].trim();
      } else {
        acc[key] = formData[key];
      }
      return acc;
    }, {});
    
    if (!selectedGroup) {
      toast.error("Vui lòng chọn chương trình học từ màn hình chính trước khi tạo lớp!");
      return;
    }
    
    // Use the trimmed data for validation
    const validationErrors = validateClassForm(trimmedFormData, semester);
    
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      toast.error("Vui lòng kiểm tra lại thông tin nhập liệu!");
      return;
    }

    if (!semester || !semester.semester) {
      toast.error("Vui lòng chọn kỳ học trước khi tạo lớp!");
      return;
    }

    setLoading(true);

    // Use the trimmed data for the payload
    const payload = prepareClassPayload(trimmedFormData, semester);
    console.log("Submitting payload:", payload);

    request(
      "post",
      "/plan-general-classes/create-single",
      (res) => {
        console.log("Success response:", res.data);
        setLoading(false);
        if (onSuccess && res.data) {
          onSuccess(res.data);
        }
        toast.success("Tạo lớp mới thành công!");
        //handleClose();
      },
      null
      ,
      payload
    ).catch((err) => {
      // This will catch any other errors that might occur
      console.error("Unexpected error:", err);
      setLoading(false);
      toast.error(err.response.data);
    });
  };  
  const handleClose = () => {
    // Reset formData but keep the selected group information
    setFormData({
      groupId: selectedGroup?.id || "",
      programName: selectedGroup?.groupName || "",
      moduleCode: "",
      moduleName: "",
      numberOfClasses: "",
      learningWeeks: "",
      weekType: "0",
      crew: "",
      duration: "",
      promotion: "",
      lectureMaxQuantity: "",
      exerciseMaxQuantity: "",
      lectureExerciseMaxQuantity: "",
      mass: "",
    });
    setSelectedCourse(null);
    setErrors({});
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Typography variant="h6">
          Tạo lớp mới {semester ? `- Kỳ học: ${semester.semester}` : ""}
        </Typography>
      </DialogTitle>

      <DialogContent>
        {!semester && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Vui lòng chọn kỳ học trước khi tạo lớp
          </Alert>
        )}
          <Box sx={{ display: 'flex', flexDirection: 'row', gap: 2, mt: 1 }}>
          <Box sx={{ flex: 2 }}>
            <Grid container spacing={2}>              
              <Grid item xs={12}>
                <FormControl fullWidth error={!!errors.groupId || !!errors.programName}>
                  <InputLabel>Chương trình học *</InputLabel>
                  <Select
                    name="groupId"
                    value={formData.groupId}
                    label="Chương trình học"
                    disabled={true}
                  >
                    {selectedGroup ? (
                      <MenuItem key={selectedGroup.id} value={selectedGroup.id}>
                        {selectedGroup.groupName}
                      </MenuItem>
                    ) : (
                      <MenuItem value="" disabled>
                        <em>Vui lòng chọn chương trình học từ màn hình chính</em>
                      </MenuItem>
                    )}
                  </Select>
                  {(errors.groupId || errors.programName) && 
                    <FormHelperText>{errors.groupId || errors.programName}</FormHelperText>}
                </FormControl>
              </Grid>
                <Grid item xs={6}>
                <Autocomplete
                  options={courses || []}
                  getOptionLabel={(option) => `${option.id} - ${option.courseName}`}
                  isOptionEqualToValue={(option, value) => option.id === value.id}
                  loading={isLoadingCourses}
                  value={selectedCourse}
                  onChange={(_, newValue) => {
                    handleCourseChange(newValue);
                  }}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Học phần"
                      required
                      error={!!errors.moduleCode || !!errors.moduleName}
                      helperText={errors.moduleCode || errors.moduleName}
                      InputProps={{
                        ...params.InputProps,
                        endAdornment: (
                          <>
                            {isLoadingCourses ? <CircularProgress size={20} /> : null}
                            {params.InputProps.endAdornment}
                          </>
                        ),
                      }}
                      
                    />
                  )}
                />
                
                <input type="hidden" name="moduleCode" value={formData.moduleCode} />
                <input type="hidden" name="moduleName" value={formData.moduleName} />
              </Grid>
              
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>Kiểu lớp</InputLabel>
                  <Select
                    name="classType"
                    value={formData.classType}
                    onChange={handleChangeClassType}
                    label="Kiểu lớp"                    
                  >
                    <MenuItem value="LT+BT">LT+BT</MenuItem>
                    <MenuItem value="LT">LT</MenuItem>
                    <MenuItem value="BT">BT</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={6}>
                <TextField
                  label="Khóa"
                  name="promotion"
                  value={formData.promotion}
                  onChange={handleChange}
                  fullWidth
                  placeholder="Ví dụ: 65, 66"
                  InputProps={{
                    endAdornment: formData.promotion ? (
                      <IconButton
                        size="small"
                        onClick={() => handleClearField("promotion")}
                      >
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    ) : null,
                  }}
                />
              </Grid>
              
              <Grid item xs={6}>
                <TextField
                  label="Khối lượng"
                  name="mass"
                  value={formData.mass}
                  onChange={handleChange}
                  fullWidth
                  placeholder="Ví dụ: 3(3-0-0-6)"
                  error={!!errors.mass}
                  helperText={errors.mass || "Định dạng: X(a-b-c-d) VD: 3(3-0-1-6)"}
                  InputProps={{
                    endAdornment: formData.mass ? (
                      <IconButton
                        size="small"
                        onClick={() => handleClearField("mass")}
                      >
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    ) : null,
                  }}
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Số lượng lớp học"
                  name="numberOfClasses"
                  value={formData.numberOfClasses}
                  onChange={handleNumberChange}
                  fullWidth
                  required
                  type="number"
                  error={!!errors.numberOfClasses}
                  helperText={errors.numberOfClasses}
                  InputProps={{
                    endAdornment: formData.numberOfClasses ? (
                      <IconButton
                        size="small"
                        onClick={() => handleClearField("numberOfClasses")}
                      >
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    ) : null,
                  }}
                />
              </Grid>
              
              <Grid item xs={6}>
                <TextField
                  label="Tuần học *"
                  name="learningWeeks"
                  value={formData.learningWeeks}
                  onChange={handleChange}
                  fullWidth
                  required
                  placeholder="Ví dụ: 2-9,11-18"
                  error={!!errors.learningWeeks}
                  helperText={errors.learningWeeks}
                  InputProps={{
                    endAdornment: formData.learningWeeks ? (
                      <IconButton
                        size="small"
                        onClick={() => handleClearField("learningWeeks")}
                      >
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    ) : null,
                  }}
                />
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>Kiểu tuần</InputLabel>
                  <Select
                    name="weekType"
                    value={formData.weekType}
                    onChange={handleChange}
                    label="Kiểu tuần"
                    endAdornment={
                      formData.weekType !== "0" ? (
                        <IconButton
                          size="small"
                          sx={{ marginRight: 2 }}
                          onClick={() => handleClearField("weekType")}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      ) : null
                    }
                  >
                    <MenuItem value="0">AB</MenuItem>
                    <MenuItem value="1">A</MenuItem>
                    <MenuItem value="2">B</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel id="crew-label">Kíp</InputLabel>
                  <Select
                    labelId="crew-label"
                    name="crew"
                    value={formData.crew}
                    onChange={handleChange}
                    label="Kíp"
                    displayEmpty
                    renderValue={(selected) => {
                      return selected ? (selected === 'S' ? 'Sáng' : 'Chiều') : '';
                    }}
                    endAdornment={
                      formData.crew ? (
                        <IconButton
                          size="small"
                          sx={{ marginRight: 2 }}
                          onClick={() => handleClearField("crew")}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      ) : null
                    }
                  >
                    <MenuItem value="S">Sáng</MenuItem>
                    <MenuItem value="C">Chiều</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Số tiết"
                  name="duration"
                  value={formData.duration}
                  onChange={handleNumberChange}
                  fullWidth
                  type="number"
                  InputProps={{
                    endAdornment: formData.duration ? (
                      <IconButton
                        size="small"
                        onClick={() => handleClearField("duration")}
                      >
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    ) : null,
                  }}
                />
              </Grid>
            </Grid>
          </Box>

          <Divider orientation="vertical" flexItem />

          <Box sx={{ flex: 1, display: 'flex', alignItems: 'flex-start' }}>
            <Paper elevation={0} sx={{ p: 2, bgcolor: '#f8f9fa', width: '100%' }}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    label="Max sv/LT"
                    name="lectureMaxQuantity"
                    value={formData.lectureMaxQuantity}
                    onChange={handleNumberChange}
                    fullWidth
                    type="number"
                    InputProps={{
                      endAdornment: formData.lectureMaxQuantity ? (
                        <IconButton
                          size="small"
                          onClick={() => handleClearField("lectureMaxQuantity")}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      ) : null,
                    }}
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    label="Max sv/BT"
                    name="exerciseMaxQuantity"
                    value={formData.exerciseMaxQuantity}
                    onChange={handleNumberChange}
                    fullWidth
                    type="number"
                    InputProps={{
                      endAdornment: formData.exerciseMaxQuantity ? (
                        <IconButton
                          size="small"
                          onClick={() => handleClearField("exerciseMaxQuantity")}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      ) : null,
                    }}
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    label="Max sv/LT+BT"
                    name="lectureExerciseMaxQuantity"
                    value={formData.lectureExerciseMaxQuantity}
                    onChange={handleNumberChange}
                    fullWidth
                    type="number"
                    InputProps={{
                      endAdornment: formData.lectureExerciseMaxQuantity ? (
                        <IconButton
                          size="small"
                          onClick={() => handleClearField("lectureExerciseMaxQuantity")}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      ) : null,
                    }}
                  />
                </Grid>
              </Grid>
            </Paper>
          </Box>
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} color="primary">
          Hủy
        </Button>
        <Button
          onClick={handleSubmit}
          color="primary"
          variant="contained"
          // disabled={loading || !semester}
        >
          Tạo lớp
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddNewClassDialog;
