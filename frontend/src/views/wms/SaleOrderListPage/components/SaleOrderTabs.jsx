import React from "react";
import { Tabs, Tab } from "@mui/material";
import { SALE_ORDER_TABS } from "views/wms/common/constants/constants";
const SaleOrderTabs = ({ value, onChange }) => {

  return (
    <Tabs value={value} onChange={onChange} sx={{ mt: 2 }}>
      {SALE_ORDER_TABS.map((tab) => (
        <Tab key={tab.value} label={tab.label} value={tab.value} />
      ))}
    </Tabs>
  );
};

export default SaleOrderTabs;