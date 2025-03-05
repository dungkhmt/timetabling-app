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

   // Trả về các hàm thay vì dữ liệu
   return {
     createSalesOrder: createSalesOrderMutation.mutateAsync,
     getMoreFacilities,
     getMoreProducts,
     getMoreCustomers,
      searchProducts,
   };
};