import React from "react";
import { Grid } from "@mui/material";
import ProductChart from "./ProductChart";

const ProductChartsGrid = ({ topImportedProducts, topExportedProducts }) => {
  return (
    <Grid container spacing={3} sx={{ mb: 3 }}>
      <Grid item xs={12} md={6}>
        <ProductChart 
          products={topImportedProducts}
          title="Top 5 sản phẩm nhập kho"
          color="#4caf50"
          emptyMessage="Không có dữ liệu nhập kho"
        />
      </Grid>
      
      <Grid item xs={12} md={6}>
        <ProductChart 
          products={topExportedProducts}
          title="Top 5 sản phẩm xuất kho"
          color="#f44336"
          emptyMessage="Không có dữ liệu xuất kho"
        />
      </Grid>
    </Grid>
  );
};

export default ProductChartsGrid;