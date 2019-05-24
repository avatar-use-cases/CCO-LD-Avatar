import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser {

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
 
   
            List<Employee> employees = null;
            Employee empl = null;
            String text = null;
   
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(
                                                    new File("./tests.rdf")));
   
            while (reader.hasNext()) {
                System.out.println(reader.next());
                //  int Event = reader.next();
   
                //  switch (Event) {
                //       case XMLStreamConstants.START_ELEMENT: {
                //            if ("Employee".equals(reader.getLocalName())) {
                //                 empl = new Employee();
                //                 empl.setID(reader.getAttributeValue(0));
                //            }
                //            if ("Employees".equals(reader.getLocalName()))
                //                 employees = new ArrayList<>();
   
                //            break;
                //       }
                //       case XMLStreamConstants.CHARACTERS: {
                //            text = reader.getText().trim();
                //            break;
                //       }
                //       case XMLStreamConstants.END_ELEMENT: {
                //            switch (reader.getLocalName()) {
                //                 case "Employee": {
                //                      employees.add(empl);
                //                      break;
                //                 }
                //                 case "Firstname": {
                //                      empl.setFirstname(text);
                //                      break;
                //                 }
                //                 case "Lastname": {
                //                      empl.setLastname(text);
                //                      break;
                //                 }
                //                 case "Age": {
                //                      empl.setAge(Integer.parseInt(text));
                //                      break;
                //                 }
                //                 case "Salary": {
                //                      empl.setSalary(Double.parseDouble(text));
                //                      break;
                //                 }
                //            }
                //            break;
                //       }
                //  }
            }
   
            // Print all employees.
            for (Employee employee : employees)
                 System.out.println(employee.toString());
       }


}