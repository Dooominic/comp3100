import java.net.*;
import java.util.Arrays;
import java.io.*;

public class MyClient {
	static BufferedReader in;
	static DataOutputStream out;
	static Socket s;

	// returns an array of integers which represents data given to us by server
	public static int[] intParser(String str, int idx) {
		System.out.println("in intParser, response is " + str);
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
	// Called after every REDY response. Recursive as it is a neat way to handle concurrent JCPL responses.
	public static String JCPLHandler(String response, BufferedReader in, DataOutputStream out) {
		String[] splt = response.split(" ");
		if (splt[0].equals("JCPL")) {
			try {
				out.write(("REDY\n").getBytes());
				String res = in.readLine();
				System.out.println(res);
				response = JCPLHandler(res, in, out);
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

			out.write(("HELO\n").getBytes());
			String response = in.readLine();
			System.out.println("Received: " + response);

			String username = System.getProperty("user.name");
			out.write(("AUTH" + username + "\n").getBytes());
			response = in.readLine();
			System.out.println("Received: " + response);

			//Schedule loop. Breaks when there are no more jobs left to schedule.
			while (!response.equals("NONE")) {
				out.write(("REDY\n").getBytes());
				response = in.readLine();
				System.out.println("Received: " + response);
				response = JCPLHandler(response, in, out);

				if(response.equals("NONE")){
					break;
				}

				int[] JOBNInfo = intParser(response, 1);
				System.out.println(Arrays.toString(JOBNInfo));

				out.write(("GETS All\n".getBytes()));
				response = in.readLine();
				System.out.println("Received: " + response);
				

				int[] parsedDATA = intParser(response, 1);

				out.write(("OK\n").getBytes());

				String[] serverList = new String[parsedDATA[0]];

				for (int i = 0; i < parsedDATA[0]; i++) {
					response = in.readLine();
					System.out.println("Received: " + response);
					serverList[i] = response;
				}
				out.write(("OK\n").getBytes());
				response = in.readLine();
				System.out.println("Received: " + response);

				Object[][] serverInfo = new Object[serverList.length][];

				for (int i = 0; i < serverList.length; i++) {
					Object[] resourceArray = serverParser(serverList[i]);
					serverInfo[i] = resourceArray;
					System.out.println(Arrays.toString(serverInfo[i]));
				}
				int largestServerIndex = serverFinder(serverInfo);

				System.out.println(largestServerIndex);

				out.write(("OK\n").getBytes());
				response = in.readLine();
				System.out.println("Received: " + response);

				out.write(("SCHD " + JOBNInfo[1] + " " + serverInfo[largestServerIndex][0] + " "
						+ serverInfo[largestServerIndex][1] + "\n").getBytes());
				response = in.readLine();
				System.out.println("Received: " + response);

				response = in.readLine();
				System.out.println("Received: " + response);

				System.out.println("Loop ended");
			}

			System.out.println("before quit");
			out.write(("QUIT\n").getBytes());
			response = in.readLine();
			System.out.println("Received: " + response);

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

}
