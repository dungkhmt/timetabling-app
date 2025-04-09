import { useState, useEffect, useCallback, useMemo } from "react";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Box, Button } from "@mui/material";
import { TextFieldCell, AutocompleteCell, createColumns } from "./useUploadTableConfig";
import { useLoadingContext } from "../contexts/LoadingContext";
import ViewClassDetailDialog from "./ViewClassDetailDialog";
import UpdateConfirmDialog from "./UpdateConfirmDialog";
import { request } from "api";
import { toast } from "react-toastify";
import { Update } from "@mui/icons-material";

const GeneralUploadTable = ({ classes, dataLoading, setClasses, onSelectionChange, selectedIds, onRefreshNeeded, setIsEditing }) => {
  const { loading: uploadLoading, setLoading } = useLoadingContext();
  const [openDetailDialog, setOpenDetailDialog] = useState(false);
  const [selectedClass, setSelectedClass] = useState(null);
  const [updateDialogOpen, setUpdateDialogOpen] = useState(false);
  const [classToUpdate, setClassToUpdate] = useState(null);
  
  const [localClasses, setLocalClasses] = useState([]);
  
  useEffect(() => {
    console.log("Classes updated:", classes?.length);
    setLocalClasses(classes || []);
  }, [classes]);

  useEffect(() => {
    if (setIsEditing) {
      return () => setIsEditing(false);
    }
  }, [setIsEditing]);

  const handleSaveClass = (classData) => {
    setLoading(true);
    request("post", "/general-classes/update-class", 
      (res) => {
        console.log(res);
        toast.success("Cập nhật lớp học thành công!");
        setLoading(false);
        
        setClasses(localClasses);
        if (onRefreshNeeded) {
          onRefreshNeeded();
        }
      }, 
      (err) => {
        console.log(err);
        toast.error("Có lỗi khi cập nhật lớp học!");
        setLoading(false);
      }, 
      {
        generalClass: {...classData}
      }
    );
  };
  
  const handleUpdateClick = useCallback((rowData) => {
    setClassToUpdate(rowData);
    setUpdateDialogOpen(true);
  }, []);

  const handleUpdateConfirm = () => {
    if (classToUpdate) {
      handleSaveClass(classToUpdate);
    }
    setUpdateDialogOpen(false);
    setClassToUpdate(null);
  };
  
  const handleOnCellChange = useCallback((e, params) => {
    if (setIsEditing) setIsEditing(true);
    
    const value = e.target.value !== "" ? e.target.value : null;
    setLocalClasses(prevClasses => 
      prevClasses.map(prevClass => 
        prevClass.id === params.id 
          ? { ...prevClass, [params.field]: value }
          : prevClass
      )
    );
  }, [setIsEditing]);

  const handleOnCellSelect = useCallback((e, params, option) => {
    if (setIsEditing) setIsEditing(true);
    
    setLocalClasses(prevClasses => 
      prevClasses.map(prevClass => 
        prevClass.id === params.id 
          ? { ...prevClass, [params.field]: option }
          : prevClass
      )
    );
  }, [setIsEditing]);

  const handleRowDoubleClick = (params) => {
    setSelectedClass(params.row);
    setOpenDetailDialog(true);
  };

  const columns = useMemo(() => {
    return [
      {
        headerName: "Mã lớp",
        field: "classCode",
        width: 100,
      },
      {
        headerName: "Lớp liên quan tới",
        field: "listGroupName",
        width: 200,
      },
      {
        headerName: "Tuần học",
        field: "learningWeeks",
        width: 120,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.learningWeeks}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "Mã học phần",
        field: "moduleCode",
        width: 80,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.moduleCode}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "Tên học phần",
        field: "moduleName",
        width: 200,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.moduleName}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "SL MAX",
        field: "quantityMax",
        width: 100,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.quantityMax}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "Loại lớp",
        field: "classType",
        width: 100,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.classType}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "Thời lượng",
        field: "mass",
        width: 100,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.mass}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "Kíp",
        field: "crew",
        width: 80,
        renderCell: (params) => (
          <AutocompleteCell
            value={params.row.crew}
            onChange={handleOnCellSelect}
            options={["S", "C"]}
            params={params}
          />
        ),
      },
      {
        headerName: "Đợt",
        field: "openBatch",
        width: 80,
        renderCell: (params) => (
          <AutocompleteCell
            value={params.row.openBatch}
            onChange={handleOnCellSelect}
            options={["Chẵn", "Lẻ", "A", "B", "AB"]}
            params={params}
          />
        ),
      },
      {
        headerName: "Khóa",
        field: "course",
        width: 40,
        renderCell: (params) => (
          <TextFieldCell
            value={params.row.course}
            onChange={handleOnCellChange}
            params={params}
          />
        ),
      },
      {
        headerName: "Thao tác",
        field: "actions",
        width: 120,
        align: "center",
        renderCell: (params) => (
          <Button 
            onClick={() => handleUpdateClick(params.row)}
            variant="outlined"
            color="primary"
            size="small"
          >
            <Update />
            Cập nhật
          </Button>
        ),
      },
    ];
  }, [handleOnCellChange, handleOnCellSelect, handleUpdateClick]);

  return (
    <Box style={{ height: 600, width: "100%" }}>
      <DataGrid
        loading={uploadLoading || dataLoading}
        className="text-xs"
        columns={columns}
        rows={localClasses}
        pageSize={10}
        initialState={{
          sorting: {
            sortModel: [{ field: 'classCode', sort: 'asc' }],
          },
          filter: {
            filterModel: {
              items: [],
              quickFilterValues: [""],
            },
          },
        }}
        // Force the grid to re-render when data changes by adding a key
        key={`data-grid-${dataLoading ? 'loading' : localClasses.length}`}
        slots={{ toolbar: GridToolbar }}
        slotProps={{
          toolbar: { 
            printOptions: { disableToolbarButton: true },
            csvOptions: { disableToolbarButton: true },
            showQuickFilter: true,
          },
        }}
        disableColumnSelector
        disableDensitySelector
        checkboxSelection
        getRowId={(row) => row.id}
        rowSelectionModel={selectedIds}
        onRowSelectionModelChange={onSelectionChange}
        onRowDoubleClick={handleRowDoubleClick}
        disableColumnFilter
        keepNonExistentRowsSelected
      />
      <ViewClassDetailDialog
        isOpen={openDetailDialog}
        classData={selectedClass}
        closeDialog={() => setOpenDetailDialog(false)}
        onRefreshParent={onRefreshNeeded}
      />
      <UpdateConfirmDialog
        open={updateDialogOpen}
        onClose={() => setUpdateDialogOpen(false)}
        onConfirm={handleUpdateConfirm}
        classData={classToUpdate}
      />
    </Box>
  );
};

export default GeneralUploadTable;
