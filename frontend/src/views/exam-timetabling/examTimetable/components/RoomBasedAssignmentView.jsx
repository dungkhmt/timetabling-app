import React, { useState, useRef, useEffect } from 'react';
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
  Divider
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { School, Event, Person, Group, LocationOn, Schedule, CalendarMonth } from '@mui/icons-material';

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
  minWidth: '80px',
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

const RoomBasedAssignmentView = ({ rooms, slots, assignments }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const [popoverContent, setPopoverContent] = useState(null);
  const tableRef = useRef(null);

  useEffect(() => {
  }, []);

  const assignmentMap = {};
  assignments.forEach(assignment => {
    const key = `${assignment.roomId}_${assignment.date}_${assignment.sessionId}`;
    assignmentMap[key] = assignment;
  });

  const handleCellClick = (event, room, slot) => {
    const key = `${room.id}_${slot.date}_${slot.slotId}`;
    const assignment = assignmentMap[key];
    
    if (assignment) {
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

  const open = Boolean(anchorEl);
  const popoverId = open ? 'assignment-popover' : undefined;

  const slotsByWeek = {};
  slots.forEach(slot => {
    if (!slotsByWeek[slot.week]) {
      slotsByWeek[slot.week] = [];
    }
    slotsByWeek[slot.week].push(slot);
  });

  const sortedRooms = [...rooms].sort((a, b) => a.name.localeCompare(b.name));

  return (
    <Box sx={{ height: '100%', position: 'relative' }}>
      <StyledTableContainer ref={tableRef}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              <CornerCell></CornerCell>
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
            {sortedRooms.map((room) => (
              <TableRow key={room.id} hover>
                <FixedColumnCell>
                  <Tooltip title={`Số chỗ ngồi: ${room.numberSeat}`} placement="right">
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>{room.name}</Typography>
                      <Typography variant="caption" color="textSecondary">
                        {room.numberSeat} chỗ ngồi
                      </Typography>
                    </Box>
                  </Tooltip>
                </FixedColumnCell>

                {slots.map((slot) => {
                  const key = `${room.id}_${slot.date}_${slot.slotId}`;
                  const assignment = assignmentMap[key];
                  
                  return (
                    <AssignmentCell 
                      key={`${room.id}-${slot.date}-${slot.slotId}`}
                      onMouseEnter={(e) => handleCellClick(e, room, slot)}
                      onMouseLeave={handleMouseLeave}
                      hasassignment={assignment ? 'true' : 'false'}
                    >
                      {assignment && (
                        <Box sx={{ 
                          p: 0.5, 
                          borderRadius: 1,
                          border: '1px solid rgba(25, 118, 210, 0.3)'
                        }}>
                          <Typography variant="body2" sx={{ fontWeight: 600, color: '#1976D2' }}>
                            Mã LT: {assignment.examClassId}
                          </Typography>
                          <Typography variant="caption" sx={{ 
                            color: 'text.primary', 
                            fontWeight: 600,
                            backgroundColor: 'rgba(25, 118, 210, 0.1)',
                            px: 0.5,
                            borderRadius: 1
                          }}>
                            Mã LH: {assignment.classId}
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

export default RoomBasedAssignmentView;
