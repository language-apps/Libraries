/**
 * ImageView.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.visual;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.image.*;

public class ImageView extends FileView
{
	   Hashtable<String, ImageIcon>hash;
	   ImageIcon missing;
   
    public static final int ICON = 20;
    
    public ImageView()   { 	hash = new Hashtable<String, ImageIcon>(); }
    public @Override Icon getIcon(File file)
    {
       if (getExtension(file) == null) return null;
       return checkFile(file);
    }
    
    /** Get the extension for this file name 
     * 
     * @param file The file in question
     * @return The extension
     */
    private String getExtension(File file)
    {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        if (lastIndex<0 || lastIndex>= name.length()-1) return null;
        return name.substring(lastIndex+1).toLowerCase();
    }    
    
    	/** See if file created. 
    	 *  @param file - Icon file to check
         */
        public Icon checkFile(File file)
        {
            try
            {
	       String path = file.getCanonicalPath();
	       if (hash.containsKey(path)) 
	       { 
	          return (ImageIcon)hash.get(path); 
	       }

               ImageIcon icon = new ImageIcon(path, path);
	       ImageIcon thumbnail = null;
	       if (icon!=null) 
	       {  thumbnail 
                  = new ImageIcon(getScaledImage(icon.getImage(), ICON, ICON));
		          }
   			      hash.put(path, thumbnail);
			         return thumbnail;
         }
         catch (Exception e) { return null; }
		  }    
      
     /**
       * Resizes an image using a Graphics2D object backed by a BufferedImage.
       * @param srcImg - source image to scale
       * @param w - desired width
       * @param h - desired height
       * @return - the new resized image
       */
      private Image getScaledImage(Image srcImg, int w, int h)
	   {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }
      
}       // End of ImageView class
