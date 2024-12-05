/**
 * TimeDomain.java - class to set convert to and from audio to time domain.
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.audio;

import javax.sound.sampled.*;
import java.awt.*;

import org.acorns.data.*;
import java.io.*;

public class TimeDomain 
{
   public static final int RANGE = 32767;
   public static final double EPSELON = 0.00001;
	
   SoundData sound;  // The sound object that this time domain corresponds to.
   
   // The following instance variables are set by the setAudioParameters() method
   int frameSizeInBytes, sampleSizeInBytes, channels, sampleSizeInBits;
   boolean signed, unsigned, alaw, ulaw, bigEndian;
   AudioFormat.Encoding encoding;
   
   // This value is used to maintain the maximum time domain value
   double   maxAmplitude = 0;  
        
   public TimeDomain(SoundData sound)  
   {  this.sound = sound;  
   }

   /** Extract Complex time domain from the signal
    *  @param point starting and ending points in the audio signal
    *  @param timeDomain double array to store data (null to define in method)
    *  @return Integer array of amplitude values or null
    */
   public double[] getComplexTimeDomain(Point point, double[] timeDomain)
   {
      if (point==null) point = new Point(-1,-1);
      return getComplexTimeDomain(point.x, point.y, timeDomain);
   }

   /** Extract Complex time domain from the signal
    *  @param startSpot beginning point in the audio signal
    *  @param endSpot   ending point in the audio signal
    *  @param timeDomain double array to store data (null to define in method)
    *  @return Integer array of amplitude values or null
    */
   public double[] getComplexTimeDomain
           (int startSpot, int endSpot, double[] timeDomain)
   {
       return getTimeDomainFromAudio(startSpot, endSpot, timeDomain, true);
   }

   /** Extract Complex time domain from the signal
    *  @param startSpot beginning point in the audio signal
    *  @param endSpot   ending point in the audio signal
    *  @return Integer array of amplitude values or null
    */
   public double[] getComplexTimeDomain(int startSpot, int endSpot)
   {
       return getTimeDomainFromAudio(startSpot, endSpot, null, true);
   }
   
   public double[] getTimeDomainFromAudio(int startSpot, int endSpot)
   {
       double[] timeDomain = getTimeDomainFromAudio(startSpot, endSpot, null, false);
       return timeDomain;
   }
    
   /** Extract Sound data from the audio signal
    *  @return Integer array of amplitude values or null.
    */
   public double[] getTimeDomainFromAudio()  
   { 
       double[] timeDomain = getTimeDomainFromAudio(-1, -1, null, false);
       return timeDomain;
   }
   
   /** Get maximum amplitude over the whole sound wave.
    *  @return maximum absolute amplitude.
    */
   public double getMaximumAmplitude() 
   {   return maxAmplitude;  }

   /** Method to delete some audio from the sound wave.
    * 
    * @param range the start (range.x) and end (range.y) of audio to remove
    * @return true if success, false otherwise
    */
   public boolean deleteAudio(Point range)
   { 
      byte[] audioBytes = sound.getAudioData();
      int startAudio = range.x * frameSizeInBytes;
      int endAudio = range.y * frameSizeInBytes;

      byte[] newAudio = new byte[audioBytes.length-(endAudio - startAudio)];
      System.arraycopy(audioBytes, 0, newAudio, 0, startAudio);
      int back = audioBytes.length - endAudio;
      for (int c=0; c<back; c++) newAudio[startAudio+c] = audioBytes[endAudio+c];

      // Update the pcm data in the sound object.
      return sound.setAudioData(newAudio);
   }
   
   /** Replace range of points with new audio clip
    * 
    * @param clip byte array of the clip
    * @param range range of audio to replace
    * @return true if successful
    */
   public boolean insertAudio(double[] clip, Point range)
   {
      byte[] audioBytes = sound.getAudioData();
      int clipSize = clip.length * frameSizeInBytes;
      int replaceSize = (range.y - range.x) * frameSizeInBytes;
      int newSize = audioBytes.length + clipSize - replaceSize;
      byte[] oldAudio = audioBytes;
      audioBytes = new byte[newSize];

      // Copy stuff before clip.
      int startAudio = range.x * frameSizeInBytes;
      System.arraycopy(oldAudio, 0, audioBytes, 0, startAudio);

      // Copy clip
      double max = saveTimeDomainClipIntoAudio(audioBytes, clip, range.x);
      if (max<0) return false;
      if (max>maxAmplitude) maxAmplitude = max;

      // Copy stuff beyond clip
      int fromBack = startAudio + replaceSize;
      int back = oldAudio.length - fromBack;
      int toBack = startAudio + clipSize;
      for (int c=0; c<back; c++) audioBytes[toBack+c] = oldAudio[fromBack+c];

      // Update the pcm data in the sound object.
      return sound.setAudioData(audioBytes);
   }
   
    /** Put Time domain sound data back into the Sound File
    *  @param timeDomain time array of time domain amplitude values
    *  @return false if operation fails
    */
   public boolean saveTimeDomainIntoAudio(double[] timeDomain)
   {
      maxAmplitude = 0;
  
      // Don't process if a record or play operation is happening.
      if (sound.isActive())  return false;
      
     setAudioParameters();
     int size = timeDomain.length * frameSizeInBytes;
     byte[] audioBytes  = new byte[size];
           
      double max = saveTimeDomainClipIntoAudio(audioBytes, timeDomain, 0);
      if (max<0) return false;
      
      // Update the current audio bytes, and force a conversion.
      maxAmplitude = max;
      return sound.setAudioData(audioBytes);    
   }

   // This method sets the encoding after a file load operation.
   private void setAudioEncoding()
   {
       if (signed) encoding = AudioFormat.Encoding.PCM_SIGNED;
       else if (unsigned) encoding = AudioFormat.Encoding.PCM_UNSIGNED;
       else if (alaw) encoding = AudioFormat.Encoding.ALAW;
       else if (ulaw) encoding = AudioFormat.Encoding.ULAW;
       
       if (encoding==null)   
       { 
           if (sampleSizeInBits>8) 
           {
               signed = true; 
               encoding = AudioFormat.Encoding.PCM_SIGNED;
           }
           else
           {
               signed = false; 
               encoding = AudioFormat.Encoding.PCM_UNSIGNED;
               
           }
       }
   }
   

   /************************  Private Methods *************************/
   /** Set audio parameters to the current values in the sound object */    
   private void setAudioParameters()
   {
      AudioFormat format = sound.getAudioFormat();
      sampleSizeInBits   = format.getSampleSizeInBits();
      sampleSizeInBytes  = sampleSizeInBits >>3;
      frameSizeInBytes   = format.getFrameSize();
      channels           = format.getChannels();
      encoding           = format.getEncoding();
      bigEndian          = format.isBigEndian();
      
      if (encoding==null) setAudioEncoding();
      signed           = encoding.equals(AudioFormat.Encoding.PCM_SIGNED);
      unsigned         = encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
      alaw             = encoding.equals(AudioFormat.Encoding.ALAW);
      ulaw             = encoding.equals(AudioFormat.Encoding.ULAW);
   }
      
   /** Extract Sound data from the audio signal
    *  @param startSpot beginning point in the audio signal
    *  @param endSpot   ending point in the audio signal
    *  @param timeDomain array to store extracted signal values
    *  @return Integer array of amplitude values or null
    */
   private double[] getTimeDomainFromAudio
       (int startSpot, int endSpot, double[] timeDomain, boolean complexDomain)
                     
   {   
       boolean computeMax = false;
       if (startSpot==-1 && endSpot==-1) 
       {
           computeMax = true;  // If getting entire domain compute max amplitude.
           maxAmplitude = 0;
       }
       
       if (sound.getAudioData()==null)   return null;
       
       setAudioParameters();
       if (frameSizeInBytes==0) return null;
       
       int frames = sound.getFrames();
       if (startSpot<0) startSpot = 0;
       if (endSpot<0 || endSpot>frames) endSpot = frames;
       if (endSpot<startSpot) startSpot = endSpot;

       int numChannels = channels;
       
       if (numChannels>1 && 
               SoundDefaults.getChannelOption() == SoundDefaults.SELECTCHANNEL)
           numChannels = 1;
       
      int index = 0, increment, N = endSpot-startSpot;
      if (complexDomain)
      {
         if (timeDomain==null || timeDomain.length<2*N)
             timeDomain = new double[2 * N];
         increment = 2;              
      }
      else
      {  
         if (timeDomain==null) timeDomain = new double[N];
         increment = 1;
      }

      double value;
      for (int f=startSpot; f<endSpot; f++) 
      {  try
         {
             timeDomain[index] = getTimeValue(f, numChannels);
         }
         catch (Exception e) {return null;}
         
         if (unsigned)
         {
             switch (sampleSizeInBytes)
             {
                 case 1:  timeDomain[index] -=128;
                          break;
                 case 2:  timeDomain[index] -= 32768;
                          break;
                 case 3:  timeDomain[index] -= 1>>23;
                          break;
                 case 4:  timeDomain[index] -= 1>>31;
                          break;
             }
         }

         // Compute maximum amplitude if appropriate.
         if (computeMax)
         {
            value = timeDomain[index];
            if (value < 0)   value = -value;
            if (value > maxAmplitude) maxAmplitude = value;
         }
         index += increment;
      }          
      return timeDomain; 
   }

   /** Put data into sound file 
    *   @param pcm audio array to insert into
    *   @param timeDomain clip to insert
    *   @param time domain offset to store clip
    */
   private double saveTimeDomainClipIntoAudio(byte[] audio, double[] clip, int offset)
   {
      double value;
      double max = 0;
      
      setAudioParameters();
 
      // Don't process if a record or play operation is happening.
      if (frameSizeInBytes==0) return -1;

      for (int f=0; f<clip.length; f++)
      {   value = clip[f]; // Convert back from signed to unsigned values
          if (value < 0)   value = -value;  
          if (value > max) max = value;   // Update maximum amplidude.
     
          value = clip[f];
          if (unsigned)
          {
             switch (sampleSizeInBytes)
             {
                 case 1:  value +=128;
                          break;
                 case 2:  value += 32768;
                          break;
                 case 3:  value += 1>>23;
                          break;
                 case 4:  value += 1>>31;
                          break;
             }
         }
   
         if (!setAudioValue(audio, f+offset, (int)value)) return -1;
      }
      return max;
   }
    
    // Refer to John C. Bellamy Telphony, 1982, John Wiley, pps 98-111 and 472-476
   //   for a distription of the U-law and A-law algorithms for digital companding.
   //   These algorithms that represent sixteen bit digital audio signals 
   //   in eight bits with minimal loss.  U-law is used int the US and A-law
   //   is used in the EU. The following tables take an 8-bit value and map 
   //   them into a sixteen bit integer.
   final int[] ulawTable =
   {
    -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956, //00-07
    -23932, -22908, -21884, -20860, -19836, -18812, -17788, -16764, //08-0f
    -15996, -15484, -14972, -14460, -13948, -13436, -12924, -12412, //10-17
    -11900, -11388, -10876, -10364,  -9852,  -9340,  -8828,  -8316, //18-1f
     -7932,  -7676,  -7420,  -7164,  -6908,  -6652,  -6396,  -6140, //20-27
     -5884,  -5628,  -5372,  -5116,  -4860,  -4604,  -4348,  -4092, //28-2f
     -3900,  -3772,  -3644,  -3516,  -3388,  -3260,  -3132,  -3004, //30-37
     -2876,  -2748,  -2620,  -2492,  -2364,  -2236,  -2108,  -1980, //38-3f
     -1884,  -1820,  -1756,  -1692,  -1628,  -1564,  -1500,  -1436, //40-47
     -1372,  -1308,  -1244,  -1180,  -1116,  -1052,   -988,   -924, //48-4f
      -876,   -844,   -812,   -780,   -748,   -716,   -684,   -652, //50-57
      -620,   -588,   -556,   -524,   -492,   -460,   -428,   -396, //58-5f
      -372,   -356,   -340,   -324,   -308,   -292,   -276,   -260, //60-67
      -244,   -228,   -212,   -196,   -180,   -164,   -148,   -132, //68-6f
      -120,   -112,   -104,    -96,    -88,    -80,    -72,    -64, //70-77
       -56,    -48,    -40,    -32,    -24,    -16,     -8,      0, //78-7f
     32124,  31100,  30076,  29052,  28028,  27004,  25980,  24956, //80-87
     23932,  22908,  21884,  20860,  19836,  18812,  17788,  16764, //88-8f
     15996,  15484,  14972,  14460,  13948,  13436,  12924,  12412, //90-97
     11900,  11388,  10876,  10364,   9852,   9340,   8828,   8316, //98-9f
      7932,   7676,   7420,   7164,   6908,   6652,   6396,   6140, //a0-a7
      5884,   5628,   5372,   5116,   4860,   4604,   4348,   4092, //a8-af
      3900,   3772,   3644,   3516,   3388,   3260,   3132,   3004, //b0-b7
      2876,   2748,   2620,   2492,   2364,   2236,   2108,   1980, //b8-bf
      1884,   1820,   1756,   1692,   1628,   1564,   1500,   1436, //c0-c7
      1372,   1308,   1244,   1180,   1116,   1052,    988,    924, //c8-cf
       876,    844,    812,    780,    748,    716,    684,    652, //d0-d7
       620,    588,    556,    524,    492,    460,    428,    396, //d8-df
       372,    356,    340,    324,    308,    292,    276,    260, //e0-e7
       244,    228,    212,    196,    180,    164,    148,    132, //e8-ef
       120,    112,    104,     96,     88,     80,     72,     64, //f0-7f
        56,     48,     40,     32,     24,     16,      8,      0  //f8-ff
   };
   
   final int[] alawTable = 
   {
      -5504,  -5248,  -6016,  -5760,  -4480,  -4224,  -4992,  -4736, //00-07
      -7552,  -7296,  -8064,  -7808,  -6528,  -6272,  -7040,  -6784, //08-0f
      -2752,  -2624,  -3008,  -2880,  -2240,  -2112,  -2496,  -2368, //10-17
      -3776,  -3648,  -4032,  -3904,  -3264,  -3136,  -3520,  -3392, //18-1f
     -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944, //20-27
     -30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136, //28-2f
     -11008, -10496, -12032, -11520,  -8960,  -8448,  -9984,  -9472, //30-37
     -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568, //38-3f
       -344,   -328,   -376,   -360,   -280,   -264,   -312,   -296, //40-47
       -472,   -456,   -504,   -488,   -408,   -392,   -440,   -424, //48-4f
        -88,    -72,   -120,   -104,    -24,     -8,    -56,    -40, //50-57
       -216,   -200,   -248,   -232,   -152,   -136,   -184,   -168, //58-5f
      -1376,  -1312,  -1504,  -1440,   -1120, -1056,  -1248,  -1184, //60-67
      -1888,  -1824,  -2016,  -1952,   -1632, -1568,  -1760,  -1696, //68-6f
       -688,   -656,   -752,   -720,    -560,  -528,   -624,   -592, //70-77
       -944,   -912,  -1008,   -976,    -816,  -784,   -880,   -848, //78-7f
       5504,   5248,   6016,   5760,    4480,  4224,   4992,   4736, //80-87
       7552,   7296,   8064,   7808,    6528,  6272,   7040,   6784, //88-8f
       2752,   2624,   3008,   2880,    2240,  2112,   2496,   2368, //90-97
       3776,   3648,   4032,   3904,    3264,  3136,   3520,   3392, //98-9f
      22016,  20992,  24064,  23040,   17920, 16896,  19968,  18944, //a0-a7
      30208,  29184,  32256,  31232,   26112, 25088,  28160,  27136, //a8-af
      11008,  10496,  12032,  11520,    8960,  8448,   9984,   9472, //b0-b7
      15104,  14592,  16128,  15616,   13056, 12544,  14080,  13568, //b8-bf
        344,    328,    376,    360,     280,   264,    312,    296, //c0-c7
        472,    456,    504,    488,     408,   392,    440,    424, //c8-cf
         88,     72,    120,    104,      24,     8,     56,     40, //d0-d7
        216,    200,    248,    232,     152,   136,    184,    168, //d8-df
       1376,   1312,   1504,   1440,    1120,  1056,   1248,   1184, //e0-e7
       1888,   1824,   2016,   1952,    1632,  1568,   1760,   1696, //e8-ef
        688,    656,    752,    720,     560,   528,    624,    592, //f0-f7
        944,    912,   1008,    976,     816,   784,    880,    848  //f8-ff
   };
   
   /** Get time value from frame in audio file.
    * 
    * @param frame desired frame to retrieve
    * @param channels number of channels in this signal
    * @return array of time values
    * @throws java.io.IOException
    */
   private double getTimeValue(int frame, int channels)  throws IOException
   {
       int value = 0;                  // Accumulated sum of all channels.
       int startSample, endSample, increment, byteValue, shift;
       byte[] audio = sound.getAudioData();
       if (audio==null) throw new IOException();
       
       if (channels == 0)  { throw new NumberFormatException(); }
       int startOffset = frameSizeInBytes * frame;
       int endOffset = startOffset + frameSizeInBytes;
       for (int offset=startOffset; offset<endOffset; offset+=sampleSizeInBytes)
       {
           if (alaw)      {  value += alawTable[audio[offset]];  }
           else if (ulaw) {  value += ulawTable[audio[offset]];  }       
           else if (signed || unsigned)
           {
              if (bigEndian) 
              {  startSample = offset;
                 endSample   = offset + sampleSizeInBytes;
                 increment   = +1;
              }
              else
              {
                 startSample = offset + sampleSizeInBytes - 1;
                 endSample   = offset - 1;
                 increment   = -1;
              }
           
              byteValue = 0;
              shift = sampleSizeInBits - 8;
              for (int byteNo=startSample; 
                       byteNo!=endSample; byteNo+=increment)
              {
                 if (signed && byteNo==startSample)
                 {  byteValue |= audio[byteNo] << shift;
                 }
                 else byteValue |= (0xff & audio[byteNo]) << shift;
                 shift -= 8;
              }
              value += byteValue;
           }
           else throw new IOException();
       }
       value /= channels;  // Average the number of channels.
       return (double)value;
   }

   // Table of exponents for computing the compressed ulaw and alaw byte values.
   final byte[] ulawExpTable = 
   {
       0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,
       4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
       5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
       5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
       6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
       6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
       6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
       6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
       7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,               
   };
   
   final short[] alawExpTable 
           = {0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF};
   
   /** Store time value back into sound data.
    * 
    * @param audio audio pcm byte array to store into
    * @param frame desired frame to store
    * @param value value of the frame to store
    * @return true if success, false otherwise
    */
   private boolean setAudioValue(byte[] audio, int frame, int value)
   {
       int     mantissa, sign = 0, exponent = -1;
       byte[]  audioValue = new byte[4];
       
       // Convert to time value to audio sample.
       if (alaw)
       {
           if (value<0) {sign = 0x55; value = -value - 8;}
           else         {sign = 0xD5;}
           
           while (++exponent<8 && value>ulawExpTable[exponent]) {}
           
           if (exponent==8)      { exponent=7; mantissa = 0x0F; }
           else if (exponent<2)  { mantissa = (value >> 4) & 0x0F; }
           else                  { mantissa = (value >> (exponent+3)) & 0x0F; }
           
           audioValue[0] = (byte)((((exponent << 4) | mantissa) ^ sign) & 0xFF);
       }
       else if (ulaw)      
       {   
           if (value<0) {sign = 0x80; value = -value; }
           if (value>32635) value = 32635;
           value += 0x84;
            
           exponent = ulawExpTable[value>>7];
           mantissa = (value >> (exponent+3)) & 0x0F;
           audioValue[0] = (byte)(~(sign | (exponent<<4 | mantissa))&0xFF);
           if (audioValue[0]==0) audioValue[0] = 0x02;
       }
       else if (signed || unsigned)
       {
          int startOffset, endOffset, increment;
          if (bigEndian) 
          {  
             startOffset = sampleSizeInBytes - 1;
             endOffset   = - 1;
             increment   = -1;
          }
          else
          {
             startOffset = 0;
             endOffset   = sampleSizeInBytes;
             increment   = +1;
          }
          
          for (int offset=startOffset; offset!=endOffset; offset+=increment)
          {
             audioValue[offset] = (byte)(value & 0xFF);
             value >>= 8;
          }
       }
       else return false;
          
       // Store data into  the sound data array.
       int framePtr = frameSizeInBytes * frame;
       for (int channel = 0; channel<channels; channel++)
       {
           for (int byteOffset=0; byteOffset<sampleSizeInBytes; byteOffset++)
           {  audio[framePtr++] = audioValue[byteOffset]; }
       }
       return true;
   }
   
   /** Method to remove the DC component from an audio signal
    * 
    * @param clip Audio signal to remove clip
    */
   public static void removeDC(double[] clip) 
   {
      if (clip==null) return;
       
      double sum = 0;
      for (int i=0; i<clip.length; i++)
      {
          sum+= clip[i];
      }
      
      if (Math.abs(sum)<.005) return;
        
      int average = (int)Math.round(sum / clip.length);
      if (average == 0) return;
      for (int i=0; i<clip.length; i++)
      {
          clip[i] -= average;
      }
   }
   
   /** Method to compute the number of zero crossings in a signal window
    * 
    * @param clip The audio signal
    * @param wStart The start of the  signal window
    * @param wEnd The end of the signal window
    * @param inc increment by one (real), or two (complex)
    */
   public static int zeroCrossings(double[] clip, int wStart, int wEnd, int inc)
   {
        int crossings = 0;
        for (int i=wStart; i<wEnd; i+=inc)
        {
            if (i<wStart+inc) continue;
            if (clip[i]*clip[i-inc]<0) crossings++;
        }
        return crossings;    
   } 
   
   /** Method to compute the energy of a portion of a signal
    * 
    * @param clip The audio signal
    * @param wStart The start of the  signal window
    * @param wEnd The end of the signal window
    * @param inc increment by one (real), or two (complex)
    */
   public static int energy(double[] clip, int wStart, int wEnd, int inc)
   {
       int total = 0;
       for (int i=wStart; i<wEnd; i+=inc)
       {
           if (i<wStart+inc) continue;
           total += clip[i]*clip[i];
       }
       return total;    
   }
   
   /** Method to compute the average Decibel level of a frame
    * 
    * @param clip The audio signal
    * @param wStart The start of the  signal window
    * @param wEnd The end of the signal window
    * @param inc increment by one (real), or two (complex)
    */
   
   public static double decibelLevel(double[] clip, int wStart, int wEnd, int inc)
   {
	 double total = 0;
	 for (int i=wStart; i<wEnd; i+=inc)
	 {
		 total += clip[i]*clip[i];
	 }
	 
	 int length = (wEnd - wStart)/inc;
		
	 if (total>EPSELON)
     {
		 return 10 * Math.log10(total/length);
		 
     }
	 else total = EPSELON;
	 
	 return total;
   }
}
