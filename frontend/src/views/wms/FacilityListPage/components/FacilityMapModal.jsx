import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Chip,
  Box,
  IconButton
} from '@mui/material';
import { Close } from '@mui/icons-material';
import MapComponent from '../../../../components/common/MapComponent';

const FacilityMapModal = ({ open, onClose, facilities }) => {
  const handleMarkerClick = (facility) => {
    console.log('Clicked facility:', facility);
    // You can add additional actions here, like showing facility details
  };

  const renderPopupContent = (facility) => (
    <Box>
      <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1 }}>
        {facility.name}
      </Typography>
      
      {facility.address && (
        <Typography variant="body2" sx={{ mb: 1 }}>
          <strong>Địa chỉ:</strong> {facility.address}
        </Typography>
      )}
      
      {facility.phone && (
        <Typography variant="body2" sx={{ mb: 1 }}>
          <strong>Số điện thoại:</strong> {facility.phone}
        </Typography>
      )}
      
      {facility.postalCode && (
        <Typography variant="body2" sx={{ mb: 1 }}>
          <strong>Mã bưu chính:</strong> {facility.postalCode}
        </Typography>
      )}
      
      <Box sx={{ mt: 1 }}>
        {facility.isDefault && (
          <Chip label="Mặc định" color="primary" size="small" sx={{ mr: 1 }} />
        )}
        
        <Chip 
          label={facility.statusId === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'} 
          color={facility.statusId === 'ACTIVE' ? 'success' : 'error'} 
          size="small" 
        />
      </Box>
    </Box>
  );

  return (
    <Dialog 
      open={open} 
      onClose={onClose} 
      maxWidth="lg" 
      fullWidth
      PaperProps={{
        sx: { minHeight: '70vh' }
      }}
    >
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">Vị trí các cơ sở</Typography>
        <IconButton onClick={onClose} size="small">
          <Close />
        </IconButton>
      </DialogTitle>
      
      <DialogContent sx={{ p: 0 }}>
        <Box sx={{ p: 2 }}>
          <MapComponent
            locations={facilities}
            height={500}
            onMarkerClick={handleMarkerClick}
            renderPopupContent={renderPopupContent}
            markerIcon="facility"
          />
        </Box>
      </DialogContent>
      
      <DialogActions sx={{ p: 2 }}>
        <Typography variant="body2" color="textSecondary" sx={{ flexGrow: 1 }}>
          Hiển thị {facilities?.length || 0} cơ sở trên bản đồ
        </Typography>
        <Button onClick={onClose} variant="contained" color="primary">
          Đóng
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default FacilityMapModal;