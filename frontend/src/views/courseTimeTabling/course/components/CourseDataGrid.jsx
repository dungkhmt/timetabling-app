import { Box, Button, Typography, TextField } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { useState } from "react";

export default function CourseDataGrid({ courses, isLoading, onEdit, onDelete, onCreate }) {
  const [searchQuery, setSearchQuery] = useState("");
  
  const filteredCourses = courses.filter(course =>
    course.id.toString().toLowerCase().includes(searchQuery.toLowerCase()) ||
    course.courseName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const columns = [
    { headerName: "Mã môn học", field: "id", width: 170 },
    { headerName: "Tên môn học", field: "courseName", width: 400 },
    { headerName: "Số tín chỉ", field: "credit", width: 170 },
    {
      headerName: "Hành động",
      field: "actions",
      width: 200,
      renderCell: (params) => (
        <div>
          <Button
            variant="outlined"
            color="primary"
            onClick={() => onEdit(params.row)}
            style={{ marginRight: "8px" }}
          >
            Sửa
          </Button>
          <Button
            variant="outlined"
            color="secondary"
            onClick={() => onDelete(params.row.id)}
          >
            Xóa
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <Box sx={{ marginBottom: 2 }}>
        <Box
          sx={{
            width: "100%",
            display: "flex",
            justifyContent: "center",
            marginBottom: 2,
          }}
        >
          <Typography variant="h5">Danh sách môn học</Typography>
        </Box>
        <Box
          sx={{
            display: "flex",
            justifyContent: "flex-end",
            marginRight: "8px",
          }}
        >
          <TextField
            label="Tìm kiếm (Mã môn học hoặc Tên môn học)"
            variant="outlined"
            size="small"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            sx={{ width: "300px", marginRight: 2, fontSize: "14px" }} 
          />
          <Button variant="outlined" color="primary" onClick={onCreate} sx={{ textTransform: "none", fontSize: "16px" }}>
            Thêm mới
          </Button>
        </Box>
      </Box>
      <DataGrid
        loading={isLoading}
        className="h-full"
        rows={filteredCourses}
        columns={columns}
        pageSize={10}
      />
    </>
  );
}