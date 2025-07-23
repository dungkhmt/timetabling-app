// RoomsOfBatch.jsx
import { toast } from "react-toastify";
import React, { useCallback, useEffect, useState } from "react";
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Box,
    Chip,
    Typography,
    Stack,
    CircularProgress
} from "@mui/material";
import { request } from "api"; // Giả định 'api' là một module tùy chỉnh để gọi API
import { DataGrid, GridToolbar } from "@mui/x-data-grid";

export default function RoomsOfBatch({ batchId }) {
    // State để lưu danh sách các phòng đã thuộc về batch hiện tại
    const [batchRooms, setBatchRooms] = useState([]);
    // State để lưu danh sách tất cả các phòng có sẵn để hiển thị trong dialog
    const [allRooms, setAllRooms] = useState([]);
    // State để lưu ID của các phòng được chọn trong DataGrid
    const [selectedRoomIds, setSelectedRoomIds] = useState([]);

    const [openAddRoomBatchDialog, setOpenAddRoomBatchDialog] = useState(false);
    const [loading, setLoading] = useState(false); // Loading cho danh sách phòng của batch
    const [loadingAllRooms, setLoadingAllRooms] = useState(false); // Loading cho DataGrid

    // Hàm lấy danh sách các phòng đã được gán cho batch
    const getBatchRooms = useCallback(() => {
        setLoading(true);
        request(
            "get",
            `/timetabling-batch/get-batch-room/${batchId}`,
            (res) => {
                setBatchRooms(res.data);
            },
            (err) => {
                toast.error("Lỗi khi tải danh sách phòng của batch!");
                console.error(err);
            }
        ).finally(() => {
            setLoading(false);
        });
    }, [batchId]);

    // Hàm lấy tất cả các phòng học có sẵn
    const getAllRooms = useCallback(() => {
        setLoadingAllRooms(true);
        request(
            "get",
            "/classroom/get-all",
            (res) => {
                setAllRooms(res.data);
            },
            (err) => {
                toast.error("Lỗi khi tải danh sách phòng!");
                console.error(err);
            }
        ).finally(() => {
            setLoadingAllRooms(false);
        });
    }, []);

    // useEffect chỉ chạy một lần khi component được mount hoặc khi batchId thay đổi
    useEffect(() => {
        getBatchRooms();
    }, [getBatchRooms]); // Phụ thuộc chính xác


    // Định nghĩa các cột cho DataGrid
    const columns = [
        {
            field: 'id',
            headerName: 'Mã phòng',
            width: 150,
            align: 'center',
            headerAlign: 'center'
        },
        {
            field: 'quantityMax',
            headerName: 'Sức chứa',
            type: 'number',
            width: 150,
            align: 'center',
            headerAlign: 'center'
        },
        {
            field: 'buildingName',
            headerName: 'Tòa nhà',
            width: 150,
            align: 'center',
            headerAlign: 'center',

            valueGetter: (params) => params.row.building?.name || 'Không xác định',
        }
    ];

    // Mở dialog và tải danh sách tất cả các phòng
    function handleOpenAddRoomDialog() {
        setOpenAddRoomBatchDialog(true);
        getAllRooms();
    }

    function handleCloseAddRoomDialog() {
        setOpenAddRoomBatchDialog(false);
        setSelectedRoomIds([]); // Xóa các lựa chọn khi đóng dialog
    }

    // Xử lý việc lưu các phòng đã chọn vào batch
    function handleSaveChanges() {
        if (selectedRoomIds.length === 0) {
            toast.warn("Vui lòng chọn ít nhất một phòng để thêm.");
            return;
        }

        const payload = {
            batchId: batchId,
            roomIds: selectedRoomIds
        };

        // API endpoint này là giả định. Bạn cần thay thế bằng endpoint thực tế của mình.
        request(
            "post",
            "/timetabling-batch/add-rooms-to-batch",

            (res) => {
                toast.success("Thêm phòng vào batch thành công!");
                handleCloseAddRoomDialog();
                // alert(JSON.stringify(payload));
                getBatchRooms(); // Tải lại danh sách phòng của batch để cập nhật giao diện
            },
            (err) => {
                toast.error("Thêm phòng vào batch thất bại.");
                // alert(JSON.stringify(payload));

                console.error(err);
            },
             payload
        );
        // alert(JSON.stringify(payload));

    }
    // ... các import khác giữ nguyên

    const handleDeleteRoom = (roomId) => {
        if (!window.confirm(`Bạn có chắc muốn xóa phòng ${roomId} khỏi batch này?`)) {
            return;
        }

        request(
            "delete",
            `/timetabling-batch/remove-room-from-batch?batchId=${batchId}&roomId=${roomId}`,
            (res) => {
                toast.success(`Đã xóa phòng ${roomId} khỏi batch thành công!`);
                getBatchRooms(); // Tải lại danh sách phòng
            },
            (err) => {
                if (err.response?.status === 404) {
                    toast.error(`Phòng ${roomId} không tồn tại trong batch này`);
                } else {
                    toast.error(`Xóa phòng ${roomId} thất bại!`);
                }
                console.error(err);
            }
        );
        alert(JSON.stringify({batchId: batchId,roomId: roomId} ));
    };


    return (
        <Box sx={{
            p: 2,

        }}>
            <Typography variant="h6" gutterBottom>
                Các phòng học của Batch {batchId}
            </Typography>

            <Button
                // variant="contained"
                sx={{ mb: 2 }}
                onClick={handleOpenAddRoomDialog}
            >
                Thêm phòng
            </Button>

            {loading ? (
                <CircularProgress />
            ) : (
                <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
                    {batchRooms.length > 0 ? batchRooms.map((room) => (
                        <Chip
                            key={room.id} // Sử dụng key duy nhất như room.id
                            label={room.roomId}
                            variant="outlined"
                            onDelete={() => handleDeleteRoom(room.roomId)}
                            sx={{ fontSize: '1rem', padding: 1 }}
                        />
                    )) : (
                        <Typography>Chưa có phòng nào được gán cho batch này.</Typography>
                    )}
                </Stack>
            )}

            <Dialog
                open={openAddRoomBatchDialog}
                onClose={handleCloseAddRoomDialog}


            >
                <DialogTitle>Thêm phòng vào Batch</DialogTitle>
                <DialogContent>
                    <Box sx={{
                        height: 500, width: '100%',

                    }}>
                        <DataGrid

                            loading={loadingAllRooms}
                            rows={allRooms}
                            columns={columns}
                            initialState={{
                                sorting: {
                                    sortModel: [{ field: 'id', sort: 'asc' }],
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
                            pageSizeOptions={[5, 10, 20]}
                            checkboxSelection
                            onRowSelectionModelChange={(newSelectionModel) => {
                                setSelectedRoomIds(newSelectionModel); // Cập nhật state với các ID phòng được chọn
                            }}
                            rowSelectionModel={selectedRoomIds}
                            getRowId={(row) => row.id} // Giúp DataGrid xác định ID duy nhất cho mỗi hàng
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseAddRoomDialog}>Hủy</Button>
                    <Button onClick={handleSaveChanges} variant="contained">Lưu thay đổi</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}