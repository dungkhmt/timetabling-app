import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-session",
  CREATE: "/exam-session/create",
  UPDATE: "/exam-session/update",
  DELETE: "/exam-session/delete",
};

class ExamSessionService {
  async getAllExamSessions() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }

  async createExamSession(data) {
    return await request("post", API_ENDPOINTS.CREATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }

  async updateExamSession(data) {
    return await request("post", API_ENDPOINTS.UPDATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async deleteExamSession(id) {
    return await request("post", `${API_ENDPOINTS.DELETE}/${id}`);
  }
}

export const examSessionService = new ExamSessionService();
