package net.q00p.bots.partybot.marshal;

import java.io.Serializable;

import net.q00p.bots.User;
import net.q00p.bots.partybot.Subscriber;

public class SubscriberBean implements Serializable {
	String userName;
	String botScreenName;
	String alias = null;
	
	public String getUserName() { return userName; }
	public String getBotScreenName() { return botScreenName; }
	public String getAlias() { return alias; }
	public void setUserName(String in) { userName = in; }
	public void setBotScreenName(String in) { botScreenName = in; }
	public void setAlias(String in) { alias = in; }
	
	public Subscriber loadSubscriber() {
		Subscriber sub = Subscriber.get(User.get(userName), botScreenName);
		sub.setAlias(alias);
		return sub;
	}
	
	public SubscriberBean(Subscriber sub) {
		userName = sub.getUser().toString();
		botScreenName = sub.getBotScreenName();
		alias = sub.getAlias();
	}
	
	public SubscriberBean() {};
}
