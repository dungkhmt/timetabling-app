export const ExamTimeTabling = {
    id: "MENU_EXAM_TIME_TABLING",
    icon: "AnalyticsIcon",
    text: "Xếp lịch thi",
    child: [
      {
        id: "MENU_EXAM_TIME_TABLING.CLASS_LIST",
        path: "/exam-time-tabling/exam-plan",
        isPublic: true,
        text: "Kế Hoạch Thi",
        child: [],
      },
      {
        id: "MENU_EXAM_TIME_TABLING.CLASS_LIST",
        path: "/exam-time-tabling/class-list",
        isPublic: true,
        text: "DS Lớp Thi",
        child: [],
      },
      {
        id: "MENU_EXAM_TIME_TABLING.EXAM_CONFLICT",
        path: "/exam-time-tabling/exam-class-conflict",
        isPublic: true,
        text: "Thiết lập xung đột",
        child: [],
      },
      {
        id: "MENU_EXAM_TIME_TABLING.EXAM_SESSION",
        path: "/exam-time-tabling/exam-session",
        isPublic: true,
        text: "Cài đặt khác",
        child: [],
      },
    ],
  };
  