/*
 * PicturesSoundData.java
 *
 *   @author  HarveyD
 *   @version 6.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */


package org.acorns.data;

import java.awt.Frame;
import java.io.*;
import javax.swing.*;

/** class to maintain the data associated with a user entered sentence
 *    This includes a picture, a set of audio and associated
 *       gloss, native, and language text
 */
public class SentenceData implements Serializable, Cloneable
{
   private String sentence;
   private PicturesSoundData soundDialog;
   private PictureData picture;
   
   private static final long serialVersionUID=-5875835772622743426L;

   /** Standard constructor
    *
    * @param sentence The sentence associated with this object
    */
   public SentenceData(String sentence)
   {  this.sentence = sentence;   
      picture = null;
      soundDialog = new PicturesSoundData();
   }
   
   public SentenceData(SentenceData data)
   {
       sentence = data.getSentence();
       picture  = data.getPicture();
       soundDialog = data.getAudio();
   }

   /** Get the sentence text */
   public String getSentence() { return sentence; }

   /** Alter the sentence text */
   public void setSentence(String data) { sentence = data; }

   /** Get the audio attached to this sentence
    *
    * @return The PicturesSoundData object maintaining recorded audio.
    */
   public PicturesSoundData getAudio() { return soundDialog; }

   /** Get the picture attached to this sentence */
   public PictureData getPicture() { return picture; }

   /** Replace the picture associated with this sentence */
   public void setPicture(PictureData data) { picture = data; }

   /** Make an identical copy  of this object */
   public @Override SentenceData clone()
   {
       try
       {   SentenceData newObject = (SentenceData)super.clone();
           newObject.sentence = sentence;

           if (soundDialog!=null) newObject.soundDialog = soundDialog.clone();
           else newObject.soundDialog = null;

           if (picture!=null) newObject.picture = picture.clone();
           else newObject.picture = null;

           return (SentenceData)newObject;
       }
       catch (Exception e)
       { 
    	   Frame frame = JOptionPane.getRootFrame();
    	   JOptionPane.showMessageDialog(frame, "Couldn't clone SentenceData"); }
       return null;
   }

}  // End of SentenceData class
