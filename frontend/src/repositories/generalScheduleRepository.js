import { request } from "api";

export const generalScheduleRepository = {
  getClasses: async (semester, groupName = "", forceRefresh = false) => {
    try {
      console.log("Fetching classes data");
      const response = await request(
        "get",
        `/general-classes/?semester=${semester}&groupName=${groupName || ""}`
      );

      const transformedData = response.data.map((item) => ({
        ...item,
        generalClassId:
          item.generalClassId != null ? String(item.generalClassId) : "",
      }));

      return transformedData;
    } catch (error) {
      console.error("Fetch error:", error);
      throw error;
    }
  },

  getClassesNoSchedule: async (semester, groupName = "") => {
    try {
      const response = await request(
        "get",
        `/general-classes/?semester=${semester}&groupName=${groupName || ""}`
      );

      const transformedData = response.data.map((classObj) => {
        const { timeSlots, ...rest } = classObj;
        return rest;
      });

      return transformedData;
    } catch (error) {
      console.error("Fetch no schedule error:", error);
      throw error;
    }
  },

  resetSchedule: async (semester, ids) => {
    return await request(
      "post",
      `/general-classes/reset-schedule?semester=${semester}`,
      null,
      null,
      { ids }
    );
  },

  autoScheduleTime: async (semester, groupName, timeLimit, algorithm) => {
    return await request(
      "post",
      `/general-classes/auto-schedule-time?semester=${semester}&groupName=${groupName}&timeLimit=${timeLimit}&algorithm=${algorithm || ''}`
    );
  },

  autoScheduleRoom: async (semester, groupName, timeLimit, algorithm) => {
    return await request(
      "post",
      `/general-classes/auto-schedule-room?semester=${semester}&groupName=${groupName}&timeLimit=${timeLimit}&algorithm=${algorithm || ''}`
    );
  },

  autoScheduleSelected: async (classIds, timeLimit, semester, algorithm,maxDaySchedule) => {
    return await request(
      "post",
      "/general-classes/auto-schedule-timeslot-room",
      null,
      null,
      {
        classIds,
        timeLimit,
        semester,
        algorithm,
        maxDaySchedule
      }
    );
  },

  exportExcel: async (semester) => {
    try {
      const response = await request(
        "post",
        `general-classes/export-excel?semester=${semester}`,
        null,
        null,
        null,
        { responseType: "arraybuffer" }
      );

      const blob = new Blob([response.data], {
        type: response.headers["content-type"],
      });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = `Danh_sach_lop_TKB_${semester}.xlsx`;

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      return response;
    } catch (error) {
      console.error("Export Excel error:", error);
      throw error;
    }
  },

  updateTimeSlot: async (semester, saveRequest, errorCallback) => {
    try {
      const response = await request(
        "post",
        `/general-classes/update-class-schedule-v2?semester=${semester}`,
        null,
        null,
        { saveRequests: [saveRequest] },
        {},
        null,
        errorCallback
      );

      return response;
    } catch (error) {
      console.log("Update error:", error);

      if (
        typeof errorCallback === "function" &&
        error?.response?.status === 410
      ) {
        errorCallback(error);
        return error;
      }

      throw error;
    }
  },

  addTimeSlot: async (params = {}) => {
    const { generalClassId, parentId, duration } = params;
    if (!generalClassId) {
      throw new Error("generalClassId is required");
    }
    const cleanId = generalClassId.toString().split("-")[0];
    return await request(
      "post",
      `/general-classes/${cleanId}/room-reservations/`,
      null,
      null,
      { parentId, duration }
    );
  },

  removeTimeSlot: async (params = {}) => {
    const { generalClassId, roomReservationId } = params;
    if (!generalClassId || !roomReservationId) {
      throw new Error("generalClassId and roomReservationId are required");
    }
    const cleanId = generalClassId.toString().split("-")[0];
    return await request(
      "delete",
      `/general-classes/${cleanId}/room-reservations/${roomReservationId}`
    );
  },

  deleteClasses: async (semester) => {
    return await request("delete", `/general-classes/?semester=${semester}`);
  },

  deleteBySemester: async (semester) => {
    return await request(
      "delete",
      `/general-classes/delete-by-semester?semester=${semester}`
    );
  },

  deleteByIds: async (ids) => {
    return await request(
      "delete",
      `/general-classes/delete-by-ids`,
      null,
      null,
      ids
    );
  },

  uploadFile: async (semester, file) => {
    const formData = new FormData();
    formData.append("file", file);

    return await request(
      "post",
      `/excel/upload-general?semester=${semester}`,
      null,
      null,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
  },

  getClassGroups: async (classId) => {
    if (!classId) {
      throw new Error("classId is required");
    }
    
    const cleanId = classId.toString().split("-")[0];
    
    const response = await request(
      "get",
      `/general-classes/get-class-groups?classId=${cleanId}`
    );
    return response.data;
  },

  updateClassGroup: async (classId, groupId) => {
    const response = await request(
      "post",
      `/general-classes/update-class-group?classId=${classId}&groupId=${groupId}`
    );
    return response.data;
  },

  deleteClassGroup: async (classId, groupId) => {
    const response = await request(
      "delete",
      `/general-classes/delete-class-group?classId=${classId}&groupId=${groupId}`
    );
    return response.data;
  },

  getSubClasses: async (parentClassId) => {
    try {
      const response = await request(
        "get",
        `/general-classes/get-by-parent-class?parentClassId=${parentClassId}`
      );
      return response.data;
    } catch (error) {
      console.error("Fetch subclasses error:", error);
      throw error;
    }
  },

  updateClassesGroup: async (params) => {
    const { ids, groupName } = params;
    try {
      return await request(
        "post",
        "/general-classes/update-classes-group",
        null,
        null,
        { ids, groupName }
      );
    } catch (error) {
      console.error("Update classes group error:", error);
      throw error;
    }
  },
  getListAlgorithms: async () => {
    try {
      const response = await request(
        "get",
        "/general-classes/get-list-algorithm-names"
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching algorithm names:", error);
      throw error;
    }
  },

  getClassesByCluster: async (clusterId) => {
    try {
      const response = await request(
        "get",
        `/general-classes/get-by-cluster/${clusterId}`
      );
      return response.data;
    } catch (error) {
      console.error("Fetch classes by cluster error:", error);
      throw error;
    }
  },

  getClustersBySemester: async (semester) => {
    try {
      const response = await request(
        "get",
        `/general-classes/get-clusters-by-semester?semester=${semester}`
      );
      return response.data;
    } catch (error) {
      console.error("Fetch clusters by semester error:", error);
      throw error;
    }
  },
};
