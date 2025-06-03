import React, { useEffect, useState } from 'react';
import { 
  MapContainer, 
  TileLayer, 
  Marker, 
  Popup,
  Tooltip,
  useMap
} from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Box, Typography, CircularProgress } from '@mui/material';
import { Home, Room } from '@mui/icons-material';

// Fix for the marker icons in React Leaflet
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
    html: `<div style="background-color: ${color}; width: 30px; height: 30px; display: flex; justify-content: center; align-items: center; border-radius: 50%; color: white; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);">
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

const facilityIcon = createCustomIcon(Home, '#2196f3');
const genericIcon = createCustomIcon(Room, '#f44336');

const MapComponent = ({ 
  locations = [], 
  height = 400, 
  defaultCenter = [21.0285, 105.8542], // Hanoi coordinates as default
  onMarkerClick,
  renderPopupContent,
  markerIcon = "facility"
}) => {
  const [isMapReady, setIsMapReady] = useState(false);

  // Validate locations
  if (!locations || locations.length === 0) {
    return (
      <Box 
        sx={{ 
          height, 
          width: '100%', 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center',
          border: '1px solid #e0e0e0',
          borderRadius: '8px'
        }}
      >
        <Typography color="textSecondary">Không có dữ liệu vị trí</Typography>
      </Box>
    );
  }

  // Filter out locations without valid coordinates
  const validLocations = locations.filter(location => 
    location.latitude && location.longitude &&
    !isNaN(location.latitude) && !isNaN(location.longitude)
  );

  if (validLocations.length === 0) {
    return (
      <Box 
        sx={{ 
          height, 
          width: '100%', 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center',
          border: '1px solid #e0e0e0',
          borderRadius: '8px'
        }}
      >
        <Typography color="textSecondary">Không có tọa độ hợp lệ</Typography>
      </Box>
    );
  }

  // Calculate bounds points for map centering
  const allMapPoints = validLocations.map(location => [location.latitude, location.longitude]);
  
  // Get the icon based on type
  const getIcon = () => {
    switch (markerIcon) {
      case "facility":
        return facilityIcon;
      default:
        return genericIcon;
    }
  };

  // Default render popup content
  const defaultRenderPopupContent = (location) => (
    <>
      <Typography variant="subtitle2">{location.name}</Typography>
      {location.address && (
        <Typography variant="body2">{location.address}</Typography>
      )}
      {location.phone && (
        <Typography variant="caption">SĐT: {location.phone}</Typography>
      )}
    </>
  );

  return (
    <Box sx={{ height, width: '100%', position: 'relative' }}>
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
        center={allMapPoints.length === 1 ? allMapPoints[0] : defaultCenter}
        zoom={allMapPoints.length === 1 ? 15 : 10}
        style={{ height: '100%', width: '100%', borderRadius: '8px' }}
        scrollWheelZoom={true}
        whenReady={() => setIsMapReady(true)}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        
        {allMapPoints.length > 1 && <SetBoundsComponent points={allMapPoints} />}
        
        {/* Location markers */}
        {validLocations.map((location, index) => (
          <Marker 
            key={location.id || `location-${index}`}
            position={[location.latitude, location.longitude]} 
            icon={getIcon()}
            eventHandlers={{
              click: () => onMarkerClick && onMarkerClick(location)
            }}
          >
            <Popup>
              {renderPopupContent ? renderPopupContent(location) : defaultRenderPopupContent(location)}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </Box>
  );
};

export default MapComponent;