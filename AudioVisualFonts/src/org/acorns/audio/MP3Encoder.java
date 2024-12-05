package org.acorns.audio;


import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

import org.acorns.data.SoundData;
import acorns.mp3.encoder.*;

public class MP3Encoder 
{
	private ID3Tag id3;
	private Lame lame;
	private GainAnalysis ga;
	private BitStream bs;
	private Presets p;
	private QuantizePVT qupvt;
	private Quantize qu;
	private VBRTag vbr;
	private Version ver;
	private Reservoir rv;
	private Takehiro tak;
	
	private int bytesRead;
	private int[] timeBuffer;

	private PropertyChangeSupport support = new PropertyChangeSupport(this);

	/** Constructor for encoding an input file
	 * 
	 * @param Object containing the input audio data
	 * @param outPath Path to the encoded output
	 * @throws IOException
	 */
	public MP3Encoder(SoundData sound, String outPath, boolean debug) throws IOException 
	{
		// initialize encoder modules
		lame = new Lame();
		ga = new GainAnalysis();
		bs = new BitStream();
		p = new Presets();
		qupvt = new QuantizePVT();
		qu = new Quantize();
		vbr = new VBRTag();
		ver = new Version();
		id3 = new ID3Tag();

		rv = new Reservoir();
		tak = new Takehiro();

	    lame.setModules(ga, bs, p, qupvt, qu, vbr, ver, id3);
		bs.setModules(ga, ver, vbr);
		id3.setModules(bs, ver);
		p.setModules(lame);
		qu.setModules(bs, rv, qupvt, tak);
		qupvt.setModules(tak, rv, lame.enc.psy);
		rv.setModules(bs);
		tak.setModules(qupvt);
		vbr.setModules(lame, bs, ver);

		// Prepare for reading source data
		TimeDomain time = new TimeDomain(sound);
		double[] timeDomain = time.getTimeDomainFromAudio();
		timeBuffer = new int[timeDomain.length];
		bytesRead = 0;
		
		AudioFormat audioFormat = sound.getAudioFormat();
		int bits = audioFormat.getSampleSizeInBits();
		for (int i=0; i<timeDomain.length; i++)
		{
			timeBuffer[i] = (int)timeDomain[i] << (32- bits);
		}

		LameGlobalFlags gf = lame.lame_init();
		gf.num_channels = audioFormat.getChannels();
		gf.in_samplerate = (int)sound.getFrameRate();
		gf.num_samples = timeBuffer.length;

		// Set encoding options
		gf.quality = 4;  			 // 0 best, 9 worst, 5 default
		gf.findReplayGain = true;  	 // RG enabled by default
		id3.id3tag_init(gf);
		
		/*
		 * turn off automatic writing of ID3 tag data into mp3 stream we have to
		 * call it before 'lame_init_params', because that function would spit
		 * out ID3v2 tag data.
		 */
		gf.write_id3tag_automatic = false;

		/*
		 * Now that all the options are set, lame needs to analyze them and set
		 * some more internal options and check for problems
		 */
		int i = lame.lame_init_params(gf);
		if (i < 0) 
		{
			System.err.println("fatal error during initialization");
			lame.lame_close(gf);
			throw new IOException();
		}

		/*
		 * encode a single input file
		 */
		new File(outPath).createNewFile();
		DataOutput outf = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outPath), 1<<20));
		
		String inPath = sound.getSoundText(SoundData.NAME);
		if (debug)
		{
			System.out.printf("Encoding %s%s to %s\n", inPath, inPath.length()
				+ outPath.length() < 66 ? "" : "\n     ", outPath);
		}

		lame_encoder(gf, debug, outf, outPath);
		((Closeable)outf).close();
		lame.lame_close(gf);
	}

	public PropertyChangeSupport getSupport() 
	{
		return support;
	}
	
	private void print_lame_tag_leading_info(final LameGlobalFlags gf) 
	{
		if (gf.bWriteVbrTag)
			System.out.println("Writing LAME Tag...");
	}
    
	private void print_trailing_info(final LameGlobalFlags gf) 
	{
		if (gf.bWriteVbrTag)
			System.out.println("done\n");

		if (gf.findReplayGain) {
			int RadioGain = gf.internal_flags.RadioGain;
			System.out.printf("ReplayGain: %s%.1fdB\n", RadioGain > 0 ? "+"
					: "", (RadioGain) / 10.0f);
			if (RadioGain > 0x1FE || RadioGain < -0x1FE)
				System.out
						.println("WARNING: ReplayGain exceeds the -51dB to +51dB range. Such a result is too\n"
								+ "         high to be stored in the header.");
		}
	}

	private int write_xing_frame(final LameGlobalFlags gf,
			final RandomAccessFile outf) 
	{
		byte mp3buffer[] = new byte[Lame.LAME_MAXMP3BUFFER];

		int imp3 = vbr.getLameTagFrame(gf, mp3buffer);
		if (imp3 > mp3buffer.length) {
			System.err
					.printf("Error writing LAME-tag frame: buffer too small: buffer size=%d  frame size=%d\n",
							mp3buffer.length, imp3);
			return -1;
		}
		if (imp3 <= 0) {
			return 0;
		}
		try {
			outf.write(mp3buffer, 0, imp3);
		} catch (IOException e) {
			System.err.println("Error writing LAME-tag");
			return -1;
		}
		return imp3;
	}
	
	/**
	 * Read a buffer of PCM audio date 
	 * 
	 * @param gfp    global flags
	 * @param buffer buffer output to the int buffer or 16-bit buffer
	 * @param buffer output buffer
	 * @return samples read
	 */
	private int readBuffer(final LameGlobalFlags gfp,
			final int buffer[][]) 
	{
		int remaining = timeBuffer.length - bytesRead;
		if (remaining<=0) return -1;
		
		int len = buffer[0].length;
		if (remaining < len) len = remaining;
		System.arraycopy(timeBuffer, bytesRead, buffer[0], 0, len);
		if (gfp.num_channels>1)
		{
			System.arraycopy(timeBuffer, bytesRead, buffer[1], 0, len);
		}
		bytesRead += len;
		return len;
	}


	private boolean lame_encoder(final LameGlobalFlags gf, boolean debug,
			final DataOutput outf, final String outPath) 
	{
		byte mp3buffer[] = new byte[Lame.LAME_MAXMP3BUFFER];
		int Buffer[][] = new int[2][1152];
		int iread;

		if (debug)	encoder_progress_begin(gf);

		int imp3 = id3.lame_get_id3v2_tag(gf, mp3buffer, mp3buffer.length);
		if (imp3 > mp3buffer.length) {
			if (debug) encoder_progress_end(gf);
			System.err.printf
			   ("Error writing ID3v2 tag: buffer too small: buffer size=%d  ID3v2 size=%d\n",
							mp3buffer.length, imp3);
			return false;
		}
		try 
		{
			outf.write(mp3buffer, 0, imp3);
		} catch (IOException e) 
		{
			if (debug) encoder_progress_end(gf);
			System.err.printf("Error writing ID3v2 tag \n");
			return false;
		}
		
		int id3v2_size = imp3;

		/* encode until we hit eof */
		do 
		{
			/* read in 'iread' samples */
			iread = readBuffer(gf, Buffer);

			if (iread >= 0) 
			{
				if (debug) encoder_progress(gf);

				/* encode */
				imp3 = lame.lame_encode_buffer_int(gf, Buffer[0], Buffer[1],
						iread, mp3buffer, 0, mp3buffer.length);

				/* was our output buffer big enough? */
				if (imp3 < 0) 
				{
					if (imp3 == -1)
						System.err.printf("mp3 buffer is not big enough... \n");
					else
						System.err.printf(
								"mp3 internal error:  error code=%d\n", imp3);
					return false;
				}

				try 
				{
					outf.write(mp3buffer, 0, imp3);
				} catch (IOException e) 
				{
					if (debug) encoder_progress_end(gf);
					System.err.printf("Error writing mp3 output \n");
					return false;
				}
			}
		} while (iread > 0);

		imp3 = lame.lame_encode_flush(gf, mp3buffer, 0, mp3buffer.length);
		
		/*
		 * may return one more mp3 frame
		 */

		if (imp3 < 0) 
		{
			if (imp3 == -1)
				System.err.printf("mp3 buffer is not big enough... \n");
			else
				System.err.printf("mp3 internal error:  error code=%d\n", imp3);
			return false;

		}

		if (debug) encoder_progress_end(gf);

		try
		{
			outf.write(mp3buffer, 0, imp3);
		} catch (IOException e) 
		{
			if (debug) encoder_progress_end(gf);
			System.err.printf("Error writing mp3 output \n");
			return false;
		}

		imp3 = id3.lame_get_id3v1_tag(gf, mp3buffer, mp3buffer.length);
		if (imp3 > mp3buffer.length) 
		{
			System.err
					.printf("Error writing ID3v1 tag: buffer too small: buffer size=%d  ID3v1 size=%d\n",
							mp3buffer.length, imp3);
		} else 
		{
			if (imp3 > 0) 
			{
				try {
					outf.write(mp3buffer, 0, imp3);
				} catch (IOException e) {
					if (debug) encoder_progress_end(gf);
					System.err.printf("Error writing ID3v1 tag \n");
					return false;
				}
			}
		}

		print_lame_tag_leading_info(gf);
		try 
		{
			((Closeable)outf).close();
			RandomAccessFile rf = new RandomAccessFile(outPath, "rw");
			rf.seek(id3v2_size);
			write_xing_frame(gf, rf);
			rf.close();
		} catch (IOException e) 
		{
			System.err.printf("fatal error: can't update LAME-tag frame!\n");
		}

		if (debug) print_trailing_info(gf);
		return true;
	}
	/** Output information about the file being encoded */
	private void encoder_progress_begin(final LameGlobalFlags gf) 
	{
		lame.lame_print_config(gf);
		/* print useful information about options being used */

		System.out.printf("Encoding as %g kHz ", 1.e-3 * gf.out_samplerate);

		{
			String[][] mode_names = {
					{ "stereo", "j-stereo", "dual-ch", "single-ch" },
					{ "stereo", "force-ms", "dual-ch", "single-ch" } };
			switch (gf.VBR) {
			case vbr_rh:
				System.out.printf(
						"%s MPEG-%d%s Layer III VBR(q=%g) qval=%d\n",
						mode_names[gf.force_ms ? 1 : 0][gf.mode.ordinal()],
						2 - gf.version, gf.out_samplerate < 16000 ? ".5"
								: "", gf.VBR_q + gf.VBR_q_frac, gf.quality);
				break;
			case vbr_mt:
			case vbr_mtrh:
				System.out.printf("%s MPEG-%d%s Layer III VBR(q=%d)\n",
						mode_names[gf.force_ms ? 1 : 0][gf.mode.ordinal()],
						2 - gf.version, gf.out_samplerate < 16000 ? ".5"
								: "", gf.quality);
				break;
			case vbr_abr:
				System.out
						.printf("%s MPEG-%d%s Layer III (%gx) average %d kbps qval=%d\n",
								mode_names[gf.force_ms ? 1 : 0][gf.mode
										.ordinal()],
								2 - gf.version,
								gf.out_samplerate < 16000 ? ".5" : "",
								0.1 * (int) (10. * gf.compression_ratio + 0.5),
								gf.VBR_mean_bitrate_kbps, gf.quality);
				break;
			default:
				System.out.printf(
						"%s MPEG-%d%s Layer III (%gx) %3d kbps qval=%d\n",
						mode_names[gf.force_ms ? 1 : 0][gf.mode.ordinal()],
						2 - gf.version, gf.out_samplerate < 16000 ? ".5"
								: "",
						0.1 * (int) (10. * gf.compression_ratio + 0.5),
						gf.brate, gf.quality);
				break;
			}
		}

		System.out.print("|");
		for (int i = 0; i < MAX_WIDTH - 2; i++) {
			System.out.print("=");
		}
		System.out.println("|");
		oldPercent = curPercent = oldConsoleX = 0;
	}

	private void encoder_progress(final LameGlobalFlags gf) 
	{
		if ((gf.frameNum % 100) == 0)	
			timestatus(gf.frameNum, lame_get_totalframes(gf));
	}

	private void encoder_progress_end(final LameGlobalFlags gf) 
	{
			timestatus(gf.frameNum, lame_get_totalframes(gf));
			System.out.print("|");
			for (int i = 0; i < MAX_WIDTH - 2; i++)	{System.out.print("=");}
			System.out.println("|");
	}

	private int oldPercent, curPercent, oldConsoleX;

	private void timestatus(final int frameNum, final int totalframes) 
	{
		int percent;

		if (frameNum < totalframes) {
			percent = (int) (100. * frameNum / totalframes + 0.5);
		} else {
			percent = 100;
		}
	
		boolean stepped = false;
		if (oldPercent != percent) {
			progressStep();
			stepped = true;
		}
		oldPercent = percent;
		if (percent == 100) {
			for (int i = curPercent; i < 100; i++) {
				progressStep();
				stepped = true;
			}
		}
		if (percent == 100 && stepped) {
			System.out.println();
		}
	}

	private static final int MAX_WIDTH = 79;

	private void progressStep() 
	{
		curPercent++;
		float consoleX = (float) curPercent * MAX_WIDTH / 100f;
		if ((int) consoleX != oldConsoleX)
			System.out.print(".");
		oldConsoleX = (int) consoleX;
		support.firePropertyChange("progress", oldPercent, curPercent);
	}

	/**
	 * LAME's estimate of the total number of encoded frames to be encoded. Only valid
	 * if calling program set num_samples.
	 */
	private int lame_get_totalframes(final LameGlobalFlags gfp) 
	{
		/* estimate based on user set num_samples: */
		int totalframes = (int) (2 + ((double) gfp.num_samples * gfp.out_samplerate)
				/ ((double) gfp.in_samplerate * gfp.framesize));

		return totalframes;
	}

}
