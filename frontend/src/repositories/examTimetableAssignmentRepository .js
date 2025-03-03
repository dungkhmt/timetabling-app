import { request } from "api";

class ExamTimetableAssignmentService {
  async getAllExamTimetableAssignments(timetableId) {
    return await request("get", `/exam-timetable/assignment/${timetableId}`);
  }

  async updateExamTimetableAssignmentAssignment(data) {
    return await request("post", "/exam-timetable/assignment/update-batch", null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }

  async checkExamTimetableAssignmentAssignmentConflict(data) {
    return await request("post", "/exam-timetable/assignment/check-conflict", null, null, data, {
      headers: {
        "Content-Type": "application/json"
      }
    });
  }
}

export const examTimetableAssignmentService = new ExamTimetableAssignmentService();
