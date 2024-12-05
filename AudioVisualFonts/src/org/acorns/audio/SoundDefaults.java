/**
 * SoundDefaults.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 * 
 */
 
package org.acorns.audio;

import javax.sound.sampled.*;
import javax.swing.JOptionPane;

import org.acorns.language.LanguageText;

import java.awt.*;
import java.io.*;
import java.util.*;

import com.plexteq.ssb.nativeimpl.SecurityScopedBookmarks;

public class SoundDefaults
{
    /** Record rate in frames per second. */
    private static final float RECORDRATE = 44100f;
    /** Recording device index (-1 = default). */
    private static final int RECORDDEVICE = 0;
    /** Internal audio compression format for */
    private static final String AUDIOCOMPRESSION = "wav";
        /** Sample size in bits */
    private static final int   BITS = 16;
    /** Samples per seconds */
    private static final float RATE = 16000f;
    /** Frame size in bytes */
    private static final int   FRAMEBYTES = 2;
    /** Frames per second */
    private static final float FRAMERATE = 16000f;
    /** Number of channels per sound clip */
    private static final int   CHANNELS  = 1;
    /** Encoding type */
    private static final 
            AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    /** Big or little ENDIAN format */
    private static final boolean BIGENDIAN = false;
    /** Buffer size for recording sound clips */
    public static final int     BUFFERSIZE = 32768;
    /** Use first channel in a  multiple channel signal. */
    public static final int     SELECTCHANNEL = 0;
    /** Average stereo channels in a  multiple channel signal. */
    public static final int     AVERAGECHANNEL = 1;
    /** Do not use any filter. */
    public static final int     NOWINDOW  = 0;
    /** Use a Blackman window for a window sync filter. */
    public static final int     HAMMING  = 1;
    /** Use a hamming window for a window sync filter. */
    public static final int     BLACKMAN = 2;
    /** Use a recursive CHEBYCHEV filter. Not Implemented! */
    public static final int    HANNING = 3;
    /** Do not filter the audio signal */
    public static final int    NOFILTER = 0;
    /** Use a butterworth bandpass filter */
    public static final int    BANDPASS = 1;
    /** Use a WIENER filter */
    public static final int    WIENER = 2;
    /** Use a Spectral Subtracting filter */
    public static final int    SPECTRAL = 3;
    /** Use a window sinc bandpass filter */
    public static final int    WINDOWSINC = 4;
    
    /** Round to the next frequency amplitude that crosses zero boundary. */
    private static final boolean ROUNDTOZERO = true;
    /** Maximum sound clip in minutes. */
    private static final int     MAXMINS = 10;
    
    /** Minimum bandpass filter frequency */
    private static final double  MINFREQ = 130;
    /** Maximum bandpass filter frequency */
    private static final double  MAXFREQ = 6800;
    /** Size of window sync filter. Must be even, and under 64 for speed. */
    private static final int     FILTERSIZE = 32;
    /** Amount to trim out of recordings loaded into memory. */
    private static final int     TRIMSIZE = 72;

    // Constants used for voice activation
    /** Milliseconds for voice activation window. */
        private static final int ACTIVATION = 10;
    /** Decibel threshold for voice activation (0 if disabled)*/
    private static final int     VOICE = 0;

    // Constants used for the front end speech recognizer
    
    /**Emphasis factor, used by the preEmphasizer method*/
    private static final float     EMPHASISFACTOR = .97f;
    /** The number of filters for the MEL filter bank 40 is optimal
     * for a 16000Hz sampling rate*/
    private static final int       NUMBEROFMELFILTERS = 40;
    /** The Gaussian variance for the mel filters */
    private static final double    MELGAUSSIANVARIANCE = 2.0;
    /** The length of the MFCC vector.  */
    private static final int       CEPSTRUMLENGTH = 13;
    /** The maximum number of Cepstrum features. */
    private static final int       MAXCEPSTRUM    = 20;
    /**The length of the window in used for hamming function*/
    private static final float     WINDOWSIZE = 25.625f;
    /**The length of the offset used for hamming function*/
    private static final float     WINDOWSHIFT = 10f;
    /** Mask to displaying feature vectors (bit on means display the feature) */
    private static final long      FEATUREMASK = (1<<(CEPSTRUMLENGTH + 1)) - 1;
    /** Boolean for short term spectrograph */
    private static final boolean NARROWBAND = true;
    /** Boolean for spectrograph with a color palette */
    private static final boolean COLORPALETTE = false;

    private static double[] NARROWSPECT = {WINDOWSIZE, WINDOWSHIFT};
    private static double[] WIDESPECT = {3.0, 1.0};
       
    // Constants for linear prediction
    /** Auto correlation method using LEVINSON-DURBIN algorithm */
    public static final int     TOEPLITZ = 0;
    /** Covariance method using CHLESKI decomposition */
    public static final int     CHOLESKI = 1;
    /** Default number of LPC coefficients */
    private static final int    LPCCOEFFICIENTS = 8;
    
    //Constants for Mel filter types
    /** Triangular MEL filters */
    public static final int    TRIANGLE = 0;
    /** GAUSSIAN MEL filters */
    public static final int    GAUSSIAN = 1;

    /** Detect pitch using the harmonic product algorithm */
    public static final int HARMONICPRODUCT = 0;
    /** Detect pitch using the YIN algorithm */
    public static final int YIN = 1;
    /** Detect pitch using the CEPSTRUM algorithm */
    public static final int CEPSTRUM = 2;
    
    /** Pitch rate change per button click */
    public static final float RATECHANGE = 10f;
    /** Default rate change - change frame rate to make signal faster */
    public static final int DEFAULTRATEALG = 0;
    /** Use PSOLA algorithm when altering signal speed to preserve pitch */
    public static final int PSOLAALG = 1;

    /** Dynamic time warp flag to indicate correct value */
    public static final int CORRECT = 0;
    /** Dynamic time warp default value for close */
    private static final double DTWCORRECTVALUE = 0.55;
    /** Dynamic time warp flag to indicate close value */
    public static final int CLOSE = 1;
    /** Dynamic time warp default value for correct */
    private static final double DTWCLOSEVALUE = 0.48;
    /** Linear distance for dynamic time warp audio comparison algorithm */
    public static final int LINEAR = 0;
    /** Euclidian distance for Dynamic Time Warp audio comparison algorithm */
    public static final int EUCLIDIAN = 1;
    /** Number of dynamic time warp weights for audio comparison algorithm
          3 * maximum cepstrum features + 3 for energy
     */
    private static final int WARPWEIGHTS = 3 * (MAXCEPSTRUM + 3);

    // Constants relating to colors.
    /** Panel background color behind buttons */
    public static final Color BACKGROUND     = new Color(200,200,200);
    /** Panel Dark background in scrollbar viewport */
    public static final Color FOREGROUND     = Color.WHITE;
    /** A darker background color */
    public static final Color DARKBACKGROUND = new Color(100,100,100);
    /** Color for error messages */
    public static final Color ERROR          = new Color(220,0,0);
    /** Color behind combo box */
    public static final Color COMBOCOLOR     = new Color(150,150,150);
    /** Color of selected area */
    public static final Color SELECT         = new Color(170,170,140, 150);
    /** Color of center line of sound display */
    public static final Color LINE           = Color.CYAN;
    /** Color of sound wave */
    public static final Color WAVE           = Color.WHITE;
    
    private static float   recordRate            = RECORDRATE;
    private static int     recordDevice          = RECORDDEVICE;
    private static String  audioCompression      = AUDIOCOMPRESSION;
    private static int     bits                  = BITS;
    private static float   rate                  = RATE;
    private static int     frameBytes            = FRAMEBYTES;
    private static float   frameRate             = FRAMERATE;
    private static int     channels              = CHANNELS;
    private static boolean bigEndian             = BIGENDIAN;
    private static int     buffer_size           = BUFFERSIZE;
    private static int     channelOption         = AVERAGECHANNEL;
    private static AudioFormat.Encoding encoding = ENCODING;
    private static boolean roundToZero           = ROUNDTOZERO;
    private static int     windowType            = HAMMING;
    private static int     filterType            = NOFILTER;
    private static int     filterSize            = FILTERSIZE;
    private static int     trimSize              = TRIMSIZE;
    private static double  minFreq               = MINFREQ;
    private static double  maxFreq               = MAXFREQ;
    private static int     maxMins               = MAXMINS;
    private static float   emphasisFactor        = EMPHASISFACTOR;
    private static int     numberOfMelFilters    = NUMBEROFMELFILTERS;
    private static double  melGaussianDamper     = MELGAUSSIANVARIANCE;
    private static int     melCepstralType       = GAUSSIAN;
    private static int     cepstrumLength        = CEPSTRUMLENGTH;
    private static float   windowSize            = WINDOWSIZE;
    private static float   windowShift           = WINDOWSHIFT;
    private static long    featureMask           = FEATUREMASK;
    private static int     zoom                  = 1;
    private static int     voice                 = VOICE;
    private static int     activation            = ACTIVATION;
    private static int     LPCType               = TOEPLITZ;
    private static int     LPCCoefficients       = LPCCOEFFICIENTS;
    private static boolean spectrograph          = NARROWBAND;
    private static boolean colorPalette          = COLORPALETTE;
    private static int     pitchDetection        = HARMONICPRODUCT;
    private static float   rateChange            = RATECHANGE;

    private static int     rateAlgorithm = PSOLAALG;
    private static int     DTWDistance   = LINEAR;
    private static double[]DTWCorrectness 
                                 = new double[]{DTWCORRECTVALUE, DTWCLOSEVALUE};
    private static double[]DTWWeights    = new double[WARPWEIGHTS];
    
    private static String  sandboxKey = null;
    private static String  bookmarkFolder = "Documents";
    private static String  bookmarkLocation = null;
    private static String  bookmark = "";
    private static boolean bookmarkOk = true;

   /**
    * Read path names and sound defaults to the 'sounds' file in the acorns 
    * folder within the user home directory.
    * @return Array of strings. 
    * Index 0 is the path to read sound files. 
    * Index 1 is the path to write sound files.
     */
   public static String[] readSoundDefaults()
   {

      ObjectInputStream ois = null;
      String[]  paths = null;

      try
      {
        // Create settings directory if necessary.   
        String dirName = getHomeDirectory();
        File   dirFile = new File(dirName);
        if (!dirFile.exists())  {  dirFile.mkdir();  }
         
        // Read settings file or create it.
        String settings         = dirName + "/" + "soundDefaults";
        ois = new ObjectInputStream(new DataInputStream(new FileInputStream(settings)));

        paths            = (String[])ois.readObject();
        recordRate       = ois.readFloat();
        recordDevice     = ois.readInt();
        audioCompression = ois.readUTF();
        bits             = ois.readInt();
        rate             = ois.readFloat();
        frameBytes       = ois.readInt();
        frameRate        = ois.readFloat();
        channels         = ois.readInt();
        bigEndian        = ois.readBoolean();
        buffer_size      = ois.readInt();
        channelOption    = ois.readInt();

        char encodeType = ois.readChar();
        switch (encodeType)
        {   case 's':
               encoding = AudioFormat.Encoding.PCM_SIGNED;
               break;
            case 'u':
               encoding = AudioFormat.Encoding.PCM_UNSIGNED;
               break;
            case 'U':
               encoding = AudioFormat.Encoding.ULAW;
               break;
            case 'A':
               encoding = AudioFormat.Encoding.ALAW;
               break;
            default:
               encoding = AudioFormat.Encoding.PCM_SIGNED;
               break;
        }

        roundToZero         = ois.readBoolean();
        windowType          = ois.readInt();
        filterSize          = ois.readInt();
        trimSize            = ois.readInt();
        minFreq             = ois.readDouble();
        maxFreq             = ois.readDouble();
        maxMins             = ois.readInt();
        emphasisFactor      = ois.readFloat();
        numberOfMelFilters  = ois.readInt();
        melGaussianDamper   = ois.readDouble();
        melCepstralType     = ois.readInt();
        cepstrumLength      = ois.readInt();
        windowSize          = ois.readFloat();
        windowShift         = ois.readFloat();
        featureMask         = ois.readLong();
        zoom                = ois.readInt();
        if (zoom==0) zoom   = 1;
        voice               = ois.readInt();
        activation          = ois.readInt();
        LPCType             = ois.readInt();
        LPCCoefficients     = ois.readInt();
        spectrograph        = ois.readBoolean();
        colorPalette        = ois.readBoolean();
        pitchDetection      = ois.readInt();
        rateChange          = ois.readFloat();
        rateAlgorithm       = ois.readInt();
        DTWDistance         = ois.readInt();
        DTWCorrectness      = (double[])ois.readObject();
        DTWWeights          = (double[])ois.readObject();
        filterType          = ois.readInt();

        // For version 8.0
        DTWCorrectness 
        = new double[]{DTWCORRECTVALUE, DTWCLOSEVALUE};
        LPCCoefficients       = LPCCOEFFICIENTS;

        // For version 8.1 to force reload of 8.0 parameters
        ois.readDouble();
        
        // For version 8.0
        LPCCoefficients     = ois.readInt();
        DTWCorrectness      = (double[])ois.readObject();
        
      }
      catch (Exception ioe)   {}  // Use defaults after first exception
      
      try {ois.close(); }catch (Exception ex) {}
      return paths;
				
   }  //End of readSoundDefaults  
   
   /**
    * Write path names and sound defaults to the 'sounds' file in the acorns 
    * folder within the user home directory.
    * @return Array of strings. 
    * Index 0 is the path to read sound files. 
    * Index 1 is the path to write sound files.
    * 
    */
   public static String[] writeSoundDefaults(String[] paths)
   {  ObjectOutputStream oos = null;
      
      try
      {  
         String dirName = getHomeDirectory();
         String settings = dirName + "/" + "soundDefaults";
         oos = new ObjectOutputStream
                      (new DataOutputStream(new FileOutputStream(settings)));

         oos.writeObject(paths);
         oos.writeFloat(recordRate);
         oos.writeInt(recordDevice);
         oos.writeUTF(audioCompression);
         oos.writeInt(bits);
         oos.writeFloat(rate);
         oos.writeInt(frameBytes);
         oos.writeFloat(frameRate);
         oos.writeInt(channels);
         oos.writeBoolean(bigEndian);
         oos.writeInt(buffer_size);
         oos.writeInt(channelOption);

         if (encoding.equals(AudioFormat.Encoding.PCM_SIGNED))
              oos.writeChar('s');
         else if (encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED))
              oos.writeChar('u');
         else if (encoding.equals(AudioFormat.Encoding.ULAW))
              oos.writeChar('U');
         else if (encoding.equals(AudioFormat.Encoding.ALAW))
              oos.writeChar('A');
         else  oos.writeChar('s');

         oos.writeBoolean(roundToZero);
         oos.writeInt(windowType);
         oos.writeInt(filterSize);
         oos.writeInt(trimSize);
         oos.writeDouble(minFreq);
         oos.writeDouble(maxFreq);
         oos.writeInt(maxMins);
         oos.writeFloat(emphasisFactor);
         oos.writeInt(numberOfMelFilters);
         oos.writeDouble(melGaussianDamper);
         oos.writeInt(melCepstralType);
         oos.writeInt(cepstrumLength);
         oos.writeFloat(windowSize);
         oos.writeFloat(windowShift);
         oos.writeLong(featureMask);

         oos.writeInt(zoom);
         oos.writeInt(voice);
         oos.writeInt(activation);
         oos.writeInt(LPCType);
         oos.writeInt(LPCCoefficients);
         oos.writeBoolean(spectrograph);
         oos.writeBoolean(colorPalette);
         oos.writeInt(pitchDetection);
         oos.writeFloat(rateChange);
         oos.writeInt(rateAlgorithm);
         oos.writeInt(DTWDistance);
         oos.writeObject(DTWCorrectness);
         oos.writeObject(DTWWeights);
         oos.writeInt(filterType);

         // For version 8.1 (Force reload of values)
         oos.writeDouble(0.0);
 
         // For version 8.0
         oos.writeInt(LPCCoefficients);
         oos.writeObject(DTWCorrectness);
         
      }
      catch (Exception exception)  {}
      try {oos.close(); }catch (Exception e) {}
      return paths;
   
   }  //End of writeSoundDefaults
    
   /** Get the frame rate required for recording */
   public static float getRecordRate()              {return recordRate;}
   /** Set the frame rate required for recording 
    * @param r frame rate required for recording.
    */    
   public static void setRecordRate(float r)        {recordRate = r;}
   
    /** Get the index to the mixer for the recording device */
   public static int getRecordDevice()              
   { 
	   ArrayList<Mixer.Info> mixers = getMixersInfo();
	   if (recordDevice > mixers.size() -1)
		   recordDevice = mixers.size() -1;
	   return recordDevice;
   }
   
   /** Set the index to the mixer for the recording device 
    * @param d recording device index.
    */    
   public static void setRecordDevice(int d)        {recordDevice = d;}
   
    /** Get the index to the mixer for the recording device */
   public static String getAudioCompression()       {return audioCompression;}
   /** Set the index to the mixer for the recording device 
    * @param c recording device index.
    */    
   public static void setAudioCompression(String c) {audioCompression = c;}
   
   /** Get default bits per sample. */
    public static int     getBits()                 {return bits;}
    /** Set default bits per sample. 
     *  @param b bits per second.
     */
    public static void    setBits(int b)            {bits = b;}
    
    /** Get default samples per second.  */
    public static float   getRate()                 {return rate;}   
    /** Set default samples per second.
     *  @param r samples per second. 
     */
    public static void    setRate(float r)          {rate = r;}
    
    /** Get default bytes per recording frame.   */
    public static int     getFrameBytes()           {return frameBytes;}
    /** Set default bytes per recording frame. 
     *  @param b bytes per recording frame.
     */
    public static void    setFrameBytes(int b)      {frameBytes = b;}
    
    /** get default frames per second.   */
    public static float   getFrameRate()            {return frameRate;}
    /** Set default frames per second.
     *  @param r frame rate. 
     */
    public static void    setFrameRate(float r)     {frameRate = r;}
    
    /** Get default number of channels per recording.  */
    public static int     getChannels()             {return channels;}
    /** Set default number of channels per recording. 
     *  @param c number of channels per recording.
     */
    public static void    setChannels(int c)        
    {  channels = c;   }
    
    /** Get default big or little Endian option, true = big.  */
    public static boolean getBigEndian()            {return bigEndian;}
    /** Set default big or little Endian option, true = big.
     *  @param b true for bigEndian, false otherwise.
     */
    public static void    setBigEndian(boolean b)   {bigEndian = b;}
    
    /** Get default recording buffer size.  */
    public static int     getBufferSize()           {return buffer_size;}
    /** Set default recording buffer size. 
     *  @param b buffer size.
     */
    public static void    setBufferSize(int b)      {buffer_size = b;} 
    
    /** Get default for averaging or selecting stereo channels.  */
    public static int     getChannelOption()        {return channelOption;}
    /** Set default for averaging or selecting stereo channels.
     *  @param c AVERAGE_CHANNEL or SELECT_CHANNEL.
     */
    public static void    setChannelOption(int c)   {channelOption = c;}
    
    /** Get default round selection to next crossing zero.   */
    public static boolean getRoundToZero()          {return roundToZero;}
    /** Set default round selection to next crossing zero. 
     * @param r true if round to next zero sample, false otherwise.
     */
    public static void  setRoundToZero(boolean r) {roundToZero = r;}
    
    /** Set key for determining if sandboxed for MaccOs apple store submitted applications */
    public static void  setSandboxKey(String key) 
    { 
    	sandboxKey = key; 
    	
    	if (isSandboxed())
    		bookmarkFolder = "Downloads";
    }
       
    /** Remove Mac app container from file path */
    public static String normalizeFilePath(String path)
    {
    	String homeDir = System.getProperty("user.home");
    	if (path.startsWith(homeDir))
    	{
    		path = path.substring(homeDir.length());
    	
	    	String separator = System.getProperty("file.separator");    	
	    	if (path.startsWith(separator))
	    		path = path.substring(1);
	    	return path;
	    }	
    	
    	path = path.replaceFirst("\\/Users\\/.*\\/", "");
    	return path;
    }
    
    
    /** Determine if the application is sandboxed */
    public static boolean isSandboxed()
    {
    	if (sandboxKey == null) return false;
    	
    	String path = getHomeDirectory();
    	return path.toLowerCase().contains(sandboxKey);
    }
    
    // FFT parameters.
    /** Get default window sync filter type.    
     * 
     * @return NOFILTER, HAMMING, BLACKMAN, HANNING
     */
    public static int  geWindowType()           {return windowType;}
    /** Set default window sync filter type.
     *  @param w NOFILTER, HAMMING, BLACKMAN, HANNING
     */
    public static void seWindowType(int w)      {windowType = w;}
    
    /** Get the type of filter
     * 
     * @return f NOWINDOW, BANDPASS, or WIENER
     */
    public static int getFilterType()  { return filterType; }
    
    /** Set the filter type
     * 
     * @param f NOWINDOW, BANDPASS, or WIENER
     */
    public static void setFilterType(int f) { filterType = f; }
    
    /** Get default window filter size (must be even). */
    public static int     getFilterSize()           {return filterSize;}
    /** Set default window filter size (must be even). 
     *  @param f Size of the widow sync filter.
     */
    public static void    setFilterSize(int f)      {filterSize = f;}
    
    /** Get minimum frequency in thousands. */
    public static double  getMinFreq()              {return minFreq;}

    /** Set minimum frequency in thousands. 
     *  @param f size of sync filter windows.
     */
    public static void    setMinFreq(double f)     {minFreq = f;}

    /** Get maximum frequency in thousands. */
    public static double  getMaxFreq()             {return maxFreq;}

    /** Set maximum frequency in thousands. 
     *  @param f size of sync filter windows.
     */
    public static void    setMaxFreq(double f)          {maxFreq = f;}
    /** Get default number of frames to trim when loading files. */
    public static int     getTrimSize()              {return trimSize;}
    /** Set default number of frames to trim when loading files. 
     *  @param s size of sync filter windows.
     */
    public static void    setTrimSize(int s)         {trimSize = s;}
    
    /** Get default encoding type (ex: signed, unsigned, ALAW, or ULAW). */
    public static AudioFormat.Encoding getEncoding()       {return encoding;}
    /** Set default encoding type (ex: signed or unsigned). 
     *  @param e Encoding type (ex: AudioFormat.Encoding.PCM_SIGNED);
     */
    public static void setEncoding(AudioFormat.Encoding e) {encoding = e;}
    
    /** Get default number of frames to trim when loading files. */
    public static int     getMaxMin()           {return maxMins;}
    /** Set default number of frames to trim when loading files. 
     *  @param m size of sync filter windows.
     */
    public static void    setMaxMins(int m)      {maxMins = m;}
    
    /** Get current zoom factor */
    public static int getZoom()   {return zoom;}
    
    /** Set current zoom factor
     *  @param zoomFactor desired zoom factor
     */
    public static void setZoom(int zoomFactor) { zoom = zoomFactor; }

    /** Get the voice activation threshold window in milliseconds
     *
     * @return voice activation window size in milliseconds
     */
    public static int getActivationWindow()  { return activation; }

    /** Set the voice activation window
     *
     * @param ms length of voice activation window in milliseconds
     */
    public static void setActivationWindow(int ms)  { activation = ms; }

    /** Get voice activation threshold
     *
     * @return threshold roughly in decibels
     */
    public static int getActivationThreshold()  { return voice; }

    /** Set voice activation threshold in decibels (rough measurement)
     *
     * @param v voice activation threshold (roughly in decibels)
     */
    public static void setActivationThreshold(int v) { voice = v; }
    
   /** Get default size for pre-emphasis */
    public static float   getEmphasisFactor()  {return emphasisFactor;}
    
    /** Set default size for emphasis
     *  @param factor desired emphasis factor
     */
    public static void setEmphasisFactor(float factor)
    {  emphasisFactor = factor; }
    
    /** Get default size for number of MEL filters */
    public static int   getNumberOfMelFilters()  {return numberOfMelFilters;}
    
    /** Set desired number of MEL filters
     *  @param num desired number of MEL filters
     */
    public static void setNumberOfMelFilters(int num)
    {  numberOfMelFilters = num;  }
    
    /** Get Gaussian variance of MEL filter bins */
    public static double getMelGaussianDampingFactor() { return melGaussianDamper;}
    
    /** Set Gaussian variance of MEL filter bins */
    public static void setMelGaussianVariance(double variance)
    {  melGaussianDamper = variance; }
    
    /** Return MEL CEPSTRAL type
     * 
     * @return TRIANGLE or GAUSSIAN
     */
    public static int getMelFilterType() { return melCepstralType; }
    
    /** Set Type of MEL CEPSTRAL filter type
     * 
     * @param type TRIANGLE or GAUSSIAN
     */
    public static void setMelFilterType(int type)
    {   melCepstralType = type; }
    
    /** Get default length of MEL frequency coefficient vector */
    public static int   getCepstrumLength()  {return cepstrumLength;}
    
    /** Set desired length of MEL frequency coefficient vector
     *  @param len desired length of MEL frequency coefficient vector
     */
    public static void setCepstrumLength(int len) { cepstrumLength = len; }
        
    /** Get default length of the frame window  */
    public static float   getWindowSize()  {return windowSize;}
    
    /** Set desired hamming window length in milliseconds
     *  @param len desired window length
     */
    public static void setWindowSize(float len)
    {  windowSize = len;  }
    
    /** Get default length of the frame offset  */
    public static float   getWindowShift()  {return windowShift;}

    /** Set desired offset of successive windows
     *  @param offset desired hamming window offset
     */
    public static void setWindowShift(float offset)
    {  windowShift = offset; }
    
    /** Get default length of the frame offset */
    public static long  getFeatureMask()  {return featureMask;}

    /** Set desired offset of successive hamming windows
     *  @param mask desired hamming window offset
     */
    public static void setFeatureMask(long mask)
    {  featureMask = mask; }
    
    /** Get the type of Linear Prediction algorithm
     * 
     * @return SoundDefaults.TOEPLITZ or SoundDefaults.CHOLESKI
     */
    public static int getLPCType() { return LPCType; }

    /** Set the type of Linear Prediction algorithm
     *
     * @param type  SoundDefaults.TOEPLITZ or SoundDefaults.CHOLESKI
     */
    public static void setLPCType(int type)  { LPCType = type; }

    /** Get then number of Linear Prediction coefficients
     *
     * @return Number of coefficients
     */
    public static int getLPCCoefficients()
    { return LPCCoefficients; }

    /** Set the number of Linear Prediction coefficients
     *
     * @param coefficients The number of coefficients
     */
    public static void setLPCCoefficients(int coefficients)
    {   LPCCoefficients = coefficients; }

    /** Get the spectrograph type
     *
     * @return false if long term, true if short term
     */
    public static boolean isNarrowBandSpectrograph() { return spectrograph; }

    /** Get spectrograph parameters
     *
     * @return [0] Spectrograph window width; [1] SPectrograph window overlap
     */
    public static double[] getSpectrographParams()
    {
        if (isNarrowBandSpectrograph())
             return NARROWSPECT;
        else return WIDESPECT;
    }
    /** Set the spectrograph type
     *
     * @param shortTerm  false = long term, true is short term
     */
    public static void setNarrowBandSpectrograph(Boolean shortTerm)
    {   spectrograph = shortTerm; }

    /** Get the color palette type for spectrograms
     *
     * @return true if color palette, false if grey scale
     */
    public static boolean isColorPalette() { return colorPalette; }

    /** Set color palette
     *
     * @param palette true = by colors, false = grey scale
     */
    public static void setColorPalette(boolean palette)
    { 
        colorPalette = palette;
    }

    /** Set the pitch detection algorithm
     *
     * @param alg 0=HARMONICPRODUCTSPECTRUM, 1=YIN, 2=CEPSTRUM
     */
    public static void setPitchDetect(int alg)
    {   
    	pitchDetection = alg; 
    }

    /** Return the pitch detection algorithm
     *
     * @return 0=HARMONICPRODUCT, 1=YIN, 2=CEPSTRUM
     */
    public static int getPitchDetect()  { return pitchDetection; }

    /** Set the rate change percentage per button click
     * 
     * @param rate  rate change per button click
     */
    public static void setRateChange(float rate) { rateChange = rate; }

    /** Return the rate change percentage per click
     *
     * @return rate change percentage
     */
    public static float getRateChange()  { return rateChange; }

    /** Set the rate change algorithm
     *
     * @param alg DEFAULTRATEALG or PSOLAALG
     */
    public static void setRateAlgorithm(int alg) { rateAlgorithm = alg; }
    /** Get the rate change algorithm
     *
     * @return DEFAULTRATEALG or PSOLAALG
     */
    public static int getRateAlgorithm() {  return rateAlgorithm; }

    /** Set the Dynamic Time Warp distance approach
     * 
     * @param distance LINEAR (0) or EUCLIDIAN (1)
     */
    public static void setDTWDistance( int distance) { DTWDistance = distance; }

    /** Get the Dynamic Time Warp distance approach
     *
     * @return distance LINEAR or EUCLIDIAN
     */
    public static int getDTWDistance() { return DTWDistance; }

    /** Set DTW correctness factors
     *
     * @param index [CORRECT], close if > index [CLOSE]
     * @param correctness factor
     */
    public static void setDTWCorrectness(int index, double correctness)
    { DTWCorrectness[index] = correctness; }

    /** Get DTW correctness factors
     *
     * @param index [if > index [CORRECT], close if > index [CLOSE]
     * @return correctness or closeness factor
     */
    public static double getDTWCorrectness(int index)
    { return DTWCorrectness[index]; }

    /** Set the Dynamic Time Warp audio feature weights
     *
     * @param weights array of weighting factors for each audio feature
     */
    public static void setDTWWeights( double[] weights) { DTWWeights = weights;}

    /** Set the Dynamic Time Warp audio feature weights
     *
     * @return array of weighting factors for each audio feature
     */
    public static double[] getDTWWeights()  { return DTWWeights; }

    private static ArrayList<Mixer.Info> getMixersInfo()
    {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		ArrayList<Mixer.Info> mixers = new ArrayList<Mixer.Info>(); 
		for (Mixer.Info info: mixerInfos)
		{
			Mixer mix = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = mix.getTargetLineInfo();
			if(lineInfos.length>=1 
					&& lineInfos[0].getLineClass().equals(TargetDataLine.class))
			{
				//Only return Microphones
				int lineCount = 0;
				
				for (Line.Info lineInfo:lineInfos)
				{
					Line line;
					try 
					{
						line = mix.getLine(lineInfo);
						line.open();
						lineCount++;
						
					} catch (LineUnavailableException e) { continue; }
					
					line.close();
				}

				if (lineCount>0) { mixers.add(info); }
			}
			
		}
		
		return mixers;

    }
    /** Get list of mixers that apply to current data specifications
     * 
     * @param format Desired format to use to find mixers.
     * @return Array of strings describing the available mixers.
     */
    public static String[] getMixers(AudioFormat format)
    {
    	ArrayList<Mixer.Info> mixerInfo =  getMixersInfo();
        String vendor, version, sep = ", ", data;
        ArrayList<String> vector = new ArrayList<String>();
        
        for (int cnt=0; cnt<mixerInfo.size(); cnt++)
        { 
            vendor = mixerInfo.get(cnt).getVendor();
            version = mixerInfo.get(cnt).getVersion();
            if (vendor.toLowerCase().startsWith("unknown")) vendor = "";
            if (version.toLowerCase().startsWith("unknown")) version="";
            if (vendor.equals("") && version.equals("")) sep = "";
			
            try
            {
                data =  mixerInfo.get(cnt).getName() + sep +  vendor + " " + version;
                
                data += "                                                        ";
                data = data.substring(0,55);
                vector.add(data); 
            }
            catch (IllegalArgumentException ile) {}
            catch (Exception e) {}
          
        }   // End of for loop
        String[] mixerArray = new String[vector.size()];
        for (int m=0; m<vector.size(); m++)  mixerArray[m] = vector.get(m);
        return mixerArray;
    }       // End of getMixers();
    
    public static TargetDataLine getMicrophone() throws LineUnavailableException
    {
        // Find the specification for the input line to use.
        AudioFormat format = createAudioFormat(true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        // Find a particular mixer.
        if (recordDevice>=0)
        {
            // Find array of mixers that we can use.
            ArrayList<Mixer.Info> mixerInfo = getMixersInfo();
            
            // Now look for the one the user wants.
            Mixer  mixer;
            TargetDataLine dataLine;
            int mixerNum = 0;
            for (int cnt=0; cnt<mixerInfo.size(); cnt++)
            { 
                try
                {
                    mixer = AudioSystem.getMixer(mixerInfo.get(cnt));
                    dataLine = (TargetDataLine)mixer.getLine(info);
                    if (mixerNum++ == recordDevice) return dataLine;
                }
                catch (IllegalArgumentException ile) {}
                catch (Exception e) {}
            }
        }
        
        // If couldn't find specified mixer, use default.
        return (TargetDataLine) AudioSystem.getLine(info);
        
    }   // End of getMicrophone()
    
    /** Determine which sound files Java supports
     * 
     *  @return Return an array of file type extensions
     */
    public static String[] getSoundsSupported()
    {
       AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
       String[] extensions = new String[types.length];
       for (int i=0; i<types.length; i++) 
       {  extensions[i] = types[i].getExtension(); }
	   return extensions;
    }
    
    public static String[] getAudioWriterExtensions()
    {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        ArrayList<String> extensions = new ArrayList<String>();
        String extension;
        for (int i=0; i<types.length; i++) 
        {  
     	   extension = types[i].getExtension();
     	   if (!extension.contentEquals("mp2"))
     	     extensions.add(extension);
        }
        
        String[] supportedSounds = new String[extensions.size()];
        supportedSounds = extensions.toArray(supportedSounds);
 	    return supportedSounds;
    }
    
    /** Check if bookmark was successfully set */
    public static boolean isBookmarkOk() { return bookmarkOk; }
    
    /** Check if user approved access to write into a directory */
    public static boolean isBookmarked()
    {
    	String home = getHomeDirectory();
    	File file = new File(home, "bookmark");
    	return file.exists();
    }
 
    /** Dialog to ask the user to select an application scoped bookmark */
    private static String getBookmarkLocation()
    {
    	Frame root = JOptionPane.getRootFrame();

    	// Present dialog for user to select data folder
    	int answer = JOptionPane.showConfirmDialog(root,
                "Give permission to access application data?"
                         , "Confirm Application Permissions"
                         , JOptionPane.YES_NO_CANCEL_OPTION);

    	if (answer != JOptionPane.OK_OPTION)
    	{
    		if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION)
    		{
    			return null;
    		}
    		
    		bookmarkOk = false;
    		return "";
    	}
    	
    	// Only show directories
    	FilenameFilter filter =
    			new FilenameFilter() 
    			{
            		public boolean accept(File dir, String name) 
            		{
            			final File file = new File( dir, name );
            	        return file.isDirectory();
            		}
    			};
    			
    	System.setProperty("apple.awt.fileDialogForDirectories", "true"); 
    	FileDialog fd= new FileDialog(root, 
    			LanguageText.getMessage("commonHelpSets", 98),
    			FileDialog.LOAD);
    	
    	// Default directory is User's documents folder
    	String home = getHomeDirectory(null);
    	if (isSandboxed())
    	{
        	int start = home.indexOf("Library/Containers");
        	if (start>0)
        		home = home.substring(0, start);
    	}
    	fd.setDirectory(home + "Documents");
    	
    	fd.setFilenameFilter(filter); // Select only directories
    	fd.setVisible(true); 
    	
    	String selected = fd.getFile();
    	if (selected==null)
    		return null;
    	
    	File selectedFile = new File(fd.getDirectory() + selected);
    	String location = selectedFile.getAbsolutePath();
    	System.setProperty("apple.awt.fileDialogForDirectories", "false"); 
    	return location;
    }
    
    public static boolean resetBookmarkFolder()
    { 
    	Frame root = JOptionPane.getRootFrame();
    	String location = getBookmarkLocation();
    	if (location==null)
    	{
    		JOptionPane.showMessageDialog
    			(root, LanguageText.getMessage("acornsApplication", 45));
    		
    		return false;
    	}
    	
    	if (bookmarkLocation != null)
    	{
     		SecurityScopedBookmarks.stopResourceAccessingImpl(bookmark);
    	}
    	
    	String home = getHomeDirectory();
    	File file = new File(home, "bookmark");
    	file.delete();
    	bookmarkLocation = null;
    	bookmark = "";
    	String msg = LanguageText.getMessage("acornsApplication", 164);
    	
    	if (location.length()==0)
    	{
          	JOptionPane.showMessageDialog(root, msg);
          	bookmarkOk = false;
          	return false;
    	}
    	
    	boolean success = setBookmarkFolder(location);
    	if (success)
    	{
    		return true;    		
    	}

    	
       	JOptionPane.showMessageDialog(root, msg);
       	{
	       	return false;
       	}
    }

    /** Set the sandbox base directory */
    public static boolean setBookmarkFolder()
    {
    	Frame root = JOptionPane.getRootFrame();
    	boolean success = setBookmarkFolder(null);
    	String msg = LanguageText.getMessage("acornsApplication", 164);

    	if (!success)
       		JOptionPane.showMessageDialog(root, msg);
       		return false;
    }
    
    /** Set the sandbox base directory
     * 
     * @param location The location of the selected folder (or null)
     * @return
     */
    public static boolean setBookmarkFolder(String bookmarkPath)
    {
    	if (!isSandboxed())
    		return true;
    	
    	bookmarkOk = false;
    	String home = getHomeDirectory();
    	File file = new File(home, "bookmark");
    	try
    	{
    		if (file.exists())
    		{
    			Scanner scanner = new Scanner(file);
    			if (scanner.hasNextLine()) 
    			{
    			   bookmark = scanner.nextLine();
    			}
	    		else
				{
				    scanner.close();
	    			return resetBookmarkFolder();
				}
			    scanner.close();
			}
			else
			{
				if (bookmarkPath == null)
					bookmarkPath = getBookmarkLocation();
    			
				if (bookmarkPath == null || bookmarkPath.length()==0)
    			{
    				return false;
    			}
				
	            bookmark = SecurityScopedBookmarks.createBookmarkImpl(bookmarkPath);
	            if (bookmark == null)
	            {
	            	return false;
	            }
	
	            file.createNewFile();
			
	    		try (PrintWriter out = new PrintWriter(file))
	    		{
	        		out.println(bookmark);
	    		}
	    		catch (Exception e) 
	    		{
	            	return false;
	    		}
			}
    		
			bookmarkLocation = SecurityScopedBookmarks.startResourceAccessingImpl(bookmark);
			if (bookmarkLocation.startsWith("file://"))
				bookmarkLocation = bookmarkLocation.substring(7);
	   	}
		catch(Throwable t)
		{
			return false;
		}    	

    	bookmarkOk = true;
    	return true;
    }
    
    /** Get the default folder where user data is to be stored */
    public static String getDataFolder()
    {
        String separator = System.getProperty("file.separator");
    	String defaultPath = "";
    	
    	if (bookmarkLocation!=null)
    	{
    		defaultPath = bookmarkLocation;
    	}
    	else
    	{
	        String os = System.getProperty("os.name").toLowerCase();
	    	String home = getHomeDirectory(null);
	        if (os.indexOf("win")<0)
	        {
	        	String userName = System.getProperty("user.name");
	        	home = separator + "Users" + separator + userName;
	        }
	        
	        String dirName = home + separator + bookmarkFolder + separator;
	
	        // Use the acornsFiles directory if it exists. Otherwise use Acorns
	        defaultPath = dirName +  "acornsFiles";
	        String defaultPath2 = dirName +  "Acorns";
	
	        File pathFile = new File(defaultPath);
	        File pathFile2 = new File(defaultPath2);
	        if (!pathFile.exists())  
	        {
	      	   pathFile2.mkdir();  // no harm if already exists
	      	   defaultPath = defaultPath2;
	        }
    	}
        
        String keyDefaultPath = defaultPath +  separator + "keyLayoutFiles";
        File pathFile = new File(keyDefaultPath);
        if (!pathFile.exists())  
        {
        	pathFile.mkdir(); 
        }

        String fontDefaultPath = defaultPath + separator + "Fonts";
        pathFile = new File(fontDefaultPath);
        if (!pathFile.exists())  
        {
        	pathFile.mkdir(); 
        }
        
        return defaultPath;
    }
    
    /** Get the home directory */
    public static String getHomeDirectory()
    {
    	return getHomeDirectory("acorns");
    }
    
    /** If sand boxed on MAC OS systems, path must be within the sandboxed folder */
    public static boolean isValidForSandbox(String path)
    {
 	   if (SoundDefaults.isSandboxed())
 	   {
 		   if (SoundDefaults.isBookmarkOk())
 		   {
 			   if (bookmarkLocation != null)
 			   {
 				   return path.contains(bookmarkLocation);
 			   }
 			   
 			   return isParent(bookmarkFolder, path);
 		   }
 		   return false;
 	   }
 	   return true;
    }

    /**
     * 
     * @param possibleParent possible parent of class
     * @param path Full path to file
     * @return true if yes
     */
    private static boolean isParent(String possibleParent, String path)
    {
 	   File child = new File(path);
 	   File parent = child.getParentFile();
 	   while ( parent != null ) {
 	     if ( parent.getName().equals( possibleParent ) )
 	       return true;
 	     parent = parent.getParentFile();
 	   }
 	   return false;
    }


    
    /** Get the home directory sub-directory */
    public static String getHomeDirectory(String subDirectoryName)
    { 
         String os = System.getProperty("os.name").toLowerCase();
         String homeDir = null;
      	 
         if (os.indexOf("windows")>=0)
      	     homeDir = System.getenv("USERPROFILE");
         if (homeDir ==null) homeDir = System.getProperty("user.home");

         if (subDirectoryName == null) return homeDir;
         
         File homeFile = new File(homeDir);
         if (!homeFile.exists()) homeFile.mkdir();
         
         String separator = System.getProperty("file.separator");
         homeDir += separator + subDirectoryName;
         homeFile = new File(homeDir);
         if (!homeFile.exists()) homeFile.mkdir();
             
         return homeDir;
     }

    /** Method to create an AudioFormat object for recording sounds. 
     * 
     * @param record true if recording, false if for input format
     * @return AudioFormat object
     */
    public static AudioFormat createAudioFormat(boolean record)
    {   float audioRate = frameRate, audioFrameRate = frameRate;
        if (record)  audioRate = audioFrameRate = recordRate;
       
        return new AudioFormat
               (encoding, audioRate, bits, channels, frameBytes, audioFrameRate, bigEndian);
    }
    
    
}  // End of SoundDefaults class
