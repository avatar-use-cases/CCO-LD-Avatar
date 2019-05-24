import React, { Fragment } from "react";
import { PrivateLayout, PublicLayout, NotLoggedInLayout } from "@layouts";
import { BrowserRouter as Router, Switch, Redirect } from "react-router-dom";

import {
  Login,
  PageNotFound,
  Welcome,
  Setup
} from "./containers";

const privateRoutes = [
  {
    id: "welcome",
    path: "/welcome",
    component: Welcome
  },
  {
    id:"setup",
    path:"/setup",
    component: Setup
  }
];

const Routes = () => (
  <Router>
    <Fragment>
      <Switch>
        <NotLoggedInLayout component={Login} path="/login" exact /> 
        <PublicLayout path="/404" component={PageNotFound} exact />
        <Redirect from="/" to="/welcome" exact />
        <PrivateLayout path="/" routes={privateRoutes} />
        <Redirect to="/404" />
      </Switch>
    </Fragment>
  </Router>
);

export default Routes;
