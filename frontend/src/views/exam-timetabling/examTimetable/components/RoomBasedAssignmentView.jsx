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
  Dialog,
  Card,
  CardContent,
  Divider,
  TextField,
  InputAdornment,
  IconButton,
  Autocomplete,
  Paper,
  Button,
  CircularProgress,
  Backdrop,
  Select,
  MenuItem,
  FormControl,
  InputLabel
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
  Close,
  ExpandMore
} from '@mui/icons-material';

// Styled components
const StyledTableContainer = styled(TableContainer)(({ theme }) => ({
  height: 'calc(100vh - 200px)', // Set a fixed height based on viewport
  maxHeight: '100%',
  overflow: 'auto',
  '&::-webkit-scrollbar': {
    width: '10px',
    height: '10px',
  },
  '&::-webkit-scrollbar-track': {
    backgroundColor: theme.palette.grey[200],
  },
  '&::-webkit-scrollbar-thumb': {
    backgroundColor: theme.palette.grey[500],
    borderRadius: '4px',
  },
  // Ensure the horizontal scrollbar appears correctly
  '& table': {
    width: 'auto',
    minWidth: '100%',
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
  minWidth: '60px',
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
  minWidth: '60px',
  maxWidth: '120px',
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
  padding: theme.spacing(0.1),
  minWidth: '60px',
  height: '32px',
  cursor: hasassignment === 'true' ? 'pointer' : 'default',
  backgroundColor: hasassignment === 'true' ? 'rgba(25, 118, 210, 0.15)' : 'transparent',
  color: hasassignment === 'true' ? theme.palette.primary.dark : theme.palette.text.primary,
  '&:hover': {
    backgroundColor: hasassignment === 'true' ? 'rgba(25, 118, 210, 0.25)' : 'rgba(0, 0, 0, 0.04)',
  },
  borderRight: `1px solid ${theme.palette.divider}`,
  transition: 'background-color 0.2s ease',
}));

// Assignment Detail Dialog Component - Preload data for better performance
const AssignmentDetailDialog = React.memo(({ open, popoverContent, rooms, slots, onClose }) => {
  if (!popoverContent) return null;
  
  // Precompute values for better performance
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const roomName = React.useMemo(() => {
    return rooms.find(r => r.id === popoverContent.roomId)?.name || '';
  }, [rooms, popoverContent.roomId]);
  
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const slotName = React.useMemo(() => {
    return slots.find(s => s.slotId === popoverContent.sessionId)?.slotName || '';
  }, [slots, popoverContent.sessionId]);
  
  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="sm"
      TransitionProps={{
        // Optimize dialog transition for better performance
        timeout: 200 // Reduced from default 300ms
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 450 }}>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', p: 0.5 }}>
          <IconButton size="small" onClick={onClose}>
            <Close fontSize="small" />
          </IconButton>
        </Box>
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
                {slotName}
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
                {roomName}
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
          
          <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
            <Button size="small" onClick={onClose} variant="outlined">
              Đóng
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Dialog>
  );
});

// Main component
const RoomBasedAssignmentView = ({ rooms, slots, assignments }) => {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [popoverContent, setPopoverContent] = useState(null);
  const tableRef = useRef(null);
  const [searchValue, setSearchValue] = useState('');
  const [activeSearchValue, setActiveSearchValue] = useState('');
  const [matchingAssignments, setMatchingAssignments] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [showAllRooms, setShowAllRooms] = useState(false);

  // Create a lookup map for efficient assignment finding
  const assignmentMap = useMemo(() => {
    const map = {};
    if (assignments && assignments.length > 0) {
      console.log(assignments.length);
      console.log(assignments[0])
      assignments.forEach(assignment => {
        const key = `${assignment.roomId}_${assignment.date}_${assignment.sessionId}`;
        map[key] = assignment;
      });
    }
    return map;
  }, [assignments]);

  // Extract unique descriptions for autocomplete
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

  // Sort rooms for a more predictable display
  const sortedRooms = useMemo(() => {
    if (!rooms || rooms.length === 0) return [];
    return [...rooms].sort((a, b) => a.name.localeCompare(b.name));
  }, [rooms]);

  // Initial loading
  useEffect(() => {
    // Show loading for at least 5 seconds
    const timer = setTimeout(() => {
      setLoading(false);
    }, 5000);

    return () => clearTimeout(timer);
  }, []);

  // Cell click handler - open dialog
  const handleCellClick = useCallback((room, slot, assignment) => {
    setPopoverContent(assignment);
    setDialogOpen(true);
  }, []);

  const handleDialogClose = useCallback(() => {
    setDialogOpen(false);
    setTimeout(() => setPopoverContent(null), 300); // Clear content after animation
  }, []);

  // Search handling
  const handleSearchChange = useCallback((event, newValue) => {
    setSearchValue(newValue);
  }, []);

  const handleSearchInputChange = useCallback((event, newInputValue) => {
    // Only for typing behavior, not selection
  }, []);

  const handleSearchSubmit = useCallback(() => {
    setActiveSearchValue(searchValue || '');
    filterAssignmentsBySearch(searchValue);
  }, [searchValue]);

  // Just filter assignments without filtering rooms
  const filterAssignmentsBySearch = useCallback((searchText) => {
    if (!searchText) {
      // When there's no search text, show all assignments
      setMatchingAssignments(new Set());
      setActiveSearchValue('');
      return;
    }
    
    const lowerSearchText = searchText.toLowerCase();
    const matches = new Set();
    
    // Check if exact description match
    const isExactDescription = uniqueDescriptions.some(
      desc => desc.toLowerCase() === lowerSearchText
    );
    
    // Find assignments that match the search criteria
    for (let i = 0; i < sortedRooms.length; i++) {
      const room = sortedRooms[i];
      for (let j = 0; j < slots.length; j++) {
        const slot = slots[j];
        const key = `${room.id}_${slot.date}_${slot.slotId}`;
        const assignment = assignmentMap[key];
        
        if (!assignment) continue;
        
        let isMatch = false;
        if (isExactDescription) {
          isMatch = assignment.description && 
                  assignment.description.toLowerCase() === lowerSearchText;
        } else {
          isMatch = (
            (assignment.description && assignment.description.toLowerCase().includes(lowerSearchText)) ||
            (assignment.courseName && assignment.courseName.toLowerCase().includes(lowerSearchText)) ||
            (assignment.courseId && assignment.courseId.toLowerCase().includes(lowerSearchText)) ||
            (assignment.classId && assignment.classId.toLowerCase().includes(lowerSearchText)) ||
            (assignment.examClassId && assignment.examClassId.toLowerCase().includes(lowerSearchText)) ||
            (assignment.school && assignment.school.toLowerCase().includes(lowerSearchText)) ||
            (assignment.managementCode && assignment.managementCode.toLowerCase().includes(lowerSearchText))
          );
        }
        
        if (isMatch) {
          matches.add(key);
        }
      }
    }

    console.log('Matching assignments:', matches.length);
    
    setMatchingAssignments(matches);
  }, [sortedRooms, slots, assignmentMap, uniqueDescriptions]);

  // Update the clear function
  const handleClearSearch = useCallback(() => {
    setSearchValue('');
    setActiveSearchValue('');
    setMatchingAssignments(new Set());
  }, []);

  // Group slots by week for better organization
  const slotsByWeek = useMemo(() => {
    if (!slots || slots.length === 0) return {};
    
    const groupedSlots = {};
    slots.forEach(slot => {
      if (!groupedSlots[slot.week]) {
        groupedSlots[slot.week] = [];
      }
      groupedSlots[slot.week].push(slot);
    });
    return groupedSlots;
  }, [slots]);

  // Toggle to show all rooms
  const handleShowAllRooms = useCallback(() => {
    setShowAllRooms(true);
  }, []);

  // Displayed rooms - either all or limited
  const displayedRooms = useMemo(() => {
    if (showAllRooms) {
      return sortedRooms;
    } else {
      // Show first 100 rooms
      return sortedRooms.slice(0, 100);
    }
  }, [sortedRooms, showAllRooms]);

  return (
    <Box sx={{ height: '100%', position: 'relative' }}>
      {/* Loading indicator */}
      <Backdrop 
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <Box sx={{ 
          display: 'flex', 
          flexDirection: 'column', 
          alignItems: 'center' 
        }}>
          <CircularProgress color="inherit" size={60} thickness={5} />
          <Typography variant="h6" sx={{ mt: 2, color: 'white' }}>
            Đang tải dữ liệu ({assignments?.length || 0} lịch thi)
          </Typography>
        </Box>
      </Backdrop>

      {/* Search Filter */}
      <Box sx={{ 
        p: 1.5, 
        display: 'flex', 
        justifyContent: 'space-between',
        alignItems: 'center',
        mb: 2,
        backgroundColor: '#f5f5f5',
        borderRadius: 1
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '60%' }}>
          <Autocomplete
            freeSolo
            disableClearable
            value={searchValue}
            onChange={handleSearchChange}
            onInputChange={handleSearchInputChange}
            options={uniqueDescriptions}
            filterOptions={(options, { inputValue }) => {
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
          {displayedRooms.length}/{sortedRooms.length} phòng
        </Typography>
      </Box>

      {/* Timetable Grid */}
      <StyledTableContainer ref={tableRef}>
        <Table stickyHeader sx={{ tableLayout: 'fixed' }}>
          <TableHead>
            <TableRow>
              <CornerCell>Phòng</CornerCell>
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
              <CornerCell>Phòng</CornerCell>
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
            {displayedRooms.map((room) => (
              <TableRow key={room.id} hover>
                <FixedColumnCell>
                  <Tooltip title={`Số chỗ ngồi: ${room.numberSeat}`} placement="right">
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>{room.name}</Typography>
                    </Box>
                  </Tooltip>
                </FixedColumnCell>

                {slots.map((slot) => {
                  if(slot.date === '26/05/2025' && room.id === 'D7-203' && slot.slotId === 'eac39a8d-086c-4207-8761-cdbdb2aa61d0') {
                    console.log('session:', slot.slotId);
                  }
                  const key = `${room.id}_${slot.date}_${slot.slotId}`;
                  const assignment = assignmentMap[key];

                  if (slot.date === '26/05/2025' && room.id === 'D7-203' && slot.slotId === 'eac39a8d-086c-4207-8761-cdbdb2aa61d0' && assignment) {
                    console.log('assignment:', assignment);
                  }
                  
                  // Check if the assignment should be visible based on search
                  const isVisible = !activeSearchValue || 
                                    (assignment && matchingAssignments.has(key));
                  
                  return (
                    <AssignmentCell 
                      key={`${room.id}-${slot.date}-${slot.slotId}`}
                      onClick={assignment && isVisible ? () => handleCellClick(room, slot, assignment) : undefined}
                      hasassignment={(assignment && isVisible) ? 'true' : 'false'}
                    >
                      {(assignment && isVisible) && (
                        <Box sx={{ 
                          p: 0.5, 
                          borderRadius: 1,
                          border: '1px solid rgba(25, 118, 210, 0.3)'
                        }}>
                          <Typography variant="body2" sx={{ fontWeight: 600, color: '#1976D2' }}>
                            {assignment.examClassId}
                          </Typography>
                        </Box>
                      )}
                    </AssignmentCell>
                  );
                })}
              </TableRow>
            ))}
          </TableBody>
        </Table>
        
        {/* Load more button */}
        {!showAllRooms && displayedRooms.length < sortedRooms.length && (
          <Box sx={{ p: 2, display: 'flex', justifyContent: 'center' }}>
            <Button 
              variant="contained" 
              color="primary"
              onClick={handleShowAllRooms}
              startIcon={<ExpandMore />}
              disabled={loading}
            >
              {loading ? 'Đang tải...' : `Hiển thị tất cả phòng (${sortedRooms.length})`}
            </Button>
          </Box>
        )}
      </StyledTableContainer>

      {/* Assignment Detail Dialog */}
      <AssignmentDetailDialog 
        open={dialogOpen}
        popoverContent={popoverContent}
        rooms={rooms}
        slots={slots}
        onClose={handleDialogClose}
      />
    </Box>
  );
};

export default React.memo(RoomBasedAssignmentView);
