import React, { useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Chip,
  CircularProgress,
  Divider,
  Grid,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Tooltip,
  Typography,
  useTheme,
  Tabs,
  Tab
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';
import CategoryIcon from '@mui/icons-material/Category';
import InventoryIcon from '@mui/icons-material/Inventory';
import InfoIcon from '@mui/icons-material/Info';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import { useWms2Data } from '../../../services/useWms2Data';
import { toast } from 'react-toastify';
import { formatCurrency } from "../common/utils/functions";

const ProductDetail = () => {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(0);
  const [inventory, setInventory] = useState([]);
  const [inventoryLoading, setInventoryLoading] = useState(false);

  const { id } = useParams();
  const history = useHistory();
  const { getProductById, getInventoryItemByProductId } = useWms2Data();
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
        console.error('Error fetching product details:', error);
        toast.error('Lỗi khi tải thông tin sản phẩm');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchProductDetails();
    }
  }, [id]);

  useEffect(() => {
    const fetchInventory = async () => {
      if (!product) return;
      setInventoryLoading(true);
      try {
        const res = await getInventoryItemByProductId(0, 50, product.id);
        if (res && res.code === 200) {
          setInventory(res.data.data || []);
        } else {
          setInventory([]);
        }
      } catch (e) {
        setInventory([]);
      } finally {
        setInventoryLoading(false);
      }
    };
    if (product && tab === 1) fetchInventory();
  }, [product, tab]);

  const handleBack = () => {
    history.push('/wms/admin/product');
  };

  const handleEdit = () => {
    history.push(`/wms/product/edit/${id}`);
  };

  const getStatusChip = (statusId) => {
    switch (statusId) {
      case 'ACTIVE':
        return <Chip label="Mở bán" color="success" />;
      case 'INACTIVE':
        return <Chip label="Ngừng bán" color="error" />;
      default:
        return <Chip label={statusId || 'N/A'} />;
    }
  };

  const getInventoryStatusChip = (statusId) => {
    switch (statusId) {
      case 'VALID':
        return <Chip label="Còn hạn" color="success" />;
      case 'EXPIRED':
        return <Chip label="Hết hạn" color="error" />;
      default:
        return <Chip label={statusId || 'N/A'} />;
    }
  }

  if (loading) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
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
          <Tab label="Thông tin sản phẩm" icon={<InventoryIcon />} iconPosition="start" />
          <Tab label="Tồn kho" icon={<WarehouseIcon />} iconPosition="start" />
        </Tabs>
      </Box>

      {/* Tab content */}
      {tab === 0 && (
        <>
          {/* Basic Info Card */}
          <Card elevation={2} sx={{ mb: 3, overflow: 'visible' }}>
            <CardHeader
              title={
                <Box display="flex" alignItems="center">
                  <InventoryIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
                  <Typography variant="h6">Thông tin cơ bản</Typography>
                </Box>
              }
              sx={{ backgroundColor: theme.palette.background.default, borderBottom: `1px solid ${theme.palette.divider}` }}
            />
            <CardContent>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Box sx={{
                    p: 2,
                    borderRadius: 1,
                    backgroundColor: theme.palette.background.default
                  }}>
                    <Typography variant="h6" gutterBottom>
                      {product.name}
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                      Mã sản phẩm: <strong>{product.id}</strong>
                    </Typography>
                    <Box mt={2} display="flex" alignItems="center">
                      <Typography variant="body1" mr={1}>
                        Trạng thái:
                      </Typography>
                      {getStatusChip(product.statusId)}
                    </Box>
                  </Box>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Box
                    sx={{
                      p: 2,
                      borderRadius: 1,
                      backgroundColor: theme.palette.background.default,
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center'
                    }}
                  >
                    <Box display="flex" alignItems="center" mb={1}>
                      <CategoryIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
                      <Typography variant="body2" color="text.secondary">
                        Danh mục sản phẩm
                      </Typography>
                    </Box>
                    <Typography variant="h6">
                      {product.productCategoryName || '—'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" mt={1}>
                      ID danh mục: {product.productCategoryId || '—'}
                    </Typography>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Detailed Info Table */}
          <Card elevation={2}>
            <CardHeader
              title={
                <Box display="flex" alignItems="center">
                  <InfoIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
                  <Typography variant="h6">Thông tin chi tiết</Typography>
                </Box>
              }
              sx={{ backgroundColor: theme.palette.background.default, borderBottom: `1px solid ${theme.palette.divider}` }}
            />
            <CardContent>
              <TableContainer component={Paper} elevation={0}>
                <Table>
                  <TableBody sx={{ '& tr:nth-of-type(even)': { backgroundColor: theme.palette.action.hover } }}>
                    <TableRow>
                      <TableCell
                        component="th"
                        width="30%"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Đơn vị tính</Typography>
                      </TableCell>
                      <TableCell>
                        {product.unit || '—'}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell
                        component="th"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Khối lượng</Typography>
                      </TableCell>
                      <TableCell>
                        {product.weight ? `${product.weight} kg` : '—'}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell
                        component="th"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Chiều cao</Typography>
                      </TableCell>
                      <TableCell>
                        {product.height ? `${product.height} cm` : '—'}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell
                        component="th"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Giá nhập</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          fontWeight="medium"
                          color="text.primary"
                        >
                          {formatCurrency(product.costPrice)}
                        </Typography>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell
                        component="th"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Giá bán lẻ</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          fontWeight="medium"
                          color={theme.palette.success.main}
                        >
                          {formatCurrency(product.retailPrice)}
                        </Typography>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell
                        component="th"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Giá bán buôn</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          fontWeight="medium"
                          color={theme.palette.info.main}
                        >
                          {formatCurrency(product.wholeSalePrice)}
                        </Typography>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell
                        component="th"
                        sx={{
                          borderLeft: `3px solid ${theme.palette.primary.light}`,
                          backgroundColor: theme.palette.background.default
                        }}
                      >
                        <Typography fontWeight="medium">Thuế VAT</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography
                          fontWeight="medium"
                          color={theme.palette.info.main}
                        >
                          {`${product.vatRate}%`}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </>
      )}

      {tab === 1 && (
        <Card elevation={2}>
          <CardHeader
            title={
              <Box display="flex" alignItems="center">
                <WarehouseIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
                <Typography variant="h6">Tồn kho theo sản phẩm</Typography>
              </Box>
            }
            sx={{ backgroundColor: theme.palette.background.default, borderBottom: `1px solid ${theme.palette.divider}` }}
          />
          <CardContent>
            {inventoryLoading ? (
              <Box display="flex" alignItems="center" justifyContent="center" minHeight={120}>
                <CircularProgress size={32} />
              </Box>
            ) : (
              <TableContainer component={Paper} elevation={0}>
                <Table>
                  <TableBody>
                    <TableRow>
                      <TableCell colSpan={7} sx={{ backgroundColor: theme.palette.action.hover }}>
                        <Typography fontWeight="medium">Tổng số dòng: {inventory.length}</Typography>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell><b>Kho</b></TableCell>
                      <TableCell><b>Số lượng</b></TableCell>
                      <TableCell><b>Lô</b></TableCell>
                      <TableCell><b>Ngày sản xuất</b></TableCell>
                      <TableCell><b>Hạn dùng</b></TableCell>
                      <TableCell><b>Trạng thái</b></TableCell>
                      <TableCell><b>Ngày nhập</b></TableCell>
                    </TableRow>
                    {inventory.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={7} align="center">Không có tồn kho</TableCell>
                      </TableRow>
                    ) : (
                      inventory.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell>{item.facilityName}</TableCell>
                          <TableCell>{item.quantity}</TableCell>
                          <TableCell>{item.lotId || '—'}</TableCell>
                          <TableCell>{item.manufacturingDate || '—'}</TableCell>
                          <TableCell>{item.expirationDate || '—'}</TableCell>
                          <TableCell>{getInventoryStatusChip(item.statusId)}</TableCell>
                          <TableCell>{item.receivedDate || '—'}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default ProductDetail;