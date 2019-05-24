
// imports

import java.util.*;
import java.io.*;

/////////////////////////////////////////////////////////////////////////////////////////
////// This class generates the query templates that we use for querying back data //////
////// It is called by the addDataHanler when data is added that a template        //////
////// currently does not exist for                                                //////
/////////////////////////////////////////////////////////////////////////////////////////

public class QueryTemplateFactory {
    private final String queryTemplatePath = "../queryTemplates/";
    private String subject;
    private Integer varcount = 0;
    private Integer objectCount = 0;
    private String templateHeader = "";
    private String templateBody = "";
    private HashMap<String, String> abreviations = new HashMap<String, String>();
    private ArrayList<String> variables = new ArrayList<String>();

    public String getQuery() {
        return this.templateHeader + "\n" + this.templateBody;
    }

    // main for testing purposes
    // public static void main(String[] args) {
    // System.out.println("starting");

    // // QueryTemplateFactory q = new
    // QueryTemplateFactory("analyst.rdf","Analyst.rq",
    // // "Person","avatar-demo-1.cubrc.org/fuseki/CaceTripleStore/sparql");
    // QueryTemplateFactory a = new QueryTemplateFactory("test.rdf", "person.rq",
    // "Person");
    // // QueryTemplateFactory jd = new QueryTemplateFactory("JohnDoe.rdf",
    // "jdoe.rq",
    // // "Person","avatar-demo-1.cubrc.org/fuseki/Test_User_Data_John_Doe/sparql");
    // // QueryTemplateFactory p = new QueryTemplateFactory("Park.rdf", "park.rq",
    // // "Park","avatar-demo-1.cubrc.org/fuseki/AVATAR_2018_07_Standard/sparql");
    // // QueryTemplateFactory r = new
    // QueryTemplateFactory("cleanSpringfieldRestaurants33.rdf", "restaurant.rq",
    // //
    // "Restaurant","avatar-demo-1.cubrc.org/fuseki/AVATAR_2018_07_Standard/sparql");
    // // System.out.println(q.templateBody);
    // }

    // constructor
    public QueryTemplateFactory(String data, String subject) {
        this.subject = subject;
        try {
            InputStream in = new ByteArrayInputStream(data.getBytes());
            BufferedReader f = new BufferedReader(new InputStreamReader(in));
            // Skips the first 2 lines of the rdf file
            f.readLine();
            makeAbbreviations(f);
            // System.out.println(this.abreviations);
            ArrayList<String> arr = createRDFArray(f);
            writeQueryBody(arr);
            // writeQueryBody(switchResources(arr));
            // System.out.println("Header is " + this.templateHeader);
            // System.out.println(arr);
            // BufferedWriter w = new BufferedWriter(new FileWriter(template));
            // w.write(this.templateHeader + "\n");
            // // service
            // w.write(this.templateBody);
            // w.close();
            // f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeAbbreviations(BufferedReader f) throws IOException {
        String line = f.readLine().trim();
        // System.out.println(line);
        while (line.startsWith("xmlns:")) {
            // gets rid of the tag indicating that its an abbreviation
            String sub1 = line.replace("xmlns:", "");
            String sub2 = sub1.split("=")[0];
            String sub3 = sub1.split("=")[1];
            // System.out.println(line);
            this.abreviations.put(sub2, sub3.replace(">", ""));
            f.mark(1000);
            line = f.readLine().trim();
        }
        f.reset();
    }

    public ArrayList<String> createRDFArray(BufferedReader f) throws IOException {
        boolean isDone = false;
        boolean first = true;
        int startPos = 0;
        ArrayList<String> arr = new ArrayList<String>();
        while (isDone == false) {
            String line = f.readLine();
            if (first == true) {
                startPos = getSpaceCount(line);

            }
            int currentPosition = getSpaceCount(line);
            arr.add(line);
            // checks to see if its done with the query
            if (currentPosition == startPos && first != true) {
                isDone = true;
                break;
            }
            first = false;
        }
        return arr;
    }

    // gets the number of left spaces
    public int getSpaceCount(String str) {
        int count = 0;
        while (str.charAt(count) == ' ') {
            count += 1;
        }
        return count;
    }

    public void writeQueryBody(ArrayList<String> arr) {
        boolean back = false;
        boolean hasSecondValue = false;
        Integer count = 0;
        String var;
        ArrayList<String> vars = new ArrayList<String>();
        for (int i = 0; i < arr.size(); i++) {
            String line = arr.get(i);
            // If its an object
            if (line.contains("rdf:about")) {
                String substring1 = line.split("<")[1];
                String value = substring1.split(">")[0];
                String abrev = value.split(":")[0];
                value = value.split(":")[1];
                value = value.split(" ")[0];
                value = this.abreviations.get(abrev).replace("\"", "") + value;
                var = "?" + this.subject + "var" + this.varcount.toString() + " a <" + value + ">.";
                this.templateBody += var + "\n";
                vars.add("?" + this.subject + "var" + this.varcount.toString());
                count++;
            }
            // skip the preicates that go back up in the ontology
            else if (line.contains("rdf:resource")) {
                continue;
            }
            // look into making this part recursive so it works for more than just 2 data
            // points
            // if its a data bearing line
            else if (isOutputLine(line)) {
                String var2 = "";
                String secondVar2 = "";
                String substring1 = line.split("<")[1];
                String value = substring1.split(">")[0];
                String abrev = value.split(":")[0];
                value = value.split(":")[1];
                value = this.abreviations.get(abrev).replace("\"", "") + value;
                String firstVar = "?" + this.subject + "var" + this.varcount.toString();
                this.varcount++;
                // checks to see if theres two data sets in a row. ex lat/long
                String newline = arr.get(i + 1);
                if (isOutputLine(newline)) {
                    hasSecondValue = true;
                    String substring2 = newline.split("<")[1];
                    String value2 = substring2.split(">")[0];
                    String abrev2 = value2.split(":")[0];
                    value2 = value2.split(":")[1];
                    value2 = this.abreviations.get(abrev2).replace("\"", "") + value2;
                    Integer prevVar = this.varcount - 1;
                    String firstVar2 = "?" + this.subject + "var" + prevVar.toString();
                    this.varcount++;
                    secondVar2 = rewriteVar(arr, i + 1, hasSecondValue);
                    var2 = firstVar2 + " <" + value2 + "> " + secondVar2 + ".";
                }
                String secondVar = rewriteVar(arr, i, hasSecondValue);
                var = firstVar + " <" + value + "> " + secondVar + ".";
                this.templateBody += var + "\n";
                this.templateHeader += secondVar + " ";
                if (!var2.isEmpty() && !secondVar2.isEmpty()) {
                    this.templateBody += var2 + "\n";
                    this.templateHeader += secondVar2 + " ";
                    i++;
                }
                hasSecondValue = false;
            }
            // if its a closing rdf tag
            else if (line.trim().startsWith("</") && back == false) {
                back = true;
                count--;
            } else if (line.trim().startsWith("</") && back == true) {
                count--;
            } else if (line.trim().startsWith("<") && back == true) {
                back = false;
                vars.subList(count / 2 + 1, vars.size()).clear();
                String substring1 = line.split("<")[1];
                String value = substring1.split(">")[0];
                String abrev = value.split(":")[0];
                value = value.split(":")[1];
                value = this.abreviations.get(abrev).replace("\"", "") + value;
                String firstVar = vars.get(vars.size() - 1);
                this.varcount++;
                String secondVar = "?" + this.subject + "var" + this.varcount.toString();
                var = firstVar + " <" + value + "> " + secondVar + ".";
                this.templateBody += var + "\n";
                count++;
            }
            // otherwise its a predicate
            else {
                String substring1 = line.split("<")[1];
                String value = substring1.split(">")[0];
                String abrev = value.split(":")[0];
                value = value.split(":")[1];
                value = this.abreviations.get(abrev).replace("\"", "") + value;
                String firstVar = "?" + this.subject + "var" + this.varcount.toString();
                this.varcount++;
                String secondVar = "?" + this.subject + "var" + this.varcount.toString();
                var = firstVar + " <" + value + "> " + secondVar + ".";
                this.templateBody += var + "\n";
                count++;
            }
        }

    }

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

    // New rewrite function that works with our new linked data
    public String rewriteVar(ArrayList<String> arr, int index, boolean hasMultValues) {
        String line = arr.get(index);
        String initialLine = line;
        String var = "";
        while (var.isEmpty()) {
            try {
                if (!line.trim().split(":")[1].split(" ")[0].contains("InformationBearingEntity")) {

                    String uri = line.split("rdf:about=\"")[1].split("\">")[0];
                    if (uri.startsWith("https://")){
                        uri = uri.substring(8,uri.length());
                    }
                    else {
                        uri = uri.substring(7,uri.length());
                    }
                    ArrayList<String> varArr = new ArrayList<String>(Arrays.asList(uri.split("/")));
                    var = "?" + this.subject;
                    for (int i = 3; i < varArr.size(); i++){
                        String tmp = varArr.get(i);
                        // filter out numbers with regx
                        tmp = tmp.replaceAll("[^A-Za-z]","");
                        var += tmp;
                    }
                    if(hasMultValues){
                        String tmp = initialLine.split(":")[1].split(">")[0];
                        tmp = tmp.split("_")[1];
                        tmp = tmp.substring(0,1).toUpperCase() + tmp.substring(1,tmp.length());
                        var += tmp;
                    }
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                
            }
            index--;
            line = arr.get(index);
        }
        return var;
    }

    // old rewrite runction

    // public String rewriteVar(ArrayList<String> arr, int index, boolean
    // hasMultValues) {
    // // System.out.println(index);
    // // System.out.println(arr.get(index).trim());
    // boolean trigger = true;
    // int lineIndex = index;
    // ArrayList<String> vars = new ArrayList<String>();
    // String newVarName = "";
    // String identifier = "";
    // String details = "";
    // int SubjectIndex = 0;
    // if (hasMultValues) {
    // String line = arr.get(index).trim();
    // String substring1 = line.split(":")[1];
    // details = substring1.split("_")[1];
    // }
    // for (int i = 0; i < arr.size(); i++) {
    // if (arr.get(i).contains(this.subject)) {
    // SubjectIndex = i;
    // break;
    // }
    // }
    // while (index >= 0) {
    // String line = arr.get(index).trim();
    // // System.out.println(line);
    // // System.out.println("-----------------------------");
    // if (line.contains("rdf:resource=") && trigger) {
    // trigger = false;
    // } else if (line.contains("rdf:about=")) {
    // if (line.contains(identifier)) {
    // // System.out.println("triggered");
    // String newline = arr.get(index + 1);
    // // System.out.println("newline = " + newline);
    // String substring = line.split(":")[1];
    // vars.add(substring.split(" ")[0]);
    // // System.out.println(substring.split(" ")[0]);
    // trigger = true;
    // lineIndex = index;
    // // in case you overshoot the subject
    // if (lineIndex < SubjectIndex) {
    // // System.out.println("Overshot this thing");
    // for (int i = lineIndex + 1; i < SubjectIndex; i++) {
    // String object = arr.get(i).trim();
    // if (object.contains("rdf:about=")) {
    // substring = object.split(":")[1];
    // object = substring.split(" ")[0];
    // vars.add(object);
    // }
    // if (object.startsWith("</")) {
    // substring = object.split(":")[1];
    // object = substring.split(" ")[0];
    // if (vars.contains(object)) {
    // vars.remove(object);
    // }
    // }
    // }
    // }
    // try {
    // String URI = newline.split("\"")[1];
    // // System.out.println("newline2 = " + newline.trim());
    // identifier = URI.split("_")[URI.split("_").length - 1];
    // // System.out.println(identifier);
    // } catch (IndexOutOfBoundsException e) {
    // // System.out.println("reached the end");
    // break;
    // }
    // // System.out.println("///////////////////////////////////////");
    // }
    // }
    // index--;
    // }
    // for (int i = 3; i > 0; i--) {
    // try {
    // newVarName += vars.get(i);
    // } catch (IndexOutOfBoundsException e) {
    // continue;
    // }
    // }
    // newVarName = newVarName.concat(details);
    // if (!newVarName.contains(this.subject)) {
    // newVarName = this.subject + newVarName;
    // }
    // if (this.variables.contains("?" + newVarName)) {
    // // System.out.println("inhere");
    // newVarName = newVarName + objectCount.toString();
    // objectCount++;
    // }
    // this.variables.add("?" + newVarName);
    // // System.out.println(newVarName);
    // // System.out.println("//////////////////////////////");
    // return "?" + newVarName;
    // }

    // public String rewriteVar(ArrayList<String> arr, int index, boolean
    // hasMultValues) {
    // String newVar = "";
    // String resource = null;
    // int spacing = 0;
    // int i = 1;
    // while(resource == null){
    // if(arr.get(index-i).contains("rdf:resource=")){
    // // split at the # to get the parent object name
    // resource = arr.get(index-i).split("#")[1].split("\"/>")[0];
    // }else if(arr.get(index+i).contains("rdf:resource=")){
    // resource = arr.get(index+i).split("#")[1].split("\"/>")[0];
    // }
    // }
    // System.out.println("resource is " + resource);

    // outerloop: while(!resource.contains(this.subject)){
    // for(String line : arr){
    // if(line.contains(resource)){
    // spacing
    // }
    // }
    // break outerloop;
    // }
    // return newVar;
    // }

    // private String rewriteVar(ArrayList<String> arr, int index, boolean
    // hasMultValues) {
    // HashSet<String> group = new HashSet<String>();
    // String uri = "";
    // HashSet<String> varNames = new HashSet<String>();
    // int indent = arr.get(index).indexOf(arr.get(index).trim());
    // System.out.println(indent);
    // int i = index;
    // String node = "";
    // System.out.println(arr.get(i).indexOf(arr.get(i).trim()));
    // while (arr.get(i).indexOf(arr.get(i).trim()) == indent) {
    // group.add(arr.get(i));
    // i--;
    // }
    // i = index;
    // while (arr.get(i).indexOf(arr.get(i).trim()) == indent) {
    // group.add(arr.get(i));
    // i++;
    // }
    // System.out.println(group);
    // for (String str : group) {
    // if (str.contains("rdf:resource=")) {
    // uri = str.split("rdf:resource=\"")[1].split("\"/>")[0];
    // System.out.println(uri);
    // }x
    // }
    // for (String x : arr) {
    // if (x.contains("rdf:about=\"" + uri)) {
    // node = x.split(":")[1].split(" ")[0];
    // varNames.add(node);
    // indent = x.indexOf(x.trim());
    // System.out.println(node);
    // break;
    // }
    // }
    // while(!node.toLowerCase().equals(this.subject.toLowerCase())){
    // group.clear();
    // for(String line: arr){
    // if(indent == line.indexOf(line.trim())){
    // group.add(line);
    // }
    // }
    // System.out.println(group);
    // }

    // String newVarName = "";
    // for (String x : varNames) {
    // newVarName += x;
    // }
    // return newVarName;
    // }

    public ArrayList<String> switchResources(ArrayList<String> arr) {
        HashMap<Integer, String> leadingLines = new HashMap<Integer, String>();
        int prevIndex = 0;
        int currIndex = 0;

        for (int x = 0; x <= arr.size() - 1; x++) {
            String i = arr.get(x);
            // System.out.println(i);
            int index = i.indexOf(i.trim());
            // if the line is not a closing tag
            if (!i.trim().startsWith("</")) {
                // if the line is an object
                if (i.contains("rdf:about")) {
                    leadingLines.put(index, i);
                }
                // if the line contains a resource
                else if (i.contains("rdf:resource")) {
                    // determine if the resource is already in the right position and if so do
                    // nothing
                    String prev = arr.get(arr.indexOf(i) - 1);
                    if (prev.indexOf(prev.trim()) < index) {
                        leadingLines.put(index, i);
                        continue;
                    }
                    Integer newIndex = arr.indexOf(leadingLines.get((Integer) index));
                    // check to make sure it goes to the begining of variables with multiple
                    // values like lat & long
                    String prev2 = arr.get(arr.indexOf(i) - 2);
                    if (prev.indexOf(prev.trim()) == index && prev2.indexOf(prev2.trim()) == index) {
                        arr.remove(i);
                        arr.add(arr.indexOf(leadingLines.get(index)) - 1, i);
                        continue;
                    }
                    Collections.swap(arr, arr.indexOf(leadingLines.get(index)), arr.indexOf(i));
                    continue;
                }
                // if the line contains a predicate
                else {
                    leadingLines.put(index, i);
                }
            }
        }
        // System.out.println(leadingLines);
        // Print out arr for debugging
        for (String s : arr) {
            System.out.println(s);
        }
        // // System.out.println("---------------------------------------");
        // for (Integer x : leadingLines.keySet()) {
        // System.out.println(leadingLines.get(x));
        // }
        return arr;
    }
}