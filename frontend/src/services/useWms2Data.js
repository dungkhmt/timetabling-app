import { useMutation, useQuery } from 'react-query';
import { toast } from 'react-toastify';
import { wms2Service } from 'repositories/wms2Repository';

export const useWms2Data = () => {
   // Sử dụng useMutation cho việc tạo đơn hàng (đã đúng)
   const createSalesOrderMutation = useMutation(wms2Service.createSalesOrder, {
     onSuccess: (res) => {
      const { data } = res;
      console.log("Res :", res);
      if(data && data.code ===201)
       toast.success("Tạo đơn hàng thành công!");
      else 
       toast.error("Có lỗi xảy ra khi tạo đơn hàng : "+data.message??'');
     },
     onError: (error) => {
       toast.error(error.response?.data || "Có lỗi xảy ra khi tạo đơn hàng");
     },
   });

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
       return  response.data;
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
       const response = await wms2Service.searchProducts(searchText, page, limit);
       return response.data;
     } catch (error) {
       console.error("Error searching products:", error);
       toast.error("Không thể tìm kiếm sản phẩm");
       return { data: {} };
      }
    }

    const getOrderDetails = async (orderId) => {
      try {
        const response = await wms2Service.getOrderDetails(orderId);
        return response.data;
      } catch (error) {
        console.error("Error fetching order details:", error);
        toast.error("Không thể tải chi tiết đơn hàng");
        return { data: {} };
      }
    }

    const updateStatusOrder = async (orderId, status) => {
      try {
        const response = await wms2Service.updateStatusOrder(orderId, status);
        return response.data;
      } catch (error) {
        console.error("Error updating order status:", error);
        toast.error("Không thể cập nhật trạng thái đơn hàng");
        return { data: {} };
      }
    }

    const approveOrder = async (orderId) => {
      try {
        const response = await wms2Service.approveOrder(orderId);
        return response.data;
      } catch (error) {
        console.error("Error approving order:", error);
        toast.error("Không thể duyệt đơn hàng");
        return { data: {} };
      }
    }

    const rejectOrder = async (orderId) => {
      try {
        const response = await wms2Service.rejectOrder(orderId);
        return response.data;
      } catch (error) {
        console.error("Error rejecting order:", error);
        toast.error("Không thể từ chối đơn hàng");
        return { data: {} };
      }
    }

    const getSalesOrders = async (page, limit, filters) => {
      try {
        const response = await wms2Service.getSalesOrders(page, limit, filters);
        return response.data;
      } catch (error) {
        console.error("Error fetching sales orders:", error);
        toast.error("Không thể tải danh sách đơn hàng");
        return { data: {} };
      }
    }
   // Trả về các hàm thay vì dữ liệu
   return {
     createSalesOrder: createSalesOrderMutation.mutateAsync,
     getMoreFacilities,
     getMoreProducts,
     getMoreCustomers,
      searchProducts,
      getOrderDetails,
      updateStatusOrder,
      approveOrder,
      rejectOrder,
      getSalesOrders
   };
};