import React, { Fragment } from "react";
// import { Route, Link } from "react-router-dom";
import { Route} from "react-router-dom";
import { withTranslation } from "react-i18next";
// import { NavBar, AuthNavBar, Footer } from "@components";
import { Footer } from "@components";
import { withWebId } from "@inrupt/solid-react-components";
// import { LanguageDropdown } from "@util-components";

const PublicLayout = props => {
  const { component: Component, webId, i18n, ...rest } = props;
  return (
    <Route
      {...rest}
      render={matchProps => (
        <Fragment>
          <Component {...matchProps} />
          <Footer></Footer>
        </Fragment>
      )}
    />
  );
};

export default withTranslation()(withWebId(PublicLayout));
