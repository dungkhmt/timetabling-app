import React, { useEffect, useState } from "react";
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
import GeneralSemesterAutoComplete from "../common-components/GeneralSemesterAutoComplete";
import { toast } from "react-toastify";
import { timeTablingVersionRepository } from "repositories/timeTablingVersionRepository";

const VersionSelectionScreen = ({
  selectedSemester,
  setSelectedSemester,
  searchName,
  setSearchName,
  versions,
  isLoading,
  handleVersionSelect,
  openNewVersionDialog,
  setOpenNewVersionDialog,
  newVersionName,
  setNewVersionName,
  newVersionStatus,
  setNewVersionStatus,
  isCreating,
  onCreateSuccess,
  createVersion 
}) => {
  const theme = useTheme();
  const [menuAnchorEl, setMenuAnchorEl] = useState(null);
  const [selectedVersionForMenu, setSelectedVersionForMenu] = useState(null);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [searchNameInput, setSearchNameInput] = useState(searchName);
  const [typingTimeout, setTypingTimeout] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editVersionDialog, setEditVersionDialog] = useState(false);
  const [editVersionData, setEditVersionData] = useState({ id: null, name: "", status: "", numberSlotsPerSession: 6 });
  const [numberSlotsPerSession, setNumberSlotsPerSession] = useState(6);
  const [newVersionSemester, setNewVersionSemester] = useState(selectedSemester);
  useEffect(() => {
    setSearchNameInput(searchName);
  }, [searchName]);
  
  // Cập nhật newVersionSemester khi selectedSemester thay đổi 
  useEffect(() => {
    setNewVersionSemester(selectedSemester);
  }, [selectedSemester]);
  const handleSearchInputChange = (e) => {
    const value = e.target.value;
    setSearchNameInput(value);
    
    if (typingTimeout) {
      clearTimeout(typingTimeout);
    }
    
    const timeout = setTimeout(() => {
      setSearchName(value);
    }, 500); 
    
    setTypingTimeout(timeout);
  };
  
  const handleClearSearch = () => {
    setSearchNameInput("");
    setSearchName("");
  };  
  const handleCreateVersion = async () => {
    if (!newVersionSemester?.semester) {
      toast.error("Vui lòng chọn học kỳ trước khi tạo phiên bản!");
      return;
    }
    if (!newVersionName) {
      toast.error("Vui lòng nhập tên phiên bản!");
      return;
    }

    const versionData = {
      name: newVersionName,
      status: newVersionStatus,
      semester: newVersionSemester.semester,
      userId: "timetablingadmin01",
      numberSlotsPerSession: numberSlotsPerSession 
    };
    
    try {
      const createdVersion = await createVersion(versionData);
      console.log("Created version:", createdVersion);
        if (createdVersion) {
          setOpenNewVersionDialog(false);
          setNewVersionName("");
          setNewVersionStatus("DRAFT");
          // setNumberSlotsPerSession(numberSlotsPerSession);
        if (onCreateSuccess) {
          onCreateSuccess(createdVersion);
        }
        
        if (handleVersionSelect) {
          handleVersionSelect(createdVersion);
        }
      }
    } catch (error) {
      console.error('Error creating version:', error);
      toast.error("Có lỗi khi tạo phiên bản mới!");
    }
  };

  const handleOpenMenu = (event, version) => {
    event.stopPropagation();
    setMenuAnchorEl(event.currentTarget);
    setSelectedVersionForMenu(version);
  };

  const handleCloseMenu = () => {
    setMenuAnchorEl(null);
    setSelectedVersionForMenu(null);
  };

  const handleConfirmDeleteOpen = () => {
    setConfirmDeleteOpen(true);
    setMenuAnchorEl(null);
  };

  const handleConfirmDeleteClose = () => {
    setConfirmDeleteOpen(false);
  };

  const handleDeleteVersion = async () => {
    if (!selectedVersionForMenu) return;

    try {
      setIsDeleting(true);
      await timeTablingVersionRepository.deleteVersion(selectedVersionForMenu.id);
      
      toast.success(`Đã xóa phiên bản "${selectedVersionForMenu.name}"`);
      
      if (onCreateSuccess) {
        onCreateSuccess(null);
      }
    } catch (error) {
      console.error("Delete version error:", error);
      toast.error("Có lỗi khi xóa phiên bản!");
    } finally {
      setIsDeleting(false);
      setConfirmDeleteOpen(false);
      setSelectedVersionForMenu(null);
    }
  };

  const handleEditDialogOpen = () => {
    if (selectedVersionForMenu) {
      setEditVersionData({
        id: selectedVersionForMenu.id,
        name: selectedVersionForMenu.name,
        status: selectedVersionForMenu.status,
        numberSlotsPerSession: selectedVersionForMenu.numberSlotsPerSession || 6
      });
      setEditVersionDialog(true);
      setMenuAnchorEl(null);
    }
  };

  const handleUpdateVersion = async () => {
    if (!editVersionData.name) {
      toast.error("Vui lòng nhập tên phiên bản!");
      return;
    }
    if (editVersionData.numberSlotsPerSession === null || editVersionData.numberSlotsPerSession === undefined || parseInt(editVersionData.numberSlotsPerSession, 10) <= 0) {
      toast.error("Số tiết mỗi buổi phải là một số dương!");
      return;
    }

    try {
      setIsEditing(true);
      
      await timeTablingVersionRepository.updateVersion(
        editVersionData.id,
        editVersionData.name,
        editVersionData.status,
        parseInt(editVersionData.numberSlotsPerSession, 10)
      );
      
      setEditVersionDialog(false);
      
      if (onCreateSuccess) {
        onCreateSuccess(null);
      }
      
      toast.success("Cập nhật phiên bản thành công!");
    } catch (error) {
      console.error("Error updating version:", error);
      toast.error("Có lỗi khi cập nhật phiên bản!");
    } finally {
      setIsEditing(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "DRAFT": return "warning";
      case "PUBLISHED": return "success";
      default: return "default";
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case "DRAFT": return <Drafts fontSize="small" />;
      case "PUBLISHED": return <CheckCircle fontSize="small" />;
      default: return null;
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case "DRAFT": return "Bản nháp";
      case "PUBLISHED": return "Đã xuất bản";
      default: return status;
    }
  };

  const handleResetFilters = () => {
    setSelectedSemester(null);
    setSearchNameInput("");
    setSearchName("");
  };

  return (
    <Container maxWidth="xl">
      <Box sx={{ py: 3, height: '100%' }}>
        {/* Header Section */}
        <Paper 
          elevation={2} 
          sx={{ 
            p: 2.5, 
            mb: 3, 
            borderRadius: 2,
            boxShadow: '0 0 15px rgba(0,0,0,0.05)'
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
            <Typography variant="h5" sx={{ fontWeight: 600, color: theme.palette.primary.main }}>
              Quản lý phiên bản thời khóa biểu
            </Typography>
            <Button
              variant="contained"
              color="primary"
              startIcon={<Add />}
              onClick={() => setOpenNewVersionDialog(true)}
              sx={{ 
                px: 2.5,
                py: 0.8,
                borderRadius: 1.5,
                textTransform: 'none',
                fontWeight: 500
              }}
            >
              Tạo phiên bản mới
            </Button>
          </Box>
          
          {/* Filter Section */}
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6} md={4} lg={3}>
              <GeneralSemesterAutoComplete
                selectedSemester={selectedSemester}
                setSelectedSemester={setSelectedSemester}
                sx={{ width: "100%" }}
                label="Lọc theo học kỳ"
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={7} lg={8}>
              <TextField
                fullWidth
                variant="outlined"
                size="small"
                placeholder="Tìm kiếm theo tên phiên bản..."
                value={searchNameInput}
                onChange={handleSearchInputChange}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search color="action" />
                    </InputAdornment>
                  ),
                  endAdornment: searchNameInput && (
                    <InputAdornment position="end">
                      <IconButton size="small" onClick={handleClearSearch}>
                        <Clear fontSize="small" />
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
                sx={{ backgroundColor: 'rgba(0,0,0,0.02)', borderRadius: 1 }}
              />
            </Grid>
            <Grid item xs={12} sm={12} md={1} lg={1}>
              <Tooltip title="Xóa bộ lọc">
                <span>
                  <Button 
                    variant="outlined"
                    color="primary"
                    onClick={handleResetFilters}
                    disabled={!selectedSemester && !searchNameInput}
                    fullWidth
                    sx={{ height: '40px' }}
                  >
                    <FilterAlt />
                  </Button>
                </span>
              </Tooltip>
            </Grid>
          </Grid>
          
          {/* Active Filters */}
          {(selectedSemester || searchNameInput) && (
            <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {selectedSemester && (
                <Chip 
                  label={`Học kỳ: ${selectedSemester.semester}`}
                  onDelete={() => setSelectedSemester(null)}
                  color="primary"
                />
              )}
              {searchNameInput && (
                <Chip 
                  label={`Tên phiên bản: ${searchNameInput}`}
                  onDelete={handleClearSearch}
                  color="primary"
                />
              )}
            </Box>
          )}
        </Paper>

        {/* Content Section - Loading */}
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 10 }}>
            <CircularProgress />
          </Box>
        )}

        {/* Content Section - Empty State */}
        {!isLoading && versions.length === 0 && (
          <Paper 
            elevation={0} 
            sx={{ 
              display: 'flex', 
              flexDirection: 'column', 
              alignItems: 'center', 
              justifyContent: 'center', 
              py: 10,
              borderRadius: 2,
              backgroundColor: 'rgba(0,0,0,0.01)',
              border: '1px dashed rgba(0,0,0,0.1)',
            }}
          >
            <CalendarMonth sx={{ fontSize: 70, color: 'text.disabled', mb: 2, opacity: 0.4 }} />
            <Typography variant="h6" color="text.secondary" align="center" sx={{ maxWidth: 600, px: 2 }}>
              {selectedSemester || searchNameInput 
                ? `Không tìm thấy phiên bản thời khóa biểu nào ${selectedSemester ? 'cho kỳ học ' + selectedSemester.semester : ''} ${searchNameInput ? 'với từ khóa "' + searchNameInput + '"' : ''}.` 
                : "Chọn học kỳ hoặc nhập từ khóa để tìm kiếm phiên bản thời khóa biểu."}
            </Typography>
            <Button 
              variant="outlined" 
              color="primary" 
              startIcon={<Add />}
              onClick={() => setOpenNewVersionDialog(true)}
              sx={{ mt: 3, textTransform: 'none' }}
            >
              Tạo phiên bản mới
            </Button>
          </Paper>
        )}

        {/* Content Section - Version Cards */}
        {!isLoading && versions.length > 0 && (
          <Grid container spacing={2.5}>
            {versions.map((version) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={version.id}>
                <Card 
                  sx={{ 
                    borderRadius: 2, 
                    transition: 'all 0.2s ease',
                    cursor: 'pointer',
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    border: `1px solid ${theme.palette.divider}`,
                    overflow: 'hidden', // Add this to prevent content overflow
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: '0 8px 24px rgba(0,0,0,0.08)',
                      borderColor: theme.palette.primary.light
                    }
                  }}
                  onClick={() => handleVersionSelect(version)}
                  elevation={1}
                >
                  <CardHeader
                    avatar={
                      <Avatar 
                        sx={{ 
                          bgcolor: getStatusColor(version.status) + '.main',
                          width: 36, // Reduced from 40px
                          height: 36, // Reduced from 40px
                          fontSize: '1rem' // Smaller icon size
                        }}
                      >
                        {getStatusIcon(version.status)}
                      </Avatar>
                    }
                    action={
                      <IconButton 
                        aria-label="settings" 
                        onClick={(e) => handleOpenMenu(e, version)}
                        size="small"
                        sx={{ padding: '4px' }} // Smaller padding
                      >
                        <MoreVert fontSize="small" />
                      </IconButton>
                    }
                    title={
                      <Tooltip title={version.name} placement="top">
                        <Typography 
                          variant="subtitle1" 
                          sx={{ 
                            fontWeight: 600,
                            textOverflow: 'ellipsis',
                            overflow: 'hidden',
                            whiteSpace: 'nowrap',
                            maxWidth: '210px'  // Increased from 180px
                          }}
                        >
                          {version.name}
                        </Typography>
                      </Tooltip>
                    }
                    subheader={
                      <Chip 
                        label={getStatusLabel(version.status)}
                        color={getStatusColor(version.status)}
                        size="small"
                        icon={getStatusIcon(version.status)}
                        sx={{ 
                          mt: 0.5, 
                          height: 22, // Smaller chip height
                          '& .MuiChip-label': { 
                            px: 1, 
                            fontSize: '0.7rem' // Smaller font
                          },
                          '& .MuiChip-icon': {
                            fontSize: '0.85rem' // Smaller icon
                          }
                        }}
                      />
                    }
                    sx={{ 
                      pb: 0,
                      px: 1.5, // Reduced horizontal padding
                      '& .MuiCardHeader-content': { 
                        overflow: 'hidden', 
                        minWidth: 0,
                        mr: 1 // Make sure there's margin between content and action
                      },
                      '& .MuiCardHeader-action': {
                        marginRight: 0,
                        marginTop: 0
                      },
                      '& .MuiCardHeader-avatar': {
                        marginRight: 1.5 // Reduced margin
                      }
                    }}
                  />
                  
                  <CardContent sx={{ pt: 1, pb: 1, flexGrow: 1 }}>                    <Box sx={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      mt: 1.5,
                      color: 'text.secondary'
                    }}>
                      <School fontSize="small" sx={{ mr: 1, color: theme.palette.primary.main, opacity: 0.7, flexShrink: 0 }} />
                      <Tooltip title={version.semester || "Chưa xác định"}>
                        <Typography 
                          variant="body2" 
                          sx={{ 
                            textOverflow: 'ellipsis',
                            overflow: 'hidden',
                            whiteSpace: 'nowrap'
                          }}
                        >
                          {version.semester || "Chưa xác định"}
                        </Typography>
                      </Tooltip>
                    </Box>
                    
                    {version.numberSlotsPerSession && (
                      <Box sx={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        mt: 1,
                        color: 'text.secondary'
                      }}>
                        <AccessTime fontSize="small" sx={{ mr: 1, color: theme.palette.primary.main, opacity: 0.7, flexShrink: 0 }} />
                        <Typography 
                          variant="body2" 
                          sx={{ 
                            textOverflow: 'ellipsis',
                            overflow: 'hidden',
                            whiteSpace: 'nowrap'
                          }}
                        >
                          {version.numberSlotsPerSession} tiết/buổi
                        </Typography>
                      </Box>
                    )}
                    
                    <Box sx={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      mt: 1,
                      color: 'text.secondary',
                      fontSize: '0.85rem'
                    }}>
                      <AccessTime fontSize="small" sx={{ mr: 1, opacity: 0.6, flexShrink: 0 }} />
                      <Typography 
                        variant="caption"
                        sx={{ 
                          textOverflow: 'ellipsis',
                          overflow: 'hidden',
                          whiteSpace: 'nowrap'
                        }}
                      >
                        {new Date(version.createdStamp).toLocaleDateString('vi-VN', {
                          year: 'numeric',
                          month: '2-digit',
                          day: '2-digit',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </Typography>
                    </Box>
                  </CardContent>
                  
                  <CardActions sx={{ justifyContent: 'flex-end', p: 1.5, pt: 0 }}>
                    <Button 
                      size="small" 
                      variant="contained" 
                      color="primary"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleVersionSelect(version);
                      }}
                      sx={{ 
                        textTransform: 'none',
                        fontWeight: 500,
                        borderRadius: 1.5,
                        px: 2
                      }}
                    >
                      Chọn
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>

      {/* Menu for version actions */}
      <Menu
        anchorEl={menuAnchorEl}
        open={Boolean(menuAnchorEl)}
        onClose={handleCloseMenu}
        elevation={2}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        <MenuItem onClick={handleEditDialogOpen}>
          <Edit fontSize="small" sx={{ mr: 1.5, color: theme.palette.primary.main }} />
          Chỉnh sửa
        </MenuItem>
        <MenuItem onClick={handleConfirmDeleteOpen}>
          <Delete fontSize="small" sx={{ mr: 1.5, color: theme.palette.error.main }} />
          Xóa phiên bản
        </MenuItem>
      </Menu>

      {/* Confirm delete dialog */}
      <Dialog
        open={confirmDeleteOpen}
        onClose={handleConfirmDeleteClose}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Bạn có chắc chắn muốn xóa phiên bản "{selectedVersionForMenu?.name}"? Hành động này không thể hoàn tác.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ p: 2, pt: 1 }}>
          <Button onClick={handleConfirmDeleteClose} variant="outlined">
            Hủy
          </Button>
          <Button
            onClick={handleDeleteVersion}
            variant="contained"
            color="error"
            disabled={isDeleting}
            startIcon={isDeleting && <CircularProgress size={20} color="inherit" />}
          >
            {isDeleting ? "Đang xóa..." : "Xóa"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog for creating new version */}
      <Dialog 
        open={openNewVersionDialog} 
        onClose={() => !isCreating && setOpenNewVersionDialog(false)}
        maxWidth="xs"
        fullWidth
        disableEscapeKeyDown={isCreating}
      >
        <DialogTitle>Tạo phiên bản mới</DialogTitle>
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
          )}          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <GeneralSemesterAutoComplete
              selectedSemester={newVersionSemester}
              setSelectedSemester={setNewVersionSemester}
              label="Học kỳ"
              size="small"
              required
              disabled={isCreating}
            />
            
            <TextField
              autoFocus
              label="Tên phiên bản"
              size="small"
              fullWidth
              value={newVersionName}
              onChange={(e) => setNewVersionName(e.target.value)}
              required
              disabled={isCreating}
            />
              <TextField
              select
              label="Trạng thái"
              size="small"
              fullWidth
              value={newVersionStatus}
              onChange={(e) => setNewVersionStatus(e.target.value)}
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
        <DialogActions sx={{ p: 2, pt: 1 }}>
          <Button 
            onClick={() => setOpenNewVersionDialog(false)}
            disabled={isCreating}
            variant="outlined"
          >
            Hủy
          </Button>          
          <Button 
            onClick={handleCreateVersion} 
            variant="contained" 
            disabled={isCreating || !newVersionName || !newVersionSemester || !numberSlotsPerSession || parseInt(numberSlotsPerSession) < 1}
            startIcon={isCreating ? <CircularProgress size={20} color="inherit" /> : null}
          >
            {isCreating ? 'Đang tạo...' : 'Tạo phiên bản'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit version dialog */}
      <Dialog
        open={editVersionDialog}
        onClose={() => !isEditing && setEditVersionDialog(false)}
        maxWidth="xs"
        fullWidth
        disableEscapeKeyDown={isEditing}
      >
        <DialogTitle>Chỉnh sửa phiên bản</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          {isEditing && (
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
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              autoFocus
              label="Tên phiên bản"
              size="small"
              fullWidth
              value={editVersionData.name}
              onChange={(e) => setEditVersionData({...editVersionData, name: e.target.value})}
              required
              disabled={isEditing}
            />
            
            <TextField
              margin="dense"
              label="Số tiết tối đa mỗi buổi (*)"
              type="number"
              fullWidth
              variant="outlined"
              value={editVersionData.numberSlotsPerSession}
              onChange={(e) => {
                const val = e.target.value;
                 if (val === "" || (parseInt(val, 10) > 0 && parseInt(val, 10) <= 20)) {
                  setEditVersionData({ ...editVersionData, numberSlotsPerSession: val === "" ? "" : parseInt(val,10) });
                }
              }}
              InputProps={{
                inputProps: { 
                  min: 1,
                  max: 20
                }
              }}
              size="small"
              helperText="Giá trị từ 1 đến 20."
            />
            
            <TextField
              select
              label="Trạng thái"
              size="small"
              fullWidth
              value={editVersionData.status}
              onChange={(e) => setEditVersionData({...editVersionData, status: e.target.value})}
              disabled={isEditing}
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
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2, pt: 1 }}>
          <Button 
            onClick={() => setEditVersionDialog(false)} 
            disabled={isEditing}
            variant="outlined"
          >
            Hủy
          </Button>
          <Button 
            onClick={handleUpdateVersion} 
            variant="contained"
            disabled={isEditing || !editVersionData.name}
            startIcon={isEditing ? <CircularProgress size={20} color="inherit" /> : null}
          >
            {isEditing ? 'Đang cập nhật...' : 'Lưu thay đổi'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default VersionSelectionScreen;