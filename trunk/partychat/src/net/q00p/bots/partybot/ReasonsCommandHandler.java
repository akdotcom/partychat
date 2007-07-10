package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class ReasonsCommandHandler extends PartyLineCommandHandler {

  public String doCommand(PartyBot partyBot, PartyLine partyLine,
      Subscriber subscriber, Matcher commandMatcher) {
    String regex = commandMatcher.group(2);
    return partyBot.printScore(partyLine.getName(), regex, true);
  }

}
