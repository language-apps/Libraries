package org.acorns.frontend;

import java.util.Arrays;

import org.acorns.audio.frequencydomain.DiscreteCosineTransform;
import org.acorns.audio.timedomain.GammaFilter;

public class PNCC
{
	/** Envelope coefficient (default: 0.999). See equation (4).*/
	private final static double LAMBDA_A = 0.999;
	/** Envelope coefficient (default: 0.5). See equation (4). */
	private final static double LAMBDA_B = 0.5;
	/** Initialization factor of lower envelope (average power or rectified envelope * initialization factor) */
	private final static double LOWER_ENVELOPE_INITIALIZATION_FACTOR = 0.9;
	/** Forgetting factor (default: 0.85). See equations (10) and (11). */
	private final static double LAMBDA_T = 0.85;
	/** Threshold for the excitation/non-excitation decision (default: 2). */
	private final static double EXCITATION_THRESHOLD = 2; // C in the pncc paper.
	/** Channels window on the spectral weight smoothing stage. See equation (12) of Kim & Stern (default: 4).*/
	private final static int CHANNEL_WINDOW  = 4;
	/** Temporal integration factor. See equation (3) of Kim & Stern (default: 2).*/
	private final static int MEDIUM_POWER_WINDOW_LENGTH = 2;
	/** The initial mean power, i.e., mu of equation 14 for the first frame.
	The default value 0.537 is taken from Kim Matlab implementation */
	private final static double INITIAL_MEAN_POWER = 0.537;
	/** Forgetting factor (default: 0.999). See equation (14). */
	private final static double lAMBDA_MU = 0.999;
	/** Coefficient of equation (11) (default: 0.2). */
	private final static double MU_T = 0.2;

	/** Small value to prevent divide by zero */
	public static final double DBL_EPSILON = 2.220446049250313E-16d;
	
	/** The mean power coefficient. See equation (15) of Kim & Stern (default: 1).*/
	private double meanPowerCoefficient = 1;

	/** Non-linearity exponent (default: 1/15). */
	private double exponent = 1.0/15;

	/** Object to compute the Gamma tone filter */
	private GammaFilter gamma;
	/** Array of filter bank center frequencies */
	private double[] cf;
	
	/** FFTW plan for computing the DCT. */
    private DiscreteCosineTransform dct;
	/** Buffer used for internal calculations. */
	private double power[][];
	/** Medium-time power buffer. */
	private double q_tilde[];
	/** Lower-envelope power buffer. */
	private double q_le[][];
	/** Q0 buffer. */
	private double q0[];
	/** Qf buffer. */
	private double q_f[][];
	/** Qp buffer. */
	private double q_p[][];
	/** Qtm buffer. */
	private double q_tm[];
	/** Rsp buffer. */
	private double r_sp[];
	/** R buffer. */
	private double r[];
	/** Internal buffer. */
	private double ratio[];
	/** Internal buffer. */
	private double s[];
	/** Internal buffer. */
	private double t[];
	/** Internal buffer. */
	private double u[];
	/** The feature buffer. */
	private double featureVector[];
	/** Mean power estimate buffer. */
	private double mu_prev, mu_cur;
	
    public PNCC(int frameRate, double[] cf, int nCeps)
    {
     	int nFilters = cf.length;
     	
     	/** Save the center frequencies */
     	this.cf = cf;

     	/** Create gamma tone filter object */
     	gamma = new GammaFilter(frameRate);
     	
       	/** FFTW plan for computing the DCT. */
    	dct = new DiscreteCosineTransform( nFilters, nCeps );
        
    	/** Ring filter spectrum buffer */
    	power = new double[MEDIUM_POWER_WINDOW_LENGTH * 2 + 1][];

     	/** Medium-time power buffer. */
    	q_tilde = new double[nFilters];
    	/** Lower-envelope power buffer. */
    	q_le = new double[2][nFilters];
    	/** Q0 buffer. */
    	q0 = new double[nFilters];
    	/** Qf buffer. */
    	q_f = new double[2][nFilters];
    	/** Qp buffer. */
    	q_p = new double[2][nFilters];
    	/** Qtm buffer. */
    	q_tm = new double[nFilters];
    	/** Rsp buffer. */
    	r_sp = new double[nFilters];;
    	/** R buffer. */
    	r = new double[nFilters];;
    	/** Internal buffer. */
    	ratio = new double[nFilters];;
    	/** Internal buffer. */
    	s = new double[nFilters];;
    	/** Internal buffer. */
    	t = new double[nFilters];;
    	/** Internal buffer. */
    	u = new double[nFilters];;
    	/** The feature buffer. */
    	featureVector = new double[nCeps];
    	
  	  	mu_cur = mu_prev = INITIAL_MEAN_POWER;
	}
    
    public void setCF(double[] cf)
    {
    	this.cf = cf;
    }

	public void getPNCC(final double frame[], int first, int last, int frameNo, final double pncc[], int pnccOffset)
	{
		int nFilters = cf.length;
		int f_idx, cur_frame_idx, prev_frame_idx;
		double t_mean = 0;
		
		/* Gammatone filterbank computation added to a ring buffer of power outputs */
		f_idx =  (int)frameNo % (MEDIUM_POWER_WINDOW_LENGTH * 2 + 1);
		
		power[f_idx] = gamma.applyGammaFilter(frame, first, last, cf, null);

		if (frameNo >= MEDIUM_POWER_WINDOW_LENGTH*2)
		{
		     cur_frame_idx  = frameNo % 2;
		     prev_frame_idx = (cur_frame_idx +1)%2;
			
		    // compute the current frame average power (q_tilde) from the ring buffer of power outputs
		    average(power, q_tilde, nFilters);
		    
		    // Initialize lower envelope value to power of previous frame * 
		    if (frameNo == 2*MEDIUM_POWER_WINDOW_LENGTH)
		    {
		       for (int filter = 0; filter < nFilters; ++filter)
		    	  q_le[prev_frame_idx][filter] = q_tilde[filter]*LOWER_ENVELOPE_INITIALIZATION_FACTOR;
		    }
		    
		    // Find low values for each filter	
		    lower_envelope(q_tilde, q_le, cur_frame_idx, LAMBDA_A, LAMBDA_B, nFilters);
		    
		    // Disallow envelope to go negative (half wave rectify)		
		    for (int filter = 0; filter < nFilters; ++filter)
		    {
		       q0[filter] = Math.max(q_tilde[filter] - q_le[cur_frame_idx][ filter], 0.0);
		    }
		
		    if (frameNo == 2*MEDIUM_POWER_WINDOW_LENGTH)
		    {
		       for (int filter = 0; filter < nFilters; ++filter)
		    	  q_f[prev_frame_idx][filter] = q0[filter] * LOWER_ENVELOPE_INITIALIZATION_FACTOR;
		    }
		
		    // Compute floor value for each filter from previous floor and rectivied current value
		    lower_envelope(q0, q_f, cur_frame_idx, LAMBDA_A, LAMBDA_B, nFilters);
		    
		    /* Temporal masking - emphasize onset of filter increase */
		    for (int filter = 0; filter < nFilters; ++filter)
		    {
		    	q_p[cur_frame_idx][filter] = Math.max(LAMBDA_T * q_p[prev_frame_idx][filter], q0[filter]);
		        if (q0[filter] >= LAMBDA_T * q_p[prev_frame_idx][filter])
		           q_tm[filter] = q0[filter];
		        else
		           q_tm[filter] = MU_T * q_p[prev_frame_idx][filter];
		
		        r_sp[filter] =  Math.max(q_tm[filter], q_f[cur_frame_idx][filter]);
		    }
		
		    /* Excitation/Non-Excitation */
		    for (int filter = 0; filter < nFilters; ++filter)
		    {
		      if (q_tilde[filter] >= EXCITATION_THRESHOLD * q_le[cur_frame_idx][filter])
		        r[filter] = r_sp[filter];
		      else
		        r[filter] = q_f[cur_frame_idx][filter];
		    }
		
		    /* Spectral weight smoothing */
		    for (int filter = 0; filter < nFilters; ++filter)
		    {
		      ratio[filter] = r[filter] / Math.max(q_tilde[filter], DBL_EPSILON);
		    }
		    
		    // Smooth horizontally over adjacent filter values
		    for (int filter = 0; filter < nFilters; ++filter)
		    {
		      s[filter] = 0.0;
		      first = Math.max(filter-CHANNEL_WINDOW, 0);
		      last   = Math.min(filter+CHANNEL_WINDOW, nFilters-1);
		      for (int idx = first; idx <= last; ++idx)
		        s[filter] += ratio[idx];
		
		      s[filter] /= last-first+1;
		    }
		
		    // Mean using power value of previous frame (assuming power window length = 2)
		    f_idx  = (int)(frameNo-MEDIUM_POWER_WINDOW_LENGTH+1) % (MEDIUM_POWER_WINDOW_LENGTH*2+1);
		    t_mean = 0.0;
		    for (int filter = 0; filter < nFilters; ++filter)
		    {
		      t[filter] = s[filter] * power[f_idx][filter];
		      t_mean += t[filter];
		    }
		    t_mean /= nFilters;
		
		  }
		  else
		  {
		    /* For the first frames, just copy the power into the t array */
		    f_idx  = (int)((frameNo-MEDIUM_POWER_WINDOW_LENGTH+1) % (MEDIUM_POWER_WINDOW_LENGTH*2+1));
		    if (f_idx<0) f_idx = 0;  // Just use current frame if there is no previous one
		    
		    System.arraycopy(power[f_idx], 0, t, 0, nFilters);

		    t_mean = 0.0;
		    for (int filter = 0; filter < nFilters; ++filter)
		      t_mean += t[filter];
		    t_mean /= nFilters;
		  }
		
		  /* Online power normalization and rate-level non-linearity */
		  /** exponent is for the power law non-linearity */
		  
		  double mu_temp = mu_prev;
		  mu_cur = lAMBDA_MU * (mu_prev) + (1-lAMBDA_MU) * t_mean;
		  mu_prev = mu_temp;
		  
		  for (int filter = 0; filter < nFilters; ++filter)
		    u[filter] = Math.pow(meanPowerCoefficient*t[filter] / (mu_cur), exponent);
		
		  /* DCT */
	      dct.applyForwardTransform(u, featureVector);
		
		  /* Normalization to conform to Matlab DCT */
		  featureVector[0] /= (2.0*Math.sqrt(nFilters));
		  
		  int nCeps = featureVector.length;
		  for (int i = 1; i < nCeps; ++i)
		    featureVector[i] /= Math.sqrt(2.0*nFilters);
		
		  /* Copy on output buffer */
		  System.arraycopy(featureVector, 0, pncc, pnccOffset, nCeps);
	}

	/** Compute lower bound envelope of data spectrum for noise removal
	 * 
	 * @param x Filter data array
	 * @param env Current/previous envelope ring buffer
	 * @param envIndex Index to store current enveelope 
	 * @param lambda_a First multiplier coefficient
	 * @param lambda_b Second multiplier coefficient
	 * @param nFilters Number of filters
	 */
	private void lower_envelope(final double x[], final double env[][]
			            , int envIndex, double lambda_a, double lambda_b, int nFilters)
	{
		int prevIndex = (envIndex + 1)%2;
		
		for (int filter = 0; filter < nFilters; ++filter)
		{
			if (x[filter] >= env[prevIndex][filter])
			{
				env[envIndex][filter] = lambda_a * env[prevIndex][filter] + (1-lambda_a) * x[filter];
			}
			else
			{
				env[envIndex][filter] = lambda_b * env[prevIndex][filter] + (1-lambda_b) * x[filter];
			}
		}
	}
	
	/** Average the values in the power ring buffer 
	 * 
	 * @param power Power ring buffer
	 * @param q_tilde Average value of the ring buffer
	 * @param nFilters Number of gamma filters
	 */
	private void average(final double power[][], final double q_tilde[], int nFilters)
	{
	    Arrays.fill(q_tilde,  0);
	  
	    int ringSize = power.length;
	
		for (int filter = 0; filter < nFilters; ++filter)
		{
		   for (int ring = 0; ring < ringSize; ++ring)
		      q_tilde[filter] += power[ring][filter];
		
		   q_tilde[filter] /= ringSize;
		}
	}
	
}   // End of PNCC class
