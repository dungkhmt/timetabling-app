
import React, { useCallback,useState, useEffect } from 'react';
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
import { title } from 'assets/jss/material-dashboard-react';
import { toast } from "react-toastify";

export default function VersionRoom({ versionId }) {

    const [rooms, setRooms] = useState([]);
    const [selectedRoomIds, setSelectedRoomIds] = useState([]);
    const [selectedRoomOfVersionIds, setSelectedRoomOfVersionIds] = useState([]);

    const [openAddRoomVersionDialog, setOpenAddRoomVersionDialog] = useState(false);
    const [allRooms, setAllRooms] = useState([]);
    const [loadingAllRooms, setLoadingAllRooms] = useState(false); // Loading cho DataGrid

    const columns= [
        {title:"ID", field:"roomId"},
        
    ];
    const columnsDialog = [
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
    const getAllRoomsOfBatch = useCallback(() => {
        setLoadingAllRooms(true);
        request(
            "get",
            "/timetabling-versions/get-rooms-of-batch/"   +versionId,
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

    function getRoomsByVersion(){
        request(
            "get",
            "/timetabling-versions/get-rooms-of-version/"+versionId,
            (res)=>{
                console.log(res);
                setRooms(res.data || []);
            },
            (error)=>{
                console.error(error);
            },
        );      
    }
    function handleCloseAddRoomDialog(){
        setOpenAddRoomVersionDialog(false); 
    }

    function handleRowClick(params){
        const roomId = params.row.roomId;
        //history.push(`/general-time-tabling/make-timetable/${versionId}`); // Navigate to the user details page   
    }

    function removeRoomsFromVersion(){
        if (selectedRoomOfVersionIds.length === 0) {
            toast.warn("Vui lòng chọn ít nhất một phòng để xóa.");
            return;
        }
            const payload = {
                versionId: versionId,
                roomIds: selectedRoomIds
            };
    
            // API endpoint này là giả định. Bạn cần thay thế bằng endpoint thực tế của mình.
            request(
                "post",
                "/timetabling-versions/remove-rooms-from-version",
    
                (res) => {
                    toast.success("Remove phòng thành công!");
                    //handleCloseAddRoomDialog();
                    // alert(JSON.stringify(payload));
                    getRoomsByVersion(); // Tải lại danh sách phòng của version để cập nhật giao diện
                },
                (err) => {
                    toast.error("Remove phòng vào batch thất bại.");
                    // alert(JSON.stringify(payload));
    
                    console.error(err);
                },
                 payload
            );
        
    
    }

        // Xử lý việc lưu các phòng đã chọn vào batch
        function handleSaveChanges() {
            if (selectedRoomIds.length === 0) {
                toast.warn("Vui lòng chọn ít nhất một phòng để thêm.");
                return;
            }
    
            const payload = {
                versionId: versionId,
                roomIds: selectedRoomIds
            };
    
            // API endpoint này là giả định. Bạn cần thay thế bằng endpoint thực tế của mình.
            request(
                "post",
                "/timetabling-versions/add-rooms-to-version",
    
                (res) => {
                    toast.success("Thêm phòng vào batch thành công!");
                    handleCloseAddRoomDialog();
                    // alert(JSON.stringify(payload));
                    getRoomsByVersion(); // Tải lại danh sách phòng của version để cập nhật giao diện
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
    
    useEffect(()=>{
        getRoomsByVersion();
    },[]);

    return (
        <div>
            VersionRoom {versionId}
            <Button variant="contained" onClick={()=>{
                setOpenAddRoomVersionDialog(true);
                getAllRoomsOfBatch();       
            }}>Thêm phòng vào Version</Button>

            <Button variant="contained" onClick={()=>{
                removeRoomsFromVersion();     
            }}>Remove phòng</Button>

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
                                rows={rooms}
                                columns={columnsDialog}
                                //initialState={{ pagination: { paginationModel } }}
                                pageSizeOptions={[5, 10]}
                                checkboxSelection
                                sx={{ border: 0 }}
                                onRowClick={handleRowClick}
                                onRowSelectionModelChange={(newSelectionModel) => {
                                            setSelectedRoomOfVersionIds(newSelectionModel); // Cập nhật state với các ID phòng được chọn
                                        }}
                                        rowSelectionModel={selectedRoomOfVersionIds}
                                        getRowId={(row) => row.id} // Giúp DataGrid xác định ID duy nhất cho mỗi hàng
                            />
                            
            <Dialog
                            open={openAddRoomVersionDialog}
                            onClose={handleCloseAddRoomDialog}
            
            
                        >
                            <DialogTitle>Thêm phòng vào Version</DialogTitle>
                            <DialogContent>
                                <Box sx={{
                                    height: 500, width: '100%',
            
                                }}>
                                    <DataGrid
            
                                        loading={loadingAllRooms}
                                        rows={allRooms}
                                        columns={columnsDialog}
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

        </div>
    );
}