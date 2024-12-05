/*
 * SoundIO.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.audio;

import javax.sound.sampled.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.acorns.data.SoundData;
import org.xiph.speex.*;
import org.xiph.speex.spi.*;
import org.xiph.libvorbis.*;
import org.xiph.libogg.*;

public class SoundIO 
{
    /*  Parameters for SPEEX encoding */
    private final static int QUALITY    = 8;      // Between 1 and 10; higher better
    private final static int COMPLEXITY = 3;      // Between 1 and 10; 10 needs 5x time
    private final static boolean VBR    = false;  // Variable bit rate
    private final static boolean VAD    = false;  // Voice activity detection
    private final static boolean DTX    = false;  // Allow discontinuous transmission

    private final static int BUFFER = 1024;       // Size of buffer for ogg writing
    /* Parameters for GSM encoding */
    
    private AudioFormat format;      // Audio format for the file to be read.
    private byte[]      audioBytes;   // The audio data that was read.
    
    /** Common constructor code for inputting URL or File based audio
     * 
     * @param input AudioInputStream object
     * @param extension file or url extension
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public SoundIO(AudioInputStream input, String extension)
            throws IOException, UnsupportedAudioFileException
    {
        format = input.getFormat();
        if (extension.equalsIgnoreCase("mp2") || extension.equalsIgnoreCase("mp3")
                || extension.equalsIgnoreCase("ogg") 
                || extension.toLowerCase().startsWith("aif")
                || extension.equalsIgnoreCase("gsm")
                || extension.equalsIgnoreCase("spx"))
           input = convertAudioFormat
                    (input, SoundDefaults.createAudioFormat(false));
        audioBytes = readSound(input); 
    }

    /** Read sound file using a URL
     * 
     * @param url object containing network address
     * @param extension file extension
     * @throws java.io.IOException
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     */
    public SoundIO(URL url, String extension) 
            throws IOException, UnsupportedAudioFileException
    {
        this(AudioSystem.getAudioInputStream(url), extension);
    }

    /** Read a compressed audio file directly
     * 
     * @param stream Input Stream of audio data
     */
    public SoundIO(InputStream stream)
    		throws IOException
    {
	      ByteArrayOutputStream out = new ByteArrayOutputStream();
	      byte[] buffer = new byte[1024];
	      int noOfBytes = 0;
	      while( (noOfBytes = stream.read(buffer)) != -1 )
	      {
	         out.write(buffer, 0, noOfBytes);
	      }
	      stream.close();
		  audioBytes = out.toByteArray();
    }
    
    /** Convert form PCX_FLOAT to PCS_SIGNED
     * 
     * @param extension
     * @throws IOException
     */
    private void convert_PCX_FLOAT_To_PCX_SIGNED()
    					throws IOException
    {
        if (format==null) return;
    	if (!format.getEncoding().equals(AudioFormat.Encoding.PCM_FLOAT)) return;
    	
    	
        int frames = audioBytes.length / getFrameSize(format);
        AudioInputStream audio = new AudioInputStream
             (new ByteArrayInputStream(audioBytes), format, frames);
        
        AudioFormat[] encodings = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, format);
        for (int i=0; i<encodings.length; i++)
        {
        	if (encodings[i].getSampleSizeInBits() != 16)
        			continue;

        	AudioFormat targetFormat = new AudioFormat
    			(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), encodings[i].getSampleSizeInBits(), 
    					encodings[i].getChannels(), getFrameSize(encodings[i]), -1, format.isBigEndian());
    	
        	AudioInputStream input = AudioSystem.getAudioInputStream(targetFormat, audio); 
        	format = targetFormat;
        
        	ByteArrayOutputStream output = new ByteArrayOutputStream();
        	AudioSystem.write(input, AudioFileFormat.Type.WAVE, output);        
        	audioBytes =  output.toByteArray();
        	audio.close();
        	input.close();
        	return;
        }
        audio.close();
    }
    
    
    /** Method to determine the loading frame rate
     * 
     * @param file Audio file
     * @return samples per second
     */
    public static int getSoundSampleRate(File file) 
            throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream input = AudioSystem.getAudioInputStream(file);
        AudioFormat format = input.getFormat();
        input.close();
        return (int)format.getSampleRate();
    }
    
    /** Method to determine the loading frame rate
     * 
     * @param url Audio file
     * @return samples per second
     */
    public static int getSoundSampleRate(URL url) 
            throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream input = AudioSystem.getAudioInputStream(url);
        AudioFormat format = input.getFormat();
        input.close();
        return (int)format.getSampleRate();
    }
    
    /** Read audio file from byte array
     *  
     * @param audioBytes The byte input array
     * @param sampleRate The sample rate for this audio signal
     * @throws java.io.IOException
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     */
    public SoundIO(byte[] audioBytes, float sampleRate)
                             throws IOException, UnsupportedAudioFileException
    {
         ByteArrayInputStream stream = new ByteArrayInputStream(audioBytes);
         AudioInputStream input
                 = AudioSystem.getAudioInputStream(stream);
         format = input.getFormat();
         
         // Needed for upwards compatibility with previous ACORNS versions
         // Some recordings look like MPEG, but are not because they are
         // a raw byte stream.
         if (format.toString().startsWith("MPEG")) 
                throw new UnsupportedAudioFileException();

         AudioFormat audioFormat = new AudioFormat(
                SoundDefaults.getEncoding(),
                sampleRate,
                SoundDefaults.getBits(),
                SoundDefaults.getChannels(),
                SoundDefaults.getFrameBytes(),
                sampleRate,
                SoundDefaults.getBigEndian());

         input = convertAudioFormat(input, audioFormat);
         audioBytes = readSound(input);
    }
    
    /** Preparation to write the audio file from the byte array
     * 
     */
    public SoundIO(byte[] audioData, AudioFormat format) 
    {
        this.audioBytes = audioData;
        this.format    = format;        
    }
    
    /** get audio byte array after reading
     * 
     * @return byte array of audio data
     */
    public byte[] getAudioFileData()  throws IOException
    { 
    	convert_PCX_FLOAT_To_PCX_SIGNED();
    	return audioBytes; 
    }
    
    /** Get audio format of input stream
     * 
     * @return AudioFormat object
     */
    public AudioFormat getFormat() { return format; }
    
    /** Convert audio from one audio format to another
     * 
     * @param stream audio input stream
     * @param desired desired audio format
     * @return converted audio stream
     */
    public static AudioInputStream convertAudioFormat
            (AudioInputStream stream, AudioFormat desired)
    {
    	AudioInputStream target;
        AudioFormat audioFormat = stream.getFormat();
        DataLine.Info info = new DataLine.Info
                (SourceDataLine.class, audioFormat, SoundDefaults.getBufferSize());
        
        if (AudioSystem.isLineSupported(info) 
                && compareAudioFormat(audioFormat, desired)) return stream;
        
        AudioFormat targetFormat = stream.getFormat(); 
        
        // Must convert if not supported.
        if (!AudioSystem.isLineSupported(info))
        {
            targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                audioFormat.getSampleRate(),
                16,
                audioFormat.getChannels(),
                audioFormat.getChannels() * 2,
                audioFormat.getSampleRate(),
                false);
        
            stream = AudioSystem.getAudioInputStream(targetFormat, stream);   
        }
        else if (desired.getEncoding() != audioFormat.getEncoding())
        {
            targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                audioFormat.getSampleRate(),
                16, //audioFormat.getSampleSizeInBits(),
                audioFormat.getChannels(),
                audioFormat.getChannels()*2, //audioFormat.getFrameSize(),
                audioFormat.getSampleRate(),
                false);
        
            target = AudioSystem.getAudioInputStream(targetFormat, stream);  
            stream = target;
            
        }
      
        if ( Math.abs(targetFormat.getFrameRate()- desired.getFrameRate())>0.0001F ||
                       targetFormat.getSampleSizeInBits()!=desired.getSampleSizeInBits() ||
                       targetFormat.isBigEndian() != desired.isBigEndian())
        {
           targetFormat = new AudioFormat(
                desired.getEncoding(),
                desired.getSampleRate(),
                desired.getSampleSizeInBits(),
                targetFormat.getChannels(),
                getFrameSize(desired),
                desired.getFrameRate(),
                desired.isBigEndian());            

           target = AudioSystem.getAudioInputStream(targetFormat, stream);  
           stream = target;
        } 
        
        if (targetFormat.getChannels()!=desired.getChannels())
        {
           targetFormat = new AudioFormat(
               targetFormat.getEncoding(),
               targetFormat.getSampleRate(),
               targetFormat.getSampleSizeInBits(),
               desired.getChannels(),
               getFrameSize(targetFormat),
               targetFormat.getSampleRate(),
               targetFormat.isBigEndian());
           
           target = AudioSystem.getAudioInputStream(targetFormat, stream);   
           stream = target;
        }  
        return stream;
    }

    /** Convert the source stream in an appropriate way for each type of encoding.
     * 
     * @param audio The source audio input stream
     * @param extension The desired encoding
     * @return The modified source audio input stream
     */
    private AudioInputStream convertForEncoding(AudioInputStream audio, String extension)
    {
        AudioFormat targetFormat;
        
        // GSM Encoding.
        if (extension.toLowerCase().equals("gsm"))
        {
          // GSM encoding requires a very specific format.
          targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            8000.0F, 16, 1, 2, 8000.0F, format.isBigEndian());
      
          audio = convertAudioFormat(audio, targetFormat);
          AudioFormat.Encoding targetEncoding = new AudioFormat.Encoding("GSM0610");
          audio = AudioSystem.getAudioInputStream(targetEncoding, audio);
        }
      
        // AIF and AIFC encoding.
        else if (extension.toLowerCase().startsWith("aif"))
        {
           // We don't need to, but convert to 8 bits per sample, mono.
           targetFormat = new AudioFormat(
           AudioFormat.Encoding.PCM_UNSIGNED,
           format.getSampleRate(), 8, 1, 1, format.getFrameRate(), format.isBigEndian());
           audio = convertAudioFormat(audio, targetFormat);           
        }
        // Au Encoding.
        else if (extension.toLowerCase().equals("au"))
        {
           // We will convert to standard 8000.0F, single channel, 8 bits per sample.
           targetFormat = new AudioFormat(
           AudioFormat.Encoding.PCM_UNSIGNED,
                  8000.0F, 8, 1, 1, 8000.0F, format.isBigEndian());
           audio = convertAudioFormat(audio, targetFormat);
        }
        else
        {
           targetFormat = SoundDefaults.createAudioFormat(false);
           audio = convertAudioFormat(audio, targetFormat);
        }      
        return audio;  // Return stream prepared for encoding.
        
    }
    
    /** Compare two audio formats to determine if differences.
     * 
     * @param format source format
     * @param compareToAudio target format
     * @return true if no differences, false otherwise
     */
    private static boolean compareAudioFormat(AudioFormat format, AudioFormat compareToAudio)
    {
        if (Math.abs(format.getFrameRate()- compareToAudio.getFrameRate())>0.0001F) return false;
        if (format.isBigEndian() != compareToAudio.isBigEndian()) return false;
        if (getFrameSize(format) != getFrameSize(compareToAudio)) return false;
        if (format.getSampleSizeInBits() != compareToAudio.getSampleSizeInBits()) return false;
        if (format.getEncoding() != compareToAudio.getEncoding()) return false;  
        if (format.getChannels() != compareToAudio.getChannels()) return false;
        return true;
    }

    /** Method to do file reading.
     * 
     * @param input input stream of file to read
     * @return byte array containing data for read sound file
     * @throws java.io.IOException
     * @throws javax.sound.sampled.UnsupportedAudioFileException
     * @throws java.lang.ArrayIndexOutOfBoundsException
     */
    private byte[] readSound(AudioInputStream input) 
                        throws IOException, UnsupportedAudioFileException, ArrayIndexOutOfBoundsException
    {
       // Verify that the input file is not longer than specified.
       format = input.getFormat();
       float maxFrames = SoundDefaults.getMaxMin()*60* format.getFrameRate();
       
       // Throw an exception if the file is too big.
       if (input.getFrameLength()>maxFrames)
       {  try {input.close();} catch (Exception e)  {}
          throw new ArrayIndexOutOfBoundsException();
       }
          
       // Use a ByteArray for output.
       ByteArrayOutputStream output = new ByteArrayOutputStream();
          
       // Create an input byte array.
       int nBytesRead=0;
       byte[] inputData = new byte[SoundDefaults.BUFFERSIZE];
       while (nBytesRead !=-1)
       {
           nBytesRead = input.read(inputData, 0, inputData.length);
           if (nBytesRead>=0) output.write(inputData, 0, nBytesRead);
       }
       try {input.close();} 
       catch (Exception e)  
       {
    	   System.out.println(e);
       }
       
       audioBytes = output.toByteArray();
       return audioBytes;
       
    }  // End of readIt();   

    /** Encode in OGG format */
    private void writeOgg
        (AudioInputStream stream, File file, AudioFileFormat.Type type)
                                                             throws IOException
    {   // First create stream of audio data in wave format
        // Converter requires two channels and 44100 bit rate
        AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED
                , 44100, 16, 2, 2, 44100, format.isBigEndian());
        stream = convertAudioFormat(stream, target);
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        
        AudioSystem.write(stream, type, outStream);
        outStream.flush();
        outStream.close();

        ByteArrayInputStream in
                            = new ByteArrayInputStream(outStream.toByteArray());
        boolean eos = false;
        vorbis_info vorbisInfo = new vorbis_info();
        vorbisenc encoder = new vorbisenc();
        if ( !encoder.vorbis_encode_init_vbr( vorbisInfo, target.getChannels()
                                          , (int)target.getFrameRate(), .3f ) )
        {  throw new IOException("Couldn't initialize vorbis encoder"); }

        vorbis_comment comment = new vorbis_comment();
        comment.vorbis_comment_add_tag("ENCODER", "ACORNS Vorbis Encoder");

        vorbis_dsp_state  state = new vorbis_dsp_state();
        if ( !state.vorbis_analysis_init( vorbisInfo ) )
        {  throw new IOException("Couldn't initialize vorbis working state"); }

        vorbis_block block = new vorbis_block( state );
        Random rand = new Random();
        ogg_stream_state streamState = new ogg_stream_state(rand.nextInt(256));

        ogg_packet header = new ogg_packet();
        ogg_packet comm = new ogg_packet();
        ogg_packet code = new ogg_packet();
        state.vorbis_analysis_headerout( comment, header, comm, code );

        streamState.ogg_stream_packetin( header); // placed in its own page
        streamState.ogg_stream_packetin( comm );
        streamState.ogg_stream_packetin( code );

        OutputStream out;
        ByteArrayOutputStream byteStream = null;
        if (file==null)
        	 out = byteStream = new ByteArrayOutputStream();
        else out = new FileOutputStream( file );
        ogg_page page = new ogg_page();
        ogg_packet data = new ogg_packet();

        while( true )
        {  if ( !streamState.ogg_stream_flush( page ) )    break;
           out.write( page.header, 0, page.header_len );
           out.write( page.body, 0, page.body_len );
        }

        byte[] bytes = new byte[BUFFER*4+44];
        int i, len, position;
        float[][] floats;

        while ( !eos )
        {  len = in.read( bytes, 0, BUFFER*4 ); // stereo hardwired here

           if ( len==0 ) { state.vorbis_analysis_wrote( 0 );  }
           else
           {  floats = state.vorbis_analysis_buffer( BUFFER );

              for ( i=0; i < len/4; i++ )
              {  position = state.pcm_current + i;
                 floats[0][position]
                     = ((bytes[i*4+1]<<8) | (0x00ff&(int)bytes[i*4]))/32768.f;
                 floats[1][position]
                     = ((bytes[i*4+3]<<8) | (0x00ff&(int)bytes[i*4+2]))/32768.f;
              }

              state.vorbis_analysis_wrote( i );
           }

           while ( block.vorbis_analysis_blockout( state ) )
           {  block.vorbis_analysis( null );
              block.vorbis_bitrate_addblock();

              while ( state.vorbis_bitrate_flushpacket( data ) )
              {  streamState.ogg_stream_packetin( data );

                 while ( !eos )
                 {  if ( !streamState.ogg_stream_pageout( page ) ) { break;  }

                    out.write( page.header, 0, page.header_len );
                    out.write( page.body, 0, page.body_len );

                    if ( page.ogg_page_eos() > 0 )   eos = true;
                 }
              }
           }
        }
        in.close();
        if (file==null) audioBytes = byteStream.toByteArray();
        else out.close();
    }   // End writeOgg()

    private void writeSpx(OutputStream stream) throws IOException
    {
    	AudioInputStream targetStream;
        AudioFileFormat.Type fileType = getAudioType("spx");
        int frames = audioBytes.length / getFrameSize(format);
        AudioInputStream audio = new AudioInputStream
                (new ByteArrayInputStream(audioBytes), format, frames);

        float sampleRate = format.getSampleRate();
        int channels = format.getChannels();
        AudioFormat target = new AudioFormat
               (AudioFormat.Encoding.PCM_SIGNED,
                  sampleRate, 16, channels, 2, sampleRate, format.isBigEndian());
        targetStream = convertAudioFormat(audio, target);
        audio = targetStream;
        
        AudioFormat speexFormat = new AudioFormat
                   (SpeexEncoding.SPEEX_Q5, sampleRate, -1, 
                      channels, -1, -1, format.isBigEndian());

           
        int   mode = 2; // Assume ultra wide band and then override if necessary
        if (sampleRate < 12000)        mode = 0;   // NarrowBand
        else if (sampleRate < 24000)   mode = 1;   // Wideband

        Pcm2SpeexAudioInputStream speex = new Pcm2SpeexAudioInputStream
                  (mode, QUALITY, audio, speexFormat, frames);
        Encoder encoder = speex.getEncoder();
        encoder.setComplexity(COMPLEXITY);
        encoder.setVbr(VBR);
        encoder.setVad(VAD);
        encoder.setDtx(DTX);
           
        SpeexAudioFileWriter writer = new SpeexAudioFileWriter();
        writer.write(speex, fileType, stream);
    }

    /** Method to copy from a PCM byte array and create to a byte array in file format
     *  @param extension the extension for the desired compressed format
     */
    public byte[] copySoundStream(String extension) throws IOException
    {
        AudioFileFormat.Type fileType = getAudioType(extension);
        
        if (fileType==null) return audioBytes;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        if (extension.equalsIgnoreCase("spx"))
        {  writeSpx(output);
           return output.toByteArray();
        }
        else 
        {  
           int frameSize = getFrameSize(format);
           if (frameSize<0) frameSize = format.getSampleSizeInBits() / 8;
           int frames = audioBytes.length / getFrameSize(format);
           AudioInputStream audio = new AudioInputStream
                (new ByteArrayInputStream(audioBytes), format, frames);
           AudioInputStream target = convertForEncoding(audio, extension);
           if (extension.equalsIgnoreCase("ogg"))
           {   
        	   writeOgg(target, null, AudioFileFormat.Type.WAVE);
               return audioBytes;
           }
           
           AudioSystem.write(target, fileType, output);        
           return output.toByteArray();
        }
        
    }   // End copySoundStream()
   
    /** Method to alter the frame rate of a recorded sound
     * 
     * @param targetFormat an AudioFormat object containing the new rate
     * @return array of bytes reflecting the altered frame rate
     */
    public byte[] changeFrameRate(AudioFormat targetFormat) throws IOException
    {
       float frameRate = format.getFrameRate();
       int   frameSize = getFrameSize(format);
       float newRate   = targetFormat.getFrameRate();
                    
       ByteArrayInputStream inputStream = new ByteArrayInputStream(audioBytes);

       int frames = audioBytes.length / frameSize;
       AudioInputStream audio = new AudioInputStream(inputStream, format, frames);
       AudioInputStream target = convertAudioFormat(audio, targetFormat);
      
       int newFrames = (int)(frames * newRate/frameRate);
       frameSize = getFrameSize(targetFormat);
       byte[] inputBytes = new byte[newFrames * frameSize];
       target.read(inputBytes, 0, newFrames * frameSize);
       
       try { audio.close(); target.close(); } catch (Exception e) {}
       return inputBytes;
    }

    public void writeSoundStream(SoundData sound, String name) throws IOException
    {
        int lastIndex = name.lastIndexOf(".");
        String extension = name.substring(lastIndex+1);
        if (lastIndex == -1) throw new IOException("Invalid Extension");

        if (extension.equalsIgnoreCase("mp3"))
        {   
		   // Encode SoundData object to MP3 output file
		   // DEBUG displays output
		   new MP3Encoder(sound, name, false);
           return;
        }
        
        writeSoundStream(name);
    }
    
    /** Method to write and encode files, from the internal PCM format.
     * 
     * @param name Name of the file to write to (byte array (audioBytes) if null).
     */
    public void writeSoundStream(String name) throws IOException
    {
       // Get desired audio file type.
       int lastIndex = name.lastIndexOf(".");
       String extension = name.substring(lastIndex+1);
       if (lastIndex == -1) throw new IOException("Invalid Extension");
     
       // Get the audioFormat type corresponding to this extension.
       AudioFileFormat.Type[] typesSupported = AudioSystem.getAudioFileTypes();
       AudioFileFormat.Type   audioType = null;

       String typeExtension = extension;
       if (extension.equalsIgnoreCase("ogg")) typeExtension = "wav";
       for (int i=0; i<typesSupported.length; i++)
       {  if (typesSupported[i].getExtension().equals(typeExtension))
	      {   audioType = typesSupported[i];
       	      break;
	      }
       }
       if (audioType == null) throw new IOException("Invalid File Type");
 
       // Verify the current format
       InputStream inStream   = new ByteArrayInputStream(audioBytes);
       AudioInputStream audio;
       AudioFormat audioFormat;
       try
       {
          audio = AudioSystem.getAudioInputStream(inStream);
          audioFormat = audio.getFormat();
          if (audioFormat.toString().toLowerCase().startsWith("mpeg"))
        	  throw new Exception("looks like mpeg");
       }
       catch (Exception e)
       {
          audio = new AudioInputStream(inStream, format, 
                                      audioBytes.length / getFrameSize(format));
          audioFormat = format;
       } 
       if (extension.equalsIgnoreCase("ogg"))
       {   writeOgg(audio, new File(name), audioType);
           return;
       }
       
       if (verifyFormat(extension, audioFormat))
       {  FileOutputStream fos = new FileOutputStream(new File(name));
          AudioSystem.write(audio, audioType, fos);
          fos.flush();
          fos.close();
          return;
       }
       
     
       // SPEEX Encoding.
       if (audioType.getExtension().toLowerCase().equals("spx"))
       {
          FileOutputStream fos = new FileOutputStream(new File(name));
          writeSpx(fos);
          fos.flush();
          fos.close();          
       }
       
       // Encodings other than speex.
       else
       {  audio = convertForEncoding(audio, extension);
          AudioSystem.write(audio, audioType, new File(name));        
       }
    }  // End of writeSoundStream();
    
    /** Method to determine if the audio format is already correct
     * 
     * @param extension desired file extension
     * @param source current encoding
     * @return return true if current equals the correct format
     */
    public boolean verifyFormat(String extension, AudioFormat source)
    {  
        AudioFormat desired = source;
        String encoding = source.getEncoding().toString();
        if (extension.equals("spx"))
        { 
            if (!encoding.startsWith("SPEEX")) return false;            
            return true;
        }
        if (extension.equals("gsm"))
        {
            if (!encoding.startsWith("GSM")) return false;
            return true;
        }
        if (extension.toLowerCase().startsWith("aif"))
        {
           desired = new AudioFormat(
              AudioFormat.Encoding.PCM_UNSIGNED,
              source.getSampleRate(), 8, 1, 1, source.getFrameRate(), source.isBigEndian());
         }
        if (extension.toLowerCase().equals("au"))
        {
            desired = new AudioFormat(
                      AudioFormat.Encoding.PCM_UNSIGNED,
                      8000.0F, 8, 1, 1, 8000.0F, source.isBigEndian());
              
        }
        if (extension.equals("wav"))
        {
            desired = SoundDefaults.createAudioFormat(false);
        }
        return compareAudioFormat(source, desired);
        
    }       // End of validateFormat();
    
    /** Get desired frame rate of written file
     * 
     * @param extension Extension to write
     * @return frame rate or -1 if to be unchanged
     */
    public static int getDesiredFrameRate(String extension)
    {
        if (extension==null) return -1;
        if (extension.equals("wav")) return (int)SoundDefaults.getRate();
        if (extension.equals("au"))  return 8000;
        return -1;
        
    }
    
    private static int getFrameSize(AudioFormat format) 
    {
    	int frameSize = format.getFrameSize();
    	if (frameSize<0) frameSize = format.getSampleSizeInBits() /8;
    	return frameSize;
    }

    /** Get encoding type for the supplied extension
     * 
     * @param extension The extension that goes with the desired file format
     * @return encoding type
     */
    private AudioFileFormat.Type getAudioType(String extension)
    {
        AudioFileFormat.Type[] typesSupported = AudioSystem.getAudioFileTypes();
		      AudioFileFormat.Type   audioType = null;
        for (int i=0; i<typesSupported.length; i++)
			     {
		  	      if (typesSupported[i].getExtension().equals(extension))
			        {
			  	         audioType = typesSupported[i];
       	       break;
			        }
			     }
        return audioType;
    }
}      // End of SoundIO class.
