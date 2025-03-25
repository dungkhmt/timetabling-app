import React, { useState, useEffect } from "react";
import { Autocomplete, TextField, CircularProgress } from "@mui/material";
import { generalScheduleRepository } from "repositories/generalScheduleRepository";

const GeneralClusterAutoComplete = ({
  selectedCluster,
  setSelectedCluster,
  selectedSemester,
  sx,
  disabled,
}) => {
  const [clusters, setClusters] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!selectedSemester?.semester) {
      setClusters([]);
      return;
    }

    const fetchClusters = async () => {
      setLoading(true);
      try {
        const data = await generalScheduleRepository.getClustersBySemester(
          selectedSemester.semester
        );
        setClusters(data || []);
      } catch (error) {
        console.error("Error fetching clusters:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchClusters();
  }, [selectedSemester]);

  return (
    <Autocomplete
      value={selectedCluster}
      onChange={(event, newValue) => {
        setSelectedCluster(newValue);
      }}
      disabled={disabled || loading || clusters.length === 0}
      options={clusters}
      getOptionLabel={(option) => option?.name || ""}
      isOptionEqualToValue={(option, value) => option?.id === value?.id}
      renderInput={(params) => (
        <TextField
          {...params}
          label="Chọn cụm"
          size="small"
          sx={sx}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? (
                  <CircularProgress color="inherit" size={20} />
                ) : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
    />
  );
};

export default GeneralClusterAutoComplete;
