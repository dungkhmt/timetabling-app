import React, { useEffect, useState, useRef } from "react";
import { 
  Box, 
  Button, 
  TextField, 
  CircularProgress, 
  List, 
  ListItem, 
  ListItemButton, 
  ListItemText, 
  Paper, 
  Typography,
  Chip,
  Collapse
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import { toast } from "react-toastify";
import { getAddressFromCoordinates, searchLocations } from "views/wms/common/OSMApi/OSMApi";

// Fix for the Leaflet icon issue in webpack
delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

// Component to center map on marker position
const SetViewOnChange = ({ center }) => {
  const map = useMap();

  useEffect(() => {
    if (center) {
      map.setView(center, 15);
    }
  }, [center, map]);

  return null;
};

// Component to handle map click events
const MapClickHandler = ({ onMapClick }) => {
  const map = useMap();

  useEffect(() => {
    const handleClick = async (e) => {
      const { lat, lng } = e.latlng;
      const roundedLat = parseFloat(lat.toFixed(6));
      const roundedLng = parseFloat(lng.toFixed(6));

      try {
        // Get address for clicked location using the API service
        const data = await getAddressFromCoordinates(roundedLat, roundedLng);
        const address = data?.display_name || "";
        onMapClick(roundedLat, roundedLng, address);
      } catch (error) {
        console.error("Error fetching address:", error);
        onMapClick(roundedLat, roundedLng, "");
      }
    };

    map.on("click", handleClick);

    return () => {
      map.off("click", handleClick);
    };
  }, [map, onMapClick]);

  return null;
};

// Component to display search results
const SearchResultsList = ({ results, onLocationSelect, onClose }) => {
  const [expanded, setExpanded] = useState(true);

  const handleLocationClick = (location) => {
    const lat = parseFloat(location.lat);
    const lon = parseFloat(location.lon);
    onLocationSelect(lat, lon, location.display_name);
    onClose();
  };

  const getLocationTypeLabel = (location) => {
    if (location.type === 'city') return 'Thành phố';
    if (location.type === 'residential') return 'Khu dân cư';
    if (location.type === 'secondary') return 'Đường chính';
    if (location.type === 'tertiary') return 'Đường phụ';
    if (location.type === 'service') return 'Đường dịch vụ';
    return location.type;
  };

  const getLocationTypeColor = (location) => {
    if (location.type === 'city') return 'primary';
    if (location.type === 'residential') return 'success';
    if (location.type === 'secondary') return 'warning';
    if (location.type === 'tertiary') return 'info';
    if (location.type === 'service') return 'secondary';
    return 'default';
  };

  return (
    <Paper 
      elevation={3} 
      sx={{ 
        mb: 2, 
        maxHeight: expanded ? 400 : 60, 
        overflow: 'hidden',
        transition: 'max-height 0.3s ease-in-out'
      }}
    >
      <Box 
        sx={{ 
          p: 2, 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          borderBottom: '1px solid #e0e0e0',
          cursor: 'pointer'
        }}
        onClick={() => setExpanded(!expanded)}
      >
        <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
          Kết quả tìm kiếm ({results.length})
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Button size="small" color="error" onClick={(e) => { e.stopPropagation(); onClose(); }}>
            Đóng
          </Button>
          {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
        </Box>
      </Box>
      
      <Collapse in={expanded}>
        <List sx={{ maxHeight: 320, overflow: 'auto' }}>
          {results.map((location, index) => (
            <ListItem key={location.place_id || index} disablePadding>
              <ListItemButton 
                onClick={() => handleLocationClick(location)}
                sx={{ 
                  py: 1.5,
                  '&:hover': { 
                    backgroundColor: 'action.hover' 
                  }
                }}
              >
                <LocationOnIcon 
                  sx={{ 
                    mr: 2, 
                    color: 'primary.main',
                    fontSize: 20
                  }} 
                />
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                        {location.name}
                      </Typography>
                      <Chip 
                        label={getLocationTypeLabel(location)} 
                        size="small" 
                        color={getLocationTypeColor(location)}
                        sx={{ height: 20, fontSize: '0.7rem' }}
                      />
                    </Box>
                  }
                  secondary={
                    <Box>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                        {location.display_name}
                      </Typography>
                      <Typography variant="caption" color="text.disabled">
                        Tọa độ: {parseFloat(location.lat).toFixed(6)}, {parseFloat(location.lon).toFixed(6)}
                      </Typography>
                    </Box>
                  }
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Collapse>
    </Paper>
  );
};

const LocationPicker = ({ latitude, longitude, onLocationSelect }) => {
  const [mapCenter, setMapCenter] = useState([21.0278, 105.8342]); // Default to Hanoi
  const [markerPosition, setMarkerPosition] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [searchResults, setSearchResults] = useState([]);
  const [showResults, setShowResults] = useState(false);
  const mapRef = useRef(null);

  // Update marker position when latitude/longitude props change
  useEffect(() => {
    if (latitude !== null && longitude !== null) {
      setMarkerPosition([latitude, longitude]);
      setMapCenter([latitude, longitude]);
    }
  }, [latitude, longitude]);

  // Handle address search using the API service
  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    setSearching(true);
    setSearchResults([]);
    setShowResults(false);
    
    try {
      const data = await searchLocations(searchQuery);

      if (data && data.length > 0) {
        setSearchResults(data);
        
        if (data.length === 1) {
          // If only one result, select it immediately
          const lat = parseFloat(data[0].lat);
          const lon = parseFloat(data[0].lon);
          setMapCenter([lat, lon]);
          setMarkerPosition([lat, lon]);
          onLocationSelect(lat, lon, data[0].display_name);
        } else {
          // Show results list for multiple results
          setShowResults(true);
        }
      } else {
        toast.warning("Không tìm thấy địa chỉ này");
      }
    } catch (error) {
      console.error("Error searching for location:", error);
      toast.error("Lỗi khi tìm kiếm địa chỉ");
    } finally {
      setSearching(false);
    }
  };

  const handleMapClick = (lat, lng, address) => {
    setMarkerPosition([lat, lng]);
    onLocationSelect(lat, lng, address);
    // Hide search results when user clicks on map
    setShowResults(false);
  };

  const handleResultLocationSelect = (lat, lon, address) => {
    setMapCenter([lat, lon]);
    setMarkerPosition([lat, lon]);
    onLocationSelect(lat, lon, address);
  };

  const handleCloseResults = () => {
    setShowResults(false);
    setSearchResults([]);
  };

  return (
    <Box>
      <Box display="flex" gap={2} mb={2} alignItems="center">
        <TextField
          fullWidth
          label="Tìm kiếm địa chỉ"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => {
            if (e.key === "Enter") {
              handleSearch();
            }
          }}
          placeholder="Nhập địa chỉ để tìm kiếm (VD: 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội)"
        />
        <Button
          variant="contained"
          color="primary"
          onClick={handleSearch}
          disabled={searching}
          startIcon={
            searching ? (
              <CircularProgress size={16} color="inherit" />
            ) : (
              <SearchIcon />
            )
          }
          sx={{
            whiteSpace: "nowrap",
            minWidth: "fit-content",
            padding: "1rem",
          }}
        >
          Tìm kiếm
        </Button>
      </Box>

      {/* Search Results List */}
      {showResults && searchResults.length > 0 && (
        <SearchResultsList
          results={searchResults}
          onLocationSelect={handleResultLocationSelect}
          onClose={handleCloseResults}
        />
      )}

      <Box
        height="450px"
        border="1px solid #ccc"
        borderRadius={1}
        overflow="hidden"
        sx={{ boxShadow: "0 2px 8px rgba(0,0,0,0.1)" }}
      >
        <MapContainer
          center={mapCenter}
          zoom={13}
          style={{ height: "100%", width: "100%" }}
          ref={mapRef}
          whenCreated={(mapInstance) => {
            // Save map instance for external control if needed
            window.leafletMap = mapInstance;
          }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          <MapClickHandler onMapClick={handleMapClick} />
          <SetViewOnChange center={mapCenter} />

          {markerPosition && (
            <Marker position={markerPosition}>
              <Popup>
                <strong>Vị trí đã chọn</strong>
                <br />
                Vĩ độ: {markerPosition[0]}
                <br />
                Kinh độ: {markerPosition[1]}
              </Popup>
            </Marker>
          )}
        </MapContainer>
      </Box>

      <Box mt={2} fontSize="0.875rem" color="text.secondary">
        <Box fontWeight="500" mb={0.5}>
          Cách sử dụng:
        </Box>
        <ol style={{ margin: 0, paddingLeft: "1.5rem" }}>
          <li>Nhập địa chỉ vào thanh tìm kiếm và nhấn tìm kiếm để định vị.</li>
          <li>Nếu có nhiều kết quả, chọn địa chỉ phù hợp từ danh sách.</li>
          <li>Hoặc nhấn trực tiếp vào bản đồ để chọn vị trí.</li>
          <li>Khi đã chọn vị trí, một ghim xanh sẽ xuất hiện trên bản đồ.</li>
        </ol>
      </Box>
    </Box>
  );
};

export default LocationPicker;