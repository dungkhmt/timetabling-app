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
import PersonIcon from '@mui/icons-material/Person';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import InfoIcon from '@mui/icons-material/Info';
import { useWms2Data } from '../../../services/useWms2Data';
import { toast } from 'react-toastify';

const CustomerDetail = () => {
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const { id } = useParams();
  const history = useHistory();
  const { getCustomerById } = useWms2Data();
  const theme = useTheme();

  useEffect(() => {
    const fetchCustomerDetails = async () => {
      try {
        setLoading(true);
        const response = await getCustomerById(id);
        if (response && response.code === 200) {
          setCustomer(response.data);
        } else {
          toast.error('Không thể tải thông tin khách hàng');
        }
      } catch (error) {
        console.error('Error fetching customer details:', error);
        toast.error('Lỗi khi tải thông tin khách hàng');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchCustomerDetails();
    }
  }, [id]);

  const handleBack = () => {
    history.push('/wms/sales/customers');
  };

  const handleEdit = () => {
    history.push(`/wms/sales/customers/edit/${id}`);
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
          Đang tải thông tin khách hàng...
        </Typography>
      </Box>
    );
  }

  if (!customer) {
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
            Không tìm thấy khách hàng
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Không tìm thấy khách hàng với mã: <strong>{id}</strong>
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
              Chi tiết khách hàng
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Xem thông tin chi tiết của khách hàng
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
              <PersonIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
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
                  {customer.name}
                </Typography>
                <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                  Mã khách hàng: <strong>{customer.id}</strong>
                </Typography>
                <Box mt={2} display="flex" alignItems="center">
                  <Typography variant="body1" mr={1}>
                    Trạng thái:
                  </Typography>
                  {getStatusChip(customer.statusId)}
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
                    {customer.email || 'Chưa có thông tin email'}
                  </Typography>
                </Box>
                
                <Box display="flex" alignItems="center">
                  <PhoneIcon sx={{ mr: 1, color: theme.palette.success.main }} />
                  <Typography variant="body1">
                    {customer.phone || 'Chưa có thông tin số điện thoại'}
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
              {customer.address || 'Chưa có thông tin địa chỉ'}
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
                    <Typography fontWeight="medium">ID Khách hàng</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography fontFamily="monospace">{customer.id}</Typography>
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
                    <Typography fontWeight="medium">Họ tên</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography>{customer.name}</Typography>
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
                    <Typography>{customer.email || '—'}</Typography>
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
                    <Typography>{customer.phone || '—'}</Typography>
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
                    <Typography>{customer.address || '—'}</Typography>
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
                    {getStatusChip(customer.statusId)}
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

export default CustomerDetail;