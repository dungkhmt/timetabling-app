import React from "react";
import InfoCard from "./InfoCard";
import { useOrderDetail } from "../context/OrderDetailContext";
import dayjs from "dayjs";
import { ORDER_TYPE_ID } from "../constants/constants";

const OrderDeliveryInfo = () => {
  const { orderData } = useOrderDetail();

  if (!orderData) return null;

  // Xác định loại đơn hàng
  const orderTypeId = orderData.orderTypeId || ORDER_TYPE_ID.SALES_ORDER;
  
  // Thông tin giao hàng chung
  const commonDeliveryItems = [

  ];

  // Thông tin theo loại đơn hàng
  let specificItems = [];
  
  switch (orderTypeId) {
    case ORDER_TYPE_ID.SALES_ORDER:
      specificItems = [
        { 
          label: "Ngày giao sau", 
          value: orderData.deliveryAfterDate 
            ? dayjs(orderData.deliveryAfterDate).format("DD/MM/YYYY") 
            : "-" 
        },
        { 
          label: "Ngày giao trước", 
          value: orderData.deliveryBeforeDate 
            ? dayjs(orderData.deliveryBeforeDate).format("DD/MM/YYYY") 
            : "-" 
        },
        { 
          label: "Địa chỉ giao hàng", 
          value: orderData.deliveryFullAddress || "-"
        },
        { 
          label: "Số điện thoại nhận hàng", 
          value: orderData.deliveryPhone || "-" 
        }
      ];
      break;
      
    case ORDER_TYPE_ID.PURCHASE_ORDER:
      specificItems = [
        { 
          label: "Ngày giao dự kiến", 
          value: orderData.deliveryAfterDate 
            ? dayjs(orderData.deliveryAfterDate).format("DD/MM/YYYY") 
            : "-" 
        },
        { 
          label: "Hạn giao hàng", 
          value: orderData.deliveryBeforeDate 
            ? dayjs(orderData.deliveryBeforeDate).format("DD/MM/YYYY") 
            : "-" 
        },
        { 
          label: "Địa chỉ giao hàng", 
          value: orderData.deliveryAddress || orderData.facilityAddress || "-" 
        }
      ];
      break;
    default:
      break;
  }
  
  // Thêm trường ghi chú cho tất cả các loại đơn hàng
  specificItems.push({ label: "Ghi chú", value: orderData.note || "-" });
  
  // Kết hợp thông tin chung và thông tin riêng
  const deliveryInfoItems = [...commonDeliveryItems, ...specificItems];

  return <InfoCard items={deliveryInfoItems} />;
};

export default React.memo(OrderDeliveryInfo);