package edu.iu.uits.lms.canvas.helpers;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */


import edu.iu.uits.lms.canvas.model.User;

public class UserHelper {

   /**
    * Format and set all the name fields
    * @param user User
    * @param firstName First name
    * @param lastName Last name
    */
   public static void setAllNameFields(User user, String firstName, String lastName) {
      user.setName(formatName(firstName, lastName));
      user.setShortName(formatShortName(firstName, lastName));
      user.setSortableName(formatSortableName(firstName, lastName));
   }

   /**
    * Check of the formatted name parts all match the original
    * @param user User
    * @param firstName First name
    * @param lastName Last name
    * @return True if all name parts match, false otherwise
    */
   public static boolean namesMatch(User user, String firstName, String lastName) {
      String computedName = formatName(firstName, lastName);
      String computedShortName = formatShortName(firstName, lastName);
      String computedSortableName = formatSortableName(firstName, lastName);
      return computedName.equals(user.getName()) && computedShortName.equals(user.getShortName()) && computedSortableName.equals(user.getSortableName());
   }

   /**
    * Format the name
    * @param firstName First name
    * @param lastName Last name
    * @return The name formatted as "first last"
    */
   public static String formatName(String firstName, String lastName) {
      return firstName + " " + lastName;
   }

   /**
    * Format the short name
    * @param firstName First name
    * @param lastName Last name
    * @return The name formatted as "first last"
    */
   public static String formatShortName(String firstName, String lastName) {
      return firstName + " " + lastName;
   }

   /**
    * Format the sortable name
    * @param firstName First name
    * @param lastName Last name
    * @return The name formatted as "last, first"
    */
   public static String formatSortableName(String firstName, String lastName) {
      return lastName + ", " + firstName;
   }
}
