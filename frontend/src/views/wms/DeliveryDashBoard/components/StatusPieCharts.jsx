import React, { useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { Typography, Tabs, Tab, Box } from '@mui/material';

const StatusPieCharts = ({ billStatusCounts, planStatusCounts, routeStatusCounts }) => {
  const [activeTab, setActiveTab] = useState(0);
  
  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  // Generate color based on status
  const getColorByStatus = (status) => {
    switch(status) {
      case 'CREATED': return '#42a5f5';
      case 'IN_PROGRESS': return '#ff9800';
      case 'READY_FOR_DELIVERY': return '#ffc107';
      case 'COMPLETED': return '#4caf50';
      case 'CANCELLED': return '#f44336';
      case 'ASSIGNED': return '#9c27b0';
      case 'UNASSIGNED': return '#607d8b';
      default: return '#9e9e9e';
    }
  };
  
  // Convert status map to pie chart data
  const mapToPieData = (statusMap) => {
    if (!statusMap) return [];
    return Object.entries(statusMap).map(([status, count]) => ({
      name: getStatusName(status),
      value: count,
      itemStyle: {
        color: getColorByStatus(status)
      }
    }));
  };
  
  // Get status display name in Vietnamese
  const getStatusName = (status) => {
    switch(status) {
      case 'CREATED': return 'Tạo mới';
      case 'READY_FOR_DELIVERY' : return 'Sẵn sàng giao hàng';
      case 'IN_PROGRESS': return 'Đang giao';
      case 'COMPLETED': return 'Hoàn thành';
      case 'CANCELLED': return 'Đã hủy';
      case 'ASSIGNED': return 'Đã gán';
      case 'UNASSIGNED': return 'Chưa gán';
      default: return status;
    }
  };

  const getBillStatusOption = () => ({
    // title: {
    //   text: 'Trạng thái phiếu giao hàng',
    //   left: 'center'
    // },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: mapToPieData(billStatusCounts).map(item => item.name)
    },
    series: [
      {
        name: 'Phiếu giao hàng',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          formatter: '{b}: {c} ({d}%)'
        },
        data: mapToPieData(billStatusCounts)
      }
    ]
  });

  const getPlanStatusOption = () => ({
    // title: {
    //   text: 'Trạng thái kế hoạch giao hàng',
    //   left: 'center'
    // },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: mapToPieData(planStatusCounts).map(item => item.name)
    },
    series: [
      {
        name: 'Kế hoạch giao hàng',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          formatter: '{b}: {c} ({d}%)'
        },
        data: mapToPieData(planStatusCounts)
      }
    ]
  });

  const getRouteStatusOption = () => ({
    // title: {
    //   text: 'Trạng thái tuyến giao hàng',
    //   left: 'center'
    // },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: mapToPieData(routeStatusCounts).map(item => item.name)
    },
    series: [
      {
        name: 'Tuyến giao hàng',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          formatter: '{b}: {c} ({d}%)'
        },
        data: mapToPieData(routeStatusCounts)
      }
    ]
  });

  // Check if there's data to display
  const hasData = (statusMap) => {
    return statusMap && Object.keys(statusMap).length > 0;
  };

  return (
    <Box>
      <Tabs
        value={activeTab}
        onChange={handleTabChange}
        variant="fullWidth"
        indicatorColor="primary"
        textColor="primary"
        centered
      >
        <Tab label="Phiếu giao hàng" />
        <Tab label="Kế hoạch" />
        <Tab label="Tuyến đường" />
      </Tabs>
      
      {activeTab === 0 && (
        hasData(billStatusCounts) ? (
          <ReactECharts
            option={getBillStatusOption()}
            style={{ height: '350px', marginTop: '20px' }}
            notMerge={true}
            lazyUpdate={true}
          />
        ) : (
          <Typography variant="body1" align="center" py={10}>
            Không có dữ liệu trạng thái phiếu giao hàng
          </Typography>
        )
      )}
      
      {activeTab === 1 && (
        hasData(planStatusCounts) ? (
          <ReactECharts
            option={getPlanStatusOption()}
            style={{ height: '350px', marginTop: '20px' }}
            notMerge={true}
            lazyUpdate={true}
          />
        ) : (
          <Typography variant="body1" align="center" py={10}>
            Không có dữ liệu trạng thái kế hoạch giao hàng
          </Typography>
        )
      )}
      
      {activeTab === 2 && (
        hasData(routeStatusCounts) ? (
          <ReactECharts
            option={getRouteStatusOption()}
            style={{ height: '350px', marginTop: '20px' }}
            notMerge={true}
            lazyUpdate={true}
          />
        ) : (
          <Typography variant="body1" align="center" py={10}>
            Không có dữ liệu trạng thái tuyến giao hàng
          </Typography>
        )
      )}
    </Box>
  );
};

export default StatusPieCharts;