import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, {useEffect, useState} from "react";
import {
    Button, Dialog, DialogContent, DialogTitle, Paper, TextField, Autocomplete, CircularProgress,
    FormControl, MenuItem, InputLabel, Select, DialogActions, Box

} from "@mui/material";

import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { request } from "api";

export default function ListOpenedClass({batchId, listClasses}){
    const [classes, setClasses] = useState([]);
    const columns = [
        {
            title: "Code",
            field: "classCode"
        },
        {
            title: "Course",
            field: "moduleCode"
        },
        {
            title: "Type",
            field: "classType"
        },
        {
            title: "So tiet",
            field: "duration"
        },
        {
            title: "Kip",
            field: "crew"
        },
        {
            title: "Ma KH",
            field: "refClassId"
        },
    ];

    function handleRowClick(row){

    }

    function getAllClassesOfBatch(){
        request("get", 
                    "/general-classes/get-all-classes-of-batch?batchId="+batchId,
                    (res) => {
                        setClasses(res.data);
                    }
                );
    }

    useEffect(() => {
                getAllClassesOfBatch();
                
            }, []);
    return (
        <>
            Danh sách lớp {batchId}
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
                //rows={classes}
                rows={listClasses}
                columns={columns}
                    //initialState={{ pagination:  { page: 0, pageSize: 5 } }}
                    pageSizeOptions={[5, 10]}
                    checkboxSelection
                    //sx={{ border: 0 }}
                    onRowClick={handleRowClick}
            />

        </>
    );
}