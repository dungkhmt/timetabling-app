import React, { createContext, useContext, useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import { useWms2Data } from 'services/useWms2Data';
import { toast } from 'react-toastify';
import { create } from '@mui/material/styles/createTransitions';

// Tạo context
const ApprovedOrderDetailContext = createContext();

export const ApprovedOrderDetailProvider = ({ children }) => {
  const { id } = useParams();
  const navigate = useHistory();
  const { getOutBoundsOrder, getMoreInventoryItems, createOutBoundOrder  } = useWms2Data();
  
  const getOutBoundsOrderApi = async (id, page, limit) => {
    try {
      const res = await getOutBoundsOrder(id, page, limit);
      if ( !res || res.code !== 200) {
        toast.error("Lỗi khi tải thông tin phiếu xuất : " + res?.message);
        return;
      }

      return res.data;
      
    } catch (error) {
      console.error("Error fetching outbound order: ", error);
      toast.error("Không thể tải thông tin phiếu xuất");
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

  const createOutBoundOrderApi = async (data) => {
    try {
      const res = await createOutBoundOrder(data);
      if(res && res.code === 201)
        toast.success("Tạo đơn hàng xuất kho thành công!");
    } catch (error) {
      console.error("Error creating out bound order:", error);
      toast.error("Không thể tạo phiếu xuất");
      return { data: {} };
    }
  }

  const value = {
    getOutBoundsOrderApi,
    createOutBoundOrder,
    createOutBoundOrderApi,
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