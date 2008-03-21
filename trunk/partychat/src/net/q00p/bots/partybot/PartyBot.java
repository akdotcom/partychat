package net.q00p.bots.partybot;

import net.q00p.bots.Bot;
import net.q00p.bots.Message;
import net.q00p.bots.User;
import net.q00p.bots.io.Logger;
import net.q00p.bots.partybot.marshal.PartyLineBean;
import net.q00p.bots.partybot.marshal.SubscriberBean;
import net.q00p.bots.util.AbstractBot;
import net.q00p.bots.util.DateUtil;
import net.q00p.bots.util.FutureTask;

import com.google.common.collect.Sets;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class PartyBot extends AbstractBot {
  private final LineManager lineManager;
  private final Timer timer;
  private final StateSaver sh;
  private final PlusPlusBot plusPlusBot;

  private static final Pattern PLUSPLUS_RX = Pattern
      .compile("(\\S+)(\\+\\+|\\-\\-)\\W*(\\w*.*)");

  private static final Pattern SR_RX = Pattern
      .compile("(\\S+)(\\+\\+|--)($|\\s+(.*))");

  static final String NO_SUBSCRIBER = "No such alias or name: %s";
  static final String USER_NO_LONGER_SNOOZING = "%s is no longer snoozing";
  static final String MESSAGE_FORMAT = "[%s] %s";
  static final String IGNORE_MESSAGE_FORMAT = "[%s to all but %s] %s";

  static final String UKNOWN_COMMAND = "'%s' is not a recognized "
      + "command, type '" + Command.COMMANDS.getShortName()
      + "' for a list of possible commands " + "and their uses.";
  static final String NEED_HELP = "you are not in a party chat, for help "
      + "using PartyChat, type '" + Command.HELP.getShortName() + "'";
  static final String SUB_STATUS_ONLINE = "you are currently in party chat #%s"
      + " as %s";
  static final String SUB_STATUS_OFFLINE = "you are not in a party chat";

  static final String SR_OUTPUT = "%s meant _%s_";

  // TODO(ak): load administrators from a config file  
  private static final Set<String> ADMINISTRATORS = Sets.immutableSet(
    "apatil@gmail.com",
    "mbolin@gmail.com",
    "ak@q00p.net",
    "mihai.parparita@gmail.com"
  );

  private final FutureTask futureTask = new FutureTask();

  private PartyBot(String name) {
    super(name);
    lineManager = loadState();

    plusPlusBot = new PlusPlusBot();

    sh = new StateSaver(lineManager);
    Runtime.getRuntime().addShutdownHook(new Thread(sh));

    timer = new Timer();
    timer.scheduleAtFixedRate(sh, new Date(), 15 * 60 * 1000); // every 15
    // minutes.
  }

  public void handleMessage(Message message) {
    Subscriber subscriber = Subscriber.get(message.getFrom(), message.getTo()
        .getName());

    String output = null;
    PartyLine partyLine = lineManager.getPartyLine(subscriber);

    boolean isCommand = false;

    String content = message.getPlainContent();
    Command command = Command.isCommand(content);
    Matcher plusPlusMatcher = PLUSPLUS_RX.matcher(content);
    Matcher replaceMatcher = SR_RX.matcher(content);
    if (command != null) {
      CommandHandler handler = command.handler;
      Matcher commandMatcher = command.pattern.matcher(content);
      // need to run matches() before CommandHandler can access groups()
      boolean doesMatch = commandMatcher.matches();
      assert doesMatch;
      output = handler.doCommand(this, lineManager, subscriber, commandMatcher);
      isCommand = true;
    } else if (plusPlusMatcher.find()) {
      String target = plusPlusMatcher.group(1);
      String delta = plusPlusMatcher.group(2);
      String reason = plusPlusMatcher.group(4);

      Message plusPlusResponse = null;

      if ("++".equals(delta)) {
        plusPlusResponse = plusPlusBot.increment(message, partyLine.getName(),
            target, reason);
      } else {
        plusPlusResponse = plusPlusBot.decrement(message, partyLine.getName(),
            target, reason);
      }

      // broadcast whatever they just said
      broadcast(subscriber, partyLine, message);

      if (plusPlusResponse != null) {
        // TODO(dolapo): share this. ak is rushing meee! who knows if this
        // works?
        User plusPlus = User.get("plusplusbot", "bot");
        Subscriber botSubscriber = Subscriber.get(plusPlus, "");
        output = broadcast(botSubscriber, partyLine, plusPlusResponse
            .getContent(), true);
      }

    } else if (replaceMatcher.find()) {
      broadcast(subscriber, partyLine, message);
      String replAnnouncement = attemptSearchReplace(subscriber, replaceMatcher
          .group(1), replaceMatcher.group(2), content.endsWith("g"));
      if (replAnnouncement != null) {
        announce(partyLine, replAnnouncement);
      } else {
        // This may not be the right thing to do here.
        output = "Malformed search replace. Try s/old/new/";
      }
    } else {
      // must be broadcasting
      if (partyLine != null) {
        output = broadcast(subscriber, partyLine, message);
        subscriber.setLastActivityTime(System.currentTimeMillis());
        subscriber.addMessageToHistory(message);
      } else {
        output = NEED_HELP;
      }
    }

    // If it wasn't a command and they were snoozing, bring em back.
    if (subscriber.isSnoozing() && !isCommand && partyLine != null) {
      subscriber.setSnoozeUntil(0);
      announce(partyLine, String.format(USER_NO_LONGER_SNOOZING, subscriber
          .getUser().getName()));
    }

    if (output != null) getMessageSender().sendMessage(message.reply(output));
  }

  /**
   * Looks back 2 items in the user's history for any message containing
   * 'search'. If found replaces it with 'replace' and returns the new message.
   */
  private String attemptSearchReplace(Subscriber subscriber, String search,
      String replace, boolean global) {
    if (subscriber == null) {
      return null;
    }

    Pattern searchPattern;

    try {
      searchPattern = Pattern.compile(search);
    } catch (PatternSyntaxException e) {
      // Don't let the user put in crappy patterns.
      // TODO(dolapo): Also, we should probably let them know they did something
      // wrong.
      return null;
    }

    List<SubscriberHistory.HistoryItem> history = subscriber.getHistoryItems();
    int historySize = history.size();

    String intent = null;
    // Loop through the history backwards, since presumably the user wants to
    // correct a more recent message
    for (int i = historySize - 1; i >= 0 && i > historySize - 3; --i) {
      String original = history.get(i).getMessage().getContent();
      Matcher searchMatcher = searchPattern.matcher(original);
      if (searchMatcher.find()) {
        // Handle /g correctly.
        if (global) {
          intent = searchMatcher.replaceAll(replace);
        } else {
          intent = searchMatcher.replaceFirst(replace);
        }
        
        // Stop looking for a match now that we've found one
        break;
      }
    }
    if (intent == null) {
      return null;
    }

    return String.format(SR_OUTPUT, subscriber.getDisplayName(), intent);
  }

  FutureTask getFutureTask() {
    return futureTask;
  }

  /**
   * Finds a subscriber with the given alias or name.
   * 
   * @param partyLine
   * @param aliasOrName
   * @return The Subscriber or null.
   */
  Subscriber findSubscriber(PartyLine partyLine, String aliasOrName) {
    for (Subscriber sub : partyLine.getSubscribers()) {
      if (aliasOrName.equals(sub.getAlias())
          || aliasOrName.equals(sub.getUser().getName())) {
        return sub;
      }
    }

    return null;
  }

  /**
   * subscriber - can be null.
   * 
   * @return message you want sent back to the broadcaster/subscriber
   */
  String broadcast(Subscriber subscriber, PartyLine partyLine, String content,
      boolean isSystem) {
    if (!isSystem)
      content = String.format(MESSAGE_FORMAT, subscriber.getDisplayName(),
          content);

    for (Subscriber listener : partyLine.getSubscribers()) {
      if (!listener.equals(subscriber)) {
        if (listener.isSnoozing()) {
          // TODO(dolapo) - Actually save the messages. Post sqlite thing.
          // listener.addDelayedMessage(content);
          continue;
        }
        Message msg = new Message(User.get(listener.getBotScreenName(),
            botName()), listener.getUser(), content);
        getMessageSender().sendMessage(msg);
      }
    }

    return null;
  }

  String broadcast(Subscriber subscriber, PartyLine partyLine, Message message) {
    return broadcast(subscriber, partyLine, message.getContent(), false);
  }

  /**
   * Sends a message from the system.
   * 
   * @param partyLine
   * @param message
   * @return
   */
  String announce(PartyLine partyLine, String message) {
    if (partyLine == null) {
      // Do nothing if there's somehow no partyline
      return null;
    }
    return broadcast(null, partyLine, message, true);
  }

  String getStatus(Subscriber sub) {
    PartyLine partyLine = lineManager.getPartyLine(sub);
    if (partyLine == null) {
      return SUB_STATUS_OFFLINE;
    } else {
      return String.format(SUB_STATUS_ONLINE, partyLine.getName(), sub
          .getDisplayName());
    }
  }

  /** Prints how long till the given time */
  static String timeTill(long futureTimeMs) {
    return DateUtil.prettyFormatTime(futureTimeMs - System.currentTimeMillis());
  }

  static String timeSince(long pastTimeMs) {
    return DateUtil.prettyFormatTime(System.currentTimeMillis() - pastTimeMs);
  }

  String printScore(String chat, String regex, boolean showReasons) {
    return plusPlusBot.getScores(chat, regex, showReasons);
  }

  String saveState(Subscriber subscriber) {
    String user = subscriber.getUser().getName();
    if (ADMINISTRATORS.contains(user)) {
      sh.run();
      return "state saved";
    } else {
      return "authorized personnel only";
    }
  }

  public static void main(String[] args) {
    List<String> argList = Arrays.asList(args);
    assert argList.size() > 2 : "usage: java PartyBot botName usn pwd usn pwd ...";
    Bot bot = new PartyBot(argList.get(0));
    Logger.log(String.format("Running %s with parameters: %s", bot.getClass(),
        Arrays.toString(args)), true);
    run(bot, argList.subList(1, argList.size()));
  }

  //TODO: This is lame. We should have some sort of backend
  class StateSaver extends TimerTask {
    LineManager manager;

    public StateSaver(LineManager managedClass) {
      super();
      this.manager = managedClass;
    }

    public void run() {
      Logger.log("saving state...");
      String xmlfilename = "state.xml";
      try {
        Collection<PartyLine> blah = lineManager.linesByName.values();
        Collection<PartyLineBean> blah2 = new HashSet<PartyLineBean>();
        for (PartyLine pl : blah) {
          blah2.add(new PartyLineBean(pl));
        }
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
            new FileOutputStream(xmlfilename)));
        e.writeObject(blah2);
        e.close();
        Logger.log("State successfully stored to " + xmlfilename);
      } catch (Exception e) {
        Logger.log("Error trying to save state: " + e.getMessage());
      }

    }
  }

  @SuppressWarnings("unchecked")
  private LineManager loadState() {
    Collection<PartyLineBean> result = null;
    try {
      XMLDecoder d = new XMLDecoder(new BufferedInputStream(
          new FileInputStream("state.xml")));
      result = (Collection<PartyLineBean>) d.readObject();
      d.close();
    } catch (FileNotFoundException e) {
      Logger.log("No preexisting state found to load.", true);
    }
    LineManager lm = new LineManager();
    if (result == null) return lm;
    for (PartyLineBean plb : result) {
      lm.startLine(plb.getName(), plb.getPassword());
      for (SubscriberBean sb : plb.getSubscribers()) {
        lm.subscribe(sb.loadSubscriber(), plb.getName(), plb.getPassword());
      }
    }
    return lm;
  }
}
