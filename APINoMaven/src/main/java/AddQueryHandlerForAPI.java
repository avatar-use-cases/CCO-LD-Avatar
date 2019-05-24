
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Scanner;
import java.io.FileReader;
import java.io.InputStreamReader;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.io.FileWriter;

import java.util.HashMap;
import java.util.HashSet;

//////////////////////////////////////////////////////////////////
////// Takes in an initial subject and tells the user what  //////
////// other subjects can be integrated with it             //////
//////////////////////////////////////////////////////////////////

public class AddQueryHandlerForAPI extends AbstractHandler {
    // Variable declorations
    private final String greeting;
    private final String body;
    private final String extensionAPIEndpoint = "http://localhost:8080";
    private String rootNode;
    private String data;
    private final String queryfile = "./Queries.txt";
    private HashMap<String, String> bindings = new HashMap<String, String>();
    private HashSet<String> vars = new HashSet<String>();

    // Getters and Setters
    public String getRootNode() {
        return this.rootNode;
    }

    public void setRootNode(String e) {
        this.rootNode = e;
    }
    // End of getters and setters

    public AddQueryHandlerForAPI() {
        this("AVATAR LOGIN");
    }

    public AddQueryHandlerForAPI(String greeting) {
        this(greeting, null);
    }// constructor

    public AddQueryHandlerForAPI(String greeting, String body) {
        this.greeting = greeting;
        this.body = body;
    }// constructor

    // This method is run handler is called in avatarServerV1
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String clientReqTopic = "";
        Enumeration<String> x = baseRequest.getHeaderNames();
        while (x.hasMoreElements()) {
            String p = x.nextElement();
            if (p.toLowerCase().equals("rootnode")) {
                this.setRootNode(baseRequest.getHeader(p));
            }
        }

        // Read the request body
        BufferedReader reader = baseRequest.getReader();
        String line;
        StringBuilder requestBody = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            // System.out.println(line);
            requestBody.append(line + "\n");
        }
        this.data = requestBody.toString();
        // Add the RDF Data
        String output = "";
        System.out.println(this.getRootNode());
        try {
            try {
                String query;
                BufferedReader r = new BufferedReader(new FileReader("./Queries.txt"));
                while ((query = r.readLine()) != null) {
                    System.out.println(query);
                    if (query.startsWith("//")) {
                        continue;
                    }
                    if (query.split(".")[0].equals(this.getRootNode())) {
                        output = "Query already exists for the data you are trying to query";
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                
            }
            if (output.isEmpty()) {
                QueryTemplateFactory f = new QueryTemplateFactory(this.data, this.getRootNode());
                FileWriter w = new FileWriter(this.queryfile, true);
                w.write("\n" + f.getQueryName());
                w.close();
                output = "Query geneartion successful";
            }

        } catch (IOException e) {
            output = "Query generation failed";
        }

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();

        out.println(output);
        if (body != null) {
            out.println(body);
        }

        baseRequest.setHandled(true);
    }// handle

    // Helper function determining if a line is an output line
    public boolean isOutputLine(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '<' || line.charAt(i) == '>') {
                count++;
            }
        }
        if (count == 4) {
            return true;
        }
        return false;
    }

}// relatedHandlerForAPI
