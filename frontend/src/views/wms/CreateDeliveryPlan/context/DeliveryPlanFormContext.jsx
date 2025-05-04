import React, { createContext, useContext } from "react";

const DeliveryPlanFormContext = createContext();

export const DeliveryPlanFormProvider = ({ value, children }) => {
  return (
    <DeliveryPlanFormContext.Provider value={value}>
      {children}
    </DeliveryPlanFormContext.Provider>
  );
};

export const useDeliveryPlanForm = () => {
  const context = useContext(DeliveryPlanFormContext);
  if (!context) {
    throw new Error("useDeliveryPlanForm must be used within a DeliveryPlanFormProvider");
  }
  return context;
};