/*
 * PhrasesSetupPanel.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.categories.relatedphrases;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.table.*;
import java.net.*;

import org.acorns.language.*;
import org.acorns.data.*;
import org.acorns.visual.*;

/**
 *  The setup panel class for related phrases lessons
 */
public class PhrasesSetupPanel extends JPanel implements ActionListener
{
   private final static long serialVersionUID = 1;
	
   private final static int ICON_SIZE  = 30;
   private final static int STRUT_WIDTH = 5;

   private JTextField              textField;
   private JTable                  sentences;
   private ColorScheme             colorScheme;
   private CategoryRelatedPhrases  lesson;
   private AbstractTableModel      sentenceTableModel;

   /** Constructor for the related phrases setup panel
    *
    * @param lesson The lesson object
    */
   public PhrasesSetupPanel(CategoryRelatedPhrases lesson)
   {
      colorScheme = lesson.getColorScheme();
      this.lesson = lesson;

      textField = new JTextField();
      textField.setToolTipText
              (LanguageText.getMessage("commonHelpSets", 78));
 
      Color color = new Color(230,230,230);
      textField.setBackground(color);
      textField.setRequestFocusEnabled(true);
      SwingUtilities.invokeLater(
        new Runnable()
        {   public void run()
            { try
              { Thread.sleep(500);
                    textField.grabFocus();
              }
              catch (Exception e) {}
            }
        });
 
      sentences = new PhrasesTable(lesson, textField);
      sentenceTableModel = (AbstractTableModel)sentences.getModel();
      
      JScrollPane center = new JScrollPane(sentences);
      center.setOpaque(false);
      center.getViewport().setOpaque(false);

      JPanel south = new JPanel();
      south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
      south.setBackground(new Color(80,80,80));

      String[] icons = LanguageText.getMessageList("commonHelpSets", 77);
      makeButton(south, "add", icons[0], "a", KeyEvent.VK_INSERT);
      south.add(Box.createHorizontalStrut(STRUT_WIDTH));
      makeButton(south, "edit", icons[1], "e", -1);
      south.add(Box.createHorizontalStrut(STRUT_WIDTH));
      makeButton(south, "garbage", icons[2], "r", KeyEvent.VK_DELETE);
      south.add(Box.createHorizontalStrut(STRUT_WIDTH));
      south.add(textField);

      setLayout(new BorderLayout());
      add(south, BorderLayout.SOUTH);
      add(center, BorderLayout.CENTER);
   }  // End of Constructor()

   /** Method to return which row is selected
    *
    * @return row number or -1 if none
    */
    public int getSelectedRow() { return sentences.getSelectedRow(); }

   /** Paint component draws the background
    *
    * @param graphics The graphics drawing object
    */
   public @Override void paintComponent(Graphics graphics)
   {
	   super.paintComponent(graphics);
       PictureData picture = colorScheme.getPicture();
       Dimension size = getSize();

       if (picture !=null)
       {  BufferedImage image = picture.getImage
                   (this, new Rectangle(0, 0, size.width, size.height));
          graphics.drawImage(image, 0, 0, size.width, size.height, null);
       }
       else
       {
          Color color = colorScheme.getColor(true);
          graphics.setColor(color);
          graphics.fillRect(0, 0, size.width, size.height);
       }
   }

    /** Method to create a button with an Icon image and add it to a JPanel
     *
     * @param panel The panel in which to put the button
     * @param icon The name of the icon image file without the extension
     * @param toolTip A tool tip to help the user
     * @param buttonName The name of the button for the action listener
     * @param hotKey A hot key to use to trigger the button event
     */
    private void makeButton(JPanel panel, String icon,
                               String toolTip, String buttonName, int hotKey)
    {
       URL url = getClass().getResource("/data/" + icon + ".png");
       if (url==null) return;

       Image newImage  = Toolkit.getDefaultToolkit().getImage(url);
		 newImage = newImage.getScaledInstance
                      (ICON_SIZE, ICON_SIZE, Image.SCALE_REPLICATE);

       JButton button = new JButton(new ImageIcon(newImage));
       button.setName(buttonName);
       button.setPreferredSize(new Dimension(ICON_SIZE+1, ICON_SIZE+1));
       button.setMaximumSize(new Dimension(ICON_SIZE+1, ICON_SIZE+1));
       button.setMinimumSize(new Dimension(ICON_SIZE+1, ICON_SIZE+1));
       button.setToolTipText(toolTip);
       if (hotKey!=-1) button.setMnemonic(hotKey);
       button.setBackground(new Color(230,230,230));
       button.addActionListener(this);
       panel.add(button);
	 }  // End of makeButton()

    /** Listener to respond to setup panel add, edit, and remove buttons
     *
     * @param event The event object
     */
    public void actionPerformed(ActionEvent event)
    {
       String sentence = textField.getText();
       textField.requestFocusInWindow();

       JButton button = (JButton)event.getSource();
       String name = button.getName();
       if (name.equals("a"))
       {  if (sentence.length()==0) 
          {  Toolkit.getDefaultToolkit().beep();
             return;
          }
          else
          {  int row = sentences.getRowCount();
             LessonActionSentenceData oldData = new LessonActionSentenceData
                          (null, lesson.getLayer(), row);

             SentenceData sentenceData = new SentenceAudioPictureData(sentence);
             addRow((SentenceAudioPictureData)sentenceData);
             selectRow(row);
             lesson.pushUndo(oldData);
             lesson.setDirty(true);
          }
       }
       else  // Process edit or remove options
       {
          int row = sentences.getSelectedRow();
          if (row<0) {  Toolkit.getDefaultToolkit().beep();
                        return;
                     }

          SentenceData data = lesson.getSentenceData().get(row);
          LessonActionSentenceData oldData = new LessonActionSentenceData
                          (data.clone(), lesson.getLayer(), row);

          if (name.equals("e"))
          {
             if (sentence.length()==0) Toolkit.getDefaultToolkit().beep();
             else  { sentenceTableModel.setValueAt(sentence, row, 0); }
          }
          else  
          {  removeRow(row);
             selectRow(row);
          }
          lesson.pushUndo(oldData);
          lesson.setDirty(true);
       }
    }  // End of actionPerformed()
    
    /** Add a row to the table 
     *
     * @param row The desired row to add
     */
    private void addRow(SentenceAudioPictureData rowData)
    {   
        Vector<SentenceAudioPictureData> data = lesson.getSentenceData();
        data.add(rowData);
        sentenceTableModel.fireTableRowsInserted
                                            ( data.size() - 1, data.size() - 1);
    }
    
    /** Remove a row from the table 
     *
     * @param row The desired row to remove
     */
    private void  removeRow(int row)
    {   Vector<SentenceAudioPictureData> data = lesson.getSentenceData();
        data.remove(row);
        sentenceTableModel.fireTableDataChanged();
    }

    /** Method to select a row and make sure that it is visible
     *
     * @param row The desired row to select
     */
    private void selectRow(int row)
    {
       int rows = sentenceTableModel.getRowCount();
       if (rows==0) return;
       if (row >= rows) row = rows - 1;

       sentences.scrollRectToVisible(sentences.getCellRect(row, 0, true));
       sentences.setRowSelectionInterval(row, row);
    }
    
   /** Method to get the row over which the mouse is positioned
     * 
     * @return row number or -1 if the mouse is not positioned over a row
     */
    public int getMouseRowPosition()
    {
      Point point = sentences.getMousePosition();
      int row = -1;
      if (point!=null) row = sentences.rowAtPoint(point);
      return row;
    }
}      // End of PhrasesSetupPanel class
