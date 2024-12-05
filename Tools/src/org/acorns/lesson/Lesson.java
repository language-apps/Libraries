/*
 * Lesson.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import javax.sound.sampled.*;
import javax.help.*;

import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.audio.*;
import org.acorns.language.*;

/** Abstract class extended by all lesson types that integrate into the
 *  ACORNS software
 */
public abstract class Lesson implements Serializable
{
   /** Java's serial file version */
   private static final long serialVersionUID = 1;	
	
   private   String      name;              // Name for appearing on menus.

   /** layer number. Lessons have access to this variable, but should
    *  use getLayer() instead. The setLayer() method sets the lesson dirty,
    *  so, in this case, it is appropriate to set the layer directly.
    */
   protected int         layer;             // Layer used for this layer.
   /** Name of a lesson to link to. Although lessons have access to this
    *  variable, they should use getLink() instead. The setLink() method sets
    *  the lesson dirty, so, in this case, it is appropriate to set the link
    *  directly.
    */
   protected String      link;              // Link to another lesson.

   /* -------------- Data past this point is transient ------------------- */
   /** Lesson is in setup mode where users enter lesson description data */
   public transient static final int SETUP=0;
   /** Lesson is in play mode where users execute a lesson */
   public transient static final int PLAY=1;
   /** Standard size of the icons */
   public transient static final int ICON = 20;
  
   private transient boolean dirty;        // Indicates if a lesson has changed.
   
   // Redo and undo stacks.
   private transient UndoRedo undoRedo = null;
   private transient AcornsProperties properties = null;
	
   /** Create a new lesson
    *
    * @param name The descriptive name of this lesson. This name displays on
    * the drop down menu when a user creates, or modifies, lesson header data.
    */
   public Lesson(String name)
   {   this.name = name;
       layer     = 1; 
       link      = "";
       dirty     = true; 
       initializeLesson();
   }  // End of constructor.
   
   /** Constructor for converting from version 3.0 */
   public Lesson(String name, String link)
   {
       this.name = name;
       this.link = link;
       dirty     = true;
       layer     = 1;
       initializeLesson();
   }
   
   /*-------------- Methods used to access Acorns properties. --------------*/
   /**  Indicate that this lesson has changed.
    */
   public boolean setFileDirty()
   {   
 	   return properties.setFileDirty(); }
   
   /**  Get an icon ImageIcon object 
    *   @param iconName Icon index (see AcornsProperties symbolic cocnstants).
    *   @param size     Size to scale the icon.
    *   @return icon ImageIcon object or null.
    */
   public ImageIcon getIcon(int iconName, int size)
   {  return properties.getIcon(iconName, size);  }
   
   /** Get a SoundData object of a sound known to Acorns
    *  @param soundName Type of sound. See AcornsProperties symbolic constants.
    *  @return SoundData object or null if the request fails.
    */
   public SoundData getSound(int soundName)
   {  return properties.getSound(soundName); }
 
   
   /** Get size of the display panel.
    *  @return Size of the area where the lesson will display.
    */
   public Dimension getDisplaySize() { return properties.getDisplaySize(); }
   
   /** Get size of the main applicaton frame.
    *  @return Size of the application frame.
    */
   public Dimension getFrameSize() { return properties.getFrameSize(); }
   
   /** Get size of the desktop screen or the size of the applet.
    *  @return Size of the sesktop screen or the size of the applet.
    */
   public Dimension getScreenSize() { return properties.getScreenSize(); }
   
   /** Get help set object
    *  @return help set url or null.
    */
   public HelpSet getHelpSet() { return properties.getHelpSet(); }
   
  /** Get the path to types of files that the user browses to.
    *  @param option type of file (See AcornsProperties symbolic constants)
    */
   public String getPath(int option) { return properties.getPath(option); };

   /** Get a picture using a Chooser dialog.
    *
    * @return The URL locating the picture.
    */
   public URL getPicture() { return properties.getPicture(); };
      
   /** Set the path to a file of a particular type. This is done so users
    *  will not have to browse to find it next time.
    *  @param option type of file (See AcornsProperties symbolic constants)
    *  @param path path to the folder containing the filefile
    */
   public void setPath(int option, String path)
   {  properties.setPath(option, path); }
   
   /** Enable or disable ACORNS menu buttons appropriately if those that
    *  are ghosted change.  */
   public void activateMenuItems() { properties.activateMenuItems(); }
   
   /** Get Header Information for this lesson. See AcornsProperties symbolic
    *  constants.
    *
    *  @return string array containing header information.
    */
   public String[] getLessonHeader() 
   {
       return properties.getLessonHeader();
   }
   
  /** Get speech option array.
    *  @return array of speech options.
    */
   public boolean[] getOptions() {return properties.getOptions(); }
   
   /** Update speech option array.
    *  @param options boolean array of speech options. Refer to symbolic
    *  constants in AcornsProperties.
    */
   public void setOptions(boolean[] options)
   {  properties.setOptions(options);  }
   
   /** Set active lesson based on anchor tag text.
    *  @param hyperlink text to another lesson
    *  @return true if successful, false otherwise
    */
   public boolean setActiveLesson(String hyperlink)
   {  return properties.setActiveLesson(hyperlink); }
      
   /** Set active lesson based on navigation buttons
    *  @param direction false to go towards front, true to go towards back
    *  @param distance  false for adjacent lesson, true to go to end
    */
   public boolean setActiveLesson(boolean direction, boolean distance)
   {  return properties.setActiveLesson(direction,distance); }
   
  /** Redisplay the current lesson.
    */
   public void displayLesson() {properties.displayLesson();}
      
   /** Get application title for display in dialog frames
    *  @return application title
    */
   public String getTitle() { return properties.getTitle(); }
   
   /** Method to get the link to the next lesson
    * 
    * @return link to the next lesson
    */
   public String getLink() { return link; }
   
   /**  Method to set the link to the next lesson
    * 
    * @param link link to the next lesson
    */
   public void setLink(String link) 
   {  setLink(link, true); }

   /**  Method to set the link to the next lesson
    *
    * @param link link to the next lesson
    * @param display true if to redisplay lesson.
    */
   public void setLink(String link, boolean display)
   { 
	   this.link = link;
       setDirty(true);
       if (display) displayLesson();
   }
   
   /** Exit play mode and return to setup mode. */
   public void exitPlayMode() { properties.exitPlayMode(); }   

  /** Map component to the active keyboard 
    *  @param component to map to thhe active keyboard
    */
  public boolean keyMapComponent(JComponent component)
  {  return KeyboardFonts.getLanguageFonts().setFont(component); }

   /*------------------ Methods used by various lessons ----------------------*/   
   
   /** Get the name of this lesson */
   public String getName() { return name.split(";")[0]; }
   
   /** Get name of lesson for display on lesson drop down choices. */
   public String getLanguageName()
   {
       String[] languageName = name.split(";");
       if (languageName.length>1) return languageName[1];
       else return languageName[0];
   }

   /**  Display an error using a text dialog
    *
    * @param text Text to display in the dialog box
    */
   public void setText(String text) 	
   {  
      if (text.length()!=0) 
      {
   	      Frame root = JOptionPane.getRootFrame();
          JOptionPane.showMessageDialog(root, text);
      }
   }

   /** Get root application frame */
   public Frame getRootFrame() { return properties.getRootFrame(); }
  
   /** Get the current layer number. */
   public int getLayer() 
   {return layer;}
   
   /** Set the current layer number. 
    * @param newLayer layer number
    * @return true if successful, false otherwise
    */
   public boolean setLayer(int newLayer) 
   {  
      boolean cookieLayer = false;
      if (newLayer<0)
      {
          cookieLayer = true;
          newLayer    = -newLayer;
      }
      
      if (newLayer<AcornsProperties.MIN_LAYERS) return false;
      if (newLayer>AcornsProperties.MAX_LAYERS) return false;
      if (isPlay()  && isPlayable(newLayer)!=null)   
      {   
          return false;  
      }
      else  
      {
         layer = newLayer;
         if (cookieLayer) return true;
         
         setDirty(true);  
         displayLesson();
      }
      return true;
   }
   
   /** Determine if a lesson is playable  */
   public boolean isPlayable()   
   { 
       String playable = isPlayable(layer);
       if (playable == null) return true;
       if (isPlay())   { Toolkit.getDefaultToolkit().beep(); }
       else setText(playable);
       return false;
   }
   
   /** Determine whether the lesson changed without being saved 
    *
    * @return true if lesson has changed
    */
   public boolean isDirty()  { return dirty; }
   
   /** Determine if this lesson is in play mode.
    *  @return true if yes
    */
   public boolean isPlay()  { return properties.isPlay(); }
	
   /** Mark whether lesson has changed or not
    *
    * @param dirty true if lesson as changed, false otherwise
    */
   public boolean setDirty(boolean dirty)
   {  
	  if (this.dirty==dirty) return true;
      this.dirty = dirty;
      if (dirty==true) return setFileDirty();
      return true;
   }

   /** Initialize the lesson. */
   public final void initializeLesson()
   {
       if (properties==null)
       {
          // Get the ACORNS property change listener.
         properties = AcornsProperties.getAcornsProperties();
       }
   }
   
   /** Determine if there is a lesson ready to paste. Called by ACORNS, not
    *  by lessons.
    * 
    * @return true if pastable, false otherwise
    */
   public static boolean isPastable()
   { return AcornsTransfer.isPastable(AcornsTransfer.LESSONTYPE); }
   
   /** Copy lesson that can be pasted to the clip board. 
    *  Called by ACORNS,not by lessons.
    * 
    * @param lesson save lesson in copy/paste workpad
    */
   public static void copyLesson(Lesson lesson) 
   {  
	  AcornsTransfer transfer = new AcornsTransfer(lesson);
	  transfer.copyObject();
   }
   
   /** Get lesson, which can be copied/pasted (called by ACORNS, not by Lessons)
    * 
    * @return return lesson for copy/paste, or null if one doesn't exist.
    */
   public static AcornsTransfer getCopyLesson()
   { 
	   try
	   {
		   return AcornsTransfer.getCopiedObject(AcornsTransfer.LESSONTYPE);
	   }
	   catch (Exception e) { return null; }
   }
    
   /** Get the stack for redo and undo operations. */
   public UndoRedo getUndoRedoStack()
   {   if (undoRedo==null) undoRedo = new UndoRedo();
       return undoRedo;
   }
   
   /** Method to reset the redo and undo stacks
    */
   public void resetUndoRedo()   // Reset the redo and undo stacks.
   {
      getUndoRedoStack().resetRedoUndo();      
   }

   /** Determine whether the redo stack is empty
    *
    *  @return true if stack is empty
    */
   public boolean isRedoEmpty()    // Return if the redo stack is empty.
   {  return getUndoRedoStack().isRedoEmpty(); }
	
   /** Determine whether the undo stack is empty
    *
    *  @return true if stack is empty
    */
   public boolean isUndoEmpty()
   {    return getUndoRedoStack().isUndoEmpty(); }
	
   /** Process a redo command. Called by ACORNS, not by lessons. */
   public boolean redoCommand()
   {
      if (isRedoEmpty()) return false;
		
       // Remove from redo stack and add to undo stack.
       UndoRedoData data = getUndoRedoStack().redo(getData());
       redo(data);
       return true;
   }
	
   /** Process an undo command. (Called by ACORNS, not by Lessons) */
   public boolean undoCommand()
   {
       if (isUndoEmpty()) return false;
       
      // Remove from undo stack and add to redo stack.
      UndoRedoData data = getUndoRedoStack().undo(getData());
      if (data==null) return false;
	     undo(data);
      return true;
   }
	
   /** Push an undo command onto the undo stack */
   public void pushUndo(UndoRedoData data)
   {  getUndoRedoStack().pushUndo(data); }
	
   
   /** Get name of picture file to be written 
    * 
    * @param directoryName Directory where file should exist
    * @param lesson The lesson number
    * @param image The number of the image
    * @param extension Extension for this file
    * @param exists True if want original file to exist
    * @return file object pointing to the correct file name
    * @throws java.io.IOException
    */
   public File getImageName
           (File directoryName, int lesson, int image, String extension
                , boolean exists)  throws IOException
   {
       PictureData data = getPictureData(image);
       if (data==null) return null;
       
       Vector<String> text = data.getVector();
       if (extension.toLowerCase().startsWith("default"))
       {  
    	   extension = text.get(1); 
    	   if (!extension.toLowerCase().equals("gif"))
    		   extension = "png";
       }
       
       String separator   = System.getProperty("file.separator");
       String filePrefix = "";
       if (directoryName!= null)
       {
          filePrefix  = directoryName.getCanonicalPath() + separator;
       }
			    
       String lessonNo    = "000" + (lesson+1);
       lessonNo = lessonNo.substring(lessonNo.length()-3, lessonNo.length() );
       
       String imageNo = "000000" + image;
       imageNo = imageNo.substring(imageNo.length()-6, imageNo.length());
       
       String code = lessonNo + "_" + imageNo; 
       
       String fileName = filePrefix + "I" + lessonNo + imageNo
			                             + "." + extension;
       
       File file = new File(fileName);                       
       String newName = text.get(0);
       if (newName!=null)
       {
           int lastIndex = newName.lastIndexOf('.');
           newName = newName.substring(0, lastIndex);
           if (newName.matches("^I{1}[-\\d]{9}.*")) return file;
           
           newName = newName.replaceAll("\\d{3}_\\d{6}$", "");
           newName = replaceIllegalXMLCharacters(newName);
           File newFile = new File(filePrefix + newName + code + "." + extension);
           
           if (newFile.exists() || !exists) file = newFile;
       }
       return file;
   }
   
   /** Disallow file names with illegal XML characters when writing/reading */
   private String replaceIllegalXMLCharacters(String name)
   {
	   String invalids = "#%{} ";
	   name = name.trim().replaceAll("%20", "-");
	   name = name.replaceAll("(%23|%25|%7B|%7D)","-");
	   name = name.replaceAll("[" + invalids + "]", "-");
	   return name;
   }
   
   /** Method to determine tha name of the file to write
    * 
    * @param directoryName directory where file should exist
    * @param lesson which lesson this one is (count from 1)
    * @param image which picture the sound is attached to
    * @param layer which lesson layer this sound applies to
    * @param point which x,y point in this picture and layer
    * @param index which sound at this x,y point
    * @param extension file format to use
    * @param exists true if file should exist, false otherwise
    * @return File object containing the file's name
    */
   public File getSoundName
           (File directoryName, int lesson, int image, int layer
                         , Point point, int index, String extension, boolean exists)
                         throws IOException
   {
	   extension = extension.split("~")[0];
       SoundData sound = getSoundData(image, layer, point, index);
       if (sound==null) return null;
       if (!sound.isRecorded()) return null;
       
       String fileName = sound.getSoundText(SoundData.NAME);
       int firstIndex = fileName.lastIndexOf('/');
       fileName = fileName.substring(firstIndex+1);
       File file = null;
       String filePrefix = "";
	   if (fileName.length()==18 && fileName.matches("^S{1}[-\\d]{17}.*")) fileName = "";
	
	   if (directoryName !=null)
       {
           filePrefix = directoryName.getCanonicalPath()
                                + System.getProperty("file.separator");
       }
       if (!fileName.equals(""))
       {
    	   fileName = replaceIllegalXMLCharacters(fileName);
           file = new File(filePrefix + fileName + "." + extension);
           if (!file.exists() && exists) file = null;
       }
       
       if (file == null)
       {
           String lessonNo    = "000" + (lesson+1);
           lessonNo = lessonNo.substring(lessonNo.length()-3, lessonNo.length() );

           String x = "000" + point.x;
           x = x.substring(x.length()-3,x.length());

           String y = "000" + point.y;
           y = y.substring(y.length()-3,y.length());

           String layerNo  = "00" + layer;
           layerNo  = layerNo.substring(layerNo.length()-2, layerNo.length());

           String imageNo = "000" + image;
           imageNo = imageNo.substring(imageNo.length()-3, imageNo.length());

           String indexNo = "000" + index;
           indexNo = indexNo.substring(indexNo.length()-3, indexNo.length());


           fileName = filePrefix + "S" + lessonNo + imageNo
                         + layerNo + x + y + indexNo + "." + extension;
           
           file = new File(fileName);
       }
       return file;
   }
   
   /** Method to write an image for export.
    *  @param data pictureData object for exporting the picture.
    *  @param directoryName file object for destination directory
    *  @param lesson which lesson number (count from zero)
    *  @param image  which image attached to this lesson
    *  @param format extension for this file
    */
   protected void writeImage
           (PictureData data, File directoryName
              , int lesson, int image, String format)  throws IOException
   {   
       File file = getImageName(directoryName, lesson, image, format, false);
       if (file!=null && !file.exists()) data.writePicture(file);       
   }   // End of writeImage()
   
   /** Method to write a sound for export
    *  @param sound recorded sound
    *  @param directoryName file object for destination directory
    *  @param lesson which lesson number (count from zero)
    *  @param image  which image to which this sound is attached
    *  @param layer  which layer within the lesson 
    *  @param point  x and y point positions
    *  @param index  which sound attached to this point
    *  @param format list of default multi-media formats
    */
   protected void writeSound
           (SoundData sound, File directoryName, int lesson
                ,int image, int layer, Point point, int index, String format)
                                    throws IOException, Exception
   {
	   String[] fmts = format.split("~");
	   File file;
	   for (int f=0; f<fmts.length; f++)
	   {
		   file = getSoundName
              (directoryName, lesson+1, image, layer, point, index, fmts[f], false);
		   if (file!=null && !file.exists())  sound.writeFile(file.getAbsolutePath());
	   }
   }    // End of writeSound()
   
   /** Abstract method to get lesson specific panels to display in the
    *  north and center portion of the lesson panel that displays to the 
    *  right of the application frame
    */
   public abstract JPanel[] getLessonData();

   /** Method to call the lesson's save method if it exists */
   public void saveData()
   {
       try
       {
          Class<?> myClass = this.getClass();
          java.lang.reflect.Method method = myClass.getMethod("save");
          method.invoke(this, new Object[0]);
       }
       catch (Exception e)  {}
   }
   

   /** Abstract method to insert a picture into a given lesson
    *
    * @param imageFile URL containing the file with the picture
    * @param scale The scale factor. Lessons use this parameter to scale a
    * picture to the desired size
    * @param angle An angle used by lessons to rotate the picture appropriately
    */
   public abstract void insertPicture(URL imageFile, int scale, int angle)
                            throws IOException;	
   
   /** Abstract method used to remove a picture from a lesson
    *
    * @param pictureNum Which picture to remove, for lessons with more than
    * one picture
    */
   public abstract void removePicture(int pictureNum);
   
   /** Abstract method to get a recorded sound for this lesson 
    * 
    * @param image which image is this sound attached to
    * @param layer which lesson layer this sound is attached to
    * @param point the corresponding x,y point location
    * @param index the index of the sound at the x,y point location
    * @return SoundData object that corresponds to this request
    */
   public abstract SoundData getSoundData(int image, int layer, Point point, int index);
   
   /** Abstract method to get an indicated picture data object.
    * 
    * @param pictureNum which picture to retrieve
    * @return The pictureData object
    */
   public abstract PictureData getPictureData(int pictureNum);
   
   /** Abstract method to indicate if a lesson is ready to execute
    *
    * @return true if lesson can execute
    */
   public abstract String isPlayable(int layer);
   
   /** Abstract method to execute a lesson */
   public abstract JPanel play();
	
   /**
    *  Export images and sounds to a resourse directory
    *
    * @param directoryFile Directory where the program should save image and 
    * sound resource files
    * @param formats The extension indicating sound and image types. 
    * <break/> Environment.SOUND_TYPE is the index to the sound extension
    * <break/> Environment.IMAGE_TYPE is the index to the image extension
    * @param number The lesson number being exported
    */
   public abstract boolean export(File directoryFile, String[] formats
                                   , int number)   throws Exception;
																						  
   /** Import sounds and link data from an export subdirectory
    *
    * @param layer Layer number corresponding to this operation
    * @param point The relative point in the picture. There are 50 
    * horizontal and vertical points within pictures
    * @param fileName File containing sound data
    * @param type of import record (0=link data, 1= sound data)
    */
   // Method to import sounds and links from an export subdirectory.
   public abstract void	importData(int layer, Point point, URL fileName
               , String[] data, int type) throws IOException
               , UnsupportedAudioFileException;
   
   /** Abstract method to process redo commands
    *
    * @param data The data object containing information to process redo 
    * commands
    */
   public abstract void redo(UndoRedoData data);
   /** Abstract method to process undo commands
    *
    * @param data The data object containing information to process undo 
    * commands
    */
   public abstract void undo(UndoRedoData data);
   /** Abstract method to get the current lesson data object
    * @return the lesson data object
    */
   public abstract UndoRedoData getData();
   
   /** Abstract method to convert between program versions
    * 
    * @param version which version to convert from
    * @return true if successful, false otherwise
    */
   public abstract Lesson convert(float version);
   
   /** Abstract method to get play options 
    * 
    * @return array of integer options
    */    
   public abstract int[] getPlayOptions();
   
   /** Abstract method to set play options for lesson 
    *  @param playOptions array of integers defining play options
    */
   public abstract void setPlayOptions(int[] playOptions);

   /** Abstract method to print lesson data using xml
    *
    * @param document The xml document object
    * @param node The node in the document to append lesson data
    * @param directory Path to where to write data (null if just printing)
    * @param extensions Image and sound extension type
    * @return true if print successful, false otherwise
    */
   public abstract boolean print(Document document, Element node
            , File directory, String[] extensions);

   /** Method to add lesson data to an xml document
    *
    * @param doc XML document object
    * @param node XML lesson node to create
    * @param attributes The attribute names
    * @param properties The attribute values
    * @return The created node
    */
    protected Element makeNode(Document doc, String node
                           , String[] attributes, String[] properties)
    {
        Element element = doc.createElement(node);
        for (int i=0; i<attributes.length; i++)
        {  if (!attributes[i].equals("") && !properties[i].equals(""))
               element.setAttribute(attributes[i], properties[i]);  }

        return element;
    }   // End of makeNode() method
    
    protected Element makeSoundNode(Document document, File directory
    		, Point point, Element parent, String extension, SoundData sound
    		, int index, int lessonNo, int layerNo, int imageNo)
    {
        if (!sound.isRecorded()) return null;

        String rate = "", fileName = "";
        try
        {  
           File file = getSoundName
                 ( directory, lessonNo+1, imageNo, layerNo
                      , new Point(point), index, extension, false);
           
           String prefix = "";
           if (directory!=null) prefix = directory.getName() + "/";
           
           fileName = prefix + file.getName().trim();
        }
        catch (IOException ioe) { return null; }

        String[] soundText = sound.getSoundText();
        if (soundText.length>3)  rate = soundText[3];
        int frames = sound.getFrames();
        float playback = sound.getFrameRate();
        try
        {
            AudioFormat fmt = SoundDefaults.createAudioFormat(false);
            float newPlayback = fmt.getFrameRate();
            frames *= newPlayback / Integer.parseInt(rate);
            playback = newPlayback;
        }
        catch (Exception e) {}
 
        String[] soundAttributes = {"src", "rate", "value", "playback", "frames"};
        String[] values = new String[] 
        		{ fileName, rate, "" + imageNo, "" + playback, "" + frames};
        Element soundNode = makeNode(document, "sound", soundAttributes, values);
        parent.appendChild(soundNode);
        return soundNode;
    }

    /** Method to make a point node attaching to an audio
     *
     * @param document The DOM document object
     * @param sound The audio object
     * @param fileName The name of the file source
     * @param point The point to which to attach this point
     * @return The point object created by this method
     */
    protected Element makeSoundPointNode(Document document
            , SoundData sound, String fileName, Point point)
    {
       String[] soundText = sound.getSoundText();
       String   gloss = soundText[SoundData.GLOSS];
       String   spell = soundText[SoundData.NATIVE];
       String   description = soundText[SoundData.DESC];

       String language = "", rate = "";

       if (soundText.length>2)  language = soundText[2];
       if (soundText.length>3)  rate = soundText[3];
       if (language.equals("English")) language = "";
       if (spell.length()==0) language = "";

       int frames = sound.getFrames();
       float playback = sound.getFrameRate();
       try
       {
           AudioFormat fmt = SoundDefaults.createAudioFormat(false);
           float newPlayback = fmt.getFrameRate();
           frames *= newPlayback / Float.parseFloat(rate);
           playback = newPlayback;
       }
       catch (Exception e) {}

       // Create a point element
       String[] pointAttributes = {"x", "y", "type"};
       String[] spellAttributes = {"language"};
       String[] soundAttributes = {"src", "rate", "playback", "frames"};
       String[] values;

       values = new String[] {""+point.x, ""+point.y, "sound"};
       Element pointNode = makeNode( document, "point"
                          , pointAttributes, values);

       // Add gloss, spell, and sound elements to it
       Element glossNode = document.createElement("gloss");
       Text textNode = document.createTextNode(gloss);
       glossNode.appendChild(textNode);
       pointNode.appendChild(glossNode);

       values = new String[] {language};
       Element spellNode = makeNode
              (document, "spell", spellAttributes, values);
       textNode = document.createTextNode(spell);
       spellNode.appendChild(textNode);
       pointNode.appendChild(spellNode);

       if (description.length()!=0)
       {  Element descNode = document.createElement("description");
          textNode = document.createTextNode(description);
          descNode.appendChild(textNode);
          pointNode.appendChild(descNode);
       }

       values = new String[] {fileName, rate, "" + playback, "" + frames};
       if (sound.isRecorded())
       {  Element soundNode = makeNode( document, "sound"
                              , soundAttributes, values);
          pointNode.appendChild(soundNode);
       }
       return pointNode;
    }

    /** Method to create a point with a lesson link
     *
     * @param document The DOM object
     * @param point The point to which to attach this link
     * @param link The name of the link
     * @return The link node created by this method
     */
    protected Element makeLinkPointNode
            (Document document, Point point, String link)
    {
       String[] pointAttributes = {"x", "y", "type"};
       String[] values = {""+point.x, ""+point.y, "link"};

       Element pointNode = makeNode( document, "point"
                              , pointAttributes, values);

       Element linkNode = document.createElement("link");
       pointNode.appendChild(linkNode);

       Text textNode = document.createTextNode(link);
       linkNode.appendChild(textNode);
       return pointNode;
    }

    /** Method to create a font node
     * 
     * @param document The DOM object that will contain the node
     * @param colors The object containing font information
     * @return The fontNode element
     */
    public Element makeFontNode(Document document, ColorScheme colors)
    {
      Color f = colors.getColor(false);
      Color b = colors.getColor(true);
      String fg = f.getRed() + "," + f.getGreen() + "," + f.getBlue();
      String bg = b.getRed() + "," + b.getGreen() + "," + b.getBlue();
      String size = "" + colors.getSize();
      String[] values = new String[]{bg, fg, size, ""};
      String[] fontAttributes = {"background", "foreground", "size"};
      return makeNode(document, "font", fontAttributes, values);
    }
    
    /** Method to create an image node
     * 
     * @param document The DOM object
     * @param directory The directory path (or null)
     * @param extension The extension for image tags
     * @param picture The PictureData object
     * @param values Array of image attribute values for<br>
     *            (value, src*, scale, angle*)<br>
     *        Note: this method fills in src and angle
     * @param lessonNo The number of this lesson in the file
     * @return The created image node
     */
    public Element makeImageNode
            (Document document, File directory, String extension
              , PictureData picture, String[] values, int lessonNo)
    {
        String prefix = "";
        if (directory!=null) prefix = directory.getName() + "/";

        int imageNo = 0;
        try  { imageNo = Integer.parseInt(values[0]); }
        catch (NumberFormatException e) {}

        // Get name of picture
        File   file;

        values[1] = "";
        values[3] = "" + picture.getAngle();
        try  
        {  file = getImageName(directory, lessonNo, imageNo, extension, false);
           values[1] = prefix + file.getName().trim();
        }
        catch (IOException e)  {}

        String[] imageAttributes = {"value", "src", "scale", "angle"};
        Element imageNode = makeNode
                   (document, "image", imageAttributes, values);
        return imageNode;
    }

    public void makeListOfPointNodes
            (Document doc, File directory, Vector<SoundData> soundVector
                         , Point point, Element parent, String extension
                         , int lessonNo, int layerNo, int imageNo)
    {
        String prefix = "", fileName = "";
        if (directory!=null) prefix = directory.getName() + "/";

        SoundData sound;
        File      file;
        Element   pointNode;
        for (int i=0; i<soundVector.size(); i++)
        {   sound = (SoundData)soundVector.elementAt(i);

            try
            {  file = getSoundName
                 ( directory, lessonNo+1, imageNo, layerNo
                      , new Point(point), i, extension, false);

               if (file!=null) fileName = prefix + file.getName().trim();
            }
              catch (IOException ioe)
              {}

              pointNode = makeSoundPointNode(doc, sound, fileName, point);
              parent.appendChild(pointNode);

        }  // End of for to loop through sound vector.
    }      // End of MakeListOfPointNodes()

   /** Convert a color string from "r,g,b" to an arrahy of integers
    *  @param color string to parse to extract colors
    *  @return instantiate color object
    */
   public Color getColor(String color)
   {   String[] rgb = color.split(",");

       try
       {
          int r = Integer.parseInt(rgb[0]);
          int g = Integer.parseInt(rgb[1]);
          int b = Integer.parseInt(rgb[2]);

          if (r<0 || r>255) return null;
          if (g<0 || g>255) return null;
          if (b<0 || b>255) return null;
          return new Color(r,g,b);
      }
      catch (Exception e) { return null; }
   }  // End of getColor()

}          // End of Lesson class.
