import React, { createContext, useContext } from 'react';

export const DeliveryPlanFormContext = createContext({
  deliveryPlan: {
    deliveryPlanName: '',
    description: '',
    deliveryDate: null,
    facilityId: '',
    deliveryBillIds: [],
    shipperIds: [],
    vehicleIds: [], // Add vehicleIds
  },
  setDeliveryPlan: () => {},
  entities: {
    facilities: [],
    deliveryBills: [],
    totalDeliveryBills: 0,
    selectedDeliveryBills: [],
    shippers: [],
    totalShippers: 0,
    selectedShippers: [],
    vehicles: [], // Add vehicles
    totalVehicles: 0,
    selectedVehicles: [], // Add selectedVehicles
  },
  setEntities: () => {}
});

export const DeliveryPlanFormProvider = ({ children, value }) => {
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