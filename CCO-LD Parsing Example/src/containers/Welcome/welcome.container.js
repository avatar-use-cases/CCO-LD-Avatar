import React, { Component } from "react";
import WelcomePageContent from "./welcome.component";
import { withWebId } from "@inrupt/solid-react-components";
import data from "@solid/query-ldflex";

import $rdf from "rdflib";

// RG - 2019-02-28
// The store is a local rdf store to hold data retrieved from Solid
// fetcher is the object that will retrieve remote data from solid
// RG - 2019-04-12
// updater is the object that will push store updates to the remote solid
const store = $rdf.graph();
const fetcher = new $rdf.Fetcher(store);
const updater = new $rdf.UpdateManager(store)
const VCARD = new $rdf.Namespace("http://www.w3.org/2006/vcard/ns#");

// RG - 2019-02-28
// Loads the data from a URL into the local store
const loadFromUrl = (url, store) => {
  return new Promise((resolve, reject) => {
    //let fetcher = new $rdf.Fetcher(store);
    try {
      fetcher.load(url).then(response => {
        resolve(response.responseText);
        console.debug(response.responseText);
        $rdf.parse(response.responseText, store, $rdf.sym(url).uri, "text/turtle");
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
    //console.debug("here");
    const wanted = qry.vars;
    const resultAry = [];
    store.query(
      qry,
      results => {
        //console.debug("here1");
        if (typeof results === "undefined") {
          reject("No results.");
        } else {
          let row = rowHandler(wanted, results);
          //console.debug(row);
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

// Manually-created vcard#hasPhoto context link
const hasPhotoContext = "http://www.w3.org/2006/vcard/ns#hasPhoto";
// img context
const imgContext = "http://xmlns.com/foaf/0.1/img";

/**
 * Container component for the Welcome Page, containing example of how to fetch data from a POD
 */
class WelcomeComponent extends Component<Props> {
  constructor(props) {
    super(props);

    this.state = {
      name: "",
      image: "",
      isLoading: false
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

  /**
   * This function retrieves a user's card data and tries to grab both the user's name and profile photo if they exist.
   *
   * This is an example of how to use the LDFlex library to fetch different linked data fields.
   */
  getProfileData = async () => {
    this.setState({ isLoading: true });

    /*
     * This is an example of how to use LDFlex. Here, we're loading the webID link into a user variable. This user object
     * will contain all of the data stored in the webID link, such as profile information. Then, we're grabbing the user.name property
     * from the returned user object.
     */
    const user = data[this.props.webId];
    const nameLd = await user.name;

    const name = nameLd ? nameLd.value : "";

    let imageLd = await user[imgContext];
    imageLd = imageLd ? imageLd : await user[hasPhotoContext];

    const image = imageLd ? imageLd.value : "/img/icon/empty-profile.svg";

    // RG - 2019-02-28
    // Load data from two public Solid URLs into the local store, and run a query on the combined data
    // Put the data into the Component state so that it can be rendered

    // RG - 2019-04-12
    // url1 and url2 are individual profile data
    const url1 = "https://rganger.solid.community/profile/card#me";
    const url2 = "https://jacobmcconomy.solid.community/profile/card#me";

    // RG - 2019-04-12
    // url3 is data that Jacob loaded into his public profile
    const url3 = "https://jacobmcconomy.solid.community/public/JohnDoe.ttl";

    // RG - 2019-04-12
    // url4 is a subject in rganger's profile; this is updated by the app below
    const url4 = "https://rganger.solid.community/profile/card#id1554811131416";

    // RG - 2019-04-12
    // doc is the url of the remote resource that will be updated
    const doc = "https://rganger.solid.community/profile/card";

    // RG - 2019-04-12
    // query strings are used to access data in the local store after it has been retrieved from solid
    const qryStr1 = "select * where { ?s ?p ?o . }";
    const qryStr2 = `
    PREFIX cco: <http://www.ontologyrepository.com/CommonCoreOntologies/>
    select * where {
      ?a a cco:Person .
      ?a ?b ?c .
    }
    `;
    const qryStr3 = `
    PREFIX cco: <http://www.ontologyrepository.com/CommonCoreOntologies/>
    PREFIX rdf:https://rdflib.readthedocs.io/en/4.2.2/namespaces_and_bindings.html
    SELECT DISTINCT ?TextName
    WHERE {
      ?P rdf:type cco:Person ; cco:designated_by ?PFN .
      ?PFN rdf:type cco:PersonFullName ; cco:inheres_in ?IBE .
      ?IBE rdf:type cco:InformationBearingEntity ; cco:has_text_value ?TextName .
    }
    `;

    // RG - 2019-04-12
    // This statement calls fundtions above to load data from solid
    // The resuls are added to the properties of this object so that they can be sent to the renderer
    loadFromUrl(url1, store).then(() =>
      prepare(qryStr1, store).then(qry =>
        execute(qry, store).then(results => {
          console.debug(results);
          this.setState({ results: results });
        })
      )
    );

    // RG - 2019-04-12
    // This loads some data into the props that will be used to make an update later
    // We can't call the update synchronously because the load function above hasn't completed yet!!
    // Needed for the update:
    // $rdf == the local store. This somehow will automatically sync updates we push to it with the remote solid store
    // updater == the rdflib object that updates a store.
    // updateUrl == the "subject" of the update
    // updateType == the "predicate" of the update
    // updateValue == the "object" of the update
    // updateDoc == the URL where the update will occur. The naming of this is not intuitive.
    this.setState({ rdf: $rdf, updater: updater, updateUrl: url4, updateType: VCARD("postal-code"), updateValue: "14226", updateDoc: doc });

    // RG - 2019-04-12
    // Commented stuff here is failed attempts, etc.

    //this.setState({ updateFunc: updateStore });

    //updateStore(url1, VCARD("postal-code"), "14225", doc);
    // loadFromUrl(url3, store).then(() =>
    //   prepare(qryStr3, store).then(qry =>
    //     execute(qry, store).then(results => {
    //       console.debug(results);
    //       this.setState({ results: results });
    //     })
    //   )
    // );
    //loadFromUrl(url3, store).then(() => {
    //  console.debug(store.match());
    //  this.setState({ results: store.match() });
    //});
    //  loadFromUrl(url1, store).then(() =>
    //    loadFromUrl(url2, store).then(() =>
    //      prepare(qryStr, store).then(qry =>
    //        execute(qry, store).then(results => {
    //          console.debug(results);
    //          this.setState({ results: results });
    //        })
    //      )
    //    )
    //  );
    // console.debug("Completed load and query");

    /**
     * This is where we set the state with the name and image values. The user[hasPhotoContext] line of code is an example of
     * what to do when LDFlex doesn't have the full context. LDFlex has many data contexts already in place, but in case
     * it's missing, you can manually add it like we're doing here.
     *
     * The hasPhotoContext variable stores a link to the definition of the vcard ontology and, specifically, the #hasPhoto
     * property that we're using to store and link the profile image.
     *
     * For more information please go to: https://github.com/solid/query-ldflex
     */
    this.setState({ name, image, isLoading: false });
  };

  render() {
    // RG 2019-02-28
    // Send the query results to the props of the page to be rendered
    const { name, image, isLoading, results, rdf, updater, updateUrl, updateType, updateValue, updateDoc } = this.state;
    return <WelcomePageContent name={name} image={image} isLoading={isLoading} results={results} rdf={rdf} updater={updater} updateUrl={updateUrl} updateType={updateType} updateValue={updateValue} updateDoc={updateDoc}/>;
  }
}

export default withWebId(WelcomeComponent);
