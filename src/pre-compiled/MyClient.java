import java.net.*;
import java.io.*;

public class MyClient {
	static BufferedReader in;
	static DataOutputStream out;
	static Socket s;
	static Object[] largestServerType = null;

	// returns an array of integers which represents data given to us by server
	public static int[] intParser(String str, int idx) {
		String[] splt = str.split(" ");
		int[] result = new int[splt.length - idx];
		for (int x = 0; x < result.length; x++) {
			result[x] = Integer.parseInt(splt[idx]);
			idx++;
		}

		return result;
	}

	// returns an array of objects representing server information based on strings
	// supplied by server.
	public static Object[] serverParser(String str) {

		String[] splt = str.split(" ");
		Object[] result = new Object[splt.length];
		Object temp;

		for (int i = 0; i < splt.length; i++) {
			if (i == 1 || i > 2) {
				temp = Integer.parseInt(splt[i]);
				result[i] = temp;
			} else {
				result[i] = splt[i];
			}
		}
		return result;
	}

	// returns the index of the first largest server
	public static int serverFinder(Object[][] serverList) {
		int indexOfLargest = 0;

		for (int i = 1; i < serverList.length; i++) {
			if ((Integer) serverList[i][4] > (Integer) serverList[i - 1][4]) {
				indexOfLargest = i;
			}
		}

		return indexOfLargest;
	}

	// Called after every REDY response. Recursive as it is a neat way to handle
	// concurrent JCPL responses.
	public static String responseHandler(String response, BufferedReader in, DataOutputStream out) {
		String[] splt = response.split(" ");
		if (splt[0].equals("JCPL")) {
			try {
				out.write(("REDY\n").getBytes());
				String res = in.readLine();
				response = responseHandler(res, in, out);
				return response;
			} catch (UnknownHostException e) {
				// prints error message if host cannot be resolved
				System.out.println("Sock:" + e.getMessage());
			} catch (EOFException e) {
				System.out.println("EOF:" + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO:" + e.getMessage());
			}
			if (s != null)
				try {
					s.close();
				} catch (IOException e) {
					System.out.println("close:" + e.getMessage());
				}
		}

		return response;
	}

	public static void main(String args[]) {
		try {
			int serverPort = 50000;
			s = new Socket("localhost", serverPort);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new DataOutputStream(s.getOutputStream());

			// begins handshake
			out.write(("HELO\n").getBytes());
			System.out.println("C SENT HELO");
			String response = in.readLine();
			System.out.println("C RCVD " + response);

			// authorises using system username
			String username = System.getProperty("user.name");
			out.write(("AUTH " + username + "\n").getBytes());
			System.out.println("C SENT AUTH " + username);
			response = in.readLine();
			System.out.println("C RCVD " + response);

			// Schedule loop. Breaks when there are no more jobs left to schedule.
			while (true) {
				out.write(("REDY\n").getBytes());
				System.out.println("C SENT REDY");
				response = in.readLine();
				System.out.println("C RCVD " + response);
				// After each REDY response I call responseHandler to check if response is JCPL
				// so it can be handled accordingly.

				response = responseHandler(response, in, out);

				// checks if there are any more jobs to schedule
				if (response.equals("NONE")) {
					break;
				}

				// if we reach this point we are guaranteed to have a JOBN as response.
				// we put it through the intParser to retrieve relevant numerical values form JOBN for later use
				int[] JOBNInfo = intParser(response, 1);

				// begins to retrieve information on server
				out.write((("GETS Capable " + JOBNInfo[3] + " " + JOBNInfo[4] + " " + JOBNInfo[5] + "\n").getBytes()));
				System.out.println("C SENT GETS Capable " + JOBNInfo[3] + " " + JOBNInfo[4] + " " + JOBNInfo[5]);
				response = in.readLine();
				System.out.println("C RCVD " + response);

				// parses numerical data from DATA response for later use.
				int[] parsedDATA = intParser(response, 1);

				// request server list
				out.write(("OK\n").getBytes());

				// adds server information to an array of strings.
				Object[][] serverList = new Object[parsedDATA[0]][];
				for (int i = 0; i < parsedDATA[0]; i++) {
					response = in.readLine();
					System.out.println("C RCVD " + response);
					serverList[i] = serverParser(response);
				}

				out.write(("OK\n").getBytes());
				System.out.println("C SENT OK");
				response = in.readLine();
				System.out.println("C RCVD " + response);

				// finds the index of largest server

				if( largestServerType == null){ 
					int largestServerIndex = serverFinder(serverList);
					largestServerType = serverList[largestServerIndex];

				}



				out.write(("OK\n").getBytes());
				System.out.println("C SENT OK");
				response = in.readLine();
				System.out.println("C RCVD " + response);

				out.write(("SCHD " + JOBNInfo[1] + " " + largestServerType[0] + " "
						+ largestServerType[1] + "\n").getBytes());
				System.out.println("C SENT" + JOBNInfo[1] + " " + largestServerType[0] + " "
				+ largestServerType[1]);
				response = in.readLine();
				System.out.println("C RCVD " + response);

				response = in.readLine();
				System.out.println("C RCVD " + response);
			}

			out.write(("QUIT\n").getBytes());
			System.out.println("C SENT QUIT");
			response = in.readLine();
			System.out.println("C RCVD " + response);

			// Catching exceptions for network errors:
		} catch (UnknownHostException e) {
			// prints error message if host cannot be resolved
			System.out.println("Sock:" + e.getMessage());
		} catch (EOFException e) {
			// prints error message if end of steam is unexpectedly reached.
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			// prints error message if IO error occurs.
			System.out.println("IO:" + e.getMessage());
		}
		// throws excption if unable to close socket
		if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				System.out.println("close:" + e.getMessage());
			}
	}

}
