package net.q00p.bots.io;

import org.jivesoftware.smack.GoogleTalkConnection;
import org.jivesoftware.smack.XMPPException;

public class ConnectionFactory {
	private static ConnectionManager cm = new ConnectionManager();
	
	public static Connection getConnection(String username, String password) {

		GoogleTalkConnection connection;
		try {
			connection = new GoogleTalkConnection();
			connection.login(username, password);
		} catch (XMPPException e) {
			throw new GoogleTalkException(e);
		}
		Connection conn = new Connection(connection);
		cm.addConnection(username+"@gmail.com", conn);

		return conn;
	}
	
	public static ConnectionManager getConnectionManager() {
		return cm;
	}
	
	@SuppressWarnings("serial")
	public static class GoogleTalkException extends RuntimeException {
		public GoogleTalkException() { super(); }
		public GoogleTalkException(Exception e) { super(e); }
	}
}
