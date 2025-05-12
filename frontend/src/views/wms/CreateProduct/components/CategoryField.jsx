import React, { useState } from 'react';
import {
  Grid,
  Typography,
  FormControl,
  Select,
  MenuItem,
  CircularProgress
} from '@mui/material';
import { useProductForm } from '../context/ProductFormContext';
import { toast } from 'react-toastify';
import {useWms2Data} from "../../../../services/useWms2Data";

const CategoryField = () => {
  const { product, setProduct, entities, setEntities } = useProductForm();
  const [loading, setLoading] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const {getProductCategories} = useWms2Data();
  const handleOpenMenu = () => {
    setMenuOpen(true);
    
    // Nếu danh sách danh mục chưa được tải, thực hiện tải 
    if (entities.categories.length === 0 && !loading) {
      fetchCategories();
    }
  };

  // Hàm tải danh mục sản phẩm
  const fetchCategories = async () => {
    setLoading(true);
    try {
      const response = await getProductCategories();
      if (response && response.code === 200) {
        setEntities(prev => ({
          ...prev,
          categories: response.data || []
        }));
      } else {
        toast.error("Không thể tải danh mục sản phẩm");
      }
    } catch (error) {
      console.error("Lỗi khi tải danh mục sản phẩm:", error);
      toast.error("Lỗi khi tải danh mục sản phẩm");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setProduct(prev => ({ ...prev, productCategoryId: e.target.value }));
  };

  return (
    <>
      <Grid item xs={4}>
        <Typography variant="body1" sx={{ pt: 1 }}>
          Danh mục sản phẩm:
        </Typography>
      </Grid>
      <Grid item xs={8}>
        <FormControl fullWidth size="small">
          <Select
            value={product.productCategoryId}
            onChange={handleChange}
            onOpen={handleOpenMenu}
            onClose={() => setMenuOpen(false)}
            displayEmpty
            renderValue={(selected) => {
              if (!selected) {
                return <em>Chọn danh mục sản phẩm</em>;
              }

              const selectedCategory = entities.categories.find(
                category => category.id === selected
              );
              return selectedCategory ? selectedCategory.name : <em>Chọn danh mục sản phẩm</em>;
            }}
          >
            {loading ? (
              <MenuItem disabled>
                <CircularProgress size={20} sx={{ mr: 2 }} />
                Đang tải danh mục...
              </MenuItem>
            ) : (
              <>
                <MenuItem value="" disabled>
                  <em>Chọn danh mục sản phẩm</em>
                </MenuItem>
                {entities.categories.map(category => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </>
            )}
          </Select>
        </FormControl>
      </Grid>
    </>
  );
};

export default CategoryField;