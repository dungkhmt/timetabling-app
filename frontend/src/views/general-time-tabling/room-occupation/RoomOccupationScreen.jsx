import { useState, useEffect } from "react";
import { request } from "api";
import { useRoomOccupations } from "./hooks/useRoomOccupations";
import FilterSelectBox from "./components/FilterSelectBox";
import { Button, TablePagination, Autocomplete, TextField, CircularProgress } from "@mui/material";
import { Refresh } from "@mui/icons-material";
const RoomOccupationScreen = ({ selectedSemester, setSelectedSemester }) => {
  const [selectedWeek, setSelectedWeek] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  const { data, loading, error, refresh } = useRoomOccupations(selectedSemester?.semester, selectedWeek);

  useEffect(() => {
    console.log("Current state:", {
      selectedSemester,
      selectedWeek,
      dataLength: data?.length,
      loading,
      error
    });
  }, [selectedSemester, selectedWeek, data, loading, error]);

  console.log(data, selectedSemester, selectedWeek);
  useEffect(() => {
    if (selectedSemester && selectedWeek) refresh();
  }, [selectedSemester, selectedWeek, refresh]);

  const handleExportExcel = () => {
    if (!selectedSemester || !selectedWeek) {
      return;
    }

    request(
      "post",
      `room-occupation/export?semester=${selectedSemester?.semester}&week=${selectedWeek?.weekIndex}&includeBorders=true`,
      (res) => {
        const blob = new Blob([res.data], { type: res.headers["content-type"] });
        const link = document.createElement("a");
        link.href = window.URL.createObjectURL(blob);
        link.download = "Room_Conflict_List.xlsx";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      },
      (error) => {
        console.error("Error exporting Excel:", error);
      },
      null,
      { responseType: "arraybuffer" }
    ).then();
  };

  const createSessionCells = (periods) => {
    const cells = Array(42).fill(null);
    if (!periods) return cells;
    
    periods.forEach(({ start, duration, classCode, moduleCode }) => {
      if (start < 0 || start >= 42) return;
      cells[start] = { colSpan: duration, classCode, moduleCode };
      for (let i = 1; i < duration; i++) {
        if (start + i < 42) {
          cells[start + i] = { hidden: true };
        }
      }
    });
    return cells;
  };

  const renderCell = (cell, index) => {
    if (cell === null) return (
      <td 
        key={index} 
        className="cell border border-slate-200"
        style={{ 
          width: '40px', 
          minWidth: '40px', 
          maxWidth: '40px',
          borderLeft: index % 6 === 0 ? '2px solid rgb(148 163 184)' : '1px solid rgb(226 232 240)'
        }}
      />
    );
    if (cell.hidden) return null;

    const day = Math.floor(index / 6) + 2;
    const dayText = day === 8 ? 'CN' : `Thứ ${day}`;
    const periodStart = (index % 6) + 1;
    const periodEnd = ((index + cell.colSpan - 1) % 6) + 1;
    const title = `${cell.classCode} - ${cell.moduleCode}: ${dayText}/${periodStart}-${periodEnd}`;
    
    const baseWidth = 40;
    const width = baseWidth * cell.colSpan;

    return (
      <td
        key={index}
        title={title}
        colSpan={cell.colSpan}
        style={{
          width: `${width}px`,
          minWidth: `${width}px`,
          maxWidth: `${width}px`,
          borderLeft: index % 6 === 0 ? '2px solid rgb(148 163 184)' : undefined
        }}
        className="cell bg-yellow-300 text-center overflow-hidden text-xs border border-slate-100"
      >
        <div className="flex flex-col items-center justify-center h-full">
          <span>{cell.classCode}</span>
          <span className="text-[10px] text-gray-700">{cell.moduleCode}</span>
        </div>
      </td>
    );
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <div className="flex flex-col gap-2 h-full mt-2">
      <div className="flex flex-row justify-between">
        <div className="flex flex-row gap-2">
          <Autocomplete
            disabled={true}
            value={selectedSemester}
            options={[selectedSemester].filter(Boolean)}
            getOptionLabel={(option) => option && option.semester}
            size="small"
            sx={{
              "& .MuiInputBase-root": {
                width: "130px",
              },
            }}
            renderInput={(params) => <TextField {...params} label="Chọn kỳ" />}
          />
          <FilterSelectBox
            selectedSemester={selectedSemester}
            selectedWeek={selectedWeek}
            setSelectedWeek={setSelectedWeek}
            sx={{
              "& .MuiInputBase-root": {
                height: "40px",
              },
            }}
          />
        </div>
        <div className="flex flex-row gap-2">
          <Button
            disabled={!selectedSemester}
            variant="contained"
            onClick={handleExportExcel}
          >
            Xuất File Excel
          </Button>
          <Button
            disabled={!selectedSemester || selectedWeek == null}
            variant="contained"
            onClick={refresh}
          >
            <Refresh />
          </Button>
        </div>
      </div>
      <div className="overflow-auto flex-grow border rounded-lg" style={{ height: "calc(100vh - 140px)" }}>
        {loading ? (
          <div className="flex items-center justify-center h-full">
            <CircularProgress />
          </div>
        ) : error ? (
          <div className="flex items-center justify-center h-full text-red-500">
            Có lỗi xảy ra khi tải dữ liệu
          </div>
        ) : data.length === 0 ? (
          <div className="flex items-center justify-center h-full text-gray-500">
            {selectedSemester && selectedWeek 
              ? "Không có dữ liệu cho tuần này"
              : "Vui lòng chọn kỳ học và tuần"}
          </div>
        ) : (
          <table className="border-collapse border border-slate-300 w-full relative">
            <thead>
              <tr>
                <th
                  className="border-2 border-slate-300 bg-white sticky top-0 left-0 z-30"
                  rowSpan="2"
                  colSpan="2"
                  style={{ 
                    minWidth: "150px",
                    borderRight: '2px solid rgb(148 163 184)'
                  }}
                >
                  Phòng học/ Thời gian
                </th>
                {Array.from({ length: 7 }).map((_, index) => (
                  <th
                    key={index}
                    colSpan="6"
                    className="cell text-center border-2 border-slate-300 bg-white sticky top-0 z-20"
                    style={{ 
                      width: "240px", 
                      minWidth: "240px",
                      borderLeft: '2px solid rgb(148 163 184)',
                      borderRight: '2px solid rgb(148 163 184)'
                    }}
                  >
                    {index + 2 === 8 ? "CN" : `Thứ ${index + 2}`}
                  </th>
                ))}
              </tr>
              <tr>
                {Array.from({ length: 42 }).map((_, index) => (
                  <th
                    key={index}
                    className="cell border border-slate-300 text-center bg-white sticky top-10 z-20"
                    style={{ 
                      width: "40px", 
                      minWidth: "40px", 
                      maxWidth: "40px",
                      borderLeft: index % 6 === 0 ? '2px solid rgb(148 163 184)' : '1px solid rgb(226 232 240)'
                    }}
                  >
                    {(index % 6) + 1}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((roomData, index) => (
                  <tr key={roomData.room} className="border-t-2 border-slate-300">
                    <th className="border border-slate-300" rowSpan="1">
                      {roomData.room}
                    </th>
                    <th className="border border-slate-300">
                      <div className="flex flex-col">
                        <div className="border-b border-slate-300 p-1">S</div>
                        <div className="p-1">C</div>
                      </div>
                    </th>
                    <td colSpan="42" className="p-0 border border-slate-300">
                      <div className="flex flex-col">
                        <div className="border-b border-slate-300 flex">
                          {createSessionCells(roomData.morningPeriods).map(
                            (cell, index) => renderCell(cell, index)
                          )}
                        </div>
                        <div className="flex">
                          {createSessionCells(roomData.afternoonPeriods).map(
                            (cell, index) => renderCell(cell, index)
                          )}
                        </div>
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        )}
      </div>

      <TablePagination
        component="div"
        count={data.length}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[25, 50, 100]}
        labelRowsPerPage="Số hàng mỗi trang:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}-${to} trên ${count}`
        }
        className="border-t border-slate-300"
      />
    </div>
  );
};

export default RoomOccupationScreen;
