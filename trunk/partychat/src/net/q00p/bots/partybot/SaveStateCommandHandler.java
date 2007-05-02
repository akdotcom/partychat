package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class SaveStateCommandHandler implements CommandHandler {

  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    return partyBot.saveState(subscriber);
  }

}
