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
package org.acorns.lesson.categories.relatedphrases;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.*;
import java.net.*;
import java.io.*;

import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.language.*;
import org.acorns.widgets.*;
import org.acorns.lesson.*;

public class PhrasesTable extends JTable 
                                  implements MouseListener, MouseMotionListener
{
	private final static long serialVersionUID = 1;
    private final static int ICON_SIZE  = 30;
      
    private CategoryRelatedPhrases lesson;
    private JTextField  textField;
    
    private ColorScheme scheme;
    private SentenceTableModel sentenceTableModel;
    private Point dragPoint;
    
    private Border emptyBorder;
    private Border coloredBorder;
    private Color  background;

    public PhrasesTable(CategoryRelatedPhrases lesson, JTextField textField)
    {
      this.lesson = lesson;    
      this.textField = textField;
      scheme = lesson.getColorScheme();
      dragPoint = new Point(-1, -1);
      
     
      emptyBorder = BorderFactory.createEmptyBorder();
      coloredBorder = BorderFactory.createLineBorder(new Color(0x66,0x33,0x33), 3);
      background = Color.lightGray;

      setSelectionBackground(Color.BLUE);
      setSelectionForeground(Color.WHITE);
      setBackground(background);
      
      //sentences.setFillsViewportHeight(true);
      setOpaque(false);
      addMouseMotionListener(this);
      addMouseListener(this);

      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setDefaultEditor(Object.class, new SentenceEditor());
      sentenceTableModel = new SentenceTableModel(lesson);
      setModel(sentenceTableModel);
      setRowHeight(30);
 
      TableColumn column = getColumnModel().getColumn(0);
      column.setHeaderValue(LanguageText.getMessage("commonHelpSets", 79));
      column.setCellRenderer(new SentenceRenderer());

      column = getColumnModel().getColumn(1);
      column.setHeaderValue(LanguageText.getMessage("commonHelpSets", 81));
      column.setCellRenderer(new SentenceRenderer());
      column.setResizable(false);
      column.setMaxWidth(ICON_SIZE*4);
      column.setMinWidth(ICON_SIZE*4);
      
      column = getColumnModel().getColumn(2);
      column.setHeaderRenderer(new SentenceRenderer());
      column.setCellRenderer(new SentenceRenderer());
      
      URL url = getClass().getResource("/data/image.png");
      column.setHeaderValue(new ImageIcon(url));
      column.setResizable(false);
      column.setMaxWidth(ICON_SIZE);
      column.setMinWidth(ICON_SIZE);

      getTableHeader().setBackground(new Color(230,230,230));
      
      new ImageAudioDropTargetListener(lesson, this);
    }
    
        /** Method to input audio, gloss, and native text  */
    private void audioDialog()
    {  int row = getSelectedRow();
       if (row<0) { Toolkit.getDefaultToolkit().beep(); return; }

       SentenceData sentenceData = lesson.getSentenceData().get(row);
       LessonActionSentenceData oldData = new LessonActionSentenceData
                               (sentenceData.clone(), lesson.getLayer(), row);

       PicturesSoundData sounds = sentenceData.getAudio();
       Frame frame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class,this);
       int result 
           = sounds.pictureDialog(lesson, frame, PicturesSoundData.DESCRIPTION);

       if(result != JOptionPane.CANCEL_OPTION)
       {  lesson.pushUndo(oldData);
          lesson.setDirty(true);
       }

    }  // End audioDialog()

    
    public @Override void changeSelection
                 (int row, int column, boolean toggle, boolean extend)
    {
        if (row == -1 || column !=0) { return; }
        
        if (!toggle)
        if (getSelectedRow() == row)
        {  audioDialog();  }
        else
        {  String data = (String)getValueAt(row, 0);
           textField.setText(data);
        }
        super.changeSelection (row, column, toggle, extend);
    }
    
   /** Method to handle user drag & drop of audio file
    *
    * @param audio The dropped file object (pass it to the SoundListener object
    */
   public void audioDropped(File audio)
   {  
      Point point = getMousePosition();
      int row = -1;
      if (point!=null) row = rowAtPoint(point);
      if (row<0)
      {
          Toolkit.getDefaultToolkit().beep();
          return;
      }
      int column = columnAtPoint(point);
      
      try
      {
          SentenceData sentenceData = lesson.getSentenceData().get(row);
          LessonActionSentenceData oldData = new LessonActionSentenceData
                               (sentenceData.clone(), lesson.getLayer(), row);

          SoundData sound = new SoundData();
          sound.readFile(audio);
          setValueAt(sound, row, 1);
          SentenceEditor editor 
                            = (SentenceEditor)getDefaultEditor(SoundData.class);
          editor.setCellEditorValue(dragPoint.x, column, sound);
          editor.stopCellEditing();
          
          lesson.setDirty(true);
          lesson.pushUndo(oldData);
      }
      catch (Exception e) { lesson.setText(e.toString()); }
      repaint();
   }
   
    /** Methods to respond to mouse events responding to list clicks */
    public void mouseExited(MouseEvent event)  {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}

    public void mouseReleased(MouseEvent event)
    {   
        Point point = event.getPoint();
        int row = rowAtPoint(point);
        int column = columnAtPoint(point);
 
        if (dragPoint.x>=0 && (row<0 || column<0)) 
        {
           if (dragPoint.y==2) lesson.removePicture(dragPoint.x);
           if (dragPoint.y==1) 
           {
               SoundData sound 
                           = (SoundData)getValueAt(dragPoint.x, dragPoint.y);
               if (!sound.isRecorded())
               {
                   Toolkit.getDefaultToolkit().beep();
                   return;
               }
               
               SentenceAudioPictureData data  
                                    = lesson.getSentenceData().get(dragPoint.x);
               LessonActionSentenceData oldData = new LessonActionSentenceData
                                (data.clone(), lesson.getLayer(), dragPoint.x);
               
               sound = new SoundData();
               setValueAt(sound, dragPoint.x, dragPoint.y);
               SentenceEditor editor 
                            = (SentenceEditor)getDefaultEditor(SoundData.class);
               editor.setCellEditorValue(dragPoint.x, column, sound);
               editor.stopCellEditing();
               lesson.setDirty(true);
               lesson.pushUndo(oldData);
           }
        }
        dragPoint = new Point(-1, -1);
    }

    public void mouseMoved(MouseEvent event) {}
    public void mouseDragged(MouseEvent event)
    {  Point point = event.getPoint();
       if (dragPoint.x>=0) return;
       dragPoint.y = columnAtPoint(point);
       if (dragPoint.y==2) dragPoint.x = rowAtPoint(point);
       if (dragPoint.y==1) dragPoint.x = rowAtPoint(point);
    }

    /** Class to handle sentence data with column 0 being text and column 1
     *     being a picture.
     */
    class SentenceTableModel extends AbstractTableModel
    {
    	private final static long serialVersionUID = 1;
    	
        private CategoryRelatedPhrases lesson;
        
        public SentenceTableModel(CategoryRelatedPhrases lesson)
        {   this.lesson = lesson;   }

        public int getColumnCount() { return 3; }
        public int getRowCount()
        { return lesson.getSentenceData().size();  }
 
        public Object getValueAt(int row, int col)
        {  SentenceAudioPictureData sentence = lesson.getSentenceData().get(row);

           if (col==0)  return sentence.getSentence();
           else if (col==1) 
           {  
              return sentence.getSound();
           }
           return sentence.getPicture();
        }

        public @Override Class<?> getColumnClass(int c)
        {   if (c==0) return String.class;
            else if (c==1) return SoundData.class;
            else      return PictureData.class;
        }

       public @Override boolean isCellEditable(int row, int col) 
       {
           return col==1;
       }

       public @Override void setValueAt(Object value, int row, int col)
       {   Vector<SentenceAudioPictureData> data = lesson.getSentenceData();
       
           SentenceAudioPictureData sentence = data.get(row);
           if (value instanceof SoundData)
           {
               sentence.setSound( (SoundData)value );
           }
           else if (value instanceof String)
           {
              sentence.setSentence((String)value);
           }
           else if (value instanceof PictureData)
           {
               sentence.setPicture( (PictureData)value );
           }
           fireTableRowsUpdated(row, row);
       }

   }   // End of SentenceTableModel

   /** Class to render the table cells, column 0 is a string and 
    *    column 1 is a panel, column 2 is a picture
    */
   class SentenceRenderer extends DefaultTableCellRenderer
   {
      private final static long serialVersionUID = 1;
	   
      RecordPanel panel;
      
      public SentenceRenderer() 
      {  super();
         panel = new RecordPanel(new SoundData());
         panel.setBackground(background);
         setBackground(background);
      }

      public @Override void setValue(Object value)
      {  
         if (value==null) { setText(null); setIcon(null); }
         else if (value instanceof PictureData)
         {  PictureData data = (PictureData)value;
            setIcon(data.getIcon(new Dimension(ICON_SIZE,ICON_SIZE)));
         }
         else  if (value instanceof ImageIcon)
              setIcon((ImageIcon)value);
         else if (value instanceof SoundData)
         {  
             panel.setSound((SoundData)value); 
         }
         else
         {  
             setText(value.toString()); 
         }
         
      }

      public @Override Component getTableCellRendererComponent
	        (JTable table, Object value, boolean isSelected,
	                       boolean hasFocus, int row, int column)
      {
         Component cell = super.getTableCellRendererComponent
                 (table, value, isSelected, hasFocus, row, column);
         
         if (row<0) return cell;
         setValue(value);
         
         if (column==1) 
         {
        	 cell = panel;
         }
         
         int height = getRowHeight();
         int fontSize = 12;
         if (height<fontSize || height<ICON_SIZE)
         {  int newHeight = fontSize;
            if (newHeight<ICON_SIZE) newHeight = ICON_SIZE;
            if (newHeight!=height) setRowHeight(newHeight);
         }
         cell.setFont(new Font(null, Font.PLAIN, fontSize));
         setPanelBackground(isSelected, row, column, (JComponent)cell);
         return cell;
      }
   }     // End of SentenceRenderer class
   public static int count = 0;
   class SentenceEditor extends AbstractCellEditor implements TableCellEditor
   {  
     private final static long serialVersionUID = 1;
	   
      RecordPanel panel;
      int panelRow;
      
      public SentenceEditor()
      {
          panel = new RecordPanel(new SoundData());
          panel.setBackground(background);
      }
   
      public Component getTableCellEditorComponent
           (JTable table, Object value, boolean isSelected, int row, int column) 
      {
          if (column!=1) 
              throw new RuntimeException("Bad editing cell: "+row+"/"+column);
          
          if (row<0) 
              throw new RuntimeException("Illegal row: " + row);
          
          panel.setSound((SoundData)value);
          setPanelBackground(isSelected, row, column, panel);
          
          SentenceData sentenceData = lesson.getSentenceData().get(row);
          LessonActionSentenceData oldData = new LessonActionSentenceData
                               (sentenceData.clone(), lesson.getLayer(), row);

          // Consider not selected because it will change the background color
          panel.setUndo(lesson, oldData, scheme, false);
          panelRow = row;
          return panel;
      } 
      
      public Object getCellEditorValue() 
      {  
        return panel.getSound();
          
      }
      
      /** Update the sound  editor cell when a sound is removed or dropped
       * 
       * @param row Which table row
       * @param sound  Audio object
       */
      public void setCellEditorValue(int row, int column, SoundData sound)
      {
         if (row!=panelRow) return;
         
         setPanelBackground(true, row, column, panel); 
         panel.setSound(sound);
      }
   }
    
   /** Set background color appropriately
     *
     *  @param select Set to true if the column is selected
     *  @paeam row The table row number
     *  @param col The table column number
     *  @param cell The desired panel
     * 
     */
   private void setPanelBackground(boolean select, int row, int col, JComponent cell)
   {
         setBackground(background);
         setForeground(Color.black);
        	 
    	 if (col==2)
    		 cell.setBackground(background);
    	 
         if (select && col == 0)
         {
        	 cell.setBorder(coloredBorder);
         }
         else 
             cell.setBorder(emptyBorder);;
   }
}  // End of PhrasesTable class
