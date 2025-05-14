import React from 'react';
import ReactECharts from 'echarts-for-react';
import { useTheme } from '@mui/material';
import { ORDER_TYPE_ID } from '../../common/constants/constants';

const OrderStatusPieChart = ({ reportData, orderType }) => {
  const theme = useTheme();

  // Get pie chart options for order status distribution
  const getChartOptions = () => {
    if (!reportData) return {};

    const statusColor = {
      totalApprovedOrders: '#52c41a', // green
      totalCanceledOrders: '#f5222d', // red
      totalWaitingOrders: '#faad14', // amber
      other: '#d9d9d9' // grey
    };

    const data = [
      { name: 'Đã duyệt',
        value: reportData.totalApprovedOrders,
        itemStyle: { color: statusColor.totalApprovedOrders }
      },
      { name: 'Đã hủy',
        value: reportData.totalCanceledOrders,
        itemStyle: { color: statusColor.totalCanceledOrders }
      },
      { name: 'Chờ duyệt',
        value: reportData.totalWaitingOrders,
        itemStyle: { color: statusColor.totalWaitingOrders }
      },
      {
        name: 'Khác',
        value: reportData.totalOrders - reportData.totalApprovedOrders
          - reportData.totalCanceledOrders - reportData.totalWaitingOrders,
        itemStyle: { color: statusColor.other }
      }
    ].filter(item => item.value > 0); // Only include non-zero values

    return {
      // title: {
      //   text: 'Phân bố trạng thái đơn hàng mua',
      //   left: 'center',
      //   textStyle: {
      //     color: theme.palette.text.primary
      //   },
      // },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        textStyle: {
          color: theme.palette.text.primary
        },
        data: data.map(item => item.name)
      },
      series: [
        {
          name: 'Trạng thái đơn hàng',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: theme.palette.background.paper,
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: data
        }
      ]
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

export default OrderStatusPieChart;