/*
*
*  Courtesy of Joren Six at the Royal Academy of Fine Arts & Royal Conservatory,
*  University College Ghent,Hoogpoort 64, 9000 Ghent - Belgium
*  
*  http://tarsos.0110.be/tag/TarsosDSP
*
*  Modified by Dan Harvey to integrate with ACORNS software
*/
package org.acorns.audio.timedomain;

import java.awt.*;

import org.acorns.audio.*;
import org.acorns.data.*;

/**
 * An implementation of the AUBIO_YIN pitch tracking algorithm. See <a href=
 * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
 * >the YIN paper.</a> Implementation based on <a
 * href="http://aubio.org">aubio</a>
 * 
 * @author Joren Six
 * @author Paul Brossier
 */
public final class Yin extends Pitch
{
	private static final double DEFAULT_THRESHOLD = 0.60; // Should be ~ 0.1 -> 0.15

	private final double threshold;     // Determine possible pitch candidates
	private final double[] yinBuffer;   // Store calculated values (buffer length/2)

	private double[] samples; 			// Time domain samples

    public Yin(SoundData sound, Point bounds, final int bufferSize) 
	{
		this(sound.getFrameRate(), bufferSize, DEFAULT_THRESHOLD);
        TimeDomain timeDomain = new TimeDomain(sound);
        samples = timeDomain.getTimeDomainFromAudio(bounds.x, bounds.y);
	}

    /**
	 * Create a new pitch detector for a stream with the defined sample rate
	 * Processes the audio in blocks of the defined size
	 * 
	 * @param sampleRate The sample rate of the audio stream. E.g. 44.1 kHz
	 * @param bufferSize The size of a buffer. E.g. 1024
	 */
	public Yin(final float sampleRate, final int bufferSize) 
	{
		this(sampleRate, bufferSize, DEFAULT_THRESHOLD);
	}

	/**
	 * Create a new pitch detector for a stream with the defined sample rate.
	 * Processes the audio in blocks of the defined size.
	 * 
	 * @param sampleRate  The sample rate of the audio stream. E.g. 44.1 kHz.
	 * @param bufferSize  The size of a buffer. E.g. 1024.
	 * @param threshold defines which peaks are kept as possible  pitch candidates. 
	 *
	 *  See the YIN paper for more details.
	 */
	public Yin(final float sampleRate, final int bufferSize, final double threshold) 
	{
		super(sampleRate);
		this.threshold = threshold;
		yinBuffer = new double[bufferSize / 2];
	}

	/** Get pitch found in the input buffer
	 * @param audioBuffer The input buffer
	 * @return a pitch frequency in Hz or -1 if no pitch is detected.
	 */
	public double getPitch(final double[] audioBuffer) 
	{
		final int tauEstimate;
		final double pitchInHertz;

		difference(audioBuffer); // step 2
		cumulativeMeanNormalizedDifference(); // step 3
		tauEstimate = absoluteThreshold(); // step 4

		if (tauEstimate != -1) // step 5
		{
			final double betterTau = parabolicInterpolation(tauEstimate);

			// TODO step 6 - YIN paper claim: reduce error rate from 0.77% to 0.5%
			// bestLocalEstimate()

			pitchInHertz = frameRate / betterTau; // Convert to Hz
		} else {	pitchInHertz = -1; } // No pitch found
		return pitchInHertz;
	}
	
    /** Calculate the pitch period for the frame using the YIN algorithm
     *  @param start Initial offset
     *  @param end   Final offset
     *  @return frequency in Hz or -1 if no pitch detected
     */
    public double getPitch(int wStart, int wEnd)
    {
        int windowSize = wEnd - wStart;
        double[] window = new double[windowSize];
        System.arraycopy(samples, wStart, window, 0, windowSize);
        
        double[] windowFilter = filter.makeWindowFilter(windowSize);
        filter.applyWindow(windowFilter, window);
        
        return getPitch(window);
    }


	/**
	 * Implements the difference function as described in step 2 of the YIN paper
	 */
	private void difference(final double[] audioBuffer) 
	{
		int index, tau, length = Math.min(audioBuffer.length/2, yinBuffer.length);
		double delta;
		
		for (tau = 1; tau < length; tau++) 
		{
			yinBuffer[tau] = 0;
			for (index = 0; index < length; index++) 
			{
				delta = audioBuffer[index] - audioBuffer[index + tau];
				yinBuffer[tau] += delta * delta;
			}
		}
	}

	/**
	 * The cumulative mean normalized difference function as described in step 3
	 * of the YIN paper. <br>
	 * <code>
	 * yinBuffer[0] == yinBuffer[1] = 1
	 * </code>
	 */
	private void cumulativeMeanNormalizedDifference() 
	{
		int tau;
		yinBuffer[0] = 1;
		float runningSum = 0;
		for (tau = 1; tau < yinBuffer.length; tau++) 
		{
			runningSum += yinBuffer[tau];
			yinBuffer[tau] *= tau / runningSum;
		}
	}

	/**
	 * Implements step 4 of the AUBIO_YIN paper.
	 * Minimizes octave errors.
	 */
	private int absoluteThreshold() 
	{
		// Uses another loop construct
		// than the AUBIO implementation
		int tau;
		// first two positions in yinBuffer are always 1
		// So start at the third (index 2)
		int endTau = (int)(Math.min(yinBuffer.length, frameRate / MIN_F0));
		int startTau = (int)(frameRate / MAX_FEMALE);
		for (tau = startTau; tau < endTau; tau++) 
		{
			if (yinBuffer[tau] < threshold) 
			{
				while (tau + 1 < yinBuffer.length && yinBuffer[tau + 1] < yinBuffer[tau]) 
				{
					tau++;
				}
				// found tau, exit loop and return
				// From the YIN paper: The threshold determines the list of
				// candidates admitted to the set, and can be interpreted as the
				// proportion of aperiodic power tolerated
				// within a periodic signal.
				//
				// Since we want the periodicity and and not aperiodicity:
				// periodicity = 1 - aperiodicity
				break;
			}
		}

		// if no pitch found, use global minimum if possible
		if (tau == endTau || yinBuffer[tau] >= threshold) 
		{ 
			double minTauValue = yinBuffer[startTau];
			int minTau = startTau;
			for (tau = startTau+1; tau<endTau; tau++)
			{
				if (yinBuffer[tau] < minTauValue && yinBuffer[tau]<1.0) 
				{
					minTauValue = yinBuffer[tau];
					minTau = tau;
				}
			}
			if (minTauValue<1.0) tau = minTau;
			else tau = -1;
		}
		return tau;
	}

	/**
	 * Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau
	 * value using parabolic interpolation. This is needed to detect higher
	 * frequencies more precisely. See http://fizyka.umk.pl/nrbook/c10-2.pdf and
	 * for more background
	 * http://fedc.wiwi.hu-berlin.de/xplore/tutorials/xegbohtmlnode62.html
	 * 
	 * @param tauEstimate
	 *            The estimated tau value.
	 * @return A better, more precise tau value.
	 */
	private double parabolicInterpolation(final int tauEstimate)
	{
		final double betterTau;
		final int x0;
		final int x2;

		if (tauEstimate < 1) {	x0 = tauEstimate;	} 
		else { x0 = tauEstimate - 1;	}
		
		if (tauEstimate + 1 < yinBuffer.length) {	x2 = tauEstimate + 1; } 
		else { x2 = tauEstimate; }
		
		if (x0 == tauEstimate) 
		{	
			if (yinBuffer[tauEstimate] <= yinBuffer[x2]) {	betterTau = tauEstimate; } 
			else { betterTau = x2; }
			
		} 
		else if (x2 == tauEstimate) 
		{
		    if (yinBuffer[tauEstimate] <= yinBuffer[x0]) {	betterTau = tauEstimate; }
			 else {	betterTau = x0; }
		} 
		else 
		{
			double s0, s1, s2;
			s0 = yinBuffer[x0];
			s1 = yinBuffer[tauEstimate];
			s2 = yinBuffer[x2];
			// fixed AUBIO implementation, thanks to Karl Helgason:
			// (2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
			betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
		}
		return betterTau;
	}

} // End of Yin class

