export const COLUMNS = [
  {
    headerName: "Tên kíp thi",
    field: "name",
    width: 250,
    headerAlign: 'center',
    align: 'center',
  },
  {
    headerName: "Thời gian bắt đầu",
    field: "startTime",
    width: 200,
    headerAlign: 'center',
    align: 'center',
    valueFormatter: (params) => {
      if (!params.value) return '';
      const date = new Date(params.value);
      return date.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      });
    }
  },
  {
    headerName: "Thời gian kết thúc",
    field: "endTime",
    width: 200,
    headerAlign: 'center',
    align: 'center',
    valueFormatter: (params) => {
      if (!params.value) return '';
      const date = new Date(params.value);
      return date.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      });
    }
  },
  {
    headerName: "Tên hiển thị",
    field: "displayName",
    width: 300,
    headerAlign: 'center',
    align: 'center',
  },
];
