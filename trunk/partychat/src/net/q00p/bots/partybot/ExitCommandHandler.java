package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class ExitCommandHandler implements CommandHandler {

  private static final String SUB_LEFT = "%s left the chat";
  
  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    Subscriber.forget(subscriber);
    
    String name = subscriber.getUser().getName();
    
    if (subscriber.getAlias() != null) {
      name += " (" + subscriber.getAlias() + ")";
    }
    
    String announcement = String.format(SUB_LEFT, name);
    
    PartyLine partyLine = lineManager.getPartyLine(subscriber);

    if (partyLine != null) {
      partyBot.announce(partyLine, announcement);
    }
    
    return lineManager.unsubscribe(subscriber, commandMatcher.group(2));
  }

}
