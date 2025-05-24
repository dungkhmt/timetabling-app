import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Typography } from '@mui/material';

const ShipperPerformanceChart = ({ shippers }) => {
  if (!shippers || shippers.length === 0) {
    return (
      <Typography variant="body1" align="center" py={5}>
        Không có dữ liệu shipper
      </Typography>
    );
  }
  
  const option = {
    title: {
      text: 'Hiệu suất của shipper',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['Hoàn thành', 'Đang giao', 'Được giao'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '25%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: shippers.map(s => s.shipperName),
      axisLabel: {
        rotate: 30,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: 'Số tuyến đường'
    },
    series: [
      {
        name: 'Hoàn thành',
        type: 'bar',
        stack: 'shipper',
        emphasis: {
          focus: 'series'
        },
        data: shippers.map(s => s.completedRoutes),
        itemStyle: {
          color: '#4caf50' // Green
        }
      },
      {
        name: 'Đang giao',
        type: 'bar',
        stack: 'shipper',
        emphasis: {
          focus: 'series'
        },
        data: shippers.map(s => s.inProgressRoutes),
        itemStyle: {
          color: '#ff9800' // Orange
        }
      },
      {
        name: 'Được giao',
        type: 'bar',
        emphasis: {
          focus: 'series'
        },
        data: shippers.map(s => s.assignedRoutes),
        itemStyle: {
          color: '#2196f3', // Blue
          opacity: 0.3
        }
      }
    ]
  };
  
  return (
    <ReactECharts 
      option={option} 
      style={{ height: '350px' }} 
      notMerge={true}
      lazyUpdate={true}
    />
  );
};

export default ShipperPerformanceChart;