import React from "react";
import { Link } from "react-router-dom";
import {Toolbar } from "./children";
type Props = { navigation: Array<Object>, toolbar: Array<React.Node> };

const NavBar = (props: Props) => {

  const { toolbar } = props;
  return (
    <header role="navigation" className="header header__desktop">
      <section className="header-wrap">
        <div className="logo-block">
          <Link to="/welcome">
            <img style={{width:"18%"}} src="/img/clock.svg" alt="clock" width="25"/>
          </Link>
        </div>
        {toolbar ? <Toolbar toolbar={toolbar} /> : ""}
      
      </section>
    </header>
  );
};

export default NavBar;
