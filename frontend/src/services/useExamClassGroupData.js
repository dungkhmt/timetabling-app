import { useQuery, } from 'react-query';
import { examClassGroupService } from "repositories/examClassGroupRepository";

export const useExamClassGroupData = () => {
  
  const { data: examClassGroups, isLoading, error } = useQuery(
    'examClassGroups',
    examClassGroupService.getAllExamClassGroups,
    {
      staleTime: 5 * 60 * 1000, // Cache for 5 minutes
      cacheTime: 30 * 60 * 1000, // Keep cache for 30 minutes
    }
  );

  return {
    examClassGroups: examClassGroups?.data || [],
    isLoading,
    error,
  };
};
