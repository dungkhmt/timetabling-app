import axios from "axios";

const OSM_API_URL = "https://nominatim.openstreetmap.org";

// Get address from coordinates (reverse geocoding)
export const getAddressFromCoordinates = async (lat, lon) => {
  try {
    const response = await axios.get(
      `${OSM_API_URL}/reverse`, {
        params: {
          format: 'json',
          lat: lat,
          lon: lon
        },
      }
    );
    return response.data;
  } catch (error) {
    console.error("Error fetching address:", error);
    return null;
  }
};

// Search for locations by query string
export const searchLocations = async (query) => {
  try {
    const response = await axios.get(
      `${OSM_API_URL}/search`, {
        params: {
          format: 'json',
          q: query
        },
      }
    );
    return response.data;
  } catch (error) {
    console.error("Error searching locations:", error);
    return [];
  }
};