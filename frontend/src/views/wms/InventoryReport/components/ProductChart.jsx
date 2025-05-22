import React from "react";
import { Box, Paper, Typography } from "@mui/material";
import ReactECharts from 'echarts-for-react';

const ProductChart = ({ products, title, color, emptyMessage }) => {
  const getChartOption = () => {
    if (!products?.length) return null;
    
    const productNames = products.map(item => item.productName);
    const quantities = products.map(item => item.quantity);
    
    return {
      title: {
        text: title,
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          return `<div>
            <div style="font-weight: bold">${params[0].name}</div>
            <div style="display: flex; align-items: center; margin-top: 5px">
              <span style="display: inline-block; margin-right: 5px; width: 10px; height: 10px; background-color: ${color};"></span>
              <span>Số lượng: </span>
              <span style="font-weight: bold; margin-left: 5px">${params[0].value.toLocaleString()}</span>
            </div>
          </div>`;
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: productNames,
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: quantities,
          type: 'bar',
          itemStyle: { color: color }
        }
      ]
    };
  };
  
  return (
    <Paper elevation={1} sx={{ p: 3, height: '100%' }}>
      {products?.length > 0 ? (
        <ReactECharts
          option={getChartOption()}
          style={{ height: '350px' }}
          notMerge={true}
        />
      ) : (
        <Box display="flex" justifyContent="center" alignItems="center" height="100%">
          <Typography>{emptyMessage}</Typography>
        </Box>
      )}
    </Paper>
  );
};

export default ProductChart;