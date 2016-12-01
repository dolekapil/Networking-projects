
/* 
 * TCPClient.java 
 * 
 * Version: 1 TCPClient.java, v 1.1 2016/19/09 22:23:06 
 *   
 * Revisions: 
 *     Revision 1.1 Kapil 2016/19/09 23:23:06 
 */
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Project #1 This class run as TCP client.
 *
 * @author Kapil Dole
 */
public class TCPClient {
	private RequestPacket requestPacket;
	private String[] args;
	int requestNumber;

	/**
	 * Constructor for initialization of TCP client object.
	 * 
	 * @param requestPacket
	 *            RequestPacket object.
	 * @param requestNumber
	 *            number of the request packet.
	 */
	public TCPClient(RequestPacket requestPacket, String[] args, int requestNumber) {
		this.requestPacket = requestPacket;
		this.args = args;
		this.requestNumber = requestNumber;
	}

	/**
	 * This method basically used for requesting time from time server.
	 * 
	 * @return none.
	 */
	public void requestTime() {
		Socket socket;
		long startTime, endTime;
		try {
			socket = new Socket(args[1], Integer.parseInt(args[args.length - 1]));
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			startTime = System.currentTimeMillis();
			outputStream.writeObject(requestPacket);
			if (requestPacket.getRequestType().equals("GET")) {
				System.out.println("Sending 'GET' request " + requestNumber + " for requesting time from server.");
			} else {
				System.out.println("Sending 'SET' request " + requestNumber + " for setting server time to "
						+ requestPacket.getUpdatedUTC());
			}

			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			ResponsePacket responsePacket = (ResponsePacket) inputStream.readObject();
			endTime = System.currentTimeMillis();
			System.out.println("Received response from server.");
			System.out.println(responsePacket.getResponse());
			System.out.println("Round Trip Time(RTT): " + (endTime - startTime));
			System.out.println("--------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
