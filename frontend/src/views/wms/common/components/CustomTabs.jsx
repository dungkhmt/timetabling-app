import React from "react";
import { Box, Tabs, Tab, useMediaQuery, useTheme } from "@mui/material";

const CustomTabs = ({ value, onChange, labels }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  return (
    <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}>
      <Tabs
        value={value}
        onChange={onChange}
        aria-label="order details tabs"
        variant={isMobile ? "scrollable" : "standard"}
        scrollButtons={isMobile ? "auto" : false}
      >
        {labels?.map((label, index) => (
          <Tab key={index} label={label} />
        ))}
      </Tabs>
    </Box>
  );
};

export default React.memo(CustomTabs);