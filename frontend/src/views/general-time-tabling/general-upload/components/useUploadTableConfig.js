import { Update } from "@mui/icons-material";
import { Autocomplete, Button, TextField } from "@mui/material";
import { memo } from "react";
import { request } from "api";
import { toast } from "react-toastify";

// Create memoized cell components to prevent unnecessary re-renders
export const TextFieldCell = memo(({ value, onChange, params }) => (
  <TextField
    variant="standard"
    value={value || ""}
    onChange={(e) => onChange(e, params)}
    sx={{ width: '100%' }}
  />
));

export const AutocompleteCell = memo(({ value, onChange, options, params }) => (
  <Autocomplete
    options={options}
    value={value || null}
    onChange={(e, option) => onChange(e, params, option)}
    renderInput={(option) => (
      <TextField variant="standard" {...option} sx={{ width: '100%' }} />
    )}
  />
));

// Export this as a utility function, not a hook
export const createColumns = (
  handleOnChangeCell, 
  handleOnCellSelect,
  onUpdateClick
) => {
  // Return the columns configuration directly
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
          onChange={handleOnChangeCell} 
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
          onChange={handleOnChangeCell} 
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
          onChange={handleOnChangeCell} 
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
          onChange={handleOnChangeCell} 
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
          onChange={handleOnChangeCell} 
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
          onChange={handleOnChangeCell} 
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
        <TextField
          variant="standard"
          value={params.row.course}
          onChange={(e) => handleOnChangeCell(e, params)}
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
          onClick={() => onUpdateClick(params.row)}
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
};
