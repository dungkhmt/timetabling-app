import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, { useEffect, useState } from "react";
import {
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    Paper,
    TextField,
    Autocomplete,
    CircularProgress,
    FormControl,
    MenuItem,
    InputLabel,
    Select,
    DialogActions,
    Box,
    Tabs,
    Chip,
    Tab
} from "@mui/material";
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { request } from "api";
import ListOpenedClass from "./ListOpenedClass";
import RoomsOfBatch from "../batch/RoomsOfBatch";

export default function OpenedClassPlan() {
    const [selectedSemester, setSelectedSemester] = useState(null);
    const { batchId } = useParams();
    const [classPlans, setClassPlans] = useState([]);
    const [classes, setClasses] = useState([]);
    const [openAddClassPlanDialog, setOpenAddClassPlanDialog] = useState(false);
    const [openEditClassPlanDialog, setOpenEditClassPlanDialog] = useState(false);
    const [selectedClassPlan, setSelectedClassPlan] = useState(null);
    const [errors, setErrors] = useState({});
    const [courses, setCourses] = useState([]);
    const [programs, setPrograms] = useState([]);
    const [formData, setFormData] = useState({
        selectedProgram: null,
        selectedCourse: null,
        course: null,
        courseCode: null,
        promotion: null,
        nbClasses: 1,
        classType: "LT+BT",
        nbStudents: 0,
        nbStudentsLTBT: 0,
        nbStudentsLT: 0,
        nbStudentsBT: 0,
        learningWeeks: null,
        duration: 0,
        durationLTBT: 0,
        durationLT: 0,
        durationBT: 0,
        weekType: "0",
        crew: "",
        separateLTBT: "N",
        generateClasses: "Y",
    });
    const [loading, setLoading] = useState(false);
    const [viewTab, setViewTab] = useState(0); // Added missing state for tabs

    const columns = [
        { field: "id", headerName: "ID", width: 70 },
        { field: "moduleCode", headerName: "CourseCode", width: 120 },
        { field: "moduleName", headerName: "CourseName", width: 200 },
        { field: "programName", headerName: "Program", width: 150 },
        { field: "classType", headerName: "classType", width: 100 },
        { field: "mass", headerName: "mass", width: 80 },
        { field: "numberOfClasses", headerName: "Classes", width: 80 },
        { field: "duration", headerName: "duration", width: 80 },
        { field: "qty", headerName: "Qty", width: 80 },
    ];

    function getCourses() {
        request("get", "/course-v2/get-all", (res) => {
            setCourses(res.data);
        });
    }

    function getPrograms() {
        request("get", "/group/get-all-group", (res) => {
            setPrograms(res.data);
        });
    }

    function getAllClassesOfBatch() {
        request("get", `/general-classes/get-all-classes-of-batch?batchId=${batchId}`, (res) => {
            setClasses(res.data);
        });
    }

    function getListOpenClassPlans() {
        request(
            "get",
            `/plan-general-classes/get-opened-class-plans?batch=${batchId}`,
            (res) => {
                const data = res.data.map((i) => ({
                    ...i,
                    qty: i.lectureMaxQuantity + i.exerciseMaxQuantity + i.lectureExerciseMaxQuantity,
                }));
                setClassPlans(data);
            },
            (err) => {
                toast.error("Có lỗi khi truy vấn kế hoạch học tập");
            }
        );
    }

    const resetForm = () => {
        setFormData({
            selectedProgram: null,
            selectedCourse: null,
            course: null,
            courseCode: null,
            promotion: null,
            nbClasses: 1,
            classType: "LT+BT",
            nbStudents: 0,
            nbStudentsLTBT: 0,
            nbStudentsLT: 0,
            nbStudentsBT: 0,
            learningWeeks: null,
            duration: 0,
            durationLTBT: 0,
            durationLT: 0,
            durationBT: 0,
            weekType: "0",
            crew: "",
            separateLTBT: "N",
            generateClasses: "Y",
        });
        setErrors({});
    };

    const handleClose = () => {
        setOpenAddClassPlanDialog(false);
        setOpenEditClassPlanDialog(false);
        resetForm();
    };

    const handleSave = (isEdit = false) => {
        const payLoad = {
            batchId: batchId,
            moduleCode: formData.courseCode,
            classType: formData.classType,
            numberOfClasses: formData.nbClasses,
            learningWeeks: formData.learningWeeks,
            groupId: formData.selectedProgram?.id,
            duration: formData.duration,
            promotion: formData.promotion,
            crew: formData.crew,
            quantityMax: formData.nbStudents,
            weekType: formData.weekType,
            separateLTBT: formData.separateLTBT,
            generateClasses: formData.generateClasses,
            lectureMaxQuantity: formData.nbStudentsLT,
            exerciseMaxQuantity: formData.nbStudentsBT,
            lectureExerciseMaxQuantity: formData.nbStudentsLTBT,
            durationLTBT: formData.durationLTBT,
            durationLT: formData.durationLT,
            durationBT: formData.durationBT,
            ...(isEdit && { id: selectedClassPlan?.id }),
        };

        setLoading(true);
        const method = isEdit ? "put" : "post";
        const url = isEdit
            ? `/plan-general-classes/update-class-openning-plan`
            : `/plan-general-classes/create-class-openning-plan`;

        request(
            method,
            url,
            (res) => {
                setLoading(false);
                if (res.data) {
                    getListOpenClassPlans();
                    getAllClassesOfBatch();
                    toast.success(isEdit ? "Cập nhật kế hoạch lớp thành công!" : "Tạo kế hoạch lớp mới thành công!");
                    handleClose();
                }
            },
            null,
            payLoad
        ).catch((err) => {
            console.error("Unexpected error:", err);
            setLoading(false);
            toast.error(err.response?.data || "Có lỗi xảy ra!");
        });
    };

    const handleChangeClassType = (newClassType) => {
        setFormData((prev) => ({
            ...prev,
            classType: newClassType,
            durationLTBT: newClassType === "LT+BT" && formData.course ? formData.course.durationLtBt : prev.durationLTBT,
            nbStudentsLTBT: newClassType === "LT+BT" && formData.course ? formData.course.maxStudentLTBT : prev.nbStudentsLTBT,
            durationLT: newClassType === "LT" && formData.course ? formData.course.durationLt : prev.durationLT,
            nbStudentsLT: newClassType === "LT" && formData.course ? formData.course.maxStudentLT : prev.nbStudentsLT,
            durationBT: newClassType === "BT" && formData.course ? formData.course.durationBt : prev.durationBT,
            nbStudentsBT: newClassType === "BT" && formData.course ? formData.course.maxStudentBT : prev.nbStudentsBT,
        }));
    };

    const handleCourseChange = (newSelectedCourse) => {
        setFormData((prev) => ({
            ...prev,
            selectedCourse: newSelectedCourse,
            courseCode: newSelectedCourse ? newSelectedCourse.id : null,
            courseName: newSelectedCourse ? newSelectedCourse.courseName : null,
        }));
        if (newSelectedCourse) {
            setLoading(true);
            request(
                "get",
                `/course-v2/${newSelectedCourse.id}`,
                (res) => {
                    setLoading(false);
                    if (res.data) {
                        setFormData((prev) => ({
                            ...prev,
                            course: res.data,
                            separateLTBT: res.data.separateLTBT,
                            durationLTBT: prev.classType === "LT+BT" ? res.data.durationLtBt : prev.durationLTBT,
                            nbStudentsLTBT: prev.classType === "LT+BT" ? res.data.maxStudentLTBT : prev.nbStudentsLTBT,
                            durationLT: prev.classType === "LT" ? res.data.durationLt : prev.durationLT,
                            nbStudentsLT: prev.classType === "LT" ? res.data.maxStudentLT : prev.nbStudentsLT,
                            durationBT: prev.classType === "BT" ? res.data.durationBt : prev.durationBT,
                            nbStudentsBT: prev.classType === "BT" ? res.data.maxStudentBT : prev.nbStudentsBT,
                        }));
                    }
                },
                null
            ).catch((err) => {
                console.error("Unexpected error:", err);
                setLoading(false);
                toast.error(err.response?.data);
            });
        }
    };

    const handleRowClick = (params) => {
        const classPlan = params.row;
        setSelectedClassPlan(classPlan);

        setFormData({
            selectedProgram: programs.find((p) => p.id === classPlan.groupId) || null,
            selectedCourse: courses.find((c) => c.id === classPlan.moduleCode) || null,
            courseName: classPlan.moduleName || null,
            courseCode: classPlan.moduleCode || null,
            promotion: classPlan.promotion || null,
            nbClasses: classPlan.numberOfClasses || 1,
            classType: classPlan.classType || "LT+BT",
            nbStudents: classPlan.qty || 0,
            nbStudentsLTBT: classPlan.lectureExerciseMaxQuantity || 0,
            nbStudentsLT: classPlan.lectureMaxQuantity || 0,
            nbStudentsBT: classPlan.exerciseMaxQuantity || 0,
            learningWeeks: classPlan.learningWeeks || null,
            duration: classPlan.duration || 0,
            durationLTBT: classPlan.durationLTBT || 0,
            durationLT: classPlan.durationLT || 0,
            durationBT: classPlan.durationBT || 0,
            weekType: classPlan.weekType || "0",
            crew: classPlan.crew || "",
            separateLTBT: classPlan.separateLTBT || "N",
            generateClasses: classPlan.generateClasses || "Y",
        });

        if (classPlan.moduleCode) {
            handleCourseChange({ id: classPlan.moduleCode, courseName: classPlan.moduleName });
        }

        setOpenEditClassPlanDialog(true);
    };

    function handleOpenAddForm() {
        setOpenAddClassPlanDialog(true);
        resetForm();
    }

    useEffect(() => {
        getListOpenClassPlans();
        getAllClassesOfBatch();
        getCourses();
        getPrograms();
    }, []);

    const renderForm = (isEdit = false) => (
        <Dialog open={isEdit ? openEditClassPlanDialog : openAddClassPlanDialog} onClose={handleClose} maxWidth="md" fullWidth>
            <DialogTitle>{isEdit ? "Chỉnh sửa kế hoạch mở lớp" : "Kế hoạch mở lớp"}</DialogTitle>
            <DialogContent>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Autocomplete
                        options={programs || []}
                        getOptionLabel={(option) => option.groupName || ""}
                        isOptionEqualToValue={(option, value) => option.id === value?.id}
                        value={formData.selectedProgram}
                        onChange={(_, newValue) => setFormData((prev) => ({ ...prev, selectedProgram: newValue }))}
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                label="Nhóm"
                                required
                                error={!!errors.groupName}
                                helperText={errors.groupName}
                                InputProps={{
                                    ...params.InputProps,
                                    endAdornment: (
                                        <>
                                            {loading ? <CircularProgress size={20} /> : null}
                                            {params.InputProps.endAdornment}
                                        </>
                                    ),
                                }}
                            />
                        )}
                    />
                    <Autocomplete
                        options={courses || []}
                        getOptionLabel={(option) => (option.id ? `${option.id} - ${option.courseName}` : "")}
                        isOptionEqualToValue={(option, value) => option.id === value?.id}
                        value={formData.selectedCourse}
                        onChange={(_, newValue) => handleCourseChange(newValue)}
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
                                            {loading ? <CircularProgress size={20} /> : null}
                                            {params.InputProps.endAdornment}
                                        </>
                                    ),
                                }}
                            />
                        )}
                    />
                    <FormControl fullWidth>
                        <InputLabel>Kiểu lớp</InputLabel>
                        <Select
                            name="classType"
                            value={formData.classType}
                            onChange={(e) => handleChangeClassType(e.target.value)}
                            label="Kiểu lớp"
                        >
                            <MenuItem value="LT+BT">LT+BT</MenuItem>
                            <MenuItem value="LT">LT</MenuItem>
                            <MenuItem value="BT">BT</MenuItem>
                        </Select>
                    </FormControl>
                    <Box sx={{ display: "flex", gap: 2 }}>
                        <TextField
                            label="Tuần học *"
                            name="learningWeeks"
                            value={formData.learningWeeks || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, learningWeeks: e.target.value }))}
                            fullWidth
                            required
                            placeholder="Ví dụ: 2-9,11-18"
                            error={!!errors.learningWeeks}
                            helperText={errors.learningWeeks}
                        />
                        <TextField
                            label="Số tiết"
                            name="duration"
                            value={formData.duration || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, duration: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.duration}
                            helperText={errors.duration}
                        />
                    </Box>
                    <Box sx={{ display: "flex", gap: 2 }}>
                        <TextField
                            label="Số tiết LT+BT"
                            name="durationLTBT"
                            value={formData.durationLTBT || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, durationLTBT: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.durationLTBT}
                            helperText={errors.durationLTBT}
                        />
                        <TextField
                            label="Số tiết LT"
                            name="durationLT"
                            value={formData.durationLT || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, durationLT: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.durationLT}
                            helperText={errors.durationLT}
                        />
                        <TextField
                            label="Số tiết BT"
                            name="durationBT"
                            value={formData.durationBT || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, durationBT: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.durationBT}
                            helperText={errors.durationBT}
                        />
                    </Box>
                    <Box sx={{ display: "flex", gap: 2 }}>
                        <TextField
                            label="Số SV LT+BT"
                            name="nbStudentsLTBT"
                            value={formData.nbStudentsLTBT || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, nbStudentsLTBT: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.nbStudentsLTBT}
                            helperText={errors.nbStudentsLTBT}
                        />
                        <TextField
                            label="Số SV LT"
                            name="nbStudentsLT"
                            value={formData.nbStudentsLT || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, nbStudentsLT: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.nbStudentsLT}
                            helperText={errors.nbStudentsLT}
                        />
                        <TextField
                            label="Số SV BT"
                            name="nbStudentsBT"
                            value={formData.nbStudentsBT || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, nbStudentsBT: e.target.value }))}
                            fullWidth
                            required
                            error={!!errors.nbStudentsBT}
                            helperText={errors.nbStudentsBT}
                        />
                    </Box>
                    <Box sx={{ display: "flex", gap: 2 }}>
                        <TextField
                            label="Số lượng lớp"
                            name="nbClasses"
                            value={formData.nbClasses || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, nbClasses: e.target.value }))}
                            type="number"
                            fullWidth
                            required
                            error={!!errors.nbClasses}
                            helperText={errors.nbClasses}
                        />
                        <TextField
                            label="Số lượng sinh viên"
                            name="nbStudents"
                            value={formData.nbStudents || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, nbStudents: e.target.value }))}
                            type="number"
                            fullWidth
                            required
                            error={!!errors.nbStudents}
                            helperText={errors.nbStudents}
                        />
                        <FormControl fullWidth>
                            <InputLabel>Tách LT-BT</InputLabel>
                            <Select
                                name="separateLTBT"
                                value={formData.separateLTBT}
                                onChange={(e) => setFormData((prev) => ({ ...prev, separateLTBT: e.target.value }))}
                                label="Tách LT-BT"
                            >
                                <MenuItem value="N">Không</MenuItem>
                                <MenuItem value="Y">Có</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                    <Box sx={{ display: "flex", gap: 2 }}>
                        <TextField
                            label="Khóa"
                            name="promotion"
                            value={formData.promotion || ""}
                            onChange={(e) => setFormData((prev) => ({ ...prev, promotion: e.target.value }))}
                            required
                            error={!!errors.promotion}
                            helperText={errors.promotion}
                            sx={{ flex: 1 }}
                        />
                        <FormControl sx={{ flex: 1 }}>
                            <InputLabel>Kiểu tuần</InputLabel>
                            <Select
                                name="weekType"
                                value={formData.weekType}
                                onChange={(e) => setFormData((prev) => ({ ...prev, weekType: e.target.value }))}
                                label="Kiểu tuần"
                            >
                                <MenuItem value="0">AB</MenuItem>
                                <MenuItem value="1">A</MenuItem>
                                <MenuItem value="2">B</MenuItem>
                            </Select>
                        </FormControl>
                        <FormControl sx={{ flex: 1 }}>
                            <InputLabel>Kíp</InputLabel>
                            <Select
                                name="crew"
                                value={formData.crew}
                                onChange={(e) => setFormData((prev) => ({ ...prev, crew: e.target.value }))}
                                label="Kíp"
                            >
                                <MenuItem value="S">Sáng</MenuItem>
                                <MenuItem value="C">Chiều</MenuItem>
                            </Select>
                        </FormControl>
                        <FormControl sx={{ flex: 1 }}>
                            <InputLabel>Sinh lớp</InputLabel>
                            <Select
                                name="generateClasses"
                                value={formData.generateClasses}
                                onChange={(e) => setFormData((prev) => ({ ...prev, generateClasses: e.target.value }))}
                                label="Sinh lớp"
                            >
                                <MenuItem value="Y">Có</MenuItem>
                                <MenuItem value="N">Không</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} variant="outlined">
                    Hủy
                </Button>
                <Button
                    onClick={() => handleSave(isEdit)}
                    color="primary"
                    variant="contained"
                    disabled={loading}
                >
                    {loading ? <CircularProgress size={24} /> : "Lưu"}
                </Button>
            </DialogActions>
        </Dialog>
    );

    return (
        <div className="flex flex-col gap-3">
            <Paper elevation={1} className="p-3">
                <Tabs
                    value={viewTab}
                    onChange={(e, newVal) => setViewTab(newVal)}
                    sx={{
                        borderBottom: 1,
                        borderColor: "divider",
                        "& .MuiTab-root": {
                            minWidth: "140px",
                            fontWeight: 500,
                            textTransform: "none",
                            fontSize: "15px",
                            py: 1.5,
                        },
                    }}
                >
                    <Tab
                        label={
                            <div className="flex items-center gap-2">
                                <span>Kế hoạch lớp</span>
                                <Chip
                                    size="small"
                                    label={classPlans?.length || 0}
                                    color="default"
                                />
                            </div>
                        }
                    />
                    <Tab
                        label={
                            <div className="flex items-center gap-2">
                                <span>Lớp đã mở</span>
                                <Chip
                                    size="small"
                                    label={classes?.length || 0}
                                    color="default"
                                />
                            </div>
                        }
                    />
                    <Tab
                        label={
                            <div className="flex items-center gap-2">
                                <span>Phòng học</span>
                            </div>
                        }
                    />
                </Tabs>

                {viewTab === 0 && (
                    <div className="mt-3">
                        <Paper variant="outlined" className="p-3">
                            <Button onClick={handleOpenAddForm} variant="contained" sx={{ mb: 2 }}>
                                Thêm kế hoạch
                            </Button>
                            <div style={{ height: 500, width: '100%' }}>
                                <DataGrid
                                    rows={classPlans}
                                    columns={columns}
                                    initialState={{
                                        sorting: { sortModel: [{ field: "id", sort: "asc" }] },
                                        pagination: { paginationModel: { pageSize: 10 } },
                                    }}
                                    pageSizeOptions={[5, 10, 25]}
                                    slots={{ toolbar: GridToolbar }}
                                    slotProps={{
                                        toolbar: {
                                            showQuickFilter: true,
                                            printOptions: { disableToolbarButton: true },
                                            csvOptions: { disableToolbarButton: true },
                                        },
                                    }}
                                    onRowClick={handleRowClick}
                                />
                            </div>
                        </Paper>
                        {renderForm(false)}
                        {renderForm(true)}
                    </div>
                )}

                {viewTab === 1 && (
                    <div className="mt-3">
                        <Paper variant="outlined" className="p-3">
                            <ListOpenedClass batchId={batchId} listClasses={classes} />
                        </Paper>
                    </div>
                )}

                {viewTab === 2 && (
                    <div className="mt-3">
                        <Paper variant="outlined" className="p-3">
                            <RoomsOfBatch batchId={batchId} />
                        </Paper>
                    </div>
                )}
            </Paper>
        </div>
    );
}