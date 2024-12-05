/**
 * ImageListener.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.editor;

import java.awt.event.*;
import java.lang.reflect.Method;

import javax.swing.*;

import org.acorns.data.*;
import org.acorns.language.*;

public class ImageListener extends SoundListener implements ActionListener
{
    /** Creates a new instance of ImageListener 
     *  @param panel panel attached to this listener
     *  @param label JLabel to display errors and messages
     *  @param glossText JTextField for entering gloss text annotations
     *  @param nativeText JTextField for entering native text annotations
     *  @param combo combo box holding native keyboard mappings
     */
    public ImageListener
       (SoundPanel panel, JLabel label, JTextField glossText
                        , JTextField nativeText, JComboBox<String> combo)
    {
        super(panel, label, glossText, nativeText, combo);        
    }
    
    /**  Listener to respond to sound commands
     *   @param e triggered by a sound command. 
     */
     public void actionPerformed(ActionEvent e)
     {
         if (wavePanel == null) return;
         
         // Depress the button border.
         JButton whichButton = (JButton)e.getSource();
 
         String buttonName   = whichButton.getName();
         String options = "ra";
         if (options.indexOf(buttonName)<0)
         {
             this.processThread(e);
             return;
         }
         
         ImageAnnotationData imageData
            = (ImageAnnotationData)soundPanelProperties.getAnnotationData(panel);
         
         try
         {
        	 Object object = imageData.getLesson();
        	 Class<?> className = object.getClass();
        	 Class<?>[] paramInt = new Class[1];
        	 paramInt[0] = Integer.TYPE;
        	 Method method = className.getMethod("removePicture", paramInt);
        	 method.invoke(object,  -1);
        	 
         }
         catch (Exception ex) 
         { 
        	 label.setText(ex.toString()); 
         }
         label.setText(LanguageText.getMessage("soundEditor", 43));
     }
}
