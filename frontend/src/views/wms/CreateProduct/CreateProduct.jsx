import React, {useState} from 'react';
import {Box, Button, CircularProgress, Paper, Typography, Divider} from '@mui/material';
import {useHistory} from 'react-router-dom';
import {toast} from 'react-toastify';

// Import các thành phần
import {ProductFormProvider} from './context/ProductFormContext';
import ProductInfoForm from './components/ProductInfoForm';
import PricingInfoForm from './components/PricingInfoForm';
import {useWms2Data} from "../../../services/useWms2Data";

const CreateProduct = () => {
  const navigate = useHistory();
  const [submitting, setSubmitting] = useState(false);
  const {createProduct} = useWms2Data();
  const [product, setProduct] = useState({
    id: '',
    name: '',
    weight: '',
    height: '',
    unit: '',
    costPrice: '',
    wholeSalePrice: '',
    retailPrice: '',
    productCategoryId: '',
    statusId: 'ACTIVE', // Trạng thái mặc định
    description: '' // Thêm trường mô tả
  });
  
  const [entities, setEntities] = useState({
    categories: [], // Danh sách danh mục rỗng, sẽ được tải khi người dùng click vào trường chọn
    statuses: [
      { id: 'ACTIVE', name: 'Mở bán' },
      { id: 'INACTIVE', name: 'Ngừng bán' }
    ]
  });

  const handleSubmit = async () => {
    // Kiểm tra các trường bắt buộc
    if (!product.name) {
      toast.warning("Vui lòng nhập tên sản phẩm");
      return;
    }
    
    if (!product.productCategoryId) {
      toast.warning("Vui lòng chọn danh mục sản phẩm");
      return;
    }
    
    if (!product.unit) {
      toast.warning("Vui lòng nhập đơn vị tính");
      return;
    }
    
    // Định dạng dữ liệu gửi đi
    const payload = {
      ...product,
      weight: product.weight ? parseFloat(product.weight) : null,
      height: product.height ? parseFloat(product.height) : null,
      costPrice: product.costPrice ? parseFloat(product.costPrice) : null,
      wholeSalePrice: product.wholeSalePrice ? parseFloat(product.wholeSalePrice) : null,
      retailPrice: product.retailPrice ? parseFloat(product.retailPrice) : null
    };
    
    setSubmitting(true);

    try {
      const response = await createProduct(payload);
      if(response && (response.code === 200 || response.code === 201)) {
        toast.success("Tạo sản phẩm thành công");
        navigate.push("/wms/admin/product");
      } else {
        toast.error("Tạo sản phẩm thất bại: " + (response.message || "Lỗi không xác định"));
      }
    } catch (error) {
        console.error("Lỗi khi tạo sản phẩm:", error);
        toast.error("Lỗi khi tạo sản phẩm: " + (error.message || "Lỗi không xác định"));
    } finally {
        setSubmitting(false);
    }
  };

  return (
    <ProductFormProvider value={{ product, setProduct, entities, setEntities }}>
      <Box p={3}>
        <Box mb={3} display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h5" fontWeight="bold">
            Tạo Sản Phẩm Mới
          </Typography>
        </Box>
        
        <Paper elevation={1} sx={{ p: 3, mb: 3 }}>
          <Box mb={4}>
            <Typography variant="h6" mb={2}>Thông Tin Sản Phẩm</Typography>
            <ProductInfoForm />
          </Box>
          
          <Box>
            <Typography variant="h6" mb={2}>Thông Tin Giá</Typography>
            <PricingInfoForm />
          </Box>
        </Paper>
        
        <Box display="flex" justifyContent="flex-end" mt={2}>
          <Button 
            variant="outlined" 
            color="secondary" 
            onClick={() => navigate.push("/wms/admin/product")}
            sx={{ mr: 2 }}
            disabled={submitting}
          >
            Hủy
          </Button>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSubmit}
            disabled={submitting}
            startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : null}
          >
            {submitting ? "Đang tạo..." : "Tạo Sản Phẩm"}
          </Button>
        </Box>
      </Box>
    </ProductFormProvider>
  );
};

export default CreateProduct;