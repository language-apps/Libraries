/**
 * DialogFilter.java
 *
 *   @author  harveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.editor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.sound.sampled.AudioSystem;
import javax.swing.filechooser.FileFilter;
import org.acorns.language.LanguageText;

/** Class to filter files for selection of an Audio File
 */
public class AudioDialogFilter extends FileFilter implements FilenameFilter
{
	private String   name;
	private Vector<String> extensions;
  
   /** Filter that detects files with any of a list of extensions
    *
    * @param title Name of this filter
    * @param extensionStrings Array of valid extensions
    */
    public AudioDialogFilter(String title)
    {
       name = getTranslation(title);
       extensions = null;
    }
    
   /** Filter that detects files with any of a list of extensions
    *
    * @param title Name of this filter
    * @param extensionStrings Array of valid extensions
    */
    public AudioDialogFilter(String title, String[] extensionStrings)
    {
       name = getTranslation(title);

       extensions = new Vector<String>();
       for (int i=0; i<extensionStrings.length; i++)
       {   
    	   extensions.addElement(extensionStrings[i]);   
       }
    }
    
   public static String getTranslation(String title)
   {
       String[] translations
               = LanguageText.getMessageList("acornsApplication", 142);
       for (int i=0; i<translations.length; i++)
       {   try
           {  if (translations[i].toLowerCase().equals("audio files"))
              {  title = translations[i]; break; }
           }   catch (Exception e) { title = "File List"; }
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
    @Override
    public boolean accept(File file)
    {
       if (file.isDirectory()) return true;

       // Determine if this is a readable audio file.
       if (extensions==null)
       {  try
          {  AudioSystem.getAudioFileFormat(file);
             return true;
          }
          catch (Exception e) { return false; }
      }
      
      // Look for particular extensions.
       String fileName = file.getName();
      int    lastPeriod = fileName.lastIndexOf(".");
      if (lastPeriod < 0) return false;

      String extension = fileName.substring(lastPeriod + 1);
      if (extension != null)
      {  if (extensions.contains(extension)) return true;  }
      return false;   }
	
   /**
    *  Return the name of this filter
    */
	  public String getDescription()	{  return name; }
}
