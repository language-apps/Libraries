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
package org.acorns.audio.timedomain;


import java.awt.*;
import java.util.*;

import org.acorns.data.*;
import org.acorns.audio.frequencydomain.*;
import org.acorns.audio.*;

/** Change the pitch using the PSOLA algorithm
 * 
 * Contributed by: John Louie, Winter 2011
 * Edited by: Dan Harvey, Summer 2011
 */
public class Psola 
{
    private static final int WIN_SIZE = 50;   // Window size in milliseconds (large enough for four periods)
    private static final int MIN_WIN  = 5;    // Min window in milliseconds
    private static final int MAX_VOICED_FREQUENCY = 2000;
    private static final int UNVOICED_WIN = 5; // Unvoiced window (milliseconds)
    private static final int MIN_RECORDS = 20; // Smallest signal to modify
    
    private Filter   filter;
    private double[] clip;       
    private double sampleRate;
    private ArrayList<Point> pitchPoints;

    /** Constructor to create an audio faster or slower than the original
    *  @param sound audio object
    *  @param bounds initial amplitude offset (x), final amplitude offset (y)
    *  @return true if successful, false otherwise
    */
    public Psola(SoundData sound, Point bounds)
    {
        filter = new Filter();

        TimeDomain timeDomainObject = new TimeDomain(sound);
        clip = timeDomainObject.getTimeDomainFromAudio(bounds.x, bounds.y);
        sampleRate = sound.getFrameRate();
        
        int wSize = (int)((sampleRate/1000)*WIN_SIZE);
        
        int pitchAlgorithm = SoundDefaults.getPitchDetect();
        Pitch pitch = null;
        switch (pitchAlgorithm)
        {
	        case SoundDefaults.HARMONICPRODUCT:
	        	pitch = new HarmonicProductSpectrum(sound, bounds);
	        	break;
	        case SoundDefaults.YIN:
	        	pitch = new Yin(sound, bounds, wSize);
	        	break;
	        case SoundDefaults.CEPSTRUM:
	        	pitch = new Cepstrum(sound, bounds, wSize);
	        	break;
        }
        pitchPoints = hpsPitchPoints(sampleRate, pitch);
    }
    
    /** Change rate of speed
     * 
     *  @param rate   rate to speed up(+) or slow down (-) (.1 = 10%)
     * @return The modified speech 
     */
    public double[] getModifiedRate(double rate)
    { 
        if (pitchPoints.size()<MIN_RECORDS) return null;  // Failed, too small
        return alterSignalSize(rate, pitchPoints); 
    }
    
    /** Calculate locations of the pitch points using harmonic product spectrum
     *  @param sampleRate audio samples per second
     *  @param hpc Pitch detection object
     *  @return array of pitch point locations
     */
    private ArrayList<Point> hpsPitchPoints
                               (double sampleRate, Pitch hps)
    {
        ArrayList<Point> pitchpnts = new ArrayList<Point>();
        int minSamples = (int)(sampleRate * MIN_WIN / 1000);
        int unvoicedSamples = (int)(sampleRate * UNVOICED_WIN / 1000);

        int pitchPeriod = minSamples;
        int newPitchPeriod;

        //determine the sample rate and make windows based on that
        int wSizeInSamples = (int)((sampleRate/1000)*WIN_SIZE);
        
        
        // Insure that window size is a power of 2
        wSizeInSamples--;
        for (int i=1; i<=16; i*=2) wSizeInSamples |= wSizeInSamples>>i;
        wSizeInSamples++;

        int wStart = 0, wEnd, epoch;
        Point epochPoint;
        while (wStart<clip.length)
        {
            //for each window, calculate the pitch period
            wEnd = Math.min(wStart + wSizeInSamples, clip.length);
            if (wStart < wEnd) 
            {
                if (!isVoiced(wStart, wEnd, sampleRate))
                {
                    newPitchPeriod = unvoicedSamples;
                }
                else 
                {
                    newPitchPeriod = (int)hps.getPitch(wStart, wEnd);
                    if (newPitchPeriod>minSamples) 
                    { 
                        pitchPeriod = newPitchPeriod;
                    }
                }
                // At end of the PSOLA region, the last clip could be small,
                // Make sure the period isn't greater than the frame length
                if (pitchPeriod>wEnd-wStart) {newPitchPeriod = wEnd-wStart; }
                
                epochPoint = findEpoch(wStart, pitchPeriod);
                epoch = epochPoint.y;
                pitchPeriod = epochPoint.x;
                
                //store the timeDomain index jumping over the length of a pitch period
                for(int i = epoch; i < wEnd; i+=pitchPeriod)
                {
                    pitchpnts.add(new Point(pitchPeriod, epoch));
                    epoch += pitchPeriod;
                }
                wStart = wEnd;  // Move to next window
            }
        }
        return pitchpnts;
    }
    
    private boolean isVoiced(int wStart, int wEnd, double sampleRate)
    {

        int crossings = TimeDomain.zeroCrossings(clip, wStart, wEnd, 1);
        double periodInMillis = (wEnd-wStart)/sampleRate;
        double frequency = crossings/(2 * periodInMillis);
        return frequency < MAX_VOICED_FREQUENCY;
    }
   
    
    /** Find epoch point in the frame
     * 
     * @param wStart Start of frame
     * @param period The pitch period
     * @return  The period size (.x) and offset (.y)
     */
    private Point findEpoch(int wStart, int period)
    {

        double min = Double.MAX_VALUE;
        int minSpot = wStart, minPeriod = period, thisPeriod, spot, nextSpot, thirdSpot;
        
        double value;
        for (int delta=-50; delta<=50; delta++)
        {   
            thisPeriod = period + delta;
            for (int i=0; i<period+delta; i++)
            {   
                spot = wStart + i;
                nextSpot = spot + thisPeriod;
                thirdSpot = spot + 2*thisPeriod;
                
                if (spot>=clip.length) break;
                if (nextSpot >= clip.length) break;
                if (thirdSpot >= clip.length) break;
                
                if (clip[spot]*clip[nextSpot]<0) continue;
                if (clip[spot]*clip[thirdSpot]<0) continue;
                if (clip[spot]>0) continue;
                
                value = clip[spot]+clip[nextSpot] + clip[thirdSpot];

                if (value < min)
                {
                    minSpot = spot;
                    min = value;
                    minPeriod = period + delta;
                }
            }
        }
        return new Point(minPeriod, minSpot);
    }
    
    /** Apply a window filter to the framed signal
     *  @param frame    A single frame of the overlapped signal
     *  @return The frame with the window applied
     */
    private double[] windowSignal(double[] frame)
    {
        int frameLength = frame.length;
        double[]  windowSignal = new double[frameLength];
        double [] windowFilter = filter.createHanningWindow(frameLength);
 
        //apply the window to the time domain (non-complex values)
        for(int i=0;i<frameLength;i++)
        {
            windowSignal[i] = frame[i] * windowFilter[i];
        }
        return windowSignal;
    }
    
    /** Alter the signal appropriately according  to the rate change
     * 
     * @param rate Rate change fraction (0<rate<1)
     * @param epochs The pitch points
     * @return The altered signal
     */
    private double[] alterSignalSize(double rate, ArrayList<Point> epochs)
    {
        double[] result = null;

        int increment = (int)(Math.round(1.0 / (rate - 1)));
        if (increment==0) return clip;
        
        int out=0, lastEpoch = 0, size = clip.length;
        Point epoch;
        
        int minEpoch = 0;
        for (minEpoch=1; minEpoch<epochs.size(); minEpoch++)
        {
            if (epochs.get(minEpoch-1).y - epochs.get(minEpoch).x >=0) break;
        }
        if (minEpoch>=epochs.size()-2) return null;

        if (increment > 0) 
        {   
        	for (int record=increment; record<epochs.size()-2; record+=increment)
            {
                epoch = epochs.get(record-1);
                size -=(epochs.get(record+1).y - epoch.x - epoch.y);
            }
            
            result = new double[size];
            
            for (int record=increment; record<epochs.size()-2; record+=increment)
            {
                size = epochs.get(record-1).y - lastEpoch;
                if (size>0)
                  System.arraycopy(clip, lastEpoch, result, out, size);
                else
                	size = 0;
                
                out += size;
                remove(result, out, epochs, record);
                epoch = epochs.get(record-1);
                out += epoch.x;
                lastEpoch = epochs.get(record+1).y;
            }
            
            size = clip.length - lastEpoch;
            System.arraycopy(clip, lastEpoch, result, out, size);
        }
        else
        {   increment = -increment;
            for (int record=increment; record<epochs.size()-2; record+=increment)
            {
                size += epochs.get(record).x;
            }
            
            result = new double[size];
            double[] frame;
            
            for (int record=increment; record<epochs.size()-2; record+=increment)
            {
            	// copy clip up to current frame
                epoch = epochs.get(record);
                size = epoch.y + epoch.x - lastEpoch;
                if (size>0)
                	System.arraycopy(clip, lastEpoch, result, out, size);
                else
                	size = 0;
                
                out += size;
               
                // copy current frame to work area for windowing
                frame = new double[epoch.x];
                System.arraycopy(clip, epoch.y, frame, 0, epoch.x);
                frame = windowSignal(frame);
                
                // Duplicate the frame in the output 
                for (int index = 0; index<frame.length/2; index++)
                {
                	result[out++] = frame[index] + frame[index+frame.length/2];
                }
                
               	System.arraycopy(clip, epoch.y + epoch.x/2 , result, out, (epoch.x+1)/2);
                out += (epoch.x + 1)/2;

// This version instead of the above windowing has less artifacts, but more distortion
//				System.arraycopy(clip, epoch.y, result, out, epoch.x);
//                out += epoch.x;
                lastEpoch = epoch.y + epoch.x;
            }
            
            size = clip.length - lastEpoch;
            if (out+size>= result.length) 
            	size = result.length-out;
            
            System.arraycopy(clip, lastEpoch, result, out, size);
        
        }
        return result;
    }
    
    /** Method to remove a frame from the audio signal 
     * 
     * @param result The audio signal
     * @param offset The offset to the output
     * @param pitchPoints The array list of pitch points
     * @param num The frame to remove
     */
    private void remove(double[] result, int offset, ArrayList<Point> epochs, int num)
    {
        Point prevPoint = epochs.get(num - 1);
        int start = Math.max(0,  prevPoint.y - prevPoint.x);
        int size = Math.min(prevPoint.x*2, clip.length - start );
        double[] prev = new double[size];
        System.arraycopy(clip, start, prev, 0, size );
        prev = windowSignal(prev);
        
        Point nextPoint = epochs.get(num + 1);
        start = Math.max(0,  nextPoint.y - nextPoint.x);
        size = Math.min(nextPoint.x*2, clip.length - start );
        double[] next = new double[size];
        System.arraycopy(clip, start, next, 0, size );
        next = windowSignal(next);
        
        start = prevPoint.x;
        size = Math.min(prev.length - prevPoint.x, result.length - offset );
        try{
        System.arraycopy(prev, start, result, offset, size);
        }catch(Exception e)
        {
        	System.out.println(e);
        }
        start = Math.max(0, prevPoint.x - nextPoint.x);
        int delta = Math.max(0, nextPoint.x - prevPoint.x);
        for (int i=start;  i<prevPoint.x && offset+i<result.length; i++)
        { 
            result[offset + i] += next[(i-start) + delta];             
        }
    }
 
}   // End of Psola class
