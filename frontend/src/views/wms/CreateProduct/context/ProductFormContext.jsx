import React, { createContext, useContext } from 'react';

const ProductFormContext = createContext({
  product: {},
  setProduct: () => {},
  entities: {},
  setEntities: () => {}
});

export const ProductFormProvider = ({ children, value }) => {
  return (
    <ProductFormContext.Provider value={value}>
      {children}
    </ProductFormContext.Provider>
  );
};

export const useProductForm = () => {
  return useContext(ProductFormContext);
};