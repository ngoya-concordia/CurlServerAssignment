package httpServerAssign;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
//import org.json.*;
//import org.json.simple.JSONObject;

public class HttpServer {

	public static void main(String[] args) throws IOException {
		ServerSocket socketServer = new ServerSocket(8080);
		System.out.println("Listening for connection on port 8080 ....");
		while (true) {
			BufferedOutputStream dataOut = null;
			PrintWriter out = null;
			Socket clientSocket = null;
			try {
				clientSocket = socketServer.accept();
				List<String> res =null;
				InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
				BufferedReader reader = new BufferedReader(isr);
				out = new PrintWriter(clientSocket.getOutputStream());
				dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
				String line = reader.readLine();
				StringTokenizer parse = new StringTokenizer(line);
				String method = parse.nextToken();
				if (method.equalsIgnoreCase("GET")) {
					 res=parseGetRequest(parse);
				}
				while (!line.isEmpty()) {
					System.out.println(line);
					line = reader.readLine();
					
				}

				sendResponseToClient(out, dataOut,res);

			} catch (IOException ioe) {
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

	public static List<String> parseGetRequest(StringTokenizer parse)
	{
		String fileRequested = parse.nextToken();
		List<String> result= null;
		if(!fileRequested.trim().equalsIgnoreCase("/"))
		{
			System.out.println("\n\n fileRequested "+fileRequested);
		}
		else
		{
			try (Stream<Path> walk = Files.walk(Paths.get("./"))) {
				
				result = walk.filter(Files::isRegularFile)
						.map(x -> x.getFileName().toString()).collect(Collectors.toList());

				result.forEach(System.out::println);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	public static void sendResponseToClient(PrintWriter out, BufferedOutputStream dataOut,List<String> httpResponse) throws IOException {
		//JSONObject obj = new JSONObject();
		out.println("HTTP/1.0 200 OK");
		out.println("Content-length: " + httpResponse.toString().length());
		out.println(); // blank line
		out.flush();

		dataOut.write(httpResponse.toString().getBytes("UTF-8"));
		dataOut.flush();
	}

}
