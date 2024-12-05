/**
 * PictureChoice.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.data;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.acorns.widgets.*;
import org.acorns.lesson.*;

/** Contains all information about a single picture with a series of
    recordings to it. Used in lessons with multiple pictures
 */
public class PictureChoice implements Serializable, Cloneable
{
    /** Java file's serial version number */
   private final static long serialVersionUID = 1;
   
   /** Scale image with no distortion to fit button without distortion. */
   public final static int SCALE = 0;
   /** Stretch image to fill button with distortion. */
   public final static int STRETCH = 1;
   /** Scale image to fill button with no distortion and truncate excess */
   public final static int TRUNCATE = 2;
   /** Scale image to fill button vertically, truncate horizontal */
   public final static int CHOP_HORIZONTAL = 3;
   /** Scale image to fill button horizontally, truncate vertical */
   public final static int CHOP_VERTICAL = 4;
   /** Number of ways to scale images */
   public final static int SCALE_TYPES = 5;
   
   private transient ChoiceButton  button;

   // The following data saves to and loads from disk.
   private PictureData data;
   private Dimension buttonSize;
   private PicturesSoundData[] questions;
   private int type;

   /** Create an instance of this picture
    * @param url location of file to read.
    *  @param size Size of buttons to hold this image
    */
   public PictureChoice(URL url, Dimension size) throws IOException
   {  
      data = new PictureData(url, null);
      buttonSize = size;
      type       = SCALE;
      questions = new PicturesSoundData[AcornsProperties.MAX_LAYERS];
      for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
      questions[i] = new PicturesSoundData("", true);
      
   }  // End PictureChoice()
 
   /** Constructor for converting lesson from ACORNS version 3.0
    * 
    * @param bytes image stored as an array of bytes 
    * @param buttonSize the size of the PictureChoice button
    * @param questions A group of audio that go with this button
    * @param type scaling type SCALE, STRETCH, TRUNCATE, CHOP_VERTICAL,
    *                           CHOP_HORIZONTAL
    * @param angle rotation angle
    * @throws java.io.IOException
    */
    public PictureChoice(byte[] bytes, Dimension buttonSize, 
           PicturesSoundData questions, int type, int angle) throws IOException
   {
       data = new PictureData(bytes, null);
       data.setAngle(angle);
       this.buttonSize = buttonSize;
       this.questions = new PicturesSoundData[AcornsProperties.MAX_LAYERS];
       this.questions[0] = questions;
       for (int i=1; i<AcornsProperties.MAX_LAYERS; i++)
       {
           this.questions[i] = new PicturesSoundData("", true);
       }
       this.type = type; 
   }

   /** Constructor to convert from version ACORNS version 5.0
    *
    * @param data PictureData object
    * @param buttonSize Size of buttons
    * @param questions Array of audio attached to a button
    * @param type The scaling type
    */
   public PictureChoice(PictureData data
           , Dimension buttonSize, PicturesSoundData[] questions, int type)
   {
       this.data = data;
       this.buttonSize = buttonSize;
       this.questions = questions;
       this.type = type;
   }

    /** Method to resize the button
     * 
     * @param size desired size
     */
    public void resize(Dimension size)
   {
       buttonSize = new Dimension(size);

       //data.loadImages(true, null);
       ChoiceButton choice = getButton(false);
       choice.resizeButton(size);
   }
   
   /** Create a clone of the data */
   public @Override PictureChoice clone()
   {  
       try
       {   PictureChoice newObject = (PictureChoice)super.clone();
           newObject.data = data.clone();
           newObject.questions
                   = new PicturesSoundData[AcornsProperties.MAX_LAYERS];
           for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
               newObject.questions[i] = questions[i].clone();
           newObject.buttonSize = new Dimension(buttonSize);
           newObject.button = button;
           return (PictureChoice)newObject; 
       }
       catch (Exception e)
       { 
    	   Frame frame = JOptionPane.getRootFrame();
    	   JOptionPane.showMessageDialog(frame, "Couln't clone PictureChoice"); }
       return null;
   }
   
   /** Get button object to hold this picture */
   public ChoiceButton getButton() { return getButton(false);}

   /** Get button object for either an applet or for the main application
    *
    * @param applet true if applet, false otherwise
    * @return The ChoiceButton object
    */
   public ChoiceButton getButton(boolean applet)
   {   Dimension size = buttonSize;
       
       if (button==null)  
       {   if (applet)   // This is in case applet buttons need to be smaller.
           {   size.width  = 5*buttonSize.width/8; 
               size.height = 5*buttonSize.height/8; 
           }
           button = new ChoiceButton(this, size); 
       }
       return button;
   }
          
   /** Get native, gloss text and audio that goes with this picture 
    * 
    * @param layer lesson layer (counting from one)
    * @return The PictureSoundData object associated with this layer
    */
   public PicturesSoundData getQuestions(int layer)  
   { return questions[layer-1]; }

   /** Transfer audio dialog from one layer to another
    * 
    * @param fromLayer The source layer
    * @param toLayer The destination layer
    */
   public void copyQuestions(int fromLayer, int toLayer)
   {
	   PicturesSoundData from = questions[fromLayer-1].clone();
	   questions[toLayer-1] = from;
   }
   
   /** Get scaling type for this picture */
   public int getType()  { return type; }
   /** Set scaling type for this picture */
   public void setType(int type) 
   {   this.type = type;  }

   /** Get the size to display this button */
   public Dimension getSize() { return buttonSize; }
   
  /** Get the PictureData object associated with this object
   * 
   * @return PictureDataObject
   */
   public PictureData getPictureData()   {  return data; }

   /** Determine if the designated layer is playable (at least one audio
    *  recording must be attached)
    * 
    * @param layer (counting from one)
    * @return true if playable
    */
   public boolean isPlayable(int layer)
   {  Vector<SoundData> soundVector = questions[layer-1].getVector();
      int count = soundVector.size();
      SoundData sound;
      if (count==0) return false;
  
      String[]  soundText;
      for(int i=0; i<count; i++)
      {
          sound = soundVector.get(i);
          if (!sound.isRecorded()) return false;
          
          soundText = sound.getSoundText();
          if (soundText[SoundData.GLOSS].length()==0) return false;
          if (soundText[SoundData.NATIVE].length()==0) return false;
      }
      return true;
   }  // End isPlayable()
       
}  // End PictureChoice