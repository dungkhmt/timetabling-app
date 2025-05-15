import { useMutation } from "react-query";
import { toast } from "react-toastify";
import { wms2Service } from "repositories/wms2Repository";
import { useHistory } from "react-router-dom"; // React Router v5
export const useWms2Data = () => {
  const history = useHistory(); // Use useHistory from React Router v5
  // Sử dụng useMutation cho việc tạo đơn hàng (đã đúng)
  const createSalesOrderMutation = useMutation(wms2Service.createSalesOrder, {
    onSuccess: (res) => {
      const { data } = res;
      console.log("Res :", res);
      if (data && data.code === 201) toast.success("Tạo đơn hàng thành công!");
      else
        toast.error("Có lỗi xảy ra khi tạo đơn hàng : " + data.message ?? "");
    },
    onError: (error) => {
      toast.error(error.response?.data || "Có lỗi xảy ra khi tạo đơn hàng");
    },
  });

  const createPurchaseOrderMutation = useMutation(
    wms2Service.createPurchaseOrder,
    {
      onSuccess: (res) => {
        const { data } = res;
        console.log("Res :", res);
        if (data && data.code === 201) {
          toast.success("Tạo đơn hàng thành công!");
          history.push("/wms/purchase/orders"); // Redirect on success
        } else
          toast.error("Có lỗi xảy ra khi tạo đơn hàng : " + data.message ?? "");
      },
      onError: (error) => {
        toast.error(error.response?.data || "Có lỗi xảy ra khi tạo đơn hàng");
      },
    }
  );

  // Tạo các hàm fetch data với tham số động thay vì useQuery
  const getMoreFacilities = async (page, limit) => {
    try {
      const response = await wms2Service.getMoreFacilities(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching facilities:", error);
      toast.error("Không thể tải danh sách kho hàng");
      return { data: {} };
    }
  };

  const getMoreProducts = async (page, limit) => {
    try {
      const response = await wms2Service.getMoreProducts(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching products:", error);
      toast.error("Không thể tải danh sách sản phẩm");
      return { data: {} };
    }
  };

  const getMoreCustomers = async (page, limit) => {
    try {
      const response = await wms2Service.getMoreCustomers(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching customers:", error);
      toast.error("Không thể tải danh sách khách hàng");
      return { data: {} };
    }
  };

  const searchProducts = async (searchText, page, limit) => {
    try {
      const response = await wms2Service.searchProducts(
        searchText,
        page,
        limit
      );
      return response.data;
    } catch (error) {
      console.error("Error searching products:", error);
      toast.error("Không thể tìm kiếm sản phẩm");
      return { data: {} };
    }
  };

  const getOrderDetails = async (orderId) => {
    try {
      const response = await wms2Service.getOrderDetails(orderId);
      return response.data;
    } catch (error) {
      console.error("Error fetching order details:", error);
      toast.error("Không thể tải chi tiết đơn hàng");
      return { data: {} };
    }
  };

  const updateStatusOrder = async (orderId, status) => {
    try {
      const response = await wms2Service.updateStatusOrder(orderId, status);
      return response.data;
    } catch (error) {
      console.error("Error updating order status:", error);
      toast.error("Không thể cập nhật trạng thái đơn hàng");
      return { data: {} };
    }
  };

  const approveOrder = async (orderId) => {
    try {
      const response = await wms2Service.approveOrder(orderId);
      return response.data;
    } catch (error) {
      console.error("Error approving order:", error);
      toast.error("Không thể duyệt đơn hàng");
      return { data: {} };
    }
  };

  const rejectOrder = async (orderId) => {
    try {
      const response = await wms2Service.rejectOrder(orderId);
      return response.data;
    } catch (error) {
      console.error("Error rejecting order:", error);
      toast.error("Không thể từ chối đơn hàng");
      return { data: {} };
    }
  };

  const getSalesOrders = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getSalesOrders(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching sales orders:", error);
      toast.error("Không thể tải danh sách đơn hàng");
      return { data: {} };
    }
  };

  const getSalesOrdersApproved = async (page, limit) => {
    try {
      const response = await wms2Service.getSalesOrdersApproved(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching approved sales orders:", error);
      toast.error("Không thể tải danh sách đơn hàng đã duyệt");
      return { data: {} };
    }
  };

  const getOutBoundsOrder = async (id, page, limit) => {
    try {
      const response = await wms2Service.getOutBoundsOrder(id, page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching order details:", error);
      toast.error("Không thể tải chi tiết đơn hàng");
      return { data: {} };
    }
  };

  const createOutBoundOrder = async (data) => {
    try {
      const res = await wms2Service.createOutBoundOrder(data);
    } catch (error) {
      console.error("Error creating out bound order:", error);
      toast.error("Không thể tạo đơn hàng xuất kho");
      return { data: {} };
    }
  };

  const getMoreInventoryItems = async (page, limit, orderId) => {
    try {
      const response = await wms2Service.getMoreInventoryItems(
        page,
        limit,
        orderId
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching facilities:", error);
      toast.error("Không thể tải danh sách kho hàng");
      return { data: {} };
    }
  };

  const getOutBoundDetail = async (shipmentId) => {
    try {
      const response = await wms2Service.getOutBoundDetail(shipmentId);
      return response.data;
    } catch (error) {
      console.error("Error fetching outbound detail:", error);
      toast.error("Không thể tải thông tin phiếu xuất");
      return { data: {} };
    }
  };

  const exportOutBoundShipment = async (shipmentId) => {
    try {
      const res = await wms2Service.exportOutBoundShipment(shipmentId);
      console.log(res);
      if (res && res.code === 200) toast.success("Xuất phiếu xuất thành công!");
    } catch (error) {
      console.error("Failed to export shipment:", error);
      toast.error("Không thể xuất phiếu xuất");
    }
  };

  const getSalesOrdersForExport = async (page, limit) => {
    try {
      const response = await wms2Service.getSalesOrdersForExport(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching sales orders for export:", error);
      toast.error("Không thể tải danh sách đơn hàng");
      return { data: {} };
    }
  };

  const getMoreSuppliers = async (page, limit) => {
    try {
      const response = await wms2Service.getMoreSuppliers(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching suppliers:", error);
      toast.error("Không thể tải danh sách nhà cung cấp");
      return { data: {} };
    }
  };

  const getPurchaseOrders = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getPurchaseOrders(
        page,
        limit,
        filters
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching purchase orders:", error);
      toast.error("Không thể tải danh sách đơn hàng mua");
      return { data: {} };
    }
  };

  const getPurchaseOrdersForExport = async (page, limit) => {
    try {
      const response = await wms2Service.getPurchaseOrdersForExport(
        page,
        limit
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching purchase orders for export:", error);
      toast.error("Không thể tải dữ liệu xuất file");
      return { data: {} };
    }
  };

  const getPurchaseOrderDetails = async (orderId) => {
    try {
      const response = await wms2Service.getPurchaseOrderDetails(orderId);
      return response.data;
    } catch (error) {
      console.error("Error fetching purchase order details:", error);
      toast.error("Không thể tải chi tiết đơn hàng mua");
      return { data: {} };
    }
  };

  const updatePurchaseOrderStatus = async (orderId, status) => {
    try {
      const response = await wms2Service.updatePurchaseOrderStatus(
        orderId,
        status
      );
      return response.data;
    } catch (error) {
      console.error("Error updating purchase order status:", error);
      toast.error("Không thể cập nhật trạng thái đơn hàng mua");
      return { data: {} };
    }
  };

  const approvePurchaseOrder = async (orderId) => {
    try {
      const response = await wms2Service.approvePurchaseOrder(orderId);
      return response.data;
    } catch (error) {
      console.error("Error approving purchase order:", error);
      toast.error("Không thể duyệt đơn hàng mua");
      return { data: {} };
    }
  };

  const createInBoundOrder = async (data) => {
    try {
      const res = await wms2Service.createInBoundOrder(data);
      return res.data;
    } catch (error) {
      console.error("Error creating inbound order:", error);
      toast.error("Không thể tạo phiếu nhập");
      return { data: {} };
    }
  };

  const getInBoundsOrder = async (id, page, limit) => {
    try {
      const response = await wms2Service.getInBoundsOrder(id, page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching inbound order:", error);
      toast.error("Không thể tải thông tin phiếu nhập");
      return { data: {} };
    }
  };

  const getInBoundDetail = async (shipmentId) => {
    try {
      const response = await wms2Service.getInBoundDetail(shipmentId);
      return response.data;
    } catch (error) {
      console.error("Error fetching inbound detail:", error);
      toast.error("Không thể tải thông tin phiếu nhập");
      return { data: {} };
    }
  };

  const exportInBoundShipment = async (shipmentId) => {
    try {
      const res = await wms2Service.exportInBoundShipment(shipmentId);
      console.log(res);
      if (res && res.code === 200) toast.success("Nhập vào kho thành công!");
    } catch (error) {
      console.error("Failed to export shipment:", error);
      toast.error("Không thể xuất phiếu nhập");
    }
  };

  const getLowStockForecast = async () => {
    try {
      const response = await wms2Service.getLowStockForecast();
      return response.data;
    } catch (error) {
      console.error("Error in getLowStockForecast:", error);
      throw error;
    }
  };

  const createDeliveryBill = async (data) => {
    try {
      const res = await wms2Service.createDeliveryBill(data);
      return res.data;
    } catch (error) {
      console.error("Error creating delivery bill:", error);
      toast.error("Không thể tạo phiếu giao hàng");
      return { data: {} };
    }
  };

  const getOutBoundsForDeliveryBill = async (page, limit) => {
    try {
      const response = await wms2Service.getOutBoundsForDeliveryBill(page, limit);
      return response.data;
    } catch (error) {
      console.error("Error fetching outbound order:", error);
      toast.error("Không thể tải thông tin phiếu giao hàng");
      return { data: {} };
    }
  };

  const getDeliveryBills = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getDeliveryBills(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching delivery bills:", error);
      toast.error("Không thể tải danh sách phiếu giao hàng");
      return { data: {} };
    }
  };

  const getDeliveryPlans = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getDeliveryPlans(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching delivery plans:", error);
      toast.error("Không thể tải danh sách kế hoạch giao hàng");
      return { data: {} };
    }
  };

  const getShippers = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getShippers(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching shippers:", error);
      toast.error("Không thể tải danh sách nhà vận chuyển");
      return { data: {} };
    }
  };

  const createDeliveryPlan = async (data) => {
    try {
      const res = await wms2Service.createDeliveryPlan(data);
      return res.data;
    } catch (error) {
      console.error("Error creating delivery plan:", error);
      toast.error("Không thể tạo kế hoạch giao hàng");
      return { data: {} };
    }
  };

  const getDeliveryPlanById = async (id) => {
    try {
      const response = await wms2Service.getDeliveryPlanById(id);
      return response.data;
    } catch (error) {
      console.error("Error fetching delivery plan by ID:", error);
      toast.error("Không thể tải thông tin kế hoạch giao hàng");
      return { data: {} };
    }
  };

  const autoAssignDeliveryRoutes = async (deliveryPlanId) => {
    try {
      const response = await wms2Service.autoAssignDeliveryRoutes(
        deliveryPlanId
      );
      return response.data;
    } catch (error) {
      console.error("Error auto-assigning delivery routes:", error);
      toast.error("Không thể tự động phân bổ lộ trình giao hàng");
      return { data: {} };
    }
  }

  const getVehicles = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getVehicles(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching vehicles:", error);
      toast.error("Không thể tải danh sách xe");
      return { data: {} };
    }
  };

  const createProduct = async (data) => {
    try {
      const res = await wms2Service.createProduct(data);
      return res.data;
    } catch (error) {
      console.error("Error creating product:", error);
      toast.error("Không thể tạo sản phẩm");
      return { data: {} };
    }
  }

  const getProductCategories = async () => {
    try {
      const response = await wms2Service.getProductCategories();
      return response.data;
    } catch (error) {
      console.error("Error fetching product categories:", error);
      toast.error("Không thể tải danh mục sản phẩm");
      return { data: {} };
    }
  };

  const getProductsWithFilters = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getProductsWithFilters(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching products with filters:", error);
      toast.error("Không thể tải danh sách sản phẩm");
      return { data: {} };
    }
  };

  const getProductById = async (id) => {
    try {
      const response = await wms2Service.getProductById(id);
      return response.data;
    } catch (error) {
      console.error("Error fetching product by ID:", error);
      toast.error("Không thể tải thông tin sản phẩm");
      return { data: {} };
    }
  };

  const createCustomer = async (data) => {
    try {
      const res = await wms2Service.createCustomer(data);
      return res.data;
    } catch (error) {
      console.error("Error creating customer:", error);
      toast.error("Không thể tạo khách hàng");
      return { data: {} };
    }
  }

  const getCustomersWithFilters = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getCustomersWithFilters(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching customers with filters:", error);
      toast.error("Không thể tải danh sách khách hàng");
      return { data: {} };
    }
  };

  const getCustomerById = async (id) => {
    try {
      const response = await wms2Service.getCustomerById(id);
      return response.data;
    } catch (error) {
      console.error("Error fetching customer by ID:", error);
      toast.error("Không thể tải thông tin khách hàng");
      return { data: {} };
    }
  }

  const createSupplier = async (data) => {
    try {
      const res = await wms2Service.createSupplier(data);
      return res.data;
    } catch (error) {
      console.error("Error creating supplier:", error);
      toast.error("Không thể tạo nhà cung cấp");
      return { data: {} };
    }
  }

  const getSuppliersWithFilters = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getSuppliersWithFilters(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching suppliers with filters:", error);
      toast.error("Không thể tải danh sách nhà cung cấp");
      return { data: {} };
    }
  }

  const getSupplierById = async (id) => {
    try {
      const response = await wms2Service.getSupplierById(id);
      return response.data;
    } catch (error) {
      console.error("Error fetching supplier by ID:", error);
      toast.error("Không thể tải thông tin nhà cung cấp");
      return { data: {} };
    }
  }

  const createFacility = async (data) => {
    try {
      const res = await wms2Service.createFacility(data);
      return res.data;
    } catch (error) {
      console.error("Error creating facility:", error);
      toast.error("Không thể tạo kho hàng");
      return { data: {} };
    }
  }

  const getFacilitiesWithFilters = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getFacilitiesWithFilters(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching facilities with filters:", error);
      toast.error("Không thể tải danh sách kho hàng");
      return { data: {} };
    }
  }

  const getFacilityById = async (id) => {
    try {
      const response = await wms2Service.getFacilityById(id);
      return response.data;
    } catch (error) {
      console.error("Error fetching facility by ID:", error);
      toast.error("Không thể tải thông tin kho hàng");
      return { data: {} };
    }
  }

  const getOrderReport = async (orderTypeId) => {
    try {
      const response = await wms2Service.getOrderReport(orderTypeId);
      return response.data;
    } catch (error) {
      console.error("Error fetching order report:", error);
      toast.error("Không thể tải báo cáo đơn hàng");
      return { data: {} };
    }
  };

  const getAllShipments = async (page, limit, filters) => {
    try {
      const response = await wms2Service.getAllShipments(page, limit, filters);
      return response.data;
    } catch (error) {
      console.error("Error fetching all shipments:", error);
      toast.error("Không thể tải danh sách phiếu xuất");
      return { data: {} };
    }
  }


  // Trả về các hàm thay vì dữ liệu
  return {
    createSalesOrder: createSalesOrderMutation.mutateAsync,
    createPurchaseOrder: createPurchaseOrderMutation.mutateAsync,
    getMoreFacilities,
    getMoreProducts,
    getMoreCustomers,
    searchProducts,
    getOrderDetails,
    updateStatusOrder,
    approveOrder,
    rejectOrder,
    getSalesOrders,
    getSalesOrdersApproved,
    createOutBoundOrder,
    getMoreInventoryItems,
    getOutBoundsOrder,
    getOutBoundDetail,
    exportOutBoundShipment,
    getSalesOrdersForExport,
    getMoreSuppliers,
    getPurchaseOrders,
    getPurchaseOrdersForExport,
    getPurchaseOrderDetails,
    updatePurchaseOrderStatus,
    approvePurchaseOrder,
    createInBoundOrder,
    getInBoundsOrder,
    getInBoundDetail,
    exportInBoundShipment,
    getLowStockForecast,
    createDeliveryBill,
    getOutBoundsForDeliveryBill,
    getDeliveryBills,
    getDeliveryPlans,
    getDeliveryPlanById,
    autoAssignDeliveryRoutes,
    getShippers,
    createDeliveryPlan,
    getVehicles,
    createProduct,
    getProductCategories,
    getProductsWithFilters,
    getProductById,
    createCustomer,
    getCustomersWithFilters,
    getCustomerById,
    createSupplier,
    getSuppliersWithFilters,
    getSupplierById,
    createFacility,
    getFacilitiesWithFilters,
    getFacilityById,
    getOrderReport,
    getAllShipments,
  };
};
