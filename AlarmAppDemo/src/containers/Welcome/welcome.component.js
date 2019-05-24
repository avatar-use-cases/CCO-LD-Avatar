import React from 'react';
// import { LogoutButton, Uploader } from '@inrupt/solid-react-components';
import isLoading from '@hocs/isLoading';
import {withTranslation } from 'react-i18next';
import {
  WelcomeWrapper,
  WelcomeCard
} from './welcome.style';
import { withToastManager } from 'react-toast-notifications';


/**
 * Welcome Page UI component, containing the styled components for the Welcome Page
 * Image component will get theimage context and resolve the value to render.
 * @param props
 */
const WelcomePageContent = props => {
    const {name} = props;
    const goToSetup = () => {
      window.location = "/setup";
    }

    return (

        <WelcomeWrapper data-testid="welcome-wrapper">
            <WelcomeCard className="card">
                <h3>
                    Welcome, <span>{name}</span>
                </h3>

                <div style={{}}>
                    <h5>to the Dynamic Alarm Clock.</h5>
                </div>
                
                <div style={{width:"60%"}}>
                    We dynamically adjust your wake up time based on traffic and weather. Click below to setup your alarm!
                </div>

                <br/>

                <div>
                    <button onClick={goToSetup}>Setup My Alarm</button>
                </div>

            </WelcomeCard>
        </WelcomeWrapper>
    );
};

export { WelcomePageContent };
export default withTranslation()(
  isLoading(withToastManager(WelcomePageContent))
);
