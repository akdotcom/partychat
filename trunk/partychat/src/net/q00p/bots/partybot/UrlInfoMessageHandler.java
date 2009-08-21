package net.q00p.bots.partybot;

import com.google.common.collect.MapMaker;

import net.q00p.bots.Message;
import net.q00p.bots.partybot.commands.UrlInfoCommandHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * {@link MessageHandler} that looks for messages that contain HTTP URLs and
 * records the most recent one per party line (meant to be used in conjunction
 * with {@link UrlInfoCommandHandler}).
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class UrlInfoMessageHandler implements MessageHandler {
  private static final Map<String, URI> recentUrisInPartyLines =
      new MapMaker().makeMap();

  public static boolean isValidUri(URI uri) {
    return uri.isAbsolute()
        && "http".equals(uri.getScheme())
        && uri.getAuthority() != null
        && uri.getAuthority().length() > 0;    
  }
  
  public static URI getLastPartyLineUri(PartyLine partyLine) {
    return recentUrisInPartyLines.get(partyLine.getName());
  }
  
  public boolean canHandle(Message message) {
    try {
      URI uri = new URI(message.getPlainContent());
      return isValidUri(uri);
    } catch (URISyntaxException err) {
      return false;
    }
  }
  
  public void handle(
      Message message,
      Subscriber subscriber,
      PartyLine partyLine,
      MessageResponder responder) {
    if (partyLine == null) {
      return;
    }
    
    recentUrisInPartyLines.put(
        partyLine.getName(),
        URI.create(message.getPlainContent()));
  }

  public boolean shouldBroadcastOriginalMessage() {
    return true;
  }

}
