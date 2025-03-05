import React from 'react';
import { Autocomplete, TextField, CircularProgress } from "@mui/material";

const EntityAutocomplete = ({
  options,
  value,
  onChange,
  onOpen,
  onScroll,
  loading,
  placeholder,
  getOptionLabel,
  noOptionsText = "Không có dữ liệu",
}) => {
  return (
    <Autocomplete
      options={options}
      getOptionLabel={getOptionLabel}
      value={value}
      onChange={onChange}
      onOpen={onOpen}
      ListboxProps={{
        onScroll: onScroll,
        style: { maxHeight: '200px', overflow: 'auto' }
      }}
      noOptionsText={noOptionsText}
      renderInput={(params) => (
        <TextField 
          {...params} 
          size="small" 
          placeholder={placeholder}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? <CircularProgress color="inherit" size={20} /> : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
    />
  );
};

export default EntityAutocomplete;