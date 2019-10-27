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

public class HttpServer {
	private ServerSocket socketServer;
	private BufferedOutputStream dataOut = null;
	private PrintWriter out = null;
	private Socket clientSocket = null;
	private static int portNo = 8080;
	private static String serverFolder = "E://Concordia/Computer Networks/ServerData";

	public HttpServer() throws IOException {
		this(portNo, serverFolder);
	}

	public HttpServer(int portNo, String serverFolder) throws IOException {
		HttpServer.portNo = portNo;
		if (serverFolder.trim().length() > 0)
			HttpServer.serverFolder = serverFolder;
		socketServer = new ServerSocket(HttpServer.portNo);
		System.out.println("Listening for connection on port : " + HttpServer.portNo);
		while (true) {
			try {
				clientSocket = socketServer.accept();
				String res = "";
				InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
				BufferedReader reader = new BufferedReader(isr);
				out = new PrintWriter(clientSocket.getOutputStream());
				dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
				String line = reader.readLine();
				StringTokenizer parse = new StringTokenizer(line);
				String method = parse.nextToken();
				System.out.println("Method Type  " + method);
				if (method.equalsIgnoreCase("GET")) {
					res = parseGetRequest(parse);
				} else {
					res = parsePostRequest(parse);
				}
				while (!line.isEmpty()) {
					System.out.println(line);
					line = reader.readLine();

				}

				sendResponseToClient(out, dataOut, res);

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

	private String parsePostRequest(StringTokenizer parse) {
		// TODO Auto-generated method stub
		String fileRequested = parse.nextToken();

		return null;
	}

	public String parseGetRequest(StringTokenizer parse) {
		String fileRequested = parse.nextToken();
		String result = "";
//		try (Stream<Path> walk = Files.walk(Paths.get("./src/"))) {
		System.out.println("Server Folder " + serverFolder);
		try (Stream<Path> walk = Files.walk(Paths.get(serverFolder + "/"))) {

			List<String> fileList = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
					.collect(Collectors.toList());
			if (!fileRequested.trim().equalsIgnoreCase("/")) {
				fileRequested = fileRequested.trim().substring(1);
				if (fileList.contains(fileRequested)) {
//					Stream<String> stream = Files.lines(Paths.get("./src/httpServerAssign/" + fileRequested));
					Stream<String> stream = Files.lines(Paths.get(serverFolder + "/" + fileRequested));

					fileList = stream.collect(Collectors.toList());
					result = fileList.toString();
				} else {
					System.out.println("Send Error Response 404");
				}
				System.out.println("\n\n fileRequested " + fileRequested);
			} else {

				result = fileList.toString();
//				result.forEach(System.out::println);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void sendResponseToClient(PrintWriter out, BufferedOutputStream dataOut, String httpResponse)
			throws IOException {
		// JSONObject obj = new JSONObject();
		out.println("HTTP/1.0 200 OK");
		out.println("Content-length: " + httpResponse.toString().length());
		out.println(); // blank line
		out.flush();

		dataOut.write(httpResponse.toString().getBytes("UTF-8"));
		dataOut.flush();
	}

}
