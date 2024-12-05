/**
 *
 *   @name Status.java
 *      This class controls the paths to .keylayout and .ttf files
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.acorns.audio.SoundDefaults;

/** Class to maintain application parameters in a persistent way */
public class Status
{
   public static String keyLayoutPath, ttfPath;

   public static void readStatus()
   {  
	  String defaultPath = SoundDefaults.getDataFolder();
      String separator = System.getProperty("file.separator");

      String keyDefaultPath = defaultPath +  separator + "keyLayoutFiles";
      String fontDefaultPath = defaultPath + separator + "Fonts";

      String dirName = SoundDefaults.getHomeDirectory();
      ObjectInputStream ois = null;

      try
      {  
    	 String settings = dirName + separator + "keylayouts";
         FileInputStream fis = new FileInputStream(settings);
         BufferedInputStream bis = new BufferedInputStream(fis);
         ois = new ObjectInputStream(bis);
         keyLayoutPath = (String)ois.readObject();
         if (keyLayoutPath==null) keyLayoutPath = keyDefaultPath;
         ttfPath = (String)ois.readObject();
         if (ttfPath==null) keyLayoutPath = fontDefaultPath;

         File openFile = new File(keyLayoutPath);
         if (!openFile.exists()) { keyLayoutPath = keyDefaultPath;  }
         openFile = new File(ttfPath);
         if (!openFile.exists()) { ttfPath = fontDefaultPath; }
      }
      catch (Exception ioe)  
      {  
    	 keyLayoutPath = keyDefaultPath;
         ttfPath = fontDefaultPath;  
      }
      try {ois.close(); }catch (Exception ex) {}

   }  //End of readStatus()

   /** Method to write the application parameters */
   public static void writeStatus()
   {  
 	 String dirName = SoundDefaults.getHomeDirectory();
     String separator = System.getProperty("file.separator");
     String settings = dirName +  separator + "keylayouts";
     ObjectOutputStream   oos = null;
     FileOutputStream     fos = null;
     BufferedOutputStream bos = null;
      try
      {  
         fos = new FileOutputStream(settings);
         bos = new BufferedOutputStream(fos);
         oos = new ObjectOutputStream(bos);
         oos.writeObject(keyLayoutPath);
         oos.writeObject(ttfPath);
      }
      catch (Exception exception)  {}
      try { oos.close(); bos.close(); fos.close(); } catch (Exception e) {}
      
   }  //End of writeStatus()

   /** Method to get the path to the default folder
    *
    * @param ttf true for path to .ttf files, false to .keylayout path
    * @return
    */
   public static File getDefaultPath(boolean ttf)
   {  
	   String path = SoundDefaults.getDataFolder();
    	   
       if (ttf) path +=  File.separator + "Fonts";
       else path += File.separator + "keyLayoutFiles";
       File pathFile = new File(path);
       return pathFile;
   }
 
  /** Setter to set path to where key layout files reside */
  public static void setPath(File filePath, boolean ttf)
  {  String path;
     try
     {  path = filePath.getCanonicalPath();
        if (ttf) ttfPath = path;
        else keyLayoutPath = path;
     }
     catch (Exception e) {}
  }

  /** Getter method to return path to keylayout files */
  public static File getPath(boolean ttf)
  { if (ttfPath==null || keyLayoutPath==null) readStatus();
    if (ttf) return new File(ttfPath);
    else     return new File(keyLayoutPath); }
}    // End of Status class
