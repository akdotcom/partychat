//Copyright 2005 Google Inc. All Rights Reserved

package net.q00p.bots.partybot;

import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.q00p.bots.Message;
import net.q00p.bots.util.AbstractBot;

/**
 * Simple plusplusbot implementation.
 * It stores the data in memory so all scores will be lost if the server is bounced -
 * this should be easily fixable once I decide to stop being lazy and do it "right".
 *
 * Created on Mar 25, 2006
 * @author <a href="mailto:dolapo@gmail.com">Dolapo Falola</a>
 */
public class PlusPlusBot {
  
  private static final String INC_MESSAGE_FORMAT =
    "yaay, %s: %d %s";
  
  private static final String DEC_MESSAGE_FORMAT =
    "ouch, %s: %d %s";
  
  /**
   * Things that can't be ++d
   */
  private final Set<String> blacklistedTargets;
  
  /**
   * chat -> {targetscore}
   */
  private final Map<String, Map<String, Integer>> scoreBoard;
  
  public PlusPlusBot() {
    blacklistedTargets = new HashSet<String>();
    scoreBoard = new HashMap<String, Map<String, Integer>>();
  }


  public Message increment(
      Message message, String chat, String target, String reason) {
    return doCommand(message, chat, target, reason, true);
  }

  public Message decrement(
      Message message, String chat, String target, String reason) {
    return doCommand(message, chat, target, reason, false);
  }


  public Message doCommand(
      Message message, String chat, String target, String reason,
      boolean increment) {
    if (blacklistedTargets.contains(target)) {
      return null;
    }
    
    int currentScore = 0;
    
    Map<String, Integer> scores = getScoreBoard(chat);

    if (scores.containsKey(target)) {
      currentScore = scores.get(target);
    }
    
    currentScore = increment ? currentScore + 1 : currentScore - 1;
    scores.put(target, currentScore);
    
    StringBuilder result = new StringBuilder();
    Formatter f = new Formatter(result);
    String actor = message.getFrom().getName();
    String formattedReason = getReasonString(reason);
    
    String format = increment ? INC_MESSAGE_FORMAT : DEC_MESSAGE_FORMAT;
    
    f.format(format, target, currentScore, formattedReason);
    
    Message m = new Message(null, null, result.toString());
    
    return m;
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
    return "(" + reason + ")";
  }
}
