package org.acorns.audio;

import java.awt.Point;

import org.acorns.audio.frequencydomain.FastFourierTransform;
import org.acorns.audio.timedomain.Filter;
import org.acorns.data.SoundData;

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


public abstract class Pitch 
{
	/** Smallest expected fundamental frequency */
	public static final int MIN_F0 = 80;
	
	/** Maximum expected fundamental frequency */
	public static final int MAX_F0 = 250;
	
	/** Minimum male fundamental frequency (normal range) */
	public final static int MIN_MALE = 100;
	
	/** Maximum female fundamental frequency (norml range) */
	public final static int MAX_FEMALE = 220;
	
	/** Minimum pitch for bandwidth filtering of the signal */
	public final static int MIN_PITCH_FREQ = 30;
	
	/** Maximum pitch for bandwidth filtering of the signal */
	public final static int MAX_PITCH_FREQ = 1200;

    protected double[] complex;
    protected int FFTSize;

    protected FastFourierTransform fourier;
    protected double[] amplitudes;
    protected double frameRate;
    protected Filter filter;
  
    public Pitch(SoundData sound, Point bounds, int wSize)
    {
        frameRate = sound.getFrameRate();
        fourier = new FastFourierTransform(wSize);
        FFTSize = fourier.getFourierSize();

        complex = new double[2 * FFTSize]; // If too small, TimeDomain corrects
        TimeDomain timeDomain = new TimeDomain(sound);
        complex = timeDomain.getComplexTimeDomain(bounds, complex);
        amplitudes = new double[FFTSize / 2 + 1];
        filter = new Filter();
    }
    
    /** Constructor for computing pitch without the audio signal data
     * 
     * @param frameRate The audio frame rate
     */
    public Pitch(double frameRate, int fftSize)
    {
    	this.frameRate = frameRate;
    	this.FFTSize = fftSize;
    	amplitudes = new double[FFTSize / 2 + 1];
    }
    
    /** Constructor for pitch without audio signal, needing a filter object 
     * 
     * @param frameRate The audio frame rate
     */
    public Pitch(double frameRate) 
    {
    	this.frameRate = frameRate;
    	filter = new Filter();
    }
    
	public abstract double getPitch(double[] spectrum);
    public abstract double getPitch(int wStart, int wEnd);
}
