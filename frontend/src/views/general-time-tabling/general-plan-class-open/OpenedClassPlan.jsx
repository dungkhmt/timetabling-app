import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, {useEffect, useState} from "react";
import {
    Button, Dialog, DialogContent, DialogTitle, Paper, TextField, Autocomplete, CircularProgress,
    FormControl, MenuItem, InputLabel, Select, DialogActions, Box

} from "@mui/material";

import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { request } from "api";
import ListOpenedClass from "./ListOpenedClass";

export default function OpenedClassPlan(){
    const {batchId} = useParams();
    const [classPlans, setClassPlans] = useState([]);
    const [classes, setClasses] = useState([]);
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
    const [nbStudentsLTBT, setNbStudentsLTBT] = useState(0);
    const [nbStudentsLT, setNbStudentsLT] = useState(0);
    const [nbStudentsBT, setNbStudentsBT] = useState(0);
    
    const [learningWeeks, setLearningWeeks] = useState(null);
    const [duration, setDuration] = useState(0);
    const [durationLTBT, setDurationLTBT] = useState(0);
    const [durationLT, setDurationLT] = useState(0);
    const [durationBT, setDurationBT] = useState(0);
    
    const [weekType, setWeekType] = useState("0");
    const [crew, setCrew] = useState("");
    const [separateLTBT, setSeparateLTBT] = useState("N");
    const [generateClasses, setGenerateClasses] = useState("Y");
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
            separateLTBT: separateLTBT,
            generateClasses:generateClasses,
            lectureMaxQuantity: nbStudentsLT,
            exerciseMaxQuantity: nbStudentsBT,
            lectureExerciseMaxQuantity: nbStudentsLTBT,
            durationLTBT: durationLTBT,
            durationLT: durationLT,
            durationBT: durationBT

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
                  getListOpenClassPlans();
                  getAllClassesOfBatch();
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
    const handleChangeClassType = (newClassType) => {
        setClassType(newClassType);
        //alert('change class type ' + newClassType + ' course ' + course.durationLtBt);
        if(course){
            //if(newClassType=="LT+BT"){
                    setDurationLTBT(course.durationLtBt);
                    setNbStudentsLTBT(course.maxStudentLTBT);

            //    }else if(newClassType=="LT"){
                    setDurationLT(course.durationLt);
                    setNbStudentsLT(course.maxStudentLT);
            //    }else if(newClassType=="BT"){
                    setDurationBT(course.durationBt);
                    setNbStudentsBT(course.maxStudentBT);
            //    }
        }
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
                console.log('setCourse ',course,'classType = ',classType);    
                setSeparateLTBT(res.data.separateLTBT);
                //if(classType=="LT+BT"){
                    setDurationLTBT(res.data.durationLtBt);
                    setNbStudentsLTBT(res.data.maxStudentLTBT);
                //}else if(classType=="LT"){
                    setDurationLT(res.data.durationLt);
                    setNbStudentsLT(res.data.maxStudentLT);
                //}else if(classType=="BT"){
                    setDurationBT(res.data.durationBt);
                    setNbStudentsBT(res.data.maxStudentBT);
                //}
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
    
    function getAllClassesOfBatch(){
              request("get", 
                          "/general-classes/get-all-classes-of-batch?batchId="+batchId,
                          (res) => {
                              setClasses(res.data);
                          }
                      );
    }

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
            getAllClassesOfBatch();
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
                            <DialogTitle>Kế hoạch mở lớp</DialogTitle>
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
                                            label="Nhóm"
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
                                            sx={{ mt: 1, width: '100%' }}
                                                        
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
                                            sx={{ mt: 2, mb: 2, width: '100%' }}


                                        />
                                    )}
                                />

                                <FormControl fullWidth>
                                    <InputLabel>Kiểu lớp</InputLabel>
                                    <Select
                                        name="classType"
                                        value={classType}
                                        onChange={(e) => {
                                            //setClassType(e.target.value);
                                            handleChangeClassType(e.target.value);
                                        }}
                                        label="Kiểu lớp"                    
                                    >
                                        <MenuItem value="LT+BT">LT+BT</MenuItem>
                                        <MenuItem value="LT">LT</MenuItem>
                                        <MenuItem value="BT">BT</MenuItem>
                                    </Select>
                                </FormControl>
                                <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>              
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
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                    <TextField
                                    label="Số tiết"
                                    name="duration"
                                    value={duration}
                                    onChange={(e) => {setDuration(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.duration}
                                    helperText={errors.duration}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                </Box>
                                <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>              
                                    <TextField
                                    label="Số tiết LT+BT"
                                    name="durationLTBT"
                                    value={durationLTBT}
                                    onChange={(e) => {setDurationLTBT(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.durationLTBT}
                                    helperText={errors.durationLTBT}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                    <TextField
                                    label="Số tiết LT"
                                    name="durationLT"
                                    value={durationLT}
                                    onChange={(e) => {setDurationLT(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.durationLT}
                                    helperText={errors.durationLT}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                    <TextField
                                    label="Số tiết BT"
                                    name="durationBT"
                                    value={durationBT}
                                    onChange={(e) => {setDurationBT(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.durationBT}
                                    helperText={errors.durationBT}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                </Box>
                                <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>              
                                    <TextField
                                    label="Số SV LT+BT"
                                    name="nbStudentsLTBT"
                                    value={nbStudentsLTBT}
                                    onChange={(e) => {setNbStudentsLTBT(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.nbStudentsLTBT}
                                    helperText={errors.nbStudentsLTBT}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                    <TextField
                                    label="Số SV LT"
                                    name="nbStudentsLT"
                                    value={nbStudentsLT}
                                    onChange={(e) => {setNbStudentsLT(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.nbStudentsLT}
                                    helperText={errors.nbStudentsLT}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                    <TextField
                                    label="Số SV BT"
                                    name="nbStudentsBT"
                                    value={nbStudentsBT}
                                    onChange={(e) => {setNbStudentsBT(e.target.value)}}
                                    fullWidth
                                    required
                                    placeholder=""
                                    error={!!errors.nbStudentsBT}
                                    helperText={errors.nbStudentsBT}
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />  
                                </Box>

                                <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                                    <TextField
                                        label="Số lượng lớp"
                                        name="nbClasses"
                                        value={nbClasses}
                                        onChange={(e) => {setNbClasses(e.target.value)}}
                                        type="number"
                                        size="small"
                                        sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                        required
                                        error={!!errors.nbClasses}
                                        helperText={errors.nbClasses}
                                    />
                                    <TextField
                                        label="Số lượng sinh viên"
                                        name="nbStudents"
                                        value={nbStudents}
                                        onChange={(e) => {setNbStudents(e.target.value)}}
                                        type="number"
                                        size="small"
                                        required
                                        error={!!errors.nbStudents}
                                        helperText={errors.nbStudents}
                                        sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    />
                                    <FormControl
                                    sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                    >
                                        <InputLabel>Tách LT-BT</InputLabel>
                                        <Select
                                            name="separateLTBT"
                                            value={separateLTBT}
                                            onChange={(e) => setSeparateLTBT(e.target.value)}
                                            label="Tách LT-BT"     
                                
                                            sx={{ mt: 2, mb: 1.5, width: '100%' }}                                  
                                        >
                                            <MenuItem value="N">N</MenuItem>                                            
                                            <MenuItem value="Y">Y</MenuItem>
                                        </Select>
                                    
                                    </FormControl>
                                </Box>

                                <Box sx={{
                                    display: 'flex',
                                    gap: 2,          // Khoảng cách giữa các item (16px)
                                    alignItems: 'flex-start', // Căn chỉnh các item theo chiều dọc
                                    mt: 2,           // Margin top
                                    mb: 1            // Margin bottom
                                }}>
                                    {/* TextField Khóa */}
                                    <TextField
                                        label="Khóa"
                                        name="Promotion"
                                        value={promotion}
                                        onChange={(e) => setPromotion(e.target.value)}
                                        required
                                        error={!!errors.promotion}
                                        helperText={errors.promotion}
                                        sx={{
                                            minWidth: 50,
                                            '& .MuiOutlinedInput-root': {
                                                height: '44px'
                                            }
                                        }}
                                    />

                                    {/* FormControl Kiểu tuần */}
                                    <FormControl
                                        sx={{ minWidth: 100,
                                            '& .MuiOutlinedInput-root': {
                                                height: '44px'
                                            }
                                    }}
                                    >
                                        <InputLabel>Kiểu tuần</InputLabel>
                                        <Select
                                            name="weekType"
                                            value={weekType}
                                            onChange={(e) => setWeekType(e.target.value)}
                                            label="Kiểu tuần"
                                        >
                                            <MenuItem value="0">AB</MenuItem>
                                            <MenuItem value="1">A</MenuItem>
                                            <MenuItem value="2">B</MenuItem>
                                        </Select>
                                    </FormControl>

                                    {/* FormControl Kíp */}
                                    <FormControl
                                        sx={{ minWidth: 250,
                                            '& .MuiOutlinedInput-root': {
                                                height: '44px'
                                            }
                                    }}
                                    >
                                        <InputLabel>Kíp</InputLabel>
                                        <Select
                                            name="crew"
                                            value={crew}
                                            onChange={(e) => setCrew(e.target.value)}
                                            label="Kíp"
                                        >
                                            <MenuItem value="S">Sáng</MenuItem>
                                            <MenuItem value="C">Chiều</MenuItem>
                                        </Select>
                                    </FormControl>
                                    <FormControl
                                        sx={{ minWidth: 250,
                                            '& .MuiOutlinedInput-root': {
                                                height: '44px'
                                            }
                                    }}
                                    >
                                        <InputLabel>Sinh lớp</InputLabel>
                                        <Select
                                            name="generateClasses"
                                            value={generateClasses}
                                            onChange={(e) => setGenerateClasses(e.target.value)}
                                            label="Sinh lớp"
                                        >
                                            <MenuItem value="Y">Y</MenuItem>
                                            <MenuItem value="N">N</MenuItem>
                                        </Select>
                                    </FormControl>
                                </Box>

                            </DialogContent>
                            <DialogActions sx={{
                                padding: "16px",
                                gap: "8px",
                                borderTop: '1px solid #e0e0e0',
                                backgroundColor: '#fafafa'
                            }}>
                                <Button
                                    onClick={() =>{
                                        setOpenAddClassPlanDialog(false);
                                    }}
                                    variant="outlined"
                                    sx={{
                                        minWidth: "100px",
                                        padding: "8px 16px",
                                        textTransform: 'none'
                                    }}
                                >
                                    Cancel
                                </Button>
                                <Button
                                    onClick={handleSave}
                                    color="primary"
                                    variant="contained"
                                    autoFocus
                                    sx={{
                                        minWidth: "120px",
                                        padding: "8px 16px",
                                        textTransform: 'none'
                                    }}
                                >
                                    Save
                                </Button>
                            </DialogActions>
                        </Dialog>
                        <ListOpenedClass
                            batchId = {batchId}
                            listClasses  = {classes}
                        >

                        </ListOpenedClass>
        </>
    );
}