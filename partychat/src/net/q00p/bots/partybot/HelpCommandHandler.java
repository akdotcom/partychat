package net.q00p.bots.partybot;

import java.util.regex.Matcher;

public class HelpCommandHandler implements CommandHandler {

  private static final String HELP_PROMPT = "To enter a party chat, type "
      + "'%s chat_name [password]' (password may not be required).\n"
      + "To exit a party chat, type '%s'.\n"
      + "For a list of PartyChat commands, type '%s'";

  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    return String.format(HELP_PROMPT, Command.JOIN.getShortName(),
        Command.EXIT.getShortName(), Command.HELP.getShortName());
  }

}
