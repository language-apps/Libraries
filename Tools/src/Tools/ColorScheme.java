/*
 * ColorScheme.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package Tools;

import java.awt.*;
import java.io.*;

public class ColorScheme implements Serializable
{
    private static final long serialVersionUID = 1;
    
    private Color  background;       // Color for background
    private Color  foreground;       // Foreground font color
    private byte[] bytes;            // Byte stream for picture
    private int    angle;            // Angle picture is rotated    
    private int    fontSize;         // Make non-transient lateer
   
	public org.acorns.visual.ColorScheme convert(float version)
	{
		org.acorns.visual.ColorScheme scheme 
		    = new org.acorns.visual.ColorScheme(foreground, background, bytes, angle, fontSize);
		return scheme;
	}
 
}     // End of ColorScheme class
