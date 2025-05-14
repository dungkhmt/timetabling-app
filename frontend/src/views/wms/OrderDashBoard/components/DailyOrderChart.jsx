import React from 'react';
import ReactECharts from 'echarts-for-react';
import { useTheme } from '@mui/material';
import { ORDER_TYPE_ID } from '../../common/constants/constants';

const DailyOrderChart = ({ dailyOrderCounts, orderType }) => {
  const theme = useTheme();

  // Get chart options for daily order counts
  const getChartOptions = () => {
    if (!dailyOrderCounts) return {};

    const dates = dailyOrderCounts.map(item => item.formattedDate);
    const counts = dailyOrderCounts.map(item => item.count);

    return {
      title: {
        text: orderType === ORDER_TYPE_ID.PURCHASE_ORDER 
          ? 'Đơn hàng mua theo ngày' 
          : 'Đơn hàng bán theo ngày',
        left: 'center',
        textStyle: {
          color: theme.palette.text.primary
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          interval: 2,
          rotate: 45,
          color: theme.palette.text.secondary
        }
      },
      yAxis: {
        type: 'value',
        name: 'Số lượng đơn',
        axisLabel: {
          color: theme.palette.text.secondary
        }
      },
      series: [{
        name: 'Số đơn hàng',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: orderType === ORDER_TYPE_ID.PURCHASE_ORDER 
            ? theme.palette.primary.main 
            : theme.palette.secondary.main
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    };
  };

  return (
    <ReactECharts 
      option={getChartOptions()} 
      style={{ height: '400px', width: '100%' }}
      opts={{ renderer: 'canvas' }}
    />
  );
};

export default DailyOrderChart;