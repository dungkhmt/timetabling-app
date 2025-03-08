import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  Button,
  TextField,
  MenuItem,
  Tabs,
  Tab,
  Box,
  Checkbox,
  CircularProgress,
  DialogActions,
  IconButton, // Thêm IconButton vào imports
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close"; // Thêm import này
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { request } from "api";
import { useGeneralSchedule } from "services/useGeneralScheduleData";
import { toast } from "react-toastify";

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 1 }}>{children}</Box>}
    </div>
  );
}

const ViewClassDetailDialog = ({
  classData,
  isOpen,
  closeDialog,
  onRefreshParent,
}) => {
  const [subClasses, setSubClasses] = useState([]);
  const [openNewDialog, setOpenNewDialog] = useState(false);
  const [tabValue, setTabValue] = useState(0);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [filteredGroups, setFilteredGroups] = useState([]);
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: 10,
  });
  const [isAddingSubClass, setIsAddingSubClass] = useState(false);
  const [isLoadingSubClasses, setIsLoadingSubClasses] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedSubClass, setSelectedSubClass] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedIds, setSelectedIds] = useState([]); // Add this

  const { states, handlers, setters } = useGeneralSchedule();

  const [newSubClass, setNewSubClass] = useState({
    studentCount: "",
    classType: "",
    classCount: "",
  });

  useEffect(() => {
    if (isOpen && classData?.id && tabValue === 1) {
      fetchClassGroups();
    }
  }, [isOpen, classData?.id, tabValue]);

  useEffect(() => {
    if (isOpen && classData?.classCode && tabValue === 0) {
      fetchSubClasses();
    }
  }, [isOpen, classData?.classCode, tabValue]);

  const fetchSubClasses = async () => {
    if (!classData?.classCode) return;

    setIsLoadingSubClasses(true);
    try {
      const data = await handlers.getSubClasses(classData.classCode);
      setSubClasses(data || []);
    } catch (error) {
      console.error("Failed to fetch subclasses", error);
      toast.error("Không thể tải danh sách lớp con");
    } finally {
      setIsLoadingSubClasses(false);
    }
  };

  const fetchClassGroups = async () => {
    if (!classData?.id) return;

    setLoading(true);
    try {
      const data = await handlers.getClassGroups(classData.id);
      setGroups(data || []);
      console.log(data);
    } catch (error) {
      console.error("Failed to fetch class groups", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (searchText.trim() === "") {
      setFilteredGroups(
        [...groups].sort(
          (a, b) => (b.assigned === true) - (a.assigned === true)
        )
      );
    } else {
      const filtered = groups.filter((group) =>
        group.groupName.toLowerCase().includes(searchText.toLowerCase())
      );
      setFilteredGroups(
        [...filtered].sort(
          (a, b) => (b.assigned === true) - (a.assigned === true)
        )
      );
    }
  }, [groups, searchText]);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleAddSubClass = async () => {
    setIsAddingSubClass(true);
    const payload = {
      fromParentClassId: classData?.id,
      classType: newSubClass.classType,
      numberStudents: parseInt(newSubClass.studentCount, 10),
      duration: classData?.duration,
      numberClasses: parseInt(newSubClass.classCount, 10),
    };

    try {
      await request(
        "post",
        "/plan-general-classes/make-subclass",
        async (res) => {
          await fetchSubClasses(); // Fetch updated list after successful POST
          setOpenNewDialog(false);
          setNewSubClass({ studentCount: "", classType: "", classCount: "" });
          await states.refetchNoSchedule();
          if (onRefreshParent) {
            await onRefreshParent();
          }
          toast.success("Thêm lớp con thành công");
        },
        (err) => {
          throw err;
        },
        payload
      );
    } catch (error) {
      console.error("Failed to add subclass", error);
      toast.error("Thêm lớp con thất bại");
    } finally {
      setIsAddingSubClass(false);
    }
  };

  const handleGroupSelectionChange = async (id) => {
    const group = groups.find((g) => g.id === id);
    if (!group) return;
    try {
      if (!group.assigned) {
        await handlers.updateClassGroup(classData?.id, id);
      } else {
        await handlers.deleteClassGroup(classData?.id, id);
      }
      setGroups(
        groups.map((g) => (g.id === id ? { ...g, assigned: !g.assigned } : g))
      );
    } catch (error) {
      console.error("Failed to update group", error);
    }
  };

  const handleSelectAllGroups = (event) => {
    const checked = event.target.checked;
    setGroups(
      groups.map((group) => ({
        ...group,
        assigned: checked,
      }))
    );
  };

  const areAllGroupsSelected =
    groups.length > 0 && groups.every((group) => group.assigned);

  const handleDeleteSubClass = async () => {
    if (!selectedSubClass?.id) return;
    setIsDeleting(true);

    const id = parseInt(selectedSubClass.id.toString().split("-")[0]);
    await handlers.handleDeleteByIds([id]);
    await fetchSubClasses();
    setDeleteDialogOpen(false);
    if (onRefreshParent) {
      await onRefreshParent();
    }
  };

  useEffect(() => {
    setters.setSelectedRows(selectedIds);
  }, [selectedIds]);

  const handleSelectionChange = (newSelection) => {
    setSelectedIds(newSelection);
  };

  const handleClose = () => {
    setSelectedIds([]);
    setters.setSelectedRows([]);
    closeDialog();
  };

  const groupColumns = [
    {
      field: "assigned",
      headerName: "Đã chọn",
      width: 130,
      renderHeader: () => (
        <div style={{ display: "flex", alignItems: "center", gap: "4px" }}>
          <Checkbox
            checked={areAllGroupsSelected}
            onChange={handleSelectAllGroups}
            indeterminate={
              groups.some((g) => g.assigned) && !areAllGroupsSelected
            }
            size="small"
          />
          <span className="MuiDataGrid-columnHeaderTitle font-medium">
            Đã chọn
          </span>
        </div>
      ),
      renderCell: (params) => (
        <Checkbox
          checked={params.row.assigned}
          onChange={() => handleGroupSelectionChange(params.row.id)}
        />
      ),
    },
    { field: "groupName", headerName: "Nhóm", width: 300 },
  ];

  const classDetailsColumns = [
    { field: "classCode", headerName: "Mã lớp", width: 120 },
    { field: "classType", headerName: "Loại lớp", width: 80 },
    {
      field: "duration",
      headerName: "Thời lượng",
      width: 130,
      valueGetter: (params) => {
        return params.row.duration ? `${params.row.duration} tiết` : "";
      },
    },
    { field: "semester", headerName: "Kỳ học", width: 90 },
    { field: "learningWeeks", headerName: "Tuần học", width: 150 },
    { field: "quantityMax", headerName: "SL MAX", width: 110 },
  ];

  return (
    <>
      <Dialog open={isOpen} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogTitle
          sx={{
            pb: 0,
            pt: 1,
            fontSize: "1rem",
            minHeight: "40px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between", 
          }}
        >
          <div className="flex items-center">
            Thông tin của lớp: {classData?.moduleCode} - {classData?.moduleName} -{" "}
            {classData?.id}/{classData?.classCode}
            {!classData?.parentClassId && (
              <span className="ml-2 text-sm text-red-500">(Lớp con)</span>
            )}
          </div>
          <IconButton
            aria-label="close"
            onClick={handleClose}
            sx={{
              position: "absolute",
              right: 8,
              top: 8,
              color: (theme) => theme.palette.grey[500],
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 1, mt: 0 }}>
            <Tabs
              value={tabValue}
              onChange={handleTabChange}
              aria-label="class details tabs"
            >
              <Tab label="Thông tin lớp con" />
              <Tab label="Nhóm" />
            </Tabs>
          </Box>

          <TabPanel value={tabValue} index={0}>
            <div style={{ margin: "8px 0" }}>
              <Button
                variant="contained"
                onClick={() => setOpenNewDialog(true)}
                disabled={!classData?.parentClassId}
              >
                Thêm mới
              </Button>
              <Button
                sx={{ ml: 2 }}
                variant="contained"
                color="error"
                onClick={() => {
                  if (selectedIds.length > 0) {
                    setSelectedSubClass({ id: selectedIds[0] });
                    setDeleteDialogOpen(true);
                  }
                }}
                disabled={selectedIds.length === 0}
              >
                Xóa các lớp đã chọn ({selectedIds.length})
              </Button>
              {!classData?.parentClassId && (
                <span className="ml-2 text-sm text-gray-500">
                  (Chỉ có thể thêm lớp con cho lớp cha)
                </span>
              )}
            </div>
            <div style={{ height: 250, width: "100%" }}>
              {isLoadingSubClasses ? (
                <div className="flex justify-center items-center h-full">
                  <CircularProgress />
                </div>
              ) : (
                <DataGrid
                  rows={subClasses}
                  columns={classDetailsColumns}
                  getRowId={(row) =>
                    row.id ||
                    `row-${Math.random().toString(36).substring(2, 9)}`
                  }
                  checkboxSelection
                  onRowSelectionModelChange={handleSelectionChange}
                  rowSelectionModel={selectedIds}
                  initialState={{
                    pagination: {
                      paginationModel: { pageSize: 5 },
                    },
                  }}
                  pageSizeOptions={[5]}
                />
              )}
            </div>
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <div className="flex flex-row gap-1 mb-1 items-center justify-end">
              <TextField
                label="Tìm kiếm theo tên nhóm"
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                size="small"
                sx={{ width: "250px" }}
                placeholder="Nhập tên nhóm để tìm kiếm..."
              />
            </div>
            <div style={{ height: 350, width: "100%" }}>
              {loading ? (
                <div className="flex justify-center items-center h-full">
                  <CircularProgress />
                </div>
              ) : (
                <DataGrid
                  rows={filteredGroups}
                  columns={groupColumns}
                  disableRowSelectionOnClick
                  paginationModel={paginationModel}
                  onPaginationModelChange={setPaginationModel}
                  pageSizeOptions={[10, 25, 100]}
                />
              )}
            </div>
          </TabPanel>
        </DialogContent>
      </Dialog>

      <Dialog open={openNewDialog} onClose={() => setOpenNewDialog(false)}>
        <DialogTitle>Thêm lớp con</DialogTitle>
        <DialogContent>
          <div className="flex flex-col gap-2 p-4 items-end">
            <TextField
              label="Số lượng sinh viên"
              type="number"
              value={newSubClass.studentCount}
              onChange={(e) =>
                setNewSubClass({ ...newSubClass, studentCount: e.target.value })
              }
              fullWidth
            />
            <TextField
              select
              label="Loại lớp"
              value={newSubClass.classType}
              onChange={(e) =>
                setNewSubClass({ ...newSubClass, classType: e.target.value })
              }
              fullWidth
            >
              <MenuItem value="LT">LT</MenuItem>
              <MenuItem value="NT">NT</MenuItem>
              <MenuItem value="TH">TH</MenuItem>
              <MenuItem value="TN">TN</MenuItem>
              <MenuItem value="BT">BT</MenuItem>
            </TextField>
            <TextField
              label="SL lớp"
              type="number"
              value={newSubClass.classCount}
              onChange={(e) =>
                setNewSubClass({ ...newSubClass, classCount: e.target.value })
              }
              fullWidth
            />
            <Button
              onClick={handleAddSubClass}
              variant="contained"
              color="primary"
              disabled={isAddingSubClass}
              startIcon={
                isAddingSubClass ? (
                  <CircularProgress size={20} color="inherit" />
                ) : null
              }
            >
              {isAddingSubClass ? "Đang xử lý..." : "Xác nhận"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          Bạn có chắc chắn muốn xóa lớp {selectedSubClass?.classCode} không?
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Hủy</Button>
          <Button
            onClick={handleDeleteSubClass}
            color="error"
            disabled={isDeleting}
            startIcon={isDeleting ? <CircularProgress size={20} /> : null}
          >
            {isDeleting ? "Đang xóa..." : "Xóa"}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default ViewClassDetailDialog;
