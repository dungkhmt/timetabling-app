import { useQuery, } from 'react-query';
import { examFacultyService } from "repositories/examFacultyRepository";

export const useExamFacultyData = () => {
  
  const { data: examFaculties, isLoading, error } = useQuery(
    'examFaculties',
    examFacultyService.getAllExamFaculties,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  return {
    examFaculties: examFaculties?.data || [],
    isLoading,
    error,
  };
};
