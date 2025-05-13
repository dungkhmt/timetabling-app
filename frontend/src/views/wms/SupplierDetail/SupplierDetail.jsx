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
  Typography,
  useTheme
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';
import BusinessIcon from '@mui/icons-material/Business';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import InfoIcon from '@mui/icons-material/Info';
import FactoryIcon from '@mui/icons-material/Factory';
import { useWms2Data } from '../../../services/useWms2Data';
import { toast } from 'react-toastify';

const SupplierDetail = () => {
  const [supplier, setSupplier] = useState(null);
  const [loading, setLoading] = useState(true);
  const { id } = useParams();
  const history = useHistory();
  const { getSupplierById } = useWms2Data();
  const theme = useTheme();

  useEffect(() => {
    const fetchSupplierDetails = async () => {
      try {
        setLoading(true);
        const response = await getSupplierById(id);
        if (response && response.code === 200) {
          setSupplier(response.data);
        } else {
          toast.error('Không thể tải thông tin nhà cung cấp');
        }
      } catch (error) {
        console.error('Error fetching supplier details:', error);
        toast.error('Lỗi khi tải thông tin nhà cung cấp');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchSupplierDetails();
    }
  }, [id]);

  const handleBack = () => {
    history.push('/wms/inventory/suppliers');
  };

  const handleEdit = () => {
    history.push(`/wms/inventory/supplier/edit/${id}`);
  };

  const getStatusChip = (statusId) => {
    switch (statusId) {
      case 'ACTIVE':
        return <Chip label="Hoạt động" color="success" />;
      case 'INACTIVE':
        return <Chip label="Không hoạt động" color="error" />;
      default:
        return <Chip label={statusId || 'N/A'} />;
    }
  };

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
          Đang tải thông tin nhà cung cấp...
        </Typography>
      </Box>
    );
  }

  if (!supplier) {
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
            Không tìm thấy nhà cung cấp
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Không tìm thấy nhà cung cấp với mã: <strong>{id}</strong>
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
              Chi tiết nhà cung cấp
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Xem thông tin chi tiết của nhà cung cấp
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
      
      {/* Basic Info Card */}
      <Card elevation={2} sx={{ mb: 3, overflow: 'visible' }}>
        <CardHeader 
          title={
            <Box display="flex" alignItems="center">
              <BusinessIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
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
                  {supplier.name}
                </Typography>
                <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                  Mã nhà cung cấp: <strong>{supplier.id}</strong>
                </Typography>
                <Box mt={2} display="flex" alignItems="center">
                  <Typography variant="body1" mr={1}>
                    Trạng thái:
                  </Typography>
                  {getStatusChip(supplier.statusId)}
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
                <Box display="flex" alignItems="center" mb={2}>
                  <EmailIcon sx={{ mr: 1, color: theme.palette.info.main }} />
                  <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                    {supplier.email || 'Chưa có thông tin email'}
                  </Typography>
                </Box>
                
                <Box display="flex" alignItems="center">
                  <PhoneIcon sx={{ mr: 1, color: theme.palette.success.main }} />
                  <Typography variant="body1">
                    {supplier.phone || 'Chưa có thông tin số điện thoại'}
                  </Typography>
                </Box>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>
      
      {/* Address Card */}
      <Card elevation={2}>
        <CardHeader 
          title={
            <Box display="flex" alignItems="center">
              <LocationOnIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
              <Typography variant="h6">Thông tin địa chỉ</Typography>
            </Box>
          }
          sx={{ backgroundColor: theme.palette.background.default, borderBottom: `1px solid ${theme.palette.divider}` }}
        />
        <CardContent>
          <Box p={2} borderRadius={1} bgcolor={theme.palette.background.default}>
            <Typography variant="body1">
              {supplier.address || 'Chưa có thông tin địa chỉ'}
            </Typography>
          </Box>
        </CardContent>
      </Card>

      {/* Detailed Info Table */}
      <Card elevation={2} sx={{ mt: 3 }}>
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
                    <Typography fontWeight="medium">ID Nhà cung cấp</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography fontFamily="monospace">{supplier.id}</Typography>
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
                    <Typography fontWeight="medium">Tên nhà cung cấp</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography>{supplier.name}</Typography>
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
                    <Typography fontWeight="medium">Email</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography>{supplier.email || '—'}</Typography>
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
                    <Typography fontWeight="medium">Số điện thoại</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography>{supplier.phone || '—'}</Typography>
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
                    <Typography fontWeight="medium">Địa chỉ</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography>{supplier.address || '—'}</Typography>
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
                    <Typography fontWeight="medium">Trạng thái</Typography>
                  </TableCell>
                  <TableCell>
                    {getStatusChip(supplier.statusId)}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  );
};

export default SupplierDetail;