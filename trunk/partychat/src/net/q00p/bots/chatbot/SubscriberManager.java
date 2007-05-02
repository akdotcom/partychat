package net.q00p.bots.chatbot;

import net.q00p.bots.User;
import net.q00p.bots.util.Tuple;

import java.util.HashMap;
import java.util.Map;

/**
 * Has a map of subscribers, keyed on on the user and bot scren name.
 * We really need a database backing.
 * 
 * @author ak
 */
public class SubscriberManager {
	public Map<Tuple<User,String>,Subscriber> lookupMap = 
		new HashMap<Tuple<User,String>,Subscriber>();

	/**
	 * @return Returns the lookupMap.
	 */
	public Map<Tuple<User, String>, Subscriber> getLookupMap() {
		return lookupMap;
	}
	
	
}
