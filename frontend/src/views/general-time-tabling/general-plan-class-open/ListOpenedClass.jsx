import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, { useEffect, useState } from "react";
import {
    Button, Dialog, DialogContent, DialogTitle, Paper, TextField, Autocomplete, CircularProgress,
    FormControl, MenuItem, InputLabel, Select, DialogActions, Box
} from "@mui/material";
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { request } from "api";

export default function ListOpenedClass({ batchId, listClasses }) {
    const [classes, setClasses] = useState([]);
    const [clusters, setClusters] = useState([]);
    const [selectedCluster, setSelectedCluster] = useState(null);
    const [openEditDialog, setOpenEditDialog] = useState(false);
    const [selectedClass, setSelectedClass] = useState(null);
    const [programs, setPrograms] = useState([]);
    const [courses, setCourses] = useState([]);
    const [loadingCourses, setLoadingCourses] = useState(false);

    const columns = [
        { title: "Code", field: "classCode" },
        { title: "Course", field: "moduleCode" },
        { title: "Type", field: "classType" },
        { title: "So tiet", field: "duration" },
        { title: "Kip", field: "crew" },
        { title: "Ma KH", field: "refClassId" },
    ];

    function getPrograms() {
        request("get", "/group/get-all-group", (res) => {
            setPrograms(res.data);
        });
    }

    function getCourses() {
        setLoadingCourses(true);
        request("get", "/course-v2/get-all", (res) => {
            setCourses(res.data);
            setLoadingCourses(false);
        }, {
            onError: () => {
                setLoadingCourses(false);
            }
        });
    }

    const handleRowClick = (row) => {
        setSelectedClass(row.row);
        setOpenEditDialog(true);
    };

    const handleCloseEditDialog = () => {
        setOpenEditDialog(false);
        setSelectedClass(null);
    };

    const handleSaveClass = () => {
        // alert(JSON.stringify(selectedClass));

        request(
            "put",
            `/plan-general-classes/update-class-openning`,
            (res) => {
                toast.success("Class updated successfully");
                getAllClassesOfBatch();
                handleCloseEditDialog();
            },
            {
                onError: (e) => {
                    toast.error("Failed to update class");
                }
            },
            selectedClass
        );
    };

    const handleInputChange = (field, value) => {
        setSelectedClass(prev => ({
            ...prev,
            [field]: value
        }));
    };

    function getClustersOfBatch() {
        request(
            "get",
            "/general-classes/get-clusters-by-batch?batchId=" + batchId,
            (res) => {
                setClusters(res.data);
            }
        );
    }

    function getAllClassesOfBatch() {
        request(
            "get",
            "/general-classes/get-all-classes-of-batch?batchId=" + batchId,
            (res) => {
                setClasses(res.data);
            }
        );
    }

    function performClustering() {
        let body = {
            semester: "",
            batchId: batchId
        };
        request(
            "post",
            "/general-classes/compute-class-cluster",
            (res) => {
                console.log('compute cluster returned ', res.data);
                getClustersOfBatch();
            },
            {
                onError: (e) => {
                }
            },
            body
        );
    }

    function getClassesOfCluster(clusterId) {
        request(
            "get",
            "/general-classes/get-by-cluster/" + clusterId,
            (res) => {
                setClasses(res.data);
            }
        );
    }

    function handleChangeCluster(newValue) {
        if (newValue) {
            getClassesOfCluster(newValue.id);
            setSelectedCluster(newValue);
        } else {
            getAllClassesOfBatch();
            setSelectedCluster(null);
        }
    }

    useEffect(() => {
        getAllClassesOfBatch();
        getClustersOfBatch();
        getPrograms();
        getCourses();
    }, []);

    return (
        <>
            Danh sách lớp {batchId}
            <Button onClick={() => { performClustering() }}>
                Phân cụm
            </Button>
            <Autocomplete
                options={clusters}
                getOptionLabel={(option) => `${option.id} - ${option.name}`}
                isOptionEqualToValue={(option, value) => option.id === value.id}
                value={selectedCluster}
                onChange={(_, newValue) => {
                    handleChangeCluster(newValue);
                }}
                sx={{ width: 300 }}
                renderInput={(params) => <TextField {...params} label="Cluster" />}
            />

            <DataGrid
                initialState={{
                    sorting: {
                        sortModel: [{ field: 'id', sort: 'asc' }],
                    },
                    filter: {
                        filterModel: {
                            items: [],
                            quickFilterValues: [""],
                        },
                    },
                }}
                slots={{ toolbar: GridToolbar }}
                slotProps={{
                    toolbar: {
                        printOptions: { disableToolbarButton: true },
                        csvOptions: { disableToolbarButton: true },
                        showQuickFilter: true,
                    },
                }}
                rows={classes}
                columns={columns}
                pageSizeOptions={[5, 10]}
                checkboxSelection
                onRowClick={handleRowClick}
            />

            <Dialog open={openEditDialog} onClose={handleCloseEditDialog} >
                {selectedClass && (
                    <>
                        <DialogTitle>Edit Class Information {selectedClass.classCode}</DialogTitle>
                        <DialogContent>
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
                                <Autocomplete
                                    options={programs}
                                    getOptionLabel={(option) => option.groupName || ""}
                                    isOptionEqualToValue={(option, value) => option.id === value.id}
                                    value={programs.find(p => p.id === selectedClass.groupId) || null}
                                    onChange={(_, newValue) => {
                                        handleInputChange('groupId', newValue?.id || '');
                                        handleInputChange('programName', newValue?.name || '');
                                    }}
                                    renderInput={(params) => (
                                        <TextField {...params} label="Nhóm" />
                                    )}
                                    fullWidth
                                />
                                <Autocomplete
                                    options={courses}
                                    loading={loadingCourses}
                                    getOptionLabel={(option) => `${option.id} - ${option.courseName}`}
                                    isOptionEqualToValue={(option, value) => option.id === value.id}
                                    value={courses.find(c => c.id === selectedClass.moduleCode) || null}
                                    onChange={(_, newValue) => {
                                        handleInputChange('moduleCode', newValue?.id || '');
                                        handleInputChange('moduleName', newValue?.courseName || '');
                                    }}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            label="Học phần"
                                            InputProps={{
                                                ...params.InputProps,
                                                endAdornment: (
                                                    <>
                                                        {loadingCourses ? <CircularProgress color="inherit" size={20} /> : null}
                                                        {params.InputProps.endAdornment}
                                                    </>
                                                ),
                                            }}
                                        />
                                    )}
                                    fullWidth
                                />
                                <FormControl sx={{ mt: 1 }}>
                                    <InputLabel>Kiểu lớp</InputLabel>
                                    <Select
                                        name="classType"
                                        value={selectedClass.classType|| ''}
                                        onChange={(e) => handleInputChange('classType', e.target.value)}
                                        label="Kiểu lớp"
                                    >
                                        <MenuItem value="LT+BT">LT+BT</MenuItem>
                                        <MenuItem value="LT">LT</MenuItem>
                                        <MenuItem value="BT">BT</MenuItem>
                                    </Select>
                                </FormControl>

                               <Box sx={{display: 'flex', gap: 2}}>
                                   <TextField
                                       label="Duration"
                                       type="number"
                                       value={selectedClass.duration || 0}
                                       onChange={(e) => handleInputChange('duration', parseInt(e.target.value))}
                                       fullWidth
                                   />
                                   <TextField
                                       label="Khóa"
                                       value={selectedClass.promotion || ''}
                                       onChange={(e) => handleInputChange('promotion', e.target.value)}
                                       fullWidth
                                   />
                               </Box>

                                <Box sx={{display: 'flex', gap: 2}}>
                                    <FormControl sx={{ minWidth: 125 }}>
                                        <InputLabel>Kíp</InputLabel>
                                        <Select
                                            name="crew"
                                            value={selectedClass.crew|| ''}
                                            onChange={(e) => handleInputChange('crew', e.target.value)}
                                            label="Kíp"
                                        >
                                            <MenuItem value="S">Sáng</MenuItem>
                                            <MenuItem value="C">Chiều</MenuItem>
                                        </Select>
                                    </FormControl>
                                    <TextField

                                        label="Số lượng sinh viên"
                                        type="number"
                                        value={selectedClass.quantityMax || ''}
                                        onChange={(e) => handleInputChange('quantityMax', parseInt(e.target.value))}
                                        fullWidth
                                    />
                                    <TextField
                                        label="Tuần học"

                                        value={selectedClass.learningWeeks || ''}
                                        onChange={(e) => handleInputChange('learningWeeks', parseInt(e.target.value))}
                                        fullWidth
                                    />
                                </Box>




                               <Box sx={{display: 'flex', gap: 2}}>
                                   <TextField
                                       label="Số SV BT"
                                       type="number"
                                       value={selectedClass.exerciseMaxQuantity || ''}
                                       onChange={(e) => handleInputChange('exerciseMaxQuantity', parseInt(e.target.value))}
                                       fullWidth
                                   />
                                   <TextField
                                       label="Số SV LT"
                                       type="number"
                                       value={selectedClass.lectureExerciseMaxQuantity || 0}
                                       onChange={(e) => handleInputChange('lectureExerciseMaxQuantity', parseInt(e.target.value))}
                                       fullWidth
                                   />
                                   {/*<TextField*/}
                                   {/*    label="kiểu tuần"*/}
                                   {/*    value={selectedClass.weekType || ''}*/}
                                   {/*    onChange={(e) => handleInputChange('weekType', e.target.value)}*/}
                                   {/*    fullWidth*/}
                                   {/*/>*/}
                                   <FormControl sx={{ width: "100%" }}>
                                       <InputLabel>Kiểu tuần</InputLabel>
                                       <Select
                                           name="weekType"
                                           value={selectedClass.weekType || ''}
                                           onChange={(e) => handleInputChange('weekType', e.target.value)}                                           label="Kiểu tuần"
                                       >
                                           <MenuItem value="0">AB</MenuItem>
                                           <MenuItem value="1">A</MenuItem>
                                           <MenuItem value="2">B</MenuItem>
                                       </Select>
                                   </FormControl>
                               </Box>





                            </Box>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={handleCloseEditDialog}>Cancel</Button>
                            <Button onClick={handleSaveClass} variant="contained">Save</Button>
                        </DialogActions>
                    </>
                )}
            </Dialog>
        </>
    );
}