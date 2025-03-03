import { request } from "api";

const API_ENDPOINTS = {
  GET_ALL: "/exam-room",
  CREATE: "/exam-room/create",
  UPDATE: "/exam-room/update",
  DELETE: "/exam-room/delete",
};

class ExamRoomService {
  async getAllExamRooms() {
    return await request("get", `${API_ENDPOINTS.GET_ALL}`);
  }

  async createExamRoom(data) {
    return await request("post", API_ENDPOINTS.CREATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      } 
    });
  }

  async updateExamRoom(data) {
    return await request("post", API_ENDPOINTS.UPDATE, null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async deleteExamRoom(id) {
    return await request("post", `${API_ENDPOINTS.DELETE}/${id}`);
  }
}

export const examRoomService = new ExamRoomService();
