import java.net.*;
import java.util.Arrays;
import java.io.*;

public class MyClient {
	static BufferedReader in;
	static DataOutputStream out;
	static Socket s;
	static Object[] largestServerType = null;
	static String response;
	static Object[][] serverList;
	static int serverIndex = 0; 
	static Object[][]largestList;
	static int lrrindex=0;
	// sends a message and returns the current state of the input stream
	public static BufferedReader send(String message, BufferedReader in, DataOutputStream out) {
		try {
			out.write((message + "\n").getBytes());
			System.out.println("C SENT " + message);
			return in;

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
		return null;
	}

	// reads whats in the input stream (recieves message)
	public static String rcv(BufferedReader in) {
		try {
			String response = in.readLine();
			System.out.println("C RCVD " + response);
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
		return null;
	}

	// a function to send requests. (send a message and read the response)
	public static String request(String message, BufferedReader in, DataOutputStream out) {
		in = send(message, in, out);
		response = rcv(in);
		return response;
	}

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
		System.out.println(	 Arrays.toString(serverList));
		for (int i = 1; i < serverList.length; i++) {
			System.out.println("in loop");
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
			String res = request("REDY", in, out);
			response = responseHandler(res, in, out);
		}
		return response;
	}

	// builds server list
	public static Object[][] buildList(int parsedDATA, BufferedReader in, DataOutputStream out) {
		// get server list
		in = send("OK", in, out);

		// adds server information to an array of strings.
		Object[][] serverList = new Object[parsedDATA][];
		for (int i = 0; i < parsedDATA; i++) {
			response = rcv(in);
			serverList[i] = serverParser(response);
		}
		return serverList;
	}

	public static void ff(Object[] serverList, int JOBNInfo, BufferedReader in, DataOutputStream out){
		
		response = request("OK", in, out);

		response = request(("SCHD " + JOBNInfo + " " + serverList[0] + " "
				+ serverList[1]), in, out);

		response = rcv(in);
	}

	public static void lrr(Object[][] serverList, int JOBNInfo, BufferedReader in, DataOutputStream out) {

		// finds the index of largest serer
		if (lrrindex > serverList.length - 1){
			lrrindex = 0;
		}

		
		response = request("OK", in, out);

		response = request(("SCHD " + JOBNInfo + " " + serverList[lrrindex][0] + " "
				+ serverList[lrrindex][1]), in, out);

		response = rcv(in);

	}

	public static void main(String args[]) {
		try {
			boolean first = true;
			int serverPort = 50000;
			s = new Socket("localhost", serverPort);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new DataOutputStream(s.getOutputStream());

			// begins handshake
			response = request("HELO", in, out);

			// authorises using system username
			String username = System.getProperty("user.name");
			response = request("AUTH " + username, in, out);

			// Schedule loop. Breaks when there are no more jobs left to schedule.
			while (true) {
				response = request("REDY", in, out);

				// After each REDY response I call responseHandler to check if response is JCPL
				// so it can be handled accordingly.
				response = responseHandler(response, in, out);

				// checks if there are any more jobs to schedule
				if (response.equals("NONE")) {
					break;
				}
				// if we reach this point we are guaranteed to have a JOBN as response.
				// we put it through the intParser to retrieve relevant numerical values form
				// JOBN for later use
				int[] JOBNInfo = intParser(response, 1);

				
				if(first){
				// begins to retrieve information on server
				first = false;
				response = request("GETS Capable " + JOBNInfo[3] + " " + JOBNInfo[4] + " " + JOBNInfo[5], in, out);

				// parses numerical data from DATA response for later use.
				int[] parsedDATA = intParser(response, 1);

				// adds server information to an array of objects.
				serverList = buildList(parsedDATA[0], in, out);

				int largestServerIndex = serverFinder(serverList);

				largestServerType = serverList[largestServerIndex];
				largestList = Arrays.copyOfRange(serverList,largestServerIndex,serverList.length);
				
				response = request("OK", in, out);
				}
				else{
					Arrays.toString(JOBNInfo);
					response = request("GETS Avail " + JOBNInfo[3] + " " + JOBNInfo[4] + " " + JOBNInfo[5], in, out);

				// parses numerical data from DATA response for later use.
				int[] parsedDATA = intParser(response, 1);

				// adds server information to an array of objects.
				serverList = buildList(parsedDATA[0], in, out);
				
				response = request("OK", in, out);
				}

				if(serverList.length == 0)
					lrr(largestList, JOBNInfo[1], in, out);
				else{
					ff(serverList[0], JOBNInfo[1], in, out);
				}
				
			}

			response = request(("QUIT"), in, out);

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
