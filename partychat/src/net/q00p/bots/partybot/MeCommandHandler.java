package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class MeCommandHandler implements CommandHandler {

  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    PartyLine partyLine = lineManager.getPartyLine(subscriber);
    if (partyLine == null) return LineManager.NOT_IN;
    String actionCast = "_" + subscriber.getDisplayName(true) + " "
        + commandMatcher.group(2) + "_";
    partyBot.broadcast(subscriber, partyLine, actionCast, true);
    return actionCast;
  }

}
