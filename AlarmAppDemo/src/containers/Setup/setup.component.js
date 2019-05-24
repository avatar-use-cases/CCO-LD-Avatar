import React from 'react';
import isLoading from '@hocs/isLoading';
import {withTranslation } from 'react-i18next';
import {
  SetupWrapper,
  SetupCard
} from './setup.style';
import { withToastManager } from 'react-toast-notifications';
import StepSlider from '..\\..\\components\\Utils\\StepSlider\\StepSlider';
import Clock from '..\\..\\components\\Utils\\Clock\\Clock';

/**
 * Welcome Page UI component, containing the styled components for the Welcome Page
 * Image component will get theimage context and resolve the value to render.
 * @param props
 */

  const getTransportationPreference = transPref => {

    if (transPref) {
      return transPref.map(tP => (
        <p>
          Today's Transportation Method:{tP.mot.split("/")[tP.mot.split("/").length-1]}
        </p>
      ));

    }
    else {
      return "No Results"
    }
  }

  const getWorkGeo = workGeo => {
    if (workGeo) {
        return workGeo.map(wG => (
          <p>
            Work Location: {wG.lat}, {wG.lon}
          </p>
        ));
    }
    else {
      return "No results";
    }
  };

  const getTest = test => {
    if (test) {
        return test.map(t => (
          <p>
            test: {t.mot}
          </p>
        ));
    }
    else {
      return "No results";
    }
  };

  const getHomeGeo = homeGeo => {
    if (homeGeo) {
        return homeGeo.map(hG => (
          <p>
            Home Location: {hG.lat}, {hG.lon}
          </p>
        ));
    } 
    else {
      return "No results";
    }
  };

  const getDriveTime = (driveTimeP,driveTimeB,driveTimeS,driveTimeF) => {
    if (driveTimeP && driveTimeB && driveTimeS && driveTimeF) {
      return (
        <p>
          {driveTimeP.toFixed(2)} minutes by foot
          <br/>
          {driveTimeB.toFixed(2)} minutes by bicycle
          <br/>
          {driveTimeS.toFixed(2)} minutes by car (shortest)
          <br/>
          {driveTimeF.toFixed(2)} minutes by car (fastest)
          <br/>

        </p>
      );
    } else {
      return "No results";
    }
  };
  
  const getWeather = (homeWeather,workWeather) => {
    if ((homeWeather) || (workWeather)) {
      return (
        <p>
          Home Weather: {homeWeather}
          <br/>
          Work Weather: {workWeather}
        </p>
  
      );
    } else {
      return "No results";
    }
  };
  
  const getWeatherPenalty = (workWeatherPenalty,homeWeatherPenalty) => {
    if ((homeWeatherPenalty) || (workWeatherPenalty)) {
      return (
        <p>
          Home weather penalty: {homeWeatherPenalty}
          <br/>
          Work weather penalty: {workWeatherPenalty}
        </p>
  
      );
    } else {
      return (
        <p>
          Home weather penalty: 0
          <br/>
          Work weather penalty: 0
        </p>
      )
    }
  };
  
  const checkTimeString = (timeString) => {
    if (timeString.length !== 4){
      return false;
    }
    else if (parseInt(timeString.substring(0,2)) > 24){
      return false;
    }
    else if (parseInt(timeString.substring(2,4)) > 60){
      return false;
    }
    else {
      return true;
    }
  }

  const calcTime = () => {
    let workTime = document.getElementById("workTime").value;

    if (checkTimeString(workTime)){

      let prepTime = parseInt(document.getElementById("slider").innerText);
      let homeWeatherTime = parseInt(document.getElementById("hwp").innerText);
      let workWeatherTime = parseInt(document.getElementById("wwp").innerText);
      let driveTime = parseInt(document.getElementById("dt").innerText);
      let totalTime = (prepTime + driveTime + homeWeatherTime + workWeatherTime).toFixed(2)
    
      const MILLIS = 60000;
      const days = MILLIS*60*24
      let alarmTime = new Date;
      let hour = parseInt(workTime.substring(0,2))
      let minutes = parseInt(workTime.substring(2,4))
      alarmTime.setHours(hour)
      alarmTime.setMinutes(minutes)
      alarmTime.setSeconds(0)
      alarmTime = new Date(((alarmTime-(totalTime*MILLIS)) + days))
      document.getElementById("wakeUpTimeDisplay").innerHTML = "<h3>Alarm set for: " + alarmTime.toLocaleTimeString() + "</h3>"
  
    }
    else {
      alert("Bad time format")
    }
  }

  const SetupPageContent = props => {
    const { name, t } = props;
    
    return (
      <SetupWrapper data-testid="welcome-wrapper">
        <SetupCard className="card">
          <h3>
            {t('welcome.welcome')}, <span>{name}</span>
          </h3>
          
          <div style={{textAlign:'center',width:'98%',marginLeft:'auto',marginRight:'auto'}}>How long do you need to get ready for work?</div>
        
          <div style={{paddingTop:"3em",width:"60%"}}>
            <input id="workTime" placeholder="When do you start work? <24hr format ex: 1430 or 0500>" style={{border:"2px solid #8CCAF2"}}/>
          </div>

          <StepSlider id="prepTime" />
            
          <div>
            <br/>
            <button onClick={calcTime} styles={{width:'100'}}>Set Alarm</button>
          </div>

          <div id="hwp" style={{display:"none"}}>
            {props.homeWeatherPenalty}
          </div>
          <div id="wwp"  style={{display:"none"}}>
            {props.workWeatherPenalty}
          </div>
          <div id="dt"  style={{display:"none"}}>
            {props.driveTime}
          </div>

        <br/><br/>
          
        <div id="wakeUpTimeDisplay">
        </div>

        <div style={{textAlign:'center',width:'98%',marginLeft:'auto',marginRight:'auto'}}>
          Query Results:
            {
              getWorkGeo(props.workGeo)
            }
            {
              getHomeGeo(props.homeGeo)
            }
        </div>
  
        <div style={{textAlign:'center',width:'98%',marginLeft:'auto',marginRight:'auto'}}>
          <br/><br/>
          Travel Time:
          {getDriveTime(props.driveTimeP,props.driveTimeB,props.driveTimeS,props.driveTimeF)}
        </div>
  
        <div style={{textAlign:'center',width:'98%',marginLeft:'auto',marginRight:'auto'}}>
          <br/><br/>
          Weather info: 
          {getWeather(props.homeWeather,props.workWeather)}
          {getWeatherPenalty(props.homeWeatherPenalty,props.workWeatherPenalty)}
        </div>
  
        <div style={{textAlign:'center',width:'98%',marginLeft:'auto',marginRight:'auto'}}>
          <br/><br/>
          Transportation info: 
          {getTransportationPreference(props.transPref)}
        </div>
            
        {/* <div>
            <br/>
            New Query Info:
            {getTest(props.test)}
        </div> */}

        <Clock/>

        </SetupCard>
      </SetupWrapper>
    );
  };
  
  export { SetupPageContent };
  export default withTranslation()(
    isLoading(withToastManager(SetupPageContent))
  );
