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
package org.acorns.audio.frequencydomain;

import java.awt.*;

import org.acorns.data.*;
import org.acorns.audio.*;

/** Calculate the fundamental frequency (F0) using the HARMONIC product spectrum
 *      algorithm. 
 * 
 * Contribution by John Louie, Winter 2011
 * Modifications by Dan Harvey, Summer 2011
 * 
 * Copyright 2007-2015, all rights reserved
 * 
 */
public class HarmonicProductSpectrum extends Pitch
{
	private final static int HARMONIC_FFT_SIZE = 2048;
	
    public HarmonicProductSpectrum(SoundData sound, Point bounds)
    {
    	super(sound, bounds, HARMONIC_FFT_SIZE);
    }
    
    /** Constructor for computing pitch without the audio signal data
     * 
     * @param frameRate The audio frame rate
     */
    public HarmonicProductSpectrum(double frameRate, int fftSize)
    {
    	super(frameRate, fftSize);
    }
    
    /** Compute pitch using a windowed FFT window
     *  @param window Windowed FFT window
     *  @return estimated pitch frequency in Hz or -1 if none detected
     */
    public double getPitch(double[] window)
    {
        //multiple the FFT signal with it's 1/2 and 1/3 resolution values
        FastFourierTransform.amplitudes(window, amplitudes, false, -1);
        double curValue = 0;
        double iHalfValue=0, iThirdValue=0, iFourthValue=0, iFifthValue=0;
        int iHalfIndex = 0, iThirdIndex = 0, iFourthIndex = 0, iFifthIndex = 0;

        double hzPerBin = frameRate / (window.length/2);
        for(int i = 0; i < amplitudes.length; i++)
        {
           iHalfIndex = i*2;
           iThirdIndex = i*3;
           iFourthIndex = i*4;
           iFifthIndex = i*5;
           if (iHalfIndex >= amplitudes.length)
                { iHalfValue = 1;  }
           else { iHalfValue = amplitudes[iHalfIndex];  }

           if (iThirdIndex >= amplitudes.length)
                { iThirdValue = 1; }
           else { iThirdValue = amplitudes[iThirdIndex]; }

           if (iFourthIndex >= amplitudes.length)
                { iFourthValue = 1; }
           else { iFourthValue = amplitudes[iFourthIndex]; }

           if (iFifthIndex >= amplitudes.length)
                { iFifthValue = 1; }
           else { iFifthValue = amplitudes[iFifthIndex]; }

           amplitudes[i] = Math.log(amplitudes[i]*iHalfValue*iThirdValue*iFourthValue*iFifthValue);
        }

        //find the peak in the HPT FFT signal

        // Note: energy is in the first bin, so we want to not consider that one
        // Therefore we add one to the start and end bin
        double max = 0;
        int startBin = (int)(MIN_F0 / hzPerBin) + 1;
        int endBin = (int)(Math.min(amplitudes.length, MAX_F0 / hzPerBin) + 1);

        int maxBin = startBin;
        for (int i = startBin; i < endBin; i++)
        {
            curValue = amplitudes[i];
            if (curValue > max) 
            {
                max = curValue;
                maxBin = i - 1;
            }
        }
        if (maxBin==0)  return -1;

        //convert from Hz to samples
        double freqInHz = (maxBin + 0.5) * hzPerBin;
    	return freqInHz;
    }

    /** Calculate the pitch period for the frame using Harmonic Product Spectrum
     *  @param start Initial offset
     *  @param end   Final offset
     *  @return pitch frequency in Hz or -1 if no picth detected
     */
    public double getPitch(int wStart, int wEnd)
    {
        //calculate the FFT of the time domain from wStart to wEnd
        double[] window = new double[FFTSize * 2];

        int windowSize = wEnd - wStart;
        System.arraycopy(complex, wStart, window, 0, windowSize*2);
        
        double[] windowFilter = filter.makeWindowFilter(windowSize);
        filter.applyWindow(windowSize, windowFilter, window);
        
        //run the FFT
        double[] fft = fourier.fft(window);
        return getPitch(fft);
    }
    
}   // End of HarmonicProductSpectrum class
