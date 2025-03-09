import React from "react";
import { Box, Typography } from "@mui/material";

const InfoItem = ({ label, value = "-" }) => {
  return (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: "35% 65%",
        gap: "8px",
        alignItems: "center",
      }}
    >
      <Typography fontWeight="500">{label}:</Typography>
      <Typography>{value}</Typography>
    </Box>
  );
};

export default React.memo(InfoItem);