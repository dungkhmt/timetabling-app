import React, { createContext, useContext, useState, useCallback } from 'react';
import axios from 'axios';
import { useWms2Data } from 'services/useWms2Data';

// Tạo context
const ShipmentContext = createContext();

// Provider Component
export const ShipmentProvider = ({ children }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
    const {getOutBoundDetail} = useWms2Data();

  // API để lấy chi tiết phiếu xuất
  const getOutBoundDetailApi = async (shipmentId) => {
    try {
      const response = await getOutBoundDetail(shipmentId);
      return response.data;
    } catch (error) {
      console.error("Error fetching outbound detail:", error);
      setError("Không thể tải thông tin phiếu xuất");
      return { data: {} };
    }
    }
    
 

  // Context value
  const value = {
    loading,
    error,
    getOutBoundDetailApi,
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