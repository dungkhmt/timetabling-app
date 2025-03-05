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
  }
};