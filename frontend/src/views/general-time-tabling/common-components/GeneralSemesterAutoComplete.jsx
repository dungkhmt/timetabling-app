import { Autocomplete, TextField } from "@mui/material";
import { useSemesters } from "../hooks/useSemester";

const GeneralSemesterAutoComplete = ({
  selectedSemester,
  setSelectedSemester,
  sx = {},
}) => {
  const {
    semesters,
  } = useSemesters();
  return (
    <div>
      <Autocomplete
        disablePortal
        loadingText="Loading..."
        getOptionLabel={(option) => option && option.semester}
        onChange={(e, semester) => {
          setSelectedSemester(semester);
        }}
        value={selectedSemester}
        options={semesters}
        size="small"
        sx={{ 
          width: 130,
          ...sx
        }}
        renderInput={(params) => <TextField {...params} label="Chọn kỳ" />}
      />
    </div>
  );
};

export default GeneralSemesterAutoComplete;
