import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Project #2 This class basically used for sending file packets to the server.
 *
 * @author Kapil Dole
 */
public class UDPClient {
	private String fileName;
	private int port, timeOut, totalPackets, lastPacketSize;
	private InetAddress serverAddress;
	private boolean quiet;
	private final int MSS = 1440;
	private DatagramSocket clientSocket;
	private byte[] array;
	private File file;
	private String MD5Checksum;

	/**
	 * Constructor for initialization.
	 * 
	 */
	public UDPClient(String fileName, int port, int timeOut, InetAddress serverAddress, boolean quiet) {
		this.fileName = fileName;
		this.port = port;
		this.timeOut = timeOut;
		this.serverAddress = serverAddress;
		this.quiet = quiet;
	}

	/**
	 * This method basically used for initiating file transfer to server.
	 * 
	 */
	public void startClient() {
		try {
			System.out.println("Client started.");
			System.out.println("===================================================================");
			clientSocket = new DatagramSocket();
			readFile();
			sendFileDetails();
			sendFile();
			calculateMD5();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically used for calculating MD5 for the file.
	 * 
	 */
	public void calculateMD5() {
		try {
			byte[] hashCode = MessageDigest.getInstance("MD5").digest(array);
			// Converting it to hexadecimal format.
			StringBuffer strBuff = new StringBuffer();
			for (int index = 0; index < hashCode.length; index++) {
				strBuff.append(Integer.toString((hashCode[index] & 0xff) + 0x100, 16).substring(1));
			}
			MD5Checksum = strBuff.toString();
			System.out.println("File Successfully sent to server.");
			System.out.println("===================================================================");
			System.out.println("MD5 for the file is " + MD5Checksum);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically used for reading the input file and convert it into
	 * packets.
	 * 
	 */
	public void readFile() {
		if (!quiet) {
			System.out.println("Reading the file and dividing the data into packets.");
			System.out.println("===================================================================");
		}
		file = new File(fileName);
		array = new byte[(int) file.length()];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(array);
			fileInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically used for doing initial handshake with the server.
	 * 
	 */
	public void sendFileDetails() {
		try {
			// computing total number of packets.
			totalPackets = array.length / MSS;
			lastPacketSize = array.length % MSS;
			if (lastPacketSize == 0) {
				lastPacketSize = 1440;
				totalPackets--;
			}
			int[] fileDetailsArray = { array.length, totalPackets, MSS, lastPacketSize };
			ByteBuffer bbuf = ByteBuffer.allocate(fileDetailsArray.length * 4);
			IntBuffer ibuf = bbuf.asIntBuffer();
			for (int counter : fileDetailsArray) {
				ibuf.put(counter);
			}
			byte[] fileDetails = bbuf.array();
			byte[] fileSizeReply = new byte[20];
			DatagramPacket fileSizeReplyPacket = new DatagramPacket(fileSizeReply, fileSizeReply.length);
			DatagramPacket sendFileSizePacket = new DatagramPacket(fileDetails, fileDetails.length, serverAddress,
					port);
			// setting timeout.
			clientSocket.setSoTimeout(timeOut);
			boolean receivedFileSize = false;
			if (!quiet) {
				System.out.println("Doing initial handshake.");
				System.out.println("Sending file information to server.");
				System.out.println("===================================================================");
			}
			// Sending file information.
			clientSocket.send(sendFileSizePacket);
			// Checking acknowledgement.
			while (!receivedFileSize) {
				try {
					clientSocket.receive(fileSizeReplyPacket);
					String response = new String(fileSizeReplyPacket.getData());
					if (response.contains("FILE SIZE RECEIVED")) {
						receivedFileSize = true;
					} else {
						continue;
					}
				} catch (SocketTimeoutException e) {
					// If timeout occurs, re-send the packet.
					if (!quiet) {
						System.out.println("Timeout occured while sending file size. Retrying...");
					}
					clientSocket.send(sendFileSizePacket);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method basically sends the file packets to the server.
	 * 
	 */
	public void sendFile() {
		if (!quiet) {
			System.out.println("Sending packets to the server.");
			System.out.println("===================================================================");
		}
		int bytesCounter = 0;
		int sequenceCounter = 0;
		try {
			for (int counter = 0; counter <= totalPackets; counter++) {

				byte[] dataBytes, fileDataBytes, checksumBytes;
				// Computing total bytes to send to the server.
				if (counter == totalPackets) {
					dataBytes = Arrays.copyOfRange(array, bytesCounter, bytesCounter + lastPacketSize);
					bytesCounter += lastPacketSize;
					fileDataBytes = new byte[lastPacketSize + 8];
				} else {
					dataBytes = Arrays.copyOfRange(array, bytesCounter, bytesCounter + MSS);
					bytesCounter += MSS;
					fileDataBytes = new byte[MSS + 8];
				}
				// Sequence number for the packet.
				byte[] sequence = ByteBuffer.allocate(4).putInt(sequenceCounter).array();

				// checksum for the packet.
				Checksum checksum = new CRC32();
				checksum.update(dataBytes, 0, dataBytes.length);
				checksumBytes = ByteBuffer.allocate(4).putInt((int) checksum.getValue()).array();

				if (!quiet) {
					System.out.println("Sending packet " + sequenceCounter + " to server.");
				}

				// Complete data packet.
				System.arraycopy(sequence, 0, fileDataBytes, 0, 4);
				System.arraycopy(checksumBytes, 0, fileDataBytes, 4, 4);
				System.arraycopy(dataBytes, 0, fileDataBytes, 8, dataBytes.length);

				byte[] acknowledgement = new byte[4];
				DatagramPacket acknowledgementPacket = new DatagramPacket(acknowledgement, acknowledgement.length);
				DatagramPacket fileDataPacket = new DatagramPacket(fileDataBytes, fileDataBytes.length, serverAddress,
						port);
				boolean receivedAcknowledgement = false;
				// Sending packet.
				clientSocket.send(fileDataPacket);
				while (!receivedAcknowledgement) {
					try {
						// Receive acknowledgement.
						clientSocket.receive(acknowledgementPacket);
						int sequenceNumber = ByteBuffer.wrap(acknowledgementPacket.getData()).getInt();
						if (sequenceNumber == sequenceCounter) {
							receivedAcknowledgement = true;
							if (!quiet) {
								System.out.println("Received acknowledgement for packet " + sequenceNumber + ".");
							}
							sequenceCounter++;
						} else {
							continue;
						}
					} catch (SocketTimeoutException e) {
						// Re-send the packet if timeout occurs.
						if (!quiet) {
							System.out.println(
									"Timeout occured while sending packet" + sequenceCounter + ". Retrying...");
						}
						clientSocket.send(fileDataPacket);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
