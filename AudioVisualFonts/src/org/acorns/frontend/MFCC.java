/*
 * Cepstrals.java
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

package org.acorns.frontend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.acorns.audio.NormalizeFrames;
import org.acorns.audio.SoundDefaults;
import org.acorns.audio.TimeDomain;
import org.acorns.audio.frequencydomain.Cepstrum;
import org.acorns.audio.frequencydomain.DiscreteCosineTransform;
import org.acorns.audio.frequencydomain.FastFourierTransform;
import org.acorns.audio.frequencydomain.MelFilterBank;
import org.acorns.audio.timedomain.Filter;
import org.acorns.data.SoundData;

/**
 *
 * Perform CEPSTRAL analysis of a speech signal
 */
public class MFCC
{
    private double frameRate;
    private int[]  params;           // FFT parameter values
    private double minFreq, maxFreq; // Min and max frequencies for MEL bins

    private double[]   complex;     // Signal data
    private double[]   spectrum;    // Signal power spectrum
    private double[]   bandPass;    // Band pass filter
    private double[]   melSpectrum; // Mel spectrum for a windowed frame
    private double[][] mfccList;    // Mel coefficients for audio signal frames

    private Filter filter;          // Create and apply time domain filters
    private MelFilterBank melFilter;       
    private static DiscreteCosineTransform transform;
    private FastFourierTransform fourier;

    public MFCC(SoundData sound, Point point)
    {
        // Get FFT parameters
        frameRate = sound.getFrameRate();
        int frames = sound.getFrames();
        params = FastFourierTransform.getFFTParams(true,frameRate,frames,point);
        fourier = new FastFourierTransform(params[FastFourierTransform.FFT_SIZE]);

        TimeDomain timeDomain = new TimeDomain(sound);
        complex = timeDomain.getComplexTimeDomain(point, null);
        spectrum = new double[params[FastFourierTransform.FFT_SIZE]];

        // Create and apply the window sinc filter.
        minFreq = SoundDefaults.getMinFreq() / frameRate;
        maxFreq = SoundDefaults.getMaxFreq() / frameRate;
        if (maxFreq > 0.5) maxFreq = 0.5;
        if (minFreq > maxFreq) minFreq = maxFreq;

        filter = new Filter();
        bandPass = filter.makeWindowedSincFilter
                      (minFreq, maxFreq, SoundDefaults.getFilterSize());

        melFilter = new MelFilterBank
                           (frameRate, params[FastFourierTransform.FFT_SIZE]);
        
         // (CEPSTRAL + one for energy) * 3 for deltas and delta deltas
        mfccList = new double[params[FastFourierTransform.FFT_WINDOWS]][];
    }   // End of constructor
 

    /** Process data, creating the MEL CEPSTRUM from an input spectrum frame
     * @param melspectrum input a MelSpectrum frame
     * @param Array to hold the output coefficeints
     * @return a MEL CEPSTRUM frame
     */
    public static double[] getMFCCs(double[] melspectrum, double[] coefficients) 
                                                 throws IllegalArgumentException
    {
        int numberOfMelFilters = SoundDefaults.getNumberOfMelFilters();
        if (melspectrum.length != numberOfMelFilters)
        {
            throw new IllegalArgumentException
                ("MelSpectrum size is incorrect: melspectrum.length == " +
                 melspectrum.length + ", numberMelFilters == " +
                 numberOfMelFilters);
        }

        // first compute the log of the spectrum
        melspectrum = Cepstrum.logCompression(melspectrum);

        // create the CEPSTRUM by apply the melcosine filter
        if (transform==null)
        {
            int cepstrumLength = SoundDefaults.getCepstrumLength();
            int filters  = SoundDefaults.getNumberOfMelFilters();
            transform = new DiscreteCosineTransform( filters, cepstrumLength );
        }

        double[] cepstrum = transform.applyForwardTransform(melspectrum, coefficients);
        return cepstrum;
    }

 
    /** Calculate Array of CEPSTRAL coefficients for the audio signal
     *
     * @return true if successful, false if not
     */
    public boolean calculateCepstral()
    {
        int step = params[FastFourierTransform.FFT_STEP] * 2;
        int FFTSize = params[FastFourierTransform.FFT_SIZE] * 2;

        // Apply FIR filter to emphasize upper frequencies
        Filter.preEmphasizer(complex, FFTSize/2, 0);

        // Process the audio signal, with windowing and overlapped step sizes
        int row = 0, startWindow;
        double[] window = new double[FFTSize];

        for (int offset=0; offset<complex.length; offset += step)
        {
           if (row == mfccList.length) break;
           startWindow = offset;
           if (startWindow + FFTSize - 1 >= complex.length)
           {
               startWindow = complex.length - FFTSize;
           }
           mfccList[row] = calculateCepstralWindow(window, startWindow, FFTSize);
           if (mfccList[row]==null) return false;
           row++;
        }
        
        NormalizeFrames.meanNormalization(mfccList, 0, SoundDefaults.getCepstrumLength());
        addDeltaValues();
        return true;
    }
    
    /** Add delta values to the Mel Coefficient array for a given frame 
     * 
     * @param frame The frame number
     */
    private void addDeltaValues()
    {
       int len = SoundDefaults.getCepstrumLength() + 1;
       double delta;

       for (int frame=1; frame<mfccList.length-1; frame++)
       {

    	   // Compute delta CEPSTRAL
    	   for (int cep=0; cep<len; cep++)
           {
               delta = (mfccList[frame+1][cep] - mfccList[frame-1][cep])/2;
               mfccList[frame][len+cep] = delta;
           }
       }
 
       for (int frame=1; frame<mfccList.length-1; frame++)
       {
    	   // Compute delta delta CEPSTRALs
    	   for (int cep=0; cep<len; cep++)
           {
               delta = (mfccList[frame+1][len+cep] - mfccList[frame-1][len+cep])/2;
               mfccList[frame][2*len+cep] = delta;
           }         
        }   
    }

    /** Calculate the CEPSTRAL coefficients for a particular window
     *
     * @param window The area to hold the audio signal window
     * @param start Start of the window
     * @param size End of the window
     * @return coefficients for this window
     */
    private double[] calculateCepstralWindow
                                          (double[] window, int start, int size)
    {      // Get section of complex signal
           System.arraycopy(complex, start, window, 0, size);
           double energy = TimeDomain.decibelLevel(window, 0, size, 2);
           
           // BandPass with windowing
           window = filter.applyFilterComplex(bandPass, window);
  
           window = fourier.fft(window);  // Perform the FFT
           if (window==null) return null;

           spectrum = FastFourierTransform.powerSpectrum(window, spectrum);  
           melSpectrum = melFilter.applyMelFilterBank(spectrum); 
           
           int cepSize = (SoundDefaults.getCepstrumLength() +1) * 3;
           double[] melCoefficients = new double[cepSize];
           melCoefficients =  getMFCCs(melSpectrum, melCoefficients);
           
           int len = SoundDefaults.getCepstrumLength();
           melCoefficients[len] = energy; 
           
           return melCoefficients;
    }      // End of calculateCepstralWindow
    
    
    /** Draw featureVector
     *
     * @param graphics Object onto which we draw
     * @param sound The object containing the audio signal
     * @param width The width of the display window
     * @param visible The portion of the display window that is visible
     * @return true if successful, false otherwise
     */
    public boolean drawFeatureVector(Graphics graphics, int width, Rectangle visible)
    {
        double mfccValue;

        // Compute which CEPSTRAL values to plot (default is all)
        int    len = SoundDefaults.getCepstrumLength() + 1;
        long   featureMask = SoundDefaults.getFeatureMask();
        featureMask &= (1<<len) -1;
        featureMask = featureMask | ((featureMask<<len) + (featureMask<<2*len));
        
        // Find the maximum and minimum feature vector amplitudes
        double min=0, max=0;
        for (int i=0; i<mfccList.length; i++)
        {
        	if (i==0)
        	{
        		min = max = mfccList[0][0];
        	}
            for (int j=0; j<mfccList[i].length; j++)
            {   
                mfccValue = mfccList[i][j];
                if (mfccValue<min)  min = mfccValue;
                if (mfccValue>max)  max = mfccValue;
            }
        }
        if (max==min) return false;
 
        // Create an array of web-safe colors
        Color[] colors = makeColorPalette();

        Point  lastPoint   = null, newPoint;
        int    pointHeight;
        double ratio       = (double)mfccList.length / width;
        double scale = Math.log(max-min), pointScale;
        
        int increment = 216 / mfccList[0].length;
        int index = 0;

        for (int h=0; h<mfccList[0].length; h++)
        {
           if ((featureMask & 1L<<h) != 0)  // Check if to process this feature
           {  
              graphics.setColor(colors[h*increment]);
              for (int w=visible.x; w<visible.x+visible.width; w++)
              {
                 index = (int)(w * ratio);
                 if (mfccList[index][h]!= max)
                	  pointScale = Math.log(max - mfccList[index][h]);
                 else pointScale = 0;

   				 pointHeight = (int)((visible.height-visible.y)*pointScale/scale);
                 newPoint = new Point(w, pointHeight);

                 if (w==visible.x) lastPoint = newPoint;
                 else if (!lastPoint.equals(newPoint))
                 {  
                	graphics.drawLine(lastPoint.x, lastPoint.y, w, pointHeight);
                    lastPoint = newPoint;
                 }
              }   // End for
           }      // End if
        }
        return true;
    }
    
   /** Draw squares of the EUCLIDEAN distances between frames
    *
    * @param graphics Object onto which we draw
    * @param sound The object containing the audio signal
    * @param width The width of the display window
    * @param visible The portion of the display window that is visible
    * @return true if successful, false otherwise
    */
    public boolean drawFrameDistances(Graphics graphics, int width, Rectangle visible)
    {
       int SNR_DELTA_ENERGY = 3;
       
       double max=0, minEnergy = 0, value;
       double[] distances = new double[mfccList.length];
       int    len = SoundDefaults.getCepstrumLength() + 1;

       for (int i=0; i<mfccList.length; i++)
       {
    	   value = mfccList[0][len-1];
    	   if (i==0 || value<minEnergy) minEnergy = value;
       }

       for (int i=0; i<mfccList.length; i++)
       {
    	   
    	   if (i==0) { max = distances[0] = 0; }
    	   else 
    	   {
    		   // Remove non-speech frames. (Energy < minimum + delta)
    		   if (mfccList[i][len-1] < minEnergy + SNR_DELTA_ENERGY)
    			    distances[i] = 0;
    		   else distances[i] = frameDistance(mfccList[i], mfccList[i-1], len);
    		   if (distances[i]>max) max = distances[i];
    	   }
       }

       if (max==0) return false;
       
       Point  lastPoint   = null, newPoint;
       int    pointHeight, index = 0;
       double pointScale, ratio = (double)mfccList.length / width;

       graphics.setColor(Color.WHITE);
       for (int w=visible.x; w<visible.x+visible.width; w++)
       {
	        index = (int)(w * ratio);
            if (distances[index]!= max)
          	     pointScale = max - distances[index];
            else pointScale = 0;
	
  		    pointHeight = (int)((visible.height-visible.y)*pointScale/max);
	        newPoint = new Point(w, pointHeight);
	
	        if (w==visible.x) lastPoint = newPoint;
	        else if (!lastPoint.equals(newPoint))
	        {  
	        	graphics.drawLine(lastPoint.x, lastPoint.y, w, pointHeight);
	        	lastPoint = newPoint;
	        }
       }      // End if
       return true;
    }		// End drawFrameDistance()

    /** Make a web-safe color palette for displaying features
     *
     * @return array of colors
     */
    private Color[] makeColorPalette()
    {
        Color[]  colors = new Color[216];
        int index = 0;
        int[] inc = {255, 204, 153, 102, 51, 0};
        for (int red=0; red<inc.length; red++)
          for (int blue=0; blue<inc.length; blue++)
            for (int green=0; green<inc.length; green++)
            {  colors[index++] = new Color(inc[red], inc[blue], inc[green]);  }
        return colors;
    }   // End of makeColorPalette()
    
	/** Compute the square of the EUCLIDEAN distance between two frames
	 * 
	 * @param source The source array of features for the frame
	 * @param destination The destination array of features for the frame
	 * @param number of values to check in the frame
	 * @return sum of squares of the distance
	 */
	public double frameDistance(double[] source, double[] destination, int len)
	{
		int length = Math.max(source.length, destination.length);
		length = Math.min(length, len);
		
		double distance = 0, difference;
		for (int i=0; i<length; i++)
		{
			if (i>=source.length) difference = destination[i];
			else if (i>=destination.length) difference = source[i];
			else difference = source[i] - destination[i];
			
			distance += difference * difference;
		}
		return Math.sqrt(distance);		
	}
}       // End of Cepstrals class
