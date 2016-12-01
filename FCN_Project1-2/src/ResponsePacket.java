
/* 
 * ResponsePacket.java 
 * 
 * Version: 1 ResponsePacket.java, v 1.1 2016/19/09 22:23:06 
 *   
 * Revisions: 
 *     Revision 1.1 Kapil 2016/19/09 23:23:06 
 */
import java.io.Serializable;

/**
 * Project #1 This class defines response packet structure.
 *
 * @author Kapil Dole
 */
public class ResponsePacket implements Serializable {
	private String responseType;
	private String response;

	/**
	 * Constructor for initialization of packet.
	 * 
	 * @param responseType
	 *            SUCCESS/ERROR
	 * @param response
	 *            response body.
	 */
	public ResponsePacket(String responseType, String response) {
		this.setResponseType(responseType);
		this.setResponse(response);
	}

	/**
	 * @return the responseType
	 */
	public String getResponseType() {
		return responseType;
	}

	/**
	 * @param responseType
	 *            the responseType to set
	 */
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}

}
