/*
 * ColorScheme.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.visual;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import org.acorns.data.*;
import org.acorns.language.*;

/** Maintain a number of visual components used in various lessons. 
 *
 *  <pre>
 * The components maintained are:
 *      Foreground and background colors of text
 *      A background wallpaper picture
 *      Font size of text to display
 * </pre>
 */
public class ColorScheme implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1;
    
    /** Minimum font size for text display */
    public final static int MIN_FONT_SIZE=8;
    /** Maximum font size for text display */
    public final static int MAX_FONT_SIZE=40;
    
    private Color  background;       // Color for background
    private Color  foreground;       // Foreground font color
    private PictureData picture;     // Byte stream for picture
    private int    fontSize;         // Make non-transient lateer
    
  /** Standard Constructor
   * 
   * @param background background color
   * @param foreground foreground color
   */
    public ColorScheme(Color background, Color foreground)
    {  
       if (background==null) background = new Color(200,200,200);
       if (foreground==null) foreground = Color.BLACK;
       
       this.foreground = foreground;
       this.background = background;
       this.fontSize   = 20;      
    }
	
 /** Constructor from version 3.0 conversion
  * 
  * @param background background color
  * @param foreground foreground color
  * @param bytes byte array for background picture
  * @param angle background picture rotate angle
  * @param fontSize size of font for graphics writing
  */
  public ColorScheme(Color background, Color foreground
                      , byte[] bytes, int angle, int fontSize)
  {
      this.background = background;
      this.foreground = foreground;
      this.fontSize   = fontSize;
      
      if (bytes!=null)
      {   try
          {   picture = new PictureData(bytes, null);
              picture.setAngle(angle);
          }
          catch (IOException io) { picture = null; }
       }
  }
    
    /** Method to set either the font or background color
     *  @param  setBackground true if get background color, false for font
     *  @return true if a new color was selected
     */
    public boolean setColor(boolean setBackground)
    {  return setColor(null, setBackground); }

    /** Method to set either the font or background color
     *  @param  setBackground true if get background color, false for font
     *  @param  parent parent component
     *  @return true if a new color was selected
     */
    public boolean setColor(Component parent, boolean setBackground)
    {
        Color color;
        String[] chooseText = LanguageText.getMessageList("commonHelpSets", 69);
        String title = chooseText[0];
        
        if (setBackground) 
        {  title = chooseText[1];
           color = background;
        }
        else            
        {   title = chooseText[2];
            color = foreground;
        }

        color = JColorChooser.showDialog(parent, title, color);
        if (color!=null)
        {  if (setBackground) background = color;
           else               foreground = color;
        }
        return (color!=null);        
    }
    
    /** Method to change the foreground and background colors
     *  @param color new color
     *  @param setBackground true if color is for background, false otherwise
     */
    public void changeColor(Color color, boolean setBackground)
    {  if (setBackground) { if (color!=null) background = color; }
       else               { if (color!=null) foreground = color;   }    
    }
    
    /** Method to get foreground and background colors
     *  @param  getBackground true if get background color, false for font
     *  @return background or foreground color
     */
    public Color getColor(boolean getBackground)
    {
        if (getBackground) return background;
        else               return foreground;        
    }
           
    /** Method to get the font size */
    public int getSize()  {   return fontSize; }
    
    /** Method to set the font size
     *
     * @param size Desired font size
     */
    public void setSize(int size) { fontSize = size; }
    
    public BufferedImage getScaledPicture(Dimension size)
    {
        if (size == null) return null;
        if (picture==null) return null;
        
        Dimension pictureSize = picture.getSize();
        
        double scaleX = 1.0 * size.width / pictureSize.width;
        double scaleY = 1.0 * size.height / pictureSize.height;
        double  scale = scaleX;
        if (scaleX > scaleY) scale = scaleY;
        
        Dimension newSize = new Dimension();
        newSize.width  = (int)(pictureSize.width * scale);
        newSize.height = (int)(pictureSize.height * scale); 
        return getPicture(newSize);
    }
    
   /** Method to return the wallpaper picture associated with this lesson
    *  @param size desired size of picture
    *  @return BufferedImage of picture or null
    */
   public BufferedImage getPicture(Dimension size) 
   {   
       if (picture==null) return null;

       Rectangle spot = new Rectangle(0, 0, size.width, size.height);
       return picture.getImage(null, spot);
   }   // End of getPicture()

    /** Method to set a wallpaper picture
     *  @param picture to use as the wallpaper, or null to reset
     */
    public void setPicture(PictureData picture) throws IOException
    {
        this.picture = picture;
    }
    
    /** Method to return the PictureDataObject of the wallpaper picture
     * 
     * @return PictureData object or null if it doesn't exist
     */
    public PictureData getPicture()   {  return picture; }
    
    /** Create clone of the color scheme object */
    public @Override ColorScheme clone()
    {
       try
       {
           ColorScheme newObject = (ColorScheme)super.clone();
           if (picture!=null) newObject.setPicture(picture.clone());
           return newObject;
       }
       catch (Exception e)
       {  
      	   Frame frame = JOptionPane.getRootFrame();
    	   JOptionPane.showMessageDialog(frame, "Couldn't clone ColorScheme"); 
       }
       return null;
    }
       
}     // End of ColorScheme class
