import { request } from "api";

export const timeTablingVersionRepository = {
  createVersion: async (versionData) => {
    try {
      const response = await request(
        "post",
        "/timetabling-versions/create",
        null,
        null,
        versionData
      );
      return response.data;
    } catch (error) {
      console.error("Create version error:", error);
      throw error;
    }
  },

  deleteVersion: async (versionId) => {
    try {
      const response = await request(
        "delete",
        `/timetabling-versions/${versionId}`
      );
      return response.data;
    } catch (error) {
      console.error("Delete version error:", error);
      throw error;
    }
  },
  
  updateVersion: async (versionId, name, status) => {
    try {
      const response = await request(
        "put",
        `/timetabling-versions/${versionId}`,
        null,
        null,
        { name, status }
      );
      return response.data;
    } catch (error) {
      console.error("Update version error:", error);
      throw error;
    }
  },

  getVersions: async (semester = "", name = "") => {
    try {
      let url = "/timetabling-versions/";
      const params = [];
      
      if (semester) {
        params.push(`semester=${encodeURIComponent(semester)}`);
      }
      
      if (name) {
        params.push(`name=${encodeURIComponent(name)}`);
      }
      
      if (params.length > 0) {
        url += "?" + params.join("&");
      }
      
      const response = await request("get", url);
      return response.data;
    } catch (error) {
      console.error("Get versions error:", error);
      throw error;
    }
  }
};
