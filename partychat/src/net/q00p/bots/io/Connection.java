package net.q00p.bots.io;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import net.q00p.bots.Bot;
import net.q00p.bots.Message;
import net.q00p.bots.MessageSender;
import net.q00p.bots.User;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * Wrapper for Google talk connection that exposes the ability to send messages.
 * 
 * @author ak
 */
public class Connection implements MessageSender {
	private final XMPPConnection connection;
	
	private final Queue<Message> messagesToSend;
	private final Timer timer = new Timer();
	private final Map<User,Chat> chats = new HashMap<User,Chat>();

	protected Connection(XMPPConnection con) {
		connection = con;
	    assert connection.isConnected();

	    messagesToSend = new LinkedList<Message>();
	    timer.schedule(new SendMessageTask(), new Date(), 10);
	    Logger.log("Connection: " + this.toString() + " created", true);
	}
	
	public String getSendingUser() {
		return connection.getUser();
	}

	/**
	 * Add the bot as a listener to this connection
	 * 
	 * @param bot
	 */
	public void addBot(final Bot bot) {
	    PacketListener listener = new PacketListener() {
	        public void processPacket(Packet packet) {
	          assert packet != null;
	          org.jivesoftware.smack.packet.Message rawMessage = 
	        	  	(org.jivesoftware.smack.packet.Message)packet;
	          if (rawMessage.getBody() != null) {
	            Logger.log(rawMessage.getFrom() + " -> " + rawMessage.getTo()); 
	            User user = User.get(packet.getFrom());
	            Message message = new Message(user, 
	            		User.get(packet.getTo().split("/")[0], bot.botName()),
	            		rawMessage.getBody());
	            bot.handleMessage(message);
	          }
	        }
	      };
	      PacketFilter filter = 
	    	  	new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.chat);
	      connection.addPacketListener(listener, filter);
	      Logger.log(bot.botName() + " added to " + toString(), true);
	}
	
	@Override
    public String toString() {
		return connection.getUser() + ":" + connection.getServiceName() + ":" +
			connection.getConnectionID();
	}
	
	public void sendMessage(Message msg) {
		assert msg != null;
		messagesToSend.offer(msg);
	}
	
	/**
	 *  Gets a Chat if it's already been created, or create one if necessary.
	 */
	public Chat getOrCreateChat(User user, boolean remember) {
		Chat chat = chats.get(user);
		if (chat == null) {
			chat = connection.getChatManager().createChat(user.getName(), new MessageListener() {
				public void processMessage(Chat c, org.jivesoftware.smack.packet.Message m) {}
			});
			if (remember) {
				chats.put(user, chat);
			}
		}
		return chat;
	}
	
	/**
	 * Call this if you plan to engage this user in a dialogue. Be sure to call
	 * forgetUser when the bot is done talking to the user. Analogous to leaving the
	 * window open; it probably has archiving implications.
	 */
	public void rememberUser(User user) {
		getOrCreateChat(user, true);
	}

	public void forgetUser(User user) {
		chats.remove(user);
	}

	private class SendMessageTask extends TimerTask {
		@Override
		public void run() {
			Message msg = messagesToSend.poll();
			if (msg == null) return;
		      	
			Chat chat = getOrCreateChat(msg.getTo(), false);
			try {
				chat.sendMessage(msg.getContent());
//				Logger.log(msg.getFrom() + " -> " + msg.getTo() + " : " 
//							+ msg.getContent());
			} catch (XMPPException e) {
				System.err.println(e);
			}
		}
	}
}
