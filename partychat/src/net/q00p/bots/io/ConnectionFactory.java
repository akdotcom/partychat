package net.q00p.bots.io;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;

public class ConnectionFactory {
	private static ConnectionManager cm = new ConnectionManager();
	
	private static final String DEFAULT_SERVER = "talk.google.com";
	private static final String DEFAULT_DOMAIN = "gmail.com";
	
	public static void init() {
    // Don't want offline messages
		ProviderManager.getInstance().
		    removeExtensionProvider("x", "jabber:x:delay");	
	}
	
	public static Connection getConnection(String username, String password) {
	  String server = DEFAULT_SERVER;
	  String domain = DEFAULT_DOMAIN;
	  
	  int atIndex = username.indexOf("@");
	  if (atIndex != -1) {
	    server = domain = username.substring(atIndex + 1);
	    username = username.substring(0, atIndex);
	  }
	  
		XMPPConnection connection;
		try {
			connection = new XMPPConnection(
			    new ConnectionConfiguration(server, 5222, domain));
			connection.connect();
			connection.login(username, password);
		} catch (XMPPException e) {
			throw new GoogleTalkException(e);
		}
		Connection conn = new Connection(connection);
		cm.addConnection(username + "@" + domain, conn);

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
