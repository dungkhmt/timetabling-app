import React, { useState, useRef, useEffect, useMemo, useCallback } from 'react';
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Tooltip,
  Popover,
  Card,
  CardContent,
  Divider,
  TextField,
  InputAdornment,
  IconButton,
  Autocomplete,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { 
  School, 
  Event, 
  Person, 
  Group, 
  LocationOn, 
  Schedule, 
  CalendarMonth, 
  Search, 
  Clear,
  Room
} from '@mui/icons-material';

const StyledTableContainer = styled(TableContainer)(({ theme }) => ({
  height: '100%',
  overflow: 'auto',
  '&::-webkit-scrollbar': {
    width: '8px',
    height: '8px',
  },
  '&::-webkit-scrollbar-track': {
    backgroundColor: theme.palette.grey[200],
  },
  '&::-webkit-scrollbar-thumb': {
    backgroundColor: theme.palette.grey[500],
    borderRadius: '4px',
  }
}));

const FixedHeaderCell = styled(TableCell)(({ theme }) => ({
  position: 'sticky',
  top: 0,
  backgroundColor: theme.palette.grey[100],
  zIndex: 2,
  padding: theme.spacing(0.75),
  fontWeight: 'bold',
  whiteSpace: 'nowrap',
  minWidth: '140px',
  borderBottom: `1px solid ${theme.palette.divider}`,
  borderRight: `1px solid ${theme.palette.divider}`,
}));

const FixedColumnCell = styled(TableCell)(({ theme }) => ({
  position: 'sticky',
  left: 0,
  backgroundColor: theme.palette.grey[100],
  zIndex: 1,
  padding: theme.spacing(0.75),
  fontWeight: 'bold',
  whiteSpace: 'nowrap',
  minWidth: '120px',
  borderRight: `1px solid ${theme.palette.divider}`,
}));

const CornerCell = styled(TableCell)(({ theme }) => ({
  position: 'sticky',
  top: 0,
  left: 0,
  backgroundColor: theme.palette.grey[200],
  zIndex: 3,
  padding: theme.spacing(1.5),
  fontWeight: 'bold',
  borderBottom: `1px solid ${theme.palette.divider}`,
  borderRight: `1px solid ${theme.palette.divider}`,
}));

const AssignmentCell = styled(TableCell)(({ theme, hasassignment }) => ({
  padding: theme.spacing(0.5),
  minWidth: '140px',
  cursor: hasassignment === 'true' ? 'pointer' : 'default',
  backgroundColor: hasassignment === 'true' ? 'rgba(25, 118, 210, 0.15)' : 'transparent',
  color: hasassignment === 'true' ? theme.palette.primary.dark : theme.palette.text.primary,
  '&:hover': {
    backgroundColor: hasassignment === 'true' ? 'rgba(25, 118, 210, 0.25)' : 'rgba(0, 0, 0, 0.04)',
  },
  borderRight: `1px solid ${theme.palette.divider}`,
  transition: 'background-color 0.2s ease',
}));

const ClassBasedAssignmentView = ({ rooms, slots, assignments }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const [popoverContent, setPopoverContent] = useState(null);
  const tableRef = useRef(null);
  const [searchValue, setSearchValue] = useState('');
  const [activeSearchValue, setActiveSearchValue] = useState('');
  const [selectedRoom, setSelectedRoom] = useState('');
  const [filteredClasses, setFilteredClasses] = useState([]);
  const [matchingAssignments, setMatchingAssignments] = useState(new Set());
  const [noResultsFound, setNoResultsFound] = useState(false);

  const assignmentMap = useMemo(() => {
    const map = {};
    assignments.forEach(assignment => {
      const key = `${assignment.examClassId}_${assignment.date}_${assignment.sessionId}`;
      map[key] = assignment;
    });
    return map;
  }, [assignments]);

  const uniqueDescriptions = useMemo(() => {
    if (!assignments || assignments.length === 0) return [];
    
    const descSet = new Set();
    
    for (let i = 0; i < assignments.length; i++) {
      const desc = assignments[i].description;
      if (desc !== null && desc !== undefined && desc !== '') {
        descSet.add(desc);
      }
    }
    
    return Array.from(descSet).sort();
  }, [assignments]);

  const examClasses = useMemo(() => {
    if (!assignments || assignments.length === 0) return [];
    
    const classesMap = new Map();
    
    assignments.forEach(assignment => {
      if (!classesMap.has(assignment.examClassId)) {
        classesMap.set(assignment.examClassId, {
          examClassId: assignment.examClassId,
          courseId: assignment.courseId,
          courseName: assignment.courseName,
          description: assignment.description,
          numberOfStudents: assignment.numberOfStudents
        });
      }
    });
    
    return Array.from(classesMap.values()).sort((a, b) => 
      a.examClassId.localeCompare(b.examClassId)
    );
  }, [assignments]);

  useEffect(() => {
    setFilteredClasses(examClasses);
  }, [examClasses]);

  const handleCellClick = (event, examClass, slot) => {
    const key = `${examClass.examClassId}_${slot.date}_${slot.slotId}`;
    const assignment = assignmentMap[key];
    
    if (assignment && (!activeSearchValue || matchingAssignments.has(key))) {
      setPopoverContent(assignment);
      setAnchorEl(event.currentTarget);
    }
  };

  const handleMouseLeave = () => {
    setAnchorEl(null);
  };

  const handlePopoverClose = () => {
    setAnchorEl(null);
  };

  // Search handling
  const handleSearchChange = (event, newValue) => {
    setSearchValue(newValue);
  };

  const handleSearchInputChange = (event, newInputValue) => {
  };

  const handleSearchSubmit = () => {
    setActiveSearchValue(searchValue || '');
    filterClassesBySearch(searchValue, selectedRoom);
  };

  const handleClearSearch = () => {
    setSearchValue('');
    setActiveSearchValue('');
    setMatchingAssignments(new Set());
    
    if (selectedRoom) {
      filterClassesByRoom(selectedRoom);
    } else {
      setFilteredClasses(examClasses);
    }
  };
  
  const handleRoomChange = (event) => {
    const roomId = event.target.value;
    setSelectedRoom(roomId);
    filterClassesByRoom(roomId, activeSearchValue);
  };
  
  const handleClearRoomFilter = () => {
    setSelectedRoom('');
    filterClassesBySearch(activeSearchValue, '');
  };

  const filterClassesBySearch = useCallback((searchText, roomId) => {
    if (!searchText && !roomId) {
      setMatchingAssignments(new Set());
      setFilteredClasses(examClasses);
      setNoResultsFound(false);
      return;
    }
    
    const lowerSearchText = searchText ? searchText.toLowerCase() : '';
    const matches = new Set();
    const matchingClassIds = new Set();
    
    const isExactDescription = searchText ? uniqueDescriptions.some(
      desc => desc.toLowerCase() === lowerSearchText
    ) : false;
    
    examClasses.forEach(examClass => {
      slots.forEach(slot => {
        const key = `${examClass.examClassId}_${slot.date}_${slot.slotId}`;
        const assignment = assignmentMap[key];
        
        if (!assignment) return;
        
        const passesRoomFilter = !roomId || assignment.roomId === roomId;
        if (!passesRoomFilter) return;
        
        let passesSearchFilter = true;
        if (searchText) {
          if (isExactDescription) {
            passesSearchFilter = assignment.description && 
                    assignment.description.toLowerCase() === lowerSearchText;
          } else {
            passesSearchFilter = (
              (assignment.description && assignment.description.toLowerCase().includes(lowerSearchText)) ||
              (assignment.courseName && assignment.courseName.toLowerCase().includes(lowerSearchText)) ||
              (assignment.courseId && assignment.courseId.toLowerCase().includes(lowerSearchText)) ||
              (assignment.classId && assignment.classId.toLowerCase().includes(lowerSearchText)) ||
              (assignment.examClassId && assignment.examClassId.toLowerCase().includes(lowerSearchText)) ||
              (assignment.school && assignment.school.toLowerCase().includes(lowerSearchText)) ||
              (assignment.managementCode && assignment.managementCode.toLowerCase().includes(lowerSearchText))
            );
          }
        }
        
        if (passesRoomFilter && passesSearchFilter) {
          matches.add(key);
          matchingClassIds.add(examClass.examClassId);
        }
      });
    });
    
    setMatchingAssignments(matches);
    
    const filtered = examClasses.filter(examClass => 
      matchingClassIds.has(examClass.examClassId)
    );
    
    setNoResultsFound(filtered.length === 0);
    setFilteredClasses(filtered);
  }, [examClasses, slots, assignmentMap, uniqueDescriptions]);
  
  const filterClassesByRoom = useCallback((roomId, searchText = '') => {
    // Reuse the search filter function with the room filter
    filterClassesBySearch(searchText, roomId);
  }, [filterClassesBySearch]);

  // Autocomplete options filter
  const filterOptions = (options, { inputValue }) => {
    if (!inputValue) return [];
    
    const lowerInput = inputValue.toLowerCase();
    
    // Find exact matches at the beginning
    const startsWithMatches = options.filter(option => 
      option.toLowerCase().startsWith(lowerInput)
    );
    
    // Find contains matches
    const containsMatches = options.filter(option => 
      option.toLowerCase().includes(lowerInput) && 
      !startsWithMatches.includes(option)
    );
    
    // Combine results
    return [...startsWithMatches, ...containsMatches].slice(0, 10);
  };

  const open = Boolean(anchorEl);
  const popoverId = open ? 'assignment-popover' : undefined;

  // Group slots by week for better organization
  const slotsByWeek = useMemo(() => {
    const groupedSlots = {};
    slots.forEach(slot => {
      if (!groupedSlots[slot.week]) {
        groupedSlots[slot.week] = [];
      }
      groupedSlots[slot.week].push(slot);
    });
    return groupedSlots;
  }, [slots]);

  return (
    <Box sx={{ height: '100%', position: 'relative' }}>
      {/* Search and Filter Controls */}
      <Box sx={{ 
        p: 1.5, 
        display: 'flex', 
        justifyContent: 'space-between',
        alignItems: 'center',
        mb: 2,
        backgroundColor: '#f5f5f5',
        borderRadius: 1
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '85%' }}>
          {/* Room filter */}
          <FormControl size="small" sx={{ minWidth: 180 }}>
            <InputLabel id="room-select-label">Phòng</InputLabel>
            <Select
              labelId="room-select-label"
              id="room-select"
              value={selectedRoom}
              label="Phòng"
              onChange={handleRoomChange}
              displayEmpty
              endAdornment={
                selectedRoom ? (
                  <IconButton
                    size="small"
                    sx={{ mr: 0.5 }}
                    onClick={handleClearRoomFilter}
                  >
                    <Clear fontSize="small" />
                  </IconButton>
                ) : null
              }
            >
              <MenuItem value="">
                <em>Tất cả phòng</em>
              </MenuItem>
              {rooms.map((room) => (
                <MenuItem key={room.id} value={room.id}>
                  {room.name} ({room.numberSeat} chỗ)
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          
          {/* Assignment search */}
          <Autocomplete
            freeSolo
            disableClearable
            value={searchValue}
            onChange={handleSearchChange}
            onInputChange={handleSearchInputChange}
            options={uniqueDescriptions}
            filterOptions={filterOptions}
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
                          onClick={handleClearSearch}
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
        
        <Typography variant="body2" color="text.secondary">
          {(activeSearchValue || selectedRoom) ? 
            `${matchingAssignments.size} kết quả tìm thấy` : 
            `${filteredClasses.length} lớp thi`}
        </Typography>
      </Box>

      {/* Class-based Timetable Grid */}
      <StyledTableContainer ref={tableRef}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              <CornerCell>Lớp thi</CornerCell>
              {Object.entries(slotsByWeek).map(([week, weekSlots]) => (
                <React.Fragment key={`week-${week}`}>
                  <FixedHeaderCell 
                    colSpan={weekSlots.length} 
                    align="center"
                    sx={{ 
                      backgroundColor: '#e3f2fd',
                      borderBottom: '2px solid #1976d2'
                    }}
                  >
                    Tuần {week}
                  </FixedHeaderCell>
                </React.Fragment>
              ))}
            </TableRow>
            <TableRow>
              <CornerCell>Mã lớp thi</CornerCell>
              {slots.map((slot) => (
                <FixedHeaderCell key={`${slot.date}-${slot.slotId}`} align="center">
                  <Tooltip title={`${slot.dateDisplay} - ${slot.slotName}`} placement="top">
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                      <Typography variant="caption" sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '150px' }}>
                        {slot.dateDisplay}
                      </Typography>
                      <Typography variant="caption" color="textSecondary">
                        {slot.slotName}
                      </Typography>
                    </Box>
                  </Tooltip>
                </FixedHeaderCell>
              ))}
            </TableRow>
          </TableHead>

          <TableBody>
            {noResultsFound ? (
              <TableRow>
                <TableCell colSpan={slots.length + 1} sx={{ textAlign: 'center', py: 8 }}>
                  <Typography variant="h6" color="text.secondary">
                    Không tìm thấy kết quả phù hợp
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    Vui lòng thử tìm kiếm khác hoặc xóa bộ lọc
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredClasses.map((examClass) => (
                <TableRow key={examClass.examClassId} hover>
                  <FixedColumnCell>
                    <Tooltip title={`${examClass.courseName} - ${examClass.description}`} placement="right">
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>{examClass.examClassId}</Typography>
                        <Typography variant="caption" color="textSecondary">
                          {examClass.courseId} - {examClass.numberOfStudents} SV
                        </Typography>
                      </Box>
                    </Tooltip>
                  </FixedColumnCell>

                  {slots.map((slot) => {
                    const key = `${examClass.examClassId}_${slot.date}_${slot.slotId}`;
                    const assignment = assignmentMap[key];
                    
                    const isVisible = (!activeSearchValue && !selectedRoom) || 
                                      (assignment && matchingAssignments.has(key));
                    
                    return (
                      <AssignmentCell 
                        key={`${examClass.examClassId}-${slot.date}-${slot.slotId}`}
                        onMouseEnter={(e) => handleCellClick(e, examClass, slot)}
                        onMouseLeave={handleMouseLeave}
                        hasassignment={(assignment && isVisible) ? 'true' : 'false'}
                      >
                        {(assignment && isVisible) && (
                          <Box sx={{ 
                            p: 0.5, 
                            borderRadius: 1,
                            border: '1px solid rgba(25, 118, 210, 0.3)'
                          }}>
                            <Typography variant="body2" sx={{ fontWeight: 600, color: '#1976D2' }}>
                              {rooms.find(r => r.id === assignment.roomId)?.name || 'Chưa xếp phòng'}
                            </Typography>
                            <Typography variant="caption" sx={{ 
                              color: 'text.primary', 
                              fontWeight: 600,
                              backgroundColor: 'rgba(25, 118, 210, 0.1)',
                              px: 0.5,
                              borderRadius: 1
                            }}>
                              {assignment.numberOfStudents} SV
                            </Typography>
                          </Box>
                        )}
                      </AssignmentCell>
                    );
                  })}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </StyledTableContainer>

      <Popover
        id={popoverId}
        open={open}
        anchorEl={anchorEl}
        onClose={handlePopoverClose}
        anchorOrigin={{
          vertical: 'center',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'center',
          horizontal: 'left',
        }}
        disableRestoreFocus
        sx={{ pointerEvents: 'none' }}
      >
        {popoverContent && (
          <Card sx={{ width: 350, boxShadow: 3 }}>
            <CardContent>
              <Box sx={{ mb: 1.5, p: 1, backgroundColor: 'rgba(25, 118, 210, 0.08)', borderRadius: 1 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 0.5 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Mã lớp thi: <span style={{ color: '#1976D2' }}>{popoverContent.examClassId}</span>
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Mã lớp học: <span style={{ color: '#1976D2' }}>{popoverContent.classId}</span>
                  </Typography>
                </Box>
              </Box>
              
              <Typography variant="body1" component="div" fontWeight={600}>
                {popoverContent.courseId} - {popoverContent.courseName}
              </Typography>
              
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                {popoverContent.description}
              </Typography>
          
              <Divider sx={{ my: 1.5 }} />

              <Box sx={{ display: 'flex', mb: 1 }}>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <Event sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    {popoverContent.date}
                  </Typography>
                </Box>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <Schedule sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    {slots.find(s => s.slotId === popoverContent.sessionId)?.slotName}
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ display: 'flex', mb: 1 }}>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <CalendarMonth sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    Kỳ: {popoverContent.period}
                  </Typography>
                </Box>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <Person sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    {popoverContent.numberOfStudents} SV
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ display: 'flex', mb: 1 }}>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <Group sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    Mã: {popoverContent.managementCode}
                  </Typography>
                </Box>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <LocationOn sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    {rooms.find(r => r.id === popoverContent.roomId)?.name}
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ mt: 1 }}>
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                  <School sx={{ color: 'primary.main', mr: 0.5, fontSize: 18 }} />
                  <Typography variant="body2">
                    {popoverContent.school}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        )}
      </Popover>
    </Box>
  );
};

export default ClassBasedAssignmentView;
