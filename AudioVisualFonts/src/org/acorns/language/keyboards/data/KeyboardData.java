/**
 * KeyboardData.java
 *
 * This class holds the data needed for the Keyboard Mapping applications
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class KeyboardData implements Constants
{    
    private ArrayList<int[]> modifierList;
    private ArrayList<char[]> keyboardMap;
    private ArrayList<Hashtable<Integer, String[]>> deadKeyMaps;
    private DeadSequences sequences;
    
    private String language;
 
     /** Constructor to initialize this object */
	public KeyboardData(String language)
    {  
       modifierList = new ArrayList<int[]>();
       keyboardMap = new ArrayList<char[]>();
       deadKeyMaps = new ArrayList<Hashtable<Integer, String[]>>();
       
       this.language = language;
    }
	
	public DeadSequences getKeySequences()
	{
		return sequences;
	}
	
	public void setDeadKeySequences(DeadSequences sequences)
	{
		this.sequences = sequences;
	}
	
	public Hashtable<String, String[]> getDeadKeySequences()
	{
		return sequences.getDeadSequences();
	}
	
	public String findTerminator(String state)
	{
		return sequences.findTerminator(state);
	}
	
  /** Method to add a modifier code to the keyboard data
   *  @param modifier must apply and can't apply values into keyMap table
   *  @param keyCode and array of character codes
   */
  public void addModifierKeyMap(char[] keyCode)
  { 
	  char[] keyCodes = new char[CODES];
	  for (int i=0; i<CODES; i++) { keyCodes[i] = keyCode[i]; }
	  keyboardMap.add(keyCodes);
  }
  
  /** Add modifier with index to keyCode and deadlist table */
  public void addModifier(int modifier[])
  {
	  modifierList.add(modifier);
  }

  /** Get the list of required and allowed modifiers for supplied index */
  public int[] getModifierList(int index)
  {
	  return modifierList.get(index);
  }
  
  /** Get the keycodes for the supplied index */
  public char[] getKeyCodeList(int index)
  {
	  return keyboardMap.get(index);
  }

  /** Add structure containing dead keys when the state is "none" */
  public void addDeadKeyMap(Hashtable<Integer, String[]> deadKeyMap)
  {	  
	  deadKeyMaps.add(deadKeyMap);
  }
  
  /** Get the dead key map that applies to the modifier */
  public Hashtable<Integer, String[]> getDeadKeyMap(int modifier)
  {
	  if (deadKeyMaps.isEmpty()) return null;

	  int index = findModifierIndex(modifier);
	  if (index<0) return null;
	  
	  return deadKeyMaps.get(index);
  }
  

  /** Method to return the keyboard mapping for a given modifier
   *
   * @param modifier The desired mofifier
   * @return array of characters for the keyboard mapping
   */
  public char[] getModifierKeyMap(int modifier)
  {   
	  if (keyboardMap.isEmpty()) return null;
	  
	  int index = findModifierIndex(modifier);
	  if (index<0) return null;

      char[] newMap = keyboardMap.get(index);
      return newMap.clone();
  }

  /** Find the Keyboard data that corresponds to the modifier 
   *  @return index (or -1 if not found)
   * 
   */
  private int findModifierIndex(int modifier)
  {
	  for (int index=0; index<modifierList.size(); index++)
	  {
		  int[] values = modifierList.get(index);
		  if ((values[MUST_HAVE] & modifier) != values[MUST_HAVE]) 
			 continue;
		  if ((values[CANT_HAVE] & modifier) != 0)
			  continue;
		  
		  return values[MODIFIER_INDEX];
	  }
	  return -1;
  }
 
  /** Method to get the language name */
  public String getLanguage() { return language; }

  /** method to display object for debugging purposes */
  public @Override String toString()
  {  
	 StringBuffer buffer = new StringBuffer();
	 String data;

	 buffer.append("var " + language.replaceAll("-", "") + " =\n");
	 buffer.append("{\"charCodes\": \n[");
	 
     for (int i=0; i<codeTable.length; i++)
     {  
    	 if (codeTable[i]<32 || codeTable[i] == 127)
    		 continue;
    	 
       	 if (i>0)
       	  buffer.append(",\n");
       	 
    	 data = "" + codeTable[i];
    	 if (codeTable[i] == '\\')
    		 data += codeTable[i];
    	 
       	 buffer.append("{\"key\":" + i + ", "
       	   		+ " \"value\": \"" + data 
       	   	    + "\"}");
     }
     buffer.append("\n],\n\n");

	 buffer.append(sequences);

     Hashtable<Integer, String[]> deadKeyMap;
     char[] charMap;
     
	 buffer.append("\n");
	 buffer.append("\"modifiers\": \n[");
	 int lastModifierIndex = -1;
     for (int index=0; index<modifierList.size(); index++)
     {
    	 int[] modifiers = modifierList.get(index);
    	 int modifierIndex = modifiers[MODIFIER_INDEX];
    	 if (modifierIndex == lastModifierIndex)
    		 continue;
    	 
    	 lastModifierIndex = modifierIndex;
    	 
         if (modifierIndex>0)
        	 buffer.append(",\n");
         
    	 buffer.append("{\"must\": " + modifiers[MUST_HAVE] + ", ");
    	 buffer.append("\"cant\": " + modifiers[CANT_HAVE]  + ", ");
    	 buffer.append("\"modIndex\": " + modifierIndex);
    	 buffer.append("}");
    	 
     }
     buffer.append("\n],\n\n");
     
     boolean comma = false;
	 buffer.append("\"modifierData\": [");
	 int len = deadKeyMaps.size();
     for (int index=0; index<len; index++)
     {  
    	 deadKeyMap = deadKeyMaps.get(index);
    	 charMap = keyboardMap.get(index);
    	 
         if (comma)
        	 buffer.append("},");
    	 
         comma = true;
         
    	 // Key map
         buffer.append("{\n\"charMap\": \n[");
    	 int count = 0;
         for (int i=0; i<charMap.length; i++)
         {  
        	   if (charMap[i]<32 || charMap[i]==127)
        		   continue;
        	   
        	   if (count++ > 0)
        	   {
        		   buffer.append(",\n");
        	   }
        	   
        	   data = "" + charMap[i];
        	   if (data.contains("\""))
     	            data = data.replace("\"", "\\\"");
     	       else data = data.replace("\\", "\\\\");
        	   
	       	   buffer.append("{\"key\":" + i + ", "
	       	   		+ " \"output\": \"" + data 
	       	   	    + "\"}");
         }
         buffer.append("\n],\n\n");
         
         buffer.append("\"deadKeyMap\": \n[");
         Enumeration<Integer> keys = deadKeyMap.keys();
         String value[], action, output;
         int key;
         while(keys.hasMoreElements())
         {
        	   key = keys.nextElement();
        	   value = deadKeyMap.get(key);
        	   action = value[ACTION];
        	   output = value[OUTPUT];
        	   if (output.contains("\""))
       	            output = output.replace("\"", "\\\"");
       	       else output = output.replace("\\", "\\\\");

        	   if (action.contains("\""))
      	            action = action.replace("\"", "\\\"");
      	       else action = action.replace("\\", "\\\\");

        	   output = output.replace("\\", "\\\\");
        	   buffer.append("{\"key\":" + key + ", "
        			+ "\"action\": \"" + action  + "\", "
        	   		+ " \"output\": \"" + output 
        	   	    + "\"}");
        	   if (keys.hasMoreElements())
        	   {
        		   buffer.append(",");
            	   buffer.append("\n");
        	   }
        	   else
        		   buffer.append("\n]");
         }
         
         buffer.append("\n");
     }
	 buffer.append("}]}");
     return buffer.toString();
   }  // End toString()

}    // End KeyboardData class