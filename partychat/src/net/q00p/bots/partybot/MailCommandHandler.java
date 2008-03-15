package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class MailCommandHandler extends PartyLineCommandHandler {

  public String doCommand(
      PartyBot partyBot,
      PartyLine partyLine,
      Subscriber subscriber,
      Matcher commandMatcher) {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    
    for (Subscriber sub : partyLine.getSubscribers()) {
      if (!isFirst) {
        sb.append(", ");
      } else {
        isFirst = false;
      }

      sb.append("<");
      sb.append(sub.getUser().getName());
      sb.append(">");
    }
    
    return sb.toString();
  }
  
}
