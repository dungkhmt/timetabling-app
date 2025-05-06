import { useQuery, } from 'react-query';
import {  } from 'react-toastify';
import { examRoomService } from "repositories/examRoomRepository";

export const useExamRoomData = () => {
  
  const { data: examRooms, isLoading, error } = useQuery(
    'examRooms',
    examRoomService.getAllExamRooms,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  return {
    examRooms: examRooms?.data || [],
    isLoading,
    error,
  };
};
