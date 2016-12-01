
/* 
 * tsapp.java 
 * 
 * Version: 1 tsapp.java, v 1.1 2016/19/09 22:23:06 
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
import java.net.UnknownHostException;

/**
 * Project #1 This the main class and used for parsing the arguments.
 *
 * @author Kapil Dole
 */
public class tsapp {
	public static void main(String[] args) {
		// Program terminates if arguments entered are invalid.
		if (!processArguments(args)) {
			System.out.println("Invalid Arguments! Please try again..");
			System.exit(1);
		}
	}

	/**
	 * This method basically process arguments provided by the user.
	 * 
	 * @param args
	 *            arguments provided by user.
	 */
	public static boolean processArguments(String[] args) {
		// Based on the argument call appropriate function for processing it.
		if (args[0].equals("-c")) {
			return processClient(args);
		} else if (args[0].equals("-s")) {
			return processServer(args);
		} else if (args[0].equals("-p")) {
			return processProxyServer(args);
		} else {
			return false;
		}
	}

	/**
	 * This method is used for processing client option.
	 * 
	 * @param args
	 *            arguments provided by user.
	 * 
	 * @return boolean true if success, else false.
	 */
	public static boolean processClient(String[] args) {
		String requestType, protocolType, timeFormat;
		InetAddress serverIPAddress;
		RequestPacket requestPacket;
		int numberOfRequest;
		try {
			serverIPAddress = InetAddress.getByName(args[1]);
			if (checkArgument("-t", args) != -1) {
				System.out.println("Starting as TCP Client.");
				protocolType = "TCP";
			} else {
				System.out.println("Starting as UDP Client.");
				protocolType = "UDP";
			}
			System.out.println("--------------------------------------------------------");
			if (checkArgument("-z", args) != -1) {
				timeFormat = "UTC";
			} else {
				timeFormat = "CALENDER";
			}
			if (checkArgument("-T", args) != -1) {
				requestType = "SET";
				int serverTime = checkArgument("-T", args);
				int userName = checkArgument("--user", args);
				int password = checkArgument("--pass", args);
				requestPacket = new RequestPacket(requestType, protocolType,
						InetAddress.getLocalHost().getHostAddress(), timeFormat, args[userName + 1], args[password + 1],
						args[serverTime + 1]);
			} else {
				requestType = "GET";
				requestPacket = new RequestPacket(requestType, protocolType,
						InetAddress.getLocalHost().getHostAddress(), timeFormat);
			}
			if (checkArgument("-n", args) != -1) {
				int number = checkArgument("-n", args);
				numberOfRequest = Integer.parseInt(args[number + 1]);
			} else {
				numberOfRequest = 1;
			}
			for (int count = 1; count <= numberOfRequest; count++) {
				if (protocolType.equals("TCP")) {
					TCPClient tcpClient = new TCPClient(requestPacket, args, count);
					tcpClient.requestTime();
				} else {
					UDPClient udpClient = new UDPClient(requestPacket, args, serverIPAddress, count);
					udpClient.requestTime();
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int checkArgument(String current, String[] args) {
		for (int counter = 1; counter < args.length; counter++) {
			if (current.equals(args[counter])) {
				return counter;
			}
		}
		return -1;
	}

	/**
	 * This method is used for processing server option.
	 * 
	 * @return boolean true if success, else false.
	 */
	public static boolean processServer(String[] args) {
		TimeServer timeServer;
		int TCPPort = Integer.parseInt(args[args.length - 1]);
		int UDPPort = Integer.parseInt(args[args.length - 2]);
		if (checkArgument("-T", args) != -1) {
			int serverTime = checkArgument("-T", args);
			if (checkArgument("--user", args) != -1 && checkArgument("--pass", args) != -1) {
				int userName = checkArgument("--user", args);
				int password = checkArgument("--pass", args);
				timeServer = new TimeServer(args[serverTime + 1], args[userName + 1], args[password + 1]);
			} else {
				timeServer = new TimeServer(args[serverTime + 1]);
			}
			Thread TCPThread = new Thread(new TCPServer(TCPPort, timeServer));
			TCPThread.start();
			Thread UDPThread = new Thread(new UDPServer(UDPPort, timeServer));
			UDPThread.start();
		} else {
			return false;
		}
		return true;
	}

	/**
	 * This method is used for processing proxy server option.
	 * 
	 * @return boolean true if success, else false.
	 */
	public static boolean processProxyServer(String[] args) {
		InetAddress serverIPAddress;
		int serverPort, proxyServerPortTCP, proxyServerPortUDP, tcpPortArg, udpPortArg;
		String serverCommunicationProtocol, tcpPort = null, udpPort = null;
		try {
			serverIPAddress = InetAddress.getByName(args[1]);
			proxyServerPortTCP = Integer.parseInt(args[args.length - 1]);
			proxyServerPortUDP = Integer.parseInt(args[args.length - 2]);
			System.out.println("Starting as Proxy Server.");
			System.out.println("--------------------------------------------------------");
			if (checkArgument("--proxy-tcp", args) != -1) {
				tcpPortArg = checkArgument("--proxy-tcp", args);
				tcpPort = args[tcpPortArg + 1];
			}
			if (checkArgument("--proxy-udp", args) != -1) {
				udpPortArg = checkArgument("--proxy-udp", args);
				udpPort = args[udpPortArg + 1];
			}
			if (checkArgument("-t", args) != -1) {
				serverCommunicationProtocol = "TCP";
				if (checkArgument("--proxy-tcp", args) == -1) {
					return false;
				}
			} else if (checkArgument("-u", args) != -1) {
				serverCommunicationProtocol = "UDP";
				if (checkArgument("--proxy-udp", args) == -1) {
					return false;
				}
			} else {
				serverCommunicationProtocol = null;
			}
			if (tcpPort == null) {
				tcpPort = udpPort;
			}
			if (udpPort == null) {
				udpPort = tcpPort;
			}
			Thread TCPThread = new Thread(
					new TCPProxyServer(serverIPAddress, serverCommunicationProtocol, tcpPort, proxyServerPortTCP));
			TCPThread.start();
			Thread UDPThread = new Thread(
					new UDPProxyServer(serverIPAddress, serverCommunicationProtocol, udpPort, proxyServerPortUDP));
			UDPThread.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
