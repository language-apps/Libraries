/*
 * SoundProperties.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/

package org.acorns.editor;

import java.beans.*;

import org.acorns.audio.SoundDefaults;

import java.awt.*;

public class SoundProperties implements PropertyChangeListener
{
    String paths;     // Paths for loading and saving sound files
    double[] clip;    // Time domain clipboard for manipulating sounds
    
    /** Creates a new instance of SoundProperties */
    public SoundProperties(String loadPath, String savePath)
    {
    	
        String paths = loadPath + ";" + savePath;
        setPaths(paths);
        
        this.clip  = null;

        String listener = "Properties";
        PropertyChangeListener[] pcl
            = Toolkit.getDefaultToolkit().getPropertyChangeListeners(listener);
        for (int i=0; i<pcl.length; i++)
        {   Toolkit.getDefaultToolkit().removePropertyChangeListener
                                                        (listener, pcl[i]); }
        // Add the new one.
        Toolkit.getDefaultToolkit().addPropertyChangeListener(listener, this);
    }
    
    public String getPaths() 
    { 
    	return paths; 
    }
    
    public void setPaths(String paths) 
    { 
    	String homeDir = SoundDefaults.getDataFolder();
    	if (paths.contentEquals(";"))
    	{
    		paths = homeDir +";" + homeDir;
    	}
    	this.paths = paths; 
    }
    
    public double[] getClip() { return clip; }
    public void setClip(double[] clip) {this.clip = clip;}
    
    public void propertyChange(PropertyChangeEvent event)  {}
}
