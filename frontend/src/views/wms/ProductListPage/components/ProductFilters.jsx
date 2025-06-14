import React, { useState, useEffect } from "react";
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
import { useWms2Data } from "../../../../services/useWms2Data";

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

const ProductFilters = ({ filters, onFilterChange, onMultipleFilterChange, onApplyFilters }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  const [expanded, setExpanded] = useState(!isMobile);
  const [categories, setCategories] = useState([]);
  const [productStatuses] = useState([
    { id: "ACTIVE", name: "Hoạt động" },
    { id: "INACTIVE", name: "Không hoạt động" },
  ]);

  const { getProductCategories } = useWms2Data();

  useEffect(() => {
    // Fetch product categories
    const fetchCategories = async () => {
      try {
        const response = await getProductCategories();
        if (response && response.code === 200) {
          setCategories(response.data || []);
        }
      } catch (error) {
        console.error("Error fetching product categories:", error);
      }
    };

    fetchCategories();
  }, []);

  const toggleExpand = () => {
    setExpanded(!expanded);
  };

  const handleStatusChange = (event) => {
    const {
      target: { value },
    } = event;
    onMultipleFilterChange("statusId", typeof value === 'string' ? value.split(',') : value);
  };

  const handleCategoryChange = (event) => {
    const {
      target: { value },
    } = event;
    onMultipleFilterChange("categoryId", typeof value === 'string' ? value.split(',') : value);
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
              label="Tìm kiếm theo tên/mã sản phẩm"
              name="keyword"
              value={filters.keyword}
              onChange={onFilterChange}
              variant="outlined"
              size="small"
            />
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel id="category-select-label">Danh mục</InputLabel>
              <Select
                labelId="category-select-label"
                id="category-select"
                multiple
                value={filters.categoryId}
                onChange={handleCategoryChange}
                input={<OutlinedInput label="Danh mục" />}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map((value) => {
                      const category = categories.find(c => c.id === value);
                      return (
                        <Chip 
                          key={value} 
                          label={category ? category.name : value} 
                          size="small" 
                        />
                      );
                    })}
                  </Box>
                )}
                MenuProps={MenuProps}
              >
                {categories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    <Checkbox checked={filters.categoryId.indexOf(category.id) > -1} />
                    <ListItemText primary={category.name} />
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel id="status-select-label">Trạng thái</InputLabel>
              <Select
                labelId="status-select-label"
                id="status-select"
                multiple
                value={filters.statusId}
                onChange={handleStatusChange}
                input={<OutlinedInput label="Trạng thái" />}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map((value) => {
                      const status = productStatuses.find(s => s.id === value);
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
                {productStatuses.map((status) => (
                  <MenuItem key={status.id} value={status.id}>
                    <Checkbox checked={filters.statusId.indexOf(status.id) > -1} />
                    <ListItemText primary={status.name} />
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

            <Grid item xs={12} md={1}>
          <Button
              variant="contained"
              color="primary"
              fullWidth
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

export default ProductFilters;