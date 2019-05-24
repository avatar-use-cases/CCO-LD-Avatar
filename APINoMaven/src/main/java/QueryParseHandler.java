
// java.io
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

// java.util
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;

// javax servlet
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// jetty stuff
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

// JSON parsing stuff
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class QueryParseHandler extends AbstractHandler {
    private final String greeting;
    private final String body;

    private String query = "";
    private ArrayList<String> variables = new ArrayList<String>();
    private long limit = 5;
    private String[][] integratedVars = null;
    private String[][] constants = null;
    private String orderBy = null;

    // start getters and setters

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

    // end getters and setters

    public QueryParseHandler() {
        this("parse");
    }

    public QueryParseHandler(String greeting) {
        this(greeting, null);
    }// constructor

    public QueryParseHandler(String greeting, String body) {
        this.greeting = greeting;
        this.body = body;
    }// constructor

    // This method is run when the handler is called by avatarServerV1
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Enumeration<String> enumer = baseRequest.getHeaderNames();
        // while (enumer.hasMoreElements()) {
        // String p = enumer.nextElement().toLowerCase();
        // // set variables based off stuff given in header
        // if (p.equals("query")) {
        // // this.query = baseRequest.getHeader(p);
        // } else if (p.equals("variables")) {
        // this.variables = new
        // ArrayList<String>(Arrays.asList(baseRequest.getHeader(p).split("-")));
        // }
        // }

        BufferedReader reader = baseRequest.getReader();
        String line;
        StringBuilder requestBody = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            // System.out.println(line);
            requestBody.append(line + "\n");
        }
        String JSONbody = requestBody.toString();
        // parse JSON body
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(JSONbody);
            JSONObject jsonObj = (JSONObject) obj;
            // create the query to parse
            JSONArray queryArr = (JSONArray) jsonObj.get("query");
            for (Object i : queryArr) {
                line = (String) i;
                this.query += line;
            }

            // if the user gives their query as a one line input
            if(this.query.split("\n").length == 1){
                this.query = this.query.replace(".?",".\n?");
                this.query = this.query.replace("{","{\n");
                this.query = this.query.replace("where{"," \nwhere{");
            }

            System.out.println("QUERY IS: " + this.query);
            // get variables to return
            try {
                JSONArray variableArr = (JSONArray) jsonObj.get("variables");
                for (Object i : variableArr) {
                    String var = (String) i;
                    if(!this.variables.contains(var)){
                        this.variables.add(var);
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("WARNING: No output variables given");
                this.variables = null;
            }

            // get limit
            try {
                this.limit = (long) jsonObj.get("limit");
            } catch (NullPointerException e) {
                System.out.println("WARNING: No limit given, defaulting to 5 results");
                this.limit = 5;
            }
            // get constants
            try {
                JSONArray constantsArr = (JSONArray) jsonObj.get("constants");
                String constant = "";
                for (Object i : constantsArr) {
                    constant += (String) i + ":";
                }
                // remove last ":"
                constant = constant.substring(0, constant.length() - 1);
                this.setConstants(constant);
            } catch (NullPointerException e) {
                this.constants = null;
            }
            // get integrated vars
            try {
                JSONArray integratedArr = (JSONArray) jsonObj.get("integrate");
                String integrate = "";
                for (Object i : integratedArr) {
                    integrate += (String) i + ":";
                }
                // remove last ":"
                integrate = integrate.substring(0, integrate.length() - 1);
                this.setIntegrationVar(integrate);
            } catch (NullPointerException e) {
                this.integratedVars = null;
            }
            // get order by
            try {
                this.orderBy = (String) jsonObj.get("orderBy");
            } catch (NullPointerException e) {
                this.orderBy = null;
            }
            // System.out.println(newData);
            // System.out.println(this.getSubject());
        } catch (ParseException e) {
            PrintWriter out = response.getWriter();
            System.out.println(
                    "Error parsing input. Make sure your data is in JSON format or your context-type header is set to the correct input format");
            out.println(
                    "Error parsing input. Make sure your data is in JSON format or your context-type header is set to the correct input format");
            e.printStackTrace();
        }
        // parse query
        System.out.println("Query is : " + this.query);
        QueryParser p = new QueryParser(this.query, this.variables, this.limit, this.constants, this.integratedVars,
                this.orderBy);
        String newQuery = p.parse();
        System.out.println(newQuery);
        PrintWriter out = response.getWriter();

        // getting the path of the request
        String path = request.getPathInfo();
        // System.out.println(this.getConstants());
        out.println("{\"query\":\"" + newQuery.replace("where {"," where {").replace("\n", "") + "\"}");
        // empty query cause of wierd bug
        this.query = "";
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }// handle

}