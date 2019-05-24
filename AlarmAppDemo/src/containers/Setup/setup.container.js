import React, { Component } from 'react';
import SetupPageContent from './setup.component';
import { withWebId } from '@inrupt/solid-react-components';
import data from '@solid/query-ldflex';
import { withToastManager } from 'react-toast-notifications';
import $rdf from "rdflib";

const defaultProfilePhoto = '/img/icon/empty-profile.svg';
const store = $rdf.graph();


// RG - 2019-02-28
// Loads the data from a URL into the local store
const loadFromUrl = (url, store) => {
    return new Promise((resolve, reject) => {
      let fetcher = new $rdf.Fetcher(store);
      try {
        fetcher.load(url).then(response => {
          resolve(response.responseText);
          // console.debug(response.responseText);
          // $rdf.parse(response.responseText, store, $rdf.sym(url).uri,"application/rdf");
        });
      } catch (err) {
        reject(err);
      }
    });
  };
  
  // RG - 2019-02-28
  // Prepares a query by converting SPARQL into a Solid query
  const prepare = (qryStr, store) => {
    return new Promise((resolve, reject) => {
      try {
        let query = $rdf.SPARQLToQuery(qryStr, false, store);
        resolve(query);
      } catch (err) {
        reject(err);
      }
    });
  };
  
  // RG - 2019-02-28
  // Executes a query on the local store
  const execute = (qry, store) => {
    return new Promise((resolve, reject) => {
      // console.debug("here");
      const wanted = qry.vars;
      const resultAry = [];
      store.query(
        qry,
        results => {
          // console.debug("here1");
          if (typeof results === "undefined") {
            reject("No results.");
          } else {
            let row = rowHandler(wanted, results);
            // console.debug(row);
            if (row) resultAry.push(row);
          }
        },
        {},
        () => {
          resolve(resultAry);
        }
      );
    });
  };
  
  // RG - 2019-02-28
  // Puts query results into an array according to the projection
  const rowHandler = (wanted, results) => {
    const row = {};
    for (var r in results) {
      let found = false;
      let got = r.replace(/^\?/, "");
      if (wanted.length) {
        for (var w in wanted) {
          if (got === wanted[w].label) {
            found = true;
            continue;
          }
        }
        if (!found) continue;
      }
      row[got] = results[r].value;
    }
    return row;
  };

// checks to see if any string in a list appears in another given string.
const listContains = (s,a) => {
  for (let i=0;i<a.length;i++){
    if(s.toLowerCase().includes(a[i])){
      return true,a[i];
    }
  }
  return false;
}

class SetupComponent extends Component<Props> {
    constructor(props) {
        super(props);
    
        this.state = {
          name: '',
          image: defaultProfilePhoto,
          isLoading: false,
          hasImage: false,
          hits:[],
          // value:0
        };
    }

    componentDidMount() {
        if (this.props.webId) {
          this.getProfileData();
        }
    }
    
    componentDidUpdate(prevProps, prevState) {
        if (this.props.webId && this.props.webId !== prevProps.webId) {
          this.getProfileData();
        }
    }

    getProfileData = async () => {
      this.setState({ isLoading: true });
      let hasImage;

      const user = data[this.props.webId];
      const nameLd = await user.name;
  
      const name = nameLd ? nameLd.value : '';
  
      let imageLd = await user.image;
      imageLd = imageLd ? imageLd : await user.vcard_hasPhoto;
  
      let image;
      if (imageLd && imageLd.value) {
        image = imageLd.value;
        hasImage = true;
      } else {
        hasImage = false;
        image = defaultProfilePhoto;
      }
  
      const badWeather = ["rain","showers","snow","flurries","thunderstorms","tornado","hurricane","rainy","snowy"]
      const routeTypes = ["BICYCLE","PEDESTRIAN","FASTEST","SHORTEST"]
  
      const userUrl = user.toString();    
      const CUPurl = userUrl.substring(0,userUrl.length-15)+"profile/card"
      
      const homeGeoQS = "prefix cco: <http://www.ontologyrepository.com/CommonCoreOntologies/> prefix obo: <http://purl.obolibrary.org/obo/> select distinct ?lat ?lon where {?person a cco:Person . ?person cco:agent_in ?reside . ?reside a cco:ActOfOwnership . ?reside cco:has_object ?home . ?home a cco:ResidentialFacility . ?home obo:RO_0001025 ?geoRegion. ?geoRegion a cco:GeospatialRegion . ?geoRegion cco:designated_by ?GSID . ?GSID obo:RO_0010001 ?ibe . ?ibe obo:BFO_0000051 ?latBE. ?latBE a cco:LatitudeBearingEntityPart . ?latBE cco:has_text_value ?lat . ?ibe obo:BFO_0000051 ?lonBE. ?lonBE a cco:LongitudeBearingEntityPart . ?lonBE cco:has_text_value ?lon .}"
      const workGeoQS = "prefix cco: <http://www.ontologyrepository.com/CommonCoreOntologies/> prefix  obo: <http://purl.obolibrary.org/obo/> select distinct ?lat ?lon where {?person a cco:Person . ?person obo:RO_0000053 ?job . ?job a cco:OccupationRole . ?job cco:has_organizational_context ?org . ?org a cco:Organization. ?org cco:agent_in ?act .?act a cco:ActOfInhabitancy. ?act cco:has_object ?facility . ?facility a cco:Facility . ?facility obo:RO_0001025 ?GeoReg . ?GeoReg a cco:GeospatialRegion . ?GeoReg cco:designated_by ?GeoID . ?GeoID obo:RO_0010001 ?ibe . ?ibe obo:BFO_0000051 ?latBE. ?latBE a cco:LatitudeBearingEntityPart . ?latBE cco:has_text_value ?lat . ?ibe obo:BFO_0000051 ?lonBE. ?lonBE a cco:LongitudeBearingEntityPart . ?lonBE cco:has_text_value ?lon .}"
      const commuteQS = "prefix cco: <http://www.ontologyrepository.com/CommonCoreOntologies/> prefix  obo: <http://purl.obolibrary.org/obo/> select distinct ?mot where {?person a cco:Person . ?person obo:RO_0000053 ?pref . ?pref a cco:Preference . ?pref cco:ModalRelationOntology/BFO_0000054 ?act . ?act a cco:ActOfCommuting . ?act cco:ModalRelationOntology/RO_0000057 ?thing. ?thing a ?mot.}"

    loadFromUrl(CUPurl, store).then(() =>
      prepare(commuteQS, store).then(qry =>
        execute(qry, store).then(results => {
          this.setState({ transPref: results});
        })
      )
    )
  


    loadFromUrl(CUPurl, store).then(() =>
        prepare(workGeoQS, store).then(qry =>
          execute(qry, store).then(results => {
            this.setState({ workGeo: results});
            this.state.workGeo.map(wG => {
              this.setState({worklat:wG.lat,workLon:wG.lon,workWeatherAPI:"https://api.weather.gov/points/" + wG.lat.toString() + "," + wG.lon.toString() + "/forecast"})
            })
            }).then( () => {
              fetch(this.state.workWeatherAPI).then(response => response.json()).then(weatherData => {
                if(weatherData['status'] !== 404){
                  this.setState({workWeather:weatherData['properties']['periods']['0']['shortForecast']})
               
                  if (listContains(this.state.workWeather,badWeather)){
                    this.setState({workWeatherPenalty:15});
                  } 
                  else{
                    this.setState({workWeatherPenalty:0});
                  }
              }
              else{
                this.setState({workWeather:'WORK WEATHER: No weather data found'})
                this.setState({workWeatherPenalty:0})
              } 
              })
            })
      )  
    );
    
    loadFromUrl(CUPurl, store).then(() =>
      prepare(homeGeoQS, store).then(qry =>
        execute(qry, store).then(results => {
          this.setState({ homeGeo: results });
          this.state.homeGeo.map(hG => {
            this.setState({homeLat:hG.lat,homeLon:hG.lon,homeWeatherAPI:"https://api.weather.gov/points/" + hG.lat.toString() + "," + hG.lon.toString() + "/forecast"})
          })
          fetch(this.state.homeWeatherAPI).then(response => response.json()).then(weatherData => {
            if(weatherData['status'] !== 404){
              this.setState({homeWeather:weatherData['properties']['periods']['0']['shortForecast']})
              if (listContains(this.state.homeWeather,badWeather)){
                this.setState({homeWeatherPenalty:15});
              } 
              else{
                this.setState({homeWeatherPenalty:0});
              }
            }
            else{
              this.setState({homeWeather:'HOME WEATHER: No weather data found'})
              this.setState({homeWeatherPenalty:0})
            }
          });
        })  
      )
    ).then(() => {
      //MAPQUEST API
      const MAPQUEST_API = 'http://www.mapquestapi.com/directions/v2/route?key=DMw31TyjrjAee8VA1XAKnGGqw5sg7Nm3&from=';
      const TO = '&to=';
      const HOMELOC = this.state.homeLat +","+this.state.homeLon
      const WORKLOC = this.state.worklat +","+this.state.workLon
      const ROUTETYPE = "&routeType=" //+ this.state.transPref
      
      fetch(MAPQUEST_API + HOMELOC + TO + WORKLOC + ROUTETYPE + routeTypes[0])
      .then(response =>  response.json())
      .then(resData => {
        
        let time = resData.route["time"]/60;
        this.setState({ driveTimeB: time }); 
      }).then(()=>

      fetch(MAPQUEST_API + HOMELOC + TO + WORKLOC + ROUTETYPE + routeTypes[1])
      .then(response =>  response.json())
      .then(resData => {
        
        let time = resData.route["time"]/60;
        this.setState({ driveTimeP: time }); 
      }).then(()=>

      fetch(MAPQUEST_API + HOMELOC + TO + WORKLOC + ROUTETYPE + routeTypes[2])
      .then(response =>  response.json())
      .then(resData => {
        
        let time = resData.route["time"]/60;
        this.setState({ driveTimeF: time }); 
      }).then(()=>

      fetch(MAPQUEST_API + HOMELOC + TO + WORKLOC + ROUTETYPE + routeTypes[3])
      .then(response =>  response.json())
      .then(resData => {
        
        let time = resData.route["time"]/60;
        this.setState({ driveTimeS: time }); 
      }))))
    })
    
    this.setState({ name, image, isLoading: false, hasImage});
    };
    
    updatePhoto = async (uri: String, message) => {
        try {
          const { user } = data;
          this.state.hasImage
            ? await user.image.set(uri)
            : await user.image.add(uri);
    
          this.props.toastManager.add(['', message], {
            appearance: 'success'
          });
        } catch (error) {
          this.props.toastManager.add(['Error', error.message], {
            appearance: 'error'
          });
        }
    };    

    render() {
        const { name, image, isLoading,driveTimeP,driveTimeB,driveTimeS,driveTimeF,homeWeather,homeWeatherPenalty,workWeather,workWeatherPenalty,workGeo,homeGeo,transPref,test } = this.state;

        return (
        <SetupPageContent
            name={name}
            image={image}
            isLoading={isLoading}
            webId={this.props.webId}
            updatePhoto={this.updatePhoto}
            
            driveTimeP={driveTimeP}
            driveTimeB={driveTimeB}
            driveTimeS={driveTimeS}
            driveTimeF={driveTimeF}

            homeWeather={homeWeather}
            homeWeatherPenalty={homeWeatherPenalty}
            
            workWeather={workWeather}
            workWeatherPenalty={workWeatherPenalty}
            
            workGeo={workGeo}
            homeGeo = {homeGeo}
            transPref={transPref}
            test={test}
        />
        );
    }
}

export default withWebId(withToastManager(SetupComponent));
