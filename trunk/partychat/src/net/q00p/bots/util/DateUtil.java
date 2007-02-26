//Copyright 2005 Google Inc. All Rights Reserved

package net.q00p.bots.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * Utilities for date stuff.
 *
 * Created on Feb 24, 2007
 * @author <a href="mailto:dolapo@gmail.com">Dolapo Falola</a>
 */
public final class DateUtil {
  private DateUtil() {}
  
  /**
   * Tries to return milliseconds from a user entered string like 10m or 10s or
   * 10h or just 10. A bare number is interpreted as seconds.
   * 
   * @param text
   * @throws ParseException on several conditions - we try not to return a 
   *         number at all if the input is malformed.
   * @return
   */
  public static long parseTime(String text) throws ParseException {
    NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
    
    ParsePosition position = new ParsePosition(0);
    Number number = nf.parse(text, position);
    
    if (position.getIndex() == 0) {
      // We didn't parse anything, so bye.
      throw new ParseException("Invalid input string", 0);
    }
    
    String unit = text.substring(position.getIndex());
    
    int multiplier = 60*1000;
    
    if ("d".equals(unit)) {
      multiplier *= 60 * 24;
    } else if ("h".equals(unit)) {
      multiplier *= 60;
    } else if ("s".equals(unit)) {
      multiplier /= 60;
    } else if ("m".equals(unit) || unit.length() == 0) {
      // bleh
    } else {
      throw new ParseException("Invalid input string", position.getIndex());
    }
    
    long result = number.longValue() * multiplier;
    if (result < 0) {
      // Oops overflow.
      throw new ParseException("Time period too large", 0); 
    }
    
    return result;
  }
  
  // Throws an exception if the parse doesn't match expected.
  // expected = -1 if we except an exception.
  private static void checkParse(String s, long expected) throws Exception {
    boolean exceptionCaught = false;
    long value = 0;
    
    try {
      value = parseTime(s);
    } catch (ParseException e) {
      exceptionCaught = true;
    }
    
    if ((expected == -1 && !exceptionCaught) ||
        (expected != -1 && expected != value)) {
      throw new Exception("Parse broken for " + s + ", expected " + 
                          expected + " but got: " + value);
    }
    
  }
  
  /**
   * Return a string form of a ms.
   * Something like 10m or 10s or 10d with loss of precision.
   * 
   * @param ms
   * @return
   */
  public static String prettyFormatTime(long ms) {
    throw new UnsupportedOperationException("dolapo=lazy");
  }
  
  
  // In lieu of unit tests...
  public static void main(String[] args) throws Exception {
    checkParse("10m", 10*60*1000);
    checkParse("20",  20*60*1000);
    checkParse("30h", 30*60*60*1000);
    checkParse("5s",  5*1000);
    checkParse("1d",  1*24*60*60*1000);
    checkParse("99d", 99L*24*60*60*1000);
    
    checkParse("",        -1);
    checkParse("crap",    -1);
    checkParse("10mmmm",  -1);
    checkParse("10hours", -1);
    
    
    checkParse("99999999999999999999d", -1);
    System.out.println("Tests passed");
  }
}
