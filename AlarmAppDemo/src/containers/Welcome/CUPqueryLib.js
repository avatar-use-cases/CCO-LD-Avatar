
// function CUPqueryLib(){

//     import $rdf from "rdflib";
//     const store = $rdf.graph();

//     const loadFromUrl = (url, store) => {
//         return new Promise((resolve, reject) => {
//         let fetcher = new $rdf.Fetcher(store);
//         try {
//             fetcher.load(url).then(response => {
//             resolve(response.responseText);
//             console.debug(response.responseText);
//             });
//         } catch (err) {
//             reject(err);
//         }
//         });
//     };

//     const prepare = (qryStr, store) => {
//         return new Promise((resolve, reject) => {
//         try {
//             let query = $rdf.SPARQLToQuery(qryStr, false, store);
//             resolve(query);
//         } catch (err) {
//             reject(err);
//         }
//         });
//     };

//     const execute = (qry, store) => {
//         return new Promise((resolve, reject) => {
//         console.debug("here");
//         const wanted = qry.vars;
//         const resultAry = [];
//         store.query(
//             qry,
//             results => {
//             console.debug("here1");
//             if (typeof results === "undefined") {
//                 reject("No results.");
//             } else {
//                 let row = rowHandler(wanted, results);
//                 console.debug(row);
//                 if (row) resultAry.push(row);
//             }
//             },
//             {},
//             () => {
//             resolve(resultAry);
//             }
//         );
//         });
//     };

//     const rowHandler = (wanted, results) => {
//         const row = {};
//         for (var r in results) {
//         let found = false;
//         let got = r.replace(/^\?/, "");
//         if (wanted.length) {
//             for (var w in wanted) {
//             if (got === wanted[w].label) {
//                 found = true;
//                 continue;
//             }
//             }
//             if (!found) continue;
//         }
//         row[got] = results[r].value;
//         }
//         return row;
//     };

//     const fetchFromCUP = () =>{
//         loadFromUrl(url3, store).then(() =>
//         prepare(qryStrQ, store).then(qry =>
//         execute(qry, store).then(results => {
        
//         console.debug(results);
//         console.debug("Completed query");
//         return results;
//         })
//     )

//     );
    

//     }

//     const qLastName = "prefix cco: <http://www.ontologyrepository.com/CommonCoreOntologies/> . select distinct ?o where {?s a cco:Person; cco:designated_by ?name . ?name a cco:FamilyName . ?name cco:inheres_in ?ibe . ?ibe cco:has_text_value ?o .}";
//     const qFirstName = "prefix cco: <http://www.ontologyrepository.com/CommonCoreOntologies/> . select distinct ?o where {?s a cco:Person; cco:designated_by ?name . ?name a cco:GivenName . ?name cco:inheres_in ?ibe . ?ibe cco:has_text_value ?o .}";
//     const qHomeAddress = "prefix cco: <http://www.ontologyrepository.com/CommonCoreOntologies/> . prefix ro: <http://www.obofoundry.org/ro/ro.owl#> . select distinct ?streetAddress ?cityname ?statename where {?person a cco:Person . ?person cco:uses ?residence . ?residence a cco:ResidentialFacility .?residence cco:designated_by ?resAddr .?resAddr a cco:StreetAddress .?resAddr cco:inheres_in/cco:has_text_value ?streetAddress .?residence ro:located_in ?tmpCity .?tmpCity a cco:City;cco:designated_by/cco:inheres_in/cco:has_text_value ?cityname.?residence ro:located_in ?tmpState .?tmpState a cco:State;cco:designated_by/cco:inheres_in/cco:has_text_value ?statename .}"
// }

const query = () => {
    console.log("yassss slay!");
}

module.exports = {query}