
/* 
 * TCPProxyServer.java 
 * 
 * Version: 1 TCPProxyServer.java, v 1.1 2016/19/09 22:23:06 
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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Project #1 This class run as TCP proxy-server.
 *
 * @author Kapil Dole
 */
public class TCPProxyServer implements Runnable {
	InetAddress serverIPAddress;
	String serverCommunicationProtocol, serverPort;
	int proxyServerPortTCP;

	/**
	 * Constructor for initialization of TCP proxy-server object.
	 * 
	 * @param serverIPAddress
	 *            IP address of the server.
	 * @param serverCommunicationProtocol
	 *            communication protocol used for contacting server.
	 * @param serverPort
	 *            port used for communicating with the server.
	 */
	public TCPProxyServer(InetAddress serverIPAddress, String serverCommunicationProtocol, String serverPort,
			int proxyServerPortTCP) {
		this.serverIPAddress = serverIPAddress;
		this.serverCommunicationProtocol = serverCommunicationProtocol;
		this.serverPort = serverPort;
		this.proxyServerPortTCP = proxyServerPortTCP;
		System.out.println("Listening for TCP on Port: " + proxyServerPortTCP);
	}

	/**
	 * Override method from the Runnable interface.
	 * 
	 * @return none.
	 */
	public void run() {
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(proxyServerPortTCP);

			while (true) {
				// Accepting client connection at given port.
				Socket clientSocket = serverSocket.accept();
				ResponsePacket responsePacket;
				System.out.println("Connected to client with IP address "
						+ clientSocket.getInetAddress().getHostAddress() + " using TCP protocol.");
				ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

				// Reading request packet.
				RequestPacket requestPacket = (RequestPacket) inputStream.readObject();
				System.out.println("Received request from client.");

				if (serverCommunicationProtocol == null) {
					serverCommunicationProtocol = requestPacket.getProtocolType();
				}

				System.out.println("Forwarding the request to server with IP address "
						+ serverIPAddress.getHostAddress() + " using " + serverCommunicationProtocol + " protocol.");
				// Forwarding request packet based on the specified protocol and
				// if not specified,
				// based on client protocol. Also, receiving response from the
				// server.
				if (serverCommunicationProtocol.equals("TCP")) {
					Socket socket = new Socket(serverIPAddress.getHostAddress(), Integer.parseInt(serverPort));
					ObjectOutputStream proxyOutputStream = new ObjectOutputStream(socket.getOutputStream());
					proxyOutputStream.writeObject(requestPacket);

					ObjectInputStream proxyInputStream = new ObjectInputStream(socket.getInputStream());
					responsePacket = (ResponsePacket) proxyInputStream.readObject();
					System.out.println("Received response from the server.");
					System.out.println(
							"Forwarding response to client with IP address " + requestPacket.getSourceIPAddress());
					System.out.println("--------------------------------------------------------");
				} else {
					DatagramSocket socket = new DatagramSocket();
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream(6400);
					ObjectOutputStream proxyOutputStream = new ObjectOutputStream(byteStream);
					proxyOutputStream.writeObject(requestPacket);
					byte[] response = byteStream.toByteArray();
					proxyOutputStream.flush();
					proxyOutputStream.close();
					DatagramPacket proxyRequestPacket = new DatagramPacket(response, response.length, serverIPAddress,
							Integer.parseInt(serverPort));
					socket.send(proxyRequestPacket);

					byte[] inputData = new byte[1024];
					DatagramPacket inputDatagramPacket = new DatagramPacket(inputData, inputData.length);
					socket.receive(inputDatagramPacket);
					ObjectInputStream proxyInputStream = new ObjectInputStream(
							new ByteArrayInputStream(inputDatagramPacket.getData()));
					responsePacket = (ResponsePacket) proxyInputStream.readObject();
					System.out.println("Received response from the server.");
					System.out.println("Forwarding response to client at " + requestPacket.getSourceIPAddress());
					System.out.println("--------------------------------------------------------");
				}
				// Forwarding the response from the main server to the client.
				ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				outputStream.writeObject(responsePacket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
