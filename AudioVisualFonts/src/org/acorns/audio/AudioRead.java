/**
 * AudioRead.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.audio;

import java.net.*;
import org.acorns.data.*;
import java.util.*;

public class AudioRead extends Thread
{
    Vector<AudioObject> audioObjects;
    
    public AudioRead()
    {
        audioObjects = new Vector<AudioObject>();
        start();
    }

    /** Add another audio object to the read queue 
     * 
     * @param data The SoundData object holding the audio object to be read
     * @param audioURL The URL where the SoundData object exists
     * @param extension The extension of the audio file
     */
    public synchronized void add(SoundData data, URL audioURL, String extension)
    {
       audioObjects.add(new AudioObject(data, audioURL, extension));
       notifyAll();
    }

    /** Return the number of queued sound objects to read 
     * 
     * @return count of queued sound objects
     */
    public int size()  { return audioObjects.size(); }
    
    /** thread to read audio file */
    public @Override synchronized void run()
    {
        AudioObject audio;
        
        try
        {
            while(true)
            {  while (audioObjects.isEmpty())  wait();
               audio = audioObjects.get(0);
               audio.read();
               audioObjects.remove(0);
            }
        }
        catch (InterruptedException e)  {}
        catch (Exception e)  {}
    }   // End of run thread.
    
    
    /** Internal class to hold the data for reading sound objects */
    class AudioObject
    {
        SoundData data;
        URL    audioURL;
        String name;
        String extension;
            
        public AudioObject
                (SoundData data, URL audioURL, String extension)
        {
            this.data = data;
            this.audioURL = audioURL;
            this.extension = extension;
        }
        
        /** Method to read the current audio object */
        public void read()
        {
            try  {   data.readFile(audioURL);  }
            catch (Exception e) {}
        }
    }       // End of AudioObject class.
}
