package net.q00p.bots.io;

import net.q00p.bots.Message;
import net.q00p.bots.MessageSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates connections and sends messages.
 * @author ak
 *
 */
public class ConnectionManager implements MessageSender {
	private final Map<String, Connection> connections;
	
	public ConnectionManager() {
		connections = new HashMap<String, Connection>();
	}
	
	void addConnection(String userName, Connection conn) {
		connections.put(userName, conn);
	}

	/**
	 * Looks at the "from" part to determine which connection/username to send
	 * the message from.
	 */
	public void sendMessage(Message message) {
		connections.get(message.getFrom().getName()).sendMessage(message);
	}
}
