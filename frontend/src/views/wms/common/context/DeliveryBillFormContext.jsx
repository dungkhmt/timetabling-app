import { createContext, useContext } from "react";

const DeliveryBillFormContext = createContext();

export const DeliveryBillFormProvider = DeliveryBillFormContext.Provider;

export const useDeliveryBillForm = () => useContext(DeliveryBillFormContext);