import { request } from "api";
import React, { useEffect, useState } from "react";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Autocomplete, TextField, Button, Box, Select, MenuItem, FormControl, InputLabel } from "@mui/material";

export default function ListClassPlan() {
    const [classes, setClasses] = useState([]);
    const [semesters, setSemesters] = useState([]);
    const [selectedSemester, setSelectedSemester] = useState(null);
    const [batches, setBatches] = useState([]);
    const [selectedBatch, setSelectedBatch] = useState({}); // Lưu batch đã chọn cho từng lớp
    const [loading, setLoading] = useState(false);



    const columns = [
        { field: "typeProgram", headerName: "Mã quản lý", width: 120 },
        { field: "classId", headerName: "Mã lớp", width: 100 },
        { field: "accompaniedClassId", headerName: "Lớp đính kèm", width: 160 },
        { field: "courseId", headerName: "Mã HP", width: 100 },
        { field: "note", headerName: "Ghi chú", width: 250 },
        { field: "maxStudents", headerName: "Max Student", width: 120 },
        {
            field: "batchSelection",
            headerName: "Chọn Batch",
            width: 200,
            renderCell: (params) => {
                const classId = params.row.classId;
                const defaultBatchId = params.row.batchClass?.id?.batchId;
                const defaultBatch = batches.find(batch => batch.id === defaultBatchId);

                return (
                    <FormControl fullWidth size="small">
                        <InputLabel>Chọn batch</InputLabel>
                        <Select
                            value={selectedBatch[classId] || (defaultBatch ? defaultBatch.id : "")}
                            label="Chọn batch"
                            onChange={(event) => handleBatchChange(classId, event.target.value)}
                        >
                            <MenuItem value="">
                                <em>None</em>
                            </MenuItem>
                            {batches.map((batch) => (
                                <MenuItem key={batch.id} value={batch.id}>
                                    {batch.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                );
            }
        },

        {
            field: "confirm",
            headerName: "Xác nhận",
            width: 120,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color="primary"
                    size="small"
                    onClick={() => handleConfirm(params.row.classId)}
                    disabled={!selectedBatch[params.row.classId]}
                >
                    Xác nhận
                </Button>
            )
        }
    ];

    function getAllClassesBySemester(semester) {
        request(
            "get",
            "/teacher-assignment-opened-class/get-all-classes-by-semester/" + semester,
            (res) => {
                console.log(res);
                setClasses(res.data || []);

                // Reset selectedBatch khi load lớp mới
                const initialBatchState = {};
                (res.data || []).forEach(classItem => {
                    initialBatchState[classItem.classId] = "";
                });
                setSelectedBatch(initialBatchState);
            },
            (error) => {
                console.error(error);
            }
        );
    }

    function getAllSemesters() {
        request(
            "get",
            "/teacher-assignment-opened-class/get-all-distinct-semester",
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

    const handleBatchChange = (classId, batchId) => {
        setSelectedBatch(prev => ({
            ...prev,
            [classId]: batchId
        }));
    };

    const handleConfirm = (classId) => {
        const batchId = selectedBatch[classId];
        if (!batchId) {
            alert("Vui lòng chọn batch trước khi xác nhận");
            return;
        }

        setLoading(true);

        // Thực hiện request ở đây
        // Ví dụ: gửi classId và batchId lên server
        const payload = {
            classId: classId,
            batchId: batchId
        };

        alert(JSON.stringify(payload))

        // request(
        //     "post",
        //     `/teacher-assignment-batch-class/create-batch-class/${batchId}/${classId}`, // Thay bằng endpoint thực tế
        //     (res) => {
        //         console.log("Request thành công:", res);
        //         alert(`Đã xác nhận lớp ${classId} với batch ${batchId}`);
        //         setLoading(false);
        //     },
        //     (error) => {
        //         console.error("Request thất bại:", error);
        //         alert("Có lỗi xảy ra khi xác nhận");
        //         setLoading(false);
        //     },
        // );
    };

    const handleBatchChangeForAll = (batchId) => {
        const newSelectedBatch = {};
        classes.forEach(classItem => {
            newSelectedBatch[classItem.classId] = batchId;
        });
        setSelectedBatch(newSelectedBatch);
    };



    useEffect(() => {
        getAllSemesters();
    }, []);

    return (
        <div style={{ height: 600, width: "100%" }}>
            <h1 className="text-2xl font-bold mb-4">Danh sách lớp học</h1>

            <Box display="flex" alignItems="center" gap={2} mb={2}>
                <Autocomplete
                    disablePortal
                    options={semesters}
                    value={selectedSemester}
                    onChange={(event, newValue) => {
                        setSelectedSemester(newValue);
                        if (newValue) {
                            getAllClassesBySemester(newValue);
                            getAllBatchBySemester(newValue);
                        }
                    }}
                    sx={{ width: 130 }}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            label="Chọn kỳ"
                            variant="outlined"
                            size="small"
                        />
                    )}
                />

                {batches.length > 0 && (
                    <FormControl size="small" sx={{ width: 200 }}>
                        <InputLabel>Chọn batch cho tất cả</InputLabel>
                        <Select
                            value=""
                            label="Chọn batch cho tất cả"
                            onChange={(event) => handleBatchChangeForAll(event.target.value)}
                        >
                            <MenuItem value="">
                                <em>Chọn batch</em>
                            </MenuItem>
                            {batches.map((batch) => (
                                <MenuItem key={batch.batchId} value={batch.batchId}>
                                    {batch.batchName}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                )}
            </Box>

            <DataGrid
                slots={{ toolbar: GridToolbar }}
                rows={classes}
                columns={columns}
                getRowId={(row) => row.classId}
                pageSizeOptions={[5, 10, 25]}
                initialState={{
                    pagination: { paginationModel: { pageSize: 10 } },
                }}
                loading={loading}
            />
        </div>
    );
}