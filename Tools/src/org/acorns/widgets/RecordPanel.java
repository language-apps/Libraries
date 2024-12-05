/**
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2010, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acorns.widgets;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.data.*;
import org.acorns.visual.*;

public class RecordPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1;
	
	/** For small audio recordings 
	 *    (less than 1 second at 16000 Hz)
	 * 		Increase the similarity factor by 1/3
	 */
	public final static int SMALL_RECORDING_THRESHOLD = 100;
	public final static double SIMILARITY_ADJUSTMENT = 4.0/3.0; 
    
	private final int RECORD=0, PLAY=1, STOP=2;
    private AcornsProperties properties = null;
    
    private final int icons[] = 
    {  AcornsProperties.RECORD, AcornsProperties.PLAY, AcornsProperties.STOP  };
    
    private SoundData sound;
    private JButton[] buttons;
    private boolean   recording;
    private AudioCompare clusters;
    
    private Lesson lesson;
    private UndoRedoData undoRedo;
    private ColorScheme  colors;
    private boolean selected;
    
    public RecordPanel(SoundData sound)
    {
    	this(sound, 20, 10);
    }
    
    public RecordPanel(SoundData sound, int height, int strut)
    {
       this.sound = sound;
       lesson = null;
       undoRedo = null;
       recording  = false;
       
       String[] toolTips = LanguageText.getMessageList("commonHelpSets",26);
       ImageIcon icon;
       
       setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       
       buttons = new JButton[3];
       for (int i=0; i<icons.length; i++)
        {
    	   
           icon = getIcon(icons[i], height);
           buttons[i] = new JButton(icon);
           buttons[i].setToolTipText(toolTips[i+5]);
           buttons[i].setOpaque(false);
           buttons[i].setMargin(new Insets(0,0,0,0));
           if (i==0)
           {
        	   buttons[i].setPreferredSize(new Dimension(height+40, height));
        	   buttons[i].setOpaque(true);
           }
           buttons[i].addActionListener(this);
           buttons[i].setAlignmentY(TOP_ALIGNMENT);
           add(Box.createHorizontalStrut(strut));
           add(buttons[i]);
       } 
    }
    
    /** Set the old data if user has the ability to undo the operation
     * 
     * @param undoRedo The old data that can be restored
     * @param lesson  The current lesson object
     * @param colors  Desired colors for  this component
     */
    
    public void setUndo(Lesson lesson, UndoRedoData undoRedo
                                        , ColorScheme colors, boolean selected)
    {
        this.lesson = lesson;
        this.undoRedo = undoRedo;
        this.colors = colors;
        this.selected = selected;
    }
    
    /** Get the icon for the audio button
     * 
     * @param icon The icon number
     * @param size The size of the icon
     * @return ImageIcon object representing the icon
     */
    private ImageIcon getIcon(int icon, int size)
    {
       if (properties==null)
       {
         // Get the ACORNS property change listener.
         properties = AcornsProperties.getAcornsProperties();
       } 
       return properties.getIcon(icon, size);

    }
  
    /** ActionListener to respond to navigation buttons and the pop up menu
     *  @param event Object triggering the event
     */
    public void actionPerformed(ActionEvent event)
    {
        JButton source = (JButton)event.getSource();
      
        if (source== buttons[RECORD])
        {
           recording = true;
           if (sound.isActive()) sound.stopSound();
           sound.record(buttons[RECORD]);
        }

        if (source == buttons[PLAY])
        {
       	   stopRecording();
           if (!sound.playBack(null,0,-1))
              JOptionPane.showMessageDialog
                          (this, LanguageText.getMessage("commonHelpSets", 27));
        }

        if (source == buttons[STOP]) 
        {
        	stopRecording();
        }
     }  
    
    /** Stop the recording when playing back or if the stop option chosen */
    public void stopRecording()
    {
        if (lesson!=null && sound.isActive() && recording)
        { 
            lesson.setDirty(true);
            lesson.pushUndo(undoRedo);
            setBackground(colors.getColor(!selected));
            setForeground(colors.getColor(selected));
        }
        sound.stopSound();
        
        // Compute audio cluster assignments 
        clusters = new AudioCompare(null, sound);
        recording = false;
    }
    
    /** Return true if the record panel contains audio data */
    public boolean isRecorded()
    {
    	return sound.isRecorded();
    }
    
    /** Compare recorded audio to the supplied audio object
     * 
     * @param bounds Start/end offsets into the audio object
     * @param sound The audio object to compare to
     * @return similarity fraction (0.0 = no similarity, 1.0 = identical)
     */
    public double compare(Point bounds, SoundData audio)
    {
    	if (!sound.isRecorded()) return 0.0;
    	if (clusters==null) return 0.0;
    	
    	clusters.setTarget(bounds,  audio);
    	double[] result = clusters.compare();
    	double total = result[SpellCheck.SOURCE_FRAMES] + result[SpellCheck.TARGET_FRAMES];
    	double similarity = (total-result[SpellCheck.DIFFERENCE]) / total;
    	if (total < SMALL_RECORDING_THRESHOLD)
    		similarity *= SIMILARITY_ADJUSTMENT;
    	return similarity;
    }
    
    /** Set the audio object */
    public void setSound(SoundData sound)
    {
        this.sound = sound;
    }

    /** Get the audio object */
    public SoundData getSound() { return sound; }

}   // End of class
