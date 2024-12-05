/**
 * MovieData.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.data;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.*;
import javax.media.bean.playerbean.*;

import org.acorns.language.*;

/** This class contains the data for MPEG video */
public class MovieData 
        implements Serializable, Cloneable, ControllerListener, WindowListener
{
    /** Java file's serial version number */
    private static final long serialVersionUID=1L;
    private static final int MAX_WAIT = 20;
    
    private File file;
    private static MediaPlayer mediaPlayer;
    private static JDialog mediaPanel;
    private transient String realized;
    
    /** Constructor to create a MovieData object
     *
     * @param file The File object of the video file
     * @throws FileNotFoundException
     */
    public MovieData(File  file) throws FileNotFoundException
    {  reset(file);   }

    /** Constructor to initialize the object without a video */
    public MovieData()  { file = null; }

    /** Get a media player for this movie
     * 
     * @return The mediaPlayer
     */
    public MediaPlayer getMediaPlayer()
    {  if (mediaPlayer==null)
       {  mediaPlayer = new MediaPlayer();
          mediaPlayer.setZoomTo("Scale 4:1");

          mediaPlayer.setPlaybackLoop ( false );
          mediaPlayer.setFixedAspectRatio ( true );
          mediaPlayer.setPopupActive ( false );
          mediaPlayer.setControlPanelVisible ( true );
          mediaPlayer.addControllerListener ( this );

          if (mediaPanel==null)
          {  mediaPanel = new JDialog();
             mediaPanel.addWindowListener(this);
          }
       }
       return mediaPlayer;
    }

    /** Method to determine if the dialog panel is active */
    public boolean isVisible()
    {   return mediaPanel!=null && mediaPanel.isVisible();  }
    
    /** Method to prepare video display of current url */
    public void reset()  {  reset(file);  }

    /** Method to alter the MediaPlayer URL
     *
     * @param file The video file object
     */
    public final void reset(File file)
    {  this.file = file;
       MediaPlayer player = getMediaPlayer();
       player.stop();
       MediaLocator mediaLocator = null;
       try
       { if (file!=null) mediaLocator
                 = new MediaLocator("file:" + file.getCanonicalPath());
       }
       catch (Exception e) 
       { 
    	   Frame frame = JOptionPane.getRootFrame();
    	   JOptionPane.showMessageDialog(frame, e.toString()); 
       }
       mediaPlayer.setMediaLocator( mediaLocator);
    }

    /** Get panel ready to play the video */
    public JDialog playVideo()
                      throws FileNotFoundException, NoPlayerException
    {  MediaPlayer player = getMediaPlayer();
       player.stop();

       // wait till player realized (fail if it never does).
       mediaPlayer.realize();
       if (!wait(false))   {   throw new NoPlayerException();   }

       // wait till visual component available (error if never does)
       if (!wait(true))
       {   throw new NoPlayerException
                   (LanguageText.getMessage("acornsApplication", 149));  }

       // Error if illegal response from controller listener
       if (realized.length()>0) { throw new NoPlayerException(realized); }

       mediaPlayer.prefetch ();

       try {  Thread.sleep(500);}
       catch (InterruptedException ie) { }

       mediaPlayer.stop();
       mediaPlayer.setMediaTime(new Time(0));

       Container container = mediaPanel.getContentPane();
       container.removeAll();
       container.add(mediaPlayer);
       mediaPanel.pack();
       mediaPanel.setVisible(true);
       mediaPanel.setLocationRelativeTo(null);
       return mediaPanel;
    }

     /** Get the File for this movie object
     * 
     * @return FileL object
     */
    public File getFile()  { return file; }
    
    /** Method to stop the playback of a movie clip */
    public void stop() { if (mediaPlayer!=null) mediaPlayer.stop(); }
    

    /** Method to listen to controller transition events   */
    public void controllerUpdate(ControllerEvent evt)
    {
        if (evt instanceof ConfigureCompleteEvent)	{}
        else if (evt instanceof PrefetchCompleteEvent) { mediaPlayer.start(); }
        else if (evt instanceof RealizeCompleteEvent)  { realized = ""; }
        else if (evt instanceof ResourceUnavailableEvent)
        {  realized = LanguageText.getMessage("acornsApplication", 150); }
	else if (evt instanceof ControllerErrorEvent)
	{ 	realized = LanguageText.getMessage("acornsApplication", 149); }

    }  // End of controllerUpdate

   
    /** Method to wait for mediaPlayer to get into right state
     *
     * @param visual waiting for visual component: true, realized otherwise
     * @return
     */
    private boolean wait(boolean visual)
    {   int max = MAX_WAIT, delta = 1000;
        if (visual)  { max = MAX_WAIT*100;  delta = 10; }
        for (int i=0; i<max; i++)
        {   try {  Thread.sleep(delta);}
            catch (InterruptedException ie) { return false; }

            Component visualComponent = mediaPlayer.getVisualComponent();
            if (visualComponent!=null && visual && visualComponent.isVisible())
                return true;
            if (!visual && realized!=null) return true;
        }
        return false;
    }
   
    /** Make an identical copy of this object 
     * 
     * @return The cloned Link object
     */
    public @Override MovieData clone()
    {   try
       {   MovieData newObject = (MovieData)super.clone();
           return (MovieData)newObject; 
       }
       catch (Exception e) 
       { JOptionPane.showMessageDialog
                 (null, "Could not clone MovieData object"); }
       return null;
    }

    /** Unused window event method */
    public void windowActivated(WindowEvent e) {}
    /** Unused window event method */
    public void windowClosed(WindowEvent e) {}
    /** Stop any video playback when the window closes */
    public void windowClosing(WindowEvent e)  { stop(); }
    /** Unused window event method */
    public void windowDeactivated(WindowEvent e) {}
    /** Unused window event method */
    public void windowDeiconified(WindowEvent e) {}
    /** Unused window event method */
    public void windowIconified(WindowEvent e) {}
    /** Unused window event method */
    public void windowOpened(WindowEvent e) {}

}   // End of MovieData class
