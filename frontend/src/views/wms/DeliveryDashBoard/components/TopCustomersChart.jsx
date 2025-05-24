import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Typography } from '@mui/material';

const TopCustomersChart = ({ customers }) => {
  if (!customers || customers.length === 0) {
    return (
      <Typography variant="body1" align="center" py={5}>
        Không có dữ liệu khách hàng
      </Typography>
    );
  }
  
  const option = {
    title: {
      text: 'Top khách hàng',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const data = params[0];
        const customerIndex = data.dataIndex;
        const customer = customers[customerIndex];
        return `
          <div style="font-weight: bold">${customer.customerName}</div>
          <div>Số đơn hàng: ${customer.deliveryCount}</div>
          <div>Tổng trọng lượng: ${customer.totalWeight.toLocaleString('vi-VN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
          })} kg</div>
        `;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: customers.map(c => c.customerName),
      axisLabel: {
        rotate: 30,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: 'Số đơn hàng'
    },
    series: [
      {
        name: 'Số đơn hàng',
        type: 'bar',
        data: customers.map(c => c.deliveryCount),
        itemStyle: {
          color: function(params) {
            const colorList = ['#2196f3', '#3f51b5', '#1976d2', '#0d47a1', '#01579b'];
            return colorList[params.dataIndex % colorList.length];
          }
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          position: 'top'
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

export default TopCustomersChart;