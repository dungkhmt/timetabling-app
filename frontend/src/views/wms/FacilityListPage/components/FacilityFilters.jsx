import React from "react";
import {
  Box,
  Button,
  Checkbox,
  Chip,
  FormControl,
  Grid,
  InputLabel,
  ListItemText,
  MenuItem,
  OutlinedInput,
  Paper,
  Select,
  TextField,
  useMediaQuery,
  useTheme
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import FilterListIcon from "@mui/icons-material/FilterList";

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
      width: 250,
    },
  },
};

const FacilityFilters = ({ filters, onFilterChange, onMultipleFilterChange, onApplyFilters }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const [expanded, setExpanded] = React.useState(!isMobile);
  
  const facilityStatuses = [
    { id: "ACTIVE", name: "Hoạt động" },
    { id: "INACTIVE", name: "Không hoạt động" }
  ];

  const toggleExpand = () => {
    setExpanded(!expanded);
  };

  const handleStatusChange = (event) => {
    const {
      target: { value },
    } = event;
    onMultipleFilterChange("statusId", typeof value === 'string' ? value.split(',') : value);
  };

  return (
    <Paper elevation={1} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Button 
          startIcon={<FilterListIcon />} 
          onClick={toggleExpand}
          variant="text"
        >
          {expanded ? "Thu gọn bộ lọc" : "Mở rộng bộ lọc"}
        </Button>
        
    
      </Box>

      {expanded && (
        <Grid container spacing={2}>
          <Grid item xs={12} md={7}>
            <TextField
              fullWidth
              label="Tìm kiếm theo tên/địa chỉ/số điện thoại"
              name="keyword"
              value={filters.keyword || ''}
              onChange={onFilterChange}
              variant="outlined"
              size="small"
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <FormControl fullWidth size="small">
              <InputLabel id="status-select-label">Trạng thái</InputLabel>
              <Select
                labelId="status-select-label"
                id="status-select"
                multiple
                value={filters.statusId || []}
                onChange={handleStatusChange}
                input={<OutlinedInput label="Trạng thái" />}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map((value) => {
                      const status = facilityStatuses.find(s => s.id === value);
                      return (
                        <Chip 
                          key={value} 
                          label={status ? status.name : value} 
                          size="small" 
                        />
                      );
                    })}
                  </Box>
                )}
                MenuProps={MenuProps}
              >
                {facilityStatuses.map((status) => (
                  <MenuItem key={status.id} value={status.id}>
                    <Checkbox checked={(filters.statusId || []).indexOf(status.id) > -1} />
                    <ListItemText primary={status.name} />
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2} textAlign={'right'}>
              <Button 
          variant="contained" 
          color="primary" 
          onClick={onApplyFilters}
          startIcon={<SearchIcon />}
        >
          Tìm kiếm
        </Button>
        </Grid>
        </Grid>
      )}
    </Paper>
  );
};

export default FacilityFilters;