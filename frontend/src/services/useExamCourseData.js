import { useQuery, } from 'react-query';
import { examCourseService } from "repositories/examCourseRepository";

export const useExamCourseData = () => {
  
  const { data: examCourses, isLoading, error } = useQuery(
    'examCourses',
    examCourseService.getAllExamCourses,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  return {
    examCourses: examCourses?.data || [],
    isLoading,
    error,
  };
};
