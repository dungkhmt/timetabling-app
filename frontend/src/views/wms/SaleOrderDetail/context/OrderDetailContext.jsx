import React, { createContext, useContext, useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { toast } from 'react-toastify';

// Tạo context
const OrderDetailContext = createContext();

export const OrderDetailProvider = ({ children }) => {
  const { id } = useParams();
  const navigate = useHistory();
  const { getOrderDetails, updateStatusOrder, approveOrder } = useWms2Data();
  
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

  // Duyệt đơn hàng
  const approveOrderApi = async () => {
    
    try {
      // setLoading(true);
      const res = await approveOrder(id);
      
      if (res && res.code == 200) {
        toast.success("Đơn hàng đã được duyệt thành công!");
        // Cập nhật dữ liệu local sau khi duyệt thành công
        setOrderData(prev => ({
          ...prev,
          status: "Đã duyệt"
        }));
      } else {
        const errorMsg = res?.message || "Không thể duyệt đơn hàng";
        toast.error(errorMsg);
      }
    } catch (error) {
      console.error("Error approving order:", error);
      toast.error("Không thể duyệt đơn hàng");
    } finally {
      // setLoading(false);
    }
  };

  // Hủy đơn hàng
  const cancelOrder = async () => {
    if (!orderData || !orderData.id) return;
    
    try {
      // setLoading(true);
      const result = await updateStatusOrder(orderData.id, "CANCELED");
      
      if (result && result.success) {
        toast.success("Đơn hàng đã được hủy thành công!");
        // Cập nhật dữ liệu local sau khi hủy thành công
        setOrderData(prev => ({
          ...prev,
          status: "Đã hủy"
        }));
      } else {
        const errorMsg = result?.message || "Không thể hủy đơn hàng";
        toast.error(errorMsg);
      }
    } catch (error) {
      console.error("Error cancelling order:", error);
      toast.error("Không thể hủy đơn hàng");
    } finally {
      // setLoading(false);
    }
  };

  // Chuyển sang trang chỉnh sửa đơn hàng
  const editOrder = () => {
    if (!orderData || !orderData.id) return;
    navigate(`/wms/edit-sales-order/${orderData.id}`);
  };

  // Mở dialog chiết khấu
  const applyDiscount = () => {
    toast.info("Tính năng chiết khấu đang được phát triển");
    // Phần mở dialog sẽ được thêm sau
  };

  const value = {
    orderData,
    // loading,
    approveOrderApi,
    cancelOrder,
    editOrder,
    applyDiscount
  };

  return (
    <OrderDetailContext.Provider value={value}>
      {children}
    </OrderDetailContext.Provider>
  );
};

export const useOrderDetail = () => {
  const context = useContext(OrderDetailContext);
  if (!context) {
    throw new Error('useOrderDetail must be used within an OrderDetailProvider');
  }
  return context;
};