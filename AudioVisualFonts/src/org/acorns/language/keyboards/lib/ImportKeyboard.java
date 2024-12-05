/**
 * ImportKeyboard.java
 *   Class to import .keylayout files in preparation for keyboard mapping
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.acorns.language.keyboards.data.Constants;
import org.acorns.language.keyboards.data.DeadSequences;
import org.acorns.language.keyboards.data.KeyCodeSet;
import org.acorns.language.keyboards.data.KeyboardData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Class to import .keylayout files for processing */
public class ImportKeyboard implements Constants, EntityResolver
{
  private KeyboardData keyboardData;
  private String language;
  private Document document;
  


  /** Method to import information from .keylayout files
   *
   * @param url The URL to the file in question
   * @param label A Jlabel for error information or null to only get language
   * @throws SAXException
   * @throws ParserConfigurationException
   * @throws IOException
   */
  public ImportKeyboard(URL url)
          throws SAXException, ParserConfigurationException, IOException
  {
     // Parse the xml and create a DOM object.
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(this);

    InputStream stream = url.openStream();
    XMLFilterStream xmlStream = new XMLFilterStream(stream);
    document = builder.parse(xmlStream);
    stream.close();
    System.gc();
    document.getDocumentElement().normalize();

    // Get the language name from the root element.
    Element keyboard = document.getDocumentElement();
    language = keyboard.getAttribute("name");

    keyboardData = new KeyboardData(language);

    // Get layout element and extract attributes.
    NodeList layoutList = document.getElementsByTagName("layouts");
    if (layoutList.getLength()==0) throw new SAXException("No key layouts");

    Element layoutElement = (Element)layoutList.item(0);
    String mapSet = getChildAttribute(layoutElement, "layout", "mapSet");
    String modifiers = getChildAttribute(layoutElement, "layout", "modifiers");

    // Now compute valid modifier combinations.
    int[][] modifierList = getModifierIndices(document, modifiers);

    // Get the key code mappings for each modifier
    Element mapSetElement = document.getElementById(mapSet);
    if (mapSetElement==null) throw new SAXException("No keyMapSet tag");
    if (!mapSetElement.getTagName().equals("keyMapSet"))
    {   
    	NodeList mapList = document.getElementsByTagName("keyMapSet");
        if (mapList.getLength()==0) throw new SAXException("No keyMapSet tag");
        else mapSetElement = (Element)mapList.item(0);
    }

    NodeList codes = mapSetElement.getElementsByTagName("keyMap");
    Element keyMap;
    ArrayList<KeyCodeSet> keyCodeSet = new ArrayList<KeyCodeSet>();
    char[] keyMapCodes;

    for (int i=0, index=0; i<codes.getLength(); i++)
    {  
       keyMap = (Element)codes.item(i);
       keyCodeSet.add(new KeyCodeSet(document, keyMap, i));
       keyMapCodes = keyCodeSet.get(i).getKeyMap();
       keyboardData.addModifierKeyMap(keyMapCodes);
       
       while (index<modifierList.length &&
    		   modifierList[index][MODIFIER_INDEX] <=i)
       {
          keyboardData.addModifier(modifierList[index]);
          index++;
       }
    }     // End outer for
    
    Hashtable<Integer, String[]> deadKeyMap;
 
    keyboardData.setDeadKeySequences(new DeadSequences(document));
    for (int i=0; i<codes.getLength(); i++)
    {  
        deadKeyMap = keyCodeSet.get(i).getDeadKeyMap();
        keyboardData.addDeadKeyMap(deadKeyMap);
    }   // End for each modifier set
  }     // End of constructor.

    /** Get keybordData object */
  public KeyboardData getData() { return keyboardData; }

  /** Method to return the language corresponding to this .keylayout file */
  public String getLanguage() { return language; }

  /** Set keyboardData object */
  public void setData(KeyboardData keyboardData)
  {  this.keyboardData = keyboardData; }
  
  public String toJSONString() throws TransformerException 
  {
	  String data = keyboardData.toString();
      return data;
  }

  /** Print keyboardData */
  public @Override String toString()
  {  if (keyboardData==null) return "No data";
     else return keyboardData.toString();
  }
    
 /** Method to compute which indices correspond to which modifier keys.
  *
  * @param document the XML document being parsed
  * @param the id of the modifier map tag
  */
  private int[][] getModifierIndices(Document document, String id)
	                      throws SAXException
  {
     Element modifierMap = document.getElementById(id);
     if (modifierMap==null) throw new SAXException("No modifierMap tag");

     if (!modifierMap.getTagName().equals("modifierMap"))
     {   NodeList modList = document.getElementsByTagName("modifierMap");
         if (modList.getLength()==0)
                throw new SAXException("No modifierMap tag");
         else modifierMap = (Element)modList.item(0);
     }

     // Compute total number of modifiers
     NodeList totalModifiersNode = modifierMap.getElementsByTagName("modifier");
     int totalModifiers = totalModifiersNode.getLength();
     int[][] validModifiers = new int[totalModifiers][3];
      
     // Find the keyMap index for each modifier.
     NodeList keyMapSelects =modifierMap.getElementsByTagName("keyMapSelect");
     int keyMapSelectsLen = keyMapSelects.getLength();
		 
     Element element;
     int index = 0;
     for (int modifier=0; modifier<keyMapSelectsLen; modifier++)
     {  
    	 element = (Element)keyMapSelects.item(modifier);

         // Get the KeyMapSelect keys attribute.
         int allow, require;
         NodeList nodes = element.getElementsByTagName("modifier");
         Element child;
         String modifierKey;
         for (int m=0; m<nodes.getLength(); m++)
         {  
        	 child = (Element)nodes.item(m);
             modifierKey = child.getAttribute("keys").toLowerCase();
   	 
            // Determine which modifiers applying to this keys value
            allow = require = 0;
            
            // Create bit combinations representing valid modifiers
            String[] modifierKeys = modifierKey.split(" ");
        	for (int j=0; j<modifierKeys.length; j++)
        	{
        		for (int i=0; i<modCombinations.length; i++)
        		{ 
            		String lower = modCombinations[i].toLowerCase();
        			if ((modifierKeys[j]).equals(lower+"?")) 
        			{
        				allow |= 1<<i;
        				break;
        			}
        			else if (modifierKeys[j].equals(lower))
            		{  
            			require |= 1<<i;
            			break;
            		}
                }
            }
            
            if ( (require & SHIFT_MASK) !=0)
            {
            	allow |= LEFT_SHIFT_MASK + RIGHT_SHIFT_MASK;
            }
            
            if ( (require & CTRL_MASK) !=0)
            {
            	allow |= LEFT_CTRL_MASK + RIGHT_CTRL_MASK;
            }

            if ( (require & ALT_MASK) !=0)
            {
            	allow |= LEFT_ALT_MASK + RIGHT_ALT_MASK;
            }

            validModifiers[index][MUST_HAVE] = require;
            validModifiers[index][CANT_HAVE] = ~(require | allow);
            validModifiers[index++][MODIFIER_INDEX] = modifier;
         }
     }
     return validModifiers;
   }
		
   /** Method to get an attribute from a child element
    *
     * @param parent the parent element
     * @param tag the tag name of the desired child element
     * @param attribute to extract from the child element
     */
   private String getChildAttribute( Element parent, String tag, String attr )
   {  NodeList nodes = parent.getElementsByTagName(tag);
      Element child = (Element)nodes.item(0);
      return child.getAttribute(attr);
   }
	    
   public InputSource resolveEntity(String publicID, String systemID)
                                                            throws SAXException
   {  String sep = "/";
      int lastPart = systemID.lastIndexOf(sep) + 1;
      systemID = systemID.substring(lastPart);
      try
      {   InputStream stream = ImportKeyboard.class.getResourceAsStream
                                        ("/dtd/" + systemID);
          return new InputSource(stream);
      }
      catch (Exception e)
      { throw new SAXException(e.getMessage()); }
   }

   /** Class to eliminate control characters from the XML input */
   class XMLFilterStream extends InputStream
   {    private InputStream stream;
        private StringBuffer buffer;
       
        private final String HEX = "&#x", DECIMAL = "&#";
       
        XMLFilterStream(InputStream stream)
        {  this.stream = stream;
           buffer = new StringBuffer(20000);
        }

        public int read() throws IOException
        {  int data, hexNumber = 0;
           if (buffer.length()==0)
           {  data = stream.read();
              if (data!= '&') { return data; }

              data = stream.read();
              if (data!='#') { buffer.append(data); return (int)'&'; }

              stream.mark(2);
              int max = 6;
              boolean decimal = false;
              data = stream.read();
              if (data !='x' && data !='X')
              {   stream.reset();
                  decimal = true;
                  buffer.append(DECIMAL);
              }
              else buffer.append(HEX);

              StringBuffer stringBuffer = new StringBuffer();
              int count = 0;

              for (count=0; count<max; count++)
              {  stringBuffer.append((char)(stream.read())  );
                 if (stringBuffer.charAt(count) == ';') break;
              }

              String type = (decimal)?"":"x";
              if (stringBuffer.length()<=count || stringBuffer.charAt(count)!=';')
              {  throw new IOException("Illegal character code: &#"
                                            + type + stringBuffer.toString());
              }

              try
              { if (decimal)
                     hexNumber
                        = Integer.parseInt(stringBuffer.substring(0,count));
                else hexNumber
                        = Integer.parseInt(stringBuffer.substring(0,count),16);
              }
              catch (NumberFormatException e)
              {  throw new IOException("Illegal character code: &#"
                                             + type + stringBuffer.toString());
              }
              if (hexNumber >= 0x20)  { buffer.append(stringBuffer);  }
              else
              {  // Convert control characters to unicode control picture range.
                 hexNumber += 0x2400;
                 String hex = "x" + Integer.toHexString(hexNumber) + ';';
                 if (decimal) buffer.append(hex);
                 else buffer.append(hex.substring(1));
              }
           }

           data = buffer.charAt(0);
           buffer.delete(0,1);
           return data;
        }  // End of read()
       
  }  // End of XMLFilterStream class

}   // End of ImportKeyboard class
