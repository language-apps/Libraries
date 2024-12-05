package org.acorns.phonemes;

import org.acorns.data.AudioFeatures;

@SuppressWarnings("unused")
public class PhonemeType
{
	private final static int MIN_MAX = 0;
	private final static int MIN_TYPE = 1;
	private final static int MAX_TYPE = 2;
	private final static int NEGATIVE = -1;
	private final static int POSITIVE = +1;
	private final static int RANGE    =  0;
	
	private final static int[] SILENCE_FEATURES =
	{
		AudioFeatures.ENERGY,
		AudioFeatures.PNCC0,
		AudioFeatures.PNCC4,
		AudioFeatures.PNCC1,
		AudioFeatures.YIN_PITCH,
	};
	
	// From 1 std units from mean to 2 std units (L0, H, E)
	// From std to 2 std units from mean
	private final static double[][] SILENCE_RANGE = 
	{
		{NEGATIVE, -1.20, -0.32},	// Energy
		{NEGATIVE, -1.07,  0.52},	// PNCC0 
		{POSITIVE, -1.13,  0.59},	// PNCC4
		{POSITIVE, -1.09,  0.46},	// PNCC1
		{NEGATIVE, -0.50, -0.50},	// YIN	
	};

	/** Construct an array of only the designated audio features */
	public static double[][] getValidFeatures
	   (int[] validFeatures, double[][] audioFeatures)
	{
		int len = validFeatures.length;
		
		double[][] stats = new double[audioFeatures.length][len];
		for (int frame=0; frame<audioFeatures.length; frame++)
		{	for (int feature=0; feature<len; feature++)
			{
				stats[frame][feature] = audioFeatures[frame][validFeatures[feature]+1];
			}
		}
		return stats;
	}
	
	
     /** Determine if this is a frame of a particular type
     * 		(frame has only the relevant audio features)
     *
     * @param features list of relevant features
     * @param range {type, min, max} array for comparing
     * @param frame Audio features for the frame of interest
     * @return true if frame matches the phoneme type
     */
	public static boolean isPhonemeType(int[] features, double[][] range, double[] frame)
	{
    	double min, max, minMax;
    	int feature;
    	for (int i=0; i<features.length; i++)
    	{
    		feature = features[i] + 1;
    		minMax = range[i][MIN_MAX];
    		min = range[i][MIN_TYPE];
    		max = range[i][MAX_TYPE];
    		if (minMax<0)
    		{
    		    if (frame[feature] <= min) return true;
    		    if (frame[feature] >= max) return false;
    		}
    		else if (minMax>0)
    		{
    			if (frame[feature] >= max) return true;
    			if (frame[feature] <= min) return false;
    		}
    		else
    		{
    			if (frame[feature] > max) return false;
    			if (frame[feature] < min) return false;
    		}
    	}
    	return true;    
	}
    
    /** Determine if this is a silent frame 
     * 		(frame has all of the audio features)
     * 
     * @param frame The audio features for a frame
     * @return true if the frame indicates silence
     */
    public static boolean isSilentFrame(double[] frame)
    {
    	return isPhonemeType(SILENCE_FEATURES, SILENCE_RANGE, frame);
    }
    
}	// End of PhonemeType class
