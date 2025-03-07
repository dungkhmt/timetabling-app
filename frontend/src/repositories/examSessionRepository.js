import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-session",
  CREATE_SESSION: "/exam-session/session/create",
  UPDATE_SESSION: "/exam-session/session/update",
  DELETE_SESSION: "/exam-session/session/delete",
  CREATE_SESSION_COLLECTION: "/exam-session/collection/create",
  UPDATE_SESSION_COLLECTION: "/exam-session/collection/update",
  DELETE_SESSION_COLLECTION: "/exam-session/collection/delete",
};

class ExamSessionService {
  async getAllExamSessions() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }

  async createExamSession(data) {
    return await request("post", API_ENDPOINTS.CREATE_SESSION, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }

  async updateExamSession(data) {
    return await request("post", `${API_ENDPOINTS.UPDATE_SESSION}/${data.id}`, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async deleteExamSession(id) {
    return await request("post", `${API_ENDPOINTS.DELETE_SESSION}/${id}`);
  }

  async createExamSessionCollection(data) {
    return await request("post", API_ENDPOINTS.CREATE_SESSION_COLLECTION, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }

  async updateExamSessionCollection(data) {
    return await request("post", `${API_ENDPOINTS.UPDATE_SESSION_COLLECTION}/${data.id}`, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async deleteExamSessionCollection(id) {
    return await request("post", `${API_ENDPOINTS.DELETE_SESSION_COLLECTION}/${id}`);
  }
}

export const examSessionService = new ExamSessionService();
