import React, {useEffect, useState} from "react";
import TimeTableNew from "./components/TimeTableNew";
import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import {request} from "api";


import {
    Button, Dialog, DialogContent, DialogTitle, Paper, TextField, Autocomplete, CircularProgress,
    FormControl, MenuItem, InputLabel, Select, DialogActions, Box

} from "@mui/material";
export default function MakeTimetable(){
    const {versionId} = useParams();
    const [classes, setClasses] = useState([]);
    const [allClasses, setAllClasses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [errors, setErrors] = useState({});
    const [selectedSemester, setSelectedSemester] = useState(null);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [selectedRows, setSelectedRows] = useState([]);
    const [selectedVersion, setSelectedVersion] = useState(null);
    const [numberSlotsToDisplay, setNumberSlotsToDisplay] = useState(6);
    
    const [searchCourseCode, setSearchCourseCode] = useState("");
    const [searchClassCode, setSearchClassCode] = useState("");
    const [searchCourseName, setSearchCourseName] = useState("");
    const [searchGroupName, setSearchGroupName] = useState("");
    const [openSearchDialog, setOpenSearchDialog] = useState(false);
    const [openScheduleDialog, setOpenScheduleDialog] = useState(false);
    const [openClearScheduleDialog,setOpenClearScheduleDialog] = useState(false);
    
    const [courses, setCourses] = useState([]);
    const [programs, setPrograms] = useState([]);

    const [scheduleTimeLimit, setScheduleTimeLimit] = useState(5);
    const [algorithm, setAlgorithm] = useState("");
    const [algorithms, setAlgorithms] = useState([]);
    const [days, setDays] = useState('2,3,4,5,6');
    const [slots, setSlots] = useState('1,2,3,4,5,6');

    function getClasses(){
        setLoading(true);
        request(
                            "get",
                            "/general-classes/get-class-segments-of-version?versionId=" + versionId
                            + "&searchCourseCode=" + searchCourseCode + "&searchCourseName=" + searchCourseName
                            + "&searchClassCode=" + searchClassCode + "&searchGroupName=" + searchGroupName,
                            (res)=>{
                                console.log(res);
                                setClasses(res.data || []);
                                setLoading(false);
                            },
                            (error)=>{
                                console.error(error);
                                setError(error);
                            },
                        );

    }
    function getAllClasses(){
        // all search keywords are null
        setLoading(true);
        request(
                            "get",
                            "/general-classes/get-class-segments-of-version?versionId=" + versionId
                            + "&searchCourseCode=" + "&searchCourseName="
                            + "&searchClassCode=" + "&searchGroupName=",
                            (res)=>{
                                console.log(res);
                                setAllClasses(res.data || []);
                                setLoading(false);
                            },
                            (error)=>{
                                console.error(error);
                                setError(error);
                            },
                        );

    }
    function onSaveSuccess(){

    }
    function handleFilter(){
        //getClasses();
        setOpenSearchDialog(false);
    }
    function handleClose(){
        setOpenSearchDialog(false);
    }

    function getCourses(){
            request("get", 
                "/course-v2/get-all",
                (res) => {
                    setCourses(res.data);
                }
            );
        }
        function getAlgorithms(){
            request("get", 
                "/general-classes/get-list-algorithm-names",
                (res) => {
                    setAlgorithms(res.data);
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
    
        function schedule(){
            //var s = '';
            //for(i = 0; i < selectedRows.length; i++) s = s + selectedRows[i] + '\n';
            //selectedRows.map((i) => s = s + i + '\n');
            //alert(s);
            /*
            let payLoad = {
                classIds: selectedRows,
                timeLimit: scheduleTimeLimit,
                algorithm: algorithm,
                versionId: versionId,
                days: days,
                slots: slots
            };
            request(
                "post",
                "/general-classes/auto-schedule-timeslot-room",
                (res) => {
                    setOpenScheduleDialog(false);
                },
                null,
                payLoad
            );
            */
            setOpenScheduleDialog(true);
        }

        function performSchedule(){
            let payLoad = {
                ids: selectedRows,
                timeLimit: scheduleTimeLimit,
                algorithm: algorithm,
                versionId: Number(versionId),
                days: days,
                slots: slots
            };
            request(
                "post",
                "/general-classes/auto-schedule-timeslot-room",
                (res) => {
                    getClasses();
                    setOpenScheduleDialog(false);
                },
                null,
                payLoad
            );
        }
        function performClearSchedule(){
            let payLoad = {
                ids: selectedRows
            };

            request(
                       "post",
                       `/general-classes/reset-schedule?semester=`,
                       (res) => {
                          if(res.data == 'ok'){
                            getClasses();
                            getAllClasses();
                          }else{
                          //  alert(res.data.message);
                          }
                       },
                       null,
                       payLoad,
                       {},
                       null,
                       null
                );
            setOpenClearScheduleDialog(false);
        }
        function handleScheduleDialogClose(){
            setOpenScheduleDialog(false);
        }
    useEffect(() => {
        getClasses();
        getAllClasses();
        getCourses();
        getPrograms();
        getAlgorithms();
    },[]);

    return(
        <>
            Version {versionId}
            <Button
                onClick = {() =>{ setOpenSearchDialog(true)}}
            >
                FILTER SETTINGS
            </Button>
            <Button
                onClick = {() =>{ getClasses() }}
            >
                FILTER
            </Button>

            <Button
                onClick = {() =>{ schedule() }}
            >
                Schedule
            </Button>

            <Button
                onClick = {() =>{ setOpenClearScheduleDialog(true); }}
            >
                Clear Schedule
            </Button>
            
            <TimeTableNew 
                selectedSemester={selectedSemester}
                classes={classes}
                getClasses = {getClasses}
                versionId={versionId}
                selectedGroup={selectedGroup}
                onSaveSuccess={onSaveSuccess}
                loading={loading}
                selectedRows={selectedRows}
                onSelectedRowsChange={setSelectedRows}
                selectedVersion={selectedVersion}
                numberSlotsToDisplay={numberSlotsToDisplay}
            />

            <TimeTableNew 
                selectedSemester={selectedSemester}
                classes={allClasses}
                getClasses = {getAllClasses}
                versionId={versionId}
                selectedGroup={selectedGroup}
                onSaveSuccess={onSaveSuccess}
                loading={loading}
                selectedRows={selectedRows}
                onSelectedRowsChange={setSelectedRows}
                selectedVersion={selectedVersion}
                numberSlotsToDisplay={numberSlotsToDisplay}
            />
            
            <Dialog
                open={openSearchDialog}
                onClose={handleClose}
                        
            >
            <DialogTitle>FILTER</DialogTitle>

                            <DialogContent>
                                {/*
                                <Autocomplete
                                    options={programs || []}
                                    getOptionLabel={(option) => `${option.groupName}`}
                                    isOptionEqualToValue={(option, value) => option.id === value.id}
                                    //loading={isLoadingCourses}
                                    value={searchGroupName}
                                    onChange={(_, newValue) => {
                                        setSearchGroupName(newValue.groupName);
                                            console.log('new value = ',newValue);
                                    }}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            label="Nhóm"
                                            required
                                            error={ !!errors.searchGroupName}
                                            helperText={errors.searchGroupName}
                                            InputProps={{
                                               ...params.InputProps,
                                               endAdornment: (
                                                   <>
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
                                    value={searchCourseCode}
                                    onChange={(_, newValue) => {
                                        setSearchCourseCode(newValue.id);
                                    }}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            label="Học phần"
                                            required
                                            error={!!errors.searchCourseCode}
                                            helperText={errors.searchCourseCode}
                                            InputProps={{
                                                ...params.InputProps,
                                                endAdornment: (
                                                    <>
                                                        {params.InputProps.endAdornment}
                                                    </>
                                                ),
                                            }}
                                            sx={{ mt: 2, mb: 2, width: '100%' }}


                                        />
                                    )}
                                />

                                */}
                                
                                <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                                    <TextField
                                        label="Mã lớp "
                                        name="classCode"
                                        value={searchClassCode}
                                        onChange={(e) => {setSearchClassCode(e.target.value)}}
                                        size="small"
                                        sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                        required
                                        error={!!errors.searchClassCode}
                                        helperText={errors.searchClassCode}
                                    />
                                    <TextField
                                        label="Nhóm "
                                        name="searchGroupName"
                                        value={searchGroupName}
                                        onChange={(e) => {setSearchGroupName(e.target.value)}}
                                        size="small"
                                        sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                        required
                                        error={!!errors.searchGroupName}
                                        helperText={errors.searchGroupName}
                                    />
                                 </Box> 

                                <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                                    <TextField
                                        label="Mã HP "
                                        name="searchCourseCode"
                                        value={searchCourseCode}
                                        onChange={(e) => {setSearchCourseCode(e.target.value)}}
                                        size="small"
                                        sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                        required
                                        error={!!errors.searchCourseCode}
                                        helperText={errors.searchCourseCode}
                                    />
                                    <TextField
                                        label="Tên HP "
                                        name="searchCourseName"
                                        value={searchCourseName}
                                        onChange={(e) => {setSearchCourseName(e.target.value)}}
                                        size="small"
                                        sx={{ mt: 2, mb: 1.5, width: '100%' }}
                                        required
                                        error={!!errors.searchCourseName}
                                        helperText={errors.searchCourseName}
                                    />
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
                                        setOpenSearchDialog(false);
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
                                    onClick={() =>{
                                        setSearchClassCode("");
                                        setSearchCourseCode("");
                                        setSearchCourseName("");
                                        setSearchGroupName("");
                                        //getClasses();
                                        setOpenSearchDialog(false);
                                    }}
                                    variant="outlined"
                                    sx={{
                                        minWidth: "100px",
                                        padding: "8px 16px",
                                        textTransform: 'none'
                                    }}
                                >
                                    Clear
                                </Button>
                                <Button
                                    onClick={handleFilter}
                                    color="primary"
                                    variant="contained"
                                    autoFocus
                                    sx={{
                                        minWidth: "120px",
                                        padding: "8px 16px",
                                        textTransform: 'none'
                                    }}
                                >
                                    Filter
                                </Button>
                            </DialogActions>
            </Dialog>


            <Dialog
                open={openScheduleDialog}
                onClose={handleScheduleDialogClose}
                        
            >
            <DialogTitle>Schedule settings</DialogTitle>

                <DialogContent>
                    <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                        <TextField
                            label="Time Limit "
                            name="scheduleTimeLimit"
                            value={scheduleTimeLimit}
                            onChange={(e) => {setScheduleTimeLimit(e.target.value)}}
                            size="small"
                            sx={{ mt: 2, mb: 1.5, width: '100%' }}
                            required
                            error={!!errors.scheduleTimeLimit}
                            helperText={errors.scheduleTimeLimit}
                        />
                        <FormControl fullWidth>
                            <InputLabel id="algo">Thuật toán</InputLabel>
                            <Select
                                labelId="algo"
                                id="algo"
                                value={algorithm}
                                label="algoritm"
                                onChange={(e) => {setAlgorithm(e.target.value)}}
                            >
                            {algorithms.map((algo) => (
                                <MenuItem key={algo} value={algo}>
                                    {algo}
                                </MenuItem>
                            ))}   
                            
                            </Select>
                        </FormControl>
                    </Box> 
                    
                    <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                        <TextField
                            label="days"
                            name="days"
                            value={days}
                            onChange={(e) => {setDays(e.target.value)}}
                            size="small"
                            sx={{ mt: 2, mb: 1.5, width: '100%' }}
                            required
                            error={!!errors.days}
                            helperText={errors.days}
                        />
                        <TextField
                            label="slots"
                            name="slots"
                            value={slots}
                            onChange={(e) => {setSlots(e.target.value)}}
                            size="small"
                            sx={{ mt: 2, mb: 1.5, width: '100%' }}
                            required
                            error={!!errors.slots}
                            helperText={errors.slots}
                        />
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
                            //setOpenScheduleDialog(false);
                            performSchedule();
                        }}
                        variant="outlined"
                        sx={{
                            minWidth: "100px",
                            padding: "8px 16px",
                            textTransform: 'none'
                        }}
                    >
                        RUN
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog
                open={openClearScheduleDialog}
                
                        
            >
            <DialogTitle>Clear Schedule </DialogTitle>

                <DialogContent>
                    

                </DialogContent>
                <DialogActions sx={{
                        padding: "16px",
                        gap: "8px",
                        borderTop: '1px solid #e0e0e0',
                        backgroundColor: '#fafafa'
                    }}>
                    <Button
                        onClick={() =>{
                            performClearSchedule();
                        }}
                        variant="outlined"
                        sx={{
                            minWidth: "100px",
                            padding: "8px 16px",
                            textTransform: 'none'
                        }}
                    >
                        YES
                    </Button>
                    <Button
                        onClick={() =>{
                            setOpenClearScheduleDialog(false);
                        }}
                        variant="outlined"
                        sx={{
                            minWidth: "100px",
                            padding: "8px 16px",
                            textTransform: 'none'
                        }}
                    >
                        NO
                    </Button>
                    
                </DialogActions>
            </Dialog>

        </>
    );
}