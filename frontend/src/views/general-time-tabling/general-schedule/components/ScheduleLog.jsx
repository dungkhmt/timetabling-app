import {request} from "api";

import React, {useEffect, useState} from "react";
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import { Paper, Box } from "@mui/material";
import { minWidth, width } from "@mui/system";

export default function ScheduleLog({versionId}) {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    

    const columns = [
        {
            title:"ClassCode",
            field:"classCode"
        },
        {
            title:"Class Segment",
            field:"classSegmentId"
        },
        {
            title:"Description",
            field:"description",
            flex: 1,
            minWidth: 400,
            renderCell: (params) => (
                <Box style={{ whiteSpace: 'normal', lineHeight: 'normal' }}>
                    {params.value}
                 </Box>
            ),
            // 3. Set the column to allow height calculation
                // This is the most crucial part for auto-height
            resizable: true, // Optional, but useful
        }
    ];
    useEffect(() => {
        getLogs();
    }, []);

    function getLogs(){
        request(
                    "get", 
                    "/general-classes/get-schedule-logs/" + versionId,
                    //"/course-v2/get-batches",
                    (res) => {    
                        setLogs(res.data);
                         console.log("getLogs, res = ",res.data);
                    },null
                ).catch((err) => {
                      // This will catch any other errors that might occur
                      console.error("Unexpected error:", err);
                      setLoading(false);
                      //toast.error(err.response.data);
                });
    }
      return (
        <div>
            <h3>Schedule Logs for Version ID: {versionId}</h3>
            <Paper sx={{ height: 400, width: '100%' }}>
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
                    rows={logs}
                    columns={columns}
                    //initialState={{ pagination: { paginationModel } }}
                    pageSizeOptions={[5, 10]}
                    checkboxSelection
                    sx={{ border: 0 }}
                    
                />
            </Paper>
        </div>        
    );
}