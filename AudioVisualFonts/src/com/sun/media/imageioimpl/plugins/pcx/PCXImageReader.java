/*
 * $RCSfile: PCXImageReader.java,v $
 *
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.3 $
 * $Date: 2007/09/07 19:13:02 $
 * $State: Exp $
 */
package com.sun.media.imageioimpl.plugins.pcx;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteOrder;
import java.util.*;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

public class PCXImageReader extends ImageReader implements PCXConstants {

    private ImageInputStream iis;
    private int width, height;
    private boolean gotHeader = false;
    private byte manufacturer;
    private byte encoding;
    private short xmax, ymax;
    private byte[] smallPalette = new byte[3 * 16];
    private byte[] largePalette = new byte[3 * 256];
    private byte colorPlanes;
    private short bytesPerLine;
    private short paletteType;

    private PCXMetadata metadata;

    private SampleModel sampleModel, originalSampleModel;
    private ColorModel colorModel, originalColorModel;

    /** The destination region. */
    private Rectangle destinationRegion;

    /** The source region. */
    private Rectangle sourceRegion;

    /** The destination image. */
    private BufferedImage bi;
 
    /** Indicates whether subsampled, subregion is required, and offset is
     *  defined
     */
    private boolean noTransform = true;

    /** Indicates whether subband is selected. */
    private boolean seleBand = false;

    /** The scaling factors. */
    private int scaleX, scaleY;

    /** source and destination bands. */
    private int[] sourceBands, destBands;

    public PCXImageReader(PCXImageReaderSpi imageReaderSpi) 
    {
    	super(imageReaderSpi);
    }
    
    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) 
    {
		super.setInput(input, seekForwardOnly, ignoreMetadata);
		
		iis = (ImageInputStream) input; // Always works
		if (iis != null)
		    iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		try { iis.seek(0); } 
    	catch (IOException e) 
		{  
			System.out.println(e);
		}
		gotHeader = false;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException 
    {
		checkIndex(imageIndex);
		readHeader();
		return height;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException 
    {
		checkIndex(imageIndex);
	    readHeader();
		return metadata;
	}

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException 
    {
		checkIndex(imageIndex);
		readHeader();
		return Collections.singletonList(new ImageTypeSpecifier(originalColorModel, originalSampleModel)).iterator();
	}

    @Override
    public int getNumImages(boolean allowSearch) throws IOException 
    {
		if (iis == null) 
		{
		    throw new IllegalStateException("input is null");
		}
		if (seekForwardOnly && allowSearch) 
		{
		    throw new IllegalStateException("cannot search with forward only input");
		}
		return 1;
	}

    @Override
    public IIOMetadata getStreamMetadata() throws IOException 
    {
    	return null;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException 
    {
		checkIndex(imageIndex);
		readHeader();
		return width;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException 
    {
    	iis = (ImageInputStream)getInput();
		checkIndex(imageIndex);
		readHeader();
	
		if (iis == null)
		    throw new IllegalStateException("input is null");
	
		clearAbortRequest();
		processImageStarted(imageIndex);
	
		if (param == null)
		    param = getDefaultReadParam();
	
		sourceRegion = new Rectangle(param.getDestinationOffset().x, param.getDestinationOffset().y, width, height);
		destinationRegion = new Rectangle(param.getDestinationOffset().x, param.getDestinationOffset().y, width, height);
	
		computeRegions(param, this.width, this.height, param.getDestination(), sourceRegion, destinationRegion);
	
		scaleX = param.getSourceXSubsampling();
		scaleY = param.getSourceYSubsampling();
	
		// If the destination band is set used it
		sourceBands = param.getSourceBands();
		destBands = param.getDestinationBands();
	
		seleBand = (sourceBands != null) && (destBands != null);
		noTransform = destinationRegion.equals(new Rectangle(0, 0, width, height)) || seleBand;
	
		if (!seleBand) 
		{
		    sourceBands = new int[colorPlanes];
		    destBands = new int[colorPlanes];
		    for (int i = 0; i < colorPlanes; i++)
			   destBands[i] = sourceBands[i] = i;
		}
	
		// If the destination is provided, then use it.  Otherwise, create new one
		bi = param.getDestination();
	
		// Get the image data.
		WritableRaster raster = null;
	
		if (bi == null) 
		{
		    if (sampleModel != null && colorModel != null) 
		    {
				sampleModel = sampleModel.createCompatibleSampleModel(destinationRegion.width - destinationRegion.x, destinationRegion.height
				        - destinationRegion.y);
				if (seleBand)
				    sampleModel = sampleModel.createSubsetSampleModel(sourceBands);
				raster = Raster.createWritableRaster(sampleModel, new Point(0, 0));
				bi = new BufferedImage(colorModel, raster, false, null);
		    }
		} else 
		{
		    raster = bi.getWritableTile(0, 0);
		    sampleModel = bi.getSampleModel();
		    colorModel = bi.getColorModel();
	
		    noTransform &= destinationRegion.equals(raster.getBounds());
		}
	
		byte bdata[] = null; // buffer for byte data
	
		if (sampleModel.getDataType() == DataBuffer.TYPE_BYTE)
		{
		    bdata = (byte[]) ((DataBufferByte) raster.getDataBuffer()).getData();
		}
		else
			throw new IllegalArgumentException("Not Data buffer of type: byte");
	
		readImage(bdata);
	    
	    if (abortRequested())
		    processReadAborted();
		else
		    processImageComplete();
		
		return bi;
    }

    private void readImage(byte[] data) throws IOException 
    {
		byte[] scanline = new byte[bytesPerLine*colorPlanes];

		if (noTransform) 
		{
		    try {
			int offset = 0;
			int nbytes = (width * metadata.bitsPerPixel + 8 - metadata.bitsPerPixel) / 8;
			// The following loop is extremely slow
			for (int line = 0; line < height; line++) 
			{
			    readScanLine(scanline);
			    for (int band = 0; band < colorPlanes; band++) 
			    {
					System.arraycopy(scanline, bytesPerLine * band, data, offset, nbytes);
					offset += nbytes;
			    }
			    processImageProgress(100.0F * line / height);
			}
		    } catch (EOFException e) {}
		} else 
		{
		    if (metadata.bitsPerPixel == 1)
			read1Bit(data);
		    else if (metadata.bitsPerPixel == 4)
			read4Bit(data);
		    else
			read8Bit(data);
		}
    }

    private void read1Bit(byte[] data) throws IOException 
    {
		byte[] scanline = new byte[bytesPerLine];
		
		try 
		{
		    // skip until source region
		    for (int line = 0; line < sourceRegion.y; line++) 
		    {
		    	readScanLine(scanline);
		    }
		    
            int lineStride =
                ((MultiPixelPackedSampleModel)sampleModel).getScanlineStride();

		    // cache the values to avoid duplicated computation
		    int[] srcOff = new int[destinationRegion.width];
		    int[] destOff = new int[destinationRegion.width];
		    int[] srcPos = new int[destinationRegion.width];
		    int[] destPos = new int[destinationRegion.width];
	
		    for (int i = destinationRegion.x, x = sourceRegion.x, j = 0; i < destinationRegion.x + destinationRegion.width; i++, j++, x += scaleX) 
		    {
				srcPos[j] = x >> 3;
				srcOff[j] = 7 - (x & 7);
				destPos[j] = i >> 3;
				destOff[j] = 7 - (i & 7);
		    }
	    
            int k = destinationRegion.y * lineStride;
	
		    for (int line = 0; line < sourceRegion.height; line++) 
		    {
				readScanLine(scanline);
				if (line % scaleY == 0) 
				{
				    for (int i = 0; i < destinationRegion.width; i++) 
				    {
						//get the bit and assign to the data buffer of the raster
						int v = (scanline[srcPos[i]] >> srcOff[i]) & 1;
						data[k + destPos[i]] |= v << destOff[i];
				    }
				    k += lineStride;
				}
				processImageProgress(100.0F * line / sourceRegion.height);
		    }
		} catch (EOFException e) {}
    }
    
    private void read4Bit(byte[] data) throws IOException 
    {
		byte[] scanline = new byte[bytesPerLine];
		try 
		{
            int lineStride =
                ((MultiPixelPackedSampleModel)sampleModel).getScanlineStride();

            // cache the values to avoid duplicated computation
            int[] srcOff = new int[destinationRegion.width];
            int[] destOff = new int[destinationRegion.width];
            int[] srcPos = new int[destinationRegion.width];
            int[] destPos = new int[destinationRegion.width];

            for (int i = destinationRegion.x, x = sourceRegion.x, j = 0;
                 i < destinationRegion.x + destinationRegion.width;
                 i++, j++, x += scaleX) 
            {
                srcPos[j] = x >> 1;
                srcOff[j] = (1 - (x & 1)) << 2;
                destPos[j] = i >> 1;
                destOff[j] = (1 - (i & 1)) << 2;
            }

            int k = destinationRegion.y * lineStride;

    	    for (int line = 0; line < sourceRegion.height; line++) 
    	    {
    	    	readScanLine(scanline);

                if (abortRequested())
                    break;
                if (line % scaleY == 0) 
                {
	                for (int i = 0; i < destinationRegion.width; i++) 
	                {
	                    //get the bit and assign to the data buffer of the raster
	                    int v = (scanline[srcPos[i]] >> srcOff[i]) & 0x0F;
	                    data[k + destPos[i]] |= v << destOff[i];
	                }
	                k += lineStride;
                }
                processImageProgress(100.0F * line / sourceRegion.height);
    	    }
		}catch(EOFException e){}
    }

    /* also handles 24 bit (three 8 bit planes) */
    private void read8Bit(byte[] data) throws IOException 
    {
		byte[] scanline = new byte[colorPlanes * bytesPerLine];
		try 
		{
		    // skip until source region
		    for (int line = 0; line < sourceRegion.y; line++) 
		    {
		    	readScanLine(scanline);
		    }
		    int dstOffset = destinationRegion.y * (destinationRegion.x + destinationRegion.width) * colorPlanes;
		    for (int line = 0; line < sourceRegion.height; line++) 
		    {
				readScanLine(scanline);
				if (line % scaleY == 0) 
				{
				    int srcOffset = sourceRegion.x;
				    for (int band = 0; band < colorPlanes; band++) 
				    {
						dstOffset += destinationRegion.x;
						for (int x = 0; x < destinationRegion.width; x += scaleX) 
						{
						    data[dstOffset++] = scanline[srcOffset + x];
						}
						srcOffset += bytesPerLine;
				    }
				}
				processImageProgress(100.0F * line / sourceRegion.height);
		    }
		} catch (EOFException e) {}
    }
    
    private void readScanLine(byte[] buffer) throws IOException 
    {
		int max = bytesPerLine * colorPlanes;
		int val, next;
		byte num;
		for (int j = 0; j < max;) 
		{
		    val = readUnsignedByte();
	
		    if ((val & 0xC0) == 0xC0) 
		    {
				int count = val & ~0xC0;
				num = (byte)(readUnsignedByte() & 0xFF);
				next = (j+count>=max) ? max : j + count;
				Arrays.fill(buffer, j, next, num);
				j = next;
		    } else 
		    {
		    	buffer[j++] = (byte) (val);
		    }
		}
    }
    
    private final static int MAX_BUFFER = 1024;
    private static byte[] buffer = new byte[MAX_BUFFER];
    private static int offset = buffer.length;
    private static int max = 0;
    
    private int readUnsignedByte() throws IOException
    {
    	
    	if (offset>=max)
    	{ 
   			max = iis.read(buffer);
    		offset = 0;
    	}
    	return buffer[offset++]&0xFF;
    	
    }
	
	private void checkIndex(int imageIndex) 
	{
		if (imageIndex != 0) 
		{
		    throw new IndexOutOfBoundsException("only one image exists in the stream");
		}
    }

    private void readHeader() throws IOException 
    {
    	if (gotHeader) 
    	{
    		iis.seek(128);
    		return;
    	}

		metadata = new PCXMetadata();
	
		manufacturer = iis.readByte(); // manufacturer
		if (manufacturer != MANUFACTURER)
		    throw new IllegalStateException("image is not a PCX file");
		metadata.version = iis.readByte(); // version
		encoding = iis.readByte(); // encoding
			if (encoding != ENCODING)
		    throw new IllegalStateException("image is not a PCX file, invalid encoding " + encoding);
	
		metadata.bitsPerPixel = iis.readByte();
		metadata.xmin = iis.readShort();
		metadata.ymin = iis.readShort();
		xmax = iis.readShort();
		ymax = iis.readShort();
	
		metadata.hdpi = iis.readShort();
		metadata.vdpi = iis.readShort();
	
		iis.readFully(smallPalette);
		iis.readByte(); // reserved

		colorPlanes = iis.readByte();
		bytesPerLine = iis.readShort();
		paletteType = iis.readShort();
	
		metadata.hsize = iis.readShort();
		metadata.vsize = iis.readShort();
	
		iis.skipBytes(54); // skip filler
	
		width = xmax - metadata.xmin + 1;
		height = ymax - metadata.ymin + 1;
		if (colorPlanes == 1) 
		{
		    if (paletteType == PALETTE_GRAYSCALE) 
		    {
				ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				int[] nBits = { 8 };
				colorModel = new ComponentColorModel(cs, nBits, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
				sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width, new int[] { 0 });
		    } else 
		    {
		    	if (metadata.bitsPerPixel == 8) 
		    	{
				    // read palette from end of file, then reset back to image data
				    iis.mark();
		
				    if (iis.length() == -1) 
				    {
						// read until eof, and work backwards
				    	byte[] data = new byte[MAX_BUFFER];
						while (iis.read(data) != -1)
						    ;
						
						iis.seek(iis.getStreamPosition() - 256 * 3 - 1);
				    } 
				    else 
				    {
				    	iis.seek(iis.length() - 256 * 3 - 1);
				    }
		
                    int palletteMagic = iis.read();
                    System.out.println(palletteMagic);
                    if(palletteMagic != 12)
                        processWarningOccurred("Expected palette magic number 12; instead read "+
                               palletteMagic+" from this image.");

				    iis.readFully(largePalette);
				    for(int i=0; i<largePalette.length; i++)
				    	System.out.println(largePalette[i]);
				    iis.reset();
		
				    colorModel = new IndexColorModel(metadata.bitsPerPixel, 256, largePalette, 0, false);
				    System.out.println(colorModel);
				    sampleModel = colorModel.createCompatibleSampleModel(width, height);
				} else 
				{
				    int msize = metadata.bitsPerPixel == 1 ? 2 : 16;
				    colorModel = new IndexColorModel(metadata.bitsPerPixel, msize, smallPalette, 0, false);
				    sampleModel = colorModel.createCompatibleSampleModel(width, height);
				}
		    }
		} else 
		{
		    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		    int[] nBits = { 8, 8, 8 };
		    colorModel = new ComponentColorModel(cs, nBits, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		    sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width * colorPlanes, new int[] { 0, width, width * 2 });
		}
	
		originalSampleModel = sampleModel;
		originalColorModel = colorModel;
	
		gotHeader = true;
    }
    
}	// End of PCXImageReader class
