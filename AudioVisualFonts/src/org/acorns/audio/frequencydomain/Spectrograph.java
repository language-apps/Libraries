/*
 * Spectrograph.java 
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

package org.acorns.audio.frequencydomain;

import java.awt.*;

import org.acorns.data.*;
import org.acorns.audio.*;
import org.acorns.audio.timedomain.*;

import java.awt.geom.*;

import java.util.*;

/** Class containing methods for creating a spectrogram of an audio signal
 *          Based on SoundDefault parameters, the spectrogram can be a
 *          wide band or narrow band; grey scale or color scale
 */
public class Spectrograph
{   private double[] complex;
    private int   FFTSize, step;
    private double[][] spectrogram;
    private double frameRate;
    private Filter filter;
    private double[] bandPass;

    /** Instantiate the spectrograph object
     *
     * @param sound  The audio object
     * @param point the range of the selected audio region
     * @param win true if signal is windowed, false if not
     */
    public Spectrograph(SoundData sound, Point point, boolean win)
    {
        // Get FFT parameters
        int rows;
        frameRate = sound.getFrameRate();
        int frames = sound.getFrames();
        int[] params 
              = FastFourierTransform.getFFTParams(win,frameRate, frames, point);
        FFTSize = params[FastFourierTransform.FFT_SIZE];
        step = params[FastFourierTransform.FFT_STEP];
        rows = params[FastFourierTransform.FFT_WINDOWS];

        complex = new double[2 * FFTSize]; // If too small, TimeDomain corrects
        TimeDomain timeDomain = new TimeDomain(sound);
        complex = timeDomain.getComplexTimeDomain(point, complex);

        // # of columns is half the FFT size plus location for max amplitude
        int columns = FFTSize / 2 + 1;  

        // Create the array to hold the spectrogram
        spectrogram = new double[rows][columns];

        // Create and apply the window sinc filter.
        double  minFreq = SoundDefaults.getMinFreq() / frameRate;
        double  maxFreq = SoundDefaults.getMaxFreq() / frameRate;
        if (maxFreq > 0.5) maxFreq = 0.5;
        if (minFreq > maxFreq) minFreq = maxFreq;

        filter = new Filter();
        bandPass = filter.makeWindowedSincFilter
                      (minFreq, maxFreq, SoundDefaults.getFilterSize());
    }

    /** Compute the spectrogram data
     *
     * @return true if successful, otherwise false
     */
    public boolean computeSpectrogram()
    {
        FastFourierTransform fourier = new FastFourierTransform(FFTSize);
        double[] window = new double[FFTSize*2];

        int row = 0, startWindow;
        for (int offset=0; offset<complex.length; offset += 2*step)
        {  if (row == spectrogram.length) break;
           startWindow = offset;
           if (startWindow + FFTSize*2 - 1 >= complex.length)
           {
               startWindow = complex.length - FFTSize*2;
           }

           System.arraycopy(complex, startWindow, window, 0, 2 * FFTSize);
           window = filter.applyFilterComplex(bandPass, window);

           double[] fft = fourier.fft(window);
           if (fft==null) return false;

           // Display in amplitudes in decibels, not linears
           FastFourierTransform.amplitudes(fft, spectrogram[row++], true,-1); 
        }
        return true;
    }

    /** Compute  the spectral envelope of the selected region
     *
     * @return true if successful
     */
    public boolean computeSpectralEnvelope()
    {
        complex = filter.applyFilterComplex(bandPass, complex);

        // Perform the fft and the polar notation spectral magnitudes.
        FastFourierTransform fourier = new FastFourierTransform(FFTSize);
        double[] fft = fourier.fft(complex);
        if (fft==null) return false;

        FastFourierTransform.amplitudes(fft, spectrogram[0], false, -1);
        return true;
    }


    /** Method to draw the spectrogram
     *
     * @param graphics The object receiving the drawing
     * @param width The total width of the panel
     * @param visible The part of the panel that is visible
     * @return true if successful, false otherwise
     */
     public boolean drawSpectrograph
                              (Graphics graphics, int width, Rectangle visible)
     {
        // Find the maximum frequency bin to display
        double maxF = (SoundDefaults.getMaxFreq())/(frameRate * 0.5);
        if (maxF>0.5) maxF = 0.5;
        double maxIndex = spectrogram[0].length * maxF;

        // Find the maximum frequency value
        double max = 0;
        for (int i=0; i<spectrogram.length; i++)
            if (spectrogram[i][0]> max)  max = spectrogram[i][0];

        boolean palette = SoundDefaults.isColorPalette();
        ArrayList<Color> colors = makeColorPalette(palette);
        
        Graphics2D g2d = (Graphics2D)graphics;

        double rowWidth;
        Rectangle2D.Double rect = new Rectangle2D.Double();
        rect.width = rowWidth = (double)width / spectrogram.length;
        rect.height = (double)visible.height / maxIndex;
        
        int gap = 1;
        if (rect.width<1)
        {
        	gap = (int)(1/rect.width);
        	rect.width *= gap;
        }

        int startX = (int)((double)visible.x / rowWidth);
        int endX = (int)( (double)(visible.x + visible.width) / rowWidth);
        if (endX >= spectrogram.length) endX = spectrogram.length - 1;

        double colorIndex;
        for (int row=startX; row<endX; row+=gap)
        {
			rect.x = row * rowWidth;
            for (int col = 1; col<=maxIndex - 1; col++)
            {
                colorIndex = colors.size() * spectrogram[row][col] / max;
                if (colorIndex>=colors.size()) colorIndex = colors.size()-1;
                g2d.setColor(colors.get( (int)colorIndex));
                
                rect.y = (maxIndex - col)*rect.height;
                g2d.fill(rect);
            }
        }
        return true;
    }

    /** Draw frequency spectral envelope
     *
     * @param graphics Object for drawing spectral envelope
     * @param width The width of the panel
     * @param visible The visible part of the panel
     * @return true if successful
     */
    public synchronized boolean drawSpectralEnvelope
                          (Graphics graphics, int width, Rectangle visible)
    {
        Graphics2D g2D = (Graphics2D)graphics;

        // Draw the frequency domain.
        int bottom = visible.height - 30;
        double xPoint, yPoint;
        double scale  = bottom / spectrogram[0][0];
        double frequenciesPerPoint = (double)width / spectrogram[0].length;
        Line2D.Double line2D;
        graphics.setColor(SoundDefaults.WAVE);
        for (int t=1; t<spectrogram[0].length; t++)
        {
            if (spectrogram[0][t]<0.00001) continue;

            // Subtract from the bottom to conform to java graphics.
            xPoint = t * frequenciesPerPoint;
            yPoint = bottom - spectrogram[0][t]*scale;

            line2D = new Line2D.Double(xPoint, bottom, xPoint, yPoint);
            g2D.draw(line2D);
        }

        // Draw the boundary
        graphics.setColor(SoundDefaults.LINE);
        graphics.drawRect(0,visible.y,width, bottom);

        String line;
        for (int k=0; k< frameRate*0.5; k+=200)
        {
            xPoint = k / (frameRate*0.5) * width;
            line2D = new Line2D.Double(xPoint, visible.y, xPoint, bottom);
            g2D.draw(line2D);

            if (k%1000==0 && k>0)
            {
              line = (int)(Math.round(k/1000))+ "k";
              g2D.drawString(line, (float)xPoint-8,visible.y+visible.height-15);
            }
        }
        return true;

    }   // End of drawSpectralEnvelope

    /** Create a color palette for the spectrogram
     *      The colors vary from light to dark.
     *
     * @param type true to display colors, false to display grey scale
     * @return ArrayList of colors
     */
    private ArrayList<Color> makeColorPalette(boolean type)
    {
        ArrayList<Color> colors = new ArrayList<Color>();

        int red=255, green=256, blue=255, index = 0;

        if (type)
        {
            int[] values = {128, 0};


            while (index < values.length)
            {
               if (green>values[index])     green--;
               else if (red>values[index])  red--;
               else if (blue>values[index]) blue--;
               else index++;

               colors.add(new Color(red, green, blue));
            }
        }
        else  // Create grey scale
        {
            for (int shade = 255; shade>=0; shade--)
            {   colors.add(new Color(shade, shade, shade)); }
        }
        return colors;
    }

}   // End of Spectrograph class
