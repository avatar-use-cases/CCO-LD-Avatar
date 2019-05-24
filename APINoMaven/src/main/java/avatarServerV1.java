//package def

//import statements
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

//request log stuff
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

// //import shiro dependencies
// import org.apache.shiro.mgt.DefaultSecurityManager;
// import org.apache.shiro.SecurityUtils;
// import org.apache.shiro.util.Factory;
// import org.apache.shiro.mgt.SecurityManager;
// import org.apache.shiro.config.IniSecurityManagerFactory;
// import org.apache.shiro.realm.text.IniRealm;
// import org.apache.shiro.authc.UsernamePasswordToken;
// import org.apache.shiro.subject.*;

public class avatarServerV1{

    public static void main(String[] args) throws Exception{
        // Create Server
        Server server = new Server(8180);
    
        // Setting up SecurityManager
        // Factory<SecurityManager> factory = new IniSecurityManagerFactory("config/shiro.ini");
        // SecurityManager securityManager = factory.getInstance();
        // SecurityUtils.setSecurityManager(securityManager);    

        // add a context 
        ContextHandler queryContext = new ContextHandler();
        queryContext.setContextPath("/query");
        queryContext.setHandler(new queryHandlerForAPI());
        
        ContextHandler parse = new ContextHandler();
        parse.setContextPath("/parse");
        parse.setHandler(new QueryParseHandler());

        // ContextHandler addContext = new ContextHandler();
        // queryContext.setContextPath("/add");
        // queryContext.setHandler(new AddQueryHandlerForAPI());

        ContextHandlerCollection cHandlers = new ContextHandlerCollection();
        cHandlers.setHandlers(new ContextHandler[] {queryContext, parse});
        
        //Setup request logging
        NCSARequestLog requestLog = new NCSARequestLog(){
            @Override
            public void log(Request request, Response response) {
                LocalDateTime now = LocalDateTime.now();
                String year = Integer.toString(now.getYear());
                String month = Integer.toString(now.getMonthValue());
                String day = Integer.toString(now.getDayOfMonth());
                BufferedWriter bw = null;
                FileWriter fw = null;
                try{
                    String filename = "logs/req/jetty-"+year+"_"+month+"_"+day+".request.log";
                    fw = new FileWriter(filename,true);
                    bw = new BufferedWriter(fw);

                    bw.write("*************************************\n");
                    bw.write("Datetime: " + now.toString() + "\n");
                    Enumeration<String> headerNames = request.getHeaderNames();
                    while(headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        String headerValue = request.getHeader(headerName);
                        String headerNameAndValue = headerName + ": " +headerValue+"\n";
                        bw.write(headerNameAndValue);
                    }
                    bw.write("*************************************\n");
                } catch(IOException e){
                    e.printStackTrace();
                } finally {
                    try{
                        bw.close();
                        fw.close();
                    } catch(IOException ex){
                        ex.printStackTrace();
                    }//catch
                }//finally
            }//log        
        };

        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogLatency(true);
        requestLog.setRetainDays(90);
        
        server.setRequestLog(requestLog);
        
        server.setHandler(cHandlers);
        server.start();
        server.join();

    }//main

    public static String readFileAsString(String filename) throws Exception
    {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(filename)));
        return data;
    }//readFileAsString

}//smallTestServer