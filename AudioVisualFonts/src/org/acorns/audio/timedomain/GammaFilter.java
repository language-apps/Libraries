package org.acorns.audio.timedomain;

/*
 *=========================================================================
 * An efficient implementation of the 4th order gammatone filter
 *-------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *-------------------------------------------------------------------------
 *
 *  The gammatone filter is commonly used in models of the auditory system.
 *  The algorithm is based on Martin Cooke's Ph.D work (Cooke, 1993) using 
 *  the base-band impulse invariant transformation. This implementation is 
 *  highly efficient in that a mathematical rearrangement is used to 
 *  significantly reduce the cost of computing complex exponentials. 
 *  
 *  This version is converted to Java and significantly edited 
 *                    by Dan Harvey, cs.sou.edu/~harveyd, May 2015 from the C version. 
 *  
 *  For more detail on the C implementation see
 *  http://www.dcs.shef.ac.uk/~ning/resources/gammatone/, Ning Ma, University of Sheffield
 *  n.ma@dcs.shef.ac.uk, 09 Mar 2006
 * 
 */
public class GammaFilter 
{
	public static final double BW_CORRECTION = 1.0190;
	public static final double VERY_SMALL_NUMBER = 1e-200;

	private static final  double EARQ  = 9.26449;	// Glasberg and Moore Parameters
	private static final  double MINBW = 24.7;

	private double fs;
	private double[][] envelope;
	
	/** INstantiate a GammaFilter object
	 * 
	 * @param fs Sampling rate in Hz
	 */
	public GammaFilter(double fs)
	{
		this.fs = fs;
	}
	
	/** Apply a gammatone filter to an audio signal
	 *  env = gammatone(x, cf, hrect) 
	 *
	 *  @param x     - input signal
	 *  @param first - starting point of a particular frame
	 *  @param last  - ending point of a particular frame
	 *  @param cf    - center frequency of the filter (Hz)
	 *
	 *  @return array of instantaneous filter amplitudes per sample
 	 * 
	 */
	public double[] applyGammaFilter( double[] x, int first, int last, double cf)
	{
	   if (first<0) first = 0;
	   if (last<0 || last>x.length) last = x.length;
	   if (first>last) throw new NumberFormatException();

	   int time, nsamples = last - first;
	   double[] env = new double[nsamples];

	   double a, tpt, tptbw, gain;
	   double p0r, p1r, p2r, p3r, p4r, p0i, p1i, p2i, p3i, p4i;
	   double a1, a2, a3, a4, a5, u0r, u0i; /*, u1r, u1i;*/
	   double qcos, qsin, oldcs, coscf, sincf;

	  /*=========================================
	   * Initialising variables
	   *=========================================
	   */
	   tpt = ( Math.PI + Math.PI ) / fs;
	   
	   double erb = 24.7 * ( 4.37e-3 * cf + 1.0 ); 
	   tptbw = tpt * erb * BW_CORRECTION;
	   a = Math.exp ( -tptbw );

	   /* based on integral of impulse response */
	   gain = ( tptbw*tptbw*tptbw*tptbw ) / 3;

	   /* Update filter coefficients */
	   a1 = 4.0*a; a2 = -6.0*a*a; a3 = 4.0*a*a*a; a4 = -a*a*a*a; a5 = a*a;
	   p0r = 0.0; p1r = 0.0; p2r = 0.0; p3r = 0.0; p4r = 0.0;
	   p0i = 0.0; p1i = 0.0; p2i = 0.0; p3i = 0.0; p4i = 0.0;
	 
	  /*===========================================================
	   * exp(a+i*b) = exp(a)*(cos(b)+i*sin(b))
	   * q = exp(-i*tpt*cf*t) = cos(tpt*cf*t) + i*(-sin(tpt*cf*t))
	   * qcos = cos(tpt*cf*t)
	   * qsin = -sin(tpt*cf*t)
	   *===========================================================
	   */
	   coscf = Math.cos ( tpt * cf );
	   sincf = Math.sin ( tpt * cf );
	   
	   qcos = 1; qsin = 0;   /* t=0 & q = exp(-i*tpt*t*cf)*/
	   for ( time=first; time<last; time++ )
	   {
	      /* Filter part 1 & shift down to d.c. */
	      p0r = qcos*x[time] + a1*p1r + a2*p2r + a3*p3r + a4*p4r;
	      p0i = qsin*x[time] + a1*p1i + a2*p2i + a3*p3i + a4*p4i;

	      /* Clip coefficients to stop them from becoming too close to zero */
	      if (Math.abs(p0r) < VERY_SMALL_NUMBER)
	        p0r = 0.0F;
	      if (Math.abs(p0i) < VERY_SMALL_NUMBER)
	        p0i = 0.0F;
	      
	      /* Filter part 2 */
	      u0r = p0r + a1*p1r + a5*p2r;
	      u0i = p0i + a1*p1i + a5*p2i;

	      /* Update filter results */
	      p4r = p3r; p3r = p2r; p2r = p1r; p1r = p0r;
	      p4i = p3i; p3i = p2i; p2i = p1i; p1i = p0i;
	      
	      /*==========================================
	       * Instantaneous magnitude (Hilbert envelope)
	       * env = abs(u) * gain;
	       *==========================================
	       */
          env[time-first] = Math.sqrt ( u0r * u0r + u0i * u0i ) * gain;

	      /*====================================================
	       * The basic idea of saving computational load:
	       * cos(a+b) = cos(a)*cos(b) - sin(a)*sin(b)
	       * sin(a+b) = sin(a)*cos(b) + cos(a)*sin(b)
	       * qcos = cos(tpt*cf*t) = cos(tpt*cf + tpt*cf*(t-1))
	       * qsin = -sin(tpt*cf*t) = -sin(tpt*cf + tpt*cf*(t-1))
	       *====================================================
	       */
	      qcos = coscf * ( oldcs = qcos ) + sincf * qsin;
	      qsin = coscf * qsin - sincf * oldcs;
	   }
	   
       return env;
	}
	
	/** Apply a gammatone filter bank to a frame of an audio signal
	 *  cochleaGram[] = gammatone(x, first, last, cf, hrect) 
	 *
	 *  @param x     - input signal
	 *  @param first - starting point of a particular frame
	 *  @param last  - ending point of a particular frame
	 *  @param filterBank - center frequency of a group of filters (Hz)
	 *  @param window - window (ex: Hamming, Hanning, etc) to apply to the filtered data
	 *
	 *  @return array of instantaneous filter bank amplitudes for the frame 
 	 * 
	 */
	public double[] applyGammaFilter( double[] x, int first, int last, double[] filterBank, double[] window)
	{
		int nFilters = filterBank.length;
		
		envelope = new double[nFilters][];
		double[] cochleaGram = new double[nFilters];
		
		if (first<0) first = 0;
		if (last<0 || last>x.length) last = x.length;
		if (first>last) throw new NumberFormatException();
		
		double value;
		for (int filter=0; filter<nFilters; filter++)
		{
			envelope[filter] = applyGammaFilter(x, first, last, filterBank[filter]);
			for (int time=first; time<last; time++)
			{
				value = (window!=null) ? window[time] : 1.0;
				cochleaGram[filter] += envelope[filter][time-first] * value;
			}	
		}
		return cochleaGram;
	}
	
	/** Return the time domain gamma filter bank
	 * 
	 * @return double[filter][time] filter bank
	 */
	public double[][] getGammaFilterBank()
	{
		return envelope;
	}
	
	/** Compute the center frequencies on the ERB scale
	 * 
	 * @param lowFreq Minimum frequency
	 * @param hiFreq Maximum frequency (half of sample rate)
	 * @param nFilters Number of filters
	 * @return Array of center frequencies
	 */
	public static double[] getCenterFrequencies(double lowFreq, double hiFreq, int nFilters)
	{
	    double freqs[] = new double[nFilters];
	
	    double earQMinBw  = EARQ*MINBW;
	    double tmp2  = (-Math.log(hiFreq +earQMinBw) + Math.log(lowFreq +earQMinBw))/nFilters;
	    double tmp3  = hiFreq + earQMinBw;
	
        for (int i = 0; i < nFilters; ++i)
        	freqs[i] = -earQMinBw + Math.exp((i+1)*tmp2) * tmp3;
	
	    return freqs;
	}
	
}

