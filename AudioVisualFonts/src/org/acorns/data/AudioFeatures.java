package org.acorns.data;

/**
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2014, all rights reserved
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


import java.awt.Point;

import org.acorns.audio.NormalizeFrames;
import org.acorns.audio.Pitch;
import org.acorns.audio.SoundDefaults;
import org.acorns.audio.TimeDomain;
import org.acorns.audio.timedomain.Butterworth;
import org.acorns.audio.timedomain.GammaFilter;
import org.acorns.audio.timedomain.ResampleAudio;
import org.acorns.audio.timedomain.Yin;
import org.acorns.frontend.PNCC;

public class AudioFeatures 
{
	// The number of LPC coefficients - Default = 8
	private static final int P = SoundDefaults.getLPCCoefficients();
	
	// The number of CEPSTRAL coefficients to model the vocal track (normally: f + sample rate/1000)
    private static final int C = SoundDefaults.getCepstrumLength();
    
    // The number of Filters
    private static final int FILTERS = SoundDefaults.getNumberOfMelFilters();
	
	/** Low frequency for pitch estimation */
	private static final int LOW_FREQ = 100;
	
	/** High frequency for computing pitch */
	private static final int HIGH_FREQ = 8000;
	
	/** Number of noise frames at front for computing pitch */
	private final static int NOISE = 10; 
	
	// Number of iterations for convergence of variance and skew statistics of features
	private static final int NORMALIZE_LOOP = 3; 
	
	// Normalized feature frame rate - Default = 16000 frames per second
	private static final float FRAME_RATE = SoundDefaults.getFrameRate();   

	// Window (frame) size - Default = 25.625 ms
	private static final int WSIZE = (int)(FRAME_RATE * SoundDefaults.getWindowSize()/1000); 

	// Window (frame) step (overlap) - Default = 10 ms
	private static final int WSTEP = (int)(FRAME_RATE * SoundDefaults.getWindowShift()/1000); 

	// Define the parameter to control degree of dynamic feature linear regression curve fitting
	private final static int D = 1;   // Curve fitting loop goes from -D to +D

	/** Starting offset to array of Perceptual Normalized Cepstral Coefficients (PNCC) */
	public static final int PNCC_COEFFICIENTS = 0;
	public static final int PNCC0  = PNCC_COEFFICIENTS;
	public static final int PNCC1  = PNCC_COEFFICIENTS  + 1;
	public static final int PNCC2  = PNCC_COEFFICIENTS  + 2;
	public static final int PNCC3  = PNCC_COEFFICIENTS  + 3;
	public static final int PNCC4  = PNCC_COEFFICIENTS  + 4;
	public static final int PNCC5  = PNCC_COEFFICIENTS  + 5;
	public static final int PNCC6  = PNCC_COEFFICIENTS  + 6;
	public static final int PNCC7  = PNCC_COEFFICIENTS  + 7;
	public static final int PNCC8  = PNCC_COEFFICIENTS  + 8;
	public static final int PNCC9  = PNCC_COEFFICIENTS  + 9;
	public static final int PNCC10 = PNCC_COEFFICIENTS  + 10;
	public static final int PNCC11 = PNCC_COEFFICIENTS  + 11;
	public static final int PNCC12 = PNCC_COEFFICIENTS  + 12;

	public static final int ZERO_CROSS = PNCC_COEFFICIENTS + C;
	public static final int ENERGY = ZERO_CROSS + 1;
	public static final int YIN_PITCH = ENERGY + 1;

	/** Number of audio features */
	public static final int FEATURES_LENGTH = YIN_PITCH + 1;
	
	/** Number of audio features including delta and delta delta values */
	public static final int FEATURE_ARRAY_LENGTH = FEATURES_LENGTH * 3;
	

	/** statistics array row for holding mean */
	private final int MEAN = 0;
	/** statistics array row for holding standard deviation */
	private final int STD = 1;
	/** statistics array row for holding variance */
	private final int VARIANCE = 2;
	/** statistics array row for holding skew */
	private final int SKEW = 3;
	/** statistics array row for holding kirtosis */
	private final int KIRTOSIS = 4;

	// Instance Variables
	private double[][] features;     // Computed features for each audio frame
	private double f0; 				// Running F0 average over the signal

	/** Constructor to create a feature list of part of an audio signal
	 * 
	 * @param bounds Starting and ending offsets
	 * @param audio Audio sound file
	 */
	public AudioFeatures(Point bounds, SoundData audio, boolean norm, boolean cvn, boolean cep)
	{
    	updateFeatures(bounds, audio, norm, cvn, cep);
	}
	
	/** Constructor to create a feature list of the audio signal 
	 * 
	 * @param audio Object containing the audio signal
	 * @param norm true to perform mean normalization
	 * @param cvn true to perform CVN and skew normalization, in addition to CMN
	 * @param cep true if to convert LPC parameters to CEPSTRALS
	 */
    public AudioFeatures(SoundData audio, boolean norm, boolean cvn, boolean cep)
    {
    	updateFeatures(null, audio, norm, cvn, cep);
    }
    
    /** Get frames of features extracted from raw data */
    public double[][] getFrames()
    {
    	return features;
    }
    
    /** extract features from signal
     * 
     * @param bounds offsets to audio signal (-1,-1) or null implies all
     * @param audio SoundData audio object
     * @param norm true to perform mean normalization
     * @param cvn true to perform CVN and skew normalization, in addition to cmn
	 * @param cep true if to convert LPC parameters to CEPSTRALS
     */
    private void updateFeatures(Point bounds, SoundData audio, boolean norm, boolean cvn, boolean cep)
    {
    	if (bounds==null) bounds = new Point(-1,-1);
    	
    	// Cannot extract features from an audio with nothing recorded
    	if (audio.isRecorded())
    	{  
			// Normalize the sample rate to accommodate recordings at differing rates
    		double oldRate = audio.getFrameRate();
    		if (FRAME_RATE != oldRate)
    		{
    			audio = audio.clone();
    			double ratio = FRAME_RATE / oldRate;
    			bounds = new Point(bounds.x, bounds.y);
    			if (bounds.x!=-1) bounds.x *= ratio;
    			if (bounds.y!=-1) bounds.y *= ratio;
    			ResampleAudio.apply(FRAME_RATE, audio);
			}
	   	
	   		// Get time domain data from the audio object
	        TimeDomain timeDomain = new TimeDomain(audio);
	        double[] samples = timeDomain.getTimeDomainFromAudio(bounds.x, bounds.y); 
	        double[] filteredSamples; // Samples after applying butterworth filter

	        if (timeDomain!=null)
	        {
	            TimeDomain.removeDC(samples);
	        }
	        
	        // Apply a buttwerworth band pass filter
	        Butterworth btw =  new Butterworth
	        		(1.0*Pitch.MIN_PITCH_FREQ/FRAME_RATE, 1.0*Pitch.MAX_PITCH_FREQ/FRAME_RATE, true);

	        filteredSamples = btw.applyFilter(samples, true);

	        extractFeatures(filteredSamples, samples, norm, cvn, cep);
    	}
    }		// End of updateFeatures()
    
	/** Method to extract features from a time domain array in complex format
	 * 
	 * @param filteredSamples array of samples filtered by ButterworthFilter
	 * @param samples array of samples
	 * @param norm true if to perform mean normalization
	 * @param cvn true if to perform variance and skew normalization, in addition to mean normalization
	 * @param cep true if to convert LPC parameters to CEPSTRALS
	 * @return feature list
	 * 
	 */
	private double[][] extractFeatures(double[] filteredSamples, double[] samples, boolean norm, boolean cvn, boolean cep)
	{
        // Compute windowing parameters
        // Window size, step size between windows, FFT size, number of windows 
        float frameRate = FRAME_RATE;
 
        // Create windowing function
        double[] pitchWindow = new double[WSIZE];   // Band pass filtered for pitch

        int startFrame, endFrame;

        // Create objects to perform digital signal algorithms 
        Pitch yin = new Yin(frameRate, WSIZE);

        // Instantiate the object for computing the PNCC features for each frame
        PNCC pncc = new PNCC((int)frameRate, new double[FILTERS], C);
        
        int frames = samples.length / WSTEP;
        if (frames*WSTEP + WSIZE > samples.length) frames--;
        if (frames<0) frames = 0;
        
        features = new double[frames][FEATURE_ARRAY_LENGTH];
        if (frames==0) return features;

        for (int frame = 0; frame< frames; frame++)
        {
           	startFrame = frame*WSTEP;
        	endFrame = startFrame + WSIZE;
        	if (endFrame>samples.length) 
        	{
        		endFrame = samples.length; 
        	}
        	
      
        	features[frame][YIN_PITCH] = 0.0;
        	if (endFrame < filteredSamples.length)
        	{
                System.arraycopy(filteredSamples, startFrame, pitchWindow, 0, endFrame - startFrame);
               	features[frame][YIN_PITCH] = yin.getPitch(pitchWindow);
        	}
    
            // Compute the number of zero crossings per window step size
            features[frame][ZERO_CROSS]   	     
       	    		= TimeDomain.zeroCrossings(samples, startFrame, endFrame, 1);
            
            features[frame][ENERGY] 
            		= 10*Math.log10(TimeDomain.energy(samples, startFrame, endFrame, 1));

         	// Compute the PNCC coefficients

            double lowFreq = getF0(frameRate, frame);
            double[] cf = GammaFilter.getCenterFrequencies(lowFreq, HIGH_FREQ, FILTERS);
            pncc.setCF(cf);
			pncc.getPNCC(samples, startFrame, endFrame, frame, features[frame], PNCC_COEFFICIENTS);
        }
        
        // Smooth the pitch contour
        medianOfFive(YIN_PITCH);
        linearSmoother(YIN_PITCH);

        // Calculate delta values
        calculateDynamicFeatures(PNCC_COEFFICIENTS);
        
        // Calculate delta delta values
        calculateDynamicFeatures(PNCC_COEFFICIENTS + FEATURES_LENGTH);
        
         // Normalize cepstrum mean (0), variance (1), skew (0)
    	int start = PNCC_COEFFICIENTS;
    	int end = FEATURES_LENGTH; 
    	if (norm) normalizeFeatures(start, end, cvn);
        
    	// Normalize delta mean (0), variance (1), skew (0)
     	start += FEATURES_LENGTH;
    	end += FEATURES_LENGTH; 
    	if (norm) normalizeFeatures(start, end, cvn);

    	// Normalize delta-delta mean (0), variance (1), skew (0)
      	start += FEATURES_LENGTH;
      	end += FEATURES_LENGTH;
    	if (norm) normalizeFeatures(start, end, cvn);
       
 	    return features;
	}

	/** Get the fundamental glottis frequency of an audio recording
	 * 
	 * @param samples Audio time domain samples
	 * @param frameRate Samples per second
	 * @return f0 fundamental frequency
	 */
	private double getF0(double frameRate, int frame)
	{
		// In noise section, simply return low estimate of the pitch
		if (frame<NOISE) return f0 = LOW_FREQ;
	
		// If pitch is undefined, return the previous calculation
		double pitch = features[frame][YIN_PITCH];
		if (pitch<0) return f0;
		
		// Update the running average
		f0 = ((frame-1) * f0 + pitch)/frame;
		return f0;
	}

	/** Method to scale the features and normalize the skew
	 *    Because variance and skew cannot be perfectly normalized
	 *      the method iterates a few times to converge on a better result
	 * 
	 * @param start starting feature to normalize
	 * @param end  ending feature to normalize
	 * @param cvn true to perform variance and skew normalization, in addition to mean normalization
	 */
	private void normalizeFeatures(int start, int end, boolean cvn)
	{
		// Iterate to converge mean to 0, variance to 1, and skew to 0
		for (int n=0; n<NORMALIZE_LOOP; n++)
		{
			// Normalize mean of featues to 0
	       features = NormalizeFrames.meanNormalization(features, start, end);
	       if (!cvn) return;
	        
	        // Normalize variances of features unity for better low SNR recognition
	        features = NormalizeFrames.varianceNormalization(features, start, end);
	        // Normalize skew of features to 0 for better Normal Distribution fit
	      	features = NormalizeFrames.skewNormalization(features, start, end);
		}	
	}
	
	/** Median of five non linear filter to eliminate discontinuities across frames
	 * 
	 * @param feature The feature to smooth
	 */
	private void medianOfFive(int feature)
	{
		if (features==null) return;
		
		double median, save, middle, out[] = new double[features.length];
		
		for (int frame=2; frame<features.length - 2; frame++)
		{
			// median is the lower of top two elements; save is the greater
			median = features[frame+2][feature];
			save = features[frame+1][feature];
			if (median > save)
			{
				median = features[frame+1][feature];
				save = features[frame+2][feature];
			}
			
			// Eliminate the high and low of the bottom two and top to
			if (features[frame-2][feature]<features[frame-1][feature])
			{
				if (features[frame-2][feature]>median) median = features[frame-2][feature];
				if (features[frame-1][feature]<save) save = features[frame-1][feature];
			}
			else
			{
				if (features[frame-1][feature]>median) median = features[frame-1][feature];
				if (features[frame-2][feature]<save) save = features[frame-2][feature];
			}
			
			// Pick the correct median to be the middle of the remaining elements
			middle = features[frame][feature];
			if ((save - middle) * (save-median) <= 0) median = save;
			if ((middle - save) * (middle - median) <= 0) median = middle;
			out[frame] = median;
		}
		
		// Move the medians to the appropriate feature locations
		for (int frame=2; frame<features.length - 2; frame++)
		{
			features[frame][feature] = out[frame];
		}
		
	}
	
	/** A linear smoother to further smooth a feature curve after median of five
	 * 
	 * @param feature The feature in question
	 */
	private void linearSmoother(int feature)
	{
		if (features==null) return;

		double[] out = new double[features.length];
		for (int frame = 2; frame<features.length; frame++)
		{
			out[frame] = features[frame][feature]/4 
			  			+ features[frame-1][feature]/2 + features[frame-2][feature]/4;
		}
		
		// Move the medians to the appropriate feature locations
		for (int frame=2; frame<features.length; frame++)
		{
			features[frame][feature] = out[frame];
		}
	}

	
	/** Calculate the delta value for a set of feature
	 * 
	 * @param frame The frame number containing the feature
	 * @param featureOffset The feature offset into the array of features
	 */
	private void calculateDynamicFeatures(int featureOffset)
	{
		int start, end;
		double numerator, denominator;
		
        // Update the delta and delta delta totals
        for (int frame=0; frame<features.length; frame++)
        {
        	for (int feature=0; feature<FEATURES_LENGTH; feature++)
        	{
        		numerator = denominator = 0;
            	start = (frame<D)? -frame: - D;
            	end = (frame>=features.length - D) ? features.length - frame - 1: +D;
        		for (int d= start; d<= end; d++)
        		{
            		numerator +=  d * features[frame + d][feature + featureOffset];
            		denominator += d * d;
        			
        		}
        		if (denominator !=0)
        			features[frame][feature + featureOffset + FEATURES_LENGTH] 
        		          = numerator / denominator;
        	}
        }
	}		// End of calculateDynamicFeatures()


	/** Compute averages for all features and all frames */
	public double[][] computeAverages()
	{
	   	int[] stats = new int[FEATURE_ARRAY_LENGTH];
		for (int feature=0; feature<FEATURE_ARRAY_LENGTH; feature++)
		{
			stats[feature] = feature;
		}
		return computeAverages(stats);		
	}
	
	/** Compute averages for all frames, but selected features
	 * 
	 * @param offsets Offsets to desired features
	 * @return Computed selected averages
	 */
	public double[][] computeAverages(int[] offsets)
	{
		int[] frames = new int[features.length];
		for (int f=0; f<features.length; f++)  frames[f] = f;
		return computeAverages(frames, offsets);
	}
    
	/** Compute statistics on the features object
	 * 
	 * @param frames the frames of interest
	 * @param offsets Array of which features are of interest
	 * @return Computed averages
	 */
	public double[][] computeAverages(int[] frames, int[] offsets)
	{
		double[][] totals = new double[5][offsets.length];
		
		int N = frames.length;
		if (N==0) return totals;
		
		// Total each feature across the frames
		int frame;
		for (int index=0; index < N; index++)
		{
			frame = frames[index];
			if (frame<0 || frame>=features.length) return totals; 
			
			for (int feature=0; feature<offsets.length; feature++)
			{
				totals[MEAN][feature] += features[frame][offsets[feature]];
			}
		}
		
		// Compute the means
		for (int feature = 0; feature<offsets.length; feature++)
		{
			totals[MEAN][feature]/= N;
		}
		if (N<2) return totals;
		
		// Compute totals for variance, stdev, skew, and kirtosis
		double delta;
		for (int index=0; index < N; index++)
		{
			frame = frames[index];

			for (int feature=0; feature<offsets.length; feature++)
			{
				delta = (features[frame][offsets[feature]] - totals[MEAN][feature]);
				totals[VARIANCE][feature] += delta * delta;
				if (N>2) totals[SKEW][feature] += Math.pow(delta, 3);
				if (N>3) totals[KIRTOSIS][feature] += Math.pow(delta,  4);
			}
		}
		
		// Complete the calculations (using Excel formulas)
		double stdev, factor;
		for (int feature=0; feature<offsets.length; feature++)
		{
			totals[VARIANCE][feature] = totals[VARIANCE][feature] /= N - 1;
			totals[STD][feature] = stdev = Math.sqrt(totals[VARIANCE][feature]);
			if (N > 2 && stdev != 0)
			{
				factor = 1.0 * N / ((N-1)*(N-2));
				totals[SKEW][feature] = factor * totals[SKEW][feature] / Math.pow(stdev, 3);
			}
			if (N > 3 && stdev !=0)
			{
				factor = 1.0 * N * (N+1) / ((N-1)*(N-2)*(N-3));
				totals[KIRTOSIS][feature] = factor * totals[KIRTOSIS][feature] / Math.pow(stdev, 4);
				factor = 3.0 * (N-1)*(N-1) / ((N-2)*(N-3));
				totals[KIRTOSIS][feature] -= factor;
			}
		}
		return totals;
	}
	
	/** Get the statistical averages for all frames of the audio
	 * 
	 * @param averages The statistical averages in deviation units of the speech features
	 */
	public double[][] getStatisticsSTD(double[][] averages)
	{
		Point bounds = new Point(0, features.length - 1);
		return getStatisticsSTD(bounds, averages);
	}
	
	/** Get the statistical averages for the selected audio frames
	 * 
	 * @param bounds - bounds.x = starting speech frame, bounds.y = The ending speech frame
	 * @param averages The statistical averages in deviation units of the speech features
	 */
	public double[][] getStatisticsSTD(Point bounds, double[][] averages)
	{
		int startSpeech = bounds.x;
		int endSpeech = bounds.y;
		
		double[][] results = new double[endSpeech -startSpeech + 1][averages[0].length + 2];
		
		double value, mean, std, units;
		int    feature;
		for (int frame=startSpeech; frame<=endSpeech; frame++)
		{
			results[frame-startSpeech][0] = frame*WSTEP;
			for (int stat=0; stat<averages[0].length; stat++)
			{
				feature = stat;
				value = features[frame][feature];
	
				mean = averages[MEAN][stat];
				std = averages[STD][stat];
				units = (value - mean) / std;
				results[frame-startSpeech][stat+1] = units;
			}
		}
		return results;
	}
    
	/** Create the title for toString output
	 * 
	 * @param offsets array of offsets of the desired features
	 * @param detail true if this is for detailed data rather then summary totals
	 */
	public static String title(int[] offsets, boolean detail)
	{
     	// Initialize text for the toString output
    	String[] toStringText = new String[FEATURE_ARRAY_LENGTH];

    	for (int i = 0; i< P; i++)
    	{
    		toStringText[i] = "PNCC" + i;
    		toStringText[i+FEATURES_LENGTH] = "DPNCC" + i;
    		toStringText[i+2*FEATURES_LENGTH] = "DDPNCC" + i;
    	}

    	toStringText[ZERO_CROSS] = "LPErr";
    	toStringText[ZERO_CROSS + FEATURES_LENGTH] = "DLPErr";
    	toStringText[ZERO_CROSS + 2 * FEATURES_LENGTH] = "DDLPErr";

    	toStringText[ENERGY] = "Energy";
    	toStringText[ENERGY + FEATURES_LENGTH] = "DEnergy";
    	toStringText[ENERGY + 2 * FEATURES_LENGTH] = "DDEnergy";

    	toStringText[YIN_PITCH] = "Yin";
    	toStringText[YIN_PITCH + FEATURES_LENGTH] = "DYin";
    	toStringText[YIN_PITCH + 2 * FEATURES_LENGTH] = "DDYin";

    	// Create string representation of the audio frame data
		StringBuilder build = new StringBuilder();
		String spaces = "        ", text;
	
        if (detail) 	build.append("#### ");
        else build.append("     ");
        
		for (int feature = 0; feature<offsets.length; feature++)
		{
			text = spaces + toStringText[offsets[feature]];
			build.append(String.format("%s ", text.substring(text.length()-10)));
		}
		build.append("\n");
		return build.toString();		
	}

	@Override
	public String toString()
	{
		int[] offsets = new int[FEATURE_ARRAY_LENGTH];
		for (int feature=0; feature<FEATURE_ARRAY_LENGTH; feature++)
		{
			offsets[feature] = feature;
		}
		
		return toString(new Point(-1,-1), offsets);
	}

	/** Create string representation of audio signal between starting and ending samples
	 * 
	 * @param bounds The starting and ending offsets (-1,-1) means entire signal
	 * @param offsets The features of interest
	 * @return String representation of the frames in question
	 */
    public String toString(Point bounds, int[] offsets)
    {
		if (bounds == null) bounds = new Point(-1,-1);
		
		int start = (bounds.x<0) ? 0 : bounds.x / WSTEP;
		int end = (bounds.y + WSTEP - 1)/WSTEP;
		if (end > features.length)  end = features.length;
		
		if (end<start) return "No frames within the specified bounds";

		int[] frames = new int[end-start];
		for (int frame=start; frame<end; frame++)
		{
			frames[frame-start] = frame;
		}
		return toString(frames, offsets);
    }
    
	/** Create a string representation of the selected features
	 * 
	 * @param offsets An array of feature offsets
	 * @param frames array of frames of interest (if null, all frames)
	 * @return The String result
	 * 
	 */
	public String toString(int[] frames, int[] offsets)
	{  
		StringBuilder build = new StringBuilder();
		build.append(title(offsets, true));
	
		int frame;
		double number;
		for (int index=0; index<frames.length; index++)
		{
			frame = frames[index];
			if (frame<0 || frame>=features.length) continue;
			
			build.append(
			String.format("%4d", frame));
			for (int feature=0; feature<offsets.length; feature++)
			{
				number = features[frame][offsets[feature]];
				build.append(String.format("%11.3f", number)); 
			}
			build.append("\n");
		}
		
		// Create the statistical totals
		build.append("\n");
		build.append(title(offsets, false));
		double[][] totals = computeAverages(frames, offsets);
		for (int line=0; line<totals.length; line++)
		{
			build.append("    ");
			for (int feature=0; feature<offsets.length; feature++)
			{
				number =  totals[line][feature];
				build.append(String.format("%11.3f", number)); 
			}
			build.append("\n");
			
		}
		return build.toString();
	}
    
}	// End of AudioFeatures class
