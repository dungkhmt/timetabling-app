import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  TextField,
  FormControl,
  Select,
  MenuItem,
  Divider,
  Typography,
  CircularProgress
} from '@mui/material';
import { useProductForm } from '../context/ProductFormContext';
import { useWms2Data } from "../../../../services/useWms2Data";
import { toast } from 'react-toastify';

const ProductInfoForm = () => {
  const { product, setProduct, entities, setEntities } = useProductForm();
  const [loading, setLoading] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const { getProductCategories } = useWms2Data();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProduct(prev => ({ ...prev, [name]: value }));
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

  const handleOpenMenu = () => {
    setMenuOpen(true);
    // Nếu danh sách danh mục chưa được tải, thực hiện tải
    if (entities.categories.length === 0 && !loading) {
      fetchCategories();
    }
  };

  // Gọi fetchCategories khi component được tạo để đảm bảo danh mục được tải
  useEffect(() => {
    if (entities.categories.length === 0 && !loading) {
      fetchCategories();
    }
  }, []);

  const handleStatusChange = (e) => {
    setProduct(prev => ({ ...prev, statusId: e.target.value }));
  };


  // Log product và entities khi chúng thay đổi
  useEffect(() => {
    console.log("Product updated:", product);
    console.log("Entities updated:", entities);
  }, [product, entities]);

  return (
    <Box sx={{ mb: 3 }}>
      <Divider sx={{ mb: 2 }} />
      <Grid container spacing={4}>
        {/* Hàng 1: Tên sản phẩm và ID */}
        <Grid item xs={12} md={6} sx={{ pr: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Tên sản phẩm:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="name"
                value={product.name}
                onChange={handleInputChange}
                required
                placeholder="Nhập tên sản phẩm"
              />
            </Grid>
          </Grid>
        </Grid>

        <Grid item xs={12} md={6} sx={{ pl: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Mã sản phẩm:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="id"
                value={product.id}
                onChange={handleInputChange}
                placeholder="Để trống sẽ tự sinh mã"
              />
            </Grid>
          </Grid>
        </Grid>

        {/* Hàng 2: Danh mục sản phẩm và Đơn vị tính */}
        <Grid item xs={12} md={6} sx={{ pr: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Danh mục:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <FormControl fullWidth size="small">
                <Select
                  value={product.productCategoryId || ''}
                  onChange={(e) => {
                    console.log("Direct onChange event value:", e.target.value);
                    setProduct(prev => ({ 
                      ...prev, 
                      productCategoryId: e.target.value 
                    }));
                  }}
                  onOpen={handleOpenMenu}
                  onClose={() => setMenuOpen(false)}
                  displayEmpty
                >
                  <MenuItem value="" disabled>
                    <em>Chọn danh mục sản phẩm</em>
                  </MenuItem>
                  {loading ? (
                    <MenuItem disabled>
                      <CircularProgress size={20} sx={{ mr: 2 }} />
                      Đang tải danh mục...
                    </MenuItem>
                  ) : (
                    entities.categories.map(category => (
                      <MenuItem key={category.id} value={category.id}>
                        {category.name}
                      </MenuItem>
                    ))
                  )}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </Grid>

        <Grid item xs={12} md={6} sx={{ pl: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Đơn vị tính:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="unit"
                value={product.unit}
                onChange={handleInputChange}
                required
                placeholder="VD: kg, cái, thùng"
              />
            </Grid>
          </Grid>
        </Grid>

        {/* Hàng 3: Khối lượng và Chiều cao */}
        <Grid item xs={12} md={6} sx={{ pr: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Khối lượng:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="weight"
                type="number"
                value={product.weight}
                onChange={handleInputChange}
                InputProps={{ inputProps: { min: 0, step: 0.01 } }}
                placeholder="VD: 0.5"
              />
            </Grid>
          </Grid>
        </Grid>

        <Grid item xs={12} md={6} sx={{ pl: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Chiều cao:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                name="height"
                type="number"
                value={product.height}
                onChange={handleInputChange}
                InputProps={{ inputProps: { min: 0, step: 0.01 } }}
                placeholder="VD: 10"
              />
            </Grid>
          </Grid>
        </Grid>

        {/* Hàng 4: Trạng thái */}
        <Grid item xs={12} md={6} sx={{ pr: { md: 2 } }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Typography variant="body1" sx={{ pt: 1 }}>
                Trạng thái:
              </Typography>
            </Grid>
            <Grid item xs={8}>
              <FormControl fullWidth size="small">
                <Select
                  value={product.statusId}
                  onChange={handleStatusChange}
                  displayEmpty
                >
                  {entities.statuses.map(status => (
                    <MenuItem key={status.id} value={status.id}>
                      {status.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ProductInfoForm;