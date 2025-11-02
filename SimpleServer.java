import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SimpleServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("🚀 Server running on http://localhost:8080");
        
        while (true) {
            Socket client = server.accept();
            new Thread(() -> handleRequest(client)).start();
        }
    }
    
    static void handleRequest(Socket client) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            
            String line = in.readLine();
            if (line != null && line.contains("OPTIONS")) {
                out.println("HTTP/1.1 200 OK");
                out.println("Access-Control-Allow-Origin: *");
                out.println("Access-Control-Allow-Methods: POST, OPTIONS");
                out.println("Access-Control-Allow-Headers: Content-Type");
                out.println();
                out.flush();
            } else if (line != null && line.contains("POST")) {
                // Skip headers
                while ((line = in.readLine()) != null && !line.isEmpty()) {}
                
                // Create mock Excel response
                out.println("HTTP/1.1 200 OK");
                out.println("Access-Control-Allow-Origin: *");
                out.println("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                out.println("Content-Disposition: attachment; filename=inventory-report.xlsx");
                out.println("Content-Length: 100");
                out.println();
                out.println("Mock Excel Data - Server Working!");
                out.flush();
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}