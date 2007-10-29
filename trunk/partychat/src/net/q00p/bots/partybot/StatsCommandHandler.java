package net.q00p.bots.partybot;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.regex.Matcher;

/**
 * TODO(mihaip)
 *
 * @author <a href="mailto:mihai.parparita@gmail.com">Mihai Parparita</a>
 */
public class StatsCommandHandler implements CommandHandler {

  private static final long DAY_IN_MS = 24 * 60 * 60 * 1000L;

  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    StringBuilder sb = new StringBuilder();
    
    long now = System.currentTimeMillis();
    long oneDayCutoff = now - DAY_IN_MS;
    long sevenDayCutoff = now - 7 * DAY_IN_MS;
    long thirtyDayCutoff = now - 30 * DAY_IN_MS;    
    
    // Total number of rooms
    sb.append("Party chats:\n");
    
    sb.append("  Total: ")
      .append(lineManager.linesByName.size())
      .append("\n");
    
    // Subscriber and room stats
    int maxSubscribers = 0;
    double totalSubscribers = 0;
    Set<String> oneDayActiveLines = Sets.newHashSet();
    Set<String> sevenDayActiveLines = Sets.newHashSet();
    Set<String> thirtyDayActiveLines = Sets.newHashSet();
    int oneDayActiveSubscribers = 0;
    int sevenDayActiveSubscribers = 0;
    int thirtyDayActiveSubscribers = 0;
        
    
    for (PartyLine line : lineManager.linesByName.values()) {
      int subscribers = line.getSubscribers().size();
      
      totalSubscribers += subscribers;
      if (subscribers > maxSubscribers) {
        maxSubscribers = subscribers;
      }
      
      for (Subscriber s : line.getSubscribers()) {
        long lastSeen = s.getLastActivityTime();
        
        if (lastSeen >= oneDayCutoff) {
          oneDayActiveSubscribers++;
          oneDayActiveLines.add(line.getName());
        }
        if (lastSeen >= sevenDayCutoff) {
          sevenDayActiveSubscribers++;
          sevenDayActiveLines.add(line.getName());
        }
        if (lastSeen >= thirtyDayCutoff) {
          thirtyDayActiveSubscribers++;
          thirtyDayActiveLines.add(line.getName());
        }
      }      
    }
    double averageSubscribers = totalSubscribers/lineManager.linesByName.size();
    sb.append("  Active in the past day: ")
      .append(oneDayActiveLines.size())
      .append("\n");
    sb.append("  Active in the past week: ")
      .append(sevenDayActiveLines.size())
      .append("\n");
    sb.append("  Active in the past 30 days: ")
      .append(thirtyDayActiveLines.size())
      .append("\n");    
    sb.append("  Average subscribers: ")
      .append(String.format("%.3g", averageSubscribers))
      .append("\n");
    sb.append("  Most subscribers: ")
      .append(maxSubscribers)
      .append("\n");
    
    // Activity stats

    sb.append("\nSubscribers:\n");
    sb.append("  Total: ")
      .append((int)totalSubscribers)
      .append("\n");
    sb.append("  Active in the past day: ")
      .append(oneDayActiveSubscribers)
      .append("\n");
    sb.append("  Active in the past week: ")
      .append(sevenDayActiveSubscribers)
      .append("\n");
    sb.append("  Active in the past 30 days: ")
      .append(thirtyDayActiveSubscribers)
      .append("\n");
    
    // Bot stats
    Multiset<String> botCounts = Multisets.newTreeMultiset();
    for (PartyLine line : lineManager.linesByName.values()) {
      for (Subscriber s : line.getSubscribers()) {
        botCounts.add(s.getBotScreenName().toLowerCase());
      }
    }
    sb.append("\nBot stats:\n");
    for (String botScreenName : botCounts.elementSet()) {
      sb.append("  ")
        .append(botScreenName)
        .append(": ")
        .append(botCounts.count(botScreenName))
        .append(" subscribers")
        .append("\n");
    }
    
    return sb.toString();
  }

}
