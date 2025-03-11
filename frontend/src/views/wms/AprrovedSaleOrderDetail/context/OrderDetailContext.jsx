import React, { createContext, useContext, useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { toast } from 'react-toastify';

// Tạo context
const ApprovedOrderDetailContext = createContext();

export const ApprovedOrderDetailProvider = ({ children }) => {
  const { id } = useParams();
  const navigate = useHistory();
  const { getOrderDetails } = useWms2Data();
  
  const [orderData, setOrderData] = useState(null);
  // const [loading, setLoading] = useState(true);

  // Fetch order data
  useEffect(() => {
    const fetchOrderData = async () => {
      if (!id) {
        // setLoading(false);
        return;
      }

      // setLoading(true);
      try {
        const res = await getOrderDetails(id);
        if ( !res || res.code != 200) {
          toast.error("Lỗi khi tải thông tin đơn hàng :" + res?.message);
          return;
        }
        
        setOrderData(res.data);
      } catch (error) {
        console.error("Error fetching order:", error);
        toast.error("Không thể tải thông tin đơn hàng");
      } finally {
        // setLoading(false);
      }
    };

    fetchOrderData();
  }, [id]);

  const value = {
    orderData,
    // loading,
  };

  return (
    <ApprovedOrderDetailContext.Provider value={value}>
      {children}
    </ApprovedOrderDetailContext.Provider>
  );
};

export const useApprovedOrderDetail = () => {
  const context = useContext(ApprovedOrderDetailContext);
  if (!context) {
    throw new Error('useOrderDetail must be used within an OrderDetailProvider');
  }
  return context;
};