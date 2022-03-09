package edu.iu.ess.util;

public final class StringUtil {

   private StringUtil() {
   }

   /**
    * Mask all but the last characters of a string with the specified masking
    * character.
    *
    * @param s The original string.
    * @param m The masking character.
    * @param n The number of chars at the end of the string to leave unmasked.
    * @return masked string
    */
   public static String mask(String s, char m, int n) {
      if (s == null || n >= s.length()) {
         return s;
      }
      StringBuilder sb = new StringBuilder(s);
      for (int i = 0; i < s.length() - n; i++) {
         sb.setCharAt(i, m);
      }
      return sb.toString();
   }


}