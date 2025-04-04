import React, { createContext, useContext, useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { toast } from 'react-toastify';

// Tạo context
const ApprovedOrderDetailContext = createContext();

export const ApprovedOrderDetailProvider = ({ children }) => {
  const { id } = useParams();
  const { getInBoundsOrder, getMoreInventoryItems, createInBoundOrder  } = useWms2Data();
  
  const getInBoundsOrderApi = async (id, page, limit) => {
    try {
      const res = await getInBoundsOrder(id, page, limit);
      if ( !res || res.code !== 200) {
        toast.error("Lỗi khi tải thông tin phiếu nhập : " + res?.message);
        return;
      }

      return res.data;

    } catch (error) {
      console.error("Error fetching inbound order: ", error);
      toast.error("Không thể tải thông tin phiếu nhập");
    }
  };

  const getMoreInventoryItemsApi = async (page, limit) => {
    try {
      const response = await getMoreInventoryItems(page, limit, id);
      return response.data;
    } catch (error) {
      console.error("Error fetching facilities:", error);
      toast.error("Không thể tải danh sách kho hàng");
      return { data: {} };
    }
  }

  const createInBoundOrderApi = async (data) => {
    try {
      const res = await createInBoundOrder(data);
      if(res && res.code === 201)
        toast.success("Tạo đơn hàng xuất kho thành công!");
    } catch (error) {
      console.error("Error creating out bound order:", error);
      toast.error("Không thể tạo phiếu xuất");
      return { data: {} };
    }
  }

  const value = {
    getInBoundsOrderApi,
    createInBoundOrderApi,
    getMoreInventoryItemsApi,
    // loading,
  };

  return (
    <ApprovedOrderDetailContext.Provider value={value}>
      {children}
    </ApprovedOrderDetailContext.Provider>
  );
};

export const useApprovedOrderDetail = () => {
  const context = useContext(ApprovedOrderDetailContext);
  if (!context) {
    throw new Error('useOrderDetail must be used within an OrderDetailProvider');
  }
  return context;
};