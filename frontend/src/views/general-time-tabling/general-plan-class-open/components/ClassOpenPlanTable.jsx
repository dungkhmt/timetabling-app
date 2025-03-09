import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { useState, useEffect } from "react";
import { usePlanTableConfig } from "../hooks/usePlanTableConfig";
import ViewClassPlanDialog from "./ViewClassPlanDialog";
import { Button } from "@mui/material";
import { request } from "api";
import { toast } from "react-toastify";
import ReactDOM from "react-dom";

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

  return (
    <div className="">
      <ViewClassPlanDialog
        row={selectedRow}
        planClassId={selectedRow?.id}
        closeDialog={() => setOpenDialog(false)}
        isOpen={isOpenDialog}
        semester={semester}
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
