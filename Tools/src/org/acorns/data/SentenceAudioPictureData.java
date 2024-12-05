/**
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acorns.data;

import java.awt.Frame;
import java.io.*;
import javax.swing.*;

public class SentenceAudioPictureData 
                         extends SentenceData implements Serializable, Cloneable
{
   private static final long serialVersionUID = 1;
   
   private SoundData sound;
 
   public SentenceAudioPictureData(String sentence)
   {
        super(sentence);
        sound = new SoundData();
   }
   
   public SentenceAudioPictureData(SentenceData data)
   {
       super(data);
       sound = new SoundData();
   }
   
   /** Get the object containing the recorded audio */
   public SoundData getSound()  {  return sound;  }
   
   /** Set the audio into this object */
   public void setSound(SoundData sound)  { this.sound = sound; }
   
   /** Create clone of this object */
   public @Override SentenceAudioPictureData clone()
   {
      try
      {
          
        SentenceAudioPictureData data = (SentenceAudioPictureData)super.clone();
        PictureData picture = getPicture();
        if (picture!=null) data.setPicture(picture.clone());
        if (sound!=null) data.setSound(sound.clone());
        return data;
     }
     catch (Exception e)
     {
    	 Frame frame = JOptionPane.getRootFrame();
    	 JOptionPane.showMessageDialog(frame, "Couldn't clone ColorScheme"); 
     }
     return null;
    }

}       // End of SentenceAudioPictureData class
