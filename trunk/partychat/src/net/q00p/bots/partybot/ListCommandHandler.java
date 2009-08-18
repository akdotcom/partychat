package net.q00p.bots.partybot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.regex.Matcher;

public class ListCommandHandler extends PartyLineCommandHandler {

  @Override
  public String doCommand(
      PartyBot partyBot,
      PartyLine partyLine,
      Subscriber subscriber,
      Matcher commandMatcher) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("#" + partyLine.getName() + " members:\n");
    
    Multimap<String, Subscriber> subscribersByName = HashMultimap.create();
    
    for (Subscriber sub : partyLine.getSubscribers()) {
      subscribersByName.put(sub.getUser().getName(), sub);
    }
    
    for (String name : subscribersByName.keySet()) {
      sb.append(name).append(":\n");
      
      for (Subscriber sub : subscribersByName.get(name)) {
        sb.append("  using ")
          .append(sub.getBotScreenName().toLowerCase());

        if (sub.getAlias() != null) {
          sb.append(" as ").append(sub.getAlias());
        }

        if (sub.isSnoozing()) {
          sb.append(" snoozing for ")
            .append(PartyBot.timeTill(sub.getSnoozeUntil()));
        }


        if (sub.getLastActivityTime() > 0) {
          sb.append(" last seen ")
            .append(PartyBot.timeSince(sub.getLastActivityTime()))
            .append(" ago");
        }
        
        sb.append("\n");
      }
    }
    
    return sb.toString();
  }
}
