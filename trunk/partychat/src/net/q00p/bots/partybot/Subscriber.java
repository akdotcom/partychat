package net.q00p.bots.partybot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.q00p.bots.Message;
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
  private long lastActivityTime;
  private long lineJoinTime;
  private long snoozeUntil;
  
  private static final int MAX_HISTORY = 10;
  private SubscriberHistory history = new SubscriberHistory(MAX_HISTORY);
  
  private static Map<Tuple<User,String>,Subscriber> cache = 
    new HashMap<Tuple<User,String>,Subscriber>();
  private static final String ALIAS_TEMPLATE = "\"%s\"";

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
    this.lastActivityTime = 0;
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
  
  public void setLastActivityTime(long ms) {
    lastActivityTime = ms;
  }
  
  public long getLastActivityTime() {
    return lastActivityTime;
  }
  
  public void setLineJoinTime(long ms) {
    lineJoinTime = ms;
  }
  
  public long getLineJoinTime() {
    return lineJoinTime;
  }
  
  public void setSnoozeUntil(long ms) {
    snoozeUntil = ms;
  }
  
  public long getSnoozeUntil() {
    return snoozeUntil;
  }
  
  public boolean isSnoozing() {
    return System.currentTimeMillis() < snoozeUntil;
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

  /**
   * Pushes the message to the subscriber history.
   * 
   * @param content
   */
  public synchronized void addMessageToHistory(Message message) {
    history.addMessage(message);
  }
  
  /**
   * Returns the history of the user. 
   * Note this returns a copy of the history to dodge any synch issues.
   * 
   * @return
   */
  public List<SubscriberHistory.HistoryItem> getHistoryItems() {
    return new ArrayList<SubscriberHistory.HistoryItem>(history.getItems());
  }
  
  public SubscriberHistory getHistory() {
    return history;
  }
  
  public void resetHistory(int totalWordCount, int totalMessageCount) {
    history = new SubscriberHistory(MAX_HISTORY, totalWordCount, totalMessageCount);
  }

}
