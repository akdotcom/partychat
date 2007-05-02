package net.q00p.bots.partybot;

import net.q00p.bots.Message;
import net.q00p.bots.User;

import java.util.regex.Matcher;

public class WhisperCommandHandler implements CommandHandler {

  private static final String PRIVATE_MESSAGE_FORMAT = "[%s to just you] %s";
  
  public String doCommand(PartyBot partyBot, LineManager lineManager,
      Subscriber subscriber, Matcher commandMatcher) {
    PartyLine partyLine = lineManager.getPartyLine(subscriber);
    if (partyLine == null) return LineManager.NOT_IN;

    String alias = commandMatcher.group(2);
    String msg = commandMatcher.group(3);
    String formattedMsg = String.format(PRIVATE_MESSAGE_FORMAT, subscriber
        .getDisplayName(), msg);

    // It makes it easier for testing to not check if they're whispering to
    // themself.
    Subscriber to = partyBot.findSubscriber(partyLine, alias);
    if (to == null) {
      return String.format(PartyBot.NO_SUBSCRIBER, alias);
    }

    Message whispermsg = new Message(User.get(to.getBotScreenName(),
        partyBot.botName()), to.getUser(), formattedMsg);
    partyBot.getMessageSender().sendMessage(whispermsg);

    return null;
  }

}
