/*
 * AcornsProperties.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.beans.*;
import org.acorns.data.*;
import javax.help.*;

/**
 *  Abstract class with methods that interact with the ACORNS application
 */
public abstract class AcornsProperties implements PropertyChangeListener
{
  /** Default size for icons to be displayed */
  public static final int ICON_SIZE = 20;

  /** Offset of sound into export/import sound extension */
   public static final int SOUND_TYPE=0;
  /** Offset of sound into export/import sound extension */
   public static final int IMAGE_TYPE=1;
   
   /* Types of import records */
   /** Importing a link to another record */
   public static final int LINK=0;
   /** Importing a recorded sound point */
   public static final int SOUND=1;
   /** Importing a foreground/background color combination */
   public static final int FONT=2;
   /** Importing a layer */
   public static final int LAYER=3;
   /** Importng a picture */
   public static final int PICTURE = 4;
   /** Importing a Parameter record */
   public static final int PARAM = 5;
   /** Importing a category record */
   public static final int CATEGORY = 6;

   /** Offset of sound on/off option in system option array */
   public static final int SPEECH = 0;
   /** Offset of native spelling on/off option in system option array */
   public static final int SPELLING = 1;
   /** Offset of gloss translation on/off option in system option array */
   public static final int GLOSS = 2;
   /** Offset for display gloss or indigenous */
   public static final int  DISPLAY = 3;
   /** Offset for select gloss or indigenous    */
   public static final int SELECT = 4;
   
   /** Number of boolean options known by ACORNS */
   public static final int MAX_OPTIONS = 6;
   
   /** Offset of lesson type in array of lesson header string */
   public final static int TYPE=0;
   /** Offset of lesson title in array of lesson header string */
   public final static int TITLE=1;
   /** Offset of lesson name in array of lesson header string */
   public final static int NAME=2;
   /** Offset of lesson description in array of lesson header string */
   public final static int DESC=3;
   /** offset to names for each layer */
   public final static int LAYERNAMES = 4;
   /** Minimum layer number */
   public final static int MIN_LAYERS = 1;
   /** Maximum layer number */
   public final static int MAX_LAYERS = 10;
   /** Length of lesson header string */
   public final static int TYPES = LAYERNAMES + MAX_LAYERS;
   
   /** Option indicating to paths to image files */
   public  static final int PICTURES=0;
   /** Option indicating to paths to audio files */
   public  static final int SOUNDS=1;
   /** Option indicating to paths to open ACORNS files */
   public  static final int OPEN=2;
   /** Option indicating to paths to close ACORNS files */
   public  static final int SAVE=3;
   /** Option indicating paths to videos */
   public  static final int VIDEO=4;
   /** Number of file type paths */
   public  static final int PATHS=5;

   /** acorn icon */
   public static final int ACORN=0;
   /** anchor hyperlink icon */
   public static final int ANCHOR=1;
   /** audio record icon */
   public static final int RECORD=2;
   /** audio playback icon */
   public static final int PLAY=3;
   /** stop playback or record icon */
   public static final int STOP=4;
   
   /** icon for browsing to files */
   public static final int BROWSE = 5;
   /** check mark icon for active windows */
   public static final int TICK =6;
   /** blank space icon */
   public static final int BLANK=7;
   /** image rotate icon */
   public static final int ROTATE=8;
   
   /** icon to goto next layer */
   public static final int UP=9;
   /** icon to goto previous layer */
   public static final int DOWN=10;
   /** icon for drop down menu of information */
   public static final int INFO=11;
   /** icon to navigate to first lesson */
   public static final int FIRST=12;
   /** icon to navigate to last lesson */
   public static final int LAST=13;
   /** icon to navigate to previous lesson */
   public static final int PREVIOUS=14;
   /** icon to navigate to the next lesson */
   public static final int NEXT=15;
   
   /** icon to zoom into an image */
   public static final int ZOOMIN=16;
   /** icon to zoom out of an image */
   public static final int ZOOMOUT=17;
   /** icon for requesting help */
   public static final int HELP=18;
   
   /** icon to close out of an option */
   public static final int CLOSE=19;
   /** icon to trigger printing */
   public static final int PRINT=20;
   /** icon for background color */
   public static final int BACKGROUND=21;
   /** icon for foreground color */
   public static final int FOREGROUND=22;
   /** icon for sound editor */
   public static final int SOUNDEDITOR=23;
   /** icon for web page creator */
   public static final int WEBPAGE=24;
   /** icon for copy */
   public static final int COPY=25;
   /** icon for paste */
   public static final int PASTE=26;
   /** icon for left justify */
   public static final int LEFT=27;
   /** icon for right justify */
   public static final int CENTER=28;
   /** icon for importing a picture */
   public static final int IMAGE=29;
   /** icon to see the correct answers */
   public static final int ANSWERS=30;
   /** icon to check if the answer is correct */
   public static final int CHECK=31;
   /** icon to replay part of an audio */
   public static final int REPLAY=32;
   /** icon to move object to the right */
   public static final int MOVE_RIGHT=33;
   /** icon to move object to the left */
   public static final int MOVE_LEFT=34;
   /** icon to make a random choice */
   public static final int RANDOM=35;
   /** icon to pause audio play back */
   public static final int PAUSE = 36;
   /** icon to slow down audio */
   public static final int SLOW = 37;
   /** icon to speed up audio */
   public static final int FAST = 38;
   /** Icon for entering special characters */
   public static final int STAR = 39;

   /** sound for correct response */
   public static final int CORRECT=0;
   /** sound for incorrect response */
   public static final int INCORRECT=1;
   /** sound for mispelling response */
   public static final int SPELL=2;
   
   /** Offset to lesson Controls panel */
   public static final int CONTROLS = 0;
   /** Offset to lesson display panel panel */
   public static final int DATA = 1;
   
   /** Get the current Acorns property listener
    * 
    * @return AcornsProperties object, or null if not found.
    */
   public static AcornsProperties getAcornsProperties()
   {      PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                                 .getPropertyChangeListeners("Acorns");
          for (int i=0; i<pcl.length; i++)
          {   try
              {   return (AcornsProperties)pcl[i];
              }
              catch (ClassCastException ex) {}
          }
          return null;
   }      // End of getAcornsProperties   
     
  /**  Indicate that this lesson has changed.
   *   @return false if no file is open.
   */
   public abstract boolean setFileDirty();
   
   /**  Get an icon ImageIcon object 
    *   @param iconName Name of the icon name.
    *   @param size     Size to scale the icon.
    *   @return icon ImageIcon object or null.
    */     
  public abstract ImageIcon getIcon(int iconName, int size);
   
   /** Get a SoundData object
    *  @param soundName Name of the desired sound.
    *  @return SoundData object.
    */
   public abstract SoundData getSound(int soundName);
  
   /** Get size of the display panel.
    *  @return Size of the area where the lesson will display.
    */
   public abstract Dimension getDisplaySize();
   
   /** Get size of the main applicaton frame.
    *  @return Size of the application frame.
    */
   public abstract Dimension getFrameSize();
   
   /** Get size of the desktop screen or the size of the applet.
    *  @return Size of the desktop screen of the size of the applet.
    */
   public abstract Dimension getScreenSize();

   /** Get help set HelpSet
    *  @return help set url or null.
    */
   public abstract HelpSet getHelpSet();
   
   /** Enable or disable menu buttons appropriately.
    */
   public abstract void activateMenuItems();   
   
  /** Get Header Information for this lesson.
    *  @return string array containing header information.
    */
   public abstract String[] getLessonHeader();
   
   /** Get the path to types of files that the user browses to.
    *  @param option type of file
    */
   public abstract String getPath(int option);
      
   /** Set the path to a file accessed by the user.
    *  @param option type of file
    *  @param path path to the folder containing the file
    */
   public abstract void setPath(int option, String path);
   
   /** Determine if this is an applet executing.
    *  @return true if applet, false otherwise
    */
   public abstract boolean isApplet();

   /** Determine if this is an we are in play mode.
    *  @return true if applet, false otherwise
    */
   public abstract boolean isPlay();

   /** Get root frame (play frame or application frame)
    * 
    * @return the visible frame
    */
  public abstract Frame getRootFrame();
   
  /** Get speech option array.
   *  @return array of speech options.
   */
   public abstract boolean[] getOptions();
   
   /** Update speech option array.
    *  @param options array of speech options
    */
   public abstract void setOptions(boolean[] options);
   
   /** Set active lesson based on anchor tag text.
    *  @param hyperlink text to another lesson
    */
   public abstract boolean setActiveLesson(String hyperlink);
   
   /** Set active lesson based on navigation buttons
    *  @param direction false to go towards front, true to go towards back
    *  @param distance  false for adjacent lesson, true to go to end
    */
   public abstract boolean setActiveLesson
           (boolean direction, boolean distance);
   
   /** Redisplay the current lesson.
    */
   public abstract void displayLesson();
   
   /** Save KeyboardFonts
   
   
   /** Get application title for display in dialog frames
    *  @return application title
    */
   public abstract String getTitle();
   
   /** Exit play mode and return to setup mode. */
   public abstract void exitPlayMode();

   /** Load a picture using a fileChooser dialog
    *
    */
   public abstract URL getPicture();
  
}
