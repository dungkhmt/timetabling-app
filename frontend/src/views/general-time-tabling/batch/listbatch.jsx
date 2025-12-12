//import {request} from "../../../api";
import {request} from "api";

import React, {useEffect, useState} from "react";
import {Button, Dialog, DialogContent, DialogTitle, Paper, TextField,Autocomplete} from "@mui/material";

import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import {Link} from "react-router-dom";
//import { useNavigate } from 'react-router-dom';
import { useHistory } from 'react-router-dom';

export default function ListBatch(){
    const [semesters, setSemesters] = useState([]);
    const [selectedSemester, setSelectedSemester] = useState(null);
    const [listBatch, setListBatch] = useState([]);
    const [loading, setLoading] = useState(false);
     const [error, setError] = useState(null);
    const [batchName, setBatchName] = useState("");
    const [openAddBatchDialog, setOpenAddBatchDialog] = useState(false);

    const history = useHistory();

    const columns = [
        {
            title:"ID",
            field:"id"
        },
        {
            title:"Batch Name",
            field:"name",
            render: (rowData) => (
            <Link
                to={{
                    pathname:
                      "/opened-class-plan/" + rowData["id"],
                     //"/opened-class-plan/" + selectedSemester?.semester + "/"+ rowData["id"]
                }}
            >
                {rowData["name"]}
            </Link>
            ),
        }
    ];
    const paginationModel = { page: 0, pageSize: 5 };

    function getAllSemesters(){
        request(
                    "get",
                    "/semester/get-all",
                    (res)=>{
                        console.log(res);
                        setSemesters(res.data || []);
                        setLoading(false);
                    },
                    (error)=>{
                        console.error(error);
                        setError(error);
                    },
                );
    }
    function getListBatch(semester){
        //request("get", "/timetabling-batch/get-all", (res) => {
        //let selected_semester = semester != null ? semester.semester : null;    
        request(
            "get", 
            "/timetabling-batch/get-all/" + semester,
            //"/course-v2/get-batches",
            (res) => {    
                setListBatch(res.data);
                 console.log("getListBatch, res = ",res.data);
            },null
        ).catch((err) => {
              // This will catch any other errors that might occur
              console.error("Unexpected error:", err);
              setLoading(false);
              //toast.error(err.response.data);
        });
    }

    function handleAdd(){
        //alert('add');
        setOpenAddBatchDialog(true);
    }
    function handleClose(){
        setOpenAddBatchDialog(false);
    }
    function handleSave(){
        if (!selectedSemester?.semester) {
            alert('Semester is NULL'); return;
        }
        let payLoad = {
            batchName: batchName,
            semester: selectedSemester.semester,
        };

        request(
            "post",
            "/timetabling-batch/add-batch",
            //"/course-v2/add-batch",
            (res) => {
                //successNoti("Batch created successfully");
                //sleep(1000).then(() => {
                //  history.push("/programming-contest/contest-manager/" + res.data.contestId);
                //});
                getListBatch();
                setOpenAddBatchDialog(false);
            },
            {
                onError: (err) => {
                    //errorNoti(err?.response?.data?.message, 5000);
                    alert('ERROR');
            },
            },
            payLoad 
    )
      .then()
      .finally(() => setLoading(false));
    }

    const handleRowClick = (params) => {
    // params.row contains the data of the clicked row
        const batchId = params.row.id;
        history.push(`/general-time-tabling/opened-class-plan/${batchId}`); // Navigate to the user details page
    };

    useEffect(() => {
        getAllSemesters();
        //getListBatch();
    }, []);

    return (
        <>
            <Autocomplete
                disablePortal
                loadingText="Loading..."
                getOptionLabel={(option) => option && option.semester}
                onChange={(e, semester) => {
                  setSelectedSemester(semester);
                  getListBatch(semester?.semester);
                }}
                value={selectedSemester}
                options={semesters}
                size="small"
                sx={{ 
                  width: 130
                }}
                renderInput={(params) => <TextField {...params} label="Chọn kỳ" />}
              />

            <Paper sx={{ height: 400, width: '100%' }}>
                SEMESTER: {selectedSemester?.semester}
                <Button
                    onClick = {handleAdd}
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
                    rows={listBatch}
                    columns={columns}
                    //initialState={{ pagination: { paginationModel } }}
                    pageSizeOptions={[5, 10]}
                    checkboxSelection
                    sx={{ border: 0 }}
                    onRowClick={handleRowClick}
                />
                <Dialog
                    open={openAddBatchDialog}
                    onClose={handleClose}

                >
                    <DialogTitle>ADD New Batch</DialogTitle>
                    <DialogContent>
                        <TextField
                            autoFocus
                            required
                            margin="dense"
                            id="batchName"
                            name="batchName"
                            value={batchName}
                            label="Batch Name"
                            //type="email"
                            fullWidth
                            variant="standard"
                            onChange={(event) => {
                               setBatchName(event.target.value);
                            }}
                        />
                        <Button onClick={() =>{
                            setOpenAddBatchDialog(false);
                        }}>
                            Cancel
                        </Button>
                        <Button onClick={handleSave}>
                            Save
                        </Button>
                    </DialogContent>
                </Dialog>
            </Paper>
        </>
    );
}