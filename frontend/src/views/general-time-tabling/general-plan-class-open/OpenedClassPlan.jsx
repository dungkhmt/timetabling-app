import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import { toast } from "react-toastify";
import React, {useEffect, useState} from "react";
import {Button, Dialog, DialogContent, DialogTitle, Paper, TextField,Autocomplete} from "@mui/material";

import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { request } from "api";

export default function OpenedClassPlan(){
    const {batchId} = useParams();
    const [classPlans, setClassPlans] = useState([]);

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

    }

    useEffect(() => {
            getListOpenClassPlans();
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
                                rows={classPlans}
                                columns={columns}
                                initialState={{ pagination:  { page: 0, pageSize: 5 } }}
                                pageSizeOptions={[5, 10]}
                                checkboxSelection
                                //sx={{ border: 0 }}
                                onRowClick={handleRowClick}
                            />
                            
                                
                        </Paper>
        </>
    );
}