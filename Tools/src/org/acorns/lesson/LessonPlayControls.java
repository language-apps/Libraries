/*
 * LessonPlayControls.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.help.*;

import org.acorns.data.*;
import org.acorns.language.*;

/** Create a panel of buttons that appears at the bottom of
 *        executing lessons.
 */
public class LessonPlayControls extends JPanel 
        implements ActionListener, FocusListener
{
     private static final long serialVersionUID=1L;

     private LessonPopupMenu  popup;     // Popup menu when info button clicked
     private JLabel           layerName; // Name of layer to display
     private JButton[]        buttons;   // Table of control JButtons
     private Lesson           lesson;    // Lesson that is in play mode
     private String           target;    // The name of the help page
     private static SoundData sound;     // Object for recording sounds
     private URL 			  backURL;   // Back URL for help back button
     private boolean          recordEnabled; // True if audio recording enabled; false otherwise
     
     private final int UP=0, DOWN=1, LINK=3, INFO=5
                           , FIRST=7, PREV=8, NEXT=9, LAST=10
                           , RECORD=12, PLAY=13, STOP=14, HELP=16;

     private final static int HEIGHT = 25;
     
     String[] playControls = LanguageText.getMessageList("commonHelpSets", 26);
     private final String[] text = 
          {"", "", null
             , "", null, playControls[0], null
             , playControls[1], playControls[2], playControls[3]
             , playControls[4], "", playControls[5], playControls[6]
             , playControls[7], "", playControls[8]};
              
     private final int icons[] = 
           {  AcornsProperties.UP, AcornsProperties.DOWN, 
              -1, AcornsProperties.ANCHOR, -1, 
              AcornsProperties.INFO, -1,  AcornsProperties.FIRST, 
              AcornsProperties.PREVIOUS, AcornsProperties.NEXT, 
              AcornsProperties.LAST, -1, AcornsProperties.RECORD,
              AcornsProperties.PLAY, AcornsProperties.STOP,
              -1, AcornsProperties.HELP
           };

     /** Constructor to create control button for playing lessons
      *  @param lesson current lesson
      *  @param popup Popup menu object
      *  @param target Name of help target
      */
     public LessonPlayControls
             (Lesson lesson, LessonPopupMenu popup, String target)
     {
        this.lesson = lesson;
        this.popup  = popup;
        this.target = target;
        
        recordEnabled = true;
        backURL = null;
        text[0] = adjustLayerTip(true);
        text[1] = adjustLayerTip(false);
        if (lesson.getLink().length()!=0) text[3] = playControls[9];
         
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
      
        // Create the buttons and their spacing.
        buttons = new JButton[text.length];        
        Dimension spacer = new Dimension(10,0);
        ImageIcon icon;
        layerName = new JLabel();
        layerName.setBackground(new Color(0,0,0));
        layerName.setForeground(new Color(255,255,255));
        layerName.setOpaque(true);
        layerName.setFont(new Font(null, Font.PLAIN, 20));
        setLayerName();
        add (layerName);

        for (int i=0; i<icons.length; i++)
        {
           if (icons[i]<0) {  add (Box.createRigidArea(spacer));  }
           else
           { 
              if (text[i].length()!=0)
              {
                 icon = lesson.getIcon(icons[i], HEIGHT);
                 buttons[i] = new ToolTipButton(icon, text[i]);
                 buttons[i].setOpaque(false);
                 buttons[i].setMargin(new Insets(0,0,0,0));
                 if (icons[i]==AcornsProperties.RECORD)
                 {
              	   buttons[i].setPreferredSize(new Dimension(HEIGHT+10, HEIGHT));
            	   buttons[i].setOpaque(true);
                 }

                 try
                 {   if (!recordEnabled && (i==RECORD || i==PLAY || i==STOP)) 
                	 	continue;
                	 	
                	 if (i==HELP)
                     {
                          HelpSet helpSet = lesson.getHelpSet();
                          CSH.setHelpIDString(buttons[i], target);
                          buttons[i].addActionListener(
                              new CSH.DisplayHelpFromFocus(helpSet
                                  , "javax.help.SecondaryWindow", null));

                     }
                     else  buttons[i].addActionListener(this);
                     add(buttons[i]);
                 }
                 catch (Throwable e) {}
              }
           }  // End else.
        }     // End for.
        add(Box.createHorizontalGlue());
        setOpaque(false);
        setFocusable(true);
        addFocusListener(this);

        if (sound==null) sound = new SoundData();
     }  // End Constructor.
     
   
     /** Method to get the array of control buttons
      *  @return JButton array
      */
     public JButton[] getButtons() { return buttons; }
     
     /** ActionListener to respond to navigation buttons and the pop up menu
      *  @param event Object triggering the event
      */
     public void actionPerformed(ActionEvent event)
     {
        JButton source = (JButton)event.getSource();
   
        if (source == buttons[INFO])  
        { if (popup!=null) popup.fire();
          else Toolkit.getDefaultToolkit().beep();
        }
        
        if (source == buttons[LINK])  
        { lesson.setActiveLesson(lesson.getLink()); }
        
        // Check if we are to adjust the current layer
        int layer = lesson.getLayer();
        if (source == buttons[UP])
        {  if (layer<AcornsProperties.MAX_LAYERS)
           {   
        		if (!lesson.setLayer(++layer))
        		{
        			Toolkit.getDefaultToolkit().beep();
        			return;
        		}
        		
                setLayerName();
                source.setToolTipText(adjustLayerTip(true));
                buttons[DOWN].setToolTipText(adjustLayerTip(false));
           }
           else Toolkit.getDefaultToolkit().beep();
        }
        
         if (source == buttons[DOWN])
         {   if (layer>AcornsProperties.MIN_LAYERS)
             {  lesson.setLayer(--layer);
                setLayerName();
                buttons[UP].setToolTipText(adjustLayerTip(true));
                source.setToolTipText(adjustLayerTip(false));
            }
            else Toolkit.getDefaultToolkit().beep();
         }
         
        boolean ok;
        if (source == buttons[PREV])
        {
            ok = lesson.setActiveLesson(false, false);
            if (!ok) Toolkit.getDefaultToolkit().beep();
            closeAllDialogs();
        }
        if (source == buttons[NEXT])
        {
            ok = lesson.setActiveLesson(true, false);
            if (!ok) Toolkit.getDefaultToolkit().beep();
            closeAllDialogs();
        }
        if (source == buttons[FIRST])
        {
            ok = lesson.setActiveLesson(false, true);
            if (!ok) Toolkit.getDefaultToolkit().beep();
            closeAllDialogs();
        }
        if (source == buttons[LAST])
        {
            ok = lesson.setActiveLesson(true, true);
            if (!ok) Toolkit.getDefaultToolkit().beep();
            closeAllDialogs();
        }

        if (source== buttons[RECORD])
        {
           if (sound.isActive()) sound.stopSound();
           sound.record(buttons[RECORD]);
        }

        if (source == buttons[PLAY])
        {
           sound.stopSound();
           if (!sound.playBack(null,0,-1))
              lesson.setText(LanguageText.getMessage("commonHelpSets", 27));
        }

        if (source == buttons[STOP])  sound.stopSound();
        
        if (source == buttons[HELP])
        {
    		try
    		{ 
    			String name = lesson.getClass().getSimpleName();
    			String urlString = "/" + name + "/" + target + ".htm";
    			URL url = this.getClass().getResource(urlString);
    			
    			JEditorPane htmlPane = new JEditorPane(url);
    			htmlPane.setEditable(false);
    			htmlPane.addHyperlinkListener(
    				new HyperlinkListener()
    				{
    					public void hyperlinkUpdate(HyperlinkEvent evt) 
    					{
    				       if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
    				       {
    				            JEditorPane pane = (JEditorPane)evt.getSource();
    				            try 
    				            {
    				            	// Show the new page in the editor pane.
    				            	if (backURL!=null)
    				            	{  
    				            		pane.setPage(backURL);
    				            		backURL = null;
    				            	}
    				            	else
    				            	{
    				            		backURL = pane.getPage();
        				            	pane.setPage(evt.getURL());
    				            		
    				            	}
    				            } catch (IOException e) 
    				            {
    				            	Frame frame = lesson.getRootFrame();
    				            	JOptionPane.showMessageDialog(frame, e.toString()); }
    					}    // end if
    					 }   // end hyperlinkUpdate
    					});  // End of anonymous  listener
    			
    			JScrollPane editorScrollPane = new JScrollPane(htmlPane);
    			editorScrollPane.setPreferredSize(new Dimension(500, 500));
            	Frame frame = lesson.getRootFrame();
			    JOptionPane.showMessageDialog(frame,  editorScrollPane);
    		}
    		catch (Exception e)
    		{  JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), e); }
        }
     }  

     /** Set the layer name into the lesson controls */
     private void setLayerName()
     {
        String[] header = lesson.getLessonHeader();
        int index = lesson.getLayer() + AcornsProperties.LAYERNAMES -1;
        if (index>=AcornsProperties.LAYERNAMES && header.length > index)
        {
            String layerText = header[index];
            if (layerText.length()>0)
            layerName.setText(" " + header[index] + " " );
        }
     }
     
   /** Adjust the tooltip for the adjusting layers
    * 
    * @param flag True if layers going up, false otherwise
    * @return text describing new tooltip
    */
   private String adjustLayerTip(boolean flag)
   {
       int layer = lesson.getLayer();
       String[] layerControls =LanguageText.getMessageList("commonHelpSets",28);
       if (flag)
       {   if (layer >= AcornsProperties.MAX_LAYERS)
                return layerControls[0];
           else return layerControls[2] + " " + layer;
       }
       else
       {    if (layer <= AcornsProperties.MIN_LAYERS)
                return layerControls[1];
            else return layerControls[3] + " "  + layer;
       }
   }        // End of AdjustLayerTip()

   /** Internal unused listener method */
   public void focusGained(FocusEvent event)   {}

   /** Internal listener method to stop playbacks when focus is lost */
   public void focusLost(FocusEvent event)
   {
       if (sound!=null) sound.stopSound();
   }
   
   /** Make a button object that controls where the tooltip is displayed
    * 
    */
   class ToolTipButton extends JButton
   {
	   private static final long serialVersionUID = 1;
	   
       /** Create a button with an icon and tool tip
        * 
        * @param icon The icon that goes with the button
        * @param tooltip The tool tip text
        */
       public ToolTipButton(ImageIcon icon, String tooltip)
       {  
           super(icon);
           this.setToolTipText(tooltip);
       }
       
       public @Override Point getToolTipLocation(MouseEvent e)
       {  return new Point(20, -30); }
       
   }
   
   
   private void closeAllDialogs()
   {
	   Window[] windows = Frame.getWindows();

       for (Window window : windows)
       {
           if (window instanceof JDialog)
           {
               window.dispose();
           }
       }
   }
}    // End of LessonPlayControls class


