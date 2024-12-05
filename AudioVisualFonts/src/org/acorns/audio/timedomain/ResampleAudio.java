/*
 * ResampleAudio.java
 *
 *   @author  harveyd
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

import org.acorns.data.*;
import javax.sound.sampled.*;

public class ResampleAudio 
{
	// Useful speech is between these frequency ranges
	private final static double MIN_FREQ = 30;
	private final static boolean BUTTERWORTH = true;  // False to use a sinc filter
	
	/** Up or down sample an audio signal 
	 *
	 * The idea is to create a long sample, fill the gaps with zeroes
	 * which preserves, but spreads out the spectrum, and then remove
	 * high frequency aliases using a low pass filter. The last step
	 * is to extract, using regular intervals (every 147th in the
	 * above example, outputs from the filtered signal to build the new
	 * one.
	 * 
	 * We don't actually have to create a huge new signal, we can
	 * focus only outputting the filtered points that pertain to the output.
	 * 
	 * Note: If old = 44.1 KHZ and new = 48 KHZ,
	 *		then 44100 * 160 = 147 * 48000 = 7056000
	 *
	 *		This means a virtual signal 7056000 consists of the 
	 *		original with a non zero value every 160th sample. The 
	 *		low pass filter, applies to this virtual signal.
	 *
	 *	  @param signal The original signal to be re-sampled
	 *    @param oldRate The original frame rate in samples per second
	 *    @param newRate the desired frame rate in samples per second
	 *    @return The re-sampled signal
	 */
	public static double[] apply(double[] signal, int oldRate, int newRate)
	{		
		int common = gcd(oldRate, newRate);
		int oldFactor = newRate / common;
		int newFactor = oldRate / common;
		
		int virtualRate = oldRate * oldFactor;
		double low = MIN_FREQ / virtualRate;
		double high = Math.min(oldRate, newRate) * 0.50 / virtualRate;
		
		// Create the low pass filter and get the coefficients
		double[][] coefficients = new double[2][0];
		if (BUTTERWORTH) 
		{
			Butterworth butterworth = new Butterworth(low, high, true);
			coefficients = butterworth.getCoefficients();
		}
		else coefficients[0] = Filter.makeSincFilter(low, high, 100);
		
		// Create a ring buffer to hold the last part of the filtered signal
		double[] ring = new double[coefficients[0].length];
		
		// The buffer to hold the new signal
		double[] output = new double[signal.length * oldFactor / newFactor];
		
		// The IIR filter coefficients
		double[] a = coefficients[1], b = coefficients[0];
		
		double x, y;
		// Filter the original virtual signal to form the new one
		for (int index=0; index<signal.length * oldFactor; index++)
		{
			y = 0;
	        for (int j = 0; j < b.length; j++)
			{
	           if (index-j<0) break; 
			   x = ((index-j)%oldFactor == 0) ? signal[(index-j)/oldFactor] : 0;
			   y += b[j]*x;
			}
			       
			if (a!=null)
			{
			    for (int j = 1; j < a.length; j ++)
			    {
			    	if (index-j<0) break;
			        y -= a[j] * ring[(index - j)%ring.length];
			    }
			}
			ring[index%ring.length] = y;
			if (index/newFactor >= output.length) break; 
			if (index%newFactor == 0) output[index/newFactor] = y;
		}
		
		// Rough attempt to equalize the loudness to the original signal
		double signalAmplitude = 0, filteredAmplitude = 0;
		for (int index=0; index<signal.length; index++)
		{
			signalAmplitude += signal[index]*signal[index];
		}
		
		for (int index=0; index<output.length; index++)
		{
			filteredAmplitude += output[index]*output[index];
		}
		
		double ratio = Math.log(signalAmplitude / filteredAmplitude);
		for (int index=0; index<output.length; index++)
		{
			output[index] *= ratio;
		}
		return output;
	}
	
	/** Method to resample an audio signal using Java Sound resampling
	 * 
	 * @param frameRate Desired frame rate
	 * @param audio The audio object
	 * @return true if successful
	 */
	public static boolean apply(float frameRate, SoundData audio)
	{
		int oldRate = (int)audio.getFrameRate();
		if (oldRate == (int)frameRate) return true;
		
		AudioFormat format = audio.getAudioFormat();
		AudioFormat targetFormat = new AudioFormat
				(frameRate, format.getSampleSizeInBits(), format.getChannels(), true, format.isBigEndian() );
		audio.changeFrameRate(audio.getFrameRate(), targetFormat);
				
		return true;
	}
	
	/** Get the greatest common denominator of two integers */
	public static int gcd(int first, int second)
	{
		if (first>second) return gcd(second, first);
		if (first==0 || first==second) return second;
		
		return gcd(second%first, first);
	}
}   // End of ResampleAudio class
