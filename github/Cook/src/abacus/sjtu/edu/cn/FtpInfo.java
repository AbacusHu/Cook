/*
 * Copyright (c) 2009, International Business Machines Corporation, 
 * CDL User Technologies Development. All rights reserved.
 */
package abacus.sjtu.edu.cn;

public class FtpInfo {

	public static final int PORT_DEFAULT = 21;

	private String protocol;
	private String host;
	private int port;
	private String userId;
	private String password;

	public FtpInfo(String protocol, String host, String userId, String password) {
		this(protocol, host, PORT_DEFAULT, userId, password);
	}

	public FtpInfo(String protocol, String host, int port, String userId,
			String password) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.userId = userId;
		this.password = password;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public String getUserId() {
		return userId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	/************************* User defined method ***********************************/

	public boolean isSecure() {
		if ("ftps".equals(protocol)) {
			return true;
		} else {
			return false;
		}
	}

	public String getModifiedPassword() {
		if (getPassword() != null && getPassword().length() >= 4) {
			return getPassword().substring(0, 4) + "****";
		}
		return "";
	}
}
