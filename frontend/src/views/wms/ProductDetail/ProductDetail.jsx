import React, { useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import {
  Box,
  Button,
  Paper,
  IconButton,
  Typography,
  useTheme,
  Tabs,
  Tab,
  CircularProgress
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';
import InfoIcon from '@mui/icons-material/Info';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import { useWms2Data } from '../../../services/useWms2Data';
import { toast } from 'react-toastify';

import ProductBasicInfo from './components/ProductBasicInfo';
import ProductDetailTable from './components/ProductDetailTable';
import InventoryTable from './components/InventoryTable';
import PriceHistoryTable from './components/PriceHistoryTable';
import PriceDialog from './components/PriceDialog';

const ProductDetail = () => {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(0);

  const [inventory, setInventory] = useState([]);
  const [inventoryLoading, setInventoryLoading] = useState(false);

  const [priceHistory, setPriceHistory] = useState([]);
  const [priceLoading, setPriceLoading] = useState(false);

  const [openPriceDialog, setOpenPriceDialog] = useState(false);
  const [submitPriceLoading, setSubmitPriceLoading] = useState(false);

  const { id } = useParams();
  const history = useHistory();
  const { getProductById, getInventoryItemByProductId, getProductPrice, createProductPrice } = useWms2Data();
  const theme = useTheme();

  useEffect(() => {
    const fetchProductDetails = async () => {
      try {
        setLoading(true);
        const response = await getProductById(id);
        if (response && response.code === 200) {
          setProduct(response.data);
        } else {
          toast.error('Không thể tải thông tin sản phẩm');
        }
      } catch (error) {
        toast.error('Lỗi khi tải thông tin sản phẩm');
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchProductDetails();
  }, [id]);

  useEffect(() => {
    if (!product) return;
    if (tab === 1) {
      setInventoryLoading(true);
      getInventoryItemByProductId(0, 50, product.id)
        .then(res => {
          if (res && res.code === 200) setInventory(res.data.data || []);
          else setInventory([]);
        })
        .catch(() => setInventory([]))
        .finally(() => setInventoryLoading(false));
    }
    if (tab === 2) {
      setPriceLoading(true);
      getProductPrice(product.id)
        .then(res => {
          if (res && res.code === 200) setPriceHistory(res.data || []);
          else setPriceHistory([]);
        })
        .catch(() => setPriceHistory([]))
        .finally(() => setPriceLoading(false));
    }
  }, [product, tab]);

  const handleBack = () => {
    history.push('/wms/admin/product');
  };

  const handleEdit = () => {
    history.push(`/wms/product/edit/${id}`);
  };

  const handleOpenPriceDialog = () => setOpenPriceDialog(true);
  const handleClosePriceDialog = () => setOpenPriceDialog(false);

  const handleSubmitPrice = async (data) => {
    setSubmitPriceLoading(true);
    try {
      const req = {
        productId: product.id,
        price: data.price,
        description: data.description,
        startDate: data.startDate,
        endDate: data.endDate,
      };
      const res = await createProductPrice(req);
      if (res && res.code === 201) {
        toast.success('Thiết lập giá bán mới thành công');
        setOpenPriceDialog(false);
        setTab(2); // Chuyển sang tab lịch sử giá bán
        // Reload price history
        setPriceLoading(true);
        const priceRes = await getProductPrice(product.id);
        setPriceHistory(priceRes && priceRes.code === 200 ? priceRes.data || [] : []);
        setPriceLoading(false);
      } else {
        toast.error(res?.message || 'Thiết lập giá bán thất bại');
      }
    } catch (e) {
      toast.error('Thiết lập giá bán thất bại');
    } finally {
      setSubmitPriceLoading(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" height="80vh">
        <CircularProgress size={40} />
        <Typography variant="body1" sx={{ mt: 2 }}>
          Đang tải thông tin sản phẩm...
        </Typography>
      </Box>
    );
  }

  if (!product) {
    return (
      <Box p={3}>
        <Paper
          elevation={3}
          sx={{
            p: 4,
            textAlign: 'center',
            backgroundColor: theme.palette.background.default,
            border: `1px solid ${theme.palette.divider}`
          }}
        >
          <InfoIcon sx={{ fontSize: 60, color: theme.palette.warning.main, mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            Không tìm thấy sản phẩm
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Không tìm thấy sản phẩm với mã: <strong>{id}</strong>
          </Typography>
          <Button
            variant="contained"
            startIcon={<ArrowBackIcon />}
            onClick={handleBack}
            sx={{ mt: 1 }}
          >
            Quay lại danh sách
          </Button>
        </Paper>
      </Box>
    );
  }

  return (
    <Box p={3} sx={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Header */}
      <Paper
        elevation={2}
        sx={{
          p: 2,
          mb: 3,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderLeft: `4px solid ${theme.palette.primary.main}`
        }}
      >
        <Box display="flex" alignItems="center">
          <IconButton
            onClick={handleBack}
            sx={{ mr: 1 }}
            color="primary"
          >
            <ArrowBackIcon />
          </IconButton>
          <Box>
            <Typography variant="h5" fontWeight="bold">
              Chi tiết sản phẩm
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Xem thông tin chi tiết của sản phẩm và các thông số kỹ thuật
            </Typography>
          </Box>
        </Box>
        <Button
          variant="contained"
          color="primary"
          startIcon={<EditIcon />}
          onClick={handleEdit}
        >
          Chỉnh sửa
        </Button>
      </Paper>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)}>
          <Tab label="Thông tin sản phẩm" />
          <Tab label="Tồn kho" />
          <Tab label="Điều chỉnh giá bán" icon={<MonetizationOnIcon />} iconPosition="start" />
        </Tabs>
      </Box>

      {/* Tab content */}
      {tab === 0 && (
        <>
          <ProductBasicInfo product={product} />
          <ProductDetailTable product={product} />
        </>
      )}

      {tab === 1 && (
        <InventoryTable inventory={inventory} loading={inventoryLoading} />
      )}

      {tab === 2 && (
        <Box>
          <Box display="flex" justifyContent="flex-end" mb={2}>
            <Button
              variant="contained"
              color="success"
              startIcon={<MonetizationOnIcon />}
              onClick={handleOpenPriceDialog}
            >
              Thiết lập giá bán mới
            </Button>
          </Box>
          <PriceHistoryTable priceHistory={priceHistory} loading={priceLoading} />
          <PriceDialog
            open={openPriceDialog}
            onClose={handleClosePriceDialog}
            onSubmit={handleSubmitPrice}
            loading={submitPriceLoading}
          />
        </Box>
      )}
    </Box>
  );
};

export default ProductDetail;