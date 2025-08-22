import {Route, Switch, useRouteMatch} from "react-router-dom";
import ListClassPlan from "../views/teacher-class-assignment/list-class/ListClassPlan";
import ListClassBySchool from "../views/teacher-class-assignment/list-class/ListClassBySchool";

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
            </Switch>
        </div>
    );
}