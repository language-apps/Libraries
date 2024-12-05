/**
 * PlayBack.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.audio;

import org.acorns.data.*;
import java.util.*;
import java.awt.Frame;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;

public class PlayBack extends Thread
{
  SoundData sound;
  JComponent playBackPanel;
  boolean stop;

  byte[] playData;
  int frames;
  int startSpot;
  int endSpot;
   
  /* The following parameters are set by the setAudioParametersMethod */
  int   frameSizeInBytes;
  float frameRate;
  
   public PlayBack
           (SoundData sound, int startFrame, int endFrame, JComponent playBackPanel)
   {
       this.sound = sound;
       this.playBackPanel = playBackPanel;
       stop=false;
       
       setAudioParameters();
       byte[] audio = sound.getAudioData();
       if (audio==null) return;

       frames    = endFrame - startFrame;
       startSpot = startFrame * frameSizeInBytes;
       endSpot   = endFrame * frameSizeInBytes;
       playData = new byte[endSpot - startSpot];
       for (int i=startSpot; i<endSpot; i++)
       {  playData[i-startSpot] = audio[i]; }
   }

   public void run()
   {
      // Used to fire "PlayBack" events.
      setLastSound(sound);
      int oldSpot = (startSpot-1)/frameSizeInBytes, spotCount = 0;

      InputStream inStream   = new ByteArrayInputStream(playData);
      AudioFormat format = sound.getAudioFormat();
      AudioInputStream audio = new AudioInputStream(inStream,format,frames);

      // Get a line for sending data from a byte array to the output.


      // Start the playing.
      try 
      { 
         DataLine.Info  info   = new DataLine.Info
                                   (SourceDataLine.class, format);
         SourceDataLine source = (SourceDataLine)AudioSystem.getLine(info);
         source.open(format);
         source.start();

         // Set buffer to about a twentieth of a second.
         int bufferSize = (int)(SoundDefaults.BUFFERSIZE);
         byte[] temp =  new byte[bufferSize];
         int count = 0;
         fireProperty(-1, 0);

         while (!stop && 
                  (count = audio.read(temp, 0, temp.length)) != -1)
         {
             if (count>0)   
             {   // Fire the play back point every twentieth of a second.               
                 spotCount += count/(frameSizeInBytes);
                 if (spotCount>frameRate/20)
                 {  fireProperty(oldSpot, spotCount+oldSpot);
                    oldSpot  += spotCount;
                    spotCount = 0;

                    // Yield if this is a windows system.
                    String os = System.getProperty("os.name");
                    if (os.toLowerCase().indexOf("windows")>=0)  
                    	Thread.yield();
                 }
                 source.write(temp, 0, count);  
                 Thread.yield();
              }
         }
         fireProperty(oldSpot, -1);
         source.drain();
         source.close();
         inStream.close();
      }
      catch(Exception e) 
      { 
    	  Frame frame = JOptionPane.getRootFrame();
    	  JOptionPane.showMessageDialog(frame, e.toString()); 
      }
      removeSound(sound);
   }  // End of run method.
   
   /** Stop active playback object from playing. The interrupt() method doesn't work */
   public void stopPlay()  {  stop = true;  }

   /* The following methods and date are to be able to stop sounds that are playing back */
   private static Vector<SoundData> lastSound = new Vector<SoundData>(); // The sound that can be stopped
   
   /** Add the last sound to the playback vector of sound objects that can be stopped
    *  @param lastSound the last sound that can be stopped
    */
   private static void setLastSound(SoundData lastSound)
   {  PlayBack.lastSound.add(lastSound); }
   
   /** Kill any sounds that are playing. */
   public static void stopPlayBack()
   {   Object[] sounds = lastSound.toArray();
       for (int l=0; l<sounds.length; l++)  
       {   SoundData soundData = (SoundData)sounds[l];
           soundData.stopSound(); 
       }
   }
   
   /**
    * 
    * @param sound remove sound object from playback vector.
    */
  private static void removeSound(SoundData sound)
   {   Object[] sounds = lastSound.toArray();
       
       for (int l=0; l<sounds.length; l++)
       {  if (sounds[l] == sound) lastSound.removeElement(sounds[l]); }
   }
   
   /** Set audio parameters to the current values in the sound object */    
   private void setAudioParameters()
   {
      AudioFormat format = sound.getAudioFormat();
      frameSizeInBytes   = format.getFrameSize();
      frameRate          = format.getFrameRate();
   }
   
   // Fire property change events.
   private void fireProperty(int oldValue, int newValue)
   {
       if (playBackPanel != null)
       {
           playBackPanel.firePropertyChange("PlayBack", oldValue, newValue);
       }
   }
}       // End of PlayBack thread
