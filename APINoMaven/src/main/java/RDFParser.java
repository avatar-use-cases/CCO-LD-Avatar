import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.IndexOutOfBoundsException;

public class RDFParser {
    // variable declorations
    private String data;
    private String rootNode;
    private ArrayList<String> rootNodes = new ArrayList<String>();
    // getters and setters
    public String getRootNode() {
        return this.rootNode;
    }
    // end of getters and setters

    // public static void main(String[] args){
    //     try
    //     {
    //         BufferedReader r = new BufferedReader(new FileReader("./Airport.rdf"));
    //         StringBuilder str = new StringBuilder();
    //         String line;
    //         while((line = r.readLine()) != null) {
    //             str.append(line + "\n");
    //         }
    //         RDFParser parser = new RDFParser(str.toString());
    //         System.out.println(parser.findRootNode());
    //         System.out.println(parser.rootNodes);
    //     } catch(IOException e){
    //         System.out.print("Could not find or read file");
    //     }
    // }

    // Constructor
    RDFParser(String data) {
        this.data = data;
    }

    // Methods

    public String findRootNode() {
        String[] arr = this.data.split("\n");
        int index = 0;
        int finalIndex = 0;
        for (String line: arr) {
            if(isOutputLine(line) == true){
                String pointer = "";
                boolean trigger = false;
                for(int i = index; i > 0; i--) {
                    if(isPointer(arr[i]) && trigger == false) {
                        pointer = getURI(arr[i]);
                        trigger = true;
                    }
                    else if(arr[i].contains("rdf:about=\""+pointer+"\"") && trigger == true) {
                        try {
                            finalIndex = i;
                            pointer = getURI(arr[i+1]);
                        }
                        catch(IndexOutOfBoundsException e) {
                            this.rootNode = getTerm(arr[finalIndex]);
                            this.rootNodes.add(this.rootNode);
                            break;
                        }
                        trigger = false;
                    }
                }
            }
            index++;
        }
        return this.rootNode;
    }

    // Determines if a line contains a data value
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
    
    private boolean isPointer(String line) {
        if(line.contains("rdf:resource=")){
            return true;
        }
        return false;
    }
    private String getURI(String line) throws IndexOutOfBoundsException{
        String substr = line.split("=\"")[1];
        String URI = substr.split("\"/>")[0];
        return URI;

    }

    private String getTerm(String line) {
        String substr = line.split(":")[1];
        String term = substr.split(" ")[0];
        return term;
    }


}