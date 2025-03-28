import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { useWms2Data } from 'services/useWms2Data';
import { useParams } from 'react-router-dom';
// Tạo context
const ShipmentContext = createContext();

// Provider Component
export const ShipmentProvider = ({ children }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { getOutBoundDetail, exportShipment } = useWms2Data();
  const [outboundData, setOutboundData] = useState(null);
  const { shipmentId } = useParams(); // Lấy ID từ URL

  // Fetch dữ liệu
  const fetchData = async () => {
    try {
      const response = await getOutBoundDetail(shipmentId);
      setOutboundData(response.data);
    } catch (err) {
      console.error("Failed to fetch outbound detail:", err);
    }
  };

  const exportShipmentApi = async (shipmentId) => {
    try {
      debugger;
      const res = await exportShipment(shipmentId);
    } catch (err) {
      console.error("Failed to export shipment:", err);
    }
  }

  // Load dữ liệu khi component mount
  useEffect(() => {
    if (shipmentId) {
      fetchData();
    }
  }, [shipmentId]);

  // Context value
  const value = {
    loading,
    error,
    outboundData,
    fetchData,
    exportShipmentApi
  };

  return (
    <ShipmentContext.Provider value={value}>
      {children}
    </ShipmentContext.Provider>
  );
};

// Hook để sử dụng context
export const useShipment = () => {
  const context = useContext(ShipmentContext);
  if (context === undefined) {
    throw new Error('useShipment must be used within a ShipmentProvider');
  }
  return context;
};