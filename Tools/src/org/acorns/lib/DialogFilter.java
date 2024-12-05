/**
 * DialogFilter.java
 *
 *   @author  harveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.sound.sampled.AudioSystem;
import javax.swing.filechooser.FileFilter;

import org.acorns.language.LanguageText;

/** Class to filter files for various kinds of FileChooser dialogs
 */
public class DialogFilter extends FileFilter implements FilenameFilter
{
   private Vector<String> extensions;
	private String   name;
   private static String[] types =
   { "audio files", "jar files", "image files", "web page files",
     "export files", "import files", "dictionary files", "acorns files" };

   /**
    * Instantiate a filter that detects files with a "lnx" extension
    */
   public DialogFilter() 
	{   extensions = new Vector<String>();
	    extensions.addElement("lnx");
            name = getTranslation("ACORNS Files");
	}
   
      
   /** Filter that detects audio files that can be read.
    * 
    * @param title Name of this filter
    */
   public DialogFilter(String title)
   {
       if (title!=null && title.equalsIgnoreCase("dictionary"))
       {   title = "Dictionary Files";
           extensions = new Vector<String>();
           extensions.addElement("adct");
       }
       else  extensions = null;
       name = getTranslation(title);
   }
   
  
   /** Filter that detects files with any of a list of extensions
    *
    * @param title Name of this filter
    * @param extensionStrings Array of valid extensions
    */
    public DialogFilter(String title, String[] extensionStrings)
    {
       extensions = new Vector<String>();
       for (int i=0; i<extensionStrings.length; i++)
       {   extensions.addElement(extensionStrings[i]);   }
       name = getTranslation(title);
    }

   public static String getTranslation(String title)
   {
       String[] translations
               = LanguageText.getMessageList("acornsApplication", 142);
       for (int i=0; i<types.length; i++)
       {   try
           {  if (title.toLowerCase().equals(types[i]))
              {  title = translations[i]; break; }
           }   catch (Exception e) { break; }
       }
       return title;
   }
   
   @Override public String toString()
   {
	   if (extensions == null)
		   return "";
	   
	   StringBuffer result = new StringBuffer();
	   boolean first = true;
	   for (String extension: extensions)
	   {
		   if (!first) result.append(";");
		   result.append("*." + extension);
		   first = false;
	   }
	   return result.toString();
   }
   
   /** Ignore the string and call the more general accept method */
   @Override public boolean accept(File directory, String file)
   {
	   File path = new File(directory, file);
	   return accept(path);
   }
	
   /* Determine if a file passes the filter
    *
    * @param file File object to filter
    *
    * @return true if this file passes the filter check
    */
    public boolean accept(File file)
    {
       if (file.isDirectory()) return true;

       String fileName = file.getName();
      
       // Determine if this is a readable audio file.
       if (extensions==null)
       {  try
          {  AudioSystem.getAudioFileFormat(file);
             return true;
          }
          catch (Exception e) { return false; }
      }
      
      // Normal looking for particular extensions.
      int    lastPeriod = fileName.lastIndexOf(".");
      if (lastPeriod < 0) return false;

      String extension = fileName.substring(lastPeriod + 1);
      if (extension != null)
      {  if (extensions.contains(extension)) return true;  }
      return false;
   }
	
   /**
    *  Return the name of this filter
    */
	  public String getDescription()	{  return name; }
}
