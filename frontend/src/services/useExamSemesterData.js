import { useQuery, } from 'react-query';
import { examSemesterService } from "repositories/examSemesterRepository";

export const useExamSemesterData = () => {
  
  const { data: examSemesters, isLoading, error } = useQuery(
    'examSemesters',
    examSemesterService.getAllExamSemesters,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  return {
    examSemesters: examSemesters?.data || [],
    isLoading,
    error,
  };
};
