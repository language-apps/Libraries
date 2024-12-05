/**
 * PictureData.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.data;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.acorns.visual.GifDecoder;
import org.acorns.visual.GifEncoder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Hold the data associated with a picture. Handles static pictures and
 *  animated GIF files.
 */
public class PictureData implements Serializable, ActionListener, Cloneable
{
    /** Background GIF transparent color */
    public static final Color TRANS = GifDecoder.TRANS;
    public static final String DPI = "72";  // JPG dots per inch
    
    private byte[] data;         // The byte array corresponding to this picture
    private Vector<String> text; // A text string Vector to animate the picture
    
    private int scale;           // How much to scale original picture
    private int angle;           // Rotation angle
    
    transient BufferedImage[] frames;          // Array of image frames
    transient int             delays[];        // Array of delay between display of each frame
    transient long            cycleTime;       // Time to display all of the pictures one time
    transient int             iterations;      // times to cycle through frames
    
    // Variable for looped display of pictures
    transient JComponent      panel;           // Panel to write picture into

    transient long            startTime = -1;  // Frame display loop start time
    transient long            endTime   = -1;  // Frame display stop time
    transient Timer           timer;           // Timer to control frame display
    transient BufferedImage   image;           // Scaled BufferedImage.
    transient ImageIcon       imageIcon = null;// Scaled icon for the image.
    transient int             lastFrame = -1;  // Number of last frame displayed

    private final static int BUFFER_SIZE = 65536; // Size of input buffer
    private final static int PERIOD = 100;        // time interval(milliseconds)

    /** Maximum Scale Factor */
    public  final static int MAX_SCALE = 500;     // Maximum scale factor

    /** Minimum Scale Factor */
    public  final static int MIN_SCALE = 20;      // Minimum scale factor

    /** Java file's serial version number */
    private static final long serialVersionUID = 1;

    /** Constructor to read a picture from a file
     * 
     * @param urlName read a file and store as a PictureData object
     * @param size maximum size for this picture
     */
    public PictureData(URL urlName, Dimension size) throws IOException
    {   
    	String file = urlName.getFile();
        int lastIndex = file.lastIndexOf('/') + 1;
        file = file.substring(lastIndex);
        file = file.replaceAll("%20", " ");
        
        lastIndex = file.lastIndexOf('.')+1;
        if (lastIndex<=0) throw new IOException("Invalid extension");
        String extension = file.substring(lastIndex);
        
        String[] imageArray = ImageIO.getReaderFormatNames();
        boolean found = false;
        for (int i=0; i<imageArray.length; i++)
        {   if (imageArray[i].equalsIgnoreCase(extension))
            { found = true; break; }
        }
        if (!found) throw new IOException("invalid extension"); 
                
        text = new Vector<String>();
        text.add(file);
        text.add(extension);
        text.add(urlName.getPath());
        
        // Read the file into a byte array.
        ImageInputStream in = ImageIO.createImageInputStream(urlName.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        while ((count = in.read(buffer, 0, BUFFER_SIZE)) > 0)
        {
            out.write(buffer, 0, count);
        }
        data = out.toByteArray();
        angle = 0;    // Assume initial angle requires no rotation
        scale = 100;  // Assume initial scale factor is 100
        try
        {
            if (!loadImages(size==null, size)) throw new IOException();
        }
        catch (IllegalArgumentException e)
        {
    		frames[0] = recoverImageRaster(urlName.openStream(), extension);
            out = new ByteArrayOutputStream();
            try
            {   encode(extension, out);
                data = out.toByteArray();
                out.close();
                if (!loadImages(size==null, size)) throw new IOException();
            }
            catch (IOException io) {}
        }
        in.close();
    }   // End of PictureData constructor.

     /** Instantiate from a byte array (for conversion from earlier ACORNS
      *  versions.
      *
      * @param bytes The byte array containing the picture
      * @param size The size to display
      * @throws IOException
      */
     public PictureData(byte[] bytes, Dimension size) throws IOException
     {   text = new Vector<String>();
         text.add(null);
         text.add("jpg");

         data = bytes;
         angle = 0;    // Assume initial angle requires no rotation
         scale = 100;  // Assume initial scale factor is 100
         if (!loadImages(size==null, size)) throw new IOException();
         frames[0] = scaleImage(frames[0], size);
     }

     /** Method to load images and scale them down specified
      * 
      * @param frame BufferedImage to scale
      * @param size maximum width and height to save picture
      * @return scaled image
      */
     private BufferedImage scaleImage
                     (BufferedImage frame, Dimension size)
     {  // Scale all the images if necessary.
        int newWidth, newHeight, width, height;

        double scaleX = 1.0, scaleY = 1.0;
	    if (size != null)  
	    {
            do
            {

	           width = frame.getWidth();
	           height = frame.getHeight();
	
	           // Don't scale if width/height = 0
	           if (width ==0 || height ==0) return frame; 
	           scaleX = 1.0 * size.width / width;
	           scaleY = 1.0 * size.height / height;
	           if (scaleX < 1.0 || scaleY < 1.0) 
	           {
	               if (scaleX > scaleY) scaleX = scaleY;
	               else                 scaleY = scaleX;
	           }
	           else scaleX = scaleY = 1.0;
	
	           newWidth = (int)((scaleX<=0.5) ? width * 0.5 : width * scaleX);
	           newHeight = (int)((scaleY<=0.5) ? height * 0.5 : height * scaleY);
	
	           BufferedImage newImage
	                   = makeNewImage(new Dimension(newWidth, newHeight));
	           Graphics2D graphics = newImage.createGraphics();
	           graphics.setComposite(AlphaComposite.Src);
	           graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	           graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	           graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	 
	           graphics.drawImage(frame, 0, 0, newWidth, newHeight, null);
	           graphics.dispose();
	           
	           frame = newImage;
           }  while (scaleX<=0.5 || scaleY<=0.5);
        }  // End of check to see if max size defined
        return frame;
     }  // End of load and scale
     
     /** Get vector of strings.
      * @return vector of strings (0 - file name or null, 1 - extension)
      */
     public Vector<String> getVector()  { return text; }
    
    /** Set the rotate angle
     * 
     * @param angle desired angle to rotate picture
     */
    public void setAngle(int angle)
    {
        this.angle = angle;
        frames = null;
        loadImages();
    }
    
    /** Get the rotate angle 
     * 
     * @return return the angle to rotate picture
     */
    public int getAngle()   {  return angle; }
    
    /** Set the scale factor
     * 
     * @param s desired scale factor
     * @return false if illegal, true otherwise
     */
    public boolean setScale(int s)  
    {
        if (s<MIN_SCALE) return false;
        if (s>MAX_SCALE) return false;

        if (s!=100)
        {
            Dimension size = getSize();
            double newPixels = 1.0 * size.width * size.height
                            * (1.0 * s/scale) * (1.0 * s/scale);
            if (newPixels > 2500*2500) return false;
        }
        
        scale = s;
        frames=null;
        loadImages();
        return true;        
    }

    /** Get the scale factor associated with this picture */
    public int getScale() { return scale; }

    /** Get a particular frame of the picture. Static pictures have one
        frame, animated GIFs can have many.
     *
     * @param frame Frame number
     */
    public BufferedImage getFrame(int frame)
    {
        loadImages();
        if (frame<0 || frame>frames.length) return null;
        return frames[frame];
    }

    /** Load the images from the byte array */
    private boolean loadImages() { return loadImages(true, null); }

    public boolean reloadImages(boolean all, Dimension size)
    {   
    	frames = null;
        return loadImages(all, size);
    }
    /** Load image frames, and resize each one that is loaded
     *
     * @param all True if load all, false otherwise
     * @param size Desired size
     * @return true if successful
     */
    public final boolean loadImages(boolean all, Dimension size)
    {
        if (data==null)   return false;
        if (frames!=null)     {   return true;  }
        
        ByteArrayInputStream in;
        BufferedImage newImage;
        try
        {   stopDisplayLoop();
            if (text.get(1).equalsIgnoreCase("gif"))
            {   
                GifDecoder decoder = new GifDecoder();
                in = new ByteArrayInputStream(data);
                decoder.read(in);
                
                int frameCount = decoder.getFrameCount();
                if (!all && frameCount>1) frameCount = 1;
                
                iterations = decoder.getLoopCount();
                BufferedImage gifFrame;
                
                frames = new BufferedImage[frameCount];
                delays = new int[frameCount];
                
                for (int f=0; f<frameCount; f++)
                {
                    gifFrame = decoder.getFrame(f);
                    frames[f] = getFrame(gifFrame);
                    delays[f] = decoder.getDelay(f);
                    if (delays[f]<PERIOD) delays[f] = PERIOD;
                }
            }
            else
            {  
                in = new ByteArrayInputStream(data);
            	frames = new BufferedImage[1];
                delays = new int[1];
                newImage = ImageIO.read(in);
                //System.gc();
                frames[0] = getFrame(newImage);
                delays[0] = 0;
                frames[0] = scaleImage(frames[0], size);
            }
            System.gc();
        }
        catch (IllegalArgumentException e)
        {
        	throw e;
        }
        catch(Throwable e)
        { 
        	return false;
        }
        return true;
    }
    
    /** Get the size of the picture 
     * 
     * @return pixel width and height of the picture
     */
    public Dimension getSize()
    {
        loadImages();
        if (frames.length==0) return null;
        
        return new Dimension(frames[0].getWidth(), frames[0].getHeight());
    }

    /** Return the number of frames in this image */
    public int getNumberFrames()
    { if (frames==null) return 0;
      return frames.length;
    }
    
    /** Get a scaled and rotated frame from a picture
     * 
     * @param original image picture
     */
    private BufferedImage getFrame(BufferedImage frameImage)
    {
        // Compute rotate and scale parameters
        Dimension size = new Dimension(frameImage.getWidth(), frameImage.getHeight());
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians)), cos = Math.abs(Math.cos(radians));
        int newWidth = (int)Math.ceil(size.width*cos + size.height * sin);
        int newHeight = (int)Math.ceil(size.width*sin + size.height*cos);
        
        int translateX = (int)(newWidth - size.width)/2;
        int translateY = (int)(newHeight - size.height)/2;
        
        // Rotate and scale the picture
        BufferedImage frame = frameImage;
        if (angle!=0)
        {

            frame = makeNewImage(new Dimension(newWidth, newHeight));
            Graphics2D graphics = frame.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                                , RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            graphics.translate(translateX, translateY);
            graphics.rotate(radians, (size.width)/2, (size.height)/2);
            graphics.drawRenderedImage(frameImage, null);
            graphics.dispose();
        }
        if (scale != 100)
        {
            newWidth  *= scale/100.0;
            newHeight *= scale/100.0;
            BufferedImage newFrame 
                    = makeNewImage(new Dimension(newWidth, newHeight));
            Graphics2D graphics = newFrame.createGraphics();            
            graphics.scale(scale/100.0, scale/100.0); 
            graphics.drawRenderedImage(frame, null);
            graphics.dispose();
            frame = newFrame;
        }
        return frame;
        
    }   // End of saveFrame() method
    
  
    /** Start a group of images displaying in a panel. This is necessary to
     *  handle animated GIF files. The paintComponent method is called each
     *  time the next frame is ready to diaplay. That method calls getImage(),
     *  which returns the correct frame.
     * 
     * @param panel JPanel to display picture in
     */
    public void startDisplayLoop(JComponent panel)
    {
        if (timer!=null) stopDisplayLoop();
        if (frames!=null && frames.length>1)
        {
            this.panel = panel;
       
            // Set up timer variables
            startTime  = System.currentTimeMillis();
                        
            // Compute time to complete display of a cycle of pictures
            cycleTime = 0;
            for (int f=0; f<frames.length; f++)
            {  cycleTime += delays[f]; }
            
            if (iterations>0) endTime = iterations * cycleTime;
            else endTime = Long.MAX_VALUE;

            timer = new Timer(PERIOD, this);
            timer.start();
            
        }   // End of startDisplay()
    }
    
    /** Stop the loop that is displaying pictures
     * 
     */
    public void stopDisplayLoop()
    {
        if (timer!=null)  
        {   timer.stop(); 
        }
        timer = null;  
        image = null;
        lastFrame = -1;
    }
    
    /** Display the picture 
     * 
     * @param panel swing component where picture will be drawn
     * @param spot x and y positions and width and height where picture will be drawn
     * @return BufferedImage object to be drawn
     */
    public BufferedImage getImage(JComponent panel, Rectangle spot)
    {   Dimension size = new Dimension(spot.width, spot.height);
        if (spot.width<0 || spot.y<0) size = null;
        
        if (!loadImages(true, size))  return null;
        long elapsed = 0;

        if (panel!=this.panel)  stopDisplayLoop();

        if (panel!= null)
        {  if (timer==null) startDisplayLoop(panel);
           
           if (frames.length>1)
           {
              elapsed = System.currentTimeMillis() - startTime;       
              if (elapsed > endTime) 
              {
                  stopDisplayLoop();
                  elapsed = 0;
                  if (panel!=null) startDisplayLoop(panel);
              }
           }
        }
  
        image = getScaledImage(spot.width, spot.height, elapsed);
        return image;
    }
    
   
    /** Get the appropriate picture
     * 
     * @param width desired width of the picture (-1 means don't scale)
     * @param height desired height of the picture (-1 means don't scale)
     * @param elapsed time elapsed since drawing started
     * @return image object
     */
     
    private BufferedImage getScaledImage(int width, int height, long elapsed)
    {
        // Determine which frame should display.
        int frame = 0;
        if (timer!=null && cycleTime>0L) 
        {
            int frameDelay = (int)(elapsed % cycleTime);
            while (frameDelay > delays[frame])  frameDelay -= delays[frame++];
        }
        
        // Compute the scale factors
        if (width <= 0) width = frames[0].getWidth();
        
        if (height<=0) height = frames[0].getHeight();
        
        int lastWidth = -1, lastHeight = -1;
        image = frames[frame];
        if (image!=null)
        {
            if (lastFrame<0) lastFrame = frame;
            lastWidth  = image.getWidth();
            lastHeight = image.getHeight();
        }
        if (image==null || frame!=lastFrame || width!=lastWidth || height!=lastHeight)
        {   
            image  = makeNewImage(new Dimension((int)(width), (int)(height)));
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                                , RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            graphics.drawImage(frames[frame], 0, 0, width, height, null);
            graphics.dispose();
            lastFrame = frame;
        }
        return image;
    }
    
    /** File to encode and write picture files
     * 
     * @param file the name of the file to write to
     * @throws java.io.IOException
     */
    public void writePicture(File file) throws IOException
    {
        if (file==null) throw new IOException();

        // Force load of all frames without scaling and rotating
        frames = null;
        int saveScale = scale, saveAngle = angle;
        scale = 100;
        angle = 0;
        loadImages();
        
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        String extension = name.substring(lastIndex+1);
        
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        encode(extension, out);
        out.close();

        // Restore the original scale factor and rotate angle
        scale = saveScale;
        angle = saveAngle;
        frames = null;
    }

    /** Rewrite picture at a smaller size to save disk space
     *
     * @param size Maximum size of picture
     */
    public void rewrite(Dimension size)
    {   // Scale only if it is too big
        frames = null;
        Dimension currentSize = getSize();
        if (currentSize.width<=size.width && currentSize.height<=size.height)
            return;

        frames = null;
        loadImages(true, size);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String extension = text.get(1);
        if (!extension.equalsIgnoreCase("gif"))
        {
        	extension = "png";
        	text.set(1, extension);
        	String file = text.get(0);
        	text.set(0, changeExtension(file, extension));
        	file = text.get(2);
            text.set(2, changeExtension(file, extension));
        }
        try
        {   encode(extension, out);
            data = out.toByteArray();
            out.close();
        }
        catch (IOException io) {}
    }
    
    private String changeExtension(String file, String newExt)
    {
    	if (file == null) return file;
    	
    	int index = file.lastIndexOf(".");
    	if (index < 0) index = file.length();
    	return file.substring(0, index) + "." + newExt;
    }
    
    /** Encode the picture into the correct format
     * 
     * @param format desired encoding format
     */
    public void encode(String format, OutputStream out) throws IOException
    { 
        Color trans = new Color(TRANS.getRed(),TRANS.getGreen(),TRANS.getBlue());
        if (format.equalsIgnoreCase("gif"))
        {
            GifEncoder encoder = new GifEncoder();
            encoder.setRepeat(iterations);
            encoder.start(out);
            BufferedImage newImage;
            for (int f=0; f<frames.length; f++)
            {   newImage = frames[f];
                encoder.setTransparent(trans);
                newImage = convert(frames[f]
                        , BufferedImage.TYPE_3BYTE_BGR, ColorModel.getRGBdefault());
                encoder.setDelay(delays[f]);
                encoder.addFrame(newImage);
            }
            encoder.finish();
        }
        
        // For JPG, encode without losing dots per inch resolution on output
        else if (format.equalsIgnoreCase("jpg") ||
        		format.equalsIgnoreCase("jpeg"))
        {
        	BufferedImage bi = frames[0];
            bi = convert(frames[0], BufferedImage.TYPE_INT_RGB, null);
            ImageIO.write(bi, format, out);
        }
        else
        {
            BufferedImage newImage = frames[0];
            if (format.equalsIgnoreCase("bmp"))
                 newImage = convert(frames[0], BufferedImage.TYPE_INT_RGB, null);
            else if (format.equalsIgnoreCase("wbmp"))
                 newImage = convert(frames[0], BufferedImage.TYPE_BYTE_BINARY, null);
//            else if (format.equalsIgnoreCase("pcx"))
 //           	 newImage = convert(frames[0], BufferedImage.TYPE_INT_RGB, new ColorModel(ICC_ProfileRGB));
  
            ImageIO.write(newImage, format, out);
        }
        out.flush();       
    }
    
    /**
     * @param node
     * @param attributeName - name of child node to return
     * @return attribute node or null if not found
     */
    static Node getAttributeByName(Node node, String attributeName)
    {
        if (node == null) return null;
        NamedNodeMap nnm = node.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++)
        {
            Node n = nnm.item(i);
            if (n.getNodeName().equals(attributeName))  return n;
        }
        return null; // no such attribute was found
        
    } //end Node getAttributeByName(Node node, String attributeName)
    
    /** Method to create an icon for a file
     *
     * @param size Size to scale icon (or null if to use original size).
     * @return The ImageIcon object
     */
    public ImageIcon getIcon(Dimension size)
    {
        if (!loadImages()) return null;

        if (imageIcon!=null && imageIcon.getIconWidth()==size.width 
                            && imageIcon.getIconHeight()==size.height)
        {
            return imageIcon;
        }

        BufferedImage scaledBI = frames[0];
        if (size!=null)
        {
            scaledBI = makeNewImage(new Dimension(size.width, size.height));
            Graphics2D graphics = scaledBI.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                         , RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            graphics.drawImage(frames[0], 0, 0, size.width, size.height, null);
            graphics.dispose();
        }
        if (scaledBI==null) return null;
        imageIcon = new ImageIcon(scaledBI); 
        return imageIcon;
    }
    
    /** Convert BufferedImage type
     *     
     * @param source BufferedImage to convert
     * @param type desired type
     * @return Converted BufferedImage object
     */
    private BufferedImage convert(BufferedImage source, int type, ColorModel cm)
    {
        if (source.getType() == type) return source;
        
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage newImage;
        if (cm!=null)
        {
           newImage = new BufferedImage(width, height, type);
        }
        else
           newImage = new BufferedImage(width, height, type);
        Graphics2D graphics = newImage.createGraphics();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0,0,width, height);
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return newImage;
    }
    
    /** Create a new buffered image for scaling, rotating, etc
     * 
     * @return blank image sized to the correct width and height
     */
    private BufferedImage makeNewImage(Dimension size)
    {
        // Create a BufferedImage that can use the graphics accelerator
        GraphicsEnvironment ge 
                            = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc 
                        = ge.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage frame = gc.createCompatibleImage
                        (size.width, size.height, BufferedImage.TYPE_INT_ARGB); 
        Graphics2D graphics = frame.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                                , RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0,0,size.width, size.height);
        graphics.setBackground(TRANS);
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.dispose();
        return frame;       
    }
    /** Listener for time object to keep drawing the animated group of frames
     * 
     * @param ae AcrionEvent object triggering this listener
     */
    public void actionPerformed(ActionEvent ae)   
    {  
        if (timer==null) return;
        if (panel==null)
        { stopDisplayLoop(); return; }
        
        Container ancestor = panel.getTopLevelAncestor();
        if (ancestor==null)
        { stopDisplayLoop(); return; }
        
        panel.repaint();
    }
    
   /** Make an identical copy of this object
    * 
    * @return identical copy of this object
    */
   public  PictureData clone()
   {
       try
       {   PictureData newObject = (PictureData)super.clone();
           newObject.data = data.clone();
           newObject.text = new Vector<String>();
           for (String temp: text)
           {
        	   newObject.text.add(temp);
           }
           
           // Clear the temporary variables
           newObject.panel     = null;
           newObject.startTime = -1; 
           newObject.endTime   = -1; 
           newObject.timer     = null; 
           newObject.image     = null;
           newObject.lastFrame = -1;
           return (PictureData)newObject; 
       }
       catch (Exception e) 
       {  
      	  Frame frame = JOptionPane.getRootFrame();
      	  JOptionPane.showMessageDialog(frame, "Couldn't clone PictureData");   
       }
       return null;
   }

   /** Method to compare two picture data objects
    *
    * @param o The other object to compare
    * @return true if equal, false otherwise
    *
    *  Two PictureData objects are considered equal if the name
    *  of the pictures (text.get(0), scale, angle,
    *          and the byte length of the pictures match
    */
   public boolean equals(Object o)
   {
       PictureData picture = (PictureData)o;
       if (!text.get(0).equals(picture.text.get(0))) return false;
       if (data.length != picture.data.length) return false;
       if (scale != picture.scale) return false;
       if (angle != picture.angle) return false;
       return true;
   }
   
   public int getPictureSize() 
   {
	   if (data!=null) return data.length;
	   else return 0;
   }
   
   /** Compute hash code for use with generic collections */
   public @Override int hashCode() 
   {
	      int PRIME = 31;
	      int result = 1;
	      
	      result = PRIME * result + scale;
	      result *= PRIME * result + angle;
          if (data!=null) result *= PRIME * result + data.length;
          if (text!=null && !text.isEmpty()) result *= PRIME * result + text.get(0).hashCode();
       return result;
    }
   
    /** Determine if an extension goes with a picture
    *
    * @param extension extension of file to check
    * @return true if an image file, false otherwise.
    */
    public static boolean isImage(String extension)
    {
       String[] imageArray = ImageIO.getReaderFormatNames();
       for (int i=0; i<imageArray.length; i++)
          if (extension.equals(imageArray[i])) return true;

       return false;
    }
    
    private static BufferedImage recoverImageRaster(InputStream stream, String extension) throws IOException
    {
        Iterator<ImageReader> imageReaders = 
            ImageIO.getImageReadersBySuffix(extension);
        ImageReader imageReader = imageReaders.next();
        ImageInputStream iis = 
            ImageIO.createImageInputStream(stream);
        imageReader.setInput(iis, true, true);
        Raster raster = imageReader.readRaster(0, null);
        int w = raster.getWidth();
        int h = raster.getHeight();

        BufferedImage result = 
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int rgb[] = new int[3];
        int pixel[] = new int[3];
        for (int x=0; x<w; x++)
        {
            for (int y=0; y<h; y++)
            {
                raster.getPixel(x, y, pixel);
                int Y = pixel[0];
                int CR = pixel[1];
                int CB = pixel[2];
                toRGB(Y, CB, CR, rgb);
                int r = rgb[0];
                int g = rgb[1];
                int b = rgb[2];
                int bgr = 
                    ((b & 0xFF) << 16) | 
                    ((g & 0xFF) <<  8) | 
                     (r & 0xFF);
                result.setRGB(x, y, bgr);
            }
        }
        return result;
    }

    // Based on http://www.equasys.de/colorconversion.html
    private static void toRGB(int y, int cb, int cr, int rgb[])
    {
        float Y = y / 255.0f;
        float Cb = (cb-128) / 255.0f;
        float Cr = (cr-128) / 255.0f;

        float R = Y + 1.4f * Cr;
        float G = Y -0.343f * Cb - 0.711f * Cr;
        float B = Y + 1.765f * Cb;

        R = Math.min(1.0f, Math.max(0.0f, R));
        G = Math.min(1.0f, Math.max(0.0f, G));
        B = Math.min(1.0f, Math.max(0.0f, B));

        int r = (int)(R * 255);
        int g = (int)(G * 255);
        int b = (int)(B * 255);

        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
    }

}   // End of PictureData class