export const wms_logistics_admin = {
  id: "MENU_WMS_LOGISTICS_ADMIN",
  icon: "DashboardIcon",
  text: "Thủ kho",
  child: [
    {
      id: "MENU_WMS_LOGISTICS_ADMIN.PRODUCT",
      path: "/wms/admin/product",
      isPublic: false,
      text: "Danh sách sản phẩm",
      child: [],
    },
    {
      id: "MENU_WMS_LOGISTICS_ADMIN.FACILITY",
      path: "/wms/admin/facility",
      isPublic: false,
      text: "Danh sách kho",
      child: [],
    },
    {
      id: "MENU_WMS_LOGISTICS_ADMIN.INVENTORY_DETAIL",
      path: "/wms/admin/inventory_detail",
      isPublic: false,
      text: "Lịch sử xuất nhập",
      child: [],
    },
    {
      id: "MENU_WMS_LOGISTICS_ADMIN.DASHBOARD",
      path: "/wms/admin/dashboard",
      isPublic: false,
      text: "Danh sách kho",
      child: [],
    },
  ],
};
