/**
 * CategoryMultiplePictures.java
 * @author HarveyD
 * @version 6.00
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.categories.multiplepictures;

import java.awt.*;

import javax.swing.*;
import java.util.*;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;
import javax.sound.sampled.*;
import java.lang.reflect.*;

import org.acorns.lesson.*;
import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.widgets.*;
import org.acorns.language.*;

/** Abstract parent class to lessons with multiple pictures. Each of
 *  these lesson need to fill in the play method.
 */
public abstract class CategoryMultiplePictures 
                              extends Lesson implements Serializable
{
   /** Java file version */
   private static final long serialVersionUID = 1;

   /** Initial size of array of pictures */
   public static final int  ARRAY_SIZE = 16;

   /** Maximum number of pictures a lesson can hod */
   public static final int  MAX_PICTURES = 999;

   /** The default size of picture buttons */
   public static final Dimension  PICTURE_SIZE = new Dimension(402,275);
   public static final Dimension  MAX_PICTURE = new Dimension(500,500);

   private PictureChoice[] choices;
   private int             count;
   private ColorScheme     colors;

   private transient PicturesSetUpPanel setup;
   private transient int selected;
   private transient int copyLayer = -1;

   /** Constructor to initialize the data structure
    *
    * @param lessonName The name of the lesson viewed by the drop down menu
    *  of lesson choices.
    */
   public CategoryMultiplePictures(String lessonName)
   {
      super(lessonName);

      choices = new PictureChoice[ARRAY_SIZE];
      setup  = null;
      count    = 0;   // PictureChoice count: 0...n
      colors = new ColorScheme(new Color(130,130,130), Color.white);
      selected = copyLayer = -1;

   }  // End CategoryMultiplePictures()

   /** Method to convert from an older to current version
    *
    * @param lesson The older version Lesson object
    * @param lessonName The name of this lesson type
    */
   public CategoryMultiplePictures(Object lesson, String lessonName)
   {
       super(lessonName);
       Class<?> lessonClass = lesson.getClass();
       Object[] params = new Object[0];
       try
       { Method getPictureDataMethod = lessonClass.getMethod
                  ("getPictureData", new Class[0]);
         choices = (PictureChoice[])getPictureDataMethod.invoke
                       (lesson, params);
       }
       catch (Exception e)
       {      choices = new PictureChoice[ARRAY_SIZE]; }

       try
       { Method getChoicesMethod = lessonClass.getMethod
                  ("getChoices", new Class[0]);
         Integer integer = (Integer)getChoicesMethod.invoke(lesson, params);
         count = integer.intValue();
       }
       catch (Exception e)  { count = 0; }
       try
       { Method getColorSchemeMethod = lessonClass.getMethod
                  ("getColorScheme", new Class[0]);
         colors = (ColorScheme)getColorSchemeMethod.invoke(lesson, params);

       }
       catch (Exception e)
       {  colors = new ColorScheme(new Color(130,130,130), Color.white);
       }

      try
       { Method getMyLink = lessonClass.getMethod
                  ("getMyLink", new Class[0]);
         link = (String)getMyLink.invoke(lesson, params);

       }
       catch (Exception e)
       {
          try
          {   Method getLink = lessonClass.getMethod
                  ("getLink", new Class[0]);
              link = (String)getLink.invoke(lesson, params);

          }
          catch (Exception ex) { link = "";  }
       }

       setup = null;
       selected = -1;
    }

   /** Meth to return the lesson category name */
   public String getCategory() { return "CategoryMultiplePictures"; }

   /** Method to return the size of each picture to display.
    *  @return Dimension of each picture
    */
   public Dimension getPictureSize()
   {   if (isPlay())
       {
           Dimension buttonSize = getScreenSize();
           buttonSize.width = buttonSize.width/2 - 50;
           buttonSize.height = buttonSize.height/2 - 100;
           if (buttonSize.width>PICTURE_SIZE.width)
               buttonSize.width = PICTURE_SIZE.width;
           if (buttonSize.height>PICTURE_SIZE.height)
               buttonSize.height = PICTURE_SIZE.height;
            return new Dimension(buttonSize);

       }
       return new Dimension(PICTURE_SIZE.width/3, PICTURE_SIZE.height/3);
   }

   /** Return the number of pictures in this lesson */
   public int getChoices()   {  return count;  }

   /** Return the setup and secondary toolBar panels for displaying lesson data.
   */
   public JPanel[] getLessonData()
   {
      if (setup==null) selected = -1;
      JPanel[] panels = new JPanel[2];
      if (count==0) return panels;

      resizeButtons();
      select(null);
      
      if (setup==null) setup = new PicturesSetUpPanel(this);
      else setup.setComponents();

      panels[AcornsProperties.DATA] = setup;
      JPanel controlPanel = SetupPanel.createSetupPanel
              (this, "MultiplePicturesSetup",
                 SetupPanel.ROTATE + SetupPanel.PICTURE +
                 SetupPanel.FILL +   SetupPanel.FGBG 
               );
      panels[AcornsProperties.CONTROLS] = controlPanel;
      return panels;
   }

   /** Method to get the sound associated with this data
    *
    * @param image PictureChoice to which it is attached
    * @param layer Which layer is it attached to
    * @param point The point x,y position (must be 0,0)
    * @param index which recorded sound attached to this picture
    * @return The sound data object
    */
   public SoundData getSoundData(int image, int layer, Point point, int index)
   {
       Vector<SoundData> soundVector
               = choices[image].getQuestions(layer).getVector();
       return (SoundData)soundVector.elementAt(index);
   }

   /** Method to get aPictureData object corresponding to particular button
    *
    * @param pictureNum The button number to retrieve
    * @return The PictureData object
    */
   public PictureData getPictureData(int pictureNum)
   {   if (pictureNum<0) return colors.getPicture();
       return choices[pictureNum].getPictureData();
   }

   /** Method to get all of the button choices */
   public PictureChoice[] getPictureData() 
   { 
	   return choices; 
   }
   
   /** Method to get non-excluded choices */
   public PictureChoice[] getActivePictureData()
   { 
	    PictureChoice[] activeChoices = new PictureChoice[count]; 
	    int activeCount = 0;
		for(int i=0; i<count; i++)
		{
	       if (isExcluded(choices[i].getQuestions(layer)))
	    	   continue;
	       
	       activeChoices[activeCount++] = choices[i];
	    }
       return activeChoices;
   }
   
   /** Method to get non-excluded count */
   public int getActiveChoices()
   {
	    int activeCount = 0;
		for(int i=0; i<count; i++)
		{
	       if (isExcluded(choices[i].getQuestions(layer)))
	    	   continue;

	       activeCount++;
	    }
      return activeCount;
   }

   /** Method to find PictureData based on a string name
    *
    * @param msg The string in question
    * @return the PictureData object
    */
   public PictureData getPictureData(String msg)
   {
       PictureData pictureData = null;
       PictureChoice choice = getSelectedPicture(msg);
       if (choice !=null) pictureData = choice.getPictureData();
       return pictureData;
   }

   /** Method to insert a picture into this lesson
    *
    * @param url The URL for the picture resource
    * @param scale The scale factor
    * @param angle The angle to rotate
    * @throws IOException
    */
   public void insertPicture(URL url, int scale, int angle)
                                               throws IOException
   {
      PictureChoice choice = new PictureChoice(url, getPictureSize());

      // Scale picture down if necessary.
      choice.getPictureData().rewrite(MAX_PICTURE);
      choice.getPictureData().setAngle(angle);

      int scaleFactor = scale;
      int type = PictureChoice.SCALE;
      if (scale>=0 && scale<PictureChoice.SCALE_TYPES)
      {
          type = scale;
          scaleFactor = 100;
      }

      choice.getPictureData().setScale(scaleFactor);
      choice.setType(type);

       // Expand array if it is full.
       Frame root = JOptionPane.getRootFrame();
      if (count == MAX_PICTURES)
      {
          JOptionPane.showMessageDialog(root,
           LanguageText.getMessage("commonHelpSets", 2, ""+MAX_PICTURES));
          throw new IOException();
      }

      if(count>=choices.length)
      {
         PictureChoice[] c2 = new PictureChoice[choices.length*2];

         System.arraycopy(choices, 0, c2, 0, choices.length);
         choices = c2;
      }
      choices[count] = choice;
      count++;
      setDirty(true);

      // return if the working panel component is not set up.
      if (setup==null) return;
      setup.setComponents();
      setup.repaint();

   }  // End insertPicture()

   /** Method to remove the selected picture from the lesson */
   public void removeSelectedPicture()
   {
       if (selected>=0 && selected<count)
           removePicture(selected);
   }

   /** Method to remove a particular picture from the lesson
    *
    * @param pictureNum The button number containing the picture
    */
   public void removePicture(int pictureNum)
   {  LessonActionPicturesData oldData;

      // Find the selected picture and remove it from the array.
      Frame root = JOptionPane.getRootFrame();
      try
      {
          if (selected<0 || selected>=count)
          {   if (colors.getPicture() ==null)
                  throw new NoSuchElementException();

              colors.setPicture(null);
              return;
          }
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog
                   (root, LanguageText.getMessage("commonHelpSets", 3));
         return;
      }

      // return if the working panel component is not set up.
      if (setup==null) return;

      oldData = new LessonActionPicturesData
                            (choices[selected], true, selected);

      // Copy the rest of the array down.
      for (int i=selected; i<(count-1); i++) choices[i] = choices[i+1];
      choices[--count] = null;
      selected = count;

      pushUndo(oldData);
      setDirty(true);
      displayLesson();
   }  // End removePicture()
   
   /** Move the selected picture before the picture passed */
   public void reorderPictures()
   {
       if (selected<0 || selected>=count)  
       {  
    	   Toolkit.getDefaultToolkit().beep();
    	   return;
       }

       int c;
       Point point = null;
       ChoiceButton button = null;
       for (c=0; c<count; c++)
       {
    	   button = choices[c].getButton();
    	   if (button==null) continue;
    	   point = button.getMousePosition(true);
    	   if (point!=null) break;
       }
       if (c>=count) {  Toolkit.getDefaultToolkit().beep(); return; }
       
       if (choices[c] == choices[selected]) return; // Nothing to do
       
       PictureChoice temp = choices[selected];
       removePicture(selected);
       
       // Copy up to make a hole at the new place
       int min = (selected<c) ? c+1 : c;
       for (int p=count; p>=min; p--)   
       {  
    	   choices[p] = choices[p-1];  
       }
       choices[c] = temp;
       count++;
       
       setDirty(true);
       resetUndoRedo();  // Stack cannot perform both a remove and insert. 
	   displayLesson();
   }

   /** Determine if a particular picture is selected
    *  @return true if yes, false otherwise
    */
   public boolean isSelected(PictureChoice choice)
   {
       if (selected<0 || selected>=count) return false;
       if (choices[selected].getButton(false)
                           == choice.getButton(false)) return true;
       return false;
   }

   /** Remove all listeners from the picture choice objects */
   public void removeListeners()
   {   if (count==0) return;
       for (int c=0; c<count; c++)
       { choices[c].getButton(false).removeListeners();  }
   }

   /** Get color scheme object */
   public ColorScheme getColorScheme() { return colors; }

   /** Get selected picture or return null if none selected.
    *  @param errMsg  null if no message, a reason string otherwise
    *  @return The selected PictureChoice object or background (none selected)
    */
   public PictureChoice getSelectedPicture(String errMsg)
   {
       if (selected>=0 && selected<count) return choices[selected];
       if (errMsg == null) return null;

       Frame root = JOptionPane.getRootFrame();
       JOptionPane.showMessageDialog
               (root, LanguageText.getMessage("commonHelpSets", 3));
       return null;
   }

   /** Method to return which PictureChoice object is selected */
   public int getSelectedIndex() { return selected; }

   /** Method to select a particular picture for editing
    *  @param choice The picture choice object to select
    */
   public void select(PictureChoice choice)
   {   ChoiceButton  button, choiceButton = null;
       if (choice!=null) choiceButton = choice.getButton(false);
       int           pictureType;

       // Select this object and deselect all the others
       selected = -1;
       for (int p=0; p<count; p++)
       {   button = choices[p].getButton(false);

           if (button==choiceButton)
           {
               selected = p;
               button.selectBorders(false,true);

               pictureType = choices[p].getType();
               if (setup!=null) {  SetupPanel.setScaleBox(pictureType); }
            }
           else  {  button.selectBorders(false,false);  }
       }
   }

   /** Method to appropriately resize
    *          the buttons based on setup or play mode
    */
   public void resizeButtons()
   {  int size = getChoices();
      PictureChoice[] pictureChoices = getPicturesData();
      ChoiceButton button;

  	   for (int i=0; i<size; i++)
      {  button = pictureChoices[i].getButton();
         button.removeListeners();
         button.setVisible(true);
         pictureChoices[i].resize(getPictureSize());
      }
   }

   /** Method to see if lesson is playable.
    *  @return error message if no (null if yes)
    */
   public String isPlayable(int layer)
   {
	 // All pictures must be playable.
	 int total = 0;
	 for(int i=0; i<count; i++)
	 {
        if (isExcluded(choices[i].getQuestions(layer)))
	       continue;
	       
	    if(!choices[i].isPlayable(layer))
	    {
	       if (!isPlay()) select(choices[i]);
	       return LanguageText.getMessage("commonHelpSets", 5);
	    }
	    else
	    {
	    	total++;
	    }
	 }
	
     // Lesson must have four pictures.
	 if(total<4) { return LanguageText.getMessage("commonHelpSets",4); }
     return null;
   }  // End isPlayable()
   
   public void copyPaste(JButton button)
   {
 	   String[] msgs = LanguageText.getMessageList("commonHelpSets", 101);
       if (button.getName().equals(msgs[0]))
	   {
           setCopyLayer(getLayer());
           button.setBorder(BorderFactory.createLoweredBevelBorder());
       }
       if (button.getName().equals(msgs[1]))
       {
           int destinationLayer = getLayer();
           int sourceLayer      = getCopyLayer();
           
           button.setBorder(BorderFactory.createLoweredBevelBorder());
           String msg = null;
           if (sourceLayer<=0) msg = LanguageText.getMessage("commonHelpSets", 102);
           else if (sourceLayer==destinationLayer)
               msg = LanguageText.getMessage("commonHelpSets", 103);
                       
           if (msg!=null)  
           {
        	   JOptionPane.showMessageDialog(button, msg);
           }
           else
           {
              msg = LanguageText.getMessage
                      ("commonHelpSets", 104, ""+sourceLayer, ""+destinationLayer);

              int answer = JOptionPane.showConfirmDialog
                 ( button, msg, LanguageText.getMessage("commonHelpSets", 105)
                 , JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
                 , getIcon(AcornsProperties.ACORN, Lesson.ICON));

              if (answer!=JOptionPane.CANCEL_OPTION)
              {  
            	  copyPasteLayer(sourceLayer, destinationLayer);
              }
           }
	   }
   }

   /** Method to copy points between layers
    *
    * @param sourceLayer The source layer number
    * @param destinationLayer The destination layer number
    */
   private void copyPasteLayer(int sourceLayer, int destinationLayer)
   {
	   for (int i=0; i<count; i++)
	   {
		   PictureChoice choice = choices[i];
		   choice.copyQuestions(sourceLayer, destinationLayer);
	   }	   
       setDirty(true);
       displayLesson();
   }  // End of copyPoints()
   
   /** Method to set the layer to be copied */
   public void setCopyLayer(int copyLayer)
   {this.copyLayer = copyLayer; }

   // Method to get the layer to be copied */
   public int getCopyLayer()  { return copyLayer; }
  

   
   /** Determine if the current picture is excluded from the layer */
   public boolean isExcluded(PicturesSoundData choice)
   {
	   String excluded = choice.getText();
	   return excluded!=null && excluded.equals("yes");
   }

   /** Method to set options for play mode
    *
    * @param options array of lesson play mode options
    *                   (none for lessons with multiple pictures)
    */
   public void setPlayOptions(int[] options) {}

   /** Method to return play mode options
    *
    * @return array of options (none for lessons with multiple pictures)
    */
   public int[] getPlayOptions()  { return new int[0]; }

   /**  write images and sounds in a standard format to a subdirectory. */
   public boolean export
           (File directoryName, String[] formats, int lesson) 
        		   throws Exception
   {
      Vector<SoundData> soundVector;
      SoundData         sound;
      Point point = new Point(0,0);

      PictureData pictureData = colors.getPicture();
      if (pictureData!=null)
      {  writeImage(pictureData, directoryName,
              lesson, -1, formats[AcornsProperties.IMAGE_TYPE]);
      }


      for (int p=0; p<count; p++)
      {  pictureData = choices[p].getPictureData();
         if (pictureData != null)
         {
            // Create file name to store the image.
            pictureData.rewrite(MAX_PICTURE);
            writeImage(pictureData, directoryName,
                          lesson, p, formats[AcornsProperties.IMAGE_TYPE]);

            // Cycle through all the layers
            for (int layerNo=AcornsProperties.MIN_LAYERS
                         ; layerNo<=AcornsProperties.MAX_LAYERS; layerNo++)
            {
                soundVector = choices[p].getQuestions(layerNo).getVector();
                for (int i=0; i<soundVector.size(); i++)
                {
                    sound = (SoundData)soundVector.elementAt(i);
                    writeSound(sound, directoryName, lesson, p, layerNo,
                                point, i, formats[AcornsProperties.SOUND_TYPE]);
                  	
                 }  // End of for to loop through sound vector.
            }
         }      // End of if (image != null).
      }         // End of loop through the pictures.
      return true;
   }                    // End of export method.

   /** Method to import data into a lesson. */
   public void importData
   (int layer, Point point, URL file, String[] data, int type)
                          throws IOException, UnsupportedAudioFileException
   {
      PicturesSoundData picturesSounds;
      Vector<SoundData> soundVector;
      SoundData         sound;
      String            excluded = "";

      // Set link if this is link data.
      if (type == AcornsProperties.LINK)   {  link = data[0];  }
      if (type == AcornsProperties.LAYER)  
      {  
    	  excluded = data[2]; 
    	  picturesSounds = choices[count-1].getQuestions(layer);
          picturesSounds.setText(excluded);
       } 
      if (type == AcornsProperties.FONT)
      {
         if (!data[0].equals(""))
             colors.changeColor(getColor(data[0]), true);
         if (!data[1].equals(""))
             colors.changeColor(getColor(data[1]), false);
         if (!data[2].equals(""))
             colors.setSize(Integer.parseInt(data[2]));
      }

      if (type==AcornsProperties.PICTURE)
      {
         colors.setPicture(new PictureData(file, null));
      }

      if (type != AcornsProperties.SOUND)  {  return; }

      // Otherwise process sound data.
      else
      {  // This applies to the last picture loaded.
          if (count<=0) throw new IOException();
          picturesSounds = choices[count-1].getQuestions(layer);

          sound = new SoundData(data);
		  if (file != null) sound.readFile(file);
	          soundVector = picturesSounds.getVector();
		  soundVector.add(sound);
      }
   }    // End of import method.

   
  /** Method to create xml for printing and exporting
   *
   * @param document The xml document
   * @param lessonNode The lesson node to which to append xml data
   * @param directory Path to export directory (null if just printing)
   * @param extensions for image or sound data (null if just printing)
   * @return true if successful, false otherwise
   */
  public boolean print( Document document, Element lessonNode
            , File directory, String[] extensions)
  {

      // Get default extensions if object null
      if (extensions == null)
      {   extensions = new String[2];
          extensions[AcornsProperties.SOUND_TYPE] = "wav";
          extensions[AcornsProperties.IMAGE_TYPE] = "jpg";
      }

      // Get the lesson number index in this file
      int lessonNo
          = Integer.parseInt(lessonNode.getAttribute("number")) -1;

      Element imageNode, layerNode, fontNode;
      String[] values;

      PictureData picture = colors.getPicture();
      if (picture != null)
      {
         values = new String[]
              {"-1", "", ""+picture.getScale(), ""+picture.getAngle() };
         imageNode = makeImageNode(document, directory
                           , extensions[AcornsProperties.IMAGE_TYPE]
                           , picture, values, lessonNo);
         lessonNode.appendChild(imageNode);
      }

      fontNode = makeFontNode(document, colors);
      lessonNode.appendChild(fontNode);

      // Create records for each picture.
      PictureData  pictureData;
      PicturesSoundData picturesSounds;
      Vector<SoundData> soundVector;

      Point point = new Point(0,0);

      for (int p=0; p<count; p++)
      {
         pictureData = choices[p].getPictureData();
         if (pictureData.getFrame(0)!=null)
         {
            values = new String[] {""+p, "", ""+choices[p].getType(), "" };
            imageNode = makeImageNode(document, directory
                               , extensions[AcornsProperties.IMAGE_TYPE]
                               , pictureData, values, lessonNo);
            lessonNode.appendChild(imageNode);
         }
         else continue;

         // Handle all of the existing layers
         for (int layerNo=AcornsProperties.MIN_LAYERS;
                  layerNo<=AcornsProperties.MAX_LAYERS; layerNo++)
         {
            values = new String[] {""+layerNo, ""};
            layerNode = makeNode
                    (document, "layer", new String[]{"value"}, values);

            // Get sound vector for this layer.
            picturesSounds = choices[p].getQuestions(layerNo);
            if (isExcluded(picturesSounds))
            {
            	layerNode.setAttribute("excluded", "yes");
            }
            	
            soundVector = picturesSounds.getVector();
            if (soundVector.size()>0)
            {
                imageNode.appendChild(layerNode);
                makeListOfPointNodes( document, directory, soundVector
                                    , point, layerNode
                                    , extensions[AcornsProperties.SOUND_TYPE]
                                    , lessonNo, layerNo, p);
            }
         }      // End of loop through layers
      }     // End of loop through pictures.
      return true;
  }   // End of print method

  /** Method to convert between versions  */
   public Lesson convert(float version)  { return this; }

  //--------------------------------------------------------
  // Methods to respond to the redo and undo commands.
  //--------------------------------------------------------
   private static Stack<LessonActionPicturesData> undoStack;
   private static Stack<LessonActionPicturesData>  redoStack;

   public UndoRedoData getData()
   {  if (selected<0 || selected>=count)
             return new LessonActionPicturesData(choices[0], false, 0);
       else return new LessonActionPicturesData
               (choices[selected], false, selected);
   }

   /** Get array of PictureChoice objects */
   public PictureChoice[] getPicturesData() {return choices;}

   /** Method to push into the undo stack */
   public void pushUndo(LessonActionPicturesData data)
   {
       if (redoStack==null)
       {   redoStack = new Stack<LessonActionPicturesData>();
           undoStack = new Stack<LessonActionPicturesData>();
       }
       redoStack.clear();
       undoStack.push(new LessonActionPicturesData(data.getData()
               , data.isDeleted(), data.getSelected()));
       super.pushUndo(data);
   }

   /** Method to process redo command */
   public void redo(UndoRedoData dataRecord)
   {
      LessonActionPicturesData data = redoStack.pop();
      if (data==null) return;
      if (setup==null) return;

      selected = data.getSelected();
      if (data.isDeleted())
           undoStack.push(data); 
      else undoStack.push((LessonActionPicturesData)getData()); 

      if (selected<0 || selected>=count) return;

      if (data.isDeleted()) 
      {
         for (int i=selected; i<(count-1); i++) choices[i] = choices[i+1];
         choices[--count] = null;
         selected = count;
      }
      else
      {   choices[selected] = data.getData();
          choices[selected].getButton
                    (false).setPictureChoice(choices[selected]);
      }

      setup.setComponents();
      setup.repaint();
   }  // End redo()

   /** Method to process an undo command */
   public void undo(UndoRedoData dataRecord)
   {
      LessonActionPicturesData data = undoStack.pop();
      if (data==null) return;
      if (setup==null) return;

      selected = data.getSelected();
      if (data.isDeleted())
           redoStack.push(data);
      else redoStack.push((LessonActionPicturesData)getData());

      if (selected<0 || selected>count) return;

      if (data.isDeleted())
      {  for (int i=count; i>selected; i--) choices[i] = choices[i-1]; }

      // Set the choices object
      choices[selected] = data.getData();
      choices[selected].getButton(false).setPictureChoice
                                     (choices[selected]);

      // Adjust the count if necessary.
      if (data.isDeleted())   { count++;  }

      setup.setComponents();
      setup.repaint();
   }  // End undo()

   public void save()
   {  PictureData pictureData;
      for (int p=0; p<count; p++)
      {  pictureData = choices[p].getPictureData();
         if (pictureData != null)
         {  // Scale to the maximum file name to store the image.
            pictureData.rewrite(MAX_PICTURE);
         }
      }
   }
}  // End CategoriesMultiplePictures
