import React from "react";
import { NavBar } from "@components";

import { NavBarProfile } from "./children";
// import { LanguageDropdown } from "@util-components";

const AuthNavBar = props => {
  const { t } = props;
  const navigation = [
    {
      id: "welcome",
      icon: "img/icon/apps.svg",
      label: t("navBar.welcome"),
      to: "/welcome"
    },
    {
      id:"example",
      icon: "img/icon/apps.svg",
      label:"example",
      to:'/welcome'
    }
    // add tabs in the above fashion -jreezy

  ];
  return (
    <NavBar
      navigation={navigation}
      toolbar={[
        {
          component: () => <NavBarProfile {...props} />,
          id: "profile"
        }
      ]}
    />
  );
};

export default AuthNavBar;
