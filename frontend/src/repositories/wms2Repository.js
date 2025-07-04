import {request} from "api";

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
    return request("put", `/order/approve/${orderId}`);
  },
  rejectOrder: (orderId) => {
    return request("put", `/sale-order/reject/${orderId}`);
  },
  getSalesOrders: (page, limit, filters) => {
    return request("post", `/sale-order/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getOutBoundsOrder: (orderId, page, limit) => {
    return request("get", `/shipment/outbound/order/${orderId}?page=${page}&limit=${limit}`);
  },
  getMoreInventoryItems: (page, limit, orderId) => {
    return request("get", `/inventory-item/for-inbound/${orderId}?page=${page}&limit=${limit}`);
  }, 
  getMoreInventoryItemsForOutbound: (page, limit, orderId) => {
    return request("get", `/inventory-item/for-outbound/${orderId}?page=${page}&limit=${limit}`);
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
    return request("put", `/invoice/import-inbound/${shipmentId}` , null, null, null);
  },
  getLowStockForecast : () => {
    return request("get", `/forecast/daily-low-stock`);
  },
  getWeeklyLowStockForecast: () => {
    return request("get", `/forecast/weekly-low-stock`);
  },
  createDeliveryBill: (data) => {
    return request("post", "/delivery-bill/create", null, null, data);
  },
  getOutBoundsForDeliveryBill : (page, limit, facilityId) => {
    return request("get", `/shipment/for-delivery?page=${page}&limit=${limit}&facilityId=${facilityId}`);
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
  autoAssignDeliveryRoutes: (deliveryPlanId, solverName) => {
    return request("get", `delivery-route/auto-assign/${deliveryPlanId}/${solverName}`, null, null, null);
  },
  getDeliveryPlanById: (deliveryPlanId) => {
    return request("get", `delivery-plan/details/${deliveryPlanId}`, null, null, null);
  },
  getVehicles : (page, limit, filters) => {
    return request("post", `/vehicle/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  createProduct : (data) => {
    return request("post", "/product/create", null, null, data);
  },
  getProductCategories : () => {
    return request("get", "/product-category/get-all");
  },
  getProductsWithFilters : (page, limit, filters) => {
    return request("post", `/product/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getProductById : (productId) => {
    return request("get", `/product/details/${productId}`);
  },
  createCustomer : (data) => {
    return request("post", "/customer/create", null, null, data);
  },
  getCustomersWithFilters : (page, limit, filters) => {
    return request("post", `/customer/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getCustomerById : (customerId) => {
    return request("get", `/customer/details/${customerId}`);
  },
  createSupplier : (data) => {
    return request("post", "/supplier/create", null, null, data);
  },
  getSuppliersWithFilters : (page, limit, filters) => {
    return request("post", `/supplier/get-all?page=${page}&limit=${limit}`, null, null, filters);
  }, 
  getSupplierById : (id) => {
    return request("get", `/supplier/details/${id}`);
  },
  createFacility : (data) => {
    return request("post", "/facility/create", null, null, data);
  },
  getFacilitiesWithFilters : (page, limit, filters) => {
    return request("post", `/facility/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getFacilityById : (facilityId) => {
    return request("get", `/facility/details/${facilityId}`);
  },
  getOrderReport : (orderType) => {
    return request("get", `/report/order/monthly?orderTypeId=${orderType}`);
  },
  getAllShipments : (page, limit, filters) => {
    return request("post", `/shipment/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getAllDeliveryRoutes : (page, limit, filters) => {
    return request("post", `/delivery-route/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getAllOrderBillItems : (page, limit, filters) => {
    return request("post", `/order-bill-item/get-all?page=${page}&limit=${limit}`, null, null, filters);
  },
  getMonthlyInventoryReport: (startDate, endDate) => {
    return request("get", `/report/inventory/monthly?startDate=${startDate}&endDate=${endDate}`);
  },

  getMonthlyFacilityReport: (facilityId, startDate, endDate) => {
    return request("get", `/report/inventory/facility/${facilityId}?startDate=${startDate}&endDate=${endDate}`);
  },
  getDeliveryDashboard : (startDate, endDate) => {
    return request("get", `/report/delivery/dashboard?startDate=${startDate}&endDate=${endDate}`);
  },
  autoAssignShipment: (orderId) => {
  return request("get", `/shipment/auto-assign-outbound/${orderId}`);
},
getInvoiceByShipmentId: (shipmentId) => {
  return request("get", `/invoice/shipment/${shipmentId}`);
},

getInvoiceById: (invoiceId) => {
  return request("get", `/invoice/${invoiceId}`);
},
  getInventoryItemByProductId(page, limit, productId) {
    return request("get", `/inventory-item/product/${productId}?page=${page}&limit=${limit}`);
  },
  getProductPrice (productId) {
    return request("get", `/product-price/${productId}`);
  },
    createProductPrice: (data) => {
        return request("post", "/product-price", null, null, data);
    },
};