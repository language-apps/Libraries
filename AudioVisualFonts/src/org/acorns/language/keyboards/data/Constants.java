/**
 * Constants.java
 *   Class defining constants used by various modules
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

package org.acorns.language.keyboards.data;

import java.awt.*;

public interface Constants
{
   /** Various colors used by the program */
   public final Color GREY        = new Color(192,192,192);
   public final Color OPTIONCOLOR = new Color(220, 220, 220);
   public final Color DARKGREY    = new Color(80, 80, 80);
   public final Color LIGHTBLUE   = new Color(210,210,255);
   public final Color DARKRED     = new Color(128,0,0);
   public final Color DARKBLUE    = new Color(0,0,128);

   /** Parameters for the DeadKeys class */
   public final int DEAD_COLUMNS      = 8;
   public final int DEAD_KEY_HEIGHT   = 30;
   public final int DEAD_HEIGHT       = 200;
   
   public final static char[] codeTable =
   {   'A', 'S', 'D', 'F', 'H', 'G', 'Z', 'X', 'C', 'V',          //0-9
       '|', 'B', 'Q', 'W', 'E', 'R', 'Y', 'T', '1', '2',          //10-19
       '3', '4', '6', '5', '=', '9', '7', '-', '8', '0',          //20-29
       ']', 'O', 'U', '[', 'I', 'P', '\n', 'L', 'J', '\'',        //30-39
       'K', ';', '\\',',', '/', 'N', 'M', '.', '\t', ' ',         //40-49
       '`', 0x8, 0x3, 0x1B, '\0', '\0', '\0', '\0', '\0', '\0',   //50-59
       '\0','\0', '\0', '\0', '\0', '.', 0x1D, '*', '\0', '+',    //60-60
       0x1C, 0x1B, 0x1F, '\0', '\0', '/', '\n', 0x1E, '-', '\0',  //70-79
       '\0', '=', '0', '1', '2', '3', '4', '5', '6', '7',         //80-89
       '\0', '8', '9', '\0', '\0', '\0', '\0', '\0', '\0', '\0',  //90-99
       '\0', '\0', '\0', '\0', '?', '\0', '\0', '\0', '\0', '\0', //100-109
       '\0', '\0', '\0', '\0', 0x5, 0x1, 0xB, 0x7F, '\0', 0x4,    //110-119
       '\0', '\n', '\0', 0x1C, 0x1D, 0x1F, 0x1E, '\0'             //120-127
    };


   public final static String[] modCombinations =
	    {  "anyShift",  "anyControl", "command", "anyOption", "caps",
	       "shift", "rightShift", "control", "rightControl",
	       "option", "rightOption"
	    };

   /** Number of modifiers
    */
   public final static int SHIFT_MASK 		=	 1; 
   public final static int CTRL_MASK  		=	 2;	// ctrl on mac
   public final static int META_MASK  		=	 4;	// command on mac
   public final static int ALT_MASK   		=	 8;	// option on mac
   public final static int CAP_MASK   		=	16;  
   public final static int LEFT_SHIFT_MASK  =	32;
   public final static int RIGHT_SHIFT_MASK = 	64;
   public final static int LEFT_CTRL_MASK   =  128;
   public final static int RIGHT_CTRL_MASK  =  256;
   public final static int LEFT_ALT_MASK    =  512;  
   public final static int RIGHT_ALT_MASK 	= 1024;
   public final static int MODIFIERS 		= 2048;   	// 2^11.
   
   /* Indices int modifier table */
   public final static int MUST_HAVE = 0;
   public final static int CANT_HAVE = 1;
   public final static int MODIFIER_INDEX = 2;
   
   /* Indices for dead key map data */
   public final static int ACTION  = 0;
   public final static int OUTPUT = 1;
   public final static int MAP_LEN = 2;
   
   /** Number of character codes */
   public final int CODES = 128;

   /** Maximum size of dead key sequence */
   public final int MAX_DEAD_SEQUENCE = 4;

   /** Map from characters to MAX keycodes */
   public final static String lowerKeyCodeMapping =
      "asdfhgzxcv" + "\0bqweryt12" + "3465=97-80" + "]ou[ip\0lj'"
         + "k;\\,/nm.\0\0" + "`";

   public final static String upperKeyCodeMapping =
      "ASDFHGZXCV" + "\0BQWERYT!@" + "#$^%+(&_*)" + "}OU{IP\0LJ\""
         + "K:|<?NM>\0\0" +"~";

   /** String offsets for keyboard mapping keys */
   public static final int TOP=0, BOTTOM=1, KEY=2;

   /** Values for modifier and space keys for virtual keyboard classes */
   public static final int STRING=65500;
   public static final int SHFT=65500;
   public static final int CTRL=65501;
   public static final int CMD=65502;
   public static final int ALT=65503;
   public static final int CAPS=65504;
   public static final int SPACE=65505;
   public static final int CTRL_SIZE = SPACE + 1 - STRING;

}