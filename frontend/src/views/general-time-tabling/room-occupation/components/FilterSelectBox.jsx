import { Autocomplete, TextField } from "@mui/material";
import { request } from "api";
import { useEffect, useState } from "react";
import { toast } from "react-toastify";

const FilterSelectBox = ({ selectedWeek, setSelectedWeek, selectedSemester, sx={} }) => {
  const [weeks, setWeeks] = useState([]);

  useEffect(() => {
    if (!selectedSemester) {
      setWeeks([]);
      setSelectedWeek(null);
      return;
    }
    // Clear week when semester changes
    setSelectedWeek(null);
    request(
      "get",
      `/academic-weeks/?semester=${selectedSemester?.semester}`,
      (res) => {
        setWeeks(res.data);
        toast.success("Truy vấn tuần học thành công với " + res.data?.length);
      },
      (error) => {
        toast.error("Có lỗi khi truy vấn tuần học!");
        console.log(error);
      }
    );
  }, [selectedSemester]);

  return (
    <div>
      <Autocomplete
        disabled={!selectedSemester}
        loadingText="Loading..."
        getOptionLabel={(option) => "Tuần " + option?.weekIndex?.toString()}
        onChange={(e, week) => setSelectedWeek(week)}
        value={selectedWeek}
        options={weeks}
        size="small"
        sx={{ width: 200, ...sx }}
        renderInput={(params) => (
          <TextField {...params} label="Lọc theo tuần" />
        )}
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
    </div>
  );
};

export default FilterSelectBox;
