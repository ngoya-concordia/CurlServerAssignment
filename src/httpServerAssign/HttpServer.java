package httpServerAssign;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

	public static void main(String[] args) throws IOException{
		ServerSocket socketServer = new ServerSocket(8080);
		System.out.println("Listening for connection on port 8080 ...."); 
		while (true) {
			BufferedOutputStream dataOut = null;
			PrintWriter out = null;
			Socket clientSocket =null;
			try {
		    clientSocket = socketServer.accept(); 
			InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream()); 
			BufferedReader reader = new BufferedReader(isr); 
			out = new PrintWriter(clientSocket.getOutputStream());
			dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
			String line = reader.readLine(); 
			while (!line.isEmpty()) 
			{ 
			System.out.println(line); 
			line = reader.readLine(); 
			} 
            String httpResponse = "hello" ;
			out.println("HTTP/1.1 501 Not Implemented");
			out.println("Content-length: " + httpResponse.length());
			out.println(); // blank line
			out.flush();
           
            dataOut.write(httpResponse.getBytes("UTF-8"));
            dataOut.flush();
            clientSocket.close();
			}
			catch (IOException ioe) {
				System.err.println("Server error : " + ioe);
			} finally {
				try {
					dataOut.close();
					clientSocket.close(); 
				} catch (Exception e) {
					System.err.println("Error closing stream : " + e.getMessage());
				} 
			}
		}
		
		}

}

