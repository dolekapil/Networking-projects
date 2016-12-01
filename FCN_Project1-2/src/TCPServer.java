
/* 
 * TCPServer.java 
 * 
 * Version: 1 TCPServer.java, v 1.1 2016/19/09 22:23:06 
 *   
 * Revisions: 
 *     Revision 1.1 Kapil 2016/19/09 23:23:06 
 */
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Project #1 This class run as TCP server.
 *
 * @author Kapil Dole
 */
public class TCPServer implements Runnable {
	private int TCPPort;
	private TimeServer timeServer;

	/**
	 * Constructor for initialization of TCP server.
	 * 
	 * @param TCPPort
	 *            Listening port.
	 * @param timeServer
	 *            Object of the time server.
	 */
	public TCPServer(int TCPPort, TimeServer timeServer) {
		this.TCPPort = TCPPort;
		System.out.println("Listening for TCP on Port: " + TCPPort);
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
			ServerSocket serverSocket = new ServerSocket(TCPPort);

			while (true) {
				// Accepting client connection.
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connected to client with IP address "
						+ clientSocket.getInetAddress().getHostAddress() + " using TCP protocol.");
				ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
				// Receiving request packet from client.
				RequestPacket requestPacket = (RequestPacket) inputStream.readObject();
				System.out.println("Received request from client.");
				ResponsePacket responsePacket = timeServer.processInput(requestPacket);
				// Sending response back to client.
				ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				outputStream.writeObject(responsePacket);
				System.out.println("Sending " + responsePacket.getResponseType() + " response to client.");
				System.out.println("--------------------------------------------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
