import React from "react";
import {OrderDetailProvider} from "../common/context/OrderDetailContext";
import OrderHeader from "./components/OrderHeader";
import OrderDetailBase from "../common/components/OrderDetailBase";
import {ORDER_TYPE_ID} from "../common/constants/constants";
import OrderBasicInfo from "../common/components/OrderBasicInfo";
import OrderDeliveryInfo from "../common/components/OrderDeliveryInfo";
import OrderItemsList from "../common/components/OrderItemsList";
import OrderSummary from "../common/components/OrderSummary";

const orderLabels = [
  "Tổng quan",
  "Thông tin chung",
  "Thanh toán",
  "Sản phẩm",
];

const SaleOrderDetailContent = () => {
  return (
    <OrderDetailBase
      tabLabels={orderLabels}
      HeaderComponent={OrderHeader}
      BasicInfoComponent={OrderBasicInfo}
      DeliveryInfoComponent={OrderDeliveryInfo}
      ItemsListComponent={OrderItemsList}
      SummaryComponent={OrderSummary}
    />
  );
};

const SaleOrderDetail = () => {
  return (
    <OrderDetailProvider orderType={ORDER_TYPE_ID.SALES_ORDER}>
      <SaleOrderDetailContent />
    </OrderDetailProvider>
  );
};

export default SaleOrderDetail;