 /**
  * Annotation.java
  *
  *   @author  HarveyD
  *   @version 3.00 Beta
  *
  *    Copyright 2007-2015, all rights reserved
  */

package org.acorns.audio;

import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.awt.*;
import org.acorns.data.*;
import org.acorns.properties.*;

/** Object to handle import and export operations */
public class Annotation
{
   // Offsets to lines relating to points.
   public final static int X=1, Y=5, TYPE=10, DATA=16, SOUND=77;
      
   // Other offsets.
   public final static int LAYER=6, HEADER=24, FIELD=60;
      
   private OutputStreamWriter out;
   private String endOfLine;
   private String spaces = "                              ";
   
   /** Create xml object */
   public Annotation() 
   {      endOfLine = System.getProperty("line.separator");  }
  
   /** Create an xml file for the sound object
    *
    * @param fileName Name of the destination file
    * @param sound Sound data object to create annotation xml.
    */
   public boolean makeXML(File fileName,AnnotationData sound) throws IOException
   {
       if (sound==null || fileName==null) return false;
       
       // Get the annotated text.
       String[] annotations = sound.getSoundText();
            
       // Instantiate the filewriter after determining the desired file name
       String realName  = fileName.getCanonicalPath();
       String soundName = fileName.getName();
       int lastIndex = realName.lastIndexOf(".");
       realName = realName.substring(0,lastIndex) + ".xml";

       FileOutputStream fileOut
               = new FileOutputStream(realName);
       out = new OutputStreamWriter(fileOut, "UTF8");

       // Declare array for attribute name declarations.
       String[] attributeValues = new String[3];
       
       // Start creating the xml output.
       try
       {
         // Generate the xml for this file.
         startDocument();
         String[] version = { "version" };
         attributeValues[0] = "2.00 beta";
         startElement(0, "annotation", version, attributeValues, true);

         // The first layer has the sound data.
         String[] layer = { "value" };
         attributeValues[0] = "0";
         startElement(1, "layer", layer, attributeValues, true);
         attributeValues[0] = "0";
         attributeValues[1] = "0";
         makePoint(attributeValues, annotations, soundName);
         endElement(1, "layer");
         
         // Make all the annotation layers
         makeLayers(sound, attributeValues, soundName);
         
         endElement(0, "annotation");
         endDocument();    
       }
       catch (Exception ex)  // CLose the file if problems encountered.
       {  out.close(); return false;  }
         
      // Close the output.         
      out.close();
      return true;
   }  // End of makeXML method.
   
   //---------------------------------------------------------------------------
   // Method to create a point
   //---------------------------------------------------------------------------
   private void makePoint
           (String[] attributeValues, String[] annotations, String soundName)
                                                   throws IOException
   {
         String[] points = {"x", "y", "type"};
         attributeValues[2] = "Sound";
         startElement(2, "point", points, attributeValues, true);
       
         // Process the gloss tag
         startElement(3, "gloss", null, null, false);
         characters(0, annotations[0]);
         endElement(0, "gloss");
         
         // Process the spell tag
         attributeValues[0] = annotations[2];
         String[] spellNames = { "language" };
         startElement(3, "spell", spellNames, attributeValues, false);
         characters(0,annotations[1]);
         endElement(0, "spell");
                  
         // Process the sound tag
         String[] soundNames = { "src" };
         attributeValues[0] = soundName;
         startElement(3, "sound", soundNames, attributeValues, false );
         endElement(0, "sound");

         // Close the open tags
         endElement(2, "point");
   }
   
   //---------------------------------------------------------------------------
   // Method to create a point
   //---------------------------------------------------------------------------
   private void makeLayers(AnnotationData annotationData
                                , String[] attributeValues, String soundName) 
                                                 throws IOException
   {   
      // Output the annotation information for each point.
      AnnotationNode[] nodes;
      String[] annotations = {"", "", ""};
 
      int lastIndex = soundName.lastIndexOf('.');
      String extension = soundName.substring(lastIndex+1);

      for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
      {
         nodes = annotationData.getScaledAnnotationNodes(i, extension);
         if (nodes!=null && annotationData.getAnnotationCount(i)!=0)
         {
             String[] layer = { "value", "language" };
             attributeValues[0] = "" + (i+1);
             attributeValues[1] = annotationData.getKeyboard();
             startElement(1, "layer", layer, attributeValues, true);
             
             for (int n=0; n<nodes.length; n++)
             {  if (nodes[n]!=null)
                {  if (n==0) 
                   {
                     attributeValues[0] = "" + nodes[n].getOffset();
                   }
                   else
                   {  
                      attributeValues[1] = "" + nodes[n].getOffset();
                      annotations[1] = nodes[n].getText();
                      if (!annotations[1].equals(""))
                      {   makePoint(attributeValues, annotations, null);  }
                      attributeValues[0] = attributeValues[1];
                   }     // End if first annotation
                }        // End if this annotation is null
             }        // End for each annotation at a level
             endElement(1, "layer");
             
         }           // End if annotation level not null          
 
      }              // End for each annotation level
   }  // End of makeLayers()     

   //---------------------------------------------------------------------------
   // Methods to help with the xml processing
   //---------------------------------------------------------------------------
   private void startElement(int indent, String name
          , String[] names, String[] values, boolean skip) throws IOException
   {
      if (indent>0) emit(spaces.substring(0,indent));
      emit("<" + name);
      
      // Handle the attributes.
      String attribute;
      if (names != null)
      {  for (int i=0; i<names.length; i++)
         {
            if (values[i]!=null && !values[i].equals(""))
            {
               emit(" ");
               attribute = addSpecialChars(values[i].trim());
               emit(names[i] + "=\"" + attribute + "\"");
            }
         }
      }        // End of handling attributes.
      emit(">");
      if (skip) endOfLine();
      
   }  // End of startElement method.
   
   private void endElement(int indent, String name)throws IOException
   {  if (indent>0) emit(spaces.substring(0,indent));
      emit("</" + name + ">");
      endOfLine();
   }
   
   private void startDocument() throws IOException
   {  emit("<?xml version='1.0' encoding='UTF-8'?>");  emit(endOfLine); }
   
   private void endDocument() throws IOException
   {   emit(endOfLine); out.flush(); }

      private void emit(String output) throws IOException  
   { 
      out.write(output);  out.flush(); 
   }
      
   // End the line.  
   private void endOfLine() throws IOException   {  out.write(endOfLine); }   
  

   //---------------------------------------------------------------------------
   // Method to output the content. 
   //---------------------------------------------------------------------------
   private void characters(int indent, String buffer) throws IOException
   {
      if (indent>0) emit(spaces.substring(0,indent));
      String outData = addSpecialChars(buffer.trim());
      emit(outData);
   }
   
   /** Add XML special characters
    *  @param str String without special characters
    *  @return string with special characters for <, >, &, ", and '
    */
   private String addSpecialChars(String str)
   {   
       if (str.indexOf('&')>=0)  str = str.replaceAll("&", "&amp;");
       if (str.indexOf('<')>=0)  str = str.replaceAll("<", "&lt;");
       if (str.indexOf('>')>=0)  str = str.replaceAll(">", "&gt;");
       if (str.indexOf('\'')>=0) str = str.replaceAll("\'", "&apos;");
       if (str.indexOf('\"')>=0) str = str.replaceAll("\"", "&quot;");
       return str;
   }
      
   /** Import xml data and create a new ACORNS file from it
    *
    * @param fileName file name containing XML data
    */
   public AnnotationData importXML(File fileName) throws Exception
   {
       String name = fileName.getName();
		     String realName = fileName.getCanonicalPath();
 	     int lastIndex = realName.lastIndexOf(name);
	      if (lastIndex<0) throw new SAXException();
       String path = realName.substring(0,lastIndex-1) + "/";

       lastIndex = realName.lastIndexOf(".");
       if (lastIndex<0) lastIndex = realName.length(); 
       realName = realName.substring(0,lastIndex) + ".xml";
       
       AnnotationData sound = new AnnotationData(null, null);
       DefaultHandler handler = new readXML(path, sound);
       SAXParserFactory factory = SAXParserFactory.newInstance();
       SAXParser saxParser = factory.newSAXParser();
      
       saxParser.parse(new File(realName), handler);       
       return sound;
   }
}  // End of xml class.

//-----------------------------------------------------------
// Start of class to import an xml file.
//-----------------------------------------------------------
class readXML extends DefaultHandler
{  String[]   attributes;   // Annotation attributes.
   int        parseLayer=0; // Determine the nesting.
   boolean    glossFlag = false;
   boolean    spellFlag = false;
 	
   private AnnotationData sound;  // Sound Object that we are importing
   private int          layer;    // Current annotation layer number
   private Point        point;    // Current point coordinates
   private String       path;
   private StringBuffer chars;
   private int          sampleRate;
   
	  public readXML(String path, AnnotationData sound)  throws SAXException
   {  this.path = path;
      this.sound = sound;
      this.chars = new StringBuffer();
      
      attributes = new String[]{"", "", "", "", ""};
      layer = 0;
   }
	
   //----------------------------------------------------------------------
   // Content handler methods; these are needed to parse the xml.
   // We could eliminate the ones that aren't used.
   // They are left for future reference for other DefaultHandler classes.
   //----------------------------------------------------------------------
   public @Override void startDocument() throws SAXException  {}
   public @Override void endDocument() throws SAXException
   {	   if (parseLayer != 0) throw new SAXException();   
        sound.setFrameRate();
        sound.setAnnotationLevel(0);
   }
   
   public void getCharacters()
   {
      String retVal = chars.toString().trim();
      chars.setLength(0);
      if (layer==0)
      {    if (glossFlag) attributes[0] = retVal;
           if (spellFlag) attributes[1] = retVal;
      }
      else
      {
          if (spellFlag) sound.insertAnnotation(retVal, point, sampleRate);
      }
      glossFlag = spellFlag = false;
   }
   
   public @Override void startElement(String namespaceURI, String sName, String qName
                               , Attributes attrs)   throws SAXException
   {
      String name = sName;
      if (name.equals("")) name = qName;
      if (name.equals("annotation")) processAnnotation(attrs);
      if (name.equals("layer"))      processLayer(attrs);
      if (name.equals("point"))      processPoint(attrs);
      if (name.equals("gloss"))      processGloss(attrs);    
      if (name.equals("spell"))      processSpell(attrs);
      if (name.equals("sound"))      processSound(attrs);
      parseLayer++; 
      
   }  // End of startElement    

   //-------------------------------------------------------------
   // Method to get the attribute name.
   //-------------------------------------------------------------         
   public String getAttributeName(Attributes attrs, int i) throws SAXException
   {  
      String attributeName = attrs.getLocalName(i);
      if (attributeName.equals("")) attributeName = attrs.getQName(i);
      return attributeName;
   }
   
   //-------------------------------------------------------------      
   // Methods to process the different element types.
   //-------------------------------------------------------------
   public void processAnnotation(Attributes attrs) throws SAXException
   {
       String version = "";
       String   name;
      
      if (parseLayer!=0 || attrs==null) throw new SAXException();
      for (int i=0; i<attrs.getLength(); i++)
      {   name = getAttributeName(attrs, i);
          if (name.equals("version"))  version = attrs.getValue(i);
      }
      char v = version.charAt(0);
      if (!(v=='2' || v=='3')) 
            throw new SAXException();      
   }  // End of processAcorn().
   
   public void processLayer(Attributes attrs) throws SAXException
   {
       if (parseLayer!=1) throw new SAXException();
       parseLayer = 0;
       
       String name, language = "English";
       for (int i=0; i<attrs.getLength(); i++)
       {
           name = getAttributeName(attrs, i);
           if (name.equals("value")) layer=Integer.parseInt(attrs.getValue(i));
           if (name.equals("language")) language = attrs.getValue(i);
       }    
       
       if (layer>0)
       {   sound.setAnnotationLevel(layer-1);
           sound.setKeyboard(language);
       }
   }      // End of processLayer
   
   public void processPoint(Attributes attrs) throws SAXException
   {  if (parseLayer!=1) throw new SAXException();  
      
      String name;
      point = new Point(0,0);
      
     for (int i=0; i<attrs.getLength(); i++)
      {
          name = getAttributeName(attrs, i);
          if (name.equals("x"))  point.x = Integer.parseInt(attrs.getValue(i));
          if (name.equals("y"))  point.y = Integer.parseInt(attrs.getValue(i));
      }      
   }

   public void processGloss(Attributes attrs) throws SAXException
   {
      if (parseLayer!=2) throw new SAXException();      
      glossFlag = true;
   }

   public void processSpell(Attributes attrs) throws SAXException
   {
      if (parseLayer!=2) throw new SAXException();
   
      String name, language = "English";
      for (int i=0; i<attrs.getLength(); i++)
      {   name = getAttributeName(attrs, i);
          if (name.equals("language"))  language = attrs.getValue(i);
          sound.setKeyboard(language);
      }      
   
      if (layer==0) attributes[2] = language;
      spellFlag = true;
   }

   public void processSound(Attributes attrs) throws SAXException
   {
      if (parseLayer!=2) throw new SAXException();
      
     String name, fileName = null;
     for (int i=0; i<attrs.getLength(); i++)
      {   name = getAttributeName(attrs, i);
          if (name.equals("src"))  fileName =  attrs.getValue(i);
      } 
     
      try
      {   if (fileName!=null)  
          {   sound.setSoundText(attributes);
              File file = new File(path + fileName);
              sampleRate = SoundIO.getSoundSampleRate(file);
              sound.readFile(file); }   
    }   catch(Exception e)   { throw new SAXException(); }
		 }

   public @Override void endElement(String namespaceURI, String sName, String qName)
                             throws SAXException
   {
      String name = sName;
      if (name.equals("")) name = qName;
   
      getCharacters();
      parseLayer--;
      if (name.equals("annotation") && parseLayer!=0) throw new SAXException();
      if (name.equals("layer"))   
      {  if (parseLayer!=0 && parseLayer!=1) throw new SAXException(); 
         else parseLayer = 1;
      }
      if (name.equals("point")      && parseLayer!=1) throw new SAXException();
      if (name.equals("gloss")      && parseLayer!=2) throw new SAXException();      
      if (name.equals("spell")      && parseLayer!=2) throw new SAXException();
      if (name.equals("sound")      && parseLayer!=2) throw new SAXException();
   }
  
   /** Method to append the parsed characters received to an input string
    * @param buf
    * @param offset
    * @param len
    * @throws org.xml.sax.SAXException
    */
   public @Override void characters(char[] buf, int offset, int len) 
                                               throws SAXException
   {   chars.append(buf, offset, len);   }
   
  
}     // End of Annotation class
