import {Route, Switch, useRouteMatch} from "react-router-dom";
import ListClassPlan from "../views/teacher-class-assignment/list-class/ListClassPlan";
import ListClassBySchool from "../views/teacher-class-assignment/list-class/ListClassBySchool";
import SettingBatch from "../views/teacher-class-assignment/list-class/SettingBatch";

export default function TeacherClassAssignmentRouter() {
    let { path } = useRouteMatch();
    console.log("Current path:", path);

    return (
        <div>
            <Switch>
                <Route
                    component={ListClassPlan}
                    exact
                    path={`${path}/ListClassPlan`}
                ></Route>
                <Route
                    component={ListClassBySchool}
                    exact
                    path={`${path}/ListClassBySchool`}
                ></Route>
                <Route
                    component={SettingBatch}
                    exact
                    path={`${path}/SettingBatch`}
                ></Route>
            </Switch>
        </div>
    );
}