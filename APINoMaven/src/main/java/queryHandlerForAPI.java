
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

// import jena libraries
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.Lang;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

// import stuff for parsing json
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.util.*;
import java.io.BufferedReader;
import java.util.concurrent.TimeoutException;

public class queryHandlerForAPI extends AbstractHandler {
    private final String greeting;
    private final String body;
    private String contentType;
    // params to determine which query constructor to use
    private Set<String> subjects = new HashSet<String>();
    private int limit = 5;
    private String[][] constants = null;
    private String orderBy = "";
    private String[][] integratedVars = null;
    private String data;
    private HashMap<String, String> newData = new HashMap<String, String>();

    // Getters and setters for query params
    public void setSubjects(String subject) {
        this.subjects = new HashSet<String>();
        String[] tmp = subject.split("-");
        for (int i = 0; i < tmp.length; i++) {
            this.subjects.add(tmp[i]);
        }
    }

    public Set<String> getSubject() {
        return this.subjects;
    }

    public void setLimit(String limit) {
        this.limit = Integer.parseInt(limit);
    }

    public int getLimit() {
        return this.limit;
    }

    public void setContentType(String type) {
        this.contentType = type;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setConstants(String constants) {
        String[] tmp = constants.split(":");
        this.constants = new String[2][tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            String[] tmp2 = tmp[i].split("-");
            this.constants[0][i] = tmp2[0];
            try {
                this.constants[1][i] = tmp2[1];
            } catch (Exception e) {
                this.constants[1][i] = "";
            }
        }
    }

    public String[][] getConstants() {
        return this.constants;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public void setIntegrationVar(String i) {
        String[] tmp = i.split(":");
        String[][] vars = new String[i.split(":").length][];
        int count = 0;
        for (String s : tmp) {
            String[] x = s.split("-");
            vars[count] = x;
            count++;
        }

        this.integratedVars = vars;
    }

    public String[][] getIntegrationVar() {
        return this.integratedVars;
    }

    // End of getters for query params

    public queryHandlerForAPI() {
        this("query");
    }

    public queryHandlerForAPI(String greeting) {
        this(greeting, null);
    }// constructor

    public queryHandlerForAPI(String greeting, String body) {
        this.greeting = greeting;
        this.body = body;
    }// constructor

    // This method is run when the handler is called by avatarServerV1
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        outerloop: while (true) {
            Enumeration<String> enumer = baseRequest.getHeaderNames();
            this.setSubjects("");
            while (enumer.hasMoreElements()) {
                String p = enumer.nextElement().toLowerCase();
                // set variables based off stuff given in header
                // System.out.println(p);
                if (p.toLowerCase().equals("content-type")) {
                    setContentType(baseRequest.getHeader(p));
                }
            }
            System.out.println(this.contentType);
            if(this.contentType == null){
                PrintWriter out = response.getWriter();
                System.out.println("No content-type header found. Read Readme for more information");
                out.println("No content-type header found. Read Readme for more information");
                break outerloop;
            }
            // Get the body of the request
            BufferedReader reader = baseRequest.getReader();
            String line;
            StringBuilder requestBody = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                requestBody.append(line + "\n");
            }
            this.data = requestBody.toString();
            // System.out.println(this.data);
            switch (this.contentType) {
            case "application/xml":
                try {
                    this.newData = parseXML(this.data);
                    // System.out.println(this.newData);
                }catch(ParseException e){
                    PrintWriter out = response.getWriter();
                    System.out.println("Error parsing input. Make sure your data is in XML format or your context-type header is set to the correct input format");
                    out.println("Error parsing input. Make sure your data is in XML format or your context-type header is set to the correct input format");
                    e.printStackTrace();
                    break outerloop;
                }
                break;
                // end of application/xml case
            case "application/json":
                // parse the body of the request
                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(this.data);
                    JSONObject jsonObj = (JSONObject) obj;
                    JSONArray arr = (JSONArray) jsonObj.get("data");
                    for (Object i : arr) {
                        JSONObject tmpObject = (JSONObject) i;
                        this.getSubject().add(tmpObject.get("rootNode").toString());
                        String tmpData = tmpObject.get("rdf").toString();
                        newData.put(tmpObject.get("rootNode").toString(), tmpObject.get("rdf").toString());
                    }
                    // System.out.println(newData);
                    // System.out.println(this.getSubject());
                } catch (ParseException e) {
                    PrintWriter out = response.getWriter();
                    System.out.println("Error parsing input. Make sure your data is in JSONLD format or your context-type header is set to the correct input format");
                    out.println("Error parsing input. Make sure your data is in JSONLD format or your context-type header is set to the correct input format");
                    e.printStackTrace();
                    break outerloop;
                }
                // Transform the JSON data in to rdf format
                for (String key : newData.keySet()) {
                    Dataset ds = DatasetFactory.create();
                    // System.out.println(newData.get(key));
                    try (InputStream in = new ByteArrayInputStream(newData.get(key).getBytes(StandardCharsets.UTF_8))) {
                        RDFParser.create().source(in).lang(Lang.JSONLD).parse(ds.asDatasetGraph());
                    } catch (IOException e) {
                        PrintWriter out = response.getWriter();
                        System.out.println("Error reytrieving data with key of " + key + " from data map");
                        e.printStackTrace();
                        break outerloop;
                    }
                    Model model = ds.getDefaultModel();
                    String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE" and "TURTLE"
                    StringWriter out = new StringWriter();
                    model.write(out, syntax);

                    String result = out.toString();

                    newData.put(key, result);
                }
                // System.out.println(newData);
                break;
                // end of application/json case
            }

            // Create query templates from transformed data
            Set<String> queries = new HashSet<String>();
            for (String key : newData.keySet()) {
                QueryTemplateFactory f = new QueryTemplateFactory(newData.get(key), key);
                queries.add(f.getQuery());
            }

            // QueryTemplateFactory f = new
            // QueryTemplateFactory(newData.get("Airport"),"Airport");

            String output = "{\n\"query\": \"";
            // System.out.println(queries);
            CustomQuery q = new CustomQuery(queries);
            if (q.getTextBody().isEmpty()) {
                try {
                    throw new AssertionError();
                } catch (AssertionError e) {
                    PrintWriter out = response.getWriter();
                    System.out.println("Error encountered while generating query");
                    out.println("{\"Error\":\"Error encountered while generating query\"");
                    e.printStackTrace();
                    break outerloop;
                }
            } else {
                output += q.getTextBody().replace("\n","") + "\"\n}";
            }
            System.out.println(q.getTextBody());
            PrintWriter out = response.getWriter();

            // getting the path of the request
            String path = request.getPathInfo();
            // System.out.println(this.getConstants());
            Model m = ModelFactory.createDefaultModel();
            // make sure the query works
            for (String key : newData.keySet()) {
                String rdf = newData.get(key);
                m.read(new ByteArrayInputStream(rdf.getBytes()), null, "RDF/XML");
            }
            // Dataset ds = DatasetFactory.create(m);
            // System.out.println(ds.isEmpty());
            boolean check = checkQuery(new CustomQuery(queries).getTextBody(), m);
            if(check){
                System.out.println("Query works !!!!");
                out.println(output);
            }
            else{
                out.println("{\"Error\":\"Initial QueryTests failed\"");
            }
            // System.out.println(output);
            
            // query = new CustomQuery();
            break outerloop;
        }
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }// handle
    

    public boolean checkQuery(String queryText, Model m) {
        try {
            Query query = QueryFactory.create(queryText, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.create(query,m);
            ResultSet resultSet = qe.execSelect();
            // ResultSet resultSet = con.query(queryText).execSelect();
            if (resultSet.hasNext()) {
                return true;
            }
            return false;
        } catch (QueryParseException e) {
            System.out.println("Could not create a query with parameters provided. Check your format.");
            System.out.println("Format should be: query/Subjects/numberOfResults/options/tripleStore");
        } catch (Exception e) {
            System.out.println("everything is broken");
            e.printStackTrace();
        }
        return false;
    }
    private HashMap<String,String> parseXML(String xml) throws ParseException {
        HashMap<String,String> map = new HashMap<String,String>();
        boolean trigger = false;
        boolean rdftrig = false;
        String rdf = "";
        String rootNode = "";
        if(!xml.startsWith("<")){
            throw new ParseException(0);
        }
        for(String line: xml.split("\n")){
            // get subject
            if(line.contains("<Data>")){
                trigger = true;
            }
            else if(line.contains("</Data>")){
                map.put(rootNode,rdf);
                trigger = false;
                rdf = "";
                rootNode = "";
            }
            else if(trigger && line.contains("<RootNode>")){
                try {
                    rootNode = line.split(">")[1].split("</")[0];
                }catch(IndexOutOfBoundsException e){
                    rootNode = "";
                    for (String x: xml.split("\n")){
                        boolean tmptrig = false;
                        if(x.contains("<RootNode>")){
                            tmptrig = true;
                        }
                        else if(tmptrig){
                            rootNode += x;
                        }else if(x.contains("</RootNode>")){
                            tmptrig = false;
                            break;
                        }
                    }
                }
            }
            else if (trigger && line.contains("<rdf")){
                rdftrig = true;
                rdf += line + "\n";
            }
            else if (rdftrig && !line.contains("</rdf>")){
                rdf += line + "\n";
                continue;
            }
            else if (rdftrig && line.contains("</rdf>")){
                rdf += line + "\n";
                rdftrig = false;
            }
        }
        return map;
    }
}// queryHandlerForAPI
