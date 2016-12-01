
/* 
 * TimeServer.java 
 * 
 * Version: 1 TimeServer.java, v 1.1 2016/19/09 22:23:06 
 *   
 * Revisions: 
 *     Revision 1.1 Kapil 2016/19/09 23:23:06 
 */
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Project #1 This class run as Time server.
 *
 * @author Kapil Dole
 */
public class TimeServer {
	private String currentUTC;
	private String userName;
	private String password;

	/**
	 * Constructor for initialization of Time server.
	 * 
	 * @param currentUTC
	 *            current UTC time on server.
	 * @param userName
	 *            user name.
	 * @param password
	 *            password.
	 */
	public TimeServer(String currentUTC, String userName, String password) {
		this(currentUTC);
		System.out.println("User Name: " + userName);
		this.userName = userName;
		System.out.println("Password: " + password);
		this.password = password;
		System.out.println("--------------------------------------------------------");
	}

	/**
	 * Constructor for initialization of Time server.
	 * 
	 * @param currentUTC
	 *            current UTC time on server.
	 */
	public TimeServer(String currentUTC) {
		System.out.println("Starting as Server.");
		System.out.println("--------------------------------------------------------");
		System.out.println("Setting server time to " + currentUTC);
		this.currentUTC = currentUTC;
		System.out.println("Server started at: " + currentUTC);
		System.out.println("--------------------------------------------------------");
	}

	/**
	 * Constructor for initialization of Time server.
	 * 
	 * @param packet
	 *            request packet structure.
	 */
	public ResponsePacket processInput(RequestPacket packet) {
		// when request if GET, for requesting time.
		if (packet.getRequestType().equals("GET")) {
			if (packet.getTimeFormat().equals("UTC")) {
				System.out.println("Client requested server time in UTC format.");
				return new ResponsePacket("SUCCESS", "Current time(UTC) at server: " + currentUTC);
			} else {
				System.out.println("Client requested server time in Calender format.");
				Date date = new Date(Long.parseLong(currentUTC) * 1000L);
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
				String calenderFormatTime = dateFormat.format(date);
				return new ResponsePacket("SUCCESS", "Current time(Calender Format) at server: " + calenderFormatTime);
			}
		}
		// When request if SET, for setting time on server.
		else {
			System.out.println("Client at " + packet.getSourceIPAddress() + " trying to set server time to "
					+ packet.getUpdatedUTC());
			if (userName != null && password != null) {
				if (userName.equals(packet.getUserName()) && password.equals(packet.getPassword())) {
					currentUTC = packet.getUpdatedUTC();
					System.out.println("Server time updated successfully.");
					return new ResponsePacket("SUCCESS", "Success! Server time updated to: " + currentUTC);
				} else {
					System.out.println("Invalid credentials, time updation failed.");
					return new ResponsePacket("ERROR", "Failed! Invalid credentials.");
				}
			} else {
				System.out.println("No client is authorized to update time on server.");
				return new ResponsePacket("ERROR", "Failed! you are not authorized to update time on server.");
			}
		}
	}
}
