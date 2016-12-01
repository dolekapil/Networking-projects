/* 
 * UDPServer.java 
 * 
 * Version: 1 UDPServer.java, v 1.1 2016/19/09 22:23:06 
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

/**
 * Project #1 This class run as UDP server.
 *
 * @author Kapil Dole
 */
public class UDPServer implements Runnable {
	private int UDPPort;
	private TimeServer timeServer;

	/**
	 * Constructor for initialization of TCP server.
	 * 
	 * @param UDPPort
	 *            Listening port.
	 * @param timeServer
	 *            Object of the time server.
	 */
	public UDPServer(int UDPPort, TimeServer timeServer) {
		this.UDPPort = UDPPort;
		System.out.println("Listening for UDP on Port: " + UDPPort);
		System.out.println("--------------------------------------------------------");
		this.timeServer = timeServer;
	}

	/**
	 * Override method from the Runnable interface.
	 * 
	 * @return none.
	 */
	public void run() {
		try {
			@SuppressWarnings("resource")
			DatagramSocket socket = new DatagramSocket(UDPPort);
			byte[] inputData = new byte[1024];

			while (true) {
				DatagramPacket inputDatagramPacket = new DatagramPacket(inputData, inputData.length);
				// Receiving request from the client.
				socket.receive(inputDatagramPacket);
				ObjectInputStream inputStream = new ObjectInputStream(
						new ByteArrayInputStream(inputDatagramPacket.getData()));
				RequestPacket requestPacket = (RequestPacket) inputStream.readObject();
				System.out.println("Connected to client with IP address " + requestPacket.getSourceIPAddress()
						+ " using UDP protocol.");
				System.out.println("Received request from client.");

				ResponsePacket responsePacket = timeServer.processInput(requestPacket);
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(6400);
				ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
				outputStream.writeObject(responsePacket);
				byte[] response = byteStream.toByteArray();
				outputStream.flush();
				outputStream.close();
				DatagramPacket replyPacket = new DatagramPacket(response, response.length,
						inputDatagramPacket.getAddress(), inputDatagramPacket.getPort());
				// Sending response back to the client.
				socket.send(replyPacket);
				System.out.println("Sending " + responsePacket.getResponseType() + " response to client.");
				System.out.println("--------------------------------------------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
