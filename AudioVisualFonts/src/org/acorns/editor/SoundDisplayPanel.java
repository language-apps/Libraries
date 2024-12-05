/*
 * DisplayPanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.editor;

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.audio.*;
import org.acorns.language.*;

/** Abstract class for panels displaying sounds and annotations.
 *
 *  Used by the lessons that display a SoundEditor panel.
 *  The only method used by lessons is drawSound(). The rest are
 *     used by internal Sound Editor modules.
 */
public class SoundDisplayPanel  extends JPanel   
        implements PropertyChangeListener, MouseMotionListener
                                         , MouseListener, ComponentListener
{
    private static final int MAXZOOM   = 32; // Maximum zoom ration 
    private static final int PERCENT   = 60; // Annotation sound panel percent
    
    private static final long serialVersionUID = 1;
    
    /** Display sound in the dime domain. */
    public final static int TIME = 0;
    /** Display sound frequencies as a spectrograph. */
    public final static int SPECTROGRAPH = 1;
    /** Display the frequency domain of part of the sound wave */
    public final static int FFT = 2;
    /** Display the Annotations with the Sound */
    public final static int ANNOTATE = 3;
    /** Display the feature vector for a recording */
    public final static int FEATURE = 4;
    /** Display the picture annotations for a recording */
    public final static int IMAGE = 5;
    /** Display the CEPSTRAL distance between frames. */
    public final static int DISTANCE = 6;
    
    /* Repaint options: bit 0 means entire scrren; bit 1 means playback */
    private int             startSelect; // The user selected area.
    private int             endSelect; 
    private boolean         enabled;     // true if we can repaint
    private int             type;        // Type of sound display.
    private ColorScheme     colors;      // Color scheme for annotations.
    
    private SoundImage      image;       // Object to draw sounds.
    private RootSoundPanel  soundPanelProperties;
    private SoundPanel      soundPanel;
    private JLabel          label;
    
    private Point           playPoint;   // x,y = current,last playback point
    private Rectangle       playSpot;    // Visible bounds calling paintComponent
    private boolean         all;         // Repaint all flag.

    /** Constructor to create a panel to display audio time domain data
     *
     * @param soundPanel The panel to display  the audio sound wave
     * @param label A label to display error messages
     * @param colors Background and foreground colors
     */
    public SoundDisplayPanel
            (SoundPanel soundPanel, JLabel label, ColorScheme colors)
    {
        startSelect = endSelect = -1;
        playPoint   = new Point(-1, -1);
        playSpot    = new Rectangle(0,0,1,1);
        type        = TIME;
        all         = true;

        this.soundPanel = soundPanel;
        this.label      = label;
        this.colors     = colors;

        image = new SoundImage();
        
        setBackground(SoundDefaults.DARKBACKGROUND);
        addListeners();
        
        // Get the ACORNS property change listener.
        PropertyChangeListener[] pcl 
              = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                               ("SoundListeners");
        if (pcl.length>0) soundPanelProperties = (RootSoundPanel)pcl[0];
    }
    
    private void addListeners()
    {
        addPropertyChangeListener("PlayBack", this);
        addMouseMotionListener(this);
        addMouseListener(this);

        new AudioDropTarget(this, this);
    }
    
    /** Override to draw the sound data object.
     *  @param graphics The graphics object for drawing to this panel.
     */
    public @Override void paintComponent(Graphics graphics)
    { 
        if (!enabled) return;
        super.paintComponent(graphics);

        Rectangle visible = getVisibleRect();

        Dimension visibleDisplay = new Dimension(visible.width, visible.height);
        BufferedImage picture = colors.getScaledPicture(visibleDisplay);
        if (picture!=null && ("bia".indexOf(label.getName())>=0))
        {
            int xOffset = (visible.width - picture.getWidth())/2;
            int yOffset = (visible.height - picture.getHeight())/2;
            graphics.drawImage(picture, visible.x + xOffset, visible.y + yOffset
                             , picture.getWidth(), picture.getHeight(), null);
        }

        // Verify that the size of the panel agrees with the zoom factor
        int zoom = SoundDefaults.getZoom();
        Dimension zoomSize = new Dimension(zoom*visible.width, visible.height);
        if (zoomSize.width<visible.width) zoomSize.width = visible.width;
        
        if (!getSize().equals(zoomSize))
        {
           setSize(zoomSize);
           setPreferredSize(zoomSize);           
        }
    
        if (playPoint.x!=-1 && all!=true) 
        {
            visible = playSpot;
            drawSoundWave(graphics, visible);
            playPoint.x = -1;
            playSpot.x = 0;
        }
        else drawSoundWave(graphics, visible);
 
        all = false;
        
        if (type == ANNOTATE)
        {  
           Border border = BorderFactory.createRaisedBevelBorder();
           border.paintBorder(this, graphics,
           visible.x, visible.height*PERCENT/100, visible.width, 5);

           if (startSelect==endSelect && startSelect>=0)
           {
               JTextField nativeText = soundPanel.getNativeText();
               Annotations annotations
                   = soundPanelProperties.getAnnotationData(soundPanel);
               Point  select = getSelectedFrames(annotations);
               AnnotationData annotationData = (AnnotationData)annotations;
               String text = annotationData.getString(select);
               nativeText.setText(text);

           }
        }
 
        // Display the selected area (only for displays other than FFT
        if (type!=FFT)
        {  
           Point spot = getSelectedPixels();
           
           if (spot != null)
           {  graphics.setColor(SoundDefaults.SELECT);
              if (spot.x==spot.y)
                   graphics.drawLine(spot.x,0,spot.x,zoomSize.height);
              else 
              {
                  // Only draw what is visible or changed from last playback
                  int startSpot = spot.x;
                  int lastSpot = spot.y-spot.x;
                  if (startSpot<visible.x) 
                  {   lastSpot  -= (visible.x-startSpot); 
                      startSpot  = visible.x;
                  }
                  if (startSpot + lastSpot - visible.x > visible.width)
                  { lastSpot = visible.x + visible.width - startSpot; }
                  graphics.fillRect(spot.x, 0, spot.y-spot.x, zoomSize.height);
              }
           }
           
           // Draw the playback line
           if (playPoint.y>0)  
           {   graphics.setColor(SoundDefaults.LINE);
               graphics.drawLine(playPoint.y, 0, playPoint.y, zoomSize.height);  
           }
        }
        
      }  // End PaintComponent();

   /** Method to handle user drag & drop of audio file
    *
    * @param audio The dropped file object (pass it to the SoundListener object
    */
   public void audioDropped(File audio)
   {  
	  soundPanel.getListener().audioDropped(audio);
      repaint();
   }

    /******************* Supporting Methods **********************************/    
    /** Method to set the type of sound display.
     * @param type FFT, SPECTROGRAPH, or TIME
     */
    public void setDisplayType(int type) 
    { 
        this.type = type;
        repaintAll();
    }

    /** Select a portion of the sound wave
     *  @param annotations sound wave object with annotations
     *  @param front If true, select the front part of the sound
     *  @param back  If true, select the back part of the sound
     */
    public boolean selectWave
            (Annotations annotations, boolean front, boolean back)
    {
        SoundData sound = annotations.getSound();
        if (sound==null||sound.getFrames()==0||type==FFT)    return false;
        
        if (front && back) 
        {  startSelect = 0; 
           endSelect   = sound.getFrames();
        }
        else if (front)
        {   if (endSelect == -1)     return false;
            startSelect=0;
        }
        else if (back)
        {   if (startSelect == -1) return false;
            endSelect = sound.getFrames();
        }
        repaintAll();
        return true;
    }
    
        
    /** Method to display a sound object and leave zoom alone or reset it.
     *  @param annotations sound wave object with annotations
     */
    public boolean drawSelect(Annotations annotations)
    {
        if (type == FFT) return false;
        
        SoundData sound = annotations.getSound();
        Point select = getSelectedFrames(annotations);
        if (select==null) return false;
        if (sound.getFrames()==0) return false;
        if (select.x >= select.y) return false;

        // Compute the zoom factor.
        int zoom = (select.y - select.x)/getWidth();
        if (zoom>MAXZOOM) zoom = MAXZOOM;
        if (zoom<1)    zoom = 1;
        SoundDefaults.setZoom(zoom);
        
        Rectangle visible = getVisibleRect();
        Dimension zoomSize = new Dimension(zoom*visible.width, visible.height);
        if (zoomSize.width<visible.width) zoomSize.width = visible.width;
        Point spot = getSelectedPixels();
        if (spot!=null)
        {   scrollRectToVisible
              (new Rectangle(spot.x, 0, spot.y-spot.x, zoomSize.height)); 
        }     
        repaintAll();
        return true;
    }
    
    /** Method to display a sound object and leave zoom alone or reset it.
     *  @param annotations Sound wave object with annotations
     *  @param reset Use current zoom (false) or reset to the default (true)
     */
    public boolean drawSound(Annotations annotations, boolean reset)
    {
        soundPanelProperties.setAnnotationData(annotations, soundPanel);
        if (reset) 
        {
            //startSelect = endSelect = -1;
        }
        repaintAll();
        return true;
    }
    
    /** Method to display a sound data graphics object 
     *
     *  @param annotations Sound wave object with annotations
     *  @param factor Multiplier to compute a new current zoom
     */
    public boolean drawSound(Annotations annotations, double factor)
    {
        int zoom = SoundDefaults.getZoom();
        if (zoom==1 && factor<1)       return false;
        if (zoom==MAXZOOM && factor>1) return false;
        if (factor==0)                 return false;
  
        zoom = (int)(zoom * factor);
        if (zoom<1)       zoom = 1;
        if (zoom>MAXZOOM) zoom = MAXZOOM;
        SoundDefaults.setZoom(zoom);
        repaintAll();
        return true;        
    }   // End drawSound()
       
    
    private boolean drawSoundWave(Graphics graphics, Rectangle visible)
    {   
        if (soundPanelProperties==null) return false;
     
        Annotations annotations 
                = soundPanelProperties.getAnnotationData(soundPanel);
        SoundData sound = annotations.getSound();
        Rectangle clip    = new Rectangle(visible);
        if (type==ANNOTATE || type==IMAGE) 
        {
            clip.height = visible.height*PERCENT/100;
        }
        
        int width = getVisibleRect().width * SoundDefaults.getZoom();
        
        // Display in the appropriate format.
        try
        { 
            switch (type)
            {
                case TIME: 
                    image.drawSound
                            (graphics, annotations, width, clip, false);
                    break;                
                case FFT:
                     if (!image.drawFFT(graphics, annotations
                             , width, clip,  getSelectedFrames(annotations)))
                     {
                         String noSelect
                                 = LanguageText.getMessage("soundEditor", 91);
                         String noDisp
                                 = LanguageText.getMessage("soundEditor", 84);
                         label.setText(noDisp + "; " + noSelect);
                     }
                   break;                  
                case SPECTROGRAPH:
                   image.drawSound(graphics, annotations, width, clip, true);
                   break; 
                case FEATURE:
                    image.drawFeatureVector(graphics, annotations, width, clip);
                    break;
                case DISTANCE:
                	image.drawFrameDistances(graphics, annotations, width, clip);
                	break;
                case ANNOTATE:
                    image.drawSound(graphics, annotations, width, clip, false);
                 
                    clip.y += clip.height;
                    clip.height = visible.height - clip.height;
                    AnnotationImage annotate = new AnnotationImage();
                    annotate.drawAnnotation(graphics, (AnnotationData)sound
                         , width, clip ,true,1,colors);
                 break;
                 
                case IMAGE:
                    image.drawSound(graphics, annotations, width, clip, false);
                    
                    clip.y += clip.height;
                    clip.height = visible.height - clip.height;
                    PictureAnnotationImage pictures 
                            = new PictureAnnotationImage();
                    pictures.drawAnnotation(graphics, annotations
                                       , width, clip , this, colors);
                    break;
            }
        }
        catch (Exception e)    
        {   if (label!=null) label.setText(e.toString()); }
        return true;        
    }   // End of drawSoundWave()
    
    
    /** Scroll to front of panel.
     *  @param sound wave object
     *  @return true if success, false otherwise
     */
    public boolean scrollToFront(SoundData sound)
    {
        if (sound==null || sound.getFrames()==0) return false;

        scrollRectToVisible(new Rectangle(0,0,1,getHeight()));
        return true;
    }
    
    /** Scroll to end of panel.
     *  @param sound wave object
     */
    public boolean scrollToBack(SoundData sound)
    {
        if (sound==null || sound.getFrames()==0) return false;
        
        scrollRectToVisible(new Rectangle(getWidth()-1,0,1, getHeight()));
        return true;
    }
    
    /** Get the current user selected area.
     *  @param annotations sound wave object with annotations
     *  @return point Where x value is the start and y value is the end
     *  
     *  Note: Null returns if there is nothing selected.
     */
   public Point getSelectedFrames(Annotations annotations)
   {
       SoundData sound = annotations.getSound();
       Point point = getSelectedPixels();
       if (point==null) return null;
   
       int frames = sound.getFrames();
       float ratio = (float)point.x / getWidth();

       SoundEditor editor = annotations.getSoundEditor();
       if (point.x!=0) point.x = editor.roundToZero((int)(frames*ratio));
       ratio = (float)point.y /getWidth();
       if (point.y!=0) point.y = editor.roundToZero((int)(frames*ratio));
       return point;      
   }
   
   /** Method to reset the current selection */
   public void resetSelection()
   {
       startSelect = endSelect = -1;
       repaintAll();
   }
    
    /** Get the current user selected area.
     *  @return point where x value is the start and y value is the end
     *  
     *  Note: Returns null if there is nothing selected.
     */
    public Point getSelectedPixels()
    {
         int startSelection = startSelect, endSelection = endSelect;
        
         if (startSelect==-1 || endSelect==-1)  { return null; }
         if (startSelect > endSelect) 
         {   endSelection   = startSelect;
             startSelection = endSelect;
         }
         int zoom = SoundDefaults.getZoom();
         startSelection = (int)Math.round(startSelection * zoom); 
         endSelection   = (int)Math.round(endSelection * zoom);
         return new Point(startSelection, endSelection);
    }
    
    /** Method to force entire page to display  */
    private void repaintAll()
    {  all = true;
       enabled = true;
       repaint();
    }
    
    /** Method to enable or disable repainting of this component
     * 
     * @param enable true if component can display, false otherwise
     */
    public void enableDisplay(boolean enable)
    {   this.enabled  = enable; }

    
    /** Listen for mouse clicks to clear the current sound selection.
     *
     * @param event The object thrown as a result of a mouse click
     */
   public void mouseClicked(MouseEvent event)
   {  if (type!=FFT)
      {  // Reset the current selection.
         if (startSelect != endSelect) startSelect = endSelect = -1;
         repaintAll();
      }
    }  // End of mouse clicked.


    /** Begin dragging a lesson to a new file position when the mouse is pressed
     *
     * @param event Object thrown in response to a mouse press operation
     */
   public void mousePressed(MouseEvent event)	
   {
       if (type!=FFT)
       {  int zoom = SoundDefaults.getZoom();
          if (zoom==0) 
          {   zoom = 1;
              SoundDefaults.setZoom(zoom);
          }

          if (startSelect<0) startSelect = event.getPoint().x / zoom;
	  endSelect = event.getPoint().x / zoom;
       }
   }
    
    /** Drop lesson at a lesson to a new file position when the mouse is released
     *
     * @param event Object thrown in response to a mouse press operation
     */
   public void mouseReleased(MouseEvent event)	
   {   if (type!=FFT)
       {  int zoom = SoundDefaults.getZoom();
          if (zoom==0) 
          {   zoom = 1;
              SoundDefaults.setZoom(zoom);
          }
          endSelect  = event.getPoint().x / zoom;
          repaintAll(); 
       }
   }
      
    /** Highlight selected area during a drag lesson operation
     *
     * @param event Object thrown in response to a mouse press operation
     */
     public void mouseDragged(MouseEvent event)
     { if (type!=FFT)
       {  Point mouseAt = event.getPoint();
          int zoom = SoundDefaults.getZoom();
          if (zoom==0) 
          {   zoom = 1;
              SoundDefaults.setZoom(zoom);
          }
	      endSelect = mouseAt.x / zoom;
	      Rectangle rectangle = new Rectangle(mouseAt.x, 0, 1, 1);
		     	 
	      scrollRectToVisible(rectangle);
	      repaintAll();
       }
     }
    
    /** Unused mouse over event  */
    public void mouseEntered(MouseEvent event)	{}

     /** Unused mouse exit event   */
     public void mouseExited(MouseEvent event)	{}
    
     /** Unused event triggered when the mouse moves  */
     public void mouseMoved(MouseEvent event)   {}
    
    /** Property Change Listener to draw vertical line during play back.
      *
      *  @param event property change "PlayBack" fired by the sound data object.
      */
     public void propertyChange(PropertyChangeEvent event)
     {  
         // return if not actively displaying time domain data.
         if (soundPanelProperties==null) return;
         if (type!=TIME && type!=ANNOTATE && type!=IMAGE) return;
         
         // Get data needed for the calculations.
         Annotations annotations 
                 = soundPanelProperties.getAnnotationData(soundPanel);
         SoundData sound = annotations.getSound();
         long frames = sound.getFrames();
         if (frames==0) return;
         int  width    = getWidth();
         
         // Get old and new property values and convert to pixel positions.
         String value = event.getOldValue().toString();
         long oldSpot  = Integer.parseInt(value);
         if (oldSpot != -1) oldSpot = width * oldSpot / frames;  
         
         value = event.getNewValue().toString();
         long newSpot  = Integer.parseInt(value);
         if (newSpot != -1) newSpot = width * newSpot / frames; 

         // Create point where last and current lines are.
         playPoint = new Point((int)oldSpot, (int)newSpot);

         // Compute update rectangle.
         int subFromFront = 1;
         int addToEnd     = 1;
         if (oldSpot < subFromFront) oldSpot = subFromFront;
         if (playSpot.x>=0) oldSpot = playSpot.x;
         if (newSpot == -1) newSpot = frames;
            
         Rectangle visible = getVisibleRect();
         playSpot = new Rectangle((int)oldSpot-subFromFront, 0, 
                                 (int)(newSpot-oldSpot+addToEnd+subFromFront), visible.height);
         playSpot = playSpot.intersection(visible);
         repaint(playSpot);
    }    
     
     /** Unused event triggered when the component becomes hidden  */
    public void componentHidden(ComponentEvent e) {}

    /** Reset selection when the component is mmoved  */
    public void componentMoved(ComponentEvent e)   
    { resetSelection(); repaintAll(); }

    /** Reset the selection when the component is resized */
    public void componentResized(ComponentEvent e)
    { resetSelection(); repaintAll(); }

    /** Unused event triggered when the mouse moves  */
    public void componentShown(ComponentEvent e)   {}
}

