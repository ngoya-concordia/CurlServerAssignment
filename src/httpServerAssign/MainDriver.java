package httpServerAssign;

import java.util.Scanner;

public class MainDriver {

	private static boolean isDebug = false;
	private static int portNo;
	private static String serverFolder = "E://Concordia/Computer Networks/Lab Assignment 1/CurlServerAssignment/ServerFolder";
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		MainDriver mainDriver = new MainDriver();
		int sel = mainDriver.displayFrontScreen();

		while (true) {

			if (sel == 1) {
				scanner = new Scanner(System.in);
				String input = scanner.nextLine();
				String[] str = input.split(" -");

				for (String s : str) {
					String[] test = s.split(" ");
					String key = test[0].trim();
					String value = "";
					if (test.length > 1) {
						for (int i = 1; test.length > i; i++)
							value = value.concat(test[i].trim() + " ");
					}

					if (key.trim().equalsIgnoreCase("v"))
						isDebug = true;
					else if (key.trim().equalsIgnoreCase("p")) {
						portNo = Integer.parseInt(value.trim());
					} else if (key.trim().equalsIgnoreCase("d")) {
						serverFolder = value.trim();
					}
				}

				HttpServer httpServer = null;
				try {
					if (portNo > 0 || serverFolder.trim().length() > 0 || isDebug)
						httpServer = new HttpServer(portNo, serverFolder, isDebug);
					else
						httpServer = new HttpServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (sel == 2) {
				System.out.print("===========EXIT the process================ ");
				sel = mainDriver.displayFrontScreen();

				break;
			}

		}

	}

	private int displayFrontScreen() {
		System.out.println("*************************************************");
		System.out.println("Welcome to the File Server Terminal");
		System.out.println("*************************************************");
		System.out.println("Press 1  to enter httpfs command");
		System.out.println("Press 2 to Exit the process.");
		return scanner.nextInt();
	}
}
