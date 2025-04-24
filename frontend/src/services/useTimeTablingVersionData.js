import { useState, useCallback, useEffect } from "react";
import { toast } from "react-toastify";
import { timeTablingVersionRepository } from "repositories/timeTablingVersionRepository";

export const useTimeTablingVersionData = (initialSemester = null) => {
  const [versions, setVersions] = useState([]);
  const [selectedVersion, setSelectedVersion] = useState(null);
  const [selectedSemester, setSelectedSemester] = useState(initialSemester);
  const [searchName, setSearchName] = useState("");
  
  const [isLoading, setIsLoading] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  
  // Add a dependency to re-fetch when filters change
  const fetchVersions = useCallback(async () => {
    setIsLoading(true);
    try {
      // Use the semester code from the object if it exists
      const semesterCode = selectedSemester?.semester || "";
      
      const data = await timeTablingVersionRepository.getVersions(
        semesterCode,
        searchName
      );
      
      setVersions(data || []);
      console.log(`Fetched ${data?.length || 0} versions for semester: '${semesterCode}', searchName: '${searchName}'`);
    } catch (error) {
      console.error("Error fetching versions:", error);
      toast.error("Không thể tải danh sách phiên bản!");
    } finally {
      setIsLoading(false);
    }
  }, [selectedSemester, searchName]);

  // Fetch versions when dependencies change
  useEffect(() => {
    fetchVersions();
  }, [fetchVersions]);

  const createVersion = useCallback(async (versionData) => {
    if (!versionData.name || !versionData.status || !versionData.semester || !versionData.userId) {
      toast.error("Vui lòng điền đầy đủ thông tin!");
      return null;
    }

    setIsCreating(true);
    try {
      const createdVersion = await timeTablingVersionRepository.createVersion(versionData);
      
      await fetchVersions();
      
      toast.success("Tạo phiên bản mới thành công!");
      return createdVersion;
    } catch (error) {
      console.error("Error creating version:", error);
      toast.error(error.response?.data || "Có lỗi khi tạo phiên bản mới!");
      return null;
    } finally {
      setIsCreating(false);
    }
  }, [fetchVersions]);

  return {
    states: {
      versions,
      selectedVersion,
      selectedSemester,
      searchName,
      isLoading,
      isCreating
    },
    setters: {
      setSelectedVersion,
      setSelectedSemester,
      setSearchName
    },
    handlers: {
      fetchVersions,
      createVersion
    }
  };
};
