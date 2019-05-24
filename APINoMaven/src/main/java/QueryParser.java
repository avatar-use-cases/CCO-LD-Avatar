
// java.util
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class QueryParser {
    private ArrayList<String> query = new ArrayList<String>();
    private ArrayList<String> variables = new ArrayList<String>();
    private long limit = 5;
    private String[][] constants = null;
    private String orderBy = "";
    private String[][] integratedVars = null;

    // Getters and setters
    public void setQuery(String q) {
        this.query = new ArrayList<String>(Arrays.asList(q.split("\n")));
    }

    public ArrayList<String> getQuery() {
        return this.query;
    }

    public void setOrderBy(String o) {
        this.orderBy = o;
    }

    public void setVariables(ArrayList variables) {
        this.variables = variables;
    }

    public ArrayList<String> getVariables() {
        return this.variables;
    }

    public void setLimit(long lim) {
        this.limit = lim;
    }

    public void setIntegratedVars(String[][] i) {
        this.integratedVars = i;
    }

    public void setConstants(String[][] c) {
        this.constants = c;
    }

    // End of getters and setters

    // Constructor
    public QueryParser(String query, ArrayList<String> vars, long limit, String[][] constants,
            String[][] integratedVars, String orderBy) {
        setQuery(query);
        System.out.println("vars is " + vars);
        setVariables(vars);
        setLimit(limit);
        setConstants(constants);
        setIntegratedVars(integratedVars);
        setOrderBy(orderBy);
    }

    public String parse() {
        String header = parseHeader();
        String body = parseBody();
        String q = header + "\n" + body;
        if (this.integratedVars != null) {
            q = integrate(q, integratedVars);
        }
        return q;
    }

    // rewrites the headers of the query with
    // only the selected variables the user wants to return
    private String parseHeader() {
        String header = "";
        boolean trigger = false;
        for (String line : this.query) {
            if (line.contains("where")) {
                break;
            } else if (line.contains("select ") || trigger == true) {
                header += line;
                trigger = true;
            }
        }
        HashSet<String> newHeader = new HashSet<String>();
        for (String i : header.split(" ")) {
            if (variables.contains(i.trim())) {
                newHeader.add(i);
            }
        }

        String h = "select distinct";
        for (String x : newHeader) {
            h += " " + x;
        }
        h += "\n";
        return h;
    }

    public String setBindings(String[][] constants) {
        String bind = "";
        for (int i = 0; i < constants[0].length; i++) {
            if (constants[1][i] != "") {
                bind += "Bind( '" + constants[1][i] + "' as ?" + constants[0][i] + ")\n";
            }
        }
        return bind;
    }

    private ArrayList<String> breakQueryIntoBlocks() {
        ArrayList<String> query = new ArrayList<String>();
        query = queryBody();
        ArrayList<String> blocks = new ArrayList<String>();
        HashMap<String, String> roots = new HashMap<String,String>();
        roots = rootNodeLines();
        // create a block for each variable
        for (String var : variables) {
            int index = 0;
            for (String x : query) {
                try {
                    String object = x.split(" ")[2];
                    if (object.equals(var + ".")) {
                        // get the index of the output line
                        index = query.indexOf(x);
                        break;
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
            // itterate back up through the query
            String block = query.get(index);
            index--;

            outer: while (index > 0) {
                // get line from query
                String line = query.get(index);
                // if object is var
                try {
                    // check to see if the line is an output line
                    if (!(line.split(" ")[2].contains("var")
                            || (line.split(" ")[2].contains("<") && line.split(" ")[2].contains(">")))) {
                        // if it is the value of the root node it adds the object decloration for the
                        // root node
                        for (String x : roots.keySet()) {
                            if (line.split(" ")[0].equals(roots.get(x.split(" ")[0]))) {
                                block = line + "\n" + block;
                                break outer;
                            }
                        }
                        block = pathToRootNode(query, index + 1, roots) + block;
                        // otherwise link up to root node
                        break;
                    } else {
                        block = line + "\n" + block;
                        // check if line is root node
                        for (String node : roots.keySet()) {
                            if (line.split(" ")[2].contains(node) && line.split(" ")[1].equals("a")) {
                                break outer;
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                index--;
            }
            blocks.add(block);
        }
        return blocks;
    }

    private String parseBody() {
        ArrayList<String> blocks =  new ArrayList<String>();
        blocks = breakQueryIntoBlocks();
        System.out.println("///////////////////////////////////////////////////");
        for (String x : blocks) {
            System.out.println(x);
            System.out.println("///////////////////////////////////////////////////");
        }
        HashMap<String, String> mappings = this.generateMap(blocks);
        String body = "where { \n";
        if (this.constants != null) {
            body += setBindings(this.constants);
        }
        for (String var : this.variables) {
            body += mappings.get(var) + "\n";
        }
        body += "}";
        try{
            if(this.orderBy != null || !this.orderBy.isEmpty()){
                body += "order by ?" + this.orderBy + "\n";
            }
        }catch (NullPointerException e){

        }
        body += "limit " + Long.toString(this.limit) + "\n";
        return body;
    }

    // parses the where{} part of the query into an array
    // list with each iteration being a ?s ?p ?o statement
    private ArrayList<String> queryBody() {
        ArrayList<String> out = new ArrayList<String>();
        boolean trigger = false;
        for (String x : this.query) {
            if (x.contains("}")) {
                break;
            } else if (trigger == true) {
                out.add(x);
            } else if (x.contains("{")) {
                trigger = true;
            }
        }
        return out;
    }

    // creates a hash set of all object declorations
    // ex ?var1 a cco:hotel
    private HashSet<String> objectTyping() {
        HashSet<String> output = new HashSet<String>();
        for (String line : this.query) {
            if (line.contains(" a ")) {
                output.add(line);
            }
        }
        return output;
    }

    // used to check if its a pair of values such as lat and long
    private boolean nextLineValue(int index, ArrayList<String> queryBody) {
        try {
            String object = queryBody.get(index + 1).split(" ")[2];
            if (!object.contains("var") && !(object.contains("<") || object.contains(">"))) {
                return true;
            }
            // if reached the end return false
        } catch (NullPointerException e) {
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }

    // generates a hash map from the blocks array list with the variable name as the
    // key
    private HashMap<String, String> generateMap(ArrayList<String> arr) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String block : arr) {
            String line = block.split("\n")[block.split("\n").length - 1];
            String obj = line.split(" ")[2];
            // takes off the preiod after the variable name
            obj = obj.substring(0, obj.length() - 1);
            map.put(obj, block);
        }
        return map;
    }

    // adds all the extra code after the where {} clause in the query
    // private String addExtraJargin() {
    //     int index = 0;
    //     boolean end = false;
    //     String out = "";
    //     for (String line : this.query) {
    //         if (line.startsWith("}")) {
    //             index = this.query.indexOf(line);
    //             end = true;
    //         }
    //         if (end) {
    //             out += line + "\n";
    //         }
    //     }
    //     return out;
    // }

    // gets the object decloration line of the query
    private HashMap<String, String> rootNodeLines() {
        String rootNode = "";
        HashMap<String, String> rootNodes = new HashMap<String, String>();
        for (String line : query) {
            try {
                if (line.contains("var0")) {
                    rootNode = line.split(" ")[0].replace("var0", "").replace("?", "");
                }
                if (!rootNode.isEmpty()) {
                    if (line.split(" ")[1].equals("a")) {
                        if (line.split(" ")[2].contains(rootNode)) {
                            rootNodes.put(rootNode, line);
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }
        }
        return rootNodes;
    }

    private String pathToRootNode(ArrayList<String> query, int index, HashMap<String, String> subs) {
        // determine the subject the query is from
        String body = "";
        String subject = "";
        String line = query.get(index);
        int i = 0;
        String s = query.get(index).split(" ")[0];
        for (String x : subs.keySet()) {
            if (line.split(" ")[0].contains(x)) {
                subject = x;
                break;
            }
        }
        // gets the approrprriate object decloration
        for (String x : query) {
            if (x.split(" ")[0].equals(s)) {
                i = query.indexOf(x);
                break;
            }
        }
        while (!line.contains(subs.get(subject).trim())) {
            // check to see if line is another output variable
            if (!line.split(" ")[2].startsWith("?" + subject + "Var") && !line.split(" ")[2].startsWith("<")
                    && !line.split(" ")[2].endsWith(">.")) {
                for (String x : query) {
                    if (x.split(" ")[0].equals(line.split(" ")[0]) && x.split(" ")[1].equals("a")) {
                        i = query.indexOf(x);
                        line = query.get(i);
                        if (!body.contains(line)) {
                            body = line + "\n" + body;
                        }
                        break;
                    }
                }
                continue;
            }
            line = query.get(i);
            body = line + "\n" + body;
            i--;
        }
        return body;
    }

    // not ideal but works
    public String integrate(String query, String[][] integratableVars) {
        String integratedQuery = query.replace("select distinct", "");
        int count = 0;
        for (String[] i : integratableVars) {
            for (String x : i) {
                integratedQuery = integratedQuery.replace("?" + x, "?IntegratedVar" + count);
            }
            String[] tmp = integratedQuery.split("\n");
            ArrayList<String> intArr = new ArrayList(Arrays.asList(tmp));
            String header = intArr.get(0);
            HashSet<String> set = new HashSet<String>();
            for (String x : header.split(" ")) {
                set.add(x);
            }
            String newline = "";
            for (String x : set) {
                newline += x + " ";
            }
            intArr.remove(0);
            intArr.add(0, newline);
            integratedQuery = "";
            for (String x : intArr) {
                integratedQuery += x + "\n";
            }
            count++;
        }
        return query = "select distinct " + integratedQuery;
    }
}