import {request} from "api";
import React, {useEffect, useState} from "react";
import {DataGrid} from "@mui/x-data-grid";
import {Autocomplete, TextField} from "@mui/material";
export default function ListClassPlan() {

    const [classes, setClasses] = useState([]);
    const [semesters, setSemesters] = useState([]); // Đổi tên state để rõ ràng hơn
    const [selectedSemester, setSelectedSemester] = useState(null); // Thêm state cho học kỳ được chọn

    const payload = {
        semester: "20251",
    };

    const columns = [
        { field: "typeProgram", headerName: "Type Program", width: 120 },
        { field: "classCode", headerName: "Class Code", width: 100 },
        { field: "accompanyingClassCode", headerName: "Accompanying Class Code", width: 160 },
        { field: "courseId", headerName: "Course ID", width: 100 },
        { field: "note", headerName: "Note", width: 250 },
        { field: "part", headerName: "Part", width: 80 },
        { field: "dayOfWeek", headerName: "Day Of Week", width: 120 },
        { field: "startTime", headerName: "Start Time", width: 100 },
        { field: "endTime", headerName: "End Time", width: 100 },
        { field: "group", headerName: "Group", width: 100 },
        { field: "weeks", headerName: "Weeks", width: 150 },
        { field: "roomId", headerName: "Room ID", width: 100 },
        { field: "maxQuantity", headerName: "Max Quantity", width: 120 },
    ];

    function getAllClassesBySemester(semester) {
        request(
            "post",
            "/open-class-plan/get-all-class-plan-by-semester/"+ semester,
            (res) => {
                console.log(res);
                // alert(JSON.stringify(payload.semester));
                setClasses(res.data || []);
            },
            (error) => {
                console.error(error);
                // alert(JSON.stringify(payload.semester));
            }
        )
    }

    function getAllSemesters() {
        request(
            "get",
            "/open-class-plan/get-all-semesters",
            (res) => {
                console.log(res);
                setSemesters(res.data || []);
            },
            (error) => {
                console.error(error);
            }
        );
    }
    useEffect(() => {
        getAllSemesters();
    } , []);

    return (
        <div style={{ height: 600, width: "100%" }}>
            <h1 className="text-2xl font-bold mb-4">Danh sách lớp học</h1>

            <Autocomplete
                disablePortal
                options={semesters}
                value={selectedSemester}
                onChange={(event, newValue) => {
                    setSelectedSemester(newValue);
                    getAllClassesBySemester(newValue);
                }}
                sx={{ width: 130, mb: 2 }}
                renderInput={(params) => (
                    <TextField
                        {...params}
                        label="Chọn kỳ"
                        variant="outlined"
                        size="small"
                    />
                )}
            />
            <DataGrid
                rows={classes}
                columns={columns}
                getRowId={(row) => row.classCode}
                pageSizeOptions={[5, 10, 25]}
                initialState={{
                    pagination: { paginationModel: { pageSize: 10 } },
                }}
            />
        </div>
    );
}