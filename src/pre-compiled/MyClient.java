import java.net.*;
import java.util.Arrays;
import java.io.*;

public class MyClient {
	static BufferedReader in;
	static DataOutputStream out;
	static Socket s;

	public static int[] parseJOBN(String str){
		String[] splt = str.split(" ");
		int[] args = new int[3];
		System.out.println(Arrays.toString(splt));
		int index = 4;
		for(int x = 0; x < 3; x++){
			args[x] = Integer.parseInt(splt[index]);
			System.out.println(Arrays.toString(args));
			index++;
		}

		int[] result = null;

		return result;
	}

	public static void main (String args[]) {
	try{
		int serverPort = 50000;
		s = new Socket("localhost", serverPort);
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new DataOutputStream( s.getOutputStream());

		out.write(("HELO\n").getBytes());
		String response = in.readLine();
		System.out.println("Received: "+ response);

		String username = System.getProperty("user.name");
		out.write(("AUTH" + username + "\n").getBytes());
		response = in.readLine();
		System.out.println("Received: "+ response);

		while(true){
			out.write(("REDY\n").getBytes());
			response = in.readLine();
			System.out.println("Received: "+ response);

			out.write(("GETS All\n".getBytes()));
			response = in.readLine();
			System.out.println("Received: "+ response);

			out.write(("OK\n").getBytes());
			response = in.readLine();
			System.out.println("Received: "+ response);
			
			out.write(("OK\n").getBytes());
			response = in.readLine();
			System.out.println("Received: "+ response);
			
			response = in.readLine();
			System.out.println("Received: "+ response);

			response = in.readLine();
			System.out.println("Received: "+ response);

			response = in.readLine();
			System.out.println("Received: "+ response);



			//int[] jobDeets = parseJOBN(response);

			// out.write(("GETS\n").getBytes());
			// response = in.readLine();
			// System.out.println("Received: "+ response);

			// if(response.equals("NONE")){
			// 	System.out.println("NONE!!!!");
			// 	break;
			// }
			break;
		}
		System.out.println("before quit");
		out.write(("QUIT\n").getBytes());
		response = in.readLine();
		System.out.println("Received: "+ response);
		
	} catch (UnknownHostException e){
		//prints error message if host cannot be resolved
		System.out.println("Sock:"+e.getMessage());
	}catch (EOFException e){System.out.println("EOF:"+e.getMessage());
	}catch (IOException e){System.out.println("IO:"+e.getMessage());}
	if (s != null)
			try {
			s.close();
		} catch (IOException e) {
			System.out.println("close:" + e.getMessage());
			}
	}

}

