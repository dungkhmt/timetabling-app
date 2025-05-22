import React from "react";
import { Box, Paper, Typography } from "@mui/material";
import ReactECharts from 'echarts-for-react';

const DailyMovementChart = ({ dailyMovements }) => {
  const getChartOption = () => {
    if (!dailyMovements?.length) return null;
    
    const dates = dailyMovements.map(item => item.date);
    const importData = dailyMovements.map(item => item.importQuantity);
    const exportData = dailyMovements.map(item => item.exportQuantity);
    
    return {
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const date = params[0].axisValue;
          let result = `<div style="font-weight: bold">${date}</div>`;
          
          params.forEach(param => {
            const color = param.seriesName === 'Nhập kho' ? '#4caf50' : '#f44336';
            result += `
              <div style="display: flex; justify-content: space-between; align-items: center; margin: 5px 0;">
                <span style="display: inline-block; margin-right: 5px; width: 10px; height: 10px; border-radius: 50%; background-color: ${color};"></span>
                <span>${param.seriesName}: </span>
                <span style="font-weight: bold; margin-left: 15px">${param.value.toLocaleString()} sản phẩm</span>
              </div>`;
          });
          
          return result;
        }
      },
      legend: {
        data: ['Nhập kho', 'Xuất kho']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          rotate: 45,
          interval: Math.max(Math.floor(dates.length / 15), 0)
        }
      },
      yAxis: {
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
    <Paper elevation={1} sx={{ p: 3, mb: 3 }}>
      <Typography variant="h6" fontWeight="bold" gutterBottom>
        Biểu đồ nhập xuất hàng theo ngày
      </Typography>
      {dailyMovements?.length > 0 ? (
        <ReactECharts 
          option={getChartOption()} 
          style={{ height: '400px' }}
          notMerge={true}
        />
      ) : (
        <Box display="flex" justifyContent="center" alignItems="center" height={200}>
          <Typography>Không có dữ liệu</Typography>
        </Box>
      )}
    </Paper>
  );
};

export default DailyMovementChart;