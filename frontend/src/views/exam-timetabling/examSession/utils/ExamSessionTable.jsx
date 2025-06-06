import React from 'react';
import {
  Box,
  Button,
  CircularProgress,
  IconButton,
  Typography,
  Tooltip,
  Chip,
  Paper
} from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { Add, Delete, Edit, LockOutlined } from "@mui/icons-material";
import localText from "./LocalText.js";

const COLUMNS = [
  {
    headerName: "Kíp",
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
  {
    headerName: "Trạng thái",
    field: "using",
    width: 150,
    headerAlign: 'center',
    align: 'center',
    renderCell: (params) => {
      return params.value ? (
        <Chip 
          label="Đang sử dụng" 
          color="success" 
          size="small" 
          sx={{ 
            fontSize: '0.75rem',
            fontWeight: 'bold'
          }} 
        />
      ) : (
        <Chip 
          label="Không sử dụng" 
          color="default" 
          size="small" 
          sx={{ fontSize: '0.75rem' }} 
        />
      );
    },
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
          
          {params.row.using ? (
            <Tooltip title="Kíp thi đang được sử dụng, không thể xóa">
              <span>
                <IconButton 
                  size="small" 
                  color="default"
                  disabled
                >
                  <LockOutlined fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          ) : (
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
          )}
        </Box>
      );
    }
  };

  return (
        <Paper sx={{ width: "100%", p: 1.5, overflow: 'hidden' }}>
      {/* Title */}
      <Box sx={{ 
        display: "flex", 
        justifyContent: "center", 
        alignItems: "center", 
        mb: 6 
      }}>
        <Typography
          variant="h5"
          sx={{
            fontWeight: 700,
            color: '#1976d2',
          }}
        >
          Danh Sách Kíp Thi
        </Typography>
      </Box>

      {/* Buttons */}
      <Box sx={{ 
        display: "flex", 
        justifyContent: "flex-end", 
        alignItems: "center", 
        mb: 3 
      }}>
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
      
      {/* DataGrid */}
      <Box sx={{ width: "100%", position: "relative" }}>
        {isLoading && (
          <CircularProgress
            style={{ position: "absolute", top: "50%", left: "50%", zIndex: 1 }}
          />
        )}
        <DataGrid
          localeText={localText}
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
            '& .MuiDataGrid-main': {
              overflow: 'hidden',
            },
            '& .MuiDataGrid-virtualScroller': {
              overflow: 'auto',
            },
            '& .MuiDataGrid-cell': {
              whiteSpace: 'normal',
              wordWrap: 'break-word',
            }
          }}
        />
      </Box>
    </Paper>
  );
};

export default ExamSessionTable;
