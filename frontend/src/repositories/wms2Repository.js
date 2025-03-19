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
    return request("get", `/shipment/${shipmentId}`);
  },
  exportShipment: (shipmentId) => {
    return request("put", `/invoice/export/${shipmentId}` , null, null, null);
  }
};