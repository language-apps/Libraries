/*
 * WaveListener.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.editor;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import org.acorns.audio.SoundDefaults;
import org.acorns.data.*;
import org.acorns.language.*;

/**
 *
 * Class to listen for SoundPanel button depressions.
 */
public class WaveListener extends SoundListener implements ActionListener
{
    /** Creates a new instance of ButtonListener 
     *  @param panel panel attached to this listener
     *  @param label JLabel to display errors and messages.
     *  @param glossText JTextField for entering gloss text annotations.
     *  @param nativeText JTextField for entering native text annotations.
     *  @param combo combo box holding native keyboard mappings.
     */
    public WaveListener
       (SoundPanel panel, JLabel label, JTextField glossText, JTextField nativeText
          , JComboBox<String> combo) 
    {
        super(panel, label, glossText, nativeText, combo);
    }
    
    protected ActionEvent event;

    /**  Listener to respond to sound commands in a separate thread
     *   @param actionEvent Event triggered by a sound command
     */
    public synchronized void actionPerformed(ActionEvent actionEvent)
    {
         JButton whichButton = (JButton)actionEvent.getSource();
         String buttonName   = whichButton.getName();
         String options = "sf;ss;ws;fs;td;sd;fd;vd;dd;";
         if (options.indexOf(buttonName)<0)
         {
             this.processThread(actionEvent);
             return;
         }

        if (event!=null)
        {
            label.setText(LanguageText.getMessage("soundEditor", 60));
            return;
        }
        
        event = actionEvent;
        label.setText(LanguageText.getMessage("soundEditor", 61));
        wavePanel.enableDisplay(true);
    	try
    	{
    		process(event);
    	}
    	catch (Exception e)
    	{
    		try
    		{
        		StackTraceElement stack = e.getStackTrace()[0];
        		int lineNo = stack.getLineNumber();
        		String name = stack.getClassName();
        		String msg = e.toString();
        		JButton button = (JButton)event.getSource();
        		String command = button.getName();
        		String output = "Command: " + command + " Line: " + lineNo + " - " + "Object: " + name
        				+ " " + msg;
        		label.setText(output);
    		}
    		catch (Exception ex)
    		{
    			label.setText(ex.toString());
    		}
        }
		event = null;
    }
        
        /**  Listener to respond to sound commands
         *   @param e triggered by a sound command. 
         */
         public String process(ActionEvent e)
         {
             if (wavePanel == null) return "";

             // Depress the button border.
             JButton whichButton = (JButton)e.getSource();
             String buttonName   = whichButton.getName();
             char category = buttonName.charAt(1);
             char option   = buttonName.charAt(0);
         
             // Set text into the sound data text array.
             Annotations annotations 
                     = soundPanelProperties.getAnnotationData(panel);
             AnnotationData sound = (AnnotationData)annotations.getSound();
             glossNative[0] = glossText.getText();
             glossNative[1] = nativeText.getText();
             if (combo==null) glossNative[2] = "English";
             else  glossNative[2] = (String)combo.getSelectedItem();
             sound.setSoundText(glossNative);

             String msg="";
             label.setText(LanguageText.getMessage("soundEditor", 61));
             
             switch(category)
             {             
                case 'f':              // File commands.
                    switch (option)
                    {
                      case 's':        // Save.
                          if (editor==null || sound==null || !sound.isRecorded())
                          { msg= LanguageText.getMessage("soundEditor",72);
                            break;
                          }

                           File saveFile = writeFile(sound, 200);
                           msg = null;
                           if (saveFile != null)
                               msg = LanguageText.getMessage
                                       ("soundEditor", 83, saveFile.getName());
                           break;

                      default: msg =  LanguageText.getMessage("soundEditor", 42);
                    }
                    break;   // End of File Commands.

                 case 's':              // Sound commands.
                    if (editor==null || sound==null || !sound.isRecorded())
                    {   msg = LanguageText.getMessage("soundEditor", 72);
                        break; 
                    }

                    Point select = wavePanel.getSelectedFrames(sound);
                    // Assume all if nothing selected.
                    if (select==null || select.x==select.y) 
                        select=new Point(-1,-1);
                    
                    int rateAlg = SoundDefaults.getRateAlgorithm();
                    if (rateAlg==SoundDefaults.DEFAULTRATEALG &&
                                 (select.x != -1 || select.y != -1)  && (option=='s' || option=='w'))
                    {  msg = LanguageText.getMessage("soundEditor", 111);
                       break;
                    }

                    switch (option)
                    {
                       case 's':        // Speed up.
                         if (editor.rateChange(select.x, select.y, true)==false)
                         {  msg = LanguageText.getMessage("soundEditor", 112);
                            break;   }

                         if (!wavePanel.drawSound( sound, false))
                         {    msg = LanguageText.getMessage("soundEditor", 91);
                         }
                         else msg = LanguageText.getMessage("soundEditor", 113);
                         break;
                       case 'w':        // Slow down.
                         if (editor.rateChange(select.x,select.y,false)==false)
                         {  msg = LanguageText.getMessage("soundEditor", 114);
                            break;   }

                         if (!wavePanel.drawSound( sound, false))
                         {   msg = LanguageText.getMessage("soundEditor", 91);}
                         else msg = LanguageText.getMessage("soundEditor", 115);
                         break;
                           
                       case 'f':      // Filter
                         try 
                         {
                           if (editor.filterSignal(select.x, select.y)==false)
                           { msg = LanguageText.getMessage("soundEditor", 192);}
                           else if (!wavePanel.drawSound( sound, false))
                           {    msg = LanguageText.getMessage("soundEditor",91);
                           }
                           else 
                               msg = LanguageText.getMessage("soundEditor",193);
                         }
                         catch (Exception ex)
                         {  msg = ex.toString(); }
                         break;

                       default: msg =  LanguageText.getMessage("soundEditor", 42);
                    }
                    break;   // End of Sound Commands.

               case 'd':              // Display commands.
                    if (editor==null || sound==null || !sound.isRecorded())
                     { msg = LanguageText.getMessage("soundEditor", 72); break;}

                     switch (option)
                     {
                       case 't':        // Time domain.
                         wavePanel.setDisplayType(SoundDisplayPanel.TIME);
                         if (!wavePanel.drawSound( sound, true))
                         {  msg = LanguageText.getMessage("soundEditor", 91); }
                         break;
                       case 's':        // Spectrograph.
                         wavePanel.setDisplayType
                                              (SoundDisplayPanel.SPECTROGRAPH);
                         if (!wavePanel.drawSound( sound, true))
                         {  msg = LanguageText.getMessage("soundEditor", 91); }
                         break;
                       case 'f':        // Frequency domain.
                         wavePanel.setDisplayType(SoundDisplayPanel.FFT);
                         if (!wavePanel.drawSound( sound, false))
                         {  msg = LanguageText.getMessage("soundEditor", 91); }
                         break;    
                       case 'v':      // Feature vector.
                         wavePanel.setDisplayType(SoundDisplayPanel.FEATURE);
                         if (!wavePanel.drawSound( sound, false))
                         {  msg = LanguageText.getMessage("soundEditor", 91); }
                         break;  
                       case 'd':     // CEPSTRAL distances
                    	 wavePanel.setDisplayType(SoundDisplayPanel.DISTANCE);
                         if (!wavePanel.drawSound( sound, false))
                         {  msg = LanguageText.getMessage("soundEditor", 91); }
                    	 break;

                       default:
                         msg = LanguageText.getMessage("soundEditor", 42);
                    }
                    break;  // End of display commands.

                 default: msg =  LanguageText.getMessage("soundEditor", 42);
             }   // End of command categories.

             // Set an appropriate label.
             if (msg != null) 
             {   if (!msg.equals("")) label.setText(msg);
                 else 
                	 label.setText(LanguageText.getMessage("soundEditor", 43));
             }
             return "";
  }  // End ProcessWaveListener
     
}            // End WaveListener.

