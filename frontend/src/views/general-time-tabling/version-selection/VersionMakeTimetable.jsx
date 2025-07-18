
import { useParams } from "react-router-dom/cjs/react-router-dom.min";
import {request} from "api";

import React, {useEffect, useState} from "react";
import { 
  Paper, 
  Typography, 
  Button, 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions,
  DialogContentText,
  TextField,
  Grid,
  Box,
  CircularProgress,
  InputAdornment,
  IconButton,
  Card,
  CardContent,
  Chip,
  Container,
  Avatar,
  useTheme,
  CardActions,
  Tooltip,
  CardHeader,
  Menu,
  MenuItem
} from "@mui/material";
import { 
  Add, 
  Search, 
  Clear, 
  CalendarMonth, 
  AccessTime, 
  School,
  MoreVert,
  Drafts,
  CheckCircle,
  FilterAlt,
  Delete,
  Edit
} from "@mui/icons-material";

import { toast } from "react-toastify";

import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import {Link} from "react-router-dom";
//import { useNavigate } from 'react-router-dom';
import { useHistory } from 'react-router-dom';

export default function VersionMakeTimetable(){
    const {batchId} = useParams();
    const [versions, setVersions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [openAddVersionDialog,setOpenAddVersionDialog] = useState(false);
    const [versionName, setVersionName] = useState("");
    const [versionStatus, setVersionStatus] = useState("");
    const [numberSlotsPerSession, setNumberSlotsPerSession] = useState(6);
    const [isCreating, setIsCreating] = useState(false);


    const columns = [
        {
            title: "ID",
            field: "id"
        },
        {
            title: "Name",
            field: "name"
        },
        {
            title: "created by",
            field: "createdByUserId"
        },
    ];    

    function handleRowClick(){

    }
    function getVersions(){
        request(
                            "get",
                            "/timetabling-versions/get-version-of-batch?batchId=" + batchId,
                            (res)=>{
                                console.log(res);
                                setVersions(res.data || []);
                                setLoading(false);
                            },
                            (error)=>{
                                console.error(error);
                                setError(error);
                            },
                        );
    }

    function handleAdd(){
        //alert('add');
        setOpenAddVersionDialog(true);
    }
    function handleCreateVersion(){
        let payLoad = {
            name: versionName,
            status: versionStatus,
            semester:"",
            userId:"",
            numberSlotsPerSession: numberSlotsPerSession,
            batchId: batchId
        };

        request(
            "post",
            "/timetabling-versions/create",
            //"/course-v2/add-batch",
            (res) => {
                //successNoti("Batch created successfully");
                //sleep(1000).then(() => {
                //  history.push("/programming-contest/contest-manager/" + res.data.contestId);
                //});
                getVersions();
                setOpenAddVersionDialog(false);
            },
            {
                onError: (err) => {
                    //errorNoti(err?.response?.data?.message, 5000);
                    alert('ERROR');
            },
            },
            payLoad 
    )
      .then()
      .finally(() => setLoading(false));

    }
    useEffect(() => {
        getVersions();

    }, []);
    return (
        <>
            <Paper sx={{ height: 400, width: '100%' }}>
                <Button
                    onClick = {handleAdd}
                >
                   ADD
                </Button>
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
                    rows={versions}
                    columns={columns}
                    //initialState={{ pagination: { paginationModel } }}
                    pageSizeOptions={[5, 10]}
                    checkboxSelection
                    sx={{ border: 0 }}
                    onRowClick={handleRowClick}
                />
            </Paper>
      <Dialog 
        open={openAddVersionDialog} 
        onClose={() => !isCreating && setOpenAddVersionDialog(false)}
        maxWidth="xs"
        fullWidth
        disableEscapeKeyDown={isCreating}
      >
        <DialogTitle sx={{ fontSize: { xs: '1.1rem', sm: '1.25rem' } }}>Tạo phiên bản TKB mới</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          {isCreating && (
            <Box sx={{ 
              position: 'absolute', 
              top: 0, 
              left: 0, 
              right: 0, 
              bottom: 0, 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              backgroundColor: 'rgba(255, 255, 255, 0.8)',
              zIndex: 1,
              borderRadius: 1
            }}>
              <CircularProgress />
            </Box>
          )}          
          
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>

            
            <TextField
              autoFocus
              label="Tên phiên bản"
              size="small"
              fullWidth
              value={versionName}
              onChange={(e) => setVersionName(e.target.value)}
              required
              disabled={isCreating}
            />
              <TextField
              select
              label="Trạng thái"
              size="small"
              fullWidth
              value={versionStatus}
              onChange={(e) => setVersionStatus(e.target.value)}
              disabled={isCreating}
              SelectProps={{
                MenuProps: {
                  PaperProps: {
                    sx: { maxHeight: 200 }
                  }
                }
              }}
            >
              <MenuItem value="DRAFT">Bản nháp</MenuItem>
              <MenuItem value="PUBLISHED">Đã xuất bản</MenuItem>
            </TextField>
            
            <TextField
              label="Số tiết mỗi buổi học"
              type="number"
              size="small"
              fullWidth
              value={numberSlotsPerSession}
              onChange={(e) => setNumberSlotsPerSession(e.target.value)}
              required
              disabled={isCreating}
              InputProps={{
                inputProps: { min: 1 }
              }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: { xs: 1.5, sm: 2 }, pt: { xs: 1, sm: 1 } }}>
          <Button 
            onClick={() => setOpenAddVersionDialog(false)}
            disabled={isCreating}
            variant="outlined"
            sx={{ fontSize: { xs: '0.8rem', sm: '0.875rem' } }}
          >
            Hủy
          </Button>          
          <Button 
            onClick={handleCreateVersion} 
            variant="contained" 
            disabled={isCreating || !versionName || !versionStatus || !numberSlotsPerSession || parseInt(numberSlotsPerSession) < 1}
            startIcon={isCreating ? <CircularProgress size={20} color="inherit" /> : null}
            sx={{ fontSize: { xs: '0.8rem', sm: '0.875rem' } }}
          >
            {isCreating ? 'Đang tạo...' : 'Tạo phiên bản TKB'}
          </Button>
        </DialogActions>
      </Dialog>
                     
        </>
    );
}