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
    const [clusters, setClusters] = useState([]);
    const [selectedCluster, setSelectedCluster] = useState(null);
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

    function getClustersOfBatch(){
        request("get", 
                    "/general-classes/get-clusters-by-batch?batchId="+batchId,
                    (res) => {
                        setClusters(res.data);
                    }
                );
    }
    function getAllClassesOfBatch(){
        request("get", 
                    "/general-classes/get-all-classes-of-batch?batchId="+batchId,
                    (res) => {
                        setClasses(res.data);
                    }
                );
    }

    function performClustering(){
        let body = {
            semester:"",
            batchId: batchId
        };
        request(
            "post",
            "/general-classes/compute-class-cluster",
            (res) => {
                console.log('compute cluster returned ',res.data);
                // Clear selection after operation completes
                //setSelectedIds([]);
                //setSelectedRows([]);
                getClustersOfBatch();
            },
            {
                onError: (e) => {
                    //setSelectedIds([]);
                    //setSelectedRows([]);
                }
            },
            body
        );
    }
    function getClassesOfCluster(clusterId){
        request("get", 
                    "/general-classes/get-by-cluster/"+clusterId,
                    (res) => {
                        setClasses(res.data);
                    }
                );
    }
    function handleChangeCluster(newValue){
        if(newValue){
            getClassesOfCluster(newValue.id);
            setSelectedCluster(newValue);
        }else{
            getAllClassesOfBatch();
            setSelectedCluster(null);
        }
    }
    useEffect(() => {
                getAllClassesOfBatch();
                getClustersOfBatch();
            }, []);
    return (
        <>
            Danh sách lớp {batchId}
            <Button
                onClick = {() => {performClustering()}}
            >
                Phân cụm
            </Button>
            <Autocomplete
                //disablePortal
                options={clusters}
                getOptionLabel={(option) => `${option.id} - ${option.name}`}
                isOptionEqualToValue={(option, value) => option.id === value.id}
                //loading={isLoadingCourses}
                value={selectedCluster}
                onChange={(_, newValue) => {
                    handleChangeCluster(newValue);
                    //setSelectedCluster(newValue);
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