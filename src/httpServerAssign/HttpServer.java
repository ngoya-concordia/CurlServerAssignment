package httpServerAssign;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

	private BufferedOutputStream dataOut = null;
	private PrintWriter out = null;
	private Socket connect;
	private static int portNo = 8080;
	private Socket clientSocket = null;
	private static ServerSocket socketServer;
	private static String serverFolder = "E://Concordia/Computer Networks/Lab Assignment 1/CurlServerAssignment/ServerFolder";

	public HttpServer() throws IOException {
		this(portNo, serverFolder);
	}

	public HttpServer(int portNo, String serverFolder) throws IOException {
		HttpServer.portNo = portNo;
		if (serverFolder.trim().length() > 0)
			HttpServer.serverFolder = serverFolder;

		System.out.println("Listening for connection on port : " + HttpServer.portNo);
		socketServer = new ServerSocket(HttpServer.portNo);
		while (true) {
			try {

				clientSocket = socketServer.accept();
//				HttpServer myServer = new HttpServer(clientSocket);
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							String res = "";
							InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
							BufferedReader reader = new BufferedReader(isr);
							out = new PrintWriter(clientSocket.getOutputStream());
							dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
							String line = reader.readLine();
							StringTokenizer parse = new StringTokenizer(line);
							String method = parse.nextToken();
							System.out.println("Method Type  " + method);
							while (!line.isEmpty()) {
								System.out.println(line);
								line = reader.readLine();

							}
							StringBuilder payload = new StringBuilder();
							while (reader.ready()) {
								payload.append((char) reader.read());
							}
							System.out.println("Pay LOad " + payload);

							if (method.equalsIgnoreCase("GET")) {
								res = parseGetRequest(parse);
							} else {
								res = parsePostRequest(parse, payload.toString());
							}

							

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
				});
				thread.start();

			} catch (IOException ioe) {
				System.err.println("Server error : " + ioe.getMessage());
			}
		}

	}

	private String parsePostRequest(StringTokenizer parse, String data) {
		// TODO Auto-generated method stub
		String status_code ="";
		String fileRequested = parse.nextToken();
		System.out.println("File Requested " + fileRequested);
		System.out.println("Data " + data);
		try (Stream<Path> walk = Files.walk(Paths.get(serverFolder + "/"))) {

			List<String> fileList = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
					.collect(Collectors.toList());
			if (!fileRequested.trim().equalsIgnoreCase("/")) {
				fileRequested = fileRequested.trim().substring(1);
				if (fileList.contains(fileRequested)) {
					System.out.println("overwriting the file ");
					writeToFile(fileRequested, data);
				} else {
					System.out.println("creating the new file ");
					writeToFile(fileRequested, data);
				}
			} else {
				status_code ="400";
				System.out.println("Invalid request");

			}
			sendResponseToClient(out, dataOut, data,status_code);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	private synchronized static void writeToFile(String fileName, String data) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(serverFolder + "/" + fileName)));
			writer.write(data);
		} catch (IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		} finally {
			writer.close();
		}
	}

	// httpc post -v -h Content-Type:application/json -h Connection:Keep-Alive -d
	// '{"name":"game_name", "publisher":"PUBLISHER","pubished_in":"YEAR"}'
	// "http://192.168.2.28/test.txt"
	public String parseGetRequest(StringTokenizer parse) {
		String fileRequested = parse.nextToken();
		String result = "";
		String status_code ="";
		System.out.println("Server Folder " + serverFolder);
		try (Stream<Path> walk = Files.walk(Paths.get(serverFolder + "/"))) {

			List<String> fileList = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
					.collect(Collectors.toList());
			if (!fileRequested.trim().equalsIgnoreCase("/")) {
				fileRequested = fileRequested.trim().substring(1);
				if (fileList.contains(fileRequested)) {
					Stream<String> stream = Files.lines(Paths.get(serverFolder + "/" + fileRequested));

					fileList = stream.collect(Collectors.toList());
					result = fileList.toString();
					status_code ="200 OK";
				} else {
					System.out.println("Send Error Response 404");
					status_code ="404 Not Found";
				}
				System.out.println("\n\n fileRequested " + fileRequested);
			} else {

				result = fileList.toString();
				status_code ="200 OK";
			}
			sendResponseToClient(out, dataOut, result,status_code);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void sendResponseToClient(PrintWriter out, BufferedOutputStream dataOut, String httpResponse, String status_code)
			throws IOException {
		// JSONObject obj = new JSONObject();
		out.println("HTTP/1.0"+status_code);
		out.println("Content-length: " + httpResponse.toString().length());
		out.println(); // blank line
		out.flush();

		dataOut.write(httpResponse.toString().getBytes("UTF-8"));
		dataOut.flush();
	}

}
