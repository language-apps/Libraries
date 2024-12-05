/**
 *
 *   @name KeyboardHandler.java
 *      Class to process to remap keyboards at runtime
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

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import org.xml.sax.*;

import javax.xml.parsers.*;

import org.acorns.language.keyboards.data.*;

public class KeyboardHandler implements Constants, KeyListener 
{
   private String language;

   private String state;
   
   private KeyboardData data;
   private int keyLocation;


   /** Constructor to set up the .keylayout data structures */
public KeyboardHandler(URL url) 
		throws SAXException, ParserConfigurationException, 
                  IOException, NoSuchMethodException
   {
      JLabel label = new JLabel(" ");
      ImportKeyboard keyboard = new ImportKeyboard(url);
      data = keyboard.getData();
      String text = label.getText();
      if (text.length()!=1)  {  throw new IOException(text); }

      language = data.getLanguage();
      state = "none";
  }

  public KeyboardHandler(String language, ImportKeyboard keyboard)
  {
	  this.language = language;
	  data = keyboard.getData();
	  state = "none";
  }
   
  public String getLanguage() { return language; }


   /** Method to translate key sequences based on .keylayout specifications */
   public String processChar(char character, int modifier)
   {
      character = ModCase.setCase(character);
      String output = computeOutput(modifier, character);
      return output;
   }  // End processCharacter()
   
   /** Method to intercept keyboard characters that are entered */
   public boolean processEvent(KeyEvent e)
   { 
	  if (e.getID()!=KeyEvent.KEY_TYPED) return false;

      char character = e.getKeyChar();
      int modifiers = getModifiers(e);

      String sequence = processChar(character, modifiers);
      if (sequence==null) return true;
      if (sequence.length()==0) { return true; }

      Component component = e.getComponent();
      if (component instanceof JTextComponent)
      {  
    	 JTextComponent field = (JTextComponent)component;
         int start = field.getSelectionStart();
         int end = field.getSelectionEnd();
         String text = field.getText();
         text = text.substring(0,start) + sequence + text.substring(end);
         field.setText(text);
         field.setCaretPosition(start + sequence.length());
         return true;
      }
      return false;
   }
   
   /** Handle the key typed event from a text field. */
   public void keyTyped(KeyEvent e)  
   { 
	   	char character = e.getKeyChar();
	   	if (character==KeyEvent.CHAR_UNDEFINED)
	   		return;
	   	
	   	if (character < 32) return;
	   	if (character == KeyEvent.CHAR_UNDEFINED) return;
	   	if (processEvent(e)) e.consume();
  }
   
   public void keyPressed(KeyEvent e)  
   {
 		int location = e.getKeyLocation();
	   	if ((location == KeyEvent.KEY_LOCATION_LEFT) ||
	   		(location == KeyEvent.KEY_LOCATION_RIGHT))
	   		    keyLocation = location;
   }

   public void keyReleased(KeyEvent e) 
   {
      	keyLocation = 0;
   }

   /** Translate the character based on the keyboardMapping
    *
    * @param mapping The keyboard character mapping to use
    * @param character The character to translate based on the mapping
    * @return The translated character ('\0' if none exists).
    */
   private char translateChar(char[] mapping, char character)
   {  if (character==' ')  return character;

      int lower = lowerKeyCodeMapping.indexOf(character);
      int upper = upperKeyCodeMapping.indexOf(character);

      int mapValue = lower;
      if (lower<0) mapValue = upper;
      if (mapValue<0) return '\0';
      return mapping[mapValue];
   }
   
   private int getModifiers(KeyEvent e)
   {
	   int modifiers = e.getModifiersEx();
	   int newModifiers = 0;
	   int location = keyLocation;

	   if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0)
	   {
		    newModifiers |= SHIFT_MASK;
		    if (location == KeyEvent.KEY_LOCATION_RIGHT) {
		      newModifiers |= RIGHT_SHIFT_MASK;
		    }
		    if (location == KeyEvent.KEY_LOCATION_LEFT) 
		    {
		      newModifiers |= LEFT_SHIFT_MASK;
		    }
	   }
	   
	   if ((modifiers & KeyEvent.CTRL_DOWN_MASK) != 0)
	   {
		    newModifiers |= CTRL_MASK;
		    if (location == KeyEvent.KEY_LOCATION_RIGHT) {
		       newModifiers |= RIGHT_CTRL_MASK;
		    }
		    if (location == KeyEvent.KEY_LOCATION_LEFT) 
		    {
		       newModifiers |= LEFT_CTRL_MASK;
		    }
	   }

	   if ((modifiers & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0)
	   {
	      newModifiers |= RIGHT_ALT_MASK + ALT_MASK;
	   }
	   
	   if ((modifiers & KeyEvent.ALT_DOWN_MASK) != 0)
	   {
		    newModifiers |= ALT_MASK;
		    if ((modifiers & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0)
		    {
		      newModifiers |= RIGHT_ALT_MASK;
		    }
	   		else
		    {
		      newModifiers |= LEFT_ALT_MASK;
		    } 
	   }
		   
	   if ((modifiers & KeyEvent.META_DOWN_MASK) != 0)
	   {
		   newModifiers |= META_MASK;
	   }
	   
	   if (Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK))
	       newModifiers += CAP_MASK;
	   
	   return newModifiers;
   }

   /** Find the largest key subsequence that matches the key
    *
    * @param current modifier
    * @param key the key value
    * @param the character code just received
    * @return The largest matching output
    */
   private String computeOutput(int modifier, char character)
   {  
      Hashtable<String, String[]> sequences = data.getDeadKeySequences();
      String key, terminator, sequenceData[];

	  Hashtable<Integer, String[]> deadKeyMap = data.getDeadKeyMap(modifier);
	  if (deadKeyMap==null)
	  {
		  terminator = data.findTerminator(state);
		  state = "none";
		  return terminator;
	  }
	
	  String[] mapData = deadKeyMap.get((int)character);
	  if (mapData == null)
	  {
		  terminator = "";
		  if (!state.equals("none"))
		  {
			  terminator = data.findTerminator(state);
		  }
    	  char[] keyMap = data.getModifierKeyMap(modifier);
    	  char xlate = translateChar
                  (keyMap, character);
    	  state = "none";
    	  return terminator + xlate;
	  }

	  if (mapData[ACTION].length()==0)
	  {
		  state = "none";
		  return mapData[OUTPUT]; // No next state
	  }

	  if (state.contentEquals("none"))
      {
		  key = state + "~~" + mapData[ACTION];
		  sequenceData = sequences.get(key);
		  if (sequenceData == null)
		  {
			  terminator = data.findTerminator(mapData[ACTION]);
			  state = "none";
			  return terminator;
		  }

		  if (sequenceData[ACTION].length()>0)
		  {
			  state = sequenceData[ACTION];
			  return "";
		  }
		  else
		  {
			  state = "none";
			  return sequenceData[OUTPUT];
		  }
      }
	  
      key = state + "~~" + mapData[ACTION];
      sequenceData = sequences.get(key);
      if (sequenceData == null)
      {
		  terminator = data.findTerminator(state);
    	  char[] keyMap = data.getModifierKeyMap(modifier);
    	  char xlate = translateChar
                  (keyMap, character);
    	  state = "none";
    	  return terminator + xlate;
      }
      
      if (sequenceData[ACTION].length()==0)
      {
    	  state = "none";
    	  return sequenceData[OUTPUT];
      }
    			  
      state = sequenceData[ACTION];
      return  "";
   }
}      // End of KeyboardHandler class
