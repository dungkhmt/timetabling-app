import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, { useEffect, useState } from "react";
import { Button, Dialog, DialogContent, DialogTitle, Paper, TextField, Autocomplete, CircularProgress, FormControl, MenuItem, InputLabel, Select, DialogActions, Box } from "@mui/material";
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

    const columns = [
        { field: "id", headerName: "ID" },
        { field: "moduleCode", headerName: "CourseCode" },
        { field: "moduleName", headerName: "CourseName" },
        { field: "programName", headerName: "Program" },
        { field: "classType", headerName: "classType" },
        { field: "mass", headerName: "mass" },
        { field: "numberOfClasses", headerName: "Classes" },
        { field: "duration", headerName: "duration" },
        { field: "qty", headerName: "Qty" },
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

        // alert(JSON.stringify(payLoad));

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
        // alert(JSON.stringify(classPlan));

        setSelectedClassPlan(classPlan);

        setFormData({
            selectedProgram: programs.find((p) => p.id === classPlan.groupId) || null,
            selectedCourse: courses.find((c) => c.id === classPlan.moduleCode) || null,
            courseName:   classPlan.moduleName || null,
            courseCode: classPlan.moduleCode || null,
            promotion: classPlan.promotion || null,
            nbClasses: classPlan.numberOfClasses || 1,
            classType: classPlan.classType || "LT+BT",
            nbStudents: classPlan.qty|| 0 ,
            nbStudentsLTBT: classPlan.lectureExerciseMaxQuantity ,
            nbStudentsLT: classPlan.lectureMaxQuantity ,
            nbStudentsBT: classPlan.exerciseMaxQuantity ,
            learningWeeks: classPlan.learningWeeks ,
            duration: classPlan.duration ,
            durationLTBT: classPlan.durationLTBT || 0,
            durationLT: classPlan.durationLT || 0,
            durationBT: classPlan.durationBT || 0,
            weekType: classPlan.weekType || "0",
            crew: classPlan.crew || "",
            separateLTBT: classPlan.separateLTBT || "N",
            generateClasses: classPlan.generateClasses || "Y",
        });



        if (classPlan.moduleCode) {
            handleCourseChange({ id: classPlan.moduleCode });
        }


        setOpenEditClassPlanDialog(true);

    };

    function handleOpenAddForm() {
        setOpenAddClassPlanDialog(true);
        // alert(JSON.stringify(formData));
        resetForm();
    }

    useEffect(() => {
        getListOpenClassPlans();
        getAllClassesOfBatch();
        getCourses();
        getPrograms();
    }, []);

    const renderForm = (isEdit = false) => (
        <Dialog open={isEdit ? openEditClassPlanDialog : openAddClassPlanDialog} onClose={handleClose} maxWidth="md">
            <DialogTitle>{isEdit ? "Chỉnh sửa kế hoạch mở lớp" : "Kế hoạch mở lớp"}</DialogTitle>
            <DialogContent>
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
                            sx={{ mt: 1, width: 500 }}
                        />
                    )}
                />
                <Autocomplete
                    options={courses || []}
                    getOptionLabel={(option) => (option.id ? `${option.id} - ${option.courseName}` : "")}
                    isOptionEqualToValue={(option, value) => option.id === value?.id}
                    value={{ id: formData.courseCode, courseName: formData.courseName}}
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
                            sx={{ mt: 2, mb: 2, width: 500 }}
                        />
                    )}
                />
                <FormControl sx={{ mt: 1,width:500 }}>
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
                <Box sx={{ display: "flex", gap: 2, mt: 1,width:500 }}>
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
                        sx={{ mt: 1, mb: 1 }}
                    />
                    <TextField
                        label="Số tiết"
                        name="duration"
                        value={formData.duration || ""}
                        onChange={(e) => setFormData((prev) => ({ ...prev, duration: e.target.value }))}
                        fullWidth
                        required
                        placeholder=""
                        error={!!errors.duration}
                        helperText={errors.duration}
                        sx={{ mt: 1, mb: 1 }}
                    />
                </Box>
                <Box sx={{ display: "flex", gap: 2, mt: 1 ,width:500 }}>
                    <TextField
                        label="Số tiết LT+BT"
                        name="durationLTBT"
                        value={formData.durationLTBT || ""}
                        onChange={(e) => setFormData((prev) => ({ ...prev, durationLTBT: e.target.value }))}
                        fullWidth
                        required
                        placeholder=""
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
                        placeholder=""
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
                        placeholder=""
                        error={!!errors.durationBT}
                        helperText={errors.durationBT}
                    />
                </Box>
                <Box sx={{ display: "flex", gap: 2, mt: 2,width: 500 }}>
                    <TextField
                        label="Số SV LT+BT"
                        name="nbStudentsLTBT"
                        value={formData.nbStudentsLTBT || ""}
                        onChange={(e) => setFormData((prev) => ({ ...prev, nbStudentsLTBT: e.target.value }))}
                        fullWidth
                        required
                        placeholder=""
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
                        placeholder=""
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
                        placeholder=""
                        error={!!errors.nbStudentsBT}
                        helperText={errors.nbStudentsBT}
                    />
                </Box>
                <Box sx={{ display: "flex", gap: 2, mt: 2, mb: 1, width: 500 }}>
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
                    <FormControl sx={{width: "100%" }}>
                        <InputLabel>Tách LT-BT</InputLabel>
                        <Select
                            name="separateLTBT"
                            value={formData.separateLTBT}
                            onChange={(e) => setFormData((prev) => ({ ...prev, separateLTBT: e.target.value }))}
                            label="Tách LT-BT"
                        >
                            <MenuItem value="N">N</MenuItem>
                            <MenuItem value="Y">Y</MenuItem>
                        </Select>
                    </FormControl>
                </Box>
                <Box sx={{ display: "flex", gap: 2, mt: 2, alignItems: "flex-start" }}>
                    <TextField
                        label="Khóa"
                        name="promotion"
                        value={formData.promotion || ""}
                        onChange={(e) => setFormData((prev) => ({ ...prev, promotion: e.target.value }))}
                        required
                        error={!!errors.promotion}
                        helperText={errors.promotion}
                        sx={{ width: 80 }}
                    />
                    <FormControl sx={{ width: 125 }}>
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
                    <FormControl sx={{ minWidth: 125 }}>
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
                    <FormControl sx={{ minWidth: 125 }}>
                        <InputLabel>Sinh lớp</InputLabel>
                        <Select
                            name="generateClasses"
                            value={formData.generateClasses}
                            onChange={(e) => setFormData((prev) => ({ ...prev, generateClasses: e.target.value }))}
                            label="Sinh lớp"
                        >
                            <MenuItem value="Y">Y</MenuItem>
                            <MenuItem value="N">N</MenuItem>
                        </Select>
                    </FormControl>
                </Box>
            </DialogContent>
            <DialogActions sx={{ padding: "16px", gap: "8px", borderTop: "1px solid #e0e0e0", backgroundColor: "#fafafa" }}>
                <Button
                    onClick={handleClose}
                    variant="outlined"
                    sx={{ minWidth: "100px", padding: "8px 16px", textTransform: "none" }}
                >
                    Cancel
                </Button>
                <Button
                    onClick={() => handleSave(isEdit)}
                    color="primary"
                    variant="contained"
                    autoFocus
                    sx={{ minWidth: "120px", padding: "8px 16px", textTransform: "none" }}
                    disabled={loading}
                >
                    {loading ? <CircularProgress size={24} /> : "Save"}
                </Button>
            </DialogActions>
        </Dialog>
    );

    return (
        <>
            Opend Class Plan {batchId}
            <Paper sx={{ height: 400, width: "100%" }}>
                <Button onClick={handleOpenAddForm}>ADD</Button>
                <DataGrid
                    initialState={{
                        sorting: { sortModel: [{ field: "id", sort: "asc" }] },
                        filter: { filterModel: { items: [], quickFilterValues: [""] } },
                    }}
                    slots={{ toolbar: GridToolbar }}
                    slotProps={{
                        toolbar: {
                            printOptions: { disableToolbarButton: true },
                            csvOptions: { disableToolbarButton: true },
                            showQuickFilter: true,
                        },
                    }}
                    rows={classPlans}
                    columns={columns}
                    pageSizeOptions={[5, 10]}
                    checkboxSelection
                    onRowClick={handleRowClick}
                />
            </Paper>
            {renderForm(false)}
            {renderForm(true)}
            <ListOpenedClass batchId={batchId} listClasses={classes} />
            <RoomsOfBatch batchId={batchId} />
        </>
    );
}