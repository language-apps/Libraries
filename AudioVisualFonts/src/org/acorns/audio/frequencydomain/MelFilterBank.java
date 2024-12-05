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
 package org.acorns.audio.frequencydomain;

 import org.acorns.audio.*;
/**
 *
 * Create and apply  a bank of MEL filters
 */
public class MelFilterBank 
{
	private double INV_SQRT_2PI = 1 / Math.sqrt(2*Math.PI);
	
    double frameRate;
    private int M;  // Number of Mel Filterrs
    
    double minFreq, maxFreq;
    double minFreqMel, maxFreqMel, deltaFreqMel;
    double minBin, maxBin, deltaFreqBin;
    
    double[][] filterBank;
    
    /** Constructor to create a bank of MEL  filters
     *  @param F Number of FFT bins
     *  @param  frameRate number of samples per second
     */
    
    public MelFilterBank(double frameRate, int F)
    {  
        this.frameRate = frameRate;

        M = SoundDefaults.getNumberOfMelFilters();
        
        // Create and apply the window sinc filter.
        minFreq = SoundDefaults.getMinFreq();
        maxFreq = SoundDefaults.getMaxFreq();
        if (maxFreq / frameRate > 0.5) maxFreq = 0.5 * frameRate;
        if (minFreq > maxFreq) minFreq = maxFreq;

        minFreqMel=FastFourierTransform.warp(minFreq, true);
        maxFreqMel=FastFourierTransform.warp(maxFreq, true);
        deltaFreqMel=(maxFreqMel-minFreqMel)/(M+1);
        
        // deltaFreqBin = (frameRate/2)/(F/2) = frameRate/F
        deltaFreqBin =(double)frameRate/F;
        minBin = Math.floor(minFreq / deltaFreqBin);
        maxBin = Math.ceil(maxFreq / deltaFreqBin);
        
        filterBank = makeMelFilterbank();
        
    }


    /** Create a MEL filter bank
     *  @return A 2 dimensional array of 40 MEL filters
     */
    private double[][] makeMelFilterbank()
    {
        if(deltaFreqBin==0)
            throw new IllegalArgumentException("deltaFreq equals zero");

        double [] leftEdge = new double[M];
        double [] centerFreq = new double[M];
        double [] rightEdge = new double[M];
        double [][] melFilters = new double[M][];

        double nextEdge;

        if(M<=0)
            throw new IllegalArgumentException("Illegal # of MEL Coefficients");

        leftEdge[0]=setToNearestFrequencyBin(minFreq,deltaFreqBin);
        double nextEdgeMel = minFreqMel;

        for(int i=0;i<M;i++)
        {   nextEdgeMel += deltaFreqMel;
            nextEdge=FastFourierTransform.unwarp(nextEdgeMel, true);
            centerFreq[i]=setToNearestFrequencyBin(nextEdge,deltaFreqBin);
            if(i>0)      rightEdge[i-1]=centerFreq[i];
            if(i<M-1)    leftEdge[i+1] =centerFreq[i];
        }

        nextEdgeMel=nextEdgeMel + deltaFreqMel;
        nextEdge=FastFourierTransform.unwarp(nextEdgeMel, true);
        rightEdge[M-1]=setToNearestFrequencyBin(nextEdge,deltaFreqBin);
        

        double initialFreqBin;
        for(int i=0; i<M; i++)
        {
          //see if left and right edge boundaries are too close
          if((Math.round(rightEdge[i]-leftEdge[i])==0)
                              || (Math.round(centerFreq[i]-leftEdge[i])==0)
                              || (Math.round(rightEdge[i]-centerFreq[i])==0))
               throw new IllegalArgumentException("Filter boundaries too close");

          initialFreqBin=setToNearestFrequencyBin(leftEdge[i],deltaFreqBin);
          if(initialFreqBin < leftEdge[i])    initialFreqBin += deltaFreqBin;
          
          if (SoundDefaults.getMelFilterType() == SoundDefaults.GAUSSIAN)
          {
        	  melFilters[i] 
        	     = produceGaussianFilter(leftEdge[i], centerFreq[i]
        	    		 , rightEdge[i], initialFreqBin, deltaFreqBin);
          }
          else
          {
               melFilters[i]
                  = produceMelFilter(leftEdge[i], centerFreq[i]
                		, rightEdge[i], initialFreqBin, deltaFreqBin);
          }
        }
        return melFilters;
    }   // End of makeMelFilterBank

    /**Produce a MEL filter
     *  @return the MEL weight
     */
    private double[] produceGaussianFilter(double leftEdge, double centerFreq
            , double rightEdge, double initialFreq,double deltaFreq)
                                             throws IllegalArgumentException
    {
        double currentFreq;
        int indexFilterWeight;

        //compute the number of element wieghts we need in the weight field
        //by computing how many frequency bins we can fit in the current freq range
        int numberElementsWeightField
                = (int)Math.round((rightEdge-leftEdge)/deltaFreq + 1);
        if(numberElementsWeightField==0)
             throw new IllegalArgumentException("# of elements in mel is zero");

        // Weight is the MEL weight array, which is the last element
        //     we store the initial frequency index
        double [] weight = new double[numberElementsWeightField+1];
        
        // Compute the Gaussian standard deviation between bins
        double dampingFactor = SoundDefaults.getMelGaussianDampingFactor();
        double std;

        for(currentFreq=initialFreq,indexFilterWeight=0;
                currentFreq<=rightEdge;
                currentFreq+=deltaFreq,indexFilterWeight++)
        {
        	// Calculate the central mel frequency of this bin
        	std = (centerFreq - initialFreq)/dampingFactor;
        	weight[indexFilterWeight] = calculateGaussianWeight(currentFreq, centerFreq, std);
        }

        //store the initial frequency index at the last element
        weight[weight.length-1]=(int)Math.round(initialFreq / deltaFreq);
        return weight;
     }

    /** Calculate the weight mapping a frequency bin to a MEL filter
     * 
     * @param currentMelFreq Central frequency of an FFT bin
     * @param freqMELFilter frequency of one of the MEL filters
     * @param sted span of MEL frequencies between bins / Gaussian spread
     * @return The weight (0<=weight<=1) to apply to an FFT bin amplitude
     */
    private double calculateGaussianWeight(double currentMelFreq, double freqMELFilter, double std)
    {
        double deltaF = currentMelFreq - freqMELFilter;
        double weight = Math.exp( -Math.pow(deltaF / std, 2) * 0.5);
        weight /= (std * INV_SQRT_2PI);
        return weight;
    }

   	/**Produce a MEL filter
     *  @return the MEL weight
     */
    private double[] produceMelFilter(double leftEdge, double centerFreq
            , double rightEdge, double initialFreq,double deltaFreq)
                                             throws IllegalArgumentException{

        double currentFreq;
        int indexFilterWeight;

        //compute the number of element weights we need in the weight field
        //by computing how many frequency bins we can fit in the current freq range
        int numberElementsWeightField
                = (int)Math.round((rightEdge-leftEdge)/deltaFreq + 1);
        if(numberElementsWeightField==0)
             throw new IllegalArgumentException("# of elements in mel is zero");

        // Weight is the mel weight array, which is the last element
        //     we store the initial frequency index
        double [] weight = new double[numberElementsWeightField+1];
        //makes the filter area equal to 1
        double filterHeight=2.0f / (rightEdge-leftEdge);
        //compute slopes based on height
        double leftSlope=filterHeight / (centerFreq-leftEdge);
        double rightSlope=filterHeight / (centerFreq-rightEdge);
        //compute the weight for each for each frequency bin

        for(currentFreq=initialFreq,indexFilterWeight=0;
                   currentFreq<=rightEdge;
                   currentFreq+=deltaFreq,indexFilterWeight++)
        {
            //used for both "sides" of the triangular filter
            if(currentFreq < centerFreq)
                  weight[indexFilterWeight]=leftSlope*(currentFreq-leftEdge);
            else  weight[indexFilterWeight]=filterHeight+rightSlope
                        * (currentFreq-centerFreq);
        }

        //store the initial frequency index at the last element
        weight[weight.length-1]=(int)Math.round(initialFreq / deltaFreq);
        return weight;

   } //produceMelFilter

    /**Calculate a MEL spectrum by applying a bank of MEL filters. This makes
     * the frequencies more resemble the bands humans are sensitive to.
     * @param powerSpectrum Power spectrum extracted from the FFT
     * @return The MEL spectrum
    */
    public double[] applyMelFilterBank(double[] powerSpectrum)
    {
        int indexSpectrum = 0;

        double[] melSpectrum = new double[filterBank.length];
        for(int i=0;i<filterBank.length;i++)
        {
        	// Starting fft bin for this filter
        	indexSpectrum = (int)(filterBank[i][filterBank[i].length-1]);
            for(int j=0;j<filterBank[i].length-1;j++)
            {  if(indexSpectrum < powerSpectrum.length)
                  melSpectrum[i]
                          +=powerSpectrum[indexSpectrum++]*filterBank[i][j];
            }
        }
        return melSpectrum;
    }

    /**Sets the given FFT frequency to the nearest frequency bin.
     * The FFT can be thought of as a sampling of the actual sprectrim
     * of a signal. We use this function to find the sampling  point of the
     * spectrum that is closest to the given frequency
     *
     *@param inFreq the input frequency
     *@param stepFreq the distance between frequency bins
     *@return the closest bin
     *@throws IllegalArgumentException
     */
    private double setToNearestFrequencyBin(double inFreq, double stepFreq)
        throws IllegalArgumentException
    {
        if(stepFreq==0)  throw new IllegalArgumentException("stepFreq is zero");
        return stepFreq * Math.round(inFreq / stepFreq);
    }

}       // End of MelFilterBank class
