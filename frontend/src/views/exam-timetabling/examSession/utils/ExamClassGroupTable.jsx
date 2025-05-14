import React, { useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  IconButton,
  Typography,
  Paper,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Delete, Edit, LockOutlined } from "@mui/icons-material";
import localText from "./LocalText";

const COLUMNS = [
  {
    headerName: "Tên nhóm",
    field: "name",
    flex: 1,
    headerAlign: 'center',
    align: 'center',
  },
  {
    headerName: "Trạng thái",
    field: "using",
    width: 150,
    headerAlign: 'center',
    align: 'center',
    valueFormatter: (params) => params.value ? "Đang sử dụng" : "Không sử dụng",
    cellClassName: (params) => params.value ? "status-active" : "status-inactive",
  },
];

const ExamClassGroupTable = ({
  classGroups,
  isLoading,
  onAddGroup,
  onEditGroup,
  onDeleteMultiGroups
}) => {
  const [selectedRows, setSelectedRows] = useState([]);
  
  const handleSelectionChange = (newSelection) => {
    const validSelection = newSelection.filter(id => {
      const group = classGroups.find(g => g.id === id);
      return group && group.using === false;
    });
    setSelectedRows(validSelection);
  };

  const isRowSelectable = (params) => {
    return !params.row.using;
  };

  const actionColumn = {
    headerName: "Thao tác",
    field: "actions",
    width: 120,
    sortable: false,
    filterable: false,
    headerAlign: 'center',
    align: 'center',
    renderCell: (params) => {
      return (
        <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
          <IconButton 
            size="small" 
            color="primary"
            onClick={(event) => {
              event.stopPropagation();
              onEditGroup(params);
            }}
          >
            <Edit fontSize="small" />
          </IconButton>
        </Box>
      );
    }
  };

  return (
    <Paper sx={{ width: "100%", p: 2 }}>
      <Box sx={{ 
        display: "flex", 
        justifyContent: "center", 
        alignItems: "center", 
        mb: 2 
      }}>
        <Typography
          variant="h5"
          sx={{
            fontWeight: 700,
            color: '#1976d2',
          }}
        >
          Danh Sách Nhóm Lớp
        </Typography>
      </Box>

      <Box sx={{ 
        display: "flex", 
        justifyContent: "flex-end", 
        alignItems: "center", 
        mb: 2 
      }}>
        <Box sx={{ display: "flex", gap: 2 }}>
          <Button
            variant="contained"
            color="error"
            size="small"
            onClick={() => onDeleteMultiGroups(selectedRows)}
            disabled={selectedRows.length === 0 || isLoading}
            startIcon={<Delete />}
          >
            Xóa ({selectedRows.length})
          </Button>
          
          <Button
            variant="contained"
            color="primary"
            size="small"
            onClick={onAddGroup}
            disabled={isLoading}
            startIcon={<Add />}
          >
            Thêm nhóm lớp
          </Button>
        </Box>
      </Box>
      
      <div style={{ height: 600, width: "100%", position: "relative" }}>
        {isLoading && (
          <CircularProgress
            style={{ position: "absolute", top: "50%", left: "50%", zIndex: 1 }}
          />
        )}
        <DataGrid
          localeText={localText}
          slots={{ toolbar: GridToolbar }}
          slotProps={{
            toolbar: {
              showQuickFilter: true,
              quickFilterProps: { debounceMs: 500 },
              printOptions: { disableToolbarButton: true },
              csvOptions: { disableToolbarButton: true },
              disableColumnFilter: true,
              disableDensitySelector: true,
              disableColumnSelector: true,
            },
          }}
          autoHeight
          rows={classGroups || []}
          columns={[...COLUMNS, actionColumn]}
          pageSizeOptions={[10, 20, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } },
          }}
          checkboxSelection
          isRowSelectable={isRowSelectable}
          onRowSelectionModelChange={handleSelectionChange}
          sx={{
            '& .MuiDataGrid-columnHeaders': {
              backgroundColor: '#5495e8',
              color: '#fff',
              fontSize: '15px',
              fontWeight: 'bold',
            },
            '& .MuiDataGrid-row:nth-of-type(even)': {
              backgroundColor: '#f9f9f9',
            },
            '& .MuiDataGrid-columnHeader': {
              '&:focus': {
                outline: 'none',
              },
            },
            '& .MuiDataGrid-columnHeaderTitle': {
              fontWeight: 'bold',
            },
            '& .status-active': {
              color: 'green',
              fontWeight: 'bold',
            },
            '& .status-inactive': {
              color: 'grey',
            },
          }}
        />
      </div>
    </Paper>
  );
};

export default ExamClassGroupTable;
