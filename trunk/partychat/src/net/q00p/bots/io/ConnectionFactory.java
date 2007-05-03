package net.q00p.bots.io;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;

public class ConnectionFactory {
	private static ConnectionManager cm = new ConnectionManager();
	
	public static Connection getConnection(String username, String password) {

		XMPPConnection connection;
		try {
			connection = new XMPPConnection(new ConnectionConfiguration("talk.google.com", 5222, "gmail.com"));
			connection.connect();
			connection.login(username, password);
		} catch (XMPPException e) {
			throw new GoogleTalkException(e);
		}
		Connection conn = new Connection(connection);
		cm.addConnection(username+"@gmail.com", conn);

		ProviderManager.getInstance().removeExtensionProvider("x", "jabber:x:delay");
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
