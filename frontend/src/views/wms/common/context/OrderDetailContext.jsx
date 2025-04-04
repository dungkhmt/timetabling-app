import React, { createContext, useContext, useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { toast } from 'react-toastify';
import { ORDER_TYPE_ID } from "../constants/constants";

// Tạo context
const OrderDetailContext = createContext();

export const OrderDetailProvider = ({ children, orderType = ORDER_TYPE_ID.SALES_ORDER }) => {
  const { id } = useParams();
  const history = useHistory();
  const { 
    getSalesOrderDetails, 
    getPurchaseOrderDetails,
    updateSalesOrderStatus, 
    updatePurchaseOrderStatus,
    approveSalesOrder,
    approvePurchaseOrder
  } = useWms2Data();
  
  const [orderData, setOrderData] = useState(null);
  const [loading, setLoading] = useState(true);

  // Xác định API functions dựa trên orderType
  const getDetailsFn = orderType === ORDER_TYPE_ID.SALES_ORDER 
    ? getSalesOrderDetails 
    : getPurchaseOrderDetails;
    
  const updateStatusFn = orderType === ORDER_TYPE_ID.SALES_ORDER 
    ? updateSalesOrderStatus 
    : updatePurchaseOrderStatus;
    
  const approveOrderFn = orderType === ORDER_TYPE_ID.SALES_ORDER 
    ? approveSalesOrder 
    : approvePurchaseOrder;

  // Fetch order data
  useEffect(() => {
    const fetchOrderData = async () => {
      if (!id) {
        setLoading(false);
        return;
      }

      setLoading(true);
      try {
        const res = await getDetailsFn(id);
        if (!res || res.code !== 200) {
          toast.error("Lỗi khi tải thông tin đơn hàng: " + res?.message);
          return;
        }
        
        // Thêm orderTypeId vào dữ liệu
        setOrderData({
          ...res.data,
          orderTypeId: orderType
        });
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
      const res = await approveOrderFn(id);
      
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
      const result = await updateStatusFn(orderData.id, "CANCELED");
      
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
    const editPath = orderType === ORDER_TYPE_ID.SALES_ORDER
      ? `/wms/edit-sale-order/${orderData.id}`
      : `/wms/edit-purchase-order/${orderData.id}`;
    
    history.push(editPath);
  };

  const value = {
    orderData,
    loading,
    approveOrderApi,
    cancelOrder,
    editOrder,
    orderType
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