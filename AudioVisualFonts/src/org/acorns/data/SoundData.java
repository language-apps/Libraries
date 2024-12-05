/**
 *   SoundData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 * 
 */
package org.acorns.data;

import java.awt.Frame;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.net.*;
import org.acorns.audio.*;

/** Maintain audio recordings and gloss/native/descriptive text */
public class SoundData implements Serializable, Cloneable
{   /** Minimum audio frames per recording */
    private static final float MINSAMPLES = SoundDefaults.getFrameRate()/4;
    /** Maximum audio frames per  recording */
    private static final float MAXSAMPLES = SoundDefaults.getFrameRate()*4;
    /** Java serial file version */
    private static final long serialVersionUID = 1;
    /** Internal audio encoding format */
    private static final String ENCODE = "wav";
       
    private   Vector<String> text;            // Vector for gloss and phonetics.
    
    private           byte[] audioData;        // The compressed audio data as a file.
    private transient byte[] audioBytes;       // Captured data as a byte array.
    private   transient JLabel  errorLabel;    // Label to display errors.
   
    // Audio data parameters.
    private boolean              bigEndian;
    private int                  channels;
    
    private transient            AudioFormat.Encoding encoding;
    private boolean              signed;
    private boolean              unsigned;
    private boolean              alaw;
    private boolean              ulaw;

    private float                frameRate;
    private int                  frameSizeInBytes;
    private float                sampleRate;
    private int                  sampleSizeInBits;
    
    transient private Recorder captureThread = null;  // Recording thread
    transient private PlayBack playBackThread = null; // Playing thread
    transient private boolean  dirty = false;         // This object changed

    /** Offset to gloss text */
    transient public static int GLOSS     = 0;
    /** Offset to native translation text */
    transient public static int NATIVE    = 1;
    /** Offset to language of native translation */
    transient public static int LANGUAGE  = 2;
    /** Offset to audio file frame rate */
    transient public static int FRAMERATE = 3;
    /** Offset to the audio file's name */
    transient public static int NAME      = 4;
    /** Offset to culturally descriptive data */
    transient public static int DESC      = 5;
    /** Size of audio text array */
    transient public static int SIZE      = 6; 
    
    
    /** Create Sound data object without error display or text strings */
    public SoundData()
    {
        errorLabel = null;
        reset(null);
    }
    
    /** Create Sound data object from another
     * 
     *  @param soundData SoundData object
     */
    public SoundData(SoundData soundData)
    {
    	this(soundData.getSoundText(), soundData.getAudioFormat(), soundData.getAudioData());
    }
    
    /** Create Sound data object without error display, and with audio data
     * 
     */
    public SoundData(String[] text, AudioFormat format, byte[] audioData)
    {
        errorLabel = null;
        reset(text);
        this.audioData = audioData;
        setAudioParameters(format);        
    }
    
    /** Create Sound data object without error display
     *  @param text Array of annotation strings to use. Refer to the
     *         AcornsProperties class for offset Constants.
     */
    public SoundData(String[] text)
    {
        errorLabel = null;
        reset(text);
    }
    
    /** Create SoundData object with error display and annotation strings 
     *
     *  @param errorLabel JLabel to display errors encountered.
     *  @param text Array of annotation strings to use. See AcornsProperties
     *         class for offset constant values.
     */
    public SoundData(String[] text, JLabel errorLabel)
    {
        this.errorLabel = errorLabel;
        reset(text);
    }   

    /** Reset the sound data object.
     *
     *  @param text Array of annotation strings to use. Refer to
     *         AcornsProperties class for offset constants.
     *
     */
    public final void reset(String[] text)
    {
       stopSound();
	   audioBytes = audioData = null;
      
       // Configure sampling parameters.
       AudioFormat audioFormat = SoundDefaults.createAudioFormat(false);
       setSoundText(text);
       setAudioParameters(audioFormat);
    }
   
    /** Erase existing audio data and replace it with a second of silence 
    *  @param text Array of annotation strings to use. Refer to
    *         AcornsProperties class for offset constants.
    *
    */
    public final void erase(String[] text)
    {
    	reset(text);
        int frames = (int)getFrameRate() * frameSizeInBytes;
        byte[] newTime = new byte[frames];
        for (int c=0; c<frames; c++)  
        {    newTime[c] = 0; }
        
        setAudioData(newTime);
    }
    
    
    /** Adjust samples per second.
     *  @return true if successful, false otherwise
     */
    public boolean changeSpeed(float factor)
    {
        float newSampleRate = sampleRate * factor;
        return setFrameRate(newSampleRate);
    }
    
    /** Set the frame rate to a designated value */
    public boolean setFrameRate(double newSampleRate)
    {
        if (newSampleRate < MINSAMPLES) return false;
        if (newSampleRate > MAXSAMPLES) return false;
    	sampleRate = frameRate = (float)newSampleRate;
    	return true;
    }
    
    /** Change the frame rate to a desired value
     * 
     * @param oldRate the original frame rate
     * @param targetFormat the desired audio parameters
     * @return true if successful, false otherwise
     */
    public boolean changeFrameRate(float oldRate, AudioFormat targetFormat)
    {
    	
        if (getAudioData()==null) return false;
        AudioFormat format = new AudioFormat
                   (encoding, oldRate, sampleSizeInBits, channels, 
                              frameSizeInBytes, oldRate, bigEndian);
        SoundIO soundIO = new SoundIO(audioBytes, format);
        
        try
        {
           audioBytes = soundIO.changeFrameRate(targetFormat);
           soundIO = new SoundIO(audioBytes, getAudioFormat());
           audioData = soundIO.copySoundStream(ENCODE);
           setAudioParameters(targetFormat);
        }
        catch (Exception e) {return false; }
        return true;        
    }
    
    /** Get array of annotation strings
     *
     * @return Array of annotation strings. Refer to the AcornsProperties
     *         class for the offset Constant values.
     */
    public String[] getSoundText()
    {
        if (text==null) text = new Vector<String>();
        for (int i=text.size(); i<SIZE; i++)
        {
            if (i!=FRAMERATE) text.add("");
            else text.add("" + getFrameRate());
        }
        
        String[] data = new String[text.size()];
        for (int i=0; i<text.size(); i++)   { data[i] = text.elementAt(i); }
        if (data.length>2 && data[LANGUAGE]==null) { data[LANGUAGE] = "English"; }
        return data;
    }
    
    /** Update the frame rate to the current

    /** <pre>Set array of annotation strings
     *       The array has the following format
     *          0 (GLOSS): Gloss text
     *          1 (NATIVE): Native text
     *          2 (LANGUAGE): Language
     *          3 (FRAMERATE): frame rate (reserved for soundData and annotationData)
     *          4 (NAME): Original file name
     *          5- (DESCRIPTION): further description
     *          6-n: open for future use
     *
     * There are symbolic constants for these in the AcornsProperties class
     *  </pre>
     *  @param userData text Array of annotation strings to use.
     */
    public void setSoundText(String[] userData)
    {   
        
        String name = "";
        if (text!=null && text.size()>=SIZE) name = text.get(NAME);
        String[] data = {"", "", "", ""+frameRate, name, ""};
        if (userData==null) userData = data;
        
        text = new Vector<String>();
        if (userData!=null) 
        {
            for (int i=0; i<userData.length || i<SIZE; i++)  
            {  
                if (i>=userData.length) 
                {
                    if (i==FRAMERATE)   text.addElement(""+frameRate);
                    else if (i==NAME)   text.addElement(name);
                    else                text.addElement("");
                }
                else                
                {   data[i] = userData[i];
                    if (userData[i]==null) data[i] = "";
                    if (data[i].equals(""))
                    {
                        if (i==FRAMERATE) data[FRAMERATE] = "" + frameRate;
                        if (i==NAME) data[NAME] = name;
                    }
                    text.addElement(data[i]);
                }
            } 
        }
        sampleRate = Float.parseFloat(text.elementAt(FRAMERATE));
        frameRate = sampleRate;
    }
    
    /** Method to change just one soundText parameter
     * 
     * @param offset which sound data parameter to get (see AcornsProperties
     *        class for clarification)
     * @param data the new value for that sound data parameter
     */
    public void setSoundText(int offset, String data)
    {
        String[] soundText = getSoundText();
        soundText[offset] = data;
        setSoundText(soundText);
    }
    
    /** Method to get a single sound data parameter 
     * 
     * @param offset which sound data parameter is of interest
     * @return the sound parameter
     */
    public String getSoundText(int offset)
    {
        String[] textData = getSoundText();
        return textData[offset];
    }
  
    /** Set frame rate to current value */
    public void setFrameRate()
    {
        String[] newText = getSoundText();
        newText[FRAMERATE] =  ""+frameRate;
        setSoundText(newText);
    }
    
        
    /** Set the error label
     *  @param error JLabel for where to output errors
     */
    public void setLabel(JLabel error)  { errorLabel = error; }
	  
    /** Get the number of frames.  */
   public int getFrames()
   {   byte[] audio = getAudioData();
       if (audio == null) return 0;
       if (frameSizeInBytes == 0) return 0;
       return  audio.length / frameSizeInBytes;
   }
   
   /** Get frames per second. */
   public float getFrameRate()  { return  frameRate;  }
   
   /** Get frame size in bytes for time domain data (always processed as a 
    * single channel)
    */
   public int getFrameSizeInBytes() { return frameSizeInBytes/channels; }
    
   /** Method to set audio data back into sound object.
    * 
    * @param  audio time domain audio byte array containing audio data.
    * @return true if successful, false otherwise
    */
    public boolean setAudioData(byte[] audio)
    {   try
        {   if (Arrays.equals(audio, audioBytes)) return true; // Don't store when nothing changed
        
    	    audioBytes = audio;
        	AudioFormat format = getAudioFormat();
            SoundIO soundIO = new SoundIO(audioBytes, format);
            audioData = soundIO.copySoundStream(ENCODE);
        }
        catch (Exception e) { return false;}
        return true;
    }

   /** Method to get the audio data array
    * 
    * @return byte array of PCM formatted audio data.
    */
   public byte[] getAudioData()
   {
       // Check to see if anything recorded.
       if (audioBytes==null && audioData==null) return null;
       
       // If recorded data exists but not converted data, convert for compression
       try
       {
           AudioFormat format = getAudioFormat();

           // Write as in a file format to the byte stream.
           if (audioData==null)
           {
                SoundIO soundIO = new SoundIO(audioBytes, format);
                audioData = soundIO.copySoundStream(ENCODE);
           }
           
           // Read from file formatted byte stream.
           if (audioBytes==null)
           {
        	   SoundIO audio = null;
               try
               { 
            	   audio = new SoundIO
                           (audioData, Float.parseFloat(getSoundText()[FRAMERATE]));
            	   audioBytes= audio.getAudioFileData();
                   setAudioParameters(audio.getFormat());
                   if (audioBytes==null) return null;
               }
               // May fail for pure byte arrays in older ACORNS version
               catch (UnsupportedAudioFileException ex) 
               {  audioBytes = audioData;  
                  setAudioEncoding();
                  audio = new SoundIO(audioBytes, format);
                  audioData = audio.copySoundStream(ENCODE);
               }
           }
       }
       catch (IOException ex) { return null; }
       return audioBytes;
   }
   
  /** Record a sound clip from a microphone  */
   public void record()
   {
       stopSound();
       captureThread = new Recorder(this, errorLabel);
       captureThread.start();
   }
   
   public void record(JButton button)
   {
       stopSound();
       captureThread = new Recorder(this, errorLabel);
	   captureThread.setButton(button);
       captureThread.start();
   }

   /** Write captured data to disk
    *
    * @param name  Name of target file with a valid extension.
    */
   public boolean writeFile(String name) throws IOException, UnsupportedAudioFileException
   {
      if (audioData==null && audioBytes==null) return true;
      if (audioData==null) getAudioData();

      AudioFormat format = getAudioFormat();
      SoundIO soundIO = new SoundIO(audioData, format);
      soundIO.writeSoundStream(this, name);
      return true;
   }
    
    /** Internal method to stop all play back and reads (not implemented) */
    public static void resetSoundData() {}

    /** Method to read an audio file using a URL (necessary for applets)
     *
     * @param audioURL The URL object
     * @return true if successful
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public boolean readFile(URL audioURL)
                      throws IOException, UnsupportedAudioFileException
    { 
       String name = audioURL.getFile();  
       name = name.replaceAll("%20", " ");

       int lastIndex = name.lastIndexOf('.');
       String extension = name.substring(lastIndex+1).toLowerCase();
       if (lastIndex>=0) name = name.substring(0,lastIndex);
       setSoundText(NAME, name);

       SoundIO audio = new SoundIO(audioURL, extension);
       boolean result = false;
       if (!extension.equalsIgnoreCase("wav") || extension.equals(ENCODE))
       {
    	   InputStream stream = audioURL.openStream();
    	   SoundIO audioCopy = new SoundIO(stream);
    	   audioData = audioCopy.getAudioFileData();
    	   
    	   // Don't need to encode to OGG
    	   result = readData(audio, extension.equalsIgnoreCase("mp3") ? true : false);  
       }
       else result = readData(audio, true);
       System.gc();
       return result;
    }
    
    /** Retrieve recorded data from a file
     *
     * @param audioFile Object pointing to the file holding recorded data
     */
   public boolean readFile(File audioFile)
                    throws IOException, UnsupportedAudioFileException
   {
	   URL url;
	   try
	   {
		   url = audioFile.toURI().toURL();
	   }
	   catch (Exception e) { throw new UnsupportedAudioFileException(); }
	   return readFile(url);
   }
    
    /** Internal method: Common file or URL file reading logic
     * 
     * @param audio The SoundIO object that read the data
     * @param encode true if the audio should be encoded
     * @return true if successful, false otherwise
     * @throws java.io.IOException
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     */
    private boolean readData(SoundIO audio, boolean encode)
                     throws IOException, UnsupportedAudioFileException
    {
       // Get the data read and initialize the SoundData object.
       stopSound();
       
       audioBytes = audio.getAudioFileData();
       setAudioParameters(audio.getFormat());

       if (audioBytes == null)   return false;

       frameRate = sampleRate;
       if (encode)
       {
    	   audioData = audio.copySoundStream(ENCODE);
       }
       return true;       
    }
    
    
    /** Determine if there is any recorded data in this object
     *
     * @return true if yes
     */
    public boolean isRecorded() 
    {  return audioData!=null || audioBytes!=null; }   
 
   /** Determine if sound system is busy recording or playing back audio
    *
    * @return true if yes
    */
   public boolean isActive()
   {
       if (captureThread != null && captureThread.isAlive())   return true;
       if (playBackThread != null && playBackThread.isAlive()) return true; 
       return false; 
   }
 
    /** Play back sound clip associated with this sound object
     *
     * @param playBackPanel Panel for firing the "PlayBack" event. 
     * @param startFrame The starting frame to play back.
     * @param endFrame The final frame to play back (or -1 for end).
     */
    public boolean playBack(JComponent playBackPanel, int startFrame, int endFrame)
    {         
    if (isRecorded())
       {
           getAudioData();       // Load audio data if not recorded.
           setAudioEncoding();
           int frames = audioBytes.length / frameSizeInBytes;
           if (startFrame<0) startFrame = 0;
           if (endFrame==-1) endFrame = frames;
           if (startFrame>endFrame || endFrame>frames) return false;
           
           playBackThread 
                   = new PlayBack(this, startFrame, endFrame, playBackPanel);
           playBackThread.start();
           return true;
       }
       else return false;
    }
   
   /** Stop the capture or play back that is active */
   public synchronized void stopSound()
   {
      if (captureThread != null )  
      {   captureThread.stopRecording();
          while (captureThread.isAlive())
          {  try { Thread.sleep(100); } 
             catch (Exception e) {}
          }
      }
      if (playBackThread != null )
      {  if (playBackThread.isAlive()) playBackThread.stopPlay();
          while (playBackThread.isAlive())
         {  
            try { Thread.sleep(100); } 
            catch (Exception e) {}
         }
      }
      playBackThread = null;
      captureThread = null;
   }
      
   /** Method to mark the sound wave changed */
   public void setDirty() { dirty = true; }

   /** Get the AudioFormat parameters for this object
    * 
    * @return AudioFormat parameter object
    */
   public AudioFormat getAudioFormat()
   {
	   if (frameSizeInBytes <= 0)
	   {
		   sampleSizeInBits = 2 * channels * 8;
		   frameSizeInBytes = sampleSizeInBits / 8;
	   }
       if (encoding==null) setAudioEncoding();
       return new AudioFormat(encoding, sampleRate, sampleSizeInBits
               , channels, frameSizeInBytes, frameRate, bigEndian);
   }

   /** Create an array of gloss strings for use in a multi-line JLabel 
    * 
    * @return string representation of this object
    */
    public @Override String toString()
    {
       String str = "<html>";
       if (text!=null && text.size()>0)
       {  str += text.elementAt(0);
          for (int i=1; i<text.size(); i++)
          {
             str += "<br>";
             str += text.elementAt(i);
          }
       }
       return str + "<hr></html>";
    }
   
   /** Make an identical copy of this object. */
   public @Override SoundData clone()
   {
       try
       {   SoundData newObject = (SoundData)super.clone();
           if (isRecorded() && dirty)  
           {   if (audioBytes!=null)
                   newObject.audioBytes = audioBytes.clone();
               if (audioData!=null) newObject.audioData = audioData.clone();
           }
           dirty = false; // Not changed since the last save.
           return (SoundData)newObject; 
       }
       catch (Exception e) { error(e); }
       return null;
   }
 
   //-------------------------------------------------------------------------
   //  Protected methods used by this class
   //-------------------------------------------------------------------------
   
   /** Display Exceptions that occur.
    * 
    * @param e exception that occurred
    */
   protected void error(Exception e)
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
   protected void error(String s)
   {  if (errorLabel == null)
      { 
	   		Frame frame = JOptionPane.getRootFrame();
	   		JOptionPane.showMessageDialog(frame, s);
      }
      else errorLabel.setText(s);
   }  

   /** Internal method to set audio parameters */
   public final void setAudioParameters(AudioFormat format)
   {
       bigEndian        = format.isBigEndian();
       channels         = format.getChannels();
       encoding         = format.getEncoding();

       frameRate         = format.getSampleRate();
       sampleRate        = format.getSampleRate();
       sampleSizeInBits  = format.getSampleSizeInBits();
       
       frameSizeInBytes = format.getFrameSize();
       if (frameSizeInBytes == AudioSystem.NOT_SPECIFIED)
       {  frameSizeInBytes = channels * sampleSizeInBits>>3; }
       
       if (encoding==null)   
       { 
           if (sampleSizeInBits>8) 
           {
               signed = true; 
               encoding = AudioFormat.Encoding.PCM_SIGNED;
           }
           else
           {
               signed = false; 
               encoding = AudioFormat.Encoding.PCM_UNSIGNED;
               
           }
       }
       
       signed           = encoding.equals(AudioFormat.Encoding.PCM_SIGNED);
       unsigned         = encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
       alaw             = encoding.equals(AudioFormat.Encoding.ALAW);
       ulaw             = encoding.equals(AudioFormat.Encoding.ULAW);
       setFrameRate();
   }

   // This method sets the encoding after a file load operation.
   protected void setAudioEncoding()
   {
       encoding = null;
       if (signed) encoding = AudioFormat.Encoding.PCM_SIGNED;
       else if (unsigned) encoding = AudioFormat.Encoding.PCM_UNSIGNED;
       else if (alaw) encoding = AudioFormat.Encoding.ALAW;
       else if (ulaw) encoding = AudioFormat.Encoding.ULAW;
       
       if (encoding==null)   
       {   if (sampleSizeInBits<0)
           {   sampleSizeInBits = SoundDefaults.getBits();
               channels = SoundDefaults.getChannels();
           }

           if (sampleSizeInBits>8) 
           {   signed = true; 
               encoding = AudioFormat.Encoding.PCM_SIGNED;
           }
           else
           {   signed = false; 
               encoding = AudioFormat.Encoding.PCM_UNSIGNED;
               
           }
       }
   }
   
}  // End of SoundData class.