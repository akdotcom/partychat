package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class ListCommandHandler extends PartyLineCommandHandler {

  public String doCommand(
      PartyBot partyBot,
      PartyLine partyLine,
      Subscriber subscriber,
      Matcher commandMatcher) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("#" + partyLine.getName() + " members:\n");

    for (Subscriber sub : partyLine.getSubscribers()) {
      sb.append(sub.getUser().getName());

      if (sub.getAlias() != null) {
        sb.append(" (")
          .append(sub.getAlias())
          .append(")");
      }
      
      if (sub.isSnoozing()) {
        sb.append(" snoozing for ")
          .append(PartyBot.timeTill(sub.getSnoozeUntil()));
      }
      
      sb.append(" using ")
        .append(sub.getBotScreenName().toLowerCase());

      if (sub.getLastActivityTime() > 0) {
        sb.append(" last seen ")
          .append(PartyBot.timeSince(sub.getLastActivityTime()))
          .append(" ago");
      }
      
      sb.append("\n");
    }
    
    return sb.toString();
  }
}
