
/* 
 * RequestPacket.java 
 * 
 * Version: 1 RequestPacket.java, v 1.1 2016/19/09 22:23:06 
 *   
 * Revisions: 
 *     Revision 1.1 Kapil 2016/19/09 23:23:06 
 */
import java.io.Serializable;

/**
 * Project #1 This class defines request packet structure.
 *
 * @author Kapil Dole
 */
public class RequestPacket implements Serializable {
	private String requestType;
	private String protocolType;
	private String sourceIPAddress;
	private String userName;
	private String password;
	private String updatedUTC;
	private String timeFormat;

	/**
	 * Constructor for initialization of packet.
	 * 
	 * @param requestType
	 *            GET/SET
	 * @param protocolType
	 *            TCP/UDP
	 * @param sourceIPAddress
	 *            IP address of requester.
	 * @param timeFormat
	 *            UTC or Calendar.
	 */
	public RequestPacket(String requestType, String protocolType, String sourceIPAddress, String timeFormat) {
		this.setRequestType(requestType);
		this.setProtocolType(protocolType);
		this.setSourceIPAddress(sourceIPAddress);
		this.setTimeFormat(timeFormat);
	}

	/**
	 * Constructor for initialization of packet.
	 * 
	 * @param requestType
	 *            GET/SET
	 * @param protocolType
	 *            TCP/UDP
	 * @param sourceIPAddress
	 *            IP address of requester.
	 * @param timeFormat
	 *            UTC or Calendar.
	 * @param userName
	 *            user name.
	 * @param password
	 *            password.
	 * @param updatedUTC
	 *            updated server time in UTC.
	 */
	public RequestPacket(String requestType, String protocolType, String sourceIPAddress, String timeFormat,
			String userName, String password, String updatedUTC) {
		this(requestType, protocolType, sourceIPAddress, timeFormat);
		this.setUserName(userName);
		this.setPassword(password);
		this.setUpdatedUTC(updatedUTC);
	}

	/**
	 * @return the requestType
	 */
	public String getRequestType() {
		return requestType;
	}

	/**
	 * @param requestType
	 *            the requestType to set
	 */
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	/**
	 * @return the protocolType
	 */
	public String getProtocolType() {
		return protocolType;
	}

	/**
	 * @param protocolType
	 *            the protocolType to set
	 */
	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	/**
	 * @return the sourceIPAddress
	 */
	public String getSourceIPAddress() {
		return sourceIPAddress;
	}

	/**
	 * @param sourceIPAddress
	 *            the sourceIPAddress to set
	 */
	public void setSourceIPAddress(String sourceIPAddress) {
		this.sourceIPAddress = sourceIPAddress;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the updatedUTC
	 */
	public String getUpdatedUTC() {
		return updatedUTC;
	}

	/**
	 * @param updatedUTC
	 *            the updatedUTC to set
	 */
	public void setUpdatedUTC(String updatedUTC) {
		this.updatedUTC = updatedUTC;
	}

	/**
	 * @return the timeFormat
	 */
	public String getTimeFormat() {
		return timeFormat;
	}

	/**
	 * @param timeFormat
	 *            the timeFormat to set
	 */
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
}
