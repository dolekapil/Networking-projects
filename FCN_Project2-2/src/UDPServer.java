import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Project #2 This class basically used for receiving file packets to the
 * client.
 *
 * @author Kapil Dole
 */
public class UDPServer {
	private int port, timeOut, clientPort;
	private boolean quiet;
	private DatagramSocket serverSocket;
	private InetAddress clientIPAddress;
	private int[] fileDetails;
	private byte[] fileArray;
	private String MD5Checksum;

	/**
	 * Constructor for initialization.
	 * 
	 */
	public UDPServer(int port, int timeOut, boolean quiet) {
		this.port = port;
		this.timeOut = timeOut;
		this.quiet = quiet;
	}

	/**
	 * This method basically used for initiating the server.
	 * 
	 */
	public void startServer() {
		try {
			System.out.println("Server Started..");
			System.out.println("===================================================================");
			serverSocket = new DatagramSocket(port);
			getFileSize();
			getFile();
			calculateMD5();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically used for calculating MD5 for the received file.
	 * 
	 */
	public void calculateMD5() {
		try {
			byte[] hashCode = MessageDigest.getInstance("MD5").digest(fileArray);
			// Converting it to hexadecimal format.
			StringBuffer strBuff = new StringBuffer();
			for (int index = 0; index < hashCode.length; index++) {
				strBuff.append(Integer.toString((hashCode[index] & 0xff) + 0x100, 16).substring(1));
			}
			MD5Checksum = strBuff.toString();
			System.out.println("File Successfully received.");
			System.out.println("===================================================================");
			System.out.println("MD5 for the file is " + MD5Checksum);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically used for doing initial handshake which receives the
	 * file information.
	 */
	public void getFileSize() {
		try {
			byte[] receiveFileSize = new byte[16];
			byte[] sendFileSizeAck = new byte[20];
			DatagramPacket receiveFileSizePacket = new DatagramPacket(receiveFileSize, receiveFileSize.length);
			// Receive file information.
			serverSocket.receive(receiveFileSizePacket);
			fileDetails = new int[4];
			ByteBuffer.wrap(receiveFileSize).asIntBuffer().get(fileDetails);
			fileArray = new byte[fileDetails[0]];
			if (!quiet) {
				System.out.println("Doing initial handshake.");
				System.out.println("File information received ");
				System.out.println("File Size " + fileDetails[0] + " bytes.");
				System.out.println("Total number of packets " + fileDetails[1] + 1);
				System.out.println("MSS " + fileDetails[2]);
				System.out.println("===================================================================");
			}
			clientIPAddress = receiveFileSizePacket.getAddress();
			clientPort = receiveFileSizePacket.getPort();
			// Sending acknowledgement.
			sendFileSizeAck = "FILE SIZE RECEIVED".getBytes();
			DatagramPacket sendFileSizeAckPacket = new DatagramPacket(sendFileSizeAck, sendFileSizeAck.length,
					clientIPAddress, clientPort);
			serverSocket.send(sendFileSizeAckPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically used for receiving the file packets from the
	 * client.
	 * 
	 */
	public void getFile() {
		if (!quiet) {
			System.out.println("Receiving packets from client.");
			System.out.println("===================================================================");
		}
		int receivedDataCounter = 0, counter = 0;
		int packetNumber, packetChecksum;
		try {
			while (counter <= fileDetails[1]) {
				byte[] receivedFileData, sendFileDataAck, resendFileDataAck;
				// Initializing the file buffer.
				if (counter == fileDetails[1]) {
					receivedFileData = new byte[fileDetails[3] + 8];
				} else {
					receivedFileData = new byte[fileDetails[2] + 8];
				}
				sendFileDataAck = ByteBuffer.allocate(4).putInt(counter).array();
				DatagramPacket receiveFileSizePacket = new DatagramPacket(receivedFileData, receivedFileData.length);
				DatagramPacket sendFileSizeAckPacket = new DatagramPacket(sendFileDataAck, sendFileDataAck.length,
						clientIPAddress, clientPort);
				// Receiving the packet.
				serverSocket.receive(receiveFileSizePacket);
				receivedFileData = receiveFileSizePacket.getData();

				// Re-sending the handshaking acknowledgement.
				if (receiveFileSizePacket.getLength() == 16) {
					byte[] resendFileSizeAck = "FILE SIZE RECEIVED".getBytes();
					DatagramPacket resendFileSizeAckPacket = new DatagramPacket(resendFileSizeAck,
							resendFileSizeAck.length, clientIPAddress, clientPort);
					serverSocket.send(resendFileSizeAckPacket);
					if (!quiet) {
						System.out.println("Resending file information acknowledgement.");
					}
					continue;
				}

				// Getting packet number and checksum from the packet.
				packetNumber = ByteBuffer.wrap(Arrays.copyOfRange(receivedFileData, 0, 4)).getInt();
				packetChecksum = ByteBuffer.wrap(Arrays.copyOfRange(receivedFileData, 4, 8)).getInt();

				if (!quiet) {
					System.out.println("Packet " + packetNumber + " received.");
				}
				// Checking for the duplicate packets.
				if (packetNumber < counter) {
					if (!quiet) {
						System.out.println("Duplicate packet detected. Resending acknowledgement.");
					}
					resendFileDataAck = ByteBuffer.allocate(4).putInt(packetNumber).array();
					DatagramPacket resendFileSizeAckPacket = new DatagramPacket(resendFileDataAck,
							resendFileDataAck.length, clientIPAddress, clientPort);
					serverSocket.send(resendFileSizeAckPacket);
					continue;
				}

				// Computing checksum.
				Checksum checksum = new CRC32();
				checksum.update(Arrays.copyOfRange(receivedFileData, 8, receivedFileData.length), 0,
						receivedFileData.length - 8);
				// Checking for packet corruption.
				if (packetChecksum != (int) checksum.getValue()) {
					if (!quiet) {
						System.out.println("Packet " + packetNumber + " is corrupted.");
					}
					continue;
				}

				// Copying the data into the byte buffer.
				System.arraycopy(receivedFileData, 8, fileArray, receivedDataCounter, receivedFileData.length - 8);

				if (counter == fileDetails[1]) {
					receivedDataCounter += fileDetails[3];
				} else {
					receivedDataCounter += fileDetails[2];
				}

				// Sending packet acknowledgement.
				if (!quiet) {
					System.out.println("sending acknowledgement for packet " + packetNumber + ".");
				}
				serverSocket.send(sendFileSizeAckPacket);

				if (counter == fileDetails[1]) {
					for (int index = 0; index < 5; index++) {
						serverSocket.send(sendFileSizeAckPacket);
					}
				}
				counter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
