package net.q00p.bots.partybot;

import java.util.regex.Pattern;

/**
 * All /-prefixed commands that PartyChat understands.
 * Each Command has a regexp to match the command,
 * some documentation, and a CommandHandler to respond
 * when the command is issued.
 * Adding a new command to PartyChat is as simple as
 * adding a new Command enum.
 * @author bolinfest@gmail.com
 */
public enum Command {

  CREATE(Pattern.compile("/(make|create|start)\\s+#?(\\S*)\\s*(\\S*)",
      Pattern.CASE_INSENSITIVE), "/create chat_name [optional_password]"
      + " - creates a new party chat. "
      + "If you provide a password, then other users must give this "
      + "password to enter the chat.", new CreateCommandHandler()),

  JOIN(Pattern.compile("/(sub|subscribe|join|enter)\\s+#?(\\S*)\\s*(\\S*)",
      Pattern.CASE_INSENSITIVE), "/join chat_name [password]"
      + " - join an existing party chat. If the "
      + "chat has a password, you must give the password to enter.",
      new SubscribeCommandHandler()),

  STATUS(Pattern.compile("/(status)(\\s+\\S+)*"), "/status"
      + " - display the party chat name and your alias",
      new StatusCommandHandler()),

  LIST(Pattern.compile("/(list|members)(\\s+\\S+)*"), "/list"
      + " - list the current members of the party chat you are in",
      new ListCommandHandler()),

  ALIAS(Pattern.compile("/(alias|aka)\\s*(\\S*)"), "/alias [name]"
      + " - give yourself an alias; if you do not specify a name,"
      + " your current alias is removed", new AliasCommandHandler()),

  SCORE(Pattern.compile("/score(\\s+)?(.*)"), "/score [name]"
      + " - get the score for a name. if no name is given, " 
      + "then all scores are printed", new ScoreCommandHandler()),

  ME(Pattern.compile("/(me)\\s+(.*)"), "/me message" 
      + " - say something in the third person", new MeCommandHandler()),

  REASONS(Pattern.compile("/reasons(\\s+)?(.*)"), "/reasons [name]"
      + " - similar to /score, except it also returns the reasons "
      + " for increments/decrements", new ReasonsCommandHandler()),

  EXIT(Pattern.compile("/(unsub|unsubscribe|leave|exit)\\s*#?(\\S*)",
      Pattern.CASE_INSENSITIVE), "/exit"
      + " - leave the party chat you are currently in",
      new ExitCommandHandler()),

  HELP(Pattern.compile("/(\\?|help)(\\s+\\S+)*", Pattern.CASE_INSENSITIVE),
      "/help" + " - offer some instructions on how to use PartyChat and "
          + "directs users to /commands", new HelpCommandHandler()),

  COMMANDS(Pattern.compile("/(commands|actions|instructions)(\\s+\\S+)*",
      Pattern.CASE_INSENSITIVE), "/commands - displays this menu",
      new CommandsCommandHandler()),

  SNOOZE(Pattern.compile("/(snooze)\\s*(.*)"), "/snooze time" 
      + " - ignore this partychat for the specified amount of time. E.g." 
      + "\"snooze 1h\" or \"snooze 15m\"",
      new SnoozeCommandHandler()),

  WHOIS(Pattern.compile("/(whois|describe)\\s+(\\S*)"), "/whois name" 
      + " - display information about this party chat user/alias",
      new WhoisCommandHandler()),

  WHISPER(Pattern.compile("/(whisper|msg)\\s+(\\S*)\\s+(\\S.*)"), 
      "/whisper name message - send a private message to another user",
      new WhisperCommandHandler()),	

  SAVE_STATE(Pattern.compile("/(save-state)(\\s+\\S+)*",
      Pattern.CASE_INSENSITIVE), "if you can use this, then you know",
      new SaveStateCommandHandler(), true),

  ;

  final Pattern pattern;

  final String documentation;

  final boolean notPublic;

  final CommandHandler handler;

  Command(Pattern pattern, String documentation, CommandHandler handler) {
    this(pattern, documentation, handler, false);
  }

  Command(Pattern pattern, String documentation, CommandHandler handler,
      boolean notPublic) {
    this.pattern = pattern;
    this.documentation = documentation;
    this.notPublic = notPublic;
    this.handler = handler;
  }

  /** @return the short name of the command, such as "/score" */
  public String getShortName() {
    return "/" + this.name().toLowerCase();
  }

  /**
   * Take the user input, such as "/score dolapo"
   * and if it matches one of the Command patterns,
   * return the Command that it matches;
   * otherwise, return null.
   */
  public static Command isCommand(String content) {
    if (content == null || content.charAt(0) != '/') {
      return null;
    }
    // TODO(bolinfest): implement a faster lookup using a regex
    // built from the "short name" of each command
    for (Command command : Command.values()) {
      if (command.pattern.matcher(content).matches()) {
        return command;
      }
    }
    return null;
  }

  /**
   * This is a string that lists the documentation for all of the commands.
   * It is lazily built from the documentation field of each Command.
   */
  private static String COMMAND_DOCUMENTATION = null;

  public static String getCommandDocumentation() {
    if (COMMAND_DOCUMENTATION != null) {
      return COMMAND_DOCUMENTATION;
    } else {
      StringBuilder builder = new StringBuilder("PartyChat commands: \n\n");
      for (Command command : Command.values()) {
        if (command.notPublic) {
          continue;
        }
        builder.append(command.documentation).append("\n\n");
      }
      return (COMMAND_DOCUMENTATION = builder.toString());
    }
  }

}
