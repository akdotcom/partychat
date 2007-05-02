package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class ReasonsCommandHandler implements CommandHandler {

  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    PartyLine partyLine = lineManager.getPartyLine(subscriber);
    if (partyLine == null) return LineManager.NOT_IN;
    String regex = commandMatcher.group(2);
    return partyBot.printScore(partyLine.getName(), regex, true);
  }

}
