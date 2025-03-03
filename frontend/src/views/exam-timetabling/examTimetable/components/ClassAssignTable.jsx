import { forwardRef, useImperativeHandle, useState, useMemo, useCallback, useEffect } from 'react';
import {
  Box,
  CircularProgress,
  FormControl,
  InputAdornment,
  MenuItem,
  Select,
  TextField,
  Typography,
  Checkbox,
  Button,
  Popover,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton
} from '@mui/material';
import {
  Search,
  FilterList,
  ViewColumn
} from '@mui/icons-material';
import { DataGrid, GridToolbarContainer } from '@mui/x-data-grid';
// Conflict validation is now handled via API

function CustomToolbar(props) {
  const { visibleColumns, setVisibleColumns, allColumns } = props;
  const [anchorEl, setAnchorEl] = useState(null);

  const handleColumnVisibilityClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleColumnToggle = (field) => {
    if (visibleColumns.includes(field)) {
      setVisibleColumns(visibleColumns.filter(col => col !== field));
    } else {
      setVisibleColumns([...visibleColumns, field]);
    }
  };

  return (
    <GridToolbarContainer>
      <Button 
        startIcon={<ViewColumn />}
        onClick={handleColumnVisibilityClick}
        size="small"
        sx={{ ml: 1 }}
      >
        Hiển thị cột
      </Button>
      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left',
        }}
      >
        <List sx={{ width: 250 }}>
          {allColumns.map((column) => (
            <ListItem 
              key={column.field} 
              button 
              onClick={() => handleColumnToggle(column.field)}
              dense
            >
              <ListItemIcon>
                <Checkbox
                  edge="start"
                  checked={visibleColumns.includes(column.field)}
                  tabIndex={-1}
                  disableRipple
                />
              </ListItemIcon>
              <ListItemText primary={column.headerName} />
            </ListItem>
          ))}
        </List>
      </Popover>
    </GridToolbarContainer>
  );
}

/**
 * A data grid component for managing exam class assignments
 * Optimized for performance with virtualization and memoization
 */
const ClassesTable = forwardRef(({ 
  classesData, 
  isLoading,
  rooms,
  weeks,
  dates,
  slots,
  onSelectionChange // Add this prop to handle selection changes
}, ref) => {
  const [searchText, setSearchText] = useState('');
  const [activeSearchText, setActiveSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [descriptionFilter, setDescriptionFilter] = useState('all');
  const [activeFilters, setActiveFilters] = useState(false);
  const [uniqueDescriptions, setUniqueDescriptions] = useState([]);
  const [assignmentChanges, setAssignmentChanges] = useState({});
  const [page, setPage] = useState(0);
  const [selectedRows, setSelectedRows] = useState([]); // Add state for selected rows
  const [visibleColumns, setVisibleColumns] = useState([
    'roomId', 'weekNumber', 'date', 'sessionId', 'examClassId', 'classId', "courseId",
     'numberOfStudents', 'description',
  ]);

  useEffect(() => {
    if (classesData && classesData.length > 0) {
      const descSet = new Set();
      const descCountMap = {};
      
      // Count occurrences of each description and add to set
      for (let i = 0; i < classesData.length; i++) {
        const desc = classesData[i].description;
        if (desc !== null && desc !== undefined && desc !== '') {
          descSet.add(desc);
          descCountMap[desc] = (descCountMap[desc] || 0) + 1;
        }
      }
      
      // Get only the most frequent descriptions (top 10)
      const topDescriptions = Array.from(descSet)
        .sort((a, b) => descCountMap[b] - descCountMap[a])
        .slice(0, 10);
      
      setUniqueDescriptions(topDescriptions);
    }
  }, [classesData]);

  // Handle row selection change
  const handleSelectionModelChange = (newSelectionModel) => {
    setSelectedRows(newSelectionModel);
    // Call the parent component's handler if provided
    if (onSelectionChange) {
      onSelectionChange(newSelectionModel);
    }
  };

  const handleSearchChange = (event) => {
    setSearchText(event.target.value);
  };

  const handleSearchSubmit = () => {
    setActiveSearchText(searchText);
    setPage(0); // Reset to first page on search
    setActiveFilters(!!searchText || statusFilter !== 'all' || descriptionFilter !== 'all');
  };

  const handleSearchKeyPress = (event) => {
    if (event.key === 'Enter') {
      handleSearchSubmit();
    }
  };
  
  const handleStatusFilterChange = (event) => {
    setStatusFilter(event.target.value);
    setPage(0); // Reset to first page on filter change
    setActiveFilters(!!activeSearchText || event.target.value !== 'all' || descriptionFilter !== 'all');
  };

  const handleDescriptionFilterChange = (event) => {
    setDescriptionFilter(event.target.value);
    setPage(0); // Reset to first page on filter change
    setActiveFilters(!!activeSearchText || statusFilter !== 'all' || event.target.value !== 'all');
  };
  
  const handleClearFilters = () => {
    setSearchText('');
    setActiveSearchText('');
    setStatusFilter('all');
    setDescriptionFilter('all');
    setPage(0);
    setActiveFilters(false);
  };
  
  const lowerSearchText = activeSearchText ? activeSearchText.toLowerCase() : '';
  
  const filteredClasses = useMemo(() => {
    let results = classesData;
    
    // Apply description filter
    if (descriptionFilter !== 'all' && descriptionFilter !== 'other') {
      results = results.filter(item => item.description === descriptionFilter);
    } else if (descriptionFilter === 'other' && activeSearchText) {
      // When "other" is selected, filter descriptions based on search text
      results = results.filter(item => 
        item.description && item.description.toLowerCase().includes(lowerSearchText)
      );
    }
    
    // Apply search text filter (only if not using search for descriptions)
    if (activeSearchText && descriptionFilter !== 'other') {
      results = results.filter(item => 
        (item.courseName && item.courseName.toLowerCase().includes(lowerSearchText)) ||
        (item.examClassIdentifier && item.examClassIdentifier.toLowerCase().includes(lowerSearchText)) ||
        (item.courseId && item.courseId.toLowerCase().includes(lowerSearchText))
      );
    }
    
    // Apply status filter - check if assignment is fully scheduled or not
    if (statusFilter !== 'all') {
      results = results.filter(item => {
        // Get the current state of the assignment (including any changes)
        const id = item.id;
        const assignmentChange = assignmentChanges[id];
        
        // Check if all required fields have values
        const roomId = assignmentChange?.roomId !== undefined ? assignmentChange.roomId : item.roomId;
        const weekNumber = assignmentChange?.weekNumber !== undefined ? assignmentChange.weekNumber : item.weekNumber;
        const date = assignmentChange?.date !== undefined ? assignmentChange.date : item.date;
        const sessionId = assignmentChange?.sessionId !== undefined ? assignmentChange.sessionId : item.sessionId;
        
        // Check if the assignment is fully scheduled (all fields have values)
        const isScheduled = roomId && weekNumber && date && sessionId;
        
        // Return items based on the filter selection
        return statusFilter === 'scheduled' ? isScheduled : !isScheduled;
      });
    }
    
    return results;
  }, [classesData, activeSearchText, lowerSearchText, statusFilter, descriptionFilter, assignmentChanges]);

  // Updated handler functions to preserve all fields
  const handleRoomChange = useCallback((classId, roomId) => {
    setAssignmentChanges(prev => {
      // Get existing values from the current assignment state or from the original row data
      const currentAssignment = prev[classId] || {};
      const originalRow = classesData.find(row => row.id === classId);
      
      return {
        ...prev,
        [classId]: {
          // Include the new value
          roomId,
          // Preserve other fields from either current changes or original data
          weekNumber: currentAssignment.weekNumber !== undefined ? 
            currentAssignment.weekNumber : originalRow.weekNumber,
          date: currentAssignment.date !== undefined ? 
            currentAssignment.date : originalRow.date,
          sessionId: currentAssignment.sessionId !== undefined ? 
            currentAssignment.sessionId : originalRow.sessionId
        }
      };
    });
  }, [classesData]);
  
  const handleWeekChange = useCallback((classId, weekNumber) => {
    // When week changes, clear date and slot since they depend on week
    setAssignmentChanges(prev => {
      const currentAssignment = prev[classId] || {};
      const originalRow = classesData.find(row => row.id === classId);
      
      return {
        ...prev,
        [classId]: {
          weekNumber,
          // Reset dependent fields
          date: '',
          sessionId: '',
          // Preserve room field
          roomId: currentAssignment.roomId !== undefined ? 
            currentAssignment.roomId : originalRow.roomId
        }
      };
    });
  }, [classesData]);
  
  const handleDateChange = useCallback((classId, date) => {
    setAssignmentChanges(prev => {
      const currentAssignment = prev[classId] || {};
      const originalRow = classesData.find(row => row.id === classId);
      
      return {
        ...prev,
        [classId]: {
          date,
          // Reset slot since it depends on date
          sessionId: '',
          // Preserve other fields
          roomId: currentAssignment.roomId !== undefined ? 
            currentAssignment.roomId : originalRow.roomId,
          weekNumber: currentAssignment.weekNumber !== undefined ? 
            currentAssignment.weekNumber : originalRow.weekNumber
        }
      };
    });
  }, [classesData]);
  
  const handleSlotChange = useCallback((classId, sessionId) => {
    setAssignmentChanges(prev => {
      const currentAssignment = prev[classId] || {};
      const originalRow = classesData.find(row => row.id === classId);
      
      return {
        ...prev,
        [classId]: {
          sessionId,
          // Preserve other fields
          roomId: currentAssignment.roomId !== undefined ? 
            currentAssignment.roomId : originalRow.roomId,
          weekNumber: currentAssignment.weekNumber !== undefined ? 
            currentAssignment.weekNumber : originalRow.weekNumber,
          date: currentAssignment.date !== undefined ? 
            currentAssignment.date : originalRow.date
        }
      };
    });
  }, [classesData]);
  
  // Use memoization to optimize cell renderers and prevent unnecessary re-renders
  const renderRoomCell = useCallback((params) => {
    const classId = params.row.id;
    const currentValue = assignmentChanges[classId]?.roomId !== undefined
      ? assignmentChanges[classId]?.roomId
      : params.value;
      
    return (
      <FormControl fullWidth size="small">
        <Select
          value={currentValue || ''}
          onChange={(e) => handleRoomChange(classId, e.target.value)}
          displayEmpty
          inputProps={{ 'aria-label': 'Chọn phòng thi' }}
        >
          <MenuItem value="" disabled>
            <em>Chọn phòng</em>
          </MenuItem>
          {rooms.map((room) => (
            <MenuItem key={room.id} value={room.id}>
              {room.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  }, [rooms, assignmentChanges, handleRoomChange]);
  
  const renderWeekCell = useCallback((params) => {
    const classId = params.row.id;
    
    const currentValue = assignmentChanges[classId]?.weekNumber !== undefined
      ? assignmentChanges[classId]?.weekNumber
      : params.row.weekNumber;  // Use params.row.weekNumber instead of params.value
      
    return (
      <FormControl fullWidth size="small">
        <Select
          value={currentValue || ''}
          onChange={(e) => handleWeekChange(classId, e.target.value)}
          displayEmpty
          inputProps={{ 'aria-label': 'Chọn tuần' }}
        >
          <MenuItem value="" disabled>
            <em>Tuần</em>
          </MenuItem>
          {weeks.map((week) => (
            <MenuItem key={week} value={week}>
              {`W${week}`}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  }, [weeks, assignmentChanges, handleWeekChange]);
  
  const renderDateCell = useCallback((params) => {
    const classId = params.row.id;
    
    const currentWeekNumber = assignmentChanges[classId]?.weekNumber !== undefined
      ? assignmentChanges[classId]?.weekNumber
      : params.row.weekNumber;
    
    const currentValue = assignmentChanges[classId]?.date !== undefined
      ? assignmentChanges[classId]?.date
      : params.row.date;

    const weekNum = Number(currentWeekNumber);
    const availableDates = !isNaN(weekNum) && weekNum
      ? dates.filter(date => date.weekNumber === weekNum)
      : [];
      
    return (
      <FormControl fullWidth size="small" disabled={!currentWeekNumber}>
        <Select
          value={currentValue || ''}
          onChange={(e) => handleDateChange(classId, e.target.value)}
          displayEmpty
          inputProps={{ 'aria-label': 'Chọn ngày' }}
        >
          <MenuItem value="" disabled>
            <em>Ngày</em>
          </MenuItem>
          {availableDates.map((date) => (
            <MenuItem key={date.date} value={date.date}>
              {date.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  }, [dates, assignmentChanges, handleDateChange]);
  
  const renderSlotCell = useCallback((params) => {
    const classId = params.row.id;
    const currentDateSelected = assignmentChanges[classId]?.date !== undefined || params.row.date !== undefined;
    const currentValue = assignmentChanges[classId]?.sessionId !== undefined
      ? assignmentChanges[classId]?.sessionId
      : params.value;
      
    return (
      <FormControl fullWidth size="small" disabled={!currentDateSelected}>
        <Select
          value={currentValue || ''}
          onChange={(e) => handleSlotChange(classId, e.target.value)}
          displayEmpty
          inputProps={{ 'aria-label': 'Chọn ca thi' }}
        >
          <MenuItem value="" disabled>
            <em>Kíp</em>
          </MenuItem>
          {slots.map((slot) => (
            <MenuItem key={slot.id} value={slot.id}>
              {slot.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  }, [slots, assignmentChanges, handleSlotChange]);
  
  const columns = useMemo(() => [
    { 
      field: 'roomId', 
      headerName: 'Phòng thi', 
      width: 120,
      renderCell: renderRoomCell
    },
    { 
      field: 'weekNumber', 
      headerName: 'Tuần', 
      width: 100,
      renderCell: renderWeekCell
    },
    { 
      field: 'date', 
      headerName: 'Ngày', 
      width: 180,
      renderCell: renderDateCell
    },
    { 
      field: 'sessionId', 
      headerName: 'Ca thi', 
      width: 110,
      renderCell: renderSlotCell
    },
    { 
      field: 'examClassId', 
      headerName: 'Mã lớp thi', 
      width: 100, 
    },
    { 
      field: 'courseId', 
      headerName: 'Mã HP', 
      width: 80 
    },
    {
      field: "classId",
      headerName: "Mã lớp học",
      width: 100,
    },
    { 
      field: 'numberOfStudents', 
      headerName: 'Số SV', 
      width: 70,
      type: 'number'
    },
    { 
      field: 'courseName', 
      headerName: 'Tên lớp thi', 
      width: 200,
    },
    { 
      field: 'description', 
      headerName: 'Ghi chú', 
      width: 250 
    },
    { 
      field: 'school', 
      headerName: 'Khoa', 
      width: 250 
    },
    {
      field: "groupId",
      headerName: "Nhóm",
      width: 70,
    },
    {
      field: "period",
      headerName: "Đợt",
      width: 50,
    },
    {
      field: "managementCode",
      headerName: "Mã quản lý",
      width: 100,
    },
  ], [renderRoomCell, renderWeekCell, renderDateCell, renderSlotCell]);

  // Update the imperative handle to include validation of assignment changes and selected rows
  useImperativeHandle(ref, () => ({
    getAssignmentChanges: () => {
      // Convert to array format with assignmentId included
      return Object.entries(assignmentChanges).map(([key, value]) => ({
        assignmentId: key,
        ...value
      }));
    },
    getRawAssignmentChanges: () => assignmentChanges,
    getSelectedRows: () => selectedRows
  }));

  return (
    <Box sx={{ height: '100%', width: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ 
        p: 2, 
        display: 'flex', 
        justifyContent: 'space-between', 
        borderBottom: '1px solid #eee'
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <TextField
            placeholder="Tìm kiếm lớp thi..."
            value={searchText}
            onChange={handleSearchChange}
            onKeyPress={handleSearchKeyPress}
            sx={{ width: 300 }}
            size="small"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <IconButton 
                    size="small" 
                    onClick={handleSearchSubmit}
                    sx={{ mr: -0.5 }}
                  >
                    <Search fontSize="small" />
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
        </Box>
        
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <FilterList sx={{ mr: 1, color: 'text.secondary' }} />
            <FormControl size="small" sx={{ minWidth: 150 }}>
              <Select
                value={statusFilter}
                onChange={handleStatusFilterChange}
                displayEmpty
              >
                <MenuItem value="all">Tất cả trạng thái</MenuItem>
                <MenuItem value="scheduled">Đã xếp lịch</MenuItem>
                <MenuItem value="unscheduled">Chưa xếp lịch</MenuItem>
              </Select>
            </FormControl>
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <FilterList sx={{ mr: 1, color: 'text.secondary' }} />
            <FormControl size="small" sx={{ minWidth: 180 }}>
              <Select
                value={descriptionFilter}
                onChange={handleDescriptionFilterChange}
                displayEmpty
                MenuProps={{
                  PaperProps: {
                    style: {
                      maxHeight: 300
                    }
                  }
                }}
              >
                <MenuItem value="all">Tất cả mô tả</MenuItem>
                {uniqueDescriptions.map(desc => (
                  <MenuItem key={desc} value={desc}>
                    {desc} 
                  </MenuItem>
                ))}
                {uniqueDescriptions.length > 0 && <MenuItem disabled divider sx={{ my: 1 }}></MenuItem>}
                <MenuItem value="other">
                  <em>Hiển thị theo ô tìm kiếm</em>
                </MenuItem>
              </Select>
            </FormControl>
          </Box>
          
          {activeFilters && (
            <Button
              variant="outlined"
              size="small"
              onClick={handleClearFilters}
              sx={{ 
                ml: 1, 
                border: '1px solid #ccc',
                color: 'text.secondary',
                '&:hover': {
                  backgroundColor: '#f5f5f5',
                  borderColor: '#aaa'
                }
              }}
            >
              Xóa bộ lọc
            </Button>
          )}
        </Box>
      </Box>
      
      <Box sx={{ flex: 1, height: 500 }}>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
            <CircularProgress />
          </Box>
        ) : (
          <DataGrid
            rows={filteredClasses}
            columns={columns}
            pagination
            pageSizeOptions={[10, 25, 50]}
            initialState={{
              pagination: { paginationModel: { pageSize: 25 } },
            }}
            rowHeight={52}
            getRowId={(row) => row.id}
            getRowClassName={() => 'datagrid-row'}
            columnBuffer={12}
            rowBuffer={100}
            density="standard"
            disableColumnFilter
            disableColumnMenu
            checkboxSelection // Enable checkbox selection
            onRowSelectionModelChange={handleSelectionModelChange} // Add selection change handler
            rowSelectionModel={selectedRows} // Controlled selection model
            disableSelectionOnClick={false} // Allow selection on click
            columnVisibilityModel={
              Object.fromEntries(columns.map(col => [col.field, visibleColumns.includes(col.field)]))
            }
            components={{
              Toolbar: CustomToolbar
            }}
            componentsProps={{
              toolbar: {
                visibleColumns,
                setVisibleColumns,
                allColumns: columns
              }
            }}
            sx={{
              height: '100%',
              width: '100%',
              border: 'none',
              '& .MuiDataGrid-columnHeaders': {
                backgroundColor: '#f5f5f5',
                fontWeight: 'bold',
              },
              '& .MuiDataGrid-cell': {
                borderBottom: '1px solid #f0f0f0',
                padding: '8px',
                overflow: 'hidden',
                whiteSpace: 'nowrap',
                textOverflow: 'ellipsis',
              },
              '& .MuiDataGrid-row:nth-of-type(even)': {
                backgroundColor: '#fafafa',
              },
              '& .MuiDataGrid-row:hover': {
                backgroundColor: '#f5f5f5',
              },
              '& .MuiDataGrid-cell:focus, & .MuiDataGrid-cell:focus-within': {
                outline: 'none',
              },
              '& .datagrid-row': {
                cursor: 'pointer',
              },
              '& .MuiDataGrid-virtualScroller': {
                scrollbarWidth: 'thin',
                '&::-webkit-scrollbar': {
                  width: '8px',
                  height: '8px',
                },
                '&::-webkit-scrollbar-track': {
                  background: '#f1f1f1',
                },
                '&::-webkit-scrollbar-thumb': {
                  backgroundColor: '#c1c1c1',
                  borderRadius: '4px',
                },
              },
            }}
          />
        )}
      </Box>
    </Box>
  );
});

export default ClassesTable;
