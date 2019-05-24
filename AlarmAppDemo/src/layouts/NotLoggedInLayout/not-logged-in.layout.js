import React, { Fragment } from "react";
import { Route, Redirect } from "react-router-dom";
import { withTranslation } from "react-i18next";
import { NavBar, Footer } from "@components";
import { withWebId } from "@inrupt/solid-react-components";

const NotLoggedInLayout = props => {
  const { component: Component, webId, ...rest } = props;
  return !webId ? (
    <Route
      {...rest}
      render={matchProps => (
        <Fragment>
          <NavBar {...matchProps} />
          <Component {...matchProps} />
          <Footer></Footer>
        </Fragment>
      )}
    />
  ) : (
    <Redirect to="/welcome" />
  );
};

export default withTranslation()(withWebId(NotLoggedInLayout));
