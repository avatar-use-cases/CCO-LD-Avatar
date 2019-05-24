
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javax.print.attribute.HashAttributeSet;

import org.apache.jena.query.*;

/////////////////////////////////////////////////////////////////////////////////////////
////// This class creates a query for one or multiple subjects defined by the user //////
////// Parameters you need to give it:                                             //////
////// A Set of strings containing subjects                                        //////
////// An integer limit of the number of results the user wants to return          //////
////// A 2D array of variable names and the constants that it should be bound to   //////
////// Should contain an empty string if the user does not want to bind it         //////
/////////////////////////////////////////////////////////////////////////////////////////

public class CustomQuery {
    // The query in text format
    private String textBody = "";
    // The result of the query after it is run
    private String result = "";
    // The variables the user wants to return
    private String header = "";

    private Set<String> subjects = new HashSet<String>();
    private String[] returns = null;
    // end of variable decloration

    // setters and geters for attributes
    public String getTextBody() {
        return this.textBody;
    }

    public void setTextBody(String text) {
        this.textBody = text;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getHeader() {
        return this.header;
    }

    public Set<String> getSubjects() {
        return this.subjects;
    }

    public void setSubjects(Set<String> s) {
        this.subjects = s;
    }


    public String[] getReturns() {
        return this.returns;
    }

    public void setReturns(String[] returns) {
        String currSubject = "None";
        if (returns != null) {
            this.returns = returns;
        } else {
            try {
                Set<String> subs = this.getSubjects();
                for (String i : subs) {
                    currSubject = i;
                    BufferedReader querySubjects = new BufferedReader(
                            new FileReader("./queryTemplates/querySubjects.txt"));
                    String line;
                    while ((line = querySubjects.readLine()) != null) {
                        if (line.startsWith("#")) {
                            continue;
                        } else if (i.toLowerCase().equals(line.split(":")[0].toLowerCase())) {
                            BufferedReader query = new BufferedReader(new FileReader(line.split(":")[1]));
                            String[] arr = query.readLine().split(" ");
                            if (this.returns != null) {
                                String[] tmp = this.returns;
                                int count = 0;
                                this.returns = new String[tmp.length + arr.length];
                                for (String x : tmp) {
                                    this.returns[count] = x;
                                    count++;
                                }
                                for (String p : arr) {
                                    this.returns[count] = p;
                                    count++;
                                }
                            } else {
                                this.returns = arr;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("No query file found for " + currSubject);
            }
        }
    }

    // Constructors
    
    // Creates a custom query object for a secured triple store with basic auth
    public CustomQuery(Set<String> templates) {
        writeHeadersFromTemplates(templates);
        this.textBody += "where{\n";
        // if (constants != null) {
        //     setBindings(constants);
        // }
        wirteQueryFromTemplates(templates);
        this.textBody += "}";
        // if (!orderBy.isEmpty()) {
        //     addOrderBy(orderBy);
        // }
        // addQueryResultLimit(limit);
        // if (integratedVars != null) {
        //     integrate(integratedVars);
        // }
        // this.setSubjects(subjects);
        // this.setReturns(constants[0]);
        // this.writeOutputLine(subjects, returns);
        // this.addQueryBody(subjects, constants);
        // this.textBody += "}";
        // this.addQueryResultLimit(limit);
        // this.runQuery(username, password, tripleStore);
        // System.out.println(this.textBody);
    }

    public void writeHeadersFromTemplates(Set<String> templates) {
        this.textBody += "select distinct ";
        for (String template : templates) {
            for (String i : template.split("\n")) {
                this.textBody += i + " ";
                break;
            }
        }
        this.textBody += "\n";
        // System.out.println(textBody);
    }

    public void wirteQueryFromTemplates(Set<String> templates) {
        for (String template : templates) {
            int count = 0;
            for (String line : template.split("\n")) {
                // skip the header line
                if (count == 0) {
                    count++;
                    continue;
                }
                this.textBody += line + "\n";
            }
        }
    }

    // selects the output"
    public Set<String> selectOutput(String afile, String[] array) throws Exception {
        // opens the file and reads the first line of the query template which
        // has all the variable names
        Set<String> returnSet = new HashSet<String>();
        BufferedReader f;
        String firstLine;
        try {
            f = new BufferedReader(new FileReader(afile));
            firstLine = f.readLine();
            String[] arr = firstLine.split(" ");
            for (int j = 0; j < array.length; j++) {
                String varYouWanaReturn = array[j];
                for (int i = 0; i < arr.length; i++) {
                    // make sure to pass in lowercase params to array
                    if (arr[i].contains(varYouWanaReturn)) {
                        returnSet.add(arr[i]);
                    }
                }
            }
            f.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file" + afile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnSet;
    }

    // sets the limit of the query
    public void addQueryResultLimit(Integer limit) {
        this.textBody += "Limit " + Integer.toString(limit);
    }

    // runs the query stored in query.textBody
    public void runQuery(String user, String password, String tripleStore) {
        try {
            Query query = QueryFactory.create(this.textBody, Syntax.syntaxARQ);
            String auth = "http://" + user + ":" + password + "@localhost:3030/" + tripleStore + "/sparql";
            QueryExecution qe = QueryExecutionFactory.sparqlService(auth, query);
            ResultSet resultSet = qe.execSelect();
            String set = "{\"results\": [";
            while (resultSet.hasNext()) {
                // Formats the default output to json format
                String tmp = resultSet.next().toString();
                tmp = "{\"" + tmp.replace(" ( ", "").replace(" )", ",\"").replace(" = ", "\":").replace("( ", "");
                tmp = tmp.substring(0, tmp.length() - 2);
                set += tmp + "},";
            }
            set = set.substring(0, set.length() - 2).replace("?", "");
            set += "}]}";
            this.setResult(set.replace("\n", ""));
            System.out.println(this.getResult());
            if (this.getResult().equals("{\"results\":}]}")) {
                this.setResult("{\"results\":[]}");
            }
        } catch (QueryParseException e) {
            System.out.println("Could not create a query with parameters provided. Check your format.");
            System.out.println("The generated query was : \n" + this.textBody);
        } catch (Exception e) {
            System.out.println("everything is broken");
            e.printStackTrace();
        }
    }

    // runs the generated query on an open tripleStore with no authentication
    public void runQuery(String tripleStore) {
        try {
            Query query = QueryFactory.create(this.textBody, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.sparqlService("http://localhost:3030/" + tripleStore + "/sparql",
                    query);
            ResultSet resultSet = qe.execSelect();
            String set = "{\"results\": [";
            // Check to see if set is empty
            while (resultSet.hasNext()) {
                // Formats the default output to json format
                String tmp = resultSet.next().toString();
                tmp = "{\"" + tmp.replace(" ( ", "").replace(" )", ",\"").replace(" = ", "\":").replace("( ", "");
                tmp = tmp.substring(0, tmp.length() - 2);
                set += tmp + "},";
            }
            set = set.substring(0, set.length() - 2).replace("?", "");
            set += "}]}";
            this.setResult(set.replace("\n", ""));
            System.out.println(this.getResult());
            if (this.getResult().equals("{\"results\":}]}")) {
                this.setResult("{\"results\":[]}");
            }
        } catch (QueryParseException e) {
            System.out.println("Could not create a query with parameters provided. Check your format.");
            System.out.println("The generated query was : \n" + this.textBody);
        } catch (Exception e) {
            System.out.println("everything is broken");
            e.printStackTrace();
        }
    }

    // Binds certain variables as constants
    public void setBindings(String[][] constants) {
        for (int i = 0; i < constants[0].length; i++) {
            if (constants[1][i] != "") {
                this.textBody += "Bind( '" + constants[1][i] + "' as ?" + constants[0][i] + ")\n";
            }
        }
    }

    // Adds an orderby clause aloowing the results to be ordered
    public void addOrderBy(String orderBy) {
        this.textBody += "order by ?" + orderBy + "\n";
    }

    // Your typical toString method
    public String toString() {
        return this.getTextBody();
    }

    // not ideal but works
    public void integrate(String[][] integratableVars) {
        String integratedQuery = this.getTextBody().replace("select distinct", "");
        int count = 0;
        for (String[] i : integratableVars) {
            for(String x : i){
                System.out.println(x);
                integratedQuery = integratedQuery.replace("?" + x, "?IntegratedVar"+count);
            }
            String[] tmp = integratedQuery.split("\n");
            ArrayList<String> intArr = new ArrayList(Arrays.asList(tmp));
            String header = intArr.get(0);
            System.out.println(header);
            HashSet<String> set = new HashSet<String>();
            for(String x: header.split(" ")){
                set.add(x);
            }
            String newline = "";
            for(String x: set){
                newline += x + " ";
            }
            intArr.remove(0);
            intArr.add(0, newline);
            integratedQuery = "";
            for(String x: intArr){
                integratedQuery += x + "\n";
            }
            count++;
        }
        System.out.println(integratedQuery);
        this.setTextBody("select distinct" + integratedQuery);
    }
}
