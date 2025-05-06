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
}

export const examRoomService = new ExamRoomService();
