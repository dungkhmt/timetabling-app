import { request } from "api";
import { useCallback, useEffect, useState } from "react";
import { toast } from "react-toastify";

export const useRoomOccupations = (semester, selectedWeek, versionId, numberSlotsPerSession = 6) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [data, setData] = useState([]);

  const mergePeriods = (periods) => {
    if (!periods || periods.length === 0) return [];
    
    const sortedPeriods = [...periods].sort((a, b) => a.start - b.start);
    const mergedPeriods = [];
    let current = { ...sortedPeriods[0] };

    for (let i = 1; i < sortedPeriods.length; i++) {
      const period = sortedPeriods[i];
      if (period.start < current.start + current.duration) {
        const end = Math.max(
          current.start + current.duration,
          period.start + period.duration
        );
        current.duration = end - current.start;
        current.classCode += `,${period.classCode}`;
      } else {
        mergedPeriods.push(current);
        current = { ...period };
      }
    }
    mergedPeriods.push(current);
    return mergedPeriods;
  };

const convertSchedule = (schedule, numberSlotsPerSession) => {
  if (!Array.isArray(schedule) || schedule.length === 0) {
    return [];
  }

  const periodsMap = {};

  schedule.forEach((item) => {
    if (!item || !item.classRoom) return;

    const {
      classRoom,
      moduleCode,
      classCode,
      startPeriod,
      endPeriod,
      dayIndex,
      crew,
      assigned,
    } = item;

    if (!classCode || !startPeriod || !endPeriod || !dayIndex || !crew || !moduleCode) {
      return;
    }

    const dayOffset = (dayIndex - 2) * numberSlotsPerSession;
    const start = dayOffset + startPeriod - 1;
    const duration = endPeriod - startPeriod + 1;

    if (!periodsMap[classRoom]) {
      periodsMap[classRoom] = {
        S: [],
        C: [],
        assigned: false
      };
    }

    if (assigned) {
      periodsMap[classRoom].assigned = true;
    }

    // Ensure crew is either 'S' or 'C'
    if (crew === 'S' || crew === 'C') {
      periodsMap[classRoom][crew].push({
        start,
        duration,
        classCode,
        crew,
        moduleCode
      });
    }
  });

  return Object.entries(periodsMap)
    .map(([room, data]) => ({
      room,
      morningPeriods: mergePeriods(data.S),
      afternoonPeriods: mergePeriods(data.C),
      assigned: data.assigned,
    }))
    .sort((a, b) => {
      if (a.assigned && !b.assigned) return -1;
      if (!a.assigned && b.assigned) return 1;
      return 0;
    });
};
  const fetchRoomOccupations = useCallback(() => {
    if (!semester || !selectedWeek?.weekIndex) {
      return;
    }

    setLoading(true);
    setError(null);

    const versionParam = versionId ? `&versionId=${versionId}` : '';
    
    request(
      "get",
      `/room-occupation/?semester=${semester}&weekIndex=${selectedWeek.weekIndex}${versionParam}`,
      (res) => {
        try {
          console.log(res);
          const convertedData = convertSchedule(res.data, numberSlotsPerSession);
          setData(convertedData);
          console.log('Converted data:', convertedData);
        } catch (err) {
          console.error('Error converting data:', err);
          setError(err);
          toast.error("Có lỗi khi xử lý dữ liệu");
        }
      },
      (error) => {
        console.error("API error:", error);
        setError(error);
        toast.error("Có lỗi khi tải dữ liệu sử dụng phòng");
      }
    ).finally(() => {      setLoading(false);
    });
  }, [semester, selectedWeek, versionId, numberSlotsPerSession]);

  useEffect(() => {
    if (!semester || !selectedWeek) { 
      setData([]);
      return;
    }
    fetchRoomOccupations();
  }, [semester, selectedWeek, versionId, numberSlotsPerSession, fetchRoomOccupations]);

  return { loading, error, data, refresh: fetchRoomOccupations };
};
