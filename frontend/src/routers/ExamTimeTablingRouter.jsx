import ExamClassScreen from "views/exam-timetabling/classListOpened/ExamClassListScreen";
import { Route, Switch, useRouteMatch } from "react-router";
import ExamPlanListPage from "views/exam-timetabling/examPlan/ExamPlanScreen"
import ExamPlanDetailPage from "views/exam-timetabling/examPlan/ExamPlanDetailScreen"
import TimetableDetailPage from "views/exam-timetabling/examTimetable/ExamTimetableDetail"
import ExamSessionManagementScreen from "views/exam-timetabling/examSession/ExamSessionScreen"
import ExamTimeTableView from "views/exam-timetabling/examTimetable/ExamTimetableView"

export default function ExamTimeTablingRouter() {
  let { path } = useRouteMatch();
  return (
    <div>
      <Switch>
        <Route
         component={ExamClassScreen}
         exact
         path={`${path}/class-list/:planId`}
        ></Route>
        <Route
          component={ExamClassScreen}
          exact
          path={`${path}/class-list`}
        ></Route>
        <Route
          component={ExamPlanListPage}
          exact
          path={`${path}/exam-plan`}
        ></Route>
        <Route
          component={ExamPlanDetailPage}
          path={`${path}/exam-plan/:id`}
          exact
        ></Route>
        <Route
          component={TimetableDetailPage}
          path={`${path}/exam-timetable/:id`}
          exact
        ></Route>
        <Route
          component={ExamTimeTableView}
          path={`${path}/exam-timetable/view/:id`}
          exact
        ></Route>
        <Route
          component={ExamSessionManagementScreen}
          path={`${path}/exam-session`}
          exact
        ></Route>
      </Switch>
    </div>
  );
}
