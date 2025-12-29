import { useState, useEffect } from "react";
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import { Clear, ArrowBack } from "@mui/icons-material";
import {toast} from "react-toastify";
import { request } from "api";
import { StandardTable } from "erp-hust/lib/StandardTable";
import { Button, Tabs, Tab, Chip, Divider, Paper, Typography, Box, TextField } from "@mui/material";

export default function SearchRoomForTimeSlotScheduledClass({openSearchRoom, setOpenSearchRoom, versionId}) {
  //const[openSearchRoom, setOpenSearchRoom] = useState(false);
  const[searchRoomCapacity, setSearchRoomCapacity] = useState(90);
  const[searchRoomData, setSearchRoomData] = useState([]);
  const[roomNameFilter, setRoomNameFilter] = useState("");
  const[filteredRoomData, setFilteredRoomData] = useState([]);
  
  const[currentTimeSlot, setCurrentTimeSlot] = useState({
    session: '',
    day: '',
    startPeriod: '',
    duration: '',
    logic: 'OR' 
  });
  const[timeSlotList, setTimeSlotList] = useState([]);

    const searchRoomColumns = [
    {
      title: "ID",
      field: "id"
    },
    {
      title: "Qty",
      field: "quantityMax"
    }
  ];

    const getDayName = (day) => {
    switch (day) {
      case 2:
        return "Thứ 2";
      case 3:
        return "Thứ 3";
      case 4:
        return "Thứ 4";
      case 5:
        return "Thứ 5";
      case 6:
        return "Thứ 6";
      case 7:
        return "Thứ 7";
      case 8:
        return "Chủ nhật";
      default:
        return `Ngày ${day}`;
    }
  };

  const handleChangeSearchRoomCapacity = (event) => {
    setSearchRoomCapacity(event.target.value);
  };
  
  // New handlers for single form approach
  const handleCurrentTimeSlotChange = (field, value) => {
    setCurrentTimeSlot(prev => ({ ...prev, [field]: value }));
  };

  const handleAddToTimeSlotList = () => {
    // Validate current time slot
    if (!currentTimeSlot.session || !currentTimeSlot.day || !currentTimeSlot.startPeriod || !currentTimeSlot.duration) {
      toast.error("Vui lòng điền đầy đủ thông tin ca học");
      return;
    }

    // Validate time slot
    const validationError = validateCurrentTimeSlot();
    if (validationError) {
      toast.error(validationError);
      return;
    }

    // Add to list
    const newTimeSlot = {
      id: Date.now(),
      ...currentTimeSlot
    };
    setTimeSlotList(prev => [...prev, newTimeSlot]);

    // Clear form (keep logic for next slot)
    setCurrentTimeSlot({
      session: '',
      day: '',
      startPeriod: '',
      duration: '',
      logic: currentTimeSlot.logic // Keep the same logic for convenience
    });
  };

  const handleRemoveLastTimeSlot = () => {
    if (timeSlotList.length > 0) {
      setTimeSlotList(prev => prev.slice(0, -1));
    }
  };

  const handleClearAllTimeSlots = () => {
    setTimeSlotList([]);
    setCurrentTimeSlot({
      session: '',
      day: '',
      startPeriod: '',
      duration: '',
      logic: 'OR'
    });
  };  const generateTimeSlotString = () => {
    if (timeSlotList.length === 0) return '';
    
    let result = '';
    timeSlotList.forEach((slot, index) => {
      const slotString = `${slot.session}-${slot.day}-${slot.startPeriod}-${slot.duration}`;
      
      if (index === 0) {
        result = slotString;
      } else {
        const separator = slot.logic === 'AND' ? ':' : ';';
        result += separator + slotString;
      }
    });
    
    return result;
  };

  const getTimeSlotMeaning = () => {
    if (timeSlotList.length === 0) return 'Chưa có ca học nào được thêm vào';
    
    let result = '';
    timeSlotList.forEach((slot, index) => {
      const sessionText = slot.session === 'S' ? 'Sáng' : 'Chiều';
      const dayText = getDayName(parseInt(slot.day));
      const endPeriod = parseInt(slot.startPeriod) + parseInt(slot.duration) - 1;
      const meaning = `${sessionText} ${dayText}, tiết ${slot.startPeriod}-${endPeriod} (${slot.duration} tiết)`;
      
      if (index === 0) {
        result = meaning;
      } else {
        const connector = slot.logic === 'AND' ? ' VÀ ' : ' HOẶC ';
        result += connector + meaning;
      }
    });
    
    return result;
  };

  const validateCurrentTimeSlot = () => {
    if (!currentTimeSlot.session || !currentTimeSlot.day || !currentTimeSlot.startPeriod || !currentTimeSlot.duration) {
      return "Vui lòng điền đầy đủ thông tin";
    }
    
    const numberSlotsPerSession = versionId?.numberSlotsPerSession || 6;
    const startPeriod = parseInt(currentTimeSlot.startPeriod);
    const duration = parseInt(currentTimeSlot.duration);
    
    if (startPeriod + duration - 1 > numberSlotsPerSession) {
      return `Tiết bắt đầu (${startPeriod}) + Số tiết (${duration}) > Tối đa (${numberSlotsPerSession})`;
    }
    
    return null;
  };  
    const handleClickSearchRoom = () => {
    // Reset form when opening dialog
    setCurrentTimeSlot({
      session: '',
      day: '',
      startPeriod: '',
      duration: '',
      logic: 'OR'
    });
    setTimeSlotList([]);
    setSearchRoomData([]);
    setRoomNameFilter("");
    setFilteredRoomData([]);
    setOpenSearchRoom(true);
  };  
  
  const handleRoomNameFilterChange = (event) => {
    setRoomNameFilter(event.target.value);
  };

    const performSearchRoom = () => {
      const generatedTimeSlots = generateTimeSlotString();
      
      if (!generatedTimeSlots) {
        toast.error("Vui lòng thêm ít nhất một ca học vào chuỗi tìm kiếm");
        return;
      }
      
      let body = {
        searchRoomCapacity: searchRoomCapacity,
        timeSlots: generatedTimeSlots,
        versionId: versionId
      };
        request(
        "post",
        "/general-classes/search-rooms",
        (res) => {
          console.log('Search Room: ', res.data);
          setSearchRoomData(res.data);
          setFilteredRoomData(res.data); 
          toast.success("Tìm phòng thành công");
        },
        {
          onError: (e) => {
            toast.error("Có lỗi khi tìm phòng");
          }
        },
        body
      );
    };
  

  return (
        <div>
      <Dialog
        open={openSearchRoom}
        onClose={() => setOpenSearchRoom(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle sx={{ 
          borderBottom: '1px solid #e0e0e0',
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          fontWeight: 600
        }}>
          🔍 Tìm kiếm phòng
        </DialogTitle>         
        
         <DialogContent sx={{ p: 3 }}>
          <Box sx={{ my: 1, display: 'flex', gap: 3, alignItems: 'stretch' }}>            
            
            <TextField
              label="Số lượng tối đa sinh viên"
              value={searchRoomCapacity}
              onChange={handleChangeSearchRoomCapacity}
              variant="outlined"
              type="number"
              size="small"
              sx={{ 
                minWidth: 200,
                '& .MuiOutlinedInput-root': {
                  height: '44px'
                }
              }}
              InputProps={{
                inputProps: { min: 1 }
              }}
            />
              {timeSlotList.length > 0 && (                
                <Paper variant="outlined" sx={{ 
                  px: 2,
                  py: 1, 
                  backgroundColor: '#f8f9fa', 
                  flex: 1, 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: 1,
                  minHeight: '40px'
                }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flex: 1 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 500, fontSize: '0.9rem', whiteSpace: 'nowrap' }}>
                    📋 Danh sách ca học đã thêm:
                  </Typography>
                  <code style={{ 
                    backgroundColor: '#e3f2fd', 
                    padding: '4px 12px', 
                    borderRadius: 6, 
                    fontSize: '0.8rem',
                    fontFamily: 'monospace',
                    border: '1px solid #ccc'
                  }}>
                    {generateTimeSlotString()}
                  </code>
                </Box>
                
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={handleRemoveLastTimeSlot}
                    color="warning"
                    sx={{ textTransform: 'none', fontSize: '0.75rem', py: 0.5, px: 1 }}
                  >
                    ↶ Xóa ca cuối cùng
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={handleClearAllTimeSlots}
                    color="error"
                    sx={{ textTransform: 'none', fontSize: '0.75rem', py: 0.5, px: 1 }}
                  >
                    🗑️ Xóa tất cả
                  </Button>
                </Box>
              </Paper>
            )}
          </Box>

          <Box sx={{ mb: 1 }}>
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
              🕐 Nhập thông tin ca học
            </Typography>
            
            <Paper variant="outlined" sx={{ p: 1, px: 2, mb: 1 }}>
              <Typography variant="subtitle2" sx={{ mb: 1, color: '#666' }}>
                Ca học {timeSlotList.length + 1}:
              </Typography>                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 1, alignItems: 'center', justifyContent: 'space-between' }}>
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>                  {timeSlotList.length > 0 && (
                    <FormControl size="small" sx={{ 
                      minWidth: 100,
                      '& .MuiOutlinedInput-root': {
                        height: '40px'
                      }
                    }}>
                      <InputLabel>Kết hợp</InputLabel>
                      <Select
                        value={currentTimeSlot.logic}
                        onChange={(e) => handleCurrentTimeSlotChange('logic', e.target.value)}
                        label="Kết hợp"
                        sx={{
                          backgroundColor: currentTimeSlot.logic === 'AND' ? '#e8f5e8' : '#fff3e0',
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: currentTimeSlot.logic === 'AND' ? '#4caf50' : '#ff9800'
                          }
                        }}
                      >
                        <MenuItem value="OR">HOẶC</MenuItem>
                        <MenuItem value="AND">VÀ</MenuItem>
                      </Select>
                    </FormControl>
                  )}

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Buổi</InputLabel>
                    <Select
                      value={currentTimeSlot.session}
                      onChange={(e) => handleCurrentTimeSlotChange('session', e.target.value)}
                      label="Buổi"
                    >
                      <MenuItem value="S">Sáng</MenuItem>
                      <MenuItem value="C">Chiều</MenuItem>
                    </Select>
                  </FormControl>

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Thứ</InputLabel>
                    <Select
                      value={currentTimeSlot.day}
                      onChange={(e) => handleCurrentTimeSlotChange('day', e.target.value)}
                      label="Thứ"
                    >
                      {[2, 3, 4, 5, 6, 7, 8].map(day => (
                        <MenuItem key={day} value={day.toString()}>
                          {getDayName(day)}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Tiết BĐ</InputLabel>
                    <Select
                      value={currentTimeSlot.startPeriod}
                      onChange={(e) => handleCurrentTimeSlotChange('startPeriod', e.target.value)}
                      label="Tiết BĐ"
                    >
                      {Array.from({ length: versionId?.numberSlotsPerSession || 6 }, (_, i) => i + 1).map(period => (
                        <MenuItem key={period} value={period.toString()}>
                          {period}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  <FormControl size="small" sx={{ 
                    minWidth: 120,
                    '& .MuiOutlinedInput-root': {
                      height: '40px'
                    }
                  }}>
                    <InputLabel>Số tiết</InputLabel>
                    <Select
                      value={currentTimeSlot.duration}
                      onChange={(e) => handleCurrentTimeSlotChange('duration', e.target.value)}
                      label="Số tiết"
                    >
                      {Array.from({ length: versionId?.numberSlotsPerSession || 6 }, (_, i) => i + 1).map(duration => (
                        <MenuItem key={duration} value={duration.toString()}>
                          {duration}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Box>

                <Button
                  variant="contained"
                  onClick={handleAddToTimeSlotList}
                  disabled={!currentTimeSlot.session || !currentTimeSlot.day || !currentTimeSlot.startPeriod || !currentTimeSlot.duration || !!validateCurrentTimeSlot()}
                  size="small"
                  sx={{ 
                    textTransform: 'none',
                    backgroundColor: '#1976d2',
                    minWidth: 180,
                    height: 40,
                    '&:hover': {
                      backgroundColor: '#1565c0'
                    }
                  }}
                >
                  ➕ Thêm vào chuỗi tìm kiếm
                </Button>
              </Box>

              {/* Validation Error */}
              {currentTimeSlot.session && currentTimeSlot.day && currentTimeSlot.startPeriod && currentTimeSlot.duration && validateCurrentTimeSlot() && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="error" sx={{ fontSize: '0.75rem' }}>
                    ❌ {validateCurrentTimeSlot()}
                  </Typography>
                </Box>
              )}</Paper>          </Box>

          {/* Room Name Filter */}
          {searchRoomData.length > 0 && (
            <Box sx={{ mb: 2 }}>
              <TextField
                label="Lọc theo tên phòng"
                value={roomNameFilter}
                onChange={handleRoomNameFilterChange}
                variant="outlined"
                size="small"
                placeholder="VD: TC-101, D3-301..."
                sx={{ 
                  minWidth: 300,
                  '& .MuiOutlinedInput-root': {
                    height: '40px'
                  }
                }}
                helperText={`Hiển thị ${filteredRoomData.length}/${searchRoomData.length} phòng`}
              />
            </Box>
          )}

          {/* Results Table */}
          <Box sx={{ 
            maxHeight: '400px', 
            overflow: 'auto',
            border: '1px solid #e0e0e0',
            borderRadius: 1,
            '& .MuiTableContainer-root': {
              maxHeight: 'none'
            },
            '& .MuiTable-root thead th': {
              position: 'sticky',
              top: 0,
              backgroundColor: '#f5f5f5',
              zIndex: 10,
              borderBottom: '2px solid #e0e0e0'
            }
          }}>            
          <StandardTable
              columns={searchRoomColumns}
              data={filteredRoomData}
              hideCommandBar
              options={{
                selection: false,
                search: true,
                paging: false,
                toolbar: false
              }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ padding: "16px", gap: "8px", borderTop: '1px solid #e0e0e0' }}>
          <Button
            onClick={() => setOpenSearchRoom(false)}
            variant="outlined"
            sx={{ minWidth: "80px", padding: "6px 16px" }}
          >
            Hủy
          </Button>
          <Button
            onClick={performSearchRoom}
            color="primary"
            variant="contained"
            autoFocus
            disabled={timeSlotList.length === 0}
            sx={{ minWidth: "120px", padding: "6px 16px" }}
          >
            🔍 Tìm phòng
          </Button>
        </DialogActions>
      </Dialog>

        </div>
    );
}