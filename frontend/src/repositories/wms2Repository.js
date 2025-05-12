import { request } from "api";

export const wms2Service = {
  
  createSalesOrder: (salesOrderData) =>
    request("post", "/sale-order/create", null, null, salesOrderData),

  getMoreFacilities: (page, limit) => {
    return request("get", `/facility/get-more?page=${page}&limit=${limit}`);
  },

  getMoreProducts: (page, limit) => {
    return request("get", `/product/get-more?page=${page}&limit=${limit}`);
  },

  getMoreCustomers: (page, limit) => {
    return request("get", `/customer/get-more?page=${page}&limit=${limit}`);
  },

  searchProducts: (searchText, page, limit) => {
    return request("get", `/product/search?query=${encodeURIComponent(searchText)}&page=${page}&limit=${limit}`);
  },

  getOrderDetails: (orderId) => {
    return request("get", `/sale-order/details/${orderId}`);
  },
  updateStatusOrder: (orderId, status) => {
    return request("put", `/sale-order/update-status/${orderId}`, null, null, {status});
  },
  approveOrder: (orderId) => {
    return request("put", `/sale-order/approve/${orderId}`);
  },
  rejectOrder: (orderId) => {
    return request("put", `/sale-order/reject/${orderId}`);
  },
  getSalesOrders: (page, limit, filters) => {
    return request("post", `/sale-order/get-all?page=${page}&limit=${limit}`, null, null, filters);
  }, 
  getSalesOrdersApproved: (page, limit) => {
    return request("get", `/sale-order/get-approved?page=${page}&limit=${limit}`);
  }, 
  getOutBoundsOrder: (orderId, page, limit) => {
    return request("get", `/shipment/outbound/order/${orderId}?page=${page}&limit=${limit}`);
  },
  getMoreInventoryItems: (page, limit, orderId) => {
    return request("get", `/inventory-item/for-order/${orderId}?page=${page}&limit=${limit}`);
  }, 
  createOutBoundOrder : (data) => {
    return request("post", "/shipment/outbound/create", null, null, data);
  }, 
  getOutBoundDetail: (shipmentId) => {
    return request("get", `shipment/outbound/${shipmentId}`);
  },
  exportOutBoundShipment: (shipmentId) => {
    return request("put", `/invoice/export-outbound/${shipmentId}` , null, null, null);
  }, 
  getSalesOrdersForExport: (page, limit) => {
    return request("get", `/sale-order/export?page=${page}&limit=${limit}`);
  },
  getMoreSuppliers: (page, limit) => {
    return request("get", `/supplier/get-more?page=${page}&limit=${limit}`);
  },
  createPurchaseOrder: (purchaseOrderData) =>
    request("post", "/purchase-order/create", null, null, purchaseOrderData),
  getPurchaseOrders : (page, limit, filters) => {
    return request("post", `/purchase-order/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getPurchaseOrderDetails: (orderId) => {
    return request("get", `/purchase-order/details/${orderId}`);
  },
  updatePurchaseOrderStatus: (orderId, status) => {
    return request("put", `/purchase-order/update-status/${orderId}`, null, null, {status});
  },
    approvePurchaseOrder: (orderId) => {
        return request("put", `/purchase-order/approve/${orderId}`);
    },
  getInBoundsOrder : (orderId, page, limit) => {
    return request("get", `/shipment/inbound/order/${orderId}?page=${page}&limit=${limit}`);
  },
    createInBoundOrder : (data) => {
        return request("post", "/shipment/inbound/create", null, null, data);
    },
    getInBoundDetail: (shipmentId) => {
        return request("get", `shipment/inbound/${shipmentId}`);
    },
  exportInBoundShipment: (shipmentId) => {
    return request("put", `/invoice/export-inbound/${shipmentId}` , null, null, null);
  },
  getLowStockForecast : () => {
    return request("get", `/forecast/daily-low-stock`);
  },
  createDeliveryBill: (data) => {
    return request("post", "/delivery-bill/create", null, null, data);
  },
  getOutBoundsForDeliveryBill : (page, limit) => {
    return request("get", `/shipment/for-delivery?page=${page}&limit=${limit}`);
  },
  getDeliveryBills: (page, limit, filters) => {
    return request("post", `/delivery-bill/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getDeliveryPlans : (page, limit, filters) => {
    return request("post", `/delivery-plan/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getShippers : (page, limit, filters) => {
    return request("post", `/shipper/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  createDeliveryPlan : (data) => {
    return request("post", "/delivery-plan/create", null, null, data);
  },
  autoAssignDeliveryRoutes: (deliveryPlanId) => {
    return request("get", `delivery-route/auto-assign/${deliveryPlanId}`, null, null, null);
  },
  getDeliveryPlanById: (deliveryPlanId) => {
    return request("get", `delivery-plan/details/${deliveryPlanId}`, null, null, null);
  },
  getVehicles : (page, limit, filters) => {
    return request("get", `/vehicle/get-all?page=${page}&limit=${limit}&statusId=${filters.statusId}`, null, null, null);
  },
  createProduct : (data) => {
    return request("post", "/product/create", null, null, data);
  },
  getProductCategories : () => {
    return request("get", "/product-category/get-all");
  },
  getProductsWithFilters : (page, limit, filters) => {
    return request("post", `/product/get-all?page=${page}&limit=${limit}`, null, null, filters);
  }
};