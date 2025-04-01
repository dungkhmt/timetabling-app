import React from "react";
import { Tabs, Tab } from "@mui/material";

const OrderTabs = ({ value, onChange, tabs }) => {
  return (
    <Tabs value={value} onChange={onChange} sx={{ mt: 2 }}>
      {tabs.map((tab) => (
        <Tab key={tab.value} label={tab.label} value={tab.value} />
      ))}
    </Tabs>
  );
};

export default OrderTabs;