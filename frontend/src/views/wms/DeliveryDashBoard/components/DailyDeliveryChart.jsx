import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Typography } from '@mui/material';

const DailyDeliveryChart = ({ data }) => {
  if (!data || data.length === 0) {
    return (
      <Typography variant="body1" align="center" py={5}>
        Không có dữ liệu biểu đồ hàng ngày
      </Typography>
    );
  }
  
  // Extract data for chart
  const dates = data.map(item => item.formattedDate);
  const billCounts = data.map(item => item.billsCount);
  const planCounts = data.map(item => item.plansCount);
  const routeCounts = data.map(item => item.routesCount);
  
  const option = {
    title: {
      text: 'Thống kê giao hàng theo ngày',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['Phiếu giao hàng', 'Kế hoạch giao hàng', 'Tuyến giao hàng'],
      bottom: 0
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
        name: 'Phiếu giao hàng',
        type: 'bar',
        data: billCounts,
        itemStyle: {
          color: '#4caf50'
        },
        emphasis: {
          focus: 'series'
        }
      },
      {
        name: 'Kế hoạch giao hàng',
        type: 'bar',
        data: planCounts,
        itemStyle: {
          color: '#2196f3'
        },
        emphasis: {
          focus: 'series'
        }
      },
      {
        name: 'Tuyến giao hàng',
        type: 'bar',
        data: routeCounts,
        itemStyle: {
          color: '#ff9800'
        },
        emphasis: {
          focus: 'series'
        }
      }
    ]
  };
  
  return (
    <ReactECharts 
      option={option} 
      style={{ height: '400px' }} 
      notMerge={true}
      lazyUpdate={true}
    />
  );
};

export default DailyDeliveryChart;