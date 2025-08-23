import {request} from "api";
import React, {useEffect, useState} from "react";
import {DataGrid} from "@mui/x-data-grid";
import {Autocomplete, TextField} from "@mui/material";
import SettingBatchDialog from "../components/SettingBatchDialog";

export default function SettingBatch() {

    const [semesters, setSemesters] = useState([]);
    const [selectedSemester, setSelectedSemester] = useState(null);
    const [batches, setBatches] = useState([]);
    const [openSettingBatchDialog, setOpenSettingBatchDialog] = useState(false);
    const [selectedBatch, setSelectedBatch] = useState(null); // Store the selected batch

    const columns = [
        { field: "id", headerName: "ID", width: 120 },
        { field: "name", headerName: "Tên batch", width: 120 },
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
        setSelectedBatch(params.row);
        setOpenSettingBatchDialog(true);
    };

    const handleCloseSettingBatchDialog = () => {
        setOpenSettingBatchDialog(false);
        setSelectedBatch(null);
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