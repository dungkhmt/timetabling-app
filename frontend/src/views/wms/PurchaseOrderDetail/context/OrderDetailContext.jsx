import React, { createContext, useContext, useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { toast } from 'react-toastify';

// Tạo context
const OrderDetailContext = createContext();

export const OrderDetailProvider = ({ children }) => {
  const { id } = useParams();
  const navigate = useHistory();
  const { getPurchaseOrderDetails, updatePurchaseOrderStatus, approvePurchaseOrder } = useWms2Data();
  
  const [orderData, setOrderData] = useState(null);
  const [loading, setLoading] = useState(true);

  // Fetch order data
  useEffect(() => {
    const fetchOrderData = async () => {
      if (!id) {
        setLoading(false);
        return;
      }

      setLoading(true);
      try {
        const res = await getPurchaseOrderDetails(id);
        if (!res || res.code !== 200) {
          toast.error("Lỗi khi tải thông tin đơn hàng: " + res?.message);
          return;
        }
        
        setOrderData(res.data);
      } catch (error) {
        console.error("Error fetching order:", error);
        toast.error("Không thể tải thông tin đơn hàng");
      } finally {
        setLoading(false);
      }
    };

    fetchOrderData();
  }, [id]);

  // Duyệt đơn hàng
  const approveOrderApi = async () => {
    try {
      setLoading(true);
      const res = await approvePurchaseOrder(id);
      
      if (res && res.code === 200) {
        toast.success("Đơn hàng đã được duyệt thành công!");
        // Cập nhật dữ liệu local sau khi duyệt thành công
        setOrderData(prev => ({
          ...prev,
          status: "APPROVED"
        }));
      } else {
        const errorMsg = res?.message || "Không thể duyệt đơn hàng";
        toast.error(errorMsg);
      }
    } catch (error) {
      console.error("Error approving order:", error);
      toast.error("Không thể duyệt đơn hàng");
    } finally {
      setLoading(false);
    }
  };

  // Hủy đơn hàng
  const cancelOrder = async () => {
    if (!orderData || !orderData.id) return;
    
    try {
      setLoading(true);
      const result = await updatePurchaseOrderStatus(orderData.id, "CANCELED");
      
      if (result && result.success) {
        toast.success("Đơn hàng đã được hủy thành công!");
        // Cập nhật dữ liệu local sau khi hủy thành công
        setOrderData(prev => ({
          ...prev,
          status: "CANCELED"
        }));
      } else {
        const errorMsg = result?.message || "Không thể hủy đơn hàng";
        toast.error(errorMsg);
      }
    } catch (error) {
      console.error("Error cancelling order:", error);
      toast.error("Không thể hủy đơn hàng");
    } finally {
      setLoading(false);
    }
  };

  // Chuyển sang trang chỉnh sửa đơn hàng
  const editOrder = () => {
    if (!orderData || !orderData.id) return;
    navigate(`/wms/edit-purchase-order/${orderData.id}`);
  };

  const value = {
    orderData,
    loading,
    approveOrderApi,
    cancelOrder,
    editOrder
  };

  return (
    <OrderDetailContext.Provider value={value}>
      {children}
    </OrderDetailContext.Provider>
  );
};

export const useOrderDetail = () => {
  const context = useContext(OrderDetailContext);
  if (context === undefined) {
    throw new Error('useOrderDetail must be used within an OrderDetailProvider');
  }
  return context;
};