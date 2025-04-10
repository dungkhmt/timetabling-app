import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { useWms2Data } from 'services/useWms2Data';
import { useParams } from 'react-router-dom';
import {SHIPMENT_TYPE_ID} from "../constants/constants";
// Tạo context
const ShipmentContext = createContext();

// Provider Component
export const ShipmentProvider = ({ children, shipmentType }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { getOutBoundDetail, exportOutBoundShipment, getInBoundDetail, ex } = useWms2Data();
  const [shipmentData, setShipmentData] = useState(null);
  const { shipmentId } = useParams(); // Lấy ID từ URL

  // Fetch dữ liệu
  const fetchData = async () => {
    try {
      const response =   await (shipmentType === SHIPMENT_TYPE_ID.OUTBOUND ? getOutBoundDetail(shipmentId) : getInBoundDetail(shipmentId));
      setShipmentData(response.data);
    } catch (err) {
      console.error("Failed to fetch outbound detail:", err);
    }
  };

  const exportOutBoundShipmentApi = async (shipmentId) => {
    try {
      const res = await exportOutBoundShipment(shipmentId);
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
    shipmentData,
    fetchData,
    exportOutBoundShipmentApi
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