import {request} from "api";
import React, {useEffect, useState} from "react";
import {DataGrid} from "@mui/x-data-grid";
import {Autocomplete, TextField} from "@mui/material";
export default function ListClassBySchool() {

    const [classes, setClasses] = useState([]);
    const [semesters, setSemesters] = useState([]); // Đổi tên state để rõ ràng hơn
    const [selectedSemester, setSelectedSemester] = useState(null); // Thêm state cho học kỳ được chọn
    const [schools, setSchools] = useState([]); // Thêm state cho danh sách trường
    const [selectedSchool, setSelectedSchool] = useState(null); // Thêm state cho trường được chọn
    const [batches, setBatches] = useState([]);

    const columns = [
        { field: "id", headerName: "ID", width: 120 },
        { field: "name", headerName: "Tên batch", width: 120 },

    ];

    function getAllClassesBatchBySemester(semester) {
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
    function getAllBatchBySemester(semester) {

        request(
            "get",
            `/teacher-assignment-batch/get-batch-semester/${semester}`,
            (res) => {
                console.log(res);
                setBatches(res.data || []);
            },
            (error) => {
                console.error(error);
            }
        );

    }


    function getAllSchools() {
        request(
            "get",
            "/studying-course/get-all-school",
            (res) => {
                console.log(res);
                setSchools(res.data || []);
            },
            (error) => {
                console.error(error);
            }
        );
    }
    useEffect(() => {
        getAllSemesters();
        getAllSchools();
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
                    getAllBatchBySemester(newValue);

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
                rows={ batches}
                columns={columns}
                getRowId={(row) => row.id}
                pageSizeOptions={[5, 10, 25]}
                initialState={{
                    pagination: { paginationModel: { pageSize: 10 } },
                }}
            />
        </div>
    );
}