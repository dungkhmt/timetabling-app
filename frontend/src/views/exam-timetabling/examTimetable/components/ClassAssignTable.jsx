import { forwardRef, useImperativeHandle, useState, useMemo, useCallback, useEffect } from 'react';
import {
  Box,
  CircularProgress,
  FormControl,
  InputAdornment,
  MenuItem,
  Select,
  TextField,
  Checkbox,
  Button,
  Popover,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  Autocomplete,
  Paper
} from '@mui/material';
import {
  Search,
  FilterList,
  ViewColumn,
  Clear,
  Restore
} from '@mui/icons-material';
import { DataGrid, GridToolbarContainer } from '@mui/x-data-grid';
import ResetChangesConfirmDialog from './ResetChangesConfirmDialog'

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
  onSelectionChange 
}, ref) => {
  const [statusFilter, setStatusFilter] = useState('all');
  const [roomFilter, setRoomFilter] = useState('all'); 
  const [dateFilter, setDateFilter] = useState('all'); 
  const [slotFilter, setSlotFilter] = useState('all'); 
  const [searchValue, setSearchValue] = useState('');
  const [activeSearchValue, setActiveSearchValue] = useState('');
  const [activeFilters, setActiveFilters] = useState(false);
  const [assignmentChanges, setAssignmentChanges] = useState({});
  const [page, setPage] = useState(0);
  const [selectedRows, setSelectedRows] = useState([]);
  const [visibleColumns, setVisibleColumns] = useState([
    'roomId', 'weekNumber', 'date', 'sessionId', 'examClassId', 'classId', "courseId",
     'numberOfStudents', 'description',
  ]);
  const [frozenFilteredClasses, setFrozenFilteredClasses] = useState(null);
  const [isResetDialogOpen, setIsResetDialogOpen] = useState(false);

  const handleResetChanges = () => {
    setAssignmentChanges({});
    setIsResetDialogOpen(false);
  };
  
  const uniqueDescriptions = useMemo(() => {
    if (!classesData || classesData.length === 0) return [];
    
    const descSet = new Set();
    
    for (let i = 0; i < classesData.length; i++) {
      const desc = classesData[i].description;
      if (desc !== null && desc !== undefined && desc !== '') {
        descSet.add(desc);
      }
    }
    
    return Array.from(descSet).sort();
  }, [classesData]);

  const uniqueRooms = useMemo(() => {
    if (!classesData || classesData.length === 0) return [];
    
    const roomsSet = new Set();
    
    for (let i = 0; i < classesData.length; i++) {
      const roomId = classesData[i].roomId;
      if (roomId) {
        roomsSet.add(roomId);
      }
    }
    
    return Array.from(roomsSet).sort();
  }, [classesData]);

  const uniqueDates = useMemo(() => {
    if (!classesData || classesData.length === 0) return [];
    
    const datesSet = new Set();
    
    for (let i = 0; i < classesData.length; i++) {
      const date = classesData[i].date;
      if (date) {
        datesSet.add(date);
      }
    }
    
    return Array.from(datesSet).sort();
  }, [classesData]);

  const uniqueSlots = useMemo(() => {
    if (!classesData || classesData.length === 0) return [];
    
    const slotsSet = new Set();
    
    for (let i = 0; i < classesData.length; i++) {
      const sessionId = classesData[i].sessionId;
      if (sessionId) {
        slotsSet.add(sessionId);
      }
    }
    
    return Array.from(slotsSet).sort();
  }, [classesData]);

  const handleRoomFilterChange = (event) => {
    setRoomFilter(event.target.value);
    setPage(0);
    setActiveFilters(!!(activeSearchValue || statusFilter !== 'all' || event.target.value !== 'all' || dateFilter !== 'all' || slotFilter !== 'all'));
    setFrozenFilteredClasses(null);
  };

  const handleDateFilterChange = (event) => {
    setDateFilter(event.target.value);
    setPage(0);
    setActiveFilters(!!(activeSearchValue || statusFilter !== 'all' || roomFilter !== 'all' || event.target.value !== 'all' || slotFilter !== 'all'));
    setFrozenFilteredClasses(null);
  };

  const handleSlotFilterChange = (event) => {
    setSlotFilter(event.target.value);
    setPage(0);
    setActiveFilters(!!(activeSearchValue || statusFilter !== 'all' || roomFilter !== 'all' || dateFilter !== 'all' || event.target.value !== 'all'));
    setFrozenFilteredClasses(null);
  };

  const handleSelectionModelChange = (newSelectionModel) => {
    setSelectedRows(newSelectionModel);
    if (onSelectionChange) {
      onSelectionChange(newSelectionModel);
    }
  };

  const handleSearchChange = (event, newValue) => {
    setSearchValue(newValue);
  };

  const handleSearchInputChange = (event, newInputValue) => {
  };

  const handleSearchSubmit = () => {
    setActiveSearchValue(searchValue || '');
    setPage(0);
    setActiveFilters(!!(searchValue || statusFilter !== 'all' || roomFilter !== 'all' || dateFilter !== 'all' || slotFilter !== 'all'));
    setFrozenFilteredClasses(null);
  };
  
  const handleStatusFilterChange = (event) => {
    setStatusFilter(event.target.value);
    setPage(0);
    setActiveFilters(!!(activeSearchValue || event.target.value !== 'all' || roomFilter !== 'all' || dateFilter !== 'all' || slotFilter !== 'all'));
    setFrozenFilteredClasses(null);
  };
  
  const handleClearFilters = () => {
    setSearchValue('');
    setActiveSearchValue('');
    setStatusFilter('all');
    setRoomFilter('all');
    setDateFilter('all');
    setSlotFilter('all');
    setPage(0);
    setActiveFilters(false);
    setFrozenFilteredClasses(null);
  };
  
  const filteredClasses = useMemo(() => {
    if (frozenFilteredClasses !== null) {
      return frozenFilteredClasses;
    }

    let results = classesData;
    
    if (activeSearchValue) {
      const lowerSearchText = activeSearchValue.toLowerCase();
      
      const isExactDescription = uniqueDescriptions.some(
        desc => desc.toLowerCase() === lowerSearchText
      );
      
      if (isExactDescription) {
        results = results.filter(item => 
          item.description && item.description.toLowerCase() === lowerSearchText
        );
      } else {
        results = results.filter(item => 
          (item.description && item.description.toLowerCase().includes(lowerSearchText)) ||
          (item.courseName && item.courseName.toLowerCase().includes(lowerSearchText)) ||
          (item.examClassIdentifier && item.examClassIdentifier.toLowerCase().includes(lowerSearchText)) ||
          (item.courseId && item.courseId.toLowerCase().includes(lowerSearchText)) ||
          (item.classId && item.classId.toLowerCase().includes(lowerSearchText)) ||
          (item.examClassId && item.examClassId.toLowerCase().includes(lowerSearchText)) ||
          (item.school && item.school.toLowerCase().includes(lowerSearchText)) ||
          (item.managementCode && item.managementCode.toLowerCase().includes(lowerSearchText))
        );
      }
    }
    
    // Apply status filter - check if assignment is fully scheduled or not
    if (statusFilter !== 'all') {
      results = results.filter(item => {
        const id = item.id;
        const assignmentChange = assignmentChanges[id];
        
        const roomId = assignmentChange?.roomId !== undefined ? assignmentChange.roomId : item.roomId;
        const weekNumber = assignmentChange?.weekNumber !== undefined ? assignmentChange.weekNumber : item.weekNumber;
        const date = assignmentChange?.date !== undefined ? assignmentChange.date : item.date;
        const sessionId = assignmentChange?.sessionId !== undefined ? assignmentChange.sessionId : item.sessionId;
        
        const isScheduled = roomId && weekNumber && date && sessionId;
        
        return statusFilter === 'scheduled' ? isScheduled : !isScheduled;
      });
    }

    if (roomFilter !== 'all') {
      results = results.filter(item => {
        const id = item.id;
        const assignmentChange = assignmentChanges[id];
        const currentRoomId = assignmentChange?.roomId !== undefined ? assignmentChange.roomId : item.roomId;
        return currentRoomId === roomFilter;
      });
    }

    if (dateFilter !== 'all') {
      results = results.filter(item => {
        const id = item.id;
        const assignmentChange = assignmentChanges[id];
        const currentDate = assignmentChange?.date !== undefined ? assignmentChange.date : item.date;
        return currentDate === dateFilter;
      });
    }

    if (slotFilter !== 'all') {
      results = results.filter(item => {
        const id = item.id;
        const assignmentChange = assignmentChanges[id];
        const currentSessionId = assignmentChange?.sessionId !== undefined ? assignmentChange.sessionId : item.sessionId;
        return currentSessionId === slotFilter;
      });
    }

    if (activeFilters) {
      setFrozenFilteredClasses(results);
    }
    
    return results;
  }, [classesData, activeSearchValue, statusFilter, roomFilter, dateFilter, slotFilter, assignmentChanges, uniqueDescriptions, activeFilters, frozenFilteredClasses]);

  const handleRoomChange = useCallback((classId, roomId) => {
    setAssignmentChanges(prev => {
      const currentAssignment = prev[classId] || {};
      const originalRow = classesData.find(row => row.id === classId);
      
      return {
        ...prev,
        [classId]: {
          roomId,
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
    setAssignmentChanges(prev => {
      const currentAssignment = prev[classId] || {};
      const originalRow = classesData.find(row => row.id === classId);
      
      return {
        ...prev,
        [classId]: {
          weekNumber,
          date: '',
          sessionId: '',
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
          sessionId: '',
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
      : params.row.weekNumber; 
      
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
  
  useImperativeHandle(ref, () => ({
    getAssignmentChanges: () => {
      return Object.entries(assignmentChanges).map(([key, value]) => ({
        assignmentId: key,
        ...value
      }));
    },
    getRawAssignmentChanges: () => assignmentChanges,
    getSelectedRows: () => selectedRows,
    resetAssignmentChanges: () => {
      setAssignmentChanges({});
    }
  }));

  const filterOptions = (options, { inputValue }) => {
    if (!inputValue) return [];
    
    const lowerInput = inputValue.toLowerCase();
    
    const startsWithMatches = options.filter(option => 
      option.toLowerCase().startsWith(lowerInput)
    );
    
    const containsMatches = options.filter(option => 
      option.toLowerCase().includes(lowerInput) && 
      !startsWithMatches.includes(option)
    );
    
    const filteredOptions = [...startsWithMatches, ...containsMatches];
    
    return filteredOptions.slice(0, 10);
  };

  return (
    <Box sx={{ height: '100%', width: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ 
        p: 2, 
        display: 'flex', 
        justifyContent: 'space-between', 
        borderBottom: '1px solid #eee',
        gap: 2
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '50%' }}>
          <Autocomplete
            freeSolo
            disableClearable
            value={searchValue}
            onChange={handleSearchChange}
            onInputChange={handleSearchInputChange}
            options={uniqueDescriptions}
            filterOptions={filterOptions}
            ListboxComponent={List}
            ListboxProps={{ 
              dense: true,
              sx: { maxHeight: 300 }
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                placeholder="Tìm kiếm lớp thi, ghi chú..."
                variant="outlined"
                size="small"
                fullWidth
                onKeyPress={(e) => e.key === 'Enter' && handleSearchSubmit()}
                InputProps={{
                  ...params.InputProps,
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
                  endAdornment: (
                    <InputAdornment position="end">
                      {searchValue && (
                        <IconButton
                          size="small"
                          aria-label="clear search"
                          onClick={() => {
                            setSearchValue('');
                            if (activeSearchValue) {
                              setActiveSearchValue('');
                              setActiveFilters(statusFilter !== 'all');
                            }
                          }}
                          sx={{ mr: -0.5 }}
                        >
                          <Clear fontSize="small" />
                        </IconButton>
                      )}
                      {params.InputProps.endAdornment}
                    </InputAdornment>
                  )
                }}
              />
            )}
            PaperComponent={({ children, ...props }) => (
              <Paper elevation={8} {...props}>
                {children}
              </Paper>
            )}
            sx={{ width: '100%' }}
          />
        </Box>

        {/* Room Filter */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <Select
              value={roomFilter}
              onChange={handleRoomFilterChange}
              displayEmpty
            >
              <MenuItem value="all">Tất cả phòng thi</MenuItem>
              {uniqueRooms.map((roomId) => {
                // Find the room name from the rooms prop
                const room = rooms.find(r => r.id === roomId);
                const roomName = room ? room.name : roomId;
                return (
                  <MenuItem key={roomId} value={roomId}>
                    {roomName}
                  </MenuItem>
                );
              })}
            </Select>
          </FormControl>
        </Box>

        {/* Date Filter */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <Select
              value={dateFilter}
              onChange={handleDateFilterChange}
              displayEmpty
            >
              <MenuItem value="all">Tất cả ngày thi</MenuItem>
              {uniqueDates.map((dateValue) => {
                // Find the date name from the dates prop
                const dateObj = dates.find(d => d.date === dateValue);
                const dateName = dateObj ? dateObj.name : dateValue;
                return (
                  <MenuItem key={dateValue} value={dateValue}>
                    {dateName}
                  </MenuItem>
                );
              })}
            </Select>
          </FormControl>
        </Box>

        {/* Slot Filter */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <Select
              value={slotFilter}
              onChange={handleSlotFilterChange}
              displayEmpty
            >
              <MenuItem value="all">Tất cả ca thi</MenuItem>
              {uniqueSlots.map((slotId) => {
                // Find the slot name from the slots prop
                const slot = slots.find(s => s.id === slotId);
                const slotName = slot ? slot.name : slotId;
                return (
                  <MenuItem key={slotId} value={slotId}>
                    {slotName}
                  </MenuItem>
                );
              })}
            </Select>
          </FormControl>
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
        </Box>
      </Box>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, justifyContent: 'flex-end', p: 2 }}>
        {Object.keys(assignmentChanges).length > 0 && (
          <Button
            variant="outlined"
            color="warning"
            size="small"
            startIcon={<Restore />}
            onClick={() => setIsResetDialogOpen(true)}
            sx={{ 
              mr: 1,
              borderColor: '#f57c00',
              color: '#f57c00',
              '&:hover': {
                backgroundColor: '#fff3e0',
                borderColor: '#f57c00'
              }
            }}
          >
            Hoàn tác thay đổi ({Object.keys(assignmentChanges).length})
          </Button>
        )}

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
        
        {activeFilters && frozenFilteredClasses !== null && (
          <Button
            variant="outlined"
            size="small"
            onClick={() => setFrozenFilteredClasses(null)}
            sx={{ ml: 1 }}
          >
            Làm mới bộ lọc
          </Button>
        )}
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
            pageSizeOptions={[10, 25, 50, 100]}
            initialState={{
              pagination: { paginationModel: { pageSize: 25 } },
            }}
            rowHeight={52}
            getRowId={(row) => row.id}
            getRowClassName={(params) => {
              return assignmentChanges[params.id] ? 'datagrid-row changed-row' : 'datagrid-row';
            }}
            columnBuffer={12}
            rowBuffer={100}
            density="standard"
            disableColumnFilter
            disableColumnMenu
            checkboxSelection
            onRowSelectionModelChange={handleSelectionModelChange}
            rowSelectionModel={selectedRows} 
            disableSelectionOnClick={false} 
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
              '& .changed-row': {
                backgroundColor: '#fff8e1', // Light yellow background for changed rows
              },
              '& .MuiDataGrid-row:nth-of-type(even)': {
                backgroundColor: '#fafafa',
              },
              '& .MuiDataGrid-row:nth-of-type(even).changed-row': {
                backgroundColor: '#fff8e1', // Light yellow background for changed rows (even)
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

      <ResetChangesConfirmDialog
        open={isResetDialogOpen}
        onClose={() => setIsResetDialogOpen(false)}
        onConfirm={handleResetChanges}
        changesCount={Object.keys(assignmentChanges).length}
      />
    </Box>
  );
});

export default ClassesTable;
