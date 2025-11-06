import { Route, Switch, useRouteMatch } from "react-router";
import ListBatch from "views/general-time-tabling/batch/listbatch";
import EmptyRoomFindingScreen from "views/general-time-tabling/empty-room-find/EmptyRoomFindingScreen";
import GeneralGroupScreen from "views/general-time-tabling/general-group-select/GeneralGroupScreen";
import GeneralPlanClassOpenScreen from "views/general-time-tabling/general-plan-class-open/GeneralPlanClassOpenScreen";
import OpenedClassPlan from "views/general-time-tabling/general-plan-class-open/OpenedClassPlan";
import GeneralScheduleScreen from "views/general-time-tabling/general-schedule/GeneralScheduleScreen";
import GeneralScheduleSummerScreen from "views/general-time-tabling/general-schedule/GeneralScheduleSummerScreen";
import ListBatchForMakeTimeTabling from "views/general-time-tabling/general-schedule/ListBatchForMakeTimeTabling";
import MakeTimetable from "views/general-time-tabling/general-schedule/MakeTimetable";
import GeneralUploadScreen from "views/general-time-tabling/general-upload/GeneralUploadScreen";
import RoomOccupationScreen from "views/general-time-tabling/room-occupation/RoomOccupationScreen";
import VersionMakeTimetable from "views/general-time-tabling/version-selection/VersionMakeTimetable";

export default function GeneralTimeTablingRouter() {
  let { path } = useRouteMatch();
  return (
    <div>
      <Switch>
        <Route
          //component={GeneralPlanClassOpenScreenV2}
          component={GeneralPlanClassOpenScreen}
          exact
          path={`${path}/plan-class-open`}
        ></Route>
        <Route
          //component={GeneralPlanClassOpenScreenV2}
          component={ListBatch}
          exact
          path={`${path}/list-batch`}
        ></Route>
        <Route
          //component={GeneralPlanClassOpenScreenV2}
          component={VersionMakeTimetable}
          exact
          path={`${path}/version-make-timetable/:batchId`}
        ></Route>
        <Route
          //component={GeneralPlanClassOpenScreenV2}
          component={MakeTimetable}
          exact
          path={`${path}/make-timetable/:versionId`}
        ></Route>
        
        <Route
          //component={GeneralPlanClassOpenScreenV2}
          component={ListBatchForMakeTimeTabling}
          exact
          path={`${path}/list-batch-make-timetabling`}
        ></Route>
        <Route
          //component={GeneralPlanClassOpenScreenV2}
          component={OpenedClassPlan}
          exact
          path={`${path}/opened-class-plan/:batchId`}
        ></Route>
        
        <Route
          component={GeneralUploadScreen}
          exact
          path={`${path}/upload-class`}
        ></Route>
        <Route
          component={GeneralGroupScreen}
          exact
          path={`${path}/group-class`}
        ></Route>
        <Route
          component={GeneralScheduleScreen}
          exact
          path={`${path}/schedule-class`}
        ></Route>
         <Route
          component={GeneralScheduleSummerScreen}
          exact
          path={`${path}/schedule-class-summer`}
        ></Route>
        <Route
          component={RoomOccupationScreen}
          exact
          path={`${path}/room-occupation`}
        ></Route>
        <Route
          component={EmptyRoomFindingScreen}
          exact
          path={`${path}/find-empty-room`}
        ></Route>
      </Switch>
    </div>
  );
}
