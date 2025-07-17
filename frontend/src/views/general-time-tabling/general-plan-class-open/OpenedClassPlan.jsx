import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, {useEffect, useState} from "react";
import {Button, Dialog, DialogContent, DialogTitle, Paper, TextField,Autocomplete,CircularProgress,
    FormControl, MenuItem,InputLabel,Select 

} from "@mui/material";

import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { request } from "api";

export default function OpenedClassPlan(){
    const {batchId} = useParams();
    const [classPlans, setClassPlans] = useState([]);
    const [openAddClassPlanDialog, setOpenAddClassPlanDialog] = useState(false);
    const [errors, setErrors] = useState({});
    const [courses, setCourses] = useState([]);
    const [programs, setPrograms] = useState([]);
    const [selectedProgram,setSelectedProgram] = useState(null);
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [course, setCourse] = useState(null);
    const [loading, setLoading] = useState(false);
    const [program, setProgram] = useState(null);
    const [courseCode, setCourseCode] = useState(null);
    const [promotion, setPromotion] = useState(null);
    const [nbClasses, setNbClasses] = useState(1);
    const [classType, setClassType] = useState("LT+BT");
    const [nbStudents, setNbStudents] = useState(0);
    const [learningWeeks, setLearningWeeks] = useState(null);
    const [duration, setDuration] = useState(0);
    const [weekType, setWeekType] = useState("0");
    const [crew, setCrew] = useState("");
    const [separateLTBT, setSeparateLTBT] = useState("N");

    const columns = [
        {
            title: "ID",
            field: "id"
        },
        {
            title: "CourseCode",
            field: "moduleCode"
        },
        {
            title: "CourseName",
            field: "moduleName"
        },
        {
            title: "Program",
            field: "programName"
        },
        {
            title: "classType",
            field: "classType"
        },
        {
            title: "mass",
            field: "mass"
        },
        {
            title: "#Classes",
            field: "numberOfClasses"
        },
        {
            title: "duration",
            field: "duration"
        },
        {
            title: "Qty",
            field: "qty"
        },

    ];
    function getCourses(){
        request("get", 
            "/course-v2/get-all",
            (res) => {
                setCourses(res.data);
            }
        );
    }

    function getPrograms(){
        request("get", 
            "/group/get-all-group",
            (res) => {
                setPrograms(res.data);
            }
        );
    }

    function handleClose(){
        setOpenAddClassPlanDialog(false);
    }
    function handleSave(){
        //alert('program ' + selectedProgram.id + 
        //    ' course ' + selectedCourse.id + ' courseCode ' + courseCode + ' class type = ' + classType + 
        //' nbStudents = ' + nbStudents + ', nbClass = ' + nbClasses + ' learning weels = ' + learningWeeks + " duration = " + duration);
        let payLoad = {
            batchId: batchId,
            moduleCode: courseCode,
            classType: classType,
            numberOfClasses: nbClasses,
            quantityMax: nbStudents,
            learningWeeks: learningWeeks,
            groupId: selectedProgram.id,
            duration: duration,
            promotion: promotion,
            crew: crew,
            quantityMax: nbStudents,
            weekType: weekType,
            separateLTBT: separateLTBT
        };

        setLoading(true);
        request(
              "post",
              "/plan-general-classes/create-class-openning-plan",
              (res) => {
                console.log("Success response:", res.data);
                setLoading(false);
                if ( res.data) {
                  //onSuccess(res.data);
                }
                toast.success("Tạo ke hoach lớp mới thành công!");
                //handleClose();
              },
              null
              ,
              payLoad
            ).catch((err) => {
              // This will catch any other errors that might occur
              console.error("Unexpected error:", err);
              setLoading(false);
              toast.error(err.response.data);
            });

    }

    const handleCourseChange = (newSelectedCourse) => {
        setSelectedCourse(newSelectedCourse);
        setLoading(true);
        request(
          "get",
          "/course-v2/" + newSelectedCourse.id,
          (res) => {
            console.log("Success response:", res.data);
            setLoading(false);
            if (res.data) {
              setCourse(res.data);           
              setSeparateLTBT(res.data.separateLTBT);
                
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
          setCourseCode(newSelectedCourse.id);  
        }
      };
    
    function getListOpenClassPlans(){
        request(
                "get",
                `/plan-general-classes/get-opened-class-plans?batch=${batchId}`,
                (res) => {
                  //let data =  [];
                  //res.data.map((i) => {
                  //  var qty = i.lectureMaxQuantity + i.exerciseMaxQuantity + i.lectureExerciseMaxQuantity;
                  //  let item = {...i,qty:qty};
                  //  data.push({item});
                  //});
                  const data = res.data.map(i => (
                    {...i,qty: i.lectureMaxQuantity + i.exerciseMaxQuantity + i.lectureExerciseMaxQuantity}
                  )); 
                  setClassPlans(data);
                },
                (err) => {
                  toast.error("Có lỗi khi truy vấn kế hoạch học tập");
                },
                null,
                null,
                null
              );
    }

    const handleRowClick = (params) => {
    // params.row contains the data of the clicked row
        const planId = params.row.id;
        alert('plan ' + planId);
        //history.push(`/general-time-tabling/opened-class-plan/${batchId}`); // Navigate to the user details page
    };

    function handleOpenAddForm(){
        setOpenAddClassPlanDialog(true);
    }

    useEffect(() => {
            getListOpenClassPlans();
            getCourses();
            getPrograms();
        }, []);
    
    return(
        <>
            Opend Class Plan {batchId}
            <Paper sx={{ height: 400, width: '100%' }}>
                 
                            <Button
                                onClick = {handleOpenAddForm}
                            >
                                ADD
                            </Button>
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
                                rows={classPlans}
                                columns={columns}
                                //initialState={{ pagination:  { page: 0, pageSize: 5 } }}
                                pageSizeOptions={[5, 10]}
                                checkboxSelection
                                //sx={{ border: 0 }}
                                onRowClick={handleRowClick}
                            />
                            
                                
                        </Paper>

                        <Dialog
                            open={openAddClassPlanDialog}
                            onClose={handleClose}
                        
                        >
                            <DialogTitle>ADD New Batch</DialogTitle>
                            <DialogContent>
                                <Autocomplete
                                    options={programs || []}
                                    getOptionLabel={(option) => `${option.groupName}`}
                                    isOptionEqualToValue={(option, value) => option.id === value.id}
                                    //loading={isLoadingCourses}
                                    value={selectedProgram}
                                    onChange={(_, newValue) => {
                                        setSelectedProgram(newValue);
                                            console.log('new value = ',newValue);
                                        }}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            label="NHóm"
                                            required
                                            error={ !!errors.groupName}
                                            helperText={errors.groupName}
                                            InputProps={{
                                               ...params.InputProps,
                                               endAdornment: (
                                                   <>
                                                      {/*isLoadingCourses ? <CircularProgress size={20} /> : null*/}
                                                      {params.InputProps.endAdornment}
                                                   </>
                                                ),
                                            }}
                                                        
                                        />
                                     )}
                                />


                                <Autocomplete
                                    options={courses || []}
                                    getOptionLabel={(option) => `${option.id} - ${option.courseName}`}
                                    isOptionEqualToValue={(option, value) => option.id === value.id}
                                    //loading={isLoadingCourses}
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
                                                        {/*isLoadingCourses ? <CircularProgress size={20} /> : null*/}
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
                                        value={classType}
                                        onChange={(e) => {
                                            setClassType(e.target.value);
                                        }}
                                        label="Kiểu lớp"                    
                                    >
                                        <MenuItem value="LT+BT">LT+BT</MenuItem>
                                        <MenuItem value="LT">LT</MenuItem>
                                        <MenuItem value="BT">BT</MenuItem>
                                    </Select>
                                </FormControl>

                                <TextField
                                    label="Tuần học *"
                                    name="learningWeeks"
                                    value={learningWeeks}
                                    onChange={(e) => {setLearningWeeks(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder="Ví dụ: 2-9,11-18"
                                    error={!!errors.learningWeeks}
                                    helperText={errors.learningWeeks}
                                />
                                <TextField
                                    label="Số lượng lớp"
                                    name="nbClasses"
                                    value={nbClasses}
                                    onChange={(e) => {setNbClasses(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.nbClasses}
                                    helperText={errors.nbClasses}
                                />
                                <TextField
                                    label="Số lượng sinh viên"
                                    name="nbStudents"
                                    value={nbStudents}
                                    onChange={(e) => {setNbStudents(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.nbStudents}
                                    helperText={errors.nbStudents}
                                />
                                <TextField
                                    label="Khóa"
                                    name="Promotion"
                                    value={promotion}
                                    onChange={(e) => {setPromotion(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.promotion}
                                    helperText={errors.promotion}
                                />
                                <FormControl fullWidth>
                                    <InputLabel>Kiểu tuần</InputLabel>
                                    <Select
                                        name="weekType"
                                        value={weekType}
                                        onChange={(e) => {setWeekType(e.target.value)}}
                                        label="Kiểu tuần"
                                    
                                    >
                                        <MenuItem value="0">AB</MenuItem>
                                        <MenuItem value="1">A</MenuItem>
                                        <MenuItem value="2">B</MenuItem>
                                    </Select>
                                </FormControl>
                                <FormControl fullWidth>
                                    <InputLabel>Kíp</InputLabel>
                                    <Select
                                        name="crew"
                                        value={crew}
                                        onChange={(e) => {setCrew(e.target.value)}}
                                        label="Kíp"
                                    
                                    >
                                        <MenuItem value="S">Sáng</MenuItem>
                                        <MenuItem value="C">Chiều</MenuItem>
                                    </Select>
                                </FormControl>
                                <FormControl fullWidth>
                                    <InputLabel>Tách LT - BT</InputLabel>
                                    <Select
                                        name="separateLTBT"
                                        value={separateLTBT}
                                        onChange={(e) => {setCrew(e.target.value)}}
                                        label="Tách LT - BT"
                                    
                                    >
                                        <MenuItem value="Y">Y</MenuItem>
                                        <MenuItem value="N">N</MenuItem>
                                    </Select>
                                </FormControl>
                                <Button onClick={() =>{
                                    setOpenAddClassPlanDialog(false);
                                }}>
                                    Cancel
                                </Button>
                                <Button onClick={handleSave}>
                                    Save
                                </Button>
                            </DialogContent>
                        </Dialog>
        </>
    );
}