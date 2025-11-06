import React, { useEffect, useState } from 'react';
import { 
  MapContainer, 
  TileLayer, 
  Marker, 
  Popup, 
  Polyline,
  Tooltip,
  useMap
} from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Box, Typography, Chip, CircularProgress } from '@mui/material';
import { LocalShipping, Room, Home } from '@mui/icons-material';

// Fix for the marker icons in React Leaflet
// We need to correct the path to marker icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
});

// Custom icons
const createCustomIcon = (IconComponent, color) => {
  return L.divIcon({
    className: 'custom-div-icon',
    html: `<div style="background-color: ${color}; width: 30px; height: 30px; display: flex; justify-content: center; align-items: center; border-radius: 50%; color: white;">
      <svg viewBox="0 0 24 24" width="18" height="18">
        ${IconComponent === Home ? 
          '<path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" fill="currentColor"></path>' : 
          '<path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"></path>'
        }
      </svg>
    </div>`,
    iconSize: [30, 30],
    iconAnchor: [15, 30],
    popupAnchor: [0, -30]
  });
};

// Component to fit bounds after map is loaded
// This fixes the bounds issue without needing the bounds prop on MapContainer
function SetBoundsComponent({ points }) {
  const map = useMap();
  
  useEffect(() => {
    if (points && points.length > 0) {
      const bounds = L.latLngBounds(points);
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [map, points]);
  
  return null;
}

const warehouseIcon = createCustomIcon(Home, '#2196f3');
const deliveryPointIcon = createCustomIcon(Room, '#f44336');

const RouteMapComponent = ({ route, facilityLocation }) => {
  console.log('RouteMapComponent', route, facilityLocation);
  const [isMapReady, setIsMapReady] = useState(false);

  // Validate route properties to prevent errors
  if (!route || !route.path || !route.deliveryPoints) {
    return (
      <Box 
        sx={{ 
          height: 600,
          width: '100%', 
          mt: 2, 
          mb: 3, 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center',
          border: '1px solid #e0e0e0',
          borderRadius: '8px'
        }}
      >
        <Typography color="textSecondary">No route data available</Typography>
      </Box>
    );
  }
  
  // The facility location is our starting point
  const depot = facilityLocation || { 
    latitude: route.path[0].latitude, 
    longitude: route.path[0].longitude 
  };
  
  // Convert path to format needed by Leaflet
  const pathPositions = route.path.map(point => [point.latitude, point.longitude]);
  
  // Calculate bounds points for map centering
  const allMapPoints = [
    [depot.latitude, depot.longitude],
    ...pathPositions,
    ...route.deliveryPoints.map(point => [point.latitude, point.longitude])
  ];

  // Default center position in case bounds fail
  const defaultCenter = [depot.latitude, depot.longitude];
  
  return (
    <Box sx={{ height: 600, width: '100%', mt: 2, mb: 3, position: 'relative' }}>
      {!isMapReady && (
        <Box 
          sx={{ 
            position: 'absolute', 
            top: 0, 
            left: 0, 
            right: 0, 
            bottom: 0, 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center',
            backgroundColor: 'rgba(255, 255, 255, 0.7)',
            zIndex: 1000,
            borderRadius: '8px'
          }}
        >
          <CircularProgress />
        </Box>
      )}
      
      <MapContainer 
        center={defaultCenter}
        zoom={13}
        style={{ height: '100%', width: '100%', borderRadius: '8px' }}
        scrollWheelZoom={true}
        whenReady={() => setIsMapReady(true)}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        
        <SetBoundsComponent points={allMapPoints} />
        
        {/* Starting point (warehouse/facility) */}
        <Marker 
          position={[depot.latitude, depot.longitude]} 
          icon={warehouseIcon}
        >
          <Popup>
            <Typography variant="subtitle2">Điểm xuất phát</Typography>
            <Typography variant="body2">
              {depot.name || 'Kho hàng'}
            </Typography>
          </Popup>
          <Tooltip permanent direction="top" offset={[0, -15]}>
            <Typography variant="caption" sx={{ fontWeight: 'bold' }}>Xuất phát</Typography>
          </Tooltip>
        </Marker>
        
        {/* Delivery points */}
        {route.deliveryPoints.map((point, index) => (
          console.log('Delivery point:', point),
          <Marker 
            key={point.id || `delivery-point-${index}`}
            position={[point.latitude, point.longitude]} 
            icon={deliveryPointIcon}
          >
            <Popup>
              <Typography variant="subtitle2">{point.customerName || `Điểm giao #${point.sequenceNumber}`}</Typography>
              <Typography variant="body2">
                Khối lượng: {point.demand} kg
              </Typography>
              <Typography variant="caption">
                Thứ tự giao: {point.sequenceNumber}
              </Typography>
            </Popup>
            <Tooltip permanent direction="top" offset={[0, -15]}>
              <Chip 
                label={point.sequenceNumber} 
                size="small"
                color="error"
                sx={{ height: 22, minWidth: 22, fontSize: '0.75rem' }}
              />
            </Tooltip>
          </Marker>
        ))}
        
        {/* Route path */}
        <Polyline 
          positions={pathPositions}
          pathOptions={{ 
            color: '#ff1a1a',
            weight: 4,
            opacity: 0.8,
            dashArray: '5, 10',
            lineCap: 'round',
            lineJoin: 'round',
          }}
        />
      </MapContainer>
    </Box>
  );
};

export default RouteMapComponent;