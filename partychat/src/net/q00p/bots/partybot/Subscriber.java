package net.q00p.bots.partybot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.q00p.bots.User;
import net.q00p.bots.util.Tuple;

/**
 * Captures a relationship between a user and a bot screen name.
 * 
 * @author ak
 */
public class Subscriber implements Serializable {
	private User user;
	private String botScreenName;
	private String alias = null;
	
	private static Map<Tuple<User,String>,Subscriber> cache = 
		new HashMap<Tuple<User,String>,Subscriber>();
	private static String ALIAS_TEMPLATE = "\"%s\"";

	public static Subscriber get(User user, String botScreenName) {
		Tuple<User,String> pair = new Tuple<User,String>(user, botScreenName);
		Subscriber sub = cache.get(pair);
		if (sub != null) return sub;
		sub = new Subscriber(user, botScreenName);
		cache.put(pair, sub);
		return sub;
	}
	
	public static void forget(Subscriber sub) {
		cache.remove(new Tuple<User,String>(sub.user, sub.botScreenName));
	}
	
	private Subscriber(User user, String sn) {
		this.user = user;
		this.botScreenName = sn;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getBotScreenName() {
		return botScreenName;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String a) {
		alias = a;
	}
	
	public String getDisplayName() {
		if (alias != null) return String.format(ALIAS_TEMPLATE, alias);
		else return user.getName();
	}
		
	public String toString() {
		return user + "|" + botScreenName + " \"" + alias + "\"";
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Subscriber) {
			Subscriber sub = (Subscriber)obj;
			return sub.getUser().equals(user) 
					&& sub.getBotScreenName().equals(botScreenName);
		}
		return false;
	}
	
	public int hashCode() {
		return user.hashCode() ^ botScreenName.hashCode();
	}
}
