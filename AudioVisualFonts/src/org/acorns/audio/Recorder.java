/**
 * Recorder.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */
package org.acorns.audio;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.acorns.data.SoundData;
import org.acorns.language.LanguageText;

public class Recorder extends Thread
{
    private SoundData sound;
    private JLabel    errorLabel;
    
    private boolean stopSound;
    
    // The following are set by setAudioParameters() method.
    private int   frameSizeInBytes;
    private float frameRate;
    private JButton button;
    private Color  saveColor = new Color(210,210,210);

   public Recorder(SoundData sound, JLabel errorLabel) 
   {
       this.sound = sound; 
       this.errorLabel = errorLabel;
       stopSound = false;
       button = null;
   }
   
   public void setButton(JButton button)
   {
	   this.button = button;
	   saveColor = button.getBackground();
   }

   public void run()
   {

      int    count=0, available;  
      byte[] temp = new byte[SoundDefaults.BUFFERSIZE];
      try
      {
          setAudioParameters();
          TargetDataLine target = SoundDefaults.getMicrophone();
          
          // Check if microphone is available
          if (target==null)
          {
              String msg =  LanguageText.getMessage("soundEditor", 29);
              error(msg);
              return;
          }
          
          AudioFormat format = target.getFormat();
          target.open(format);
          target.addLineListener(new LineListener() 
          {
			public void update(LineEvent evt) 
			{
				LineEvent.Type type = evt.getType();
				if(type.equals(LineEvent.Type.START))
				{
					//System.out.println("Start");
					if (button != null)
					{
						button.setBackground(Color.RED);
						button.setOpaque(true);
					}
				}
				else if(type.equals(LineEvent.Type.STOP))
				{
					//System.out.println("Stop");
					if (button != null)
					{
						button.setBackground(saveColor);
					}
				}
				else if(type.equals(LineEvent.Type.OPEN))
				{
					//System.out.println("Open");
				}
				else if(type.equals(LineEvent.Type.CLOSE))
				{
					//System.out.println("Close");
				}
		    }});
          
          AudioInputStream stream = new AudioInputStream(target);
          stream = SoundIO.convertAudioFormat(stream, sound.getAudioFormat());
          
          ByteArrayOutputStream outStream = new ByteArrayOutputStream();          
 
          // Compute the maximum number of reads.
          float maxReads
                  = SoundDefaults.getMaxMin()*frameRate*60*frameSizeInBytes;

          int readNum = 0;
          target.start();
	      int trim = SoundDefaults.getTrimSize();
          while(!stopSound)
          { 
            available = Math.min(temp.length, stream.available());
            count = stream.read(temp, 0, available);
            if (count>0) 
            { if (trim<count)
	          {
                 outStream.write(temp, trim, count-trim);
                 readNum += count - trim;
                 if (readNum++ > maxReads)
                 {    
                	  error(LanguageText.getMessage("commonHelpSets", 70));
                      target.close();
                      stream.close();
                      outStream.close();
                      if (button!=null)
                    	  button.setBackground(saveColor);
                      break;
                 }
  	             trim =-count;
	             if (trim<0) trim = 0;
	          }
            }
          } // End while

          // Close the capture and copy to a byte array.
          target.close();
          stream.close();
          if (button!=null)
        	  button.setBackground(saveColor);

          byte[] audioBytes = outStream.toByteArray();
          outStream.close();
          sound.setAudioData(audioBytes);

          int windowFrame
             = (int)frameRate*SoundDefaults.getActivationWindow()/1000;

          if (windowFrame==0) return;

          TimeDomain domain = new TimeDomain(sound);
          double[] timeDomain = domain.getTimeDomainFromAudio();

          int threshold = SoundDefaults.getActivationThreshold();
          if (threshold==0) return;

          if (timeDomain !=null)
          {  int start = computeStartPoint
                            (timeDomain, windowFrame, threshold);

             if (start>0) domain.deleteAudio(new Point(0, start));
          }
      }
      catch (Exception e) { error(e); }
   }  // End of run.    
   
   public void stopRecording() 
   { 
	   stopSound = true; 
   }
   
    /** Display Exceptions that occur.
    * 
    * @param e exception that occurred
    */
   private void error(Exception e)
   {
       String message = e.toString();
       int index = message.indexOf(":");
       if (index <0) index = 0;
       error(message.substring(index));
   }
   
   /** Display error in pane, or in a label (if one exists)
    * 
    * @param s error message to be displayed
    */
   private void error(String s)
   {  
	   Frame root = JOptionPane.getRootFrame();
	   if (errorLabel == null) 
           JOptionPane.showMessageDialog(root, s);
      else errorLabel.setText(s);
   }  
   
  /** Set audio parameters to the current values in the sound object */    
   private void setAudioParameters()
   {
      AudioFormat format = sound.getAudioFormat();
      frameSizeInBytes   = format.getFrameSize();
      if (frameSizeInBytes<0) frameSizeInBytes = format.getSampleSizeInBits() / 8;
      frameRate = format.getSampleRate();
   }

   /** Computer where the actual voice begins
    *
    * @param data Array of bytes of recorded data
    * @param windowFrame width of samples per voice activation frame
    * @param threshold approximate decibel level to begin recording
    * @return offset to where the recorded voice begins
    */
   public int computeStartPoint(double[] data
                                  , int windowFrame, int threshold)
   {
       int backFrames  = 5;

       // Scale the algorithm to a maximum of 32768.
       double maxValue = (1 << (sound.getFrameSizeInBytes() * 8 -1));
       if (sound.getAudioFormat().getEncoding()
                                 .equals(AudioFormat.Encoding.ALAW)
         || sound.getAudioFormat().getEncoding()
                                 .equals(AudioFormat.Encoding.ULAW))
            maxValue = 32768;


       double ratio = 32768. / maxValue;
       ratio = ratio * ratio;

       int start = data.length - backFrames * windowFrame;
       if (start<0) return 0;

       double sum;
       for (int i=0; i<data.length; i+=windowFrame)
       {   sum = 0;
           for (int j=i; j<i+windowFrame; j++)
           {  sum = 0;
              if (j>=data.length) break;

              sum = sum + (data[j] * data[j] * ratio);

           }
           sum /= windowFrame;
           if ((10 * Math.log10(sum)) > threshold)
           {
              start = i- backFrames*windowFrame;
              if (start<0) start = 0;
              return start;
           }
       }
       return start;
   }   // End compute start point

}   // End of Recorder Thread
