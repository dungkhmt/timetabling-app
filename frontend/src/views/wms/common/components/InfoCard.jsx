import React from "react";
import { Card, CardContent, Stack } from "@mui/material";
import InfoItem from "./InfoItem";

const InfoCard = ({ items }) => {
  return (
    <Card variant="outlined" sx={{ height: "100%" }}>
      <CardContent>
        <Stack spacing={1.5}>
          {items.map((item, index) => (
            <InfoItem key={index} label={item.label} value={item.value} />
          ))}
        </Stack>
      </CardContent>
    </Card>
  );
};

export default React.memo(InfoCard);