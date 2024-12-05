/*
 * AnnotationListener.java
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
import org.acorns.data.*;
import org.acorns.language.*;

/**
 *
 * Class to listen for SoundPanel button depressions.
 */
public class AnnotationListener extends SoundListener implements ActionListener
{
    
    /** Creates a new instance of AnnotationListener 
     *  @param panel panel attached to this listener
     *  @param label JLabel to display errors and messages
     *  @param glossText JTextField for entering gloss text annotations
     *  @param nativeText JTextField for entering native text annotations
     *  @param combo combo box holding native keyboard mappings
     */
    public AnnotationListener
       (SoundPanel panel, JLabel label, JTextField glossText, JTextField nativeText
           , JComboBox<String> combo) 
    {
        super(panel, label, glossText, nativeText, combo);        
     }
    
    /**  Listener to respond to sound commands
     *   @param e triggered by a sound command. 
     */
     public void actionPerformed(ActionEvent e)
     {
         if (wavePanel == null) return;
         
         // Create wave panel if this is the first call.
         Annotations annotations = soundPanelProperties.getAnnotationData(panel);         
         AnnotationData sound = (AnnotationData)annotations.getSound();
         
         // Depress the button border.
         JButton whichButton = (JButton)e.getSource();
 
         String buttonName   = whichButton.getName();
         char category = buttonName.charAt(1);
         char option   = buttonName.charAt(0);
         String options = "ea;aa;ra";
         System.gc();  // Maximize available memory.
         if (options.indexOf(buttonName)<0)
         {
             this.processThread(e);
             return;
         }

         String msg="";
         label.setText(LanguageText.getMessage("soundEditor", 31));
         String text = nativeText.getText();
                       
         switch(category)
         {
             case 'a':              // Annotation commands.
                if (!sound.isRecorded()) 
                { msg = LanguageText.getMessage("soundEditor", 32); break; }

                Point select = wavePanel.getSelectedFrames(sound);
                if (select==null)
                { msg = LanguageText.getMessage("soundEditor", 33); break;  }
                if (option=='e' && select.x != select.y)
                { msg = LanguageText.getMessage("soundEditor", 34); break; }
                if ((option=='e' || option == 'r') 
                               && sound.getAnnotationSize()==0)
                {  msg = LanguageText.getMessage("soundEditor", 35); break; }

                if ("aer".indexOf(option)>=0)
                    undoRedo.pushUndo(new SoundUndoRedoData(sound.clone()));

                switch (option)
                {   case 'a':
                        if (sound.insertAnnotation(text, select) ==  false)
                        {  msg =  LanguageText.getMessage("soundEditor", 36);
                           break;
                        }

                        if (!wavePanel.drawSound( sound, false))
                        {  msg = LanguageText.getMessage("soundEditor", 37); }
                        break;

                    case 'e':
                        if (sound.modify(text, select) ==  false)
                        {  msg =  LanguageText.getMessage("soundEditor", 38);
                           break;
                        }

                        if (!wavePanel.drawSound( sound, false))
                        {  msg = LanguageText.getMessage("soundEditor", 39); }
                        break;

                    case 'r':
                        if (sound.delete(select) ==  false)
                        {  msg =  LanguageText.getMessage("soundEditor", 40);
                           break;
                        }
                        if (!wavePanel.drawSound( sound, false))
                        {  msg = LanguageText.getMessage("soundEditor", 41); }
                        break;

                    default: msg = null;
                }
                break;  // End of Annotation Commands.
                           
             default: msg = null;
                
         }   // End of command categories.            

         // Set an appropriate label.
         if (msg == null) 
               label.setText(LanguageText.getMessage("soundEditor", 42));
         else if (!msg.equals("")) label.setText(msg);
              else label.setText(LanguageText.getMessage("soundEditor", 43));
     }   // End actionPerformed()
   
}   // End AnnotationListener.
