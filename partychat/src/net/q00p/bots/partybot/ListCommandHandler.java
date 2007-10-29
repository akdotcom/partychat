package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class ListCommandHandler implements CommandHandler {

  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    PartyLine partyLine = lineManager.getPartyLine(subscriber);
    if (partyLine == null)
      return LineManager.NOT_IN;
    else
      return formatSubscriberList(partyLine);
  }

  private String formatSubscriberList(PartyLine partyLine) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("#" + partyLine.getName() + " members:\n");
    for (Subscriber sub : partyLine.getSubscribers()) {
      sb.append(sub.getUser().getName());

      if (sub.getAlias() != null) sb.append(" (" + sub.getAlias() + ")");
      
      if (sub.isSnoozing()) {
        sb.append(" snoozing for " + PartyBot.timeTill(sub.getSnoozeUntil()));
      }
      
      sb.append(" ");
      sb.append(sub.getUser().getClientInfo());
      
      sb.append("\n");
    }
    
    return sb.toString();
  }
}
