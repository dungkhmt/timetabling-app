import { Autocomplete, TextField } from "@mui/material";
import { useGroupData } from "services/useGroupData";

const GeneralGroupAutoComplete = ({
  selectedGroup,
  setSelectedGroup,
  width = 200,
  sx = {},
}) => {
  const { isLoading, error, allGroups } = useGroupData();

  return (
    <Autocomplete
      disablePortal
      loadingText="Loading..."
      getOptionLabel={(option) => option && option.groupName}
      onChange={(e, group) => {
        setSelectedGroup(group);
      }}
      value={selectedGroup}
      options={allGroups}
      size="small"
      sx={{ 
        width: width,
        ...sx  
      }}
      renderInput={(params) => <TextField {...params} label="Chọn nhóm" />}
      PopperProps={{
        popperOptions: {
          modifiers: [
            {
              name: "preventOverflow",
              enabled: true,
              options: {
                altAxis: true,
              },
            },
          ],
        },
      }}
    />
  );
};

export default GeneralGroupAutoComplete;
