export const wms_sales = {
    id: "MENU_WMS_SALES",
    icon: "InfoIcon",
    text: "Bán hàng",
    child: [
      {
        id: "MENU_WMS_SALES.SALES_ORDER",
        path: "/wms/sales/orders",
        isPublic: true,
        text: "Đơn hàng bán",
        child: [],
      },
      {
        id: "MENU_WMS_SALES.CUSTOMERS",
        path: "/wms/sales/customers",
        isPublic: true,
        text: "Khách hàng",
        child: [],
      },
      {
        id: "MENU_WMS_SALES.DASHBOARD",
        path: "/wms/sales/dashboard",
        isPublic: true,
        text: "Dashboard",
        child: [],
      }
    ],
  };
  