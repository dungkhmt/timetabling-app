import React, { useEffect, useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  CircularProgress,
} from "@mui/material";
import { useCourseData } from "services/useCourseData";

export default function CreateNewCourse({ open, handleClose, selectedCourse }) {
  const { createCourse, updateCourse, isCreating, isUpdating } = useCourseData();
  const [course, setCourse] = useState({
    id: "",
    courseName: "",
    slotsPriority: "",
    maxTeacherInCharge: 1
  });
  const [errors, setErrors] = useState({});

  // Set form values when editing an existing course
  useEffect(() => {
    if (selectedCourse) {
      setCourse({
        id: selectedCourse.id || "",
        courseName: selectedCourse.courseName || "",
        slotsPriority: selectedCourse.slotsPriority || "",
        maxTeacherInCharge: selectedCourse.maxTeacherInCharge || 1
      });
      setErrors({});
    } else {
      // Reset form when creating a new course
      setCourse({
        id: "",
        courseName: "",
        slotsPriority: "",
        maxTeacherInCharge: 1
      });
      setErrors({});
    }
  }, [selectedCourse, open]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCourse((prev) => ({
      ...prev,
      [name]: name === 'maxTeacherInCharge' ? (value === '' ? '' : parseInt(value, 10)) : value,
    }));

    // Clear errors for this field
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!course.id) newErrors.id = "Mã môn học không được để trống";
    if (!course.courseName) newErrors.courseName = "Tên môn học không được để trống";
    if (!course.slotsPriority) newErrors.slotsPriority = "Độ ưu tiên ca học không được để trống";
    if (!course.maxTeacherInCharge && course.maxTeacherInCharge !== 0) {
      newErrors.maxTeacherInCharge = "Số giáo viên tối đa không được để trống";
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      if (selectedCourse) {
        await updateCourse(course);
      } else {
        await createCourse(course);
      }
      handleClose();
    } catch (error) {
      console.error("Error submitting form:", error);
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {selectedCourse ? "Cập nhật môn học" : "Thêm môn học mới"}
      </DialogTitle>
      <DialogContent>
        <Box
          component="form"
          sx={{
            display: "flex",
            flexDirection: "column",
            gap: 2,
            mt: 2,
          }}
        >
          <TextField
            label="Mã môn học"
            name="id"
            value={course.id}
            onChange={handleChange}
            variant="outlined"
            fullWidth
            disabled={!!selectedCourse}
            error={!!errors.id}
            helperText={errors.id}
          />
          <TextField
            label="Tên môn học"
            name="courseName"
            value={course.courseName}
            onChange={handleChange}
            variant="outlined"
            fullWidth
            error={!!errors.courseName}
            helperText={errors.courseName}
          />
          <TextField
            label="Độ ưu tiên ca học (vd: 3,9,2,8,1,7)"
            name="slotsPriority"
            value={course.slotsPriority}
            onChange={handleChange}
            variant="outlined"
            fullWidth
            error={!!errors.slotsPriority}
            helperText={errors.slotsPriority}
          />
          <TextField
            label="Số giáo viên tối đa"
            name="maxTeacherInCharge"
            value={course.maxTeacherInCharge}
            onChange={handleChange}
            variant="outlined"
            type="number"
            fullWidth
            InputProps={{ inputProps: { min: 1 } }}
            error={!!errors.maxTeacherInCharge}
            helperText={errors.maxTeacherInCharge}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="inherit">
          Hủy
        </Button>
        <Button
          onClick={handleSubmit}
          color="primary"
          variant="contained"
          disabled={isCreating || isUpdating}
          startIcon={
            (isCreating || isUpdating) && <CircularProgress size={16} />
          }
        >
          {selectedCourse ? "Cập nhật" : "Tạo mới"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
