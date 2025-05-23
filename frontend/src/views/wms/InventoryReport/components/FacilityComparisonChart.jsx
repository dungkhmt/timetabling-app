import React from "react";
import { Paper, Typography } from "@mui/material";
import ReactECharts from 'echarts-for-react';

const FacilityComparisonChart = ({ facilityMovements }) => {
  const getChartOption = () => {
    if (!facilityMovements?.length) return null;
    
    const facilityNames = facilityMovements.map(item => item.facilityName);
    const importData = facilityMovements.map(item => item.importQuantity);
    const exportData = facilityMovements.map(item => item.exportQuantity);
    
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['Nhập kho', 'Xuất kho']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      yAxis: {
        type: 'category',
        data: facilityNames
      },
      xAxis: {
        type: 'value',
        name: 'Số lượng'
      },
      series: [
        {
          name: 'Nhập kho',
          type: 'bar',
          stack: 'total',
          emphasis: { focus: 'series' },
          itemStyle: { color: '#4caf50' },
          data: importData
        },
        {
          name: 'Xuất kho',
          type: 'bar',
          stack: 'total',
          emphasis: { focus: 'series' },
          itemStyle: { color: '#f44336' },
          data: exportData
        }
      ]
    };
  };
  
  return (
    <Paper elevation={1} sx={{ p: 3 }}>
      <Typography variant="h6" fontWeight="bold" gutterBottom>
        Biểu đồ nhập xuất theo kho
      </Typography>
      <ReactECharts
        option={getChartOption()}
        style={{ height: '500px' }}
        notMerge={true}
      />
    </Paper>
  );
};

export default FacilityComparisonChart;