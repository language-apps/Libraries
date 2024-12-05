/**
 *   SoundData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 * 
 */
package Tools;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import org.acorns.audio.*;

/** Handle manipulation of sound objects */
public class SoundData implements Serializable, Cloneable
{  
    public static final float MINSAMPLES = SoundDefaults.getFrameRate()/4;
    public static final float MAXSAMPLES = SoundDefaults.getFrameRate()*4;
    private static final long serialVersionUID = 1;
       
	   private   Vector<String> text;            // Vector for gloss and phonetics.
    
    // The following is not used but needed for compatibility with old SoundData objects
    // We solved import problems by making errorLabel transient. It is not used in the  program.
    public   JLabel  dummyPosition;  
    
    protected           byte[] audioData;        // The compressed audio data as a file.
 
    
    // Audio data parameters.
    private boolean              bigEndian;
    private int                  channels;
    
    private transient            AudioFormat.Encoding encoding;
    public boolean              signed;
    public boolean              unsigned;
    public boolean              alaw;
    public boolean              ulaw;

    private float                frameRate;
    private int                  frameSizeInBytes;
    private float                sampleRate;
    private int                  sampleSizeInBits;
 
   
    /* Get array of annotation strings */
    protected String[] getSoundText()
    {
        for (int i=text.size(); i<4; i++)
        {
            if (i!=3) text.add("");
            else text.add("" + frameRate);
        }
        
        String[] data = new String[text.size()];
        for (int i=0; i<text.size(); i++)   { data[i] = text.elementAt(i); }
        if (data.length>2 && data[2]==null) { data[2] = "English"; }
        return data;
    } 
    
   // This method gets the encoding based on the object's parameters
   public AudioFormat getAudioFormat()
   {
       return new AudioFormat(encoding, sampleRate, sampleSizeInBits
               , channels, frameSizeInBytes, frameRate, bigEndian);
   }
   
     
    public org.acorns.data.SoundData convert(float version)
    {
       org.acorns.data.SoundData newData = new org.acorns.data.SoundData
               (getSoundText(), getAudioFormat(), audioData);
       return newData;
    }
   
}  // End of SoundData class.