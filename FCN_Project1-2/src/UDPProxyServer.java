
/* 
 * UDPProxyServer.java 
 * 
 * Version: 1 UDPProxyServer.java, v 1.1 2016/19/09 22:23:06 
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
import java.net.Socket;

/**
 * Project #1 This class run as UDP proxy-server.
 *
 * @author Kapil Dole
 */
public class UDPProxyServer implements Runnable {
	InetAddress serverIPAddress;
	String serverCommunicationProtocol, serverPort;
	int proxyServerPortUDP;

	/**
	 * Constructor for initialization of TCP proxy-server object.
	 * 
	 * @param serverIPAddress
	 *            IP address of the server.
	 * @param serverCommunicationProtocol
	 *            communication protocol used for contacting server.
	 * @param serverPort
	 *            port used for communicating with the server.
	 * @param proxyServerPortUDP
	 *            port on which server will be listening.
	 */
	public UDPProxyServer(InetAddress serverIPAddress, String serverCommunicationProtocol, String serverPort,
			int proxyServerPortUDP) {
		this.serverIPAddress = serverIPAddress;
		this.serverCommunicationProtocol = serverCommunicationProtocol;
		this.serverPort = serverPort;
		this.proxyServerPortUDP = proxyServerPortUDP;
		System.out.println("Listening for UDP on Port: " + proxyServerPortUDP);
		System.out.println("--------------------------------------------------------");
	}

	/**
	 * Override method from the Runnable interface.
	 * 
	 * @return none.
	 */
	public void run() {
		try {
			@SuppressWarnings("resource")
			DatagramSocket proxyReceiveSocket = new DatagramSocket(proxyServerPortUDP);
			byte[] inputData = new byte[1024];
			ResponsePacket responsePacket;

			while (true) {
				DatagramPacket inputDatagramPacket = new DatagramPacket(inputData, inputData.length);
				// Receiving request from the client.
				proxyReceiveSocket.receive(inputDatagramPacket);
				ObjectInputStream inputStream = new ObjectInputStream(
						new ByteArrayInputStream(inputDatagramPacket.getData()));
				RequestPacket requestPacket = (RequestPacket) inputStream.readObject();
				System.out.println("Connected to client with IP address " + requestPacket.getSourceIPAddress()
						+ " using UDP protocol.");
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
					System.out.println("Forwarding response to client at " + requestPacket.getSourceIPAddress());
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

					byte[] proxyInputData = new byte[1024];
					DatagramPacket proxyInputDatagramPacket = new DatagramPacket(proxyInputData, proxyInputData.length);
					socket.receive(proxyInputDatagramPacket);
					ObjectInputStream proxyInputStream = new ObjectInputStream(
							new ByteArrayInputStream(proxyInputDatagramPacket.getData()));
					responsePacket = (ResponsePacket) proxyInputStream.readObject();
					System.out.println("Received response from the server.");
					System.out.println("Forwarding response to client at " + requestPacket.getSourceIPAddress());
					System.out.println("--------------------------------------------------------");
				}

				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(6400);
				ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
				// forwarding response back to the client.
				outputStream.writeObject(responsePacket);
				byte[] response = byteStream.toByteArray();
				outputStream.flush();
				outputStream.close();
				DatagramPacket replyPacket = new DatagramPacket(response, response.length,
						inputDatagramPacket.getAddress(), inputDatagramPacket.getPort());
				proxyReceiveSocket.send(replyPacket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
