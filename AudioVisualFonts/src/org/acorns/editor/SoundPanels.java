/*
 * SoundPanels.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.editor;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.net.*;
import javax.sound.sampled.*;
import javax.help.*;
import org.acorns.data.*;
import org.acorns.audio.*;
import org.acorns.visual.*;
import org.acorns.language.*;

/** Create a panel with more than onee sound panels. */
public class SoundPanels extends RootSoundPanel      
{
    private SoundPanel[]  panels;
    private SoundPanel    annotationPanel;
    private JCheckBox     volumeMute, microphoneMute;
    private JPanel        thisPanel, center;
    private boolean       annotationMode;
    
    private final static int ICONSIZE = 30;
    private static final long serialVersionUID = 1;
    
    /** Creates a new instance of SoundPanels */
    public SoundPanels(int number)
    {
    	this(number, null);
    }
	
    /** Creates a new instance of SoundPanels and possibly open an audio file */
    public SoundPanels(int number, String[] args) 
    {
        // Set the layout of this panel.
        thisPanel = this;
        setLayout(new BorderLayout());
        setBackground(SoundDefaults.BACKGROUND);

        annotationMode = false;
        makeAnnotationData();
        
        // Create the "SoundListeners" properties change listener
        // Remove previous "SoundListeners"
        String listener = "SoundListeners";
        PropertyChangeListener[] pcl 
            = Toolkit.getDefaultToolkit().getPropertyChangeListeners(listener);
        for (int i=0; i<pcl.length; i++)
        {   Toolkit.getDefaultToolkit().removePropertyChangeListener
                                                        (listener, pcl[i]); }
        // Add the new one.
        Toolkit.getDefaultToolkit().addPropertyChangeListener(listener, this);
 
        // Create array of sound panels and add them to the center.
        center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(SoundDefaults.BACKGROUND);
        
        panels = new SoundPanel[number];
        SoundDisplayPanel display;
        for (int i=0; i<panels.length; i++)
        {
            panels[i] = new SoundPanel
                    ('s', new ColorScheme(null, null), null);
            panels[i].setBackground(SoundDefaults.BACKGROUND);
            panels[i].setBorder(BorderFactory.createLoweredBevelBorder());
            center.add(panels[i]);
            center.add(Box.createVerticalStrut(10));
            
            if (i==0)
            {
            	if (args!=null && args.length>0 && args[0]!=null)
            	{
            		display = panels[0].getDisplayPanel();
            		display.audioDropped(new File(args[0]));
            	}
            }
        }
        add(center, BorderLayout.CENTER);
        
        annotationPanel = new SoundPanel
                ('a', new ColorScheme(null, null), null);
        annotationPanel.setBackground(SoundDefaults.BACKGROUND);
        annotationPanel.setBorder(BorderFactory.createLoweredBevelBorder());
                
        // Create the slider that controls speaker volume.
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
        north.setBackground(SoundDefaults.BACKGROUND);
       
        // Create a check Box to indicate if the speaker is muted.
        String[] msgs = LanguageText.getMessageList("soundEditor", 116);
        volumeMute = new JCheckBox(msgs[0]);
        volumeMute.setBorder(BorderFactory.createEtchedBorder());
        volumeMute.setBackground(SoundDefaults.BACKGROUND);
        volumeMute.setToolTipText(msgs[1]);
        volumeMute.setSelected(updateMute(Port.Info.SPEAKER, false, false));
        volumeMute.addActionListener(
                new ActionListener()
                {
                   public void actionPerformed(ActionEvent event)
                   {
                       JCheckBox checkBox = (JCheckBox)event.getSource();
                       boolean  value    = checkBox.isSelected();                       
                       if (!updateMute(Port.Info.SPEAKER, value, true))
                       {
                           checkBox.setSelected(false);
                           JOptionPane.showMessageDialog(thisPanel
                                 , LanguageText.getMessage("soundEditor", 118));
                       }
                   }
                });
       
        JSlider volume  = new JSlider();
        volume.setBackground(SoundDefaults.BACKGROUND);
        volume.setMinimum(0);
        volume.setMaximum(20);
        volume.setToolTipText(msgs[2]);
        float speakerRatio = updateControls(Port.Info.SPEAKER, -1);
        volume.setValue((int)(20 * speakerRatio));
        volume.addChangeListener(
                new ChangeListener()
                {
                   public void stateChanged(ChangeEvent ce) 
                   {
                       JSlider slider = (JSlider)ce.getSource();
                       float   value    = slider.getValue();
                       float   minValue = slider.getMinimum();
                       float   maxValue = slider.getMaximum();
                       float   ratio    = (value-minValue)/(maxValue-minValue);
                       
                       float result = updateControls(Port.Info.SPEAKER, ratio);
                       if (result<0)
                       {  JOptionPane.showMessageDialog(thisPanel, 
                                  LanguageText.getMessage("soundEditor", 119));
                       }
                   }   // End of StateChanged.
                });
    
        // Create a check Box to indicate if the microphone is muted.
        msgs = LanguageText.getMessageList("soundEditor", 117);
        microphoneMute = new JCheckBox(msgs[0]);
        microphoneMute.setBorder(BorderFactory.createEtchedBorder());
        microphoneMute.setBackground(SoundDefaults.BACKGROUND);
        microphoneMute.setToolTipText(msgs[1]);
        microphoneMute.setSelected
                (updateMute(Port.Info.MICROPHONE, false, false));
        microphoneMute.addActionListener(
                new ActionListener()
                {
                   public void actionPerformed(ActionEvent event)
                   {
                       JCheckBox checkBox = (JCheckBox)event.getSource();
                       boolean  value    = checkBox.isSelected();                       
                       if (!updateMute(Port.Info.MICROPHONE, value, true))
                       {
                           checkBox.setSelected(false);
                           JOptionPane.showMessageDialog(thisPanel, 
                                   LanguageText.getMessage("soundEditor", 118));
                       }
                   }
                });
       
        // Create the slider that controls microphone volume.
        JSlider microphone = new JSlider();
        microphone.setBackground(SoundDefaults.BACKGROUND);
        microphone.setMinimum(0);
        microphone.setMaximum(20);
        microphone.setToolTipText(msgs[2]);

        float microphoneRatio = updateControls(Port.Info.MICROPHONE, -1);
        microphone.setValue((int)(20 * microphoneRatio));
        microphone.addChangeListener(
                new ChangeListener()
                {
                   public void stateChanged(ChangeEvent ce) 
                   {
                       JSlider slider = (JSlider)ce.getSource();
                       float   value    = slider.getValue();
                       float   minValue = slider.getMinimum();
                       float   maxValue = slider.getMaximum();
                       float   ratio    = (value-minValue)/(maxValue-minValue);
                       
                       float result = updateControls(Port.Info.MICROPHONE, ratio);
                       if (result<0)
                       {  JOptionPane.showMessageDialog(thisPanel
                                , LanguageText.getMessage("soundEditor", 120));
                       }
                   }
                });

        // Create the Help Button.
        JButton helpButton = null;
        URL     url  = getClass().getResource("/data/help.png");
            
        if (url!=null)
        {   Image newImage  = Toolkit.getDefaultToolkit().getImage(url);
			         newImage = newImage.getScaledInstance
                (ICONSIZE, ICONSIZE, Image.SCALE_REPLICATE);
        
            helpButton = new JButton(new ImageIcon(newImage));
            helpButton.setPreferredSize(new Dimension(ICONSIZE+1, ICONSIZE+1));
            helpButton.setMinimumSize  (new Dimension(ICONSIZE+1, ICONSIZE+1));
            helpButton.setMaximumSize  (new Dimension(ICONSIZE+1, ICONSIZE+1));
            helpButton.setToolTipText
                       (LanguageText.getMessage("soundEditor", 121));
            helpButton.setBackground(SoundDefaults.BACKGROUND);
    
            try
            {
   		           HelpSet helpSet = LanguageText.getHelpSet();
	 	             CSH.setHelpIDString(helpButton, "overview");
                helpButton.addActionListener(
		                 new CSH.DisplayHelpFromFocus(helpSet
			                      , "javax.help.SecondaryWindow", null));
            }
            catch (Throwable t)
            {
                helpButton.addActionListener(
                    new ActionListener()
                    {   public void actionPerformed(ActionEvent ae)
                        {  JOptionPane.showMessageDialog(thisPanel
                                 , LanguageText.getMessage("soundEditor", 122));
                        }
                    });
            }
        }
        
        // Create the mode switch button
        JButton modeButton = null;
        url = getClass().getResource("/data/flip.png");
        if (url!=null)
        {   Image newImage  = Toolkit.getDefaultToolkit().getImage(url);
			         newImage = newImage.getScaledInstance
                (ICONSIZE, ICONSIZE, Image.SCALE_REPLICATE);
        
            modeButton = new JButton(new ImageIcon(newImage));
            modeButton.setPreferredSize(new Dimension(ICONSIZE+1, ICONSIZE+1));
            modeButton.setMinimumSize  (new Dimension(ICONSIZE+1, ICONSIZE+1));
            modeButton.setMaximumSize  (new Dimension(ICONSIZE+1, ICONSIZE+1));
            modeButton.setToolTipText
                                (LanguageText.getMessage("soundEditor", 123));
            modeButton.setBackground(SoundDefaults.BACKGROUND);
            modeButton.addActionListener(
               new ActionListener()
               {  public void actionPerformed(ActionEvent ae)
                  {
                     // Create option dialog panel.
                     if (annotationMode) annotationMode = false;
                     else annotationMode = true;
                     
                     center.removeAll();
                 
                     if (annotationMode)  
                     {   center.add(annotationPanel);  }
                     else
                     {   for (int i=0; i<panels.length; i++)
                         {   center.add(panels[i]);
                             center.add(Box.createVerticalStrut(10));
                         }                         
                     }       // End of if annotationMode
                     PlayBack.stopPlayBack();
                     revalidate();
                     repaint();
                  }       // End of actionPerformed
               });        // End of add action listener
        }                 // End if mode button url exists
        
        
        // Create the advanced controls button.
        JButton controlsButton = null;
        url        = getClass().getResource("/data/options.png");
            
        if (url!=null)
        {   Image newImage  = Toolkit.getDefaultToolkit().getImage(url);
			         newImage = newImage.getScaledInstance
                (ICONSIZE, ICONSIZE, Image.SCALE_REPLICATE);
        
            controlsButton = new JButton(new ImageIcon(newImage));
            controlsButton.setPreferredSize(new Dimension(ICONSIZE+1, ICONSIZE+1));
            controlsButton.setMinimumSize  (new Dimension(ICONSIZE+1, ICONSIZE+1));
            controlsButton.setMaximumSize  (new Dimension(ICONSIZE+1, ICONSIZE+1));
            controlsButton.setToolTipText
                                  (LanguageText.getMessage("soundEditor", 124));
            controlsButton.setBackground(SoundDefaults.BACKGROUND);
            controlsButton.addActionListener(
               new ActionListener()
               {  public void actionPerformed(ActionEvent ae)
                  {
                     // Create option dialog panel.
                     OptionPanel options = new OptionPanel();

                     //options.setBackground(SoundDefaults.BACKGROUND);
                     Frame frame = JOptionPane.getRootFrame();
                     Component[] components = frame.getComponents();
                     for (int c=0; c<components.length; c++)
                     {  components[c].setBackground(SoundDefaults.BACKGROUND);
                     }
                     frame = JOptionPane.getFrameForComponent(thisPanel);
                     components = frame.getComponents();
                     for (int c=0; c<components.length; c++)
                     {  components[c].setBackground(SoundDefaults.BACKGROUND);
                     }
                     components = options.getComponents();
                     for (int c=0; c<components.length; c++)
                     {  components[c].setBackground(SoundDefaults.BACKGROUND);
                     }

                     // Display dialog and get user response.
                     String[] msgs = LanguageText.getMessageList
                                                         ("soundEditor", 125);
                     String[] dialogOptions = {msgs[0], msgs[1]};
                     String title = msgs[2];

                     boolean more = true;
                     while (more)
                     {   more = false;
                         int result  = JOptionPane.showOptionDialog
                             (thisPanel, options, title
                             , JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE
                             , null, dialogOptions, dialogOptions[1]);

                         // If canceled, restore initial values.
                         if (result == 0 && !options.updateValues()) more=true;
                     }
                  }
               });
        }

        // Add the components to the north panel.
        if (helpButton!=null)    north.add(helpButton);
        north.add(Box.createHorizontalGlue());
        if (AudioSystem.isLineSupported(Port.Info.SPEAKER))
        {
           msgs = LanguageText.getMessageList("soundEditor", 127);
           north.add(new JLabel(msgs[0] + " "));
           north.add(volumeMute);
           north.add(Box.createRigidArea(new Dimension(15, 40)));
           north.add(new JLabel(msgs[1]));
           north.add(volume);
           north.add(Box.createRigidArea(new Dimension(30, 40)));
        }
        if (AudioSystem.isLineSupported(Port.Info.MICROPHONE))
        {
           north.add(new JLabel(msgs[2] + " "));
           north.add(microphoneMute);        
           north.add(Box.createRigidArea(new Dimension(15, 40)));
           north.add(new JLabel(msgs[1]));
           north.add(Box.createRigidArea(new Dimension(5, 40)));
           north.add(microphone);
        }
        north.add(Box.createHorizontalGlue());
        if (modeButton!=null)     north.add(modeButton);
        if (controlsButton!=null) north.add(controlsButton);
        
        // Add the north portion to the main panel.
        add(north, BorderLayout.NORTH);
    }    
     
    /** Update speaker and microphone mute control.
     *  @param port Either Port.Info.SPEAKER or Port.Info.MICROPHONE
     *  @param on  true to set mute, false otherwise.
     *  @param update true to update, false otherwise.
     *  @return if update is true, return true if success
     *  @return if update is false, return current value
     */
   private boolean updateMute(Port.Info port, boolean on, boolean update)
   {   boolean result = true;
       String  controlName = "Mute";
       if (port.equals(Port.Info.MICROPHONE)) controlName = "Microphone Boost";
       
       if (AudioSystem.isLineSupported(port))
       {   
           Line line = null;
           try
           {
              line = (Port)AudioSystem.getLine(port);
              line.open();
              
             String controlType;
             CompoundControl compound;
             BooleanControl  mute = null;
             
             Control   member;
             Control[] members;
             Control[] controls = line.getControls();
             
             for (int i=0; i<controls.length; i++)
             {   member = controls[i];
                 if (controls[i] instanceof CompoundControl)
                 {  compound = (CompoundControl)controls[i];
                    members = compound.getMemberControls();
                    for (int m=0; m<members.length; m++)
                    {  member = members[m];
                       controlType = members[m].getType().toString();
                       if (controlType.equals("Select"))
                       { ((BooleanControl) member).setValue(true); 
                       }
                       if (controlType.equals(controlName)) { break;  } 
                    }  // End Inner loop.
                 }     // End if.
                
                 if (member.getType().toString().equals(controlName)) 
                 {  mute = (BooleanControl)member; 

                    // Update the mute control.
                    if (update) { mute.setValue(on); result = true; }
                    else        result = mute.getValue();
                 }
             }       // End outer loop.
           }
           catch (Exception e) {  result = false;  }
           finally { try {line.close();} catch (Exception e) {} }
       } 
       return result;
   }
   
   /** Open file for MAC/OS start up */
   public void openFile(File file)
   {
		SoundDisplayPanel display = panels[0].getDisplayPanel();
		display.audioDropped(file);
   }
    
    /** Access speaker and microphone volume controls.
     *  @param port Either Port.Info.SPEAKER or Port.Info.MICROPHONE
     *  @param ratio percentage of maximum to set (<0 to skip updating)
     *  @return a value of -1 if the call fails.
     */ 
    private float updateControls(Port.Info port, float ratio)
    { 
       Line line    = null;
       float result = -1;
       

       if (AudioSystem.isLineSupported(port))
       {
          try
          {  line = AudioSystem.getLine(port);
             line.open();
  
             String controlType;
             float max, min, vol, value, newValue;
             
             CompoundControl compound;
             FloatControl    scale = null;
             
             Control   member;
             Control[] members;
             Control[] controls = line.getControls();
             
             for (int i=0; i<controls.length; i++)
             {  member = controls[i];
                if (controls[i] instanceof CompoundControl)
                {  compound = (CompoundControl)controls[i];
                   members = compound.getMemberControls();
                   for (int m=0; m<members.length; m++)
                   {  member = members[m];
                      controlType = members[m].getType().toString();
                      if (controlType.equals("Select"))
                      { ((BooleanControl) member).setValue(true); 
                      }
                      if (controlType.equals("Volume")) { break;  } 
                   }  // End Inner loop.
                }     // End if.
                
                if (member.getType().toString().equals("Volume")) 
                {  scale = (FloatControl)member; 

                   // Update the volume control.
                   max      = scale.getMaximum();
                   min      = scale.getMinimum();
                   vol      = scale.getValue();
                   value    = (vol-min)/(max-min);
                   newValue = min + (max-min)*ratio;
               
                   // Prevent storing an illegal value.
                   if (ratio>0 && newValue>max && newValue<min) 
                   throw new NumberFormatException();

                   if (ratio<0) ratio = value;
                   else         newValue = min + (max-min)*ratio;
                 
                   if (newValue<0)   { result = vol; }
                   else                
                   { ((FloatControl) scale).setValue(newValue);
                     result = newValue;
                   }                 
                }
              }       // End outer loop.
          }
          catch (Exception e) { result = -1;}
          finally { try{line.close();} catch (Exception e) {}}
       } 
       return result;
    }    // End of updateControls.

    // Filled in abstract methods
    private Annotations[] annotationData;
   // private UndoRedo[] undoRedo = null;
    
    /** Set the annotation data object 
     *  @param data Annotation data object
     */
    public void setAnnotationData(Annotations data, JPanel panel)
    {  annotationData[getSoundPanelIndex(panel)] = data;  }
    
    /** Get the annotation data object
     *  @return Annotation data object
     */
    public Annotations getAnnotationData(JPanel panel)
    {  return annotationData[getSoundPanelIndex(panel)];        
    }

    /** Stop any sounds that are playing */
    public void stopSounds()
    {
        if (annotationData==null) return;
        for (int s=0; s<annotationData.length; s++)
        {
            if (annotationData[s]!=null)
                annotationData[s].getSound().stopSound();
        }
    }
    
    /** Get the undo and redo stack (create if it doesn't exist)
     *  @return Object for undo and redo operations
     */
    public UndoRedo getUndoRedo() { return null; }
    
    /** Method to get panel index
     */
    private int getSoundPanelIndex(JPanel soundPanel)
    {
        if (soundPanel == panels[0]) return 0;
        if (soundPanel == panels[1]) return 1;
        return 2;
    }
    
    /** Method to create the annotation data arrays
     */
    private void makeAnnotationData()
    {  
       if (annotationData == null)
       {  String[] text = {"", "", ""};
          annotationData = new AnnotationData[3];
          for (int i=0; i<3; i++)
          {
             annotationData[i] =  new AnnotationData(text, null);
          }
            
       }
    }

}   // End of SoundPanels class
