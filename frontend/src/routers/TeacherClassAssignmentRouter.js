import {Route, Switch, useRouteMatch} from "react-router-dom";
import ListClassPlan from "../views/teacher-class-assignment/list-class/ListClassPlan";

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

            </Switch>
        </div>
    );
}