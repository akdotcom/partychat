package net.q00p.bots.partybot;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.q00p.bots.Bot;
import net.q00p.bots.Message;
import net.q00p.bots.User;
import net.q00p.bots.io.Logger;
import net.q00p.bots.partybot.marshal.PartyLineBean;
import net.q00p.bots.partybot.marshal.SubscriberBean;
import net.q00p.bots.util.AbstractBot;


public class PartyBot extends AbstractBot {
  private final LineManager lineManager;
  private final Timer timer;
  private final StateSaver sh;
    private final PlusPlusBot plusPlusBot;

  public static String JOIN = "/enter";
  public static String STATUS = "/status";
  public static String LIST = "/list";
  public static String ALIAS = "/alias";
  public static String SCORE = "/score";
  public static String EXIT = "/exit";
  public static String HELP = "/help";
  public static String CREATE = "/make";
  public static String COMMANDS = "/commands";
  
  static final String MESSAGE_FORMAT = "[%s] %s";
  
  static final String HELP_PROMPT = "to enter a party chat, type " +
      "'%s chat_name [password]' (password may not be required)\n" +
      "to exit a party chat, type '%s'\nfor a list of" +
      " PartyChat commands, type '%s'";
  static final String UKNOWN_COMMAND = "'%s' is not a recognized " +
      "command, type '" + COMMANDS + "' for a list of possible commands " +
          "and their uses.";
  static final String NEED_HELP = "you are not in a party chat, for help "+
      "using PartyChat, type '" + PartyBot.HELP + "'";
  static final String SUB_STATUS_ONLINE = "you are currently in party chat #%s" +
      " as %s";
  static final String SUB_STATUS_OFFLINE = "you are not in a party chat";
  static final String ALIAS_SET = "alias set to %s";
  static final String ALIAS_REMOVED = "alias removed";
  static final String ALIAS_TAKEN = "alias %s is already taken, " +
      "try a different alias";
  static final String SUB_JOINED = "%s joined the chat";
  static final String SUB_LEFT = "%s left the chat";
  static final String SUB_ALIAS_CHANGE = "%s is now known as %s";
  static final String SUB_ALIAS_CHANGE_HAD_PREVIOUS = 
	  "%s (%s) is now known as %s";
  static final String NO_SUBSCRIBER = "No such alias or name: %s";
  static final String SR_OUTPUT = "%s meant _%s_";
  
  private static final Pattern CREATE_RX = 
    Pattern.compile("(make|create|start)\\s+#?(\\S*)\\s*(\\S*)", 
        Pattern.CASE_INSENSITIVE);
  private static final Pattern SUBSCRIBE_RX = 
    Pattern.compile("(sub|subscribe|join|enter)\\s+#?(\\S*)\\s*(\\S*)", 
        Pattern.CASE_INSENSITIVE);
  private static final Pattern STATUS_RX = 
    Pattern.compile("(status)(\\s+\\S+)*");
  private static final Pattern SCORE_RX =
       Pattern.compile("(score)\\s+(.*)");
  private static final Pattern ME_RX =
    Pattern.compile("(me)\\s+(.*)");
  private static final Pattern LIST_RX = 
    Pattern.compile("(list|members)(\\s+\\S+)*");
  private static final Pattern ALIAS_RX = 
    Pattern.compile("(alias|aka)\\s*(\\S*)");
  private static final Pattern WHOIS_RX =
    Pattern.compile("(whois|describe)\\s+(\\S*)");
  private static final Pattern UNSUBSCRIBE_RX = 
    Pattern.compile("(unsub|unsubscribe|leave|exit)\\s*#?(\\S*)", 
        Pattern.CASE_INSENSITIVE);
  private static final Pattern HELP_RX = 
    Pattern.compile("(\\?|help)(\\s+\\S+)*", Pattern.CASE_INSENSITIVE);
  private static final Pattern COMMANDS_RX = 
    Pattern.compile("(commands|actions|instructions)(\\s+\\S+)*", 
        Pattern.CASE_INSENSITIVE);
  private static final Pattern SAVE_RX = 
    Pattern.compile("(save-state)(\\s+\\S+)*", 
        Pattern.CASE_INSENSITIVE);

  private static final Pattern COMMAND_RX = 
    Pattern.compile("/(.*)");
  private static final Pattern PLUSPLUS_RX =
    Pattern.compile("(\\S+)(\\+\\+|\\-\\-)\\W*(\\w*.*)");
  
  private static final Pattern SR_RX =
    Pattern.compile("^s/([^/]+)/([^/]*)/(g?)$");


  private PartyBot(String name) {
    super(name);
    lineManager = loadState();
        
    plusPlusBot = new PlusPlusBot();

    sh = new StateSaver(lineManager);
    Runtime.getRuntime().addShutdownHook(new Thread(sh));
    
    timer = new Timer();
    timer.schedule(sh, new Date(), 15*60*1000); // every 15 minutes.
  }
  
  public void handleMessage(Message message) {
    Subscriber subscriber = 
      Subscriber.get(message.getFrom(), message.getTo().getName());
    
    String output = null;
    PartyLine partyLine = lineManager.getPartyLine(subscriber);

    String content = message.getPlainContent();
    Matcher commandMatcher = COMMAND_RX.matcher(content);
    Matcher plusPlusMatcher = PLUSPLUS_RX.matcher(content);
    Matcher replaceMatcher = SR_RX.matcher(content);
    if (commandMatcher.matches()) {
      output = doCommand(subscriber, commandMatcher.group(1));
    } else if (plusPlusMatcher.find()) {
      String target = plusPlusMatcher.group(1);
      String delta = plusPlusMatcher.group(2);
      String reason = plusPlusMatcher.group(3);
      
      Message plusPlusResponse = null;
      
      if ("++".equals(delta)) {
        plusPlusResponse = plusPlusBot.increment(
            message, partyLine.getName(), target, reason);
      } else {
        plusPlusResponse = plusPlusBot.decrement(
            message, partyLine.getName(), target, reason);
      }
      
      // broadcast whatever they just said
      broadcast(subscriber, partyLine, message);
      
      if (plusPlusResponse != null) {
        // TODO(dolapo): share this. ak is rushing meee! who knows if this works?
        User plusPlus = User.get("plusplusbot", "bot");
        Subscriber botSubscriber = Subscriber.get(plusPlus, "");
        output =
          broadcast(botSubscriber, partyLine, plusPlusResponse.getContent(), true);
      }
      
    } else if (replaceMatcher.find()) {
      broadcast(subscriber, partyLine, message);
      String replAnnouncement = attemptSearchReplace(
          subscriber, replaceMatcher.group(1), replaceMatcher.group(2),
          content.endsWith("g"));
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
    
    if (output != null) 
      getMessageSender().sendMessage(message.reply(output));
  }
  
  /**
   * Looks back 2 items in the user's history for any message containing 'search'.
   * If found replaces it with 'replace' and returns the new message.
   */
  private String attemptSearchReplace(Subscriber subscriber, 
                                      String search, String replace, boolean global) {
    if (subscriber == null) {
      return null;
    }
    
    Pattern searchPattern;
    
    try {
      searchPattern = Pattern.compile(search);
    } catch (PatternSyntaxException e) {
      // Don't let the user put in crappy patterns. 
      // TODO(dolapo): Also, we should probably let them know they did something wrong.
      return null;
    }
    
    List<SubscriberHistory.HistoryItem> history = subscriber.getHistoryItems();
    int historySize = history.size();
    
    String intent = null;
    for (int i = historySize - 1; i >= 0 && i > historySize - 3; --i) {
      Matcher searchMatcher = searchPattern.matcher(
          history.get(i).getMessage().getContent());
      if (searchMatcher.find()) {
        // Handle /g correctly.
        if (global) {
          intent = searchMatcher.replaceAll(replace);
        } else {
          intent = searchMatcher.replaceFirst(replace);
        }
      }
    }
    if (intent == null) {
      return null;
    }
        
    return String.format(SR_OUTPUT, subscriber.getDisplayName(), intent);
  }
  
  private String doCommand(Subscriber subscriber, String command) {

    Matcher matcher = HELP_RX.matcher(command);
    if (matcher.matches()) {
      return help();
    }
    
    matcher = CREATE_RX.matcher(command);
    if (matcher.matches()) {
      String lineName = matcher.group(2);
      String pwd = matcher.group(3);

      String output = lineManager.startLine(lineName, pwd);
      return output + (lineManager.subscribe(subscriber, lineName, pwd));

    }
    
    matcher = SUBSCRIBE_RX.matcher(command);    
    if (matcher.matches()) {
      String announcement = String.format(
    		  SUB_JOINED, subscriber.getUser().getName());
      

      String userResponse = lineManager.subscribe(subscriber, matcher.group(2), 
                    matcher.group(3));
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      
      announce(partyLine, announcement);
      
      return userResponse;
    }

    matcher = STATUS_RX.matcher(command);   
    if (matcher.matches()) {
      return getStatus(subscriber);
    }

    matcher = LIST_RX.matcher(command);
    if (matcher.matches()) {
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      if (partyLine == null)
        return LineManager.NOT_IN;
      else
        return formatSubscriberList(partyLine);
    }
    
    matcher = ALIAS_RX.matcher(command);
    if (matcher.matches()) {
      // make sure they're in a party chat before aliasing them.
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      if (partyLine == null)
        return LineManager.NOT_IN;

      String alias = matcher.group(2);
      for (Subscriber sub : partyLine.getSubscribers()) {
        if (alias.equals(sub.getAlias()))
          return String.format(ALIAS_TAKEN, alias);
      }
      
      String aliasResponse;
      String oldAlias = subscriber.getAlias();
      if (alias.equals("")) {
        subscriber.setAlias(null);
        aliasResponse = ALIAS_REMOVED;
      } else {
        subscriber.setAlias(alias);
        aliasResponse = String.format(ALIAS_SET, alias);
      }
      
      String announcement;
      if (oldAlias == null) {
        announcement = String.format(
            SUB_ALIAS_CHANGE, subscriber.getUser().getName(), alias);
      } else {
        announcement = String.format(
            SUB_ALIAS_CHANGE_HAD_PREVIOUS, subscriber.getUser().getName(),
            oldAlias, alias);
      }

      announce(partyLine, announcement);
      
      return aliasResponse;
    }

    matcher = UNSUBSCRIBE_RX.matcher(command);
    if (matcher.matches()) {
      Subscriber.forget(subscriber);
      String announcement = String.format(
          SUB_LEFT, subscriber.getUser().getName());
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      
      if (partyLine != null) {
        announce(partyLine, announcement);
      }
      return lineManager.unsubscribe(subscriber, matcher.group(2));
    }
        
    matcher = COMMANDS_RX.matcher(command);
    if(matcher.matches()) {
      return getCommands();
    }
    
    matcher = SAVE_RX.matcher(command);
    if (matcher.matches()) {
      return saveState(subscriber);
    }
        
    matcher = SCORE_RX.matcher(command);
    if (matcher.matches()) {
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      if (partyLine == null)
          return LineManager.NOT_IN;
      return printScore(partyLine.getName(), matcher.group(2));
    }
    
    matcher = ME_RX.matcher(command);
    if (matcher.matches()) {
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      if (partyLine == null)
        return LineManager.NOT_IN;
      String actionCast = "_" + subscriber.getAlias() + " " + matcher.group(2) + "_";
      broadcast(subscriber, partyLine, actionCast, true);
      return actionCast;
    }
    
    matcher = WHOIS_RX.matcher(command);
    if (matcher.matches()) {
      // Hey wouldn't it be cool if we could separate commands based on what
      // they need - like being in an active line? nah, copy/paste is much more
      // fun.
      PartyLine partyLine = lineManager.getPartyLine(subscriber);
      if (partyLine == null)
        return LineManager.NOT_IN;
      
      // Find a subscriber with that alias or "name".
      String alias = matcher.group(2);
      for (Subscriber sub : partyLine.getSubscribers()) {
        if (alias.equals(sub.getAlias()) || alias.equals(sub.getUser().getName())) {
          return formatSubscriberInfo(sub);
        }
      }
      // Sorry, no such subscriber.
      return String.format(NO_SUBSCRIBER, alias);
    }
    
    return unknownCommand(command);
  }
  
  /** 
   * subscriber - can be null.
   * @return message you want sent back to the broadcaster/subscriber 
   * */
  String broadcast(Subscriber subscriber, PartyLine partyLine, 
      String content, boolean isSystem) {
    if (!isSystem)
      content = String.format(MESSAGE_FORMAT, 
                         subscriber.getDisplayName(),
                         content);
    
    for (Subscriber listener : partyLine.getSubscribers()) {
      if (! listener.equals(subscriber)) {
        Message msg = new Message(
            User.get(listener.getBotScreenName(), botName()), 
            listener.getUser(), content);
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

  private String getStatus(Subscriber sub) {
    PartyLine partyLine = lineManager.getPartyLine(sub);
    if (partyLine == null) {
      return SUB_STATUS_OFFLINE;
    } else {
      return String.format(SUB_STATUS_ONLINE, partyLine.getName(), 
                sub.getDisplayName());
    }
  }
  
  private String help() {
    return String.format(HELP_PROMPT, JOIN, EXIT, COMMANDS);
    
  }
  
  private String unknownCommand(String command) {
    return String.format(UKNOWN_COMMAND, command);
  }
  
  private String formatSubscriberList(PartyLine partyLine) {
    StringBuffer sb = new StringBuffer();
    sb.append("#"+partyLine.getName() + " members:\n");
    for (Subscriber sub : partyLine.getSubscribers()) {
      sb.append(sub.getUser().getName());

      if (sub.getAlias() != null)
        sb.append(" (" + sub.getAlias() + ")");
      sb.append("\n");
    }
    return sb.toString();
  }
  
  /**
   * Returns a message like:
   * joe@gmail.com (joe)
   * joined chat @ [time] 
   * last seen @ [time]
   * total messages [num]
   * total words [num]
   * 
   */
  private String formatSubscriberInfo(Subscriber subscriber) {
    StringBuffer sb = new StringBuffer();
    sb.append(subscriber.getUser().getName());
    if (subscriber.getAlias() != null) {
      sb.append(" (").append(subscriber.getAlias()).append(")");
    }
    
    DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss z");
    
    if (subscriber.getLineJoinTime() > 0) {
      sb.append("\nJoined chat @ ").append(df.format(
          new Date(subscriber.getLineJoinTime())));
    }
    
    if (subscriber.getLastActivityTime() > 0) {
      sb.append("\nLast seen @ ").append(df.format(
          new Date(subscriber.getLastActivityTime())));
    }
    
    if (subscriber.getHistory().getTotalMessageCount() > 0) {
      sb.append("\nTotal messages: ").append(subscriber.getHistory().getTotalMessageCount());
    }
    
    if (subscriber.getHistory().getTotalWordCount() > 0) {
      sb.append("\nApproximate words: ").append(subscriber.getHistory().getTotalWordCount());
    }
    return sb.toString();
  }
    
  private String printScore(String chat, String regex) {
    return plusPlusBot.getScores(chat, regex);
  }
  
  private String getCommands() {
    return String.format("PartyChat commands: \n\n" +
        "%s chat_name [optional_password] - creates a new party chat. "+
        "if you provide a password, then other users must give this " +
        "password to enter the chat.\n\n" +
        
        "%s chat_name [password] - join an existing party chat. if the" +
        "chat has a password, you must give the password to enter.\n\n" +
        
        "%s - display the party chat name and your alias.\n\n" +
        
        "%s - list the current members of the party chat you are in.\n\n" +
        
        "%s [name] - give yourself an alias; if you do not specify a name," +
        " your current alias is removed.\n\n" +
                
                 "%s [name] - return the score for a given name.\n\n" +

        "%s - leave the party chat you are currently in.\n\n" +
        
        "%s - offer some instructions on how to use PartyChat and" +
        "directs users to '" + COMMANDS + "'.\n\n" +
        
        "%s - displays this menu", 
                CREATE, JOIN, STATUS, LIST, ALIAS, SCORE, EXIT, HELP, COMMANDS);
  }
  
  private String saveState(Subscriber subscriber) {
    String user = subscriber.getUser().getName();
    if (user.equals("apatil@gmail.com") || user.equals("mbolin@gmail.com")) {
      sh.run();
      return "state saved";
    } else {
      return "authorized personnel only";
    }
  }
  
  public static void main(String[] args) {
    List<String> argList = Arrays.asList(args);
    assert argList.size() > 2 : 
      "usage: java PartyBot botName usn pwd usn pwd ...";
    Bot bot = new PartyBot(argList.get(0));
    Logger.log(String.format("Running %s with parameters: %s", bot.getClass(),
                Arrays.toString(args)), true);
    run(bot, argList.subList(1, argList.size()));
  }

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
        XMLEncoder e = new XMLEncoder(
                      new BufferedOutputStream(
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
      XMLDecoder d = new XMLDecoder(
                new BufferedInputStream(new FileInputStream("state.xml")));
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
