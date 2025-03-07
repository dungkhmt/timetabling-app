import { Button, TextField, InputAdornment, Typography } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import { useState, useMemo } from 'react';
import SearchIcon from '@mui/icons-material/Search';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import AddIcon from '@mui/icons-material/Add';
import { useClassroomData } from 'services/useClassroomData';
import CreateNewClassroomScreen from './CreateNewClassroomScreen';
import { DeleteConfirmDialog } from './components/DeleteConfirmDialog';

export default function ClassroomListScreen() {
  const { classrooms, deleteClassroom, importExcel, isLoading, isImporting, refetchClassrooms } = useClassroomData();
  const [selectedClassroom, setSelectedClassroom] = useState(null);
  const [isDialogOpen, setDialogOpen] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deleteClassroomCode, setDeleteClassroomCode] = useState(null);
  const [openLoading, setOpenLoading] = useState(false);
  const [searchText, setSearchText] = useState('');

  // Filter classrooms based on search text
  const filteredClassrooms = useMemo(() => {
    if (!searchText.trim()) return classrooms;
    
    const searchLower = searchText.toLowerCase();
    return classrooms.filter(room => 
      (room.classroom && room.classroom.toLowerCase().includes(searchLower)) ||
      (room.building?.name && room.building.name.toLowerCase().includes(searchLower)) ||
      (room.description && room.description.toLowerCase().includes(searchLower)) ||
      (room.quantityMax && room.quantityMax.toString().includes(searchLower))
    );
  }, [classrooms, searchText]);

  const columns = [
    {
      headerName: "STT",
      field: "index",
      width: 120,
      renderCell: (params) => params.api.getRowIndex(params.row.id) + 1,
      renderCell: (params) => {
        return params.id;
      }
    },
    {
      headerName: "Lớp học",
      field: "classroom",
      width: 120
    },
    {
      headerName: "Tòa nhà",
      field: "building",
      width: 120,
      valueGetter: (params) => params.row.building?.name
    },
    {
      headerName: "Số lượng chỗ ngồi",
      field: "quantityMax",
      width: 170
    },
    {
      headerName: "Mô tả",
      field: "description",
      width: 180
    },
    {
      headerName: "Hành động",
      field: "actions",
      width: 200,
      renderCell: (params) => (
        <div>
          <Button
            variant="outlined"
            color="primary"
            onClick={() => handleUpdate(params.row)}
            style={{ marginRight: "8px" }}
          >
            Sửa
          </Button>
          <Button
            variant="outlined"
            color="secondary"
            onClick={() => handleDelete(params.row.classroom)}
          >
            Xóa
          </Button>
        </div>
      ),
    },
  ];

  const handleConfirmDelete = async () => {
    if (deleteClassroomCode) {
      await deleteClassroom(deleteClassroomCode.toString());  
      await refetchClassrooms();
    }
    setConfirmDeleteOpen(false);
    setDeleteClassroomCode(null);
  };

  const handleUpdate = (selectedRow) => {
    setSelectedClassroom(selectedRow);
    setDialogOpen(true);
  };

  const handleDelete = (classroom) => {
    setDeleteClassroomCode(classroom.toString());  
    setConfirmDeleteOpen(true);
  };

  const handleImportExcel = () => {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', '.xlsx, .xls');

    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (file) {
        try {
          setOpenLoading(true);
          const formData = new FormData();
          formData.append('file', file);
          await importExcel(formData);
          await refetchClassrooms();
        } catch (error) {
          console.error("Error uploading file", error);
        } finally {
          setOpenLoading(false);
        }
      }
    };

    input.click();
  };

  return (
    <div className='h-[500px] w-full'>
      <div
        className="flex justify-center items-center w-full mb-[16px]"
      >
        <Typography variant="h5">Danh sách kỳ học</Typography>
      </div>

      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          marginBottom: "16px",
          gap: "8px",
        }}
      >
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => setDialogOpen(true)}
        >
          Thêm mới
        </Button>

        <Button
          variant="contained"
          color="primary"
          startIcon={<CloudUploadIcon />}
          onClick={handleImportExcel}
        >
          Tải lên danh sách phòng
        </Button>
      </div>

      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          marginBottom: "16px",
        }}
      >
        <TextField
          placeholder="Tìm kiếm phòng học..."
          variant="outlined"
          size="small"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          sx={{
            width: "300px",
            "& .MuiInputBase-root": {
              height: "36px",
            },
          }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
            endAdornment: searchText ? (
              <InputAdornment position="end">
                <Button size="small" onClick={() => setSearchText("")}>
                  Xóa
                </Button>
              </InputAdornment>
            ) : null,
          }}
        />
      </div>

      <DataGrid
        loading={isLoading || isImporting || openLoading}
        getRowId={(row) => row.id}
        getRowSpacing={(params) => ({
          top: params.isFirstVisible ? 0 : 5,
          bottom: params.isLastVisible ? 0 : 5,
        })}
        rows={filteredClassrooms.map((row, index) => ({
          ...row,
          id: index + 1,
        }))}
        columns={columns}
        pageSize={10}
      />

      <CreateNewClassroomScreen
        open={isDialogOpen}
        handleClose={() => {
          setDialogOpen(false);
          setSelectedClassroom(null);
        }}
        selectedClassroom={selectedClassroom}
      />

      <DeleteConfirmDialog
        open={confirmDeleteOpen}
        onClose={() => setConfirmDeleteOpen(false)}
        onConfirm={handleConfirmDelete}
      />
    </div>
  );
}