import React, {useEffect, useState} from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    Stack, Chip, Typography, FormControl, Select, InputLabel, MenuItem, TextField
} from "@mui/material";
import {request} from "../../../../api";
import {Box} from "@mui/system";

export default function SettingBatchDialog({ open, onClose, selectedBatch }) {
    const [selectedSchool, setSelectedSchool] = useState(null);
    const [schools, setSchools] = useState([]);
    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState(""); // For the course select dropdown
    const [selectedCourses, setSelectedCourses] = useState([]); // For the course stack
    const [teachers, setTeachers] = useState([]);
    const [selectedTeacher, setSelectedTeacher] = useState(""); // For the teacher select dropdown
    const [selectedTeachers, setSelectedTeachers] = useState([]); // For the teacher stack

    function getAllSchools() {
        request(
            "get",
            "/studying-course/get-all-school",
            (res) => {
                console.log(res);
                setSchools(res.data || []);
            },
            (error) => {
                console.error(error);
            }
        );
    }

    function getCoursesBySchool(schoolId) {
        request(
            "get",
            `/studying-course/get-courses-by-school/${schoolId}`,
            (res) => {
                console.log("Courses response:", res);
                setCourses(res.data || []);
            },
            (error) => {
                console.error("Error fetching courses:", error);
            }
        );
    }

    const handleSchoolChange = (event) => {
        const schoolName = event.target.value;
        setSelectedSchool(schoolName);

        if (schoolName) {
            getCoursesBySchool(schoolName);
        } else {
            setCourses([]);
        }
        setSelectedCourses([]);
        setSelectedTeachers([]);
    };

    const handleCourseChange = (event) => {
        const courseId = event.target.value;
        setSelectedCourse(courseId);

        // Find the selected course object and add to stack
        const courseToAdd = courses.find(course => course.courseId === courseId);
        if (courseToAdd && !selectedCourses.some(c => c.courseId === courseId)) {
            setSelectedCourses([...selectedCourses, courseToAdd]);
        }

        // Reset the select dropdown
        setSelectedCourse("");
    };

    const handleTeacherChange = (event) => {
        const teacherId = event.target.value;
        setSelectedTeacher(teacherId);

        // Find the selected teacher object and add to stack
        const teacherToAdd = teachers.find(teacher => teacher.id === teacherId);
        if (teacherToAdd && !selectedTeachers.some(t => t.id === teacherId)) {
            setSelectedTeachers([...selectedTeachers, teacherToAdd]);
        }

        // Reset the select dropdown
        setSelectedTeacher("");
    };

    const getAllTeacher = () => {
        request(
            "get",
            "/teacher/get-all-teacher",
            (res) => {
                console.log(res);
                setTeachers(res.data || []);
            },
            (error) => {
                console.error(error);
            }
        );
    }

    const handleRemoveCourse = (courseToRemove) => {
        setSelectedCourses(selectedCourses.filter(course => course.courseId  !== courseToRemove.courseId ));
    };

    const handleRemoveTeacher = (teacherToRemove) => {
        setSelectedTeachers(selectedTeachers.filter(teacher => teacher.id  !== teacherToRemove.id ));
    };

    const handleSave = () => {
        if (selectedSchool) {
            const payload = {
                batchId: selectedBatch.id,
                courseIds: selectedCourses.map(course => course.courseId),
                teacherIds: selectedTeachers.map(teacher => teacher.id),
            };

            alert(JSON.stringify(payload));

            // request(
            //     "post",
            //     `/teacher-assignment-batch-class/create-batch-class/${payload.batchId}/${payload.classId}`, // Thay bằng endpoint thực tế
            //     (res) => {
            //         console.log("Request thành công:", res);
            //
            //     },
            //     (error) => {
            //         console.error("Request thất bại:", error);
            //
            //     },
            // );

            console.log("Selected School:", selectedSchool);
            console.log("Selected Courses:", selectedCourses);
            console.log("Selected Teachers:", selectedTeachers);
            setSelectedSchool(null);
            setSelectedCourses([]);
            setSelectedTeachers([]);
            setCourses([]);
            onClose();
        }
    };

    const handleClose = () => {
        setSelectedSchool(null);
        setSelectedCourses([]);
        setSelectedTeachers([]);
        setCourses([]);
        onClose();
    };

    useEffect(() => {
        if (open) {
            getAllSchools();
            getAllTeacher();
        }
    }, [open]);

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                Cài đặt Batch: {selectedBatch?.name}
            </DialogTitle>

            <DialogContent>
                <FormControl fullWidth sx={{ mt: 2 }}>
                    <InputLabel>Chọn trường *</InputLabel>
                    <Select
                        value={selectedSchool}
                        onChange={handleSchoolChange}
                        label="Chọn trường *"
                    >
                        <MenuItem value="" disabled>
                            <em>Vui lòng chọn trường</em>
                        </MenuItem>
                        {schools.map((school, index) => (
                            <MenuItem key={index} value={school}>
                                {school}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>

                {courses.length > 0 && (
                    <Box mt={2}>
                        <Typography variant="h6" gutterBottom>
                            Chọn khóa học:
                        </Typography>
                        <FormControl fullWidth>
                            <InputLabel>Chọn khóa học</InputLabel>
                            <Select
                                value={selectedCourse}
                                onChange={handleCourseChange}
                                label="Chọn khóa học"
                            >
                                <MenuItem value="" disabled>
                                    <em>Vui lòng chọn khóa học</em>
                                </MenuItem>
                                {courses.map(course => (
                                    <MenuItem key={course.courseId} value={course.courseId}>
                                        {course.courseId + " - "+course.courseName}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Box>
                )}

                {selectedCourses.length > 0 && (
                    <Box mt={2}>
                        <Typography variant="h6" gutterBottom>
                            Khóa học đã chọn:
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                            {selectedCourses.map(course => (
                                <Chip
                                    key={course.courseId}
                                    label={`${course.courseId}`}
                                    onDelete={() => handleRemoveCourse(course)}
                                    color="primary"
                                    variant="filled"
                                    style={{ marginBottom: '8px' }}
                                />
                            ))}
                        </Stack>
                    </Box>
                )}

                {teachers.length > 0 && (
                    <Box mt={2}>
                        <Typography variant="h6" gutterBottom>
                            Chọn giáo viên:
                        </Typography>
                        <FormControl fullWidth>
                            <InputLabel>Chọn giáo viên</InputLabel>
                            <Select
                                value={selectedTeacher}
                                onChange={handleTeacherChange}
                                label="Chọn giáo viên"
                            >
                                <MenuItem value="" disabled>
                                    <em>Vui lòng chọn giáo viên</em>
                                </MenuItem>
                                {teachers.map(teacher => (
                                    <MenuItem key={teacher.id} value={teacher.id}>
                                        {teacher.id + " - " + teacher.teacherName}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Box>
                )}

                {selectedTeachers.length > 0 && (
                    <Box mt={2}>
                        <Typography variant="h6" gutterBottom>
                            Giáo viên đã chọn:
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                            {selectedTeachers.map(teacher => (
                                <Chip
                                    key={teacher.id}
                                    label={`${teacher.teacherName}`}
                                    onDelete={() => handleRemoveTeacher(teacher)}
                                    color="secondary"
                                    variant="filled"
                                    style={{ marginBottom: '8px' }}
                                />
                            ))}
                        </Stack>
                    </Box>
                )}
            </DialogContent>

            <DialogActions>
                <Button onClick={handleClose} color="secondary">
                    Hủy
                </Button>
                <Button
                    onClick={handleSave}
                    color="primary"
                    variant="contained"
                    disabled={!selectedSchool}
                >
                    Lưu
                </Button>
            </DialogActions>
        </Dialog>
    );
}