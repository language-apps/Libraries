/**
 *
 *   @name ModCase.java
 *           Class for logic related to modifiers pressed
 *
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2010, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.acorns.language.keyboards.lib;

import org.acorns.language.keyboards.data.Constants;

public class ModCase implements Constants
{
   private static final String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
   private static final String upperLetters = lowerLetters.toUpperCase();
   private static final String lowerSpcl = "`1234567890-=[]\\;',./";
   private static final String upperSpcl = "~!@#$%^&*()_+{}|:\"<>?";

   /** Convert to match keylayout case of character codes 
    * 		Let the keymap determine the case using upper case keys
    * 
    */
   public static char setCase(char data)
   {
       return convert(data,lowerLetters+upperSpcl,upperLetters+lowerSpcl);
   }
   
   private static char convert(char character, String source, String destination)
   {
       int index = source.indexOf(character);
       if (index>=0) return destination.charAt(index);
       else          return character;
	   
   }

}  // End of ModCase class
