import React, { createContext, useContext, useState } from 'react';

const OrderFormContext = createContext();

export const OrderFormProvider = ({ children, value }) => {
  // Thêm state quản lý tìm kiếm sản phẩm
  const [productSearchState, setProductSearchState] = useState({
    query: '',
    page: 0,
    hasMore: true,
    loading: false,
    results: []
  });

  // Kết hợp value từ props với state tìm kiếm mới
  const contextValue = {
    ...value,
    productSearchState,
    setProductSearchState
  };

  return (
    <OrderFormContext.Provider value={contextValue}>
      {children}
    </OrderFormContext.Provider>
  );
};

export const useOrderForm = () => {
  const context = useContext(OrderFormContext);
  if (!context) {
    throw new Error('useOrderForm must be used within an OrderFormProvider');
  }
  return context;
};