import { useQuery, useMutation, useQueryClient } from 'react-query';
import { toast } from 'react-toastify';
import { academicWeekServices } from "repositories/academicWeekRepository";

export const useAcademicWeeks = (selectedSemester) => {
  const queryClient = useQueryClient();

  const { data, isLoading, error, refetch } = useQuery(
    ['academicWeeks', selectedSemester],
    () => academicWeekServices.getAcademicWeeks(selectedSemester),
    {
      enabled: !!selectedSemester,
      staleTime: 5 * 60 * 1000,
      cacheTime: 30 * 60 * 1000
    }
  );

  const deleteMutation = useMutation(academicWeekServices.deleteAcademicWeeks, {
    onSuccess: () => {
      queryClient.invalidateQueries(['academicWeeks', selectedSemester]);
      toast.success('Xóa danh sách tuần học thành công!');
    },
    onError: () => {
      toast.error('Có lỗi khi xóa danh sách tuần học!');
    }
  });

  const createMutation = useMutation(academicWeekServices.createAcademicWeeks, {
    onSuccess: async (_, variables) => {
      queryClient.invalidateQueries(['academicWeeks', variables.semester]);
      toast.success('Tạo danh sách tuần thành công!');
    },
    onError: () => {
      toast.error('Lỗi, tạo danh sách tuần thất bại!');
    }
  });

  return {
    weeks: data?.data || [],
    isLoading,
    error,
    deleteWeeks: deleteMutation.mutateAsync,
    createWeeks: createMutation.mutateAsync,
    isDeleting: deleteMutation.isLoading,
    isCreating: createMutation.isLoading,
    refetch
  };
};
