import React from "react";
import { useWms2Data } from 'services/useWms2Data';
import OrderListBase from "../common/components/OrderListBase";
import OrderHeader from "../common/components/OrderHeader";
import OrderTabs from "../common/components/OrderTabs";
import OrderFilters from "../common/components/OrderFilters";
import OrderTable from "../common/components/OrderTable";
import { PURCHASE_ORDER_SCHEMA, PURCHASE_ORDER_TABS, ORDER_TYPE_ID } from "../common/constants/constants";

const PurchaseOrderListPage = () => {
  const { getPurchaseOrders, getPurchaseOrdersForExport } = useWms2Data();

  const initialFilters = {
    keyword: "",
    status: null,
    startCreatedAt: null,
    endCreatedAt: null,
    channelId: null
  };

  return (
    <OrderListBase
      type= {ORDER_TYPE_ID.PURCHASE_ORDER}
      tabs={PURCHASE_ORDER_TABS}
      filters={initialFilters}
      schema={PURCHASE_ORDER_SCHEMA}
      getOrdersFunction={getPurchaseOrders}
      getOrdersForExportFunction={getPurchaseOrdersForExport}
      Header={OrderHeader}
      Tabs={OrderTabs}
      Filters={OrderFilters}
      Table={OrderTable}
    />
  );
};

export default PurchaseOrderListPage;