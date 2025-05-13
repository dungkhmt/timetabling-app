import React from 'react';
import {
  Box,
  Button,
  CircularProgress,
  IconButton,
  Typography
} from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { Add, Delete, Edit } from "@mui/icons-material";
import localText from "./LocalText.js";

const COLUMNS = [
  {
    headerName: "Kíp thi",
    field: "name",
    width: 100,
    headerAlign: 'center',
    align: 'center',
  },
  {
    headerName: "Bắt đầu",
    field: "startTime",
    width: 100,
    headerAlign: 'center',
    align: 'center',
    valueFormatter: (params) => {
      if (!params.value) return '';
      const date = new Date(params.value);
      return date.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    sortComparator: (v1, v2) => {
      const date1 = v1 ? new Date(v1) : new Date(0);
      const date2 = v2 ? new Date(v2) : new Date(0);
      return date1.getTime() - date2.getTime();
    }
  },
  {
    headerName: "Kết thúc",
    field: "endTime",
    width: 100,
    headerAlign: 'center',
    align: 'center',
    valueFormatter: (params) => {
      if (!params.value) return '';
      const date = new Date(params.value);
      return date.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      });
    }
  },
];

const ExamSessionTable = ({
  sessions,
  isLoading,
  onAddSession,
  onEditSession,
  onDeleteSession,
  selectedCollection,
}) => {
  function DataGridTitle() {
    return (
      <Box
        sx={{
          width: "100%",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          pt:2,
          pb:6.5,
        }}
      >
        <Typography
          variant="h5"
          sx={{
            fontWeight: 700,
            color: '#1976d2',
            position: 'relative',
          }}
        >
          Danh Sách Kíp Thi
        </Typography>
      </Box>
    );
  }

  function DataGridToolbar() {
    return (
      <Box sx={{ px: 2, pb: 2 }}>
        {/* Session actions */}
        <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 2 }}>
          <Button
            variant="contained"
            color="primary"
            size="small"
            onClick={onAddSession}
            disabled={!selectedCollection || isLoading}
            startIcon={<Add />}
          >
            Thêm kíp thi
          </Button>
        </Box>
      </Box>
    );
  }

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
        <Box sx={{ display: 'flex', gap: 1 }}>
          <IconButton 
            size="small" 
            color="primary"
            onClick={(event) => {
              event.stopPropagation();
              onEditSession(params);
            }}
          >
            <Edit fontSize="small" />
          </IconButton>
          <IconButton 
            size="small" 
            color="error"
            onClick={(event) => {
              event.stopPropagation();
              onDeleteSession(params);
            }}
          >
            <Delete fontSize="small" />
          </IconButton>
        </Box>
      );
    }
  };

  return (
    <div style={{ height: 600, width: "100%" }}>
      {isLoading && (
        <CircularProgress
          style={{ position: "absolute", top: "50%", left: "50%" }}
        />
      )}
      <DataGrid
        localeText={localText}
        components={{
          Toolbar: () => (
            <>
              <DataGridTitle />
              <DataGridToolbar />
            </>
          ),
        }}
        autoHeight
        rows={sessions || []}
        columns={[...COLUMNS, actionColumn]}
        pageSizeOptions={[10, 20, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 10 } },
          sorting: {
            sortModel: [{ field: 'startTime', sort: 'asc' }],
          },
        }}
        disableRowSelectionOnClick
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
        }}
      />
    </div>
  );
};

export default ExamSessionTable;
