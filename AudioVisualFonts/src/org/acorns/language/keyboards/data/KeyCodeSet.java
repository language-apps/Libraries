/**
 * KeyCodeSet.java
 *
 * This Class to hold the data structure containing the set of key codes 
 *   mapping extracted from an XML file 
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

import java.util.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class KeyCodeSet implements Constants
{
    /** A list of key code output characters  */
    private char[] map; 

    /** The initial dead key map */
    private Hashtable<Integer, String[]> deadKeyMap;

    /** Constructor
     * @param document The XML DOM
     * @param keyMap The element containing the key codes
     * @param modifiers number of valid modifier keys
     * @param setNo which KeyCodeSet to which this object corresponds
     */
    public KeyCodeSet(Document document, Element keyMap
                , int setNo)  throws SAXException
    {
       // Initialize the state table and the key map list
       NodeList list = keyMap.getElementsByTagName("key");
       map = initializeKeyMaps(document, list);
       deadKeyMap = initializeDeadKeyMap(document, list);
         
    }  // End of constructor
    
    private Hashtable<Integer, String[]> initializeDeadKeyMap
       (Document document, NodeList list)
     {
    	Element tag;
    	int code = 0;
    	
    	Hashtable<Integer, String[]> deadKeyMap = new Hashtable<Integer,String[]>();
    	
        for (int i=0; i<list.getLength(); i++)
        {
            tag = (Element)list.item(i);
            try  { code = Integer.parseInt(tag.getAttribute("code"));  }
            catch (NumberFormatException nfe)
            {  JOptionPane.showMessageDialog
            	(null, "Warning: Skipped " + tag.getTagName() 
                    + " number "
            		+ ++code +
            		" because it is missing a code attribute");
            	continue;
            }
            
            if (tag.hasAttribute("action"))
            {
                String[] data = new String[MAP_LEN];
                int key  = getCode(code);
                data[ACTION] = tag.getAttribute("action");
                data[OUTPUT] = tag.getAttribute("output");
                deadKeyMap.put(key, data);
            }
        }
    	return deadKeyMap;
     }
    
     public Hashtable<Integer, String[]> getDeadKeyMap()
     {
    	 return deadKeyMap;
     }
   
    /** Method to initialize the keyCode keys list
      * @param document The XML document DOM
      * @param list The list of key tag elements
      * @return the key code mapping    
      */
	private char[]  initializeKeyMaps
            (Document document, NodeList list)  throws SAXException
    {
       int code = 0;
       Element tag;
       char[] map = new char[CODES];
       String output;
       char character;
       for (int i=0; i<list.getLength(); i++)
       {
    	   tag = (Element)list.item(i);
    	   try  { code = Integer.parseInt(tag.getAttribute("code")); }
           catch (NumberFormatException nfe) {  continue; }
   
    	   if (tag.hasAttribute("output"))
    	   { 
    		   output = tag.getAttribute("output");
    		   if (output.length()==0 || output.length()>1) continue;
    		   character = output.charAt(0);
    		   if (character>=0x2400 && character<=0x2421)  continue;
    		   map[code] = character;
    	   }
       }   // End of for loop through all the key codes
       return map;
    }  // End of initializeKeyMap()
      

     /** Translate KeyEvent.VK_code to ASCII value
      *  @param code The VK_code
      * @return The Equivalent ascii code 
		    *   Note: '\0' for undefined keys
		    */
     private char getCode(int code)
     {  
    	 char value = codeTable[code];
    	 return value;
     }
     
     /** Method to get the keyboard codes
      *  @return character array indexed by code.
      */
      public char[] getKeyMap()
      {
    	  return map;
      }
     
}  // End of KeyCodeSet class
