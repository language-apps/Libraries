/**
 *   SoundEditor.java
 *
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
package org.acorns.editor;

import java.awt.*;
import java.beans.*;
import javax.sound.sampled.*;
import org.acorns.data.*;
import org.acorns.audio.*;
import org.acorns.audio.frequencydomain.FastFourierTransform;
import org.acorns.audio.timedomain.*;

public class SoundEditor 
{
    private static final int NORMLEN = 2; // Normalize average signal length.
    
    private SoundData sound;
    private Annotations annotations;
    private double[] timeDomain;
    private SoundProperties properties;
    private UndoRedo undoredo;
    private TimeDomain timeDomainObject;

    private static AudioFormat copyFormat;
    
    /** Creates a new instance of Sound Editor
     *  @param sound Object holding recorded sound
     */
    public SoundEditor
            (Annotations annotations, SoundData sound, UndoRedo undoredo)
    {
        this.annotations = annotations;
        this.sound       = sound;
        this.undoredo    = undoredo;
        timeDomainObject = new TimeDomain(sound);
        timeDomain = timeDomainObject.getTimeDomainFromAudio();   

        if (timeDomain!=null)
        {
            TimeDomain.removeDC(timeDomain);
            timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        }
        
        // Get clip board for cutting and pasting.
        PropertyChangeListener[] pcl = 
           Toolkit.getDefaultToolkit().getPropertyChangeListeners("Properties");
        
       properties = null;
       for (int i=0; i<pcl.length && properties==null; i++)
       {   try
           {   properties = (SoundProperties)pcl[0];
           }
           catch (ClassCastException ic) {}
       }
    }
    
    /** Method to find the maximum amplitude of the time domain.
     * 
     * @return maximum amplitude.
     */
    public double getMaximumAmplitude()
    {  return timeDomainObject.getMaximumAmplitude(); }
        
    /** Cut a piece out of a sound for copying and remove it
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     *
     */    
    public boolean cut(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;

        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
  
        // Extract the clip.
        double[] clip    = new double[bounds.y - bounds.x + 1];
        clip[0] = sound.getFrameRate();
        System.arraycopy(timeDomain, bounds.x - 1, clip, 1, clip.length-1);
        
        // Delete the clip from the wave.
        double[] newTime = new double[timeDomain.length - (bounds.y-bounds.x)];
        
        // Remove annotations and shift the higher ones over.
        annotations.delete(bounds, true);
        annotations.shift(bounds, true, false);
        System.arraycopy(timeDomain, 0, newTime, 0, bounds.x);
        int back = timeDomain.length - bounds.y;
        System.arraycopy(timeDomain, bounds.y,newTime, bounds.x, back);
        
        timeDomain = newTime;
                
        // Save clip in system properties for pasting.
        properties.setClip(clip);
        copyFormat = sound.getAudioFormat();
        
        timeDomainObject.deleteAudio(bounds);
        return true;
    }
    
    /** Delete a piece of a sound
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     *
     */    
    public boolean delete(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;

        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
        double[] newTime = new double[timeDomain.length - (bounds.y-bounds.x)];
        
        // Delete the portion of the sound wave and shift annotations down.
        annotations.delete(bounds, true);
        annotations.shift(bounds, true, false);
        
        System.arraycopy(timeDomain, 0, newTime, 0, bounds.x);
        int back = timeDomain.length - bounds.y;
        System.arraycopy(timeDomain, bounds.y, newTime, bounds.x, back);
        
        timeDomain = newTime;
        timeDomainObject.deleteAudio(bounds);
        return true;
    }
    
    /** Reset a recorded sound to a second of silence
     *  @return true if successful, false otherwise
     *
     */    
    public boolean resetSound()
    {
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 

        int frames = (int)sound.getFrameRate();
        double[] newTime = new double[frames];
        for (int c=0; c<frames; c++)  
        {    newTime[c] = 0; }
        
        timeDomain = newTime;
        annotations.delete(new Point(-1,-1), true);
        
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        return true;
    }
    
    /** Copy a section of a sound for copying
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     *
     */
    public boolean copy(int startCut, int endCut)
    {   
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        
        double[] clip    = new double[bounds.y - bounds.x + 1];
        clip[0] = sound.getFrameRate();
        System.arraycopy(timeDomain, bounds.x, clip, 1, clip.length-1);
        
        // Insert clip into  system property class.
        properties.setClip(clip);
        copyFormat = sound.getAudioFormat();
        return true;
    }
    
    /** Duplicate a section of a sound and return the clip
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     */
    public boolean duplicate(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
        double[] clip    = new double[bounds.y - bounds.x];
        double[] newTime = new double[clip.length+timeDomain.length];
        annotations.shift(bounds, true, true);
        
        System.arraycopy(timeDomain, 0, newTime, 0, bounds.y);
        System.arraycopy(timeDomain, bounds.x, newTime, bounds.y, clip.length);
        System.arraycopy(timeDomain, bounds.x, clip, 0, clip.length);

        int len = timeDomain.length - bounds.y;
        System.arraycopy(timeDomain,bounds.y,newTime,bounds.y+clip.length,len);
        
        timeDomain = newTime;
        timeDomainObject.insertAudio(clip, new Point(startCut, startCut));
        return true;
    }
    
    /** Paste a section of a sound
     *  @param startCut Place to insert sound clip 
     *  @param endCut Offset of the end of the insertion spot
     *  @return true if successful, false otherwise
     */
    public boolean paste(int startCut, int endCut)
    {
        // Get selected area.
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 

        // Get soundClip and recording rate from system property class
        double[] data = properties.getClip();
        if (data==null) return true;
        if (data.length < 1) return true;

        float oldFrameRate = (float)data[0];
        double[] clip = new double[data.length - 1];
        System.arraycopy(data, 1, clip, 0, data.length-1);
        
        // If recording rate changed, we need to scale the data
        if ((int)oldFrameRate != (int)sound.getFrameRate())
        {
           String[] text = {"", "", "", ""+clip[0], ""};

           SoundData newSound = new SoundData
                                     (text, copyFormat, new byte[1]);
           TimeDomain newTimeDomainObject = new TimeDomain(newSound);
           newTimeDomainObject.saveTimeDomainIntoAudio(clip);
           newSound.changeFrameRate(oldFrameRate, sound.getAudioFormat());
           clip = newTimeDomainObject.getTimeDomainFromAudio();            
        }
        
        if (!replace(bounds.x, bounds.y, clip)) return false;
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);

        if (bounds.y>bounds.x)
        {  annotations.delete(bounds, true);
           annotations.shift(bounds, true, false);
        }
        annotations.shift(new Point(bounds.x, bounds.x+clip.length), true,true);
        return true;
    }
    
    /** Replace a section of a sound
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @param clip Integer array of amplitudes in a sound clip
     *  @return true if successful, false otherwise
     */
    private boolean replace(int startCut, int endCut, double[] clip)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;

        // Create a new time array.
        int middle  = bounds.y - bounds.x;
        int newSize = timeDomain.length + clip.length - middle;
        double[] newTime = new double[newSize];
        
        // Copy the front part.
        System.arraycopy(timeDomain, 0, newTime, 0, bounds.x);
        // Copy the clip.
        System.arraycopy(clip, 0, newTime, bounds.x, clip.length);
        // Copy the trailing part.
        int front = bounds.x + clip.length;
        int endSize = newTime.length - front;
        System.arraycopy(timeDomain, bounds.y, newTime, front, endSize);
        timeDomain = newTime;
        return true;
    }
    
    /** Create a silent clip 
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     */
    public boolean silence(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
        annotations.delete(bounds, true);
        for (int i=bounds.x; i<bounds.y; i++) timeDomain[i] = 0;
   
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        return true;
    }
    
    /** Create a silent clip 
     *  @param startCut Initial offset
     *  @param endCut   Final offset
     *  @param speedUp  amount of  speed up (+) or slowdown (-):  (.1 = 10%)
     *  @return true if successful, false otherwise
     */
    public boolean rateChange(int startCut, int endCut, boolean speedUp)
    {   
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        
        float changeRate = SoundDefaults.getRateChange();
        if (speedUp) changeRate = (100 + changeRate)/100;
        else         changeRate = (100 - changeRate)/100;
        
        int rateAlg = SoundDefaults.getRateAlgorithm();
        if (rateAlg == SoundDefaults.DEFAULTRATEALG)
        {   // Default algorithm ignores selection, applies to the entire signal
            if (!sound.changeSpeed(changeRate)) return false;
        }
        else
        {   
           Psola psola = new Psola(sound, new Point(bounds));
           double[] newTime = psola.getModifiedRate(changeRate);
           if (newTime==null) return false;
            
           undoredo.pushUndo(annotations.undoRedoObject()); 
           if (!replace(bounds.x, bounds.y, newTime)) return false;
           timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        }
        sound.setDirty();
        return true;
    }

    /** Perform algorithms to filter a signal
     *  @param startCut Initial offset
     *  @param endCut   Final offset
     *  @param P Number of LPC coefficients
     *  @return true if successful, false otherwise
     */
    public boolean filterSignal(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;

        double  frameRate = sound.getFrameRate();
        int clipSize = bounds.y - bounds.x;
        double[] newData = null, clip = null;
        
        
        int filterType = SoundDefaults.getFilterType();
        double minFreq = SoundDefaults.getMinFreq() / frameRate;
        double maxFreq = SoundDefaults.getMaxFreq() / frameRate;
        if (filterType != SoundDefaults.SPECTRAL)
        {
        	clip = new  double[clipSize];
        	System.arraycopy(timeDomain, bounds.x, clip, 0, clipSize);
        }
        
        switch (filterType)
        {
            case SoundDefaults.BANDPASS: // Uses a Butterworth filter
                Butterworth btw;
                if (maxFreq<=0 || maxFreq>5000) btw = new Butterworth(minFreq, false);
                else btw = new Butterworth(minFreq, maxFreq, true);
                newData = btw.applyFilter(clip, true);
                break;
                
            case SoundDefaults.WINDOWSINC:                
                Filter filter = new Filter();
                double[] bandPass = filter.makeWindowedSincFilter
                      (minFreq, maxFreq, SoundDefaults.getFilterSize());
                newData = filter.convolute(clip, bandPass, 0, clip.length);
                break;
                
            case SoundDefaults.WIENER:
                LinearPrediction lpcFilter = new LinearPrediction(clip, frameRate);
                newData = lpcFilter.lpc();
                if (newData==null) return false;
                break;
                
            case SoundDefaults.SPECTRAL:
                int[] params = FastFourierTransform.getFFTParams
                                  (true,frameRate, clipSize, bounds);
                FastFourierTransform fourier = new FastFourierTransform
                				  (params[FastFourierTransform.FFT_SIZE]);
                
                clip = timeDomainObject.getComplexTimeDomain(bounds, null);
                newData = fourier.spectralSubtraction(clip, params);
                break;

            default: return false;
        }
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject());
        if (!replace(bounds.x, bounds.y, newData)) return false;
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        return true;
    }


    /** Trim outside a selected portion of a sound 
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     */
    public boolean trim(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
        annotations.delete(new Point(0, startCut), true);
        annotations.delete(new Point(endCut, timeDomain.length), true);
        for (int i=0; i<bounds.x; i++) timeDomain[i] = 0;
        for (int i=bounds.y; i<timeDomain.length; i++) timeDomain[i] = 0;
        
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        return true;
    }
        
    /** Get current time domain array.
     *  @return array of amplitudes from the sound wave
     */
    public double[] getTimeDomain()   { return timeDomain; }
    
   /** Adjust the sound volume 
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @param factor  Increase or decrease factor (must be >= 0).
     *  @return true if successful, false otherwise
     */
    public boolean volume(int startCut, int endCut, float factor)
    {
        int value;
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;
        if (factor < 0)     return false;

       int maxValue = (1 << (sound.getFrameSizeInBytes() * 8 -1));

        for (int c=bounds.x; c<bounds.y; c++)
        {
            // Set new value and check if clipping needed.
            value = (int)(timeDomain[c] * factor);
            if (value<-maxValue) timeDomain[c] = -maxValue;
            else if (value>maxValue-1) timeDomain[c] = maxValue-1;
            else timeDomain[c] = value;            
        }
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        return true;
    }
  
     /** Normalize the sound volume 
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return true if successful, false otherwise
     */
    public boolean normalize(int startCut, int endCut)
    {
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return false;

        double max = findMax(bounds.x, bounds.y, NORMLEN);
        if (max<=0) return true;
        
        int maxValue = (1 << (sound.getFrameSizeInBytes() * 8 -1));
        if (sound.getAudioFormat().getEncoding().equals(AudioFormat.Encoding.ALAW)
         || sound.getAudioFormat().getEncoding().equals(AudioFormat.Encoding.ULAW))
            maxValue = 32768;
        
        double ratio = (double)maxValue / max;
        for (int c=bounds.x; c<bounds.y; c++)
        {
            timeDomain[c] = (int)(timeDomain[c]*ratio);
        }
        sound.setDirty();
        undoredo.pushUndo(annotations.undoRedoObject()); 
        timeDomainObject.saveTimeDomainIntoAudio(timeDomain);
        return true;
    }
    
    /** Set up sound selection 
     *  @param startCut Initial amplitude offset
     *  @param endCut   Final amplitude offset
     *  @return maximum absolute amplitude   
     */
    public double findMax(int startCut, int endCut)
    {   
        return findMax(startCut, endCut, 1);
    }
    
    
    // Internal method to find the average maximum.
    private double findMax(int startCut, int endCut, int avglength)
    {
       double value = 0, max=0;
        
        Point bounds = setSelection(startCut, endCut);
        if (bounds == null) return 0;        
        if (bounds.y-bounds.x<avglength) return 0;
        
        for (int a=bounds.x; a<bounds.y; a++)
        {
            value += timeDomain[a];
            if (value<0) value = -value;
            
            if (a-bounds.x >= avglength) 
            {   value = timeDomain[a-avglength];
                if (value<0) value = -value;
                value -= timeDomain[a-avglength];
            }
            if (value > max) max = value;    
        }
        return max / avglength;
    }
    

    // Get actual selected range.
    private Point setSelection(int startSelect, int endSelect)
    {
        // Create the timeDomain array if necessary.
        if (timeDomain==null)
        {   timeDomain = new double[endSelect + 1];   }
        
        if (timeDomain.length==0) return null;
        
        if (startSelect<0) startSelect = 0;
        if (endSelect <0)  endSelect   = timeDomain.length;
        
        if (startSelect>endSelect)       return null;
        if (endSelect>timeDomain.length) return null;
        
        return new Point(startSelect, endSelect);
    }
    
    /** Find the next frame that crosses zero amplitude
     *  @param frame number in the time domain
     *  @return next frame that crosses zero
     */
    public int roundToZero(int frame)
    {
        if (frame<0) frame = 0;
        if (timeDomain==null) return 0;
        if (frame>=timeDomain.length) return timeDomain.length;
        if (timeDomain[frame]==0) return frame;
        if (!SoundDefaults.getRoundToZero())  return frame;
        
        for (int f=frame+1; f<timeDomain.length; f++)
        {
            if (timeDomain[f]>=0 && timeDomain[f-1]<=0)  return f;
            if (timeDomain[f]<=0 && timeDomain[f-1]>=0) return f;
        }
        return timeDomain.length;
    }
    
    /** Get sound object */
    public SoundData getSound()  {  return sound; }
    
}   // End of SoundEditor class
