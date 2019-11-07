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

	private static boolean isDebug = false;

	public HttpServer() throws IOException {
		this(portNo, serverFolder, isDebug);
	}

	public HttpServer(int portNo, String serverFolder, boolean isDebug) throws IOException {

		if (portNo > 0)
			HttpServer.portNo = portNo;
		if (serverFolder.trim().length() > 0)
			HttpServer.serverFolder = serverFolder;
		if (isDebug)
			HttpServer.isDebug = true;
		log("Enable Logs: " + HttpServer.isDebug);
		log("Port No: " + HttpServer.portNo);
		log("Server Folder: " + HttpServer.serverFolder);
		System.out.println("Listening for connection on port : " + HttpServer.portNo);
		socketServer = new ServerSocket(HttpServer.portNo);
		while (true) {
			try {
				clientSocket = socketServer.accept();
				log("Recieved Request !!");
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
							String contentType = "";
							String contentDisposition = "";
							log("Method Type  " + method);
							while (!line.isEmpty()) {
								log(line);
								line = reader.readLine();
								if (line.toLowerCase().contains("content-type")) {
									contentType = line.substring(line.indexOf(":") + 1, line.length());
								}
								if (line.toLowerCase().contains("content-disposition")) {
									contentDisposition = line.substring(line.indexOf(":") + 1, line.length());
								}

							}
							StringBuilder payload = new StringBuilder();
							while (reader.ready()) {
								payload.append((char) reader.read());
							}

							if (method.equalsIgnoreCase("GET")) {
								parseGetRequest(parse, contentType.trim(), contentDisposition);
							} else {
								res = parsePostRequest(parse, payload.toString(), contentType.trim(),
										contentDisposition);
							}

						} catch (IOException ioe) {
							log("Server error : " + ioe);
						} finally {
							try {
								dataOut.close();
								clientSocket.close();
							} catch (Exception e) {
								log("Error closing stream : " + e.getMessage());
							}
						}
					}
				});
				thread.start();

			} catch (IOException ioe) {
				log("Server error : " + ioe.getMessage());
			}
		}

	}

	private String parsePostRequest(StringTokenizer parse, String data, String contentType, String contentDisposition)
			throws IOException {
		// TODO Auto-generated method stub
		String path = parse.nextToken();
		log("Data " + data);
		String fileName = "";
		String[] arr = path.split("/");
		boolean isValid = false;
		if (arr.length > 0) {
			fileName = arr[arr.length - 1];
			path = path.replace(fileName, "");

			if (path.trim().length() > 0 && path.trim().charAt(path.trim().length() - 1) == '/') {
				path = path.substring(0, path.length() - 1);
			}
		}

		Path pathVal = null;
		Path originalPath = Paths.get(serverFolder);
		String newPath = "";
		if (!path.trim().isEmpty())
			newPath = serverFolder + "/" + path;
		else
			newPath = serverFolder;
		pathVal = Paths.get(newPath);
		if (pathVal.normalize().toString().contains(originalPath.normalize().toString())) {
			isValid = true;
		} else {
			isValid = false;
		}
		if (isValid) {
			if (!Files.exists(pathVal)) {

				Files.createDirectory(pathVal);
				log("Directory created");
			} else {
				log("Directory already exists");
			}

			try (Stream<Path> walk = Files.walk(Paths.get(pathVal.normalize().toString() + "/"))) {

				List<String> fileList = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
						.collect(Collectors.toList());
				if (!fileName.trim().isEmpty()) {
					if (fileList.contains(fileName)) {
						log("overwriting the file ");
						writeToFile(newPath, fileName, data);
					} else {
						log("creating the new file ");
						writeToFile(newPath, fileName, data);
					}
					log("Request Successful");
					sendResponseToClient(out, dataOut, data, "200 OK", "", "");
				} else {
					sendResponseToClient(out, dataOut, "", "400 Bad Request", "", "");
					log("Invalid request");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			sendResponseToClient(out, dataOut, "", "401 Access Denied", "", "");
			log("Access Denied");

		}

		return data;
	}

	private synchronized static void writeToFile(String path, String fileName, String data) throws IOException {

		BufferedWriter writer = null;
		try {

			writer = new BufferedWriter(new FileWriter(new File(path + "/" + fileName)));
			writer.write(data);
		} catch (IOException ex) {
			log("An IOException was caught!");
			ex.printStackTrace();
		} finally {
			writer.close();
		}
	}

	public void parseGetRequest(StringTokenizer parse, String contentType, String contentDisposition)
			throws IOException {
		boolean isValid = false;
		String path = parse.nextToken();
		log(path);
		String result = "";
		String status_code = "";
		String fileName = "";
		String[] arr = path.split("/");
		if (arr.length > 0) {
			fileName = arr[arr.length - 1];
			path = path.replace(fileName, "");

			if (path.trim().length() > 0 && path.trim().charAt(path.trim().length() - 1) == '/') {
				path = path.substring(0, path.length() - 1);
			}
		}

		Path pathVal = null;
		Path originalPath = Paths.get(serverFolder);
		String newPath = "";
		if (!path.trim().isEmpty())
			newPath = serverFolder + "/" + path;
		else
			newPath = serverFolder;
		pathVal = Paths.get(newPath);

		if (pathVal.normalize().toString().contains(originalPath.normalize().toString())) {
			isValid = true;
		} else {
			isValid = false;
		}
		if (isValid) {
			if (!Files.exists(pathVal)) {
				Files.createDirectory(pathVal);
				log("Directory created");
			} else {
				log("Directory already exists");
			}
			Stream<Path> walk = Files.walk(Paths.get(pathVal.normalize().toString() + "/"));
			log("Accessing Folder " + serverFolder);

			List<String> fileList = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
					.collect(Collectors.toList());
			if (!fileName.trim().isEmpty()) {
				String ext = "";
				if (fileName.indexOf(".") == -1) {

					if (contentType.equalsIgnoreCase("application/json"))
						ext = "json";
					if (contentType.equalsIgnoreCase("application/xml") || contentType.equalsIgnoreCase("text/xml"))
						ext = "xml";
					if (contentType.equalsIgnoreCase("application/pdf"))
						ext = "pdf";
					if (contentType.equalsIgnoreCase("text/html"))
						ext = "html";
					if (contentType.equalsIgnoreCase("text/plain"))
						ext = "txt";
					if (contentType.equalsIgnoreCase("text/css"))
						ext = "css";
					if (contentType.equalsIgnoreCase("text/csv"))
						ext = "csv";

					fileName = fileName + "." + ext;
				}

				if (fileList.contains(fileName)) {
					Stream<String> stream = Files.lines(Paths.get(serverFolder + "/" + fileName));
					fileList = stream.collect(Collectors.toList());
					result = fileList.toString();
					status_code = "200 OK";
					log("Request is successful.");
				} else {
					log("Send Error Response 404");
					status_code = "404 Not Found";
				}
			} else {

				result = fileList.toString();
				status_code = "200 OK";
			}
			sendResponseToClient(out, dataOut, result, status_code, contentType, contentDisposition);

		} else {

			log("Send Error Response 401");
			status_code = "401 Access Denied";
			sendResponseToClient(out, dataOut, result, status_code, contentType, contentDisposition);
			return;
		}

	}

	public void sendResponseToClient(PrintWriter out, BufferedOutputStream dataOut, String httpResponse,
			String status_code, String contentType, String contentDisposition) throws IOException {

		log("Sending response to client");
		out.println("HTTP/1.0 " + status_code);
		out.println("Content-length: " + httpResponse.toString().length());

		if (!contentType.isEmpty()) {
			out.println("Content-Type: " + contentType);
		}
		if (!contentDisposition.isEmpty()) {
			out.println("Content-Disposition: " + contentDisposition);
		}
		out.println();
		out.flush();

		dataOut.write(httpResponse.toString().getBytes("UTF-8"));
		dataOut.flush();
	}

	static void log(String logMessage) {
		if (HttpServer.isDebug)
			System.out.println(logMessage);
	}

}
