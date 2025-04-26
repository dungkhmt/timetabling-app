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
import { useGeneralSchedule } from "services/useGeneralScheduleData"; // Import hook

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
  const [editVersionData, setEditVersionData] = useState({ id: null, name: "", status: "" });

  const { handlers: saveScheduleToVersion } = useGeneralSchedule();

  useEffect(() => {
    setSearchNameInput(searchName);
  }, [searchName]);
  
  const handleSearchInputChange = (e) => {
    const value = e.target.value;
    setSearchNameInput(value);
    
    // Clear previous timeout
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
    if (!selectedSemester?.semester) {
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
      semester: selectedSemester.semester,
      userId: "timetablingadmin01" 
    };
    
    try {
      const createdVersion = await createVersion(versionData);
      
      if (createdVersion) {
        setOpenNewVersionDialog(false);
        setNewVersionName("");
        setNewVersionStatus("DRAFT");
        
        if (onCreateSuccess) {
          console.log("Refreshing versions list after creation");
          onCreateSuccess(createdVersion);
        }
        
        if (handleVersionSelect) {
          console.log("Auto-selecting newly created version:", createdVersion);
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
        status: selectedVersionForMenu.status
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

    try {
      setIsEditing(true);
      
      await timeTablingVersionRepository.updateVersion(
        editVersionData.id,
        editVersionData.name,
        editVersionData.status
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
      <Box sx={{ 
        py: 4, 
        display: 'flex', 
        flexDirection: 'column',
        height: '100%'
      }}>
        <Paper 
          elevation={0} 
          sx={{ 
            p: 3, 
            mb: 3, 
            borderRadius: 2,
            background: theme.palette.background.paper,
            boxShadow: '0 2px 10px rgba(0,0,0,0.08)'
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h5" component="h1" sx={{ fontWeight: 600 }}>
              Quản lý phiên bản thời khóa biểu
            </Typography>
            <Button
              variant="contained"
              color="primary"
              startIcon={<Add />}
              onClick={() => setOpenNewVersionDialog(true)}
              sx={{ 
                px: 3,
                py: 1,
                borderRadius: 2,
                textTransform: 'none',
                fontWeight: 600
              }}
            >
              Tạo phiên bản mới
            </Button>
          </Box>
          
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <GeneralSemesterAutoComplete
                selectedSemester={selectedSemester}
                setSelectedSemester={setSelectedSemester}
                sx={{ width: "100%" }}
                label="Lọc theo học kỳ"
                size="small"
              />
            </Grid>
            <Grid item xs={12} md={7}>
              <TextField
                fullWidth
                variant="outlined"
                size="small"
                placeholder="Tìm kiếm phiên bản..."
                value={searchNameInput}
                onChange={handleSearchInputChange}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search />
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
                sx={{ backgroundColor: '#f5f5f5', borderRadius: 1 }}
              />
            </Grid>
            <Grid item xs={12} md={1}>
              <Tooltip title="Xóa bộ lọc">
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
              </Tooltip>
            </Grid>
          </Grid>
          
          {(selectedSemester || searchNameInput) && (
            <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {selectedSemester && (
                <Chip 
                  label={`Học kỳ: ${selectedSemester.semester}`}
                  onDelete={() => setSelectedSemester(null)}
                  color="primary"
                  variant="outlined"
                />
              )}
              {searchNameInput && (
                <Chip 
                  label={`Tìm kiếm: ${searchNameInput}`}
                  onDelete={handleClearSearch}
                  color="primary"
                  variant="outlined"
                />
              )}
            </Box>
          )}
        </Paper>

        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8, flexGrow: 1 }}>
            <CircularProgress />
          </Box>
        ) : versions.length === 0 ? (
          <Paper 
            elevation={0} 
            sx={{ 
              display: 'flex', 
              flexDirection: 'column', 
              alignItems: 'center', 
              justifyContent: 'center', 
              py: 8,
              borderRadius: 2,
              backgroundColor: 'rgba(0,0,0,0.02)',
              border: '1px dashed rgba(0,0,0,0.15)',
              flexGrow: 1
            }}
          >
            <CalendarMonth sx={{ fontSize: 60, color: 'text.disabled', mb: 2 }} />
            <Typography variant="h6" color="text.secondary" align="center">
              {selectedSemester || searchNameInput 
                ? `Không tìm thấy phiên bản thời khóa biểu nào ${selectedSemester ? 'cho kỳ học ' + selectedSemester.semester : ''} ${searchNameInput ? 'với từ khóa "' + searchNameInput + '"' : ''}.` 
                : "Vui lòng chọn học kỳ hoặc nhập từ khóa để tìm kiếm phiên bản thời khóa biểu."}
            </Typography>

          </Paper>
        ) : (
          <Box sx={{ flexGrow: 1 }}>
            <Grid container spacing={3}>
              {versions.map((version) => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={version.id}>
                  <Card 
                    sx={{ 
                      borderRadius: 2, 
                      transition: 'all 0.3s ease',
                      cursor: 'pointer',
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: '0 8px 24px rgba(0,0,0,0.12)'
                      }
                    }}
                    onClick={() => handleVersionSelect(version)}
                  >
                    <CardHeader
                      avatar={
                        <Avatar 
                          sx={{ 
                            bgcolor: getStatusColor(version.status) + '.main',
                            width: 40,
                            height: 40
                          }}
                        >
                          {getStatusIcon(version.status)}
                        </Avatar>
                      }
                      action={
                        <Tooltip title="Xem thêm tùy chọn">
                          <IconButton aria-label="settings" onClick={(e) => handleOpenMenu(e, version)}>
                            <MoreVert />
                          </IconButton>
                        </Tooltip>
                      }
                      title={
                        <Typography variant="h6" noWrap sx={{ fontWeight: 600 }}>
                          {version.name}
                        </Typography>
                      }
                      subheader={
                        <Chip 
                          label={getStatusLabel(version.status)}
                          color={getStatusColor(version.status)}
                          size="small"
                          icon={getStatusIcon(version.status)}
                          sx={{ mt: 0.5 }}
                        />
                      }
                    />
                    
                    <CardContent sx={{ pt: 0, flexGrow: 1 }}>
                      <Box sx={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        mt: 2,
                        color: 'text.secondary'
                      }}>
                        <School fontSize="small" sx={{ mr: 1 }} />
                        <Typography variant="body2" noWrap>
                          Học kỳ: <b>{version.semester || "Chưa xác định"}</b>
                        </Typography>
                      </Box>
                      
                      <Box sx={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        mt: 1,
                        color: 'text.secondary'
                      }}>
                        <AccessTime fontSize="small" sx={{ mr: 1 }} />
                        <Typography variant="body2">
                          {new Date(version.createdStamp).toLocaleDateString('vi-VN', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </Typography>
                      </Box>
                    </CardContent>
                    
                    <CardActions sx={{ justifyContent: 'flex-end', p: 2, pt: 0 }}>
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
                          fontWeight: 600,
                          borderRadius: 1.5
                        }}
                      >
                        Chọn
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Box>
        )}
      </Box>

      {/* Menu for version actions */}
      <Menu
        anchorEl={menuAnchorEl}
        open={Boolean(menuAnchorEl)}
        onClose={handleCloseMenu}
      >
        <MenuItem onClick={handleConfirmDeleteOpen}>
          <Delete fontSize="small" sx={{ mr: 1 }} />
          Xóa phiên bản
        </MenuItem>
        <MenuItem onClick={handleEditDialogOpen}>
          <Edit fontSize="small" sx={{ mr: 1 }} />
          Chỉnh sửa
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
        <DialogActions>
          <Button onClick={handleConfirmDeleteClose} variant="outlined">
            Hủy
          </Button>
          <Button
            onClick={handleDeleteVersion}
            variant="contained"
            color="error"
            disabled={isDeleting}
            startIcon={isDeleting && <CircularProgress size={20} />}
          >
            Xóa
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
              backgroundColor: 'rgba(255, 255, 255, 0.7)',
              zIndex: 1,
              borderRadius: 1
            }}>
              <CircularProgress />
            </Box>
          )}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <GeneralSemesterAutoComplete
              selectedSemester={selectedSemester}
              setSelectedSemester={setSelectedSemester}
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
            >
              <MenuItem value="DRAFT">Bản nháp</MenuItem>
              <MenuItem value="PUBLISHED">Đã xuất bản</MenuItem>
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => setOpenNewVersionDialog(false)}
            disabled={isCreating}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleCreateVersion} 
            variant="contained" 
            disabled={isCreating || !newVersionName || !selectedSemester}
            startIcon={isCreating ? <CircularProgress size={20} /> : null}
          >
            {isCreating ? 'Đang tạo...' : 'Tạo'}
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
              backgroundColor: 'rgba(255, 255, 255, 0.7)',
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
              select
              label="Trạng thái"
              size="small"
              fullWidth
              value={editVersionData.status}
              onChange={(e) => setEditVersionData({...editVersionData, status: e.target.value})}
              disabled={isEditing}
            >
              <MenuItem value="DRAFT">Bản nháp</MenuItem>
              <MenuItem value="PUBLISHED">Đã xuất bản</MenuItem>
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => setEditVersionDialog(false)} 
            disabled={isEditing}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleUpdateVersion} 
            variant="contained"
            disabled={isEditing || !editVersionData.name}
            startIcon={isEditing ? <CircularProgress size={20} /> : null}
          >
            {isEditing ? 'Đang cập nhật...' : 'Lưu'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default VersionSelectionScreen;