export const ExamTimeTabling = {
    id: "MENU_EXAM_TIME_TABLING",
    icon: "AnalyticsIcon",
    text: "Xếp lịch thi",
    child: [
      {
        id: "MENU_EXAM_TIME_TABLING.CLASS_LIST",
        path: "/exam-time-tabling/exam-plan",
        isPublic: false,
        text: "Kế Hoạch Thi",
        child: [],
      },
      {
        id: "MENU_EXAM_TIME_TABLING.CLASS_LIST",
        path: "/exam-time-tabling/class-list",
        isPublic: false,
        text: "DS Lớp Thi",
        child: [],
      },
      {
        id: "MENU_EXAM_TIME_TABLING.EXAM_CONFLICT",
        path: "/exam-time-tabling/exam-class-conflict",
        isPublic: false,
        text: "Thiết lập xung đột",
        child: [],
      },
      {
        id: "MENU_EXAM_TIME_TABLING.EXAM_SESSION",
        path: "/exam-time-tabling/exam-session",
        isPublic: false,
        text: "DS Kíp Thi",
        child: [],
      },
    ],
  };
  