/*
 * SoundImage.java
 *
 * Created on July 16, 2007, 2:45 PM
 *
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.editor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.acorns.audio.SoundDefaults;
import org.acorns.audio.frequencydomain.Spectrograph;
import org.acorns.data.Annotations;
import org.acorns.data.SoundData;
import org.acorns.frontend.MFCC;

public class SoundImage
{   
    /** Draw a time domain image as wave or spectrograph
     */
    public synchronized void drawSound(Graphics graphics
            , Annotations annotations, int width
            , Rectangle visible, boolean spectrograph)
    {
        graphics.setColor(SoundDefaults.FOREGROUND);

        if (spectrograph)
             drawSpectrograph(graphics, width, visible, annotations);
        else drawTime(graphics, width, visible, annotations);
    }
            
    // Draw sound in the time domain.
    private boolean drawTime
           (Graphics graphics,int width
           , Rectangle visible, Annotations annotations)
    {   
        SoundData soundData = annotations.getSound();
        int   lastX = 0, lastY = visible.height/2, mid = lastY;
        int   xPoint = 0, yPoint = 0;
        
        
        SoundEditor editor = annotations.getSoundEditor();
        if (editor==null) return false;
        double[] timeDomain = editor.getTimeDomain();
        if (timeDomain==null) return false;
        double max = (1 << (soundData.getFrameSizeInBytes() * 8 -1));

        double pixelsPerPoint = (double)width / timeDomain.length;
        
        int start = (int)(visible.x/pixelsPerPoint);
        int end   = (int)((visible.x + visible.width)/ pixelsPerPoint);
        for (int t=start; t<end; t++)
        {
            // Multiply by -1 to conform to java graphics.
            xPoint = (int)(t * pixelsPerPoint);
            yPoint = (int)(timeDomain[t]/max*mid + mid) + visible.y;
            if (xPoint>lastX && yPoint!=lastY)
            {
                graphics.drawLine(lastX, lastY, xPoint, yPoint);
                lastX = xPoint;
                lastY = yPoint;
            }               
        }
         
        graphics.setColor(SoundDefaults.LINE);
        graphics.drawLine(visible.x, visible.height/2+visible.y
                                          , width, visible.height/2);
        return true;
    }   // End of drawTime();
    
    // Draw sound as a spectrograph.
    private boolean drawSpectrograph
      (Graphics graphics, int width, Rectangle visible, Annotations annotations)
    {
        Spectrograph spect 
            = new Spectrograph(annotations.getSound(), null, true);
        if (!spect.computeSpectrogram()) return false;
        return spect.drawSpectrograph(graphics, width, visible);
    }  // End of drawSpectrograph()
 
    // Draw featureVector
    public boolean drawFeatureVector(Graphics graphics
            ,Annotations annotations, int width, Rectangle visible)
    {
        SoundData soundData = annotations.getSound();
        MFCC cepstrals = new MFCC(soundData, null);
        if (!cepstrals.calculateCepstral()) return false;

        return cepstrals.drawFeatureVector(graphics, width, visible);
    }
    
    // Draw frame distances
    public boolean drawFrameDistances(Graphics graphics
            ,Annotations annotations, int width, Rectangle visible)
    {
        SoundData soundData = annotations.getSound();
        MFCC cepstrals = new MFCC(soundData, null);
        if (!cepstrals.calculateCepstral()) return false;

        return cepstrals.drawFrameDistances(graphics, width, visible);
    }
 

    // Draw frequency domain image.
    public synchronized boolean drawFFT
            (Graphics graphics, Annotations annotations, int width
                                          , Rectangle visible, Point selection)
    {
        Spectrograph spect = new Spectrograph
                (annotations.getSound(), selection, false);
        if (!spect.computeSpectralEnvelope()) return false;
        return spect.drawSpectralEnvelope(graphics, width, visible);
    }   // End of drawFFT    
}       // End of SoundImage class

