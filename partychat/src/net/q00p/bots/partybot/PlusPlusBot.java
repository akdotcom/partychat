//Copyright 2005 Google Inc. All Rights Reserved

package net.q00p.bots.partybot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.q00p.bots.Message;

/**
 * Simple plusplusbot implementation.
 * It stores the data in memory so all scores will be lost if the server is bounced -
 * this should be easily fixable once I decide to stop being lazy and do it "right".
 *
 * Created on Mar 25, 2006
 * @author <a href="mailto:dolapo@gmail.com">Dolapo Falola</a>
 */
public class PlusPlusBot {
  
  private static final String LOG_DELIMITER = "\t";

  private static final String INC_MESSAGE_FORMAT =
    "woot! %s -> %d (%s)";
  
  private static final String DEC_MESSAGE_FORMAT =
    "ouch! %s -> %d (%s)";
  
  /**
   * Things that can't be ++d
   */
  private final Set<String> blacklistedTargets;
  
  /**
   * chat -> {targetscore}
   */
  private final Map<String, Map<String, Integer>> scoreBoard;

  private BufferedWriter logFile;
  
  public PlusPlusBot() {
    blacklistedTargets = new HashSet<String>();
    scoreBoard = new HashMap<String, Map<String, Integer>>();
    try {
      logFile = prepareAndLoadLog("ppblog");
    } catch (IOException e) {
      System.err.println("Unable to log ppb!");
    }
  }

  
  /**
   * We're too lazy to write proper tests. Use this instead. ;-)
   */
  public static void main(String[] args) {
    Matcher matcher = Pattern.compile("(\\S+)(\\+\\+|\\-\\-)\\W*(\\w*.*)").matcher("cows++");
    matcher.find();
    System.err.println(matcher.group(1));
    PlusPlusBot ppb = new PlusPlusBot();
    System.out.println(ppb.getScores("chat", null));
    ppb.doCommand("sender", "chat", "target", "reason", true);
    System.out.println(ppb.getScores("chat", null));
    try {
      ppb.logFile.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * For now, assume partychat is so small that we can load a full log
   * at startup.
   * @throws IOException 
   */
  private BufferedWriter prepareAndLoadLog(String filename) throws IOException {
    File file = new File(filename);
    // Replay any existing log
    if (file.exists()) {
      BufferedReader rdr = new BufferedReader(new FileReader(file));
      String line;
      while (null != (line = rdr.readLine())) {
        String[] logElts = line.split(LOG_DELIMITER);
        if (logElts.length != 5) {
          continue; // skip things we don't understand
        }
        doCommand(logElts[0], logElts[1], logElts[2], logElts[3],
            logElts[4].equals("+"));
      }
      rdr.close(); // do we also need to close the file reader?
    } else {
      file.createNewFile();
    }
    return new BufferedWriter(new FileWriter(file, true));
  }


  public Message increment(
      Message message, String chat, String target, String reason) {
    return doCommand(message.getFrom().getName(), chat, target, reason, true);
  }

  public Message decrement(
      Message message, String chat, String target, String reason) {
    return doCommand(message.getFrom().getName(), chat, target, reason, false);
  }


  public Message doCommand(String from, String chat, String target, String reason,
      boolean increment) {
    
    // Skip the blacklist
    if (blacklistedTargets.contains(target)) {
      return null;
    }
    
    // For old time's sake, also increment the letter associated with this action
    if (target.length() > 1) {
      getUpdatedScore(chat, target.substring(target.length() - 1), increment);
    }
    
    // Log the event
    try {
      logEvent(from, chat, target, reason, increment);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // Update the map
    int currentScore = getUpdatedScore(chat, target, increment);
    return getResponseMessage(target, reason, increment, currentScore);
  }

  private void logEvent(String from, String chat, String target, String reason,
      boolean increment) throws IOException {
    if (logFile == null) {
      return;
    }
    reason = reason.replaceAll("\n", " ").replaceAll("\t", " "); // just in case
    for (String s : new String[] {from, chat, target, reason}) {
      logFile.write(s);
      logFile.write(LOG_DELIMITER);
    }
    logFile.write(increment ? "+" : "-");
    logFile.newLine();
  }

  private Message getResponseMessage(String target, String reason,
      boolean increment, int currentScore) {
    String formattedReason = getReasonString(reason);
    String format = increment ? INC_MESSAGE_FORMAT : DEC_MESSAGE_FORMAT;
    String result = String.format(format, target, currentScore, formattedReason);
    Message m = new Message(null, null, result);
    return m;
  }


  private int getUpdatedScore(String chat, String target, boolean increment) {
    int currentScore = 0;
    Map<String, Integer> scores = getScoreBoard(chat);

    if (scores.containsKey(target)) {
      currentScore = scores.get(target);
    }
    
    currentScore = increment ? currentScore + 1 : currentScore - 1;
    scores.put(target, currentScore);
    return currentScore;
  }
  
  private synchronized Map<String, Integer> getScoreBoard(String chat) {
    if (scoreBoard.containsKey(chat)) {
      return scoreBoard.get(chat);
    }
    Map<String, Integer> scores = Collections.synchronizedMap(
        new HashMap<String, Integer>());
    scoreBoard.put(chat, scores);
    return scores;
  }
  
  private String getReasonString(String reason) {
    if (reason == null || reason.trim().length() == 0) {
      return "";
    }
    return reason;
  }


  public String getScores(String chat, String regex) {
    List<Entry<String, Integer>> toShow =
      new ArrayList<Entry<String, Integer>>();
    
    Set<Entry<String, Integer>> allScores = getScoreBoard(chat).entrySet();
    
    if (regex != null && !regex.equals("")) {
      Pattern searchPattern;
      try {
        searchPattern = Pattern.compile(regex);
      } catch (RuntimeException e) {
        return "invalid pattern";
      }
      
      for (Entry<String, Integer> elt : allScores) {
        if (searchPattern.matcher(elt.getKey()).matches()) {
          toShow.add(elt);
        }
      }  
    } else {
      toShow.addAll(allScores);
      Collections.sort(toShow, new Comparator<Entry<String, Integer>>() {
        public int compare(Entry<String, Integer> a,
            Entry<String, Integer> b) {
          return a.getValue().compareTo(b.getValue());
        }
      });
    }
    
    StringBuilder result = new StringBuilder();
    for (Entry<String, Integer> elt : toShow) {
      if (result.length() > 0) {
        result.append("\n");
      }
      result.append(elt.getKey())
            .append(":")
            .append(elt.getValue());
    }
    
    if (result.length() == 0) {
      return "no scores found";
    }
    
    return result.toString();
  }
}
