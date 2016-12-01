
/* 
 * UDPClient.java 
 * 
 * Version: 1 UDPClient.java, v 1.1 2016/19/09 22:23:06 
 *   
 * Revisions: 
 *     Revision 1.1 Kapil 2016/19/09 23:23:06 
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Project #1 This class run as UDP client.
 *
 * @author Kapil Dole
 */
public class UDPClient {
	private RequestPacket requestPacket;
	private String[] args;
	InetAddress serverIPAddress;
	int requestNumber;

	/**
	 * Constructor for initialization of UDP client object.
	 * 
	 * @param requestPacket
	 *            RequestPacket object.
	 * @param serverIPAddress
	 *            IP address of server.
	 * @param requestNumber
	 *            number of the request packet.
	 */
	public UDPClient(RequestPacket requestPacket, String[] args, InetAddress serverIPAddress, int requestNumber) {
		this.requestPacket = requestPacket;
		this.args = args;
		this.serverIPAddress = serverIPAddress;
		this.requestNumber = requestNumber;
	}

	/**
	 * This method basically used for requesting time from time server.
	 * 
	 * @return none.
	 */
	public void requestTime() {
		DatagramSocket socket;
		long startTime, endTime;
		try {
			socket = new DatagramSocket();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(6400);
			ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
			startTime = System.currentTimeMillis();
			// Sending request to the server.
			outputStream.writeObject(requestPacket);
			byte[] response = byteStream.toByteArray();
			outputStream.flush();
			outputStream.close();
			DatagramPacket requestDatagramPacket = new DatagramPacket(response, response.length, serverIPAddress,
					Integer.parseInt(args[args.length - 1]));
			socket.send(requestDatagramPacket);
			if (requestPacket.getRequestType().equals("GET")) {
				System.out.println("Sending 'GET' request " + requestNumber + " for requesting time from server.");
			} else {
				System.out.println("Sending 'SET' request " + requestNumber + " for setting server time to "
						+ requestPacket.getUpdatedUTC());
			}

			byte[] receiveData = new byte[1024];
			DatagramPacket responseDatagramPacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(responseDatagramPacket);
			ObjectInputStream inputStream = new ObjectInputStream(
					new ByteArrayInputStream(responseDatagramPacket.getData()));
			// Reading response from the server.
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
