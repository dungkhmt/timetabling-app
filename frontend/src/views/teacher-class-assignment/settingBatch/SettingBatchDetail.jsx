import React, {useEffect, useState} from "react";
import {
    Box,
    Paper,
    Typography,
    Tabs,
    Tab,
    FormControl,
    Select,
    InputLabel,
    MenuItem,
    Stack,
    Chip,
    Button,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Card,
    CardContent,
    Grid
} from "@mui/material";
import {request} from "api";
import {useParams} from "react-router-dom";

function TabPanel({ children, value, index, ...other }) {
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`tabpanel-${index}`}
            aria-labelledby={`tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
        </div>
    );
}

export default function SettingBatchDetail() {
    const [tabValue, setTabValue] = useState(0);
    const [selectedSchool, setSelectedSchool] = useState(null);
    const {batchId} = useParams();
    const [schools, setSchools] = useState([]);
    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState("");
    const [selectedCourses, setSelectedCourses] = useState([]);
    const [teachers, setTeachers] = useState([]);
    const [selectedTeacher, setSelectedTeacher] = useState("");
    const [selectedTeachers, setSelectedTeachers] = useState([]);
    const [batchData, setBatchData] = useState([]);
    const [batchInfo, setBatchInfo] = useState({});
    const [classes, setClasses] = useState([]);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    // Lấy thông tin batch
    const getBatchInfo = () => {
        request(
            "get",
            `/teacher-assignment-batch/get-batch/${batchId}`,
            (res) => {
                console.log("Batch info:", res);
                setBatchInfo(res.data || {});
                if (res.data && res.data.batchTeachers) {
                    console.log("Batch teachers:", res.data.batchTeachers);

                    // Tự động thêm các giáo viên đã có vào selectedTeachers
                    const existingTeachers = res.data.batchTeachers.map(bt => bt.teacher);
                    setSelectedTeachers(existingTeachers);
                }
            },
            (error) => {
                console.error("Error fetching batch info:", error);
            }
        );

        request(
            "post",
            `/teacher-assignment/assign-classes-for-teachers/${batchId}`,
            (res) => {
                console.log("Batch info:", res);

            },
            (error) => {
                console.error("Error fetching batch info:", error);
            }
        );

    };

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

    function getAllClassesByBatch(){
        request(
            "get",
            `/teacher-assignment-opened-class/get-all-classes-by-batch/${batchId}`,
            (res) => {
                console.log(res);
                setClasses(res.data || []);
            },
            (error) => {
                console.error(error);
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

        const courseToAdd = courses.find(course => course.courseId === courseId);
        if (courseToAdd && !selectedCourses.some(c => c.courseId === courseId)) {
            setSelectedCourses([...selectedCourses, courseToAdd]);
        }

        setSelectedCourse("");
    };

    const handleTeacherChange = (event) => {
        const teacherId = event.target.value;
        setSelectedTeacher(teacherId);

        // Tìm giáo viên theo ID và thêm vào danh sách đã chọn
        const teacherToAdd = teachers.find(teacher => teacher.id === teacherId);
        if (teacherToAdd && !selectedTeachers.some(t => t.id === teacherId)) {
            setSelectedTeachers([...selectedTeachers, teacherToAdd]);
        }

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

    // Lấy dữ liệu batch (giáo viên và khóa học đã gán)
    const getBatchData = () => {
        request(
            "get",
            `/batch/${batchId}/details`,
            (res) => {
                console.log("Batch data:", res);
                setBatchData(res.data || []);
            },
            (error) => {
                console.error("Error fetching batch data:", error);
            }
        );
    }

    const handleRemoveCourse = (courseToRemove) => {
        setSelectedCourses(selectedCourses.filter(course => course.courseId !== courseToRemove.courseId));
    };

    const handleRemoveTeacher = (teacherToRemove) => {
        setSelectedTeachers(selectedTeachers.filter(teacher => teacher.id !== teacherToRemove.id));
    };

    const handleSaveTeacher = () => {
        if (selectedSchool && selectedTeachers.length > 0) {
            const payload = {
                batchId: batchId,
                teacherIds: selectedTeachers.map(teacher => teacher.id),
            };

            // alert(JSON.stringify(payload));

            selectedTeachers.forEach(teacher => {
                request(
                    "post",
                    `/teacher-assignment-batch-teacher/create-batch-teacher/${batchId}/${teacher.id}`,
                    payload,
                    (res) => {
                        console.log("Teachers added successfully:", res);

                    },
                    (error) => {
                        console.error("Error adding teachers:", error);
                    }
                );
            })
            setSelectedTeachers([]);


        }
    };

    const handleSaveCourse = () => {
        if (selectedSchool && selectedCourses.length > 0) {
            const payload = {
                semester: batchInfo.semester,
                batchId: batchId,
                courseIds: selectedCourses.map(course => course.courseId),
            };

            // alert(JSON.stringify(payload));

            request(
                "post",
                `/teacher-assignment-opened-class/add-class-to-batch-based-on-courses`,
                (res) => {
                    console.log("Courses added successfully:", res);
                    setSelectedCourses([]);
                    getBatchData(); // Refresh data
                    alert("Thêm khóa học thành công!");
                },
                (error) => {
                    console.error("Error adding courses:", error);
                    alert("Có lỗi xảy ra khi thêm khóa học!");
                },
                payload
            );
        }
    };

    useEffect(() => {
        getBatchInfo();
        getAllSchools();
        getAllTeacher();
        getBatchData();
        getAllClassesByBatch();
    }, [batchId]);

    return (
        <Box sx={{ p: 3 }}>

            {/* Tabs */}
            <Paper sx={{ width: '100%' }}>
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={tabValue} onChange={handleTabChange}>
                        <Tab label="Danh sách giáo viên & khóa học" />
                        <Tab label="Thêm Giáo viên" />
                        <Tab label="Thêm Khóa học" />
                    </Tabs>
                </Box>

                {/* Tab 1: Data Grid */}
                <TabPanel value={tabValue} index={0}>
                    <Grid container spacing={3}>
                        {/* Danh sách giáo viên */}
                        <Grid item xs={12} md={6}>
                            <Typography variant="h6" gutterBottom>
                                Giáo viên trong batch
                            </Typography>
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>ID</TableCell>
                                            <TableCell>Tên giáo viên</TableCell>
                                            <TableCell>Email</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {batchInfo.batchTeachers && batchInfo.batchTeachers.length > 0 ? (
                                            batchInfo.batchTeachers.map((batchTeacher) => (
                                                <TableRow key={batchTeacher.id.teacherUserId}>
                                                    <TableCell>{batchTeacher.id.teacherUserId}</TableCell>
                                                    <TableCell>{batchTeacher.teacher.teacherName}</TableCell>
                                                    <TableCell>{batchTeacher.id.teacherUserId}</TableCell>
                                                </TableRow>
                                            ))
                                        ) : (
                                            <TableRow>
                                                <TableCell colSpan={3} align="center">
                                                    Chưa có giáo viên nào
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Grid>

                        {/* Danh sách khóa học */}
                        <Grid item xs={12} md={6}>
                            <Typography variant="h6" gutterBottom>
                                Khóa học trong batch
                            </Typography>
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>ID</TableCell>
                                            <TableCell>Tên khóa học</TableCell>
                                            <TableCell>Trường</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {batchData.courses && batchData.courses.length > 0 ? (
                                            batchData.courses.map((course) => (
                                                <TableRow key={course.courseId}>
                                                    <TableCell>{course.courseId}</TableCell>
                                                    <TableCell>{course.courseName}</TableCell>
                                                    <TableCell>{course.school}</TableCell>
                                                </TableRow>
                                            ))
                                        ) : (
                                            <TableRow>
                                                <TableCell colSpan={3} align="center">
                                                    Chưa có khóa học nào
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Grid>
                    </Grid>
                </TabPanel>

                {/*Tab 2: Add Teacher*/}
                <TabPanel value={tabValue} index={1}>
                    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
                        <FormControl fullWidth sx={{ mb: 3 }}>
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

                        {teachers.length > 0 && (
                            <>
                                <FormControl fullWidth sx={{ mb: 3 }}>
                                    <InputLabel>Chọn giáo viên</InputLabel>
                                    <Select
                                        value={selectedTeacher}
                                        onChange={handleTeacherChange}
                                        label="Chọn giáo viên"
                                    >
                                        <MenuItem value="" disabled>
                                            <em>Vui lòng chọn giáo viên</em>
                                        </MenuItem>
                                        {teachers
                                            .filter(teacher => !selectedTeachers.some(selected => selected.id === teacher.id)) // Lọc những giáo viên chưa được chọn
                                            .map(teacher => (
                                                <MenuItem key={teacher.id} value={teacher.id}>
                                                    {teacher.teacherName} - {teacher.id}
                                                </MenuItem>
                                            ))
                                        }
                                    </Select>
                                </FormControl>
                            </>
                        )}

                        {/* Hiển thị danh sách giáo viên đã chọn */}
                        {selectedTeachers.length > 0 && (
                            <Box mt={2}>
                                <Typography variant="h6" gutterBottom>
                                    Giáo viên đã chọn:
                                </Typography>
                                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                                    {selectedTeachers.map(teacher => (
                                        <Chip
                                            key={teacher.id}
                                            label={teacher.teacherName}
                                            onDelete={() => handleRemoveTeacher(teacher)}
                                            color="secondary"
                                            variant="filled"
                                            style={{ marginBottom: '8px' }}
                                        />
                                    ))}
                                </Stack>
                                <Button
                                    onClick={handleSaveTeacher}
                                    color="primary"
                                    variant="contained"
                                    disabled={!selectedSchool || selectedTeachers.length === 0}
                                    sx={{ mt: 2 }}
                                >
                                    Lưu Giáo viên
                                </Button>
                            </Box>
                        )}
                    </Box>
                </TabPanel>

                {/* Tab 3: Add Course */}
                <TabPanel value={tabValue} index={2}>
                    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
                        <FormControl fullWidth sx={{ mb: 3 }}>
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
                            <>
                                <FormControl fullWidth sx={{ mb: 3 }}>
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
                                                {course.courseId + " - " + course.courseName}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </>
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
                                            label={`${course.courseId} - ${course.courseName}`}
                                            onDelete={() => handleRemoveCourse(course)}
                                            color="primary"
                                            variant="filled"
                                            style={{ marginBottom: '8px' }}
                                        />
                                    ))}
                                </Stack>
                                <Button
                                    onClick={handleSaveCourse}
                                    color="primary"
                                    variant="contained"
                                    disabled={!selectedSchool || selectedCourses.length === 0}
                                    sx={{ mt: 2 }}
                                >
                                    Lưu Khóa học
                                </Button>
                            </Box>
                        )}
                    </Box>
                </TabPanel>
            </Paper>
        </Box>
    );
}