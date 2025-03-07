import { request } from "api";
import { useCallback, useEffect, useState } from "react";
import { toast } from "react-toastify";

export const useRoomOccupations = (semester, selectedWeek) => {
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

  const convertSchedule = (schedule) => {
    const periodsMap = {};

    schedule.forEach((item) => {
      const { classRoom, classCode, startPeriod, endPeriod, dayIndex, crew, assigned } = item;
      const dayOffset = (dayIndex - 2) * 6;
      const start = dayOffset + startPeriod - 1;
      const duration = endPeriod - startPeriod + 1;

      if (!periodsMap[classRoom]) {
        periodsMap[classRoom] = {
          S: [],
          C: [],
          assigned: assigned || false 
        };
      }

      if (assigned) {
        periodsMap[classRoom].assigned = true;
      }

      periodsMap[classRoom][crew].push({ 
        start, 
        duration, 
        classCode,
        crew 
      });
    });

    // Convert to array and sort by assigned status (true first)
    return Object.entries(periodsMap)
      .map(([room, data]) => ({
        room,
        morningPeriods: mergePeriods(data.S),
        afternoonPeriods: mergePeriods(data.C),
        assigned: data.assigned
      }))
      .sort((a, b) => {
        // Sort assigned=true rooms first
        if (a.assigned && !b.assigned) return -1;
        if (!a.assigned && b.assigned) return 1;
        // If both have the same assigned status, maintain original order
        return 0;
      });
  };

  const fetchRoomOccupations = useCallback(() => {
    setLoading(true);
    try {
      request(
        "get",
        `/room-occupation/?semester=${semester}&weekIndex=${selectedWeek.weekIndex}`,
        (res) => {
          setData(convertSchedule(res.data));
          console.log(res.data);
        },
        (error) => {
          console.log(error);
          toast.error("Có lỗi khi tải dữ liệu sử dụng phòng");
        }
      );
    } catch (error) {
      setError(error);
    } finally {
      setLoading(false);
    }
  }, [semester, selectedWeek]);

  useEffect(() => {
    if (!semester || !selectedWeek) { 
      setData([]);
      return;
    }
    fetchRoomOccupations();
  }, [semester, selectedWeek]);

  return { loading, error, data, refresh: fetchRoomOccupations };
};
