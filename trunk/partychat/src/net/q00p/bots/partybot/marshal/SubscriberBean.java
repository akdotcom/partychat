package net.q00p.bots.partybot.marshal;

import java.io.Serializable;

import net.q00p.bots.User;
import net.q00p.bots.partybot.Subscriber;

public class SubscriberBean implements Serializable {
	String userName;
	String botScreenName;
	String alias = null;
  long lastActivityTime;
  long lineJoinTime;
  int totalWordCount;
  int totalMessageCount;
	
	public String getUserName() { return userName; }
	public String getBotScreenName() { return botScreenName; }
	public String getAlias() { return alias; }
  public long getLastActivityTime() { return lastActivityTime; }
  public long getLineJoinTime() { return lineJoinTime; }
  public int getTotalWordCount() { return totalWordCount; }
  public int getTotalMessageCount() { return totalMessageCount; }
	public void setUserName(String in) { userName = in; }
	public void setBotScreenName(String in) { botScreenName = in; }
	public void setAlias(String in) { alias = in; }
  public void setLastActivityTime(long t) { lastActivityTime = t; }
  public void setLineJoinTime(long t) { lineJoinTime = t; }
  public void setTotalWordCount(int i) { totalWordCount = i; }
  public void setTotalMessageCount(int i) { totalMessageCount = i; }
	
	public Subscriber loadSubscriber() {
		Subscriber sub = Subscriber.get(User.get(userName), botScreenName);
		sub.setAlias(alias);
    sub.setLastActivityTime(lastActivityTime);
    sub.setLineJoinTime(lineJoinTime);
    sub.resetHistory(totalWordCount, totalMessageCount);
		return sub;
	}
	
	public SubscriberBean(Subscriber sub) {
		userName = sub.getUser().toString();
		botScreenName = sub.getBotScreenName();
		alias = sub.getAlias();
    lastActivityTime = sub.getLastActivityTime();
    lineJoinTime = sub.getLineJoinTime();
    totalWordCount = sub.getHistory().getTotalWordCount();
    totalMessageCount = sub.getHistory().getTotalMessageCount();
	}
	
	public SubscriberBean() {};
}
