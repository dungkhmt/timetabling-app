import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { useState } from "react";
import { request } from "api";
import { toast } from "react-toastify";
import { usePlanTableConfig } from "../hooks/usePlanTableConfig";
import ViewClassPlanDialog from "./ViewClassPlanDialog";

const ClassOpenPlanTable = ({
  isOpenDialog,
  semester,
  classes,
  setOpenDialog,
  setClasses,
  onSelectionChange,
  selectedRows  // Add this prop
}) => {
  const [selectedRow, setSelectedRow] = useState(null);

  const handleRowDoubleClick = (rowModel) => {
    if (rowModel.row) {
      console.log(rowModel.row);
      setSelectedRow(rowModel.row);
      setOpenDialog(true);
    }
  };

  // Function to refresh the main plan table
  const refreshMainPlanTable = () => {
    if (semester) {
      request(
        "get",
        `/plan-general-classes/?semester=${semester}`,
        (res) => {
          setClasses(res.data);
        },
        (err) => {
          toast.error("Có lỗi khi truy vấn kế hoạch học tập");
        },
        null,
        null,
        null
      );
    }
  };

  return (
    <div className="">
      <ViewClassPlanDialog
        row={selectedRow}
        planClassId={selectedRow?.id}
        closeDialog={() => setOpenDialog(false)}
        isOpen={isOpenDialog}
        semester={semester}
        refreshMainPlanTable={refreshMainPlanTable}
      />
      <DataGrid
        initialState={{
          sorting: {
            sortModel: [{ field: 'id', sort: 'asc' }],
          },
          filter: {
            filterModel: {
              items: [],
              quickFilterValues: [""],
            },
          },
        }}
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
        onRowDoubleClick = {(row) => handleRowDoubleClick(row)}
        rowSelection={true}
        columns={usePlanTableConfig(setClasses)}
        rows={classes}
        sx={{ height: 550 }}
        checkboxSelection
        onRowSelectionModelChange={onSelectionChange}
        rowSelectionModel={selectedRows}
      />
    </div>
  );
};

export default ClassOpenPlanTable;
