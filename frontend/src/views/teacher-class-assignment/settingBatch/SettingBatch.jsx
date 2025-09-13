import {request} from "api";
import React, {useEffect, useState} from "react";
import {DataGrid} from "@mui/x-data-grid";
import {Autocomplete, Button, TextField} from "@mui/material";
import SettingBatchDialog from "./components/SettingBatchDialog";
import AddBatchDialog from "./components/AddBatchDialog";
import {useHistory} from "react-router-dom";

export default function SettingBatch() {

    const [semesters, setSemesters] = useState([]);
    const [selectedSemester, setSelectedSemester] = useState(null);
    const [batches, setBatches] = useState([]);
    const [openSettingBatchDialog, setOpenSettingBatchDialog] = useState(false);
    const [selectedBatch, setSelectedBatch] = useState(null); // Store the selected batch
    const [classes, setClasses] = useState([]);
    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
    const history = useHistory();


    const columns = [
        { field: "id", headerName: "ID", width: 120 },
        { field: "name", headerName: "Tên batch", width: 250 },
    ];

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

    const handleRowClick = (params) => {
        // Store the selected batch and open the dialog
        history.push(`/teacher-class-assignment/SettingBatch/${params.row.id}`);
        // setSelectedBatch(params.row);
        // setOpenSettingBatchDialog(true);
        // getAllClassesByBatchId(params.row.id);
    };

    const handleCloseSettingBatchDialog = () => {
        setOpenSettingBatchDialog(false);
        setSelectedBatch(null);
    };

    function handleAssignment() {
        // Sửa lỗi: Kiểm tra nếu đã chọn semester
        if (!selectedSemester) {
            alert("Vui lòng chọn kỳ trước khi phân lớp tự động!");
            return;
        }

        request(
            "post",
            // Sửa đường dẫn API - kiểm tra lại endpoint chính xác
            `/teacher-assignment/assign-classes-for-teachers/${selectedSemester}`,
            (res) => {
                console.log("Phân lớp thành công:", res);
                alert("Phân lớp tự động thành công!");
            },
            (error) => {
                console.error("Lỗi phân lớp:", error);
                alert("Có lỗi xảy ra khi phân lớp tự động!");
            }
        );
    }

    function getAllClassesByBatchId(BatchId) {
        request(
            "get",
            `/teacher-assignment-batch-class/get-all-classes-by-batch/${BatchId}`,
            (res) => {
                console.log(res);
                setClasses(res.data || []);
            },
            (error) => {
                console.error(error);
            }
        );
    }

    const handleAddBatchClick = () => {
        if (!selectedSemester) {
            alert("Vui lòng chọn kỳ trước khi thêm batch mới!");
            return;
        }
        setIsAddDialogOpen(true);
    };


    useEffect(() => {
        getAllSemesters();
    }, []);

    return (
        <div style={{ height: 600, width: "100%" }}>
            <h1 className="text-2xl font-bold mb-4">Danh sách batch</h1>

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

            <Button
                onClick={handleAddBatchClick}
                variant="contained"
                sx={{mb: 2}}
            >
                Thêm Batch Mới
            </Button>

            <Button
                onClick={handleAssignment}
                variant="contained"
                sx={{mb: 2, ml: 2}}
            >
                Phân lớp tự động
            </Button>

            <AddBatchDialog
                open={isAddDialogOpen}
                onClose={() => setIsAddDialogOpen(false)}
                semester={selectedSemester}
                onBatchAdded={(newBatch) => {
                    // Xử lý khi batch mới được thêm
                    // getAllBatchBySemester()
                    getAllBatchBySemester(selectedSemester)
                    console.log("Batch mới:", newBatch);
                }}
            />

            <DataGrid
                rows={batches}
                columns={columns}
                getRowId={(row) => row.id}
                pageSizeOptions={[5, 10, 25]}
                initialState={{
                    pagination: { paginationModel: { pageSize: 10 } },
                }}
                onRowClick={handleRowClick}
            />

            {/* Setting Batch Dialog */}
            <SettingBatchDialog
                open={openSettingBatchDialog}
                onClose={handleCloseSettingBatchDialog}
                selectedBatch={selectedBatch}
            />
        </div>
    );
}