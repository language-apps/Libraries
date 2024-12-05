/*
 * CepstrumPitch.java
 *
 * Created on March 26, 2012
 *
 *
 *   @author  HarveyD
 *   @version 7.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.audio.frequencydomain;

import java.awt.Point;

import org.acorns.audio.*;
import org.acorns.data.SoundData;

/** Calculate the fundamental frequency (F0) using the CEPSTRUM approach
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
public class Cepstrum extends Pitch
{
	private final static double EPSELON = .00001;
	
	private FastFourierTransform fourier;
	
	public Cepstrum(double frameRate, int fftSize)
	{
		super(frameRate, fftSize);
		fourier = new FastFourierTransform(fftSize);
	}
	
    public Cepstrum(SoundData sound, Point bounds, int wSize)
    {
    	super(sound, bounds, wSize);
		fourier = new FastFourierTransform(wSize);
    }

	/** Get the CEPSTRUM of a FFT spectrum
	 * 
	 * @param spectrum The input spectrum
	 * @return The associated CEPSTRUM
	 */
	public double[] getCepstrum(double[] spectrum)
	{
		double[] amplitudes = new double[spectrum.length];
        for (int bin=0; bin<spectrum.length; bin+=2)
        {
        	amplitudes[bin] = Math.log(EPSELON + Math.sqrt(spectrum[bin]*spectrum[bin] + spectrum[bin+1]*spectrum[bin+1]));
        	amplitudes[bin+1] = 0;
        }
        
        // Log of amplitudes to separate pitch (then perform DCT to form cepstrum)
		double[] cepstrum = fourier.ifft(amplitudes);
		return cepstrum;
		
	}

	/** Create the log of the Cepstrum amplitude values in preparation of the
	 *     discrete cosine transform
     *
     *  @param melValues the spectrum
     *  @return the log compressed spectrum
     */
    public static double[] logCompression(double [] amplitudes)
    {
        double [] logCompressed = new double[amplitudes.length];
        for(int i=0;i<amplitudes.length;i++){
            logCompressed[i]=Math.log(.00001 + amplitudes[i]);
        }
        return logCompressed;
    }
    
    /** Calculate the pitch period for the frame based on the CEPSTRUM of a window
     *  @param start Initial offset
     *  @param end   Final offset
     *  @return double of the pitch frequency in Hz or -1 if no pitch detected
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
        double freqInHz = getPitch(fft);
        return freqInHz;
    }
	
	/** Compute the estimated pitch
	 * 
	 * @param spectrum FFT windowed spectrum
	 * @return pitch frequency in Hz or -1 if no pitch detected
	 */
	public double getPitch(double[] spectrum)
	{
        double[] cepstrum = getCepstrum(spectrum);
 
        int startBin, endBin, maxBin = cepstrum.length;
        
        double value, maxValue = -1;
    	startBin = (int)(frameRate/ MAX_F0);
    	double[][] range = { {frameRate / MIN_MALE, frameRate/MAX_FEMALE},
    			             {frameRate/MIN_F0, frameRate/MAX_F0} };
    	
    	for (int r=0; r<range.length; r++)
    	{
    		startBin = (int)(range[r][1]);
    		endBin = maxBin = (int)(range[r][0]);
    		if (endBin>=cepstrum.length) endBin = cepstrum.length;
    	
	        for (int bin=endBin;  bin>startBin; bin--)
	        {
	        	value = cepstrum[bin];
	        	if (bin==endBin || value > maxValue) 
	        	{
	        		maxValue = value;
	        		maxBin = bin;
	        	}
	        }
        	if (maxValue!=-1) break;
    	}
  
        double pitch = frameRate / maxBin;
		return pitch;
	}
	
    /** CEPSTRAL Normalization
     *  Each column will have the fraction of the total
     *
     *  @param cepstrum CEPSTRUM spectrum
     */
    public static double[] cepstralNormalization(double[] cepstrum)
    {
       double sum;
       int len = cepstrum.length;

       sum = 0;
       for(int column=0; column<len; column++)  { sum += cepstrum[column]; }

       for (int column=0; column<len; column++)  
       {
      	  cepstrum[column] /= sum;
       }
       return cepstrum;
    }

	
}	// End of Cepstrum class
