/*
 * JFontChooser.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.language;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.acorns.lesson.AcornsProperties;

public class JFontChooser extends JDialog
{
   private static final long serialVersionUID = 1;

   static KeyboardFonts saveKeyboards = null;
   
   protected KeyboardFonts    keyboards;
   protected DefaultListModel<String> model;
   protected JList<String>    infoList;
   protected JComboBox<String>sizeCombo;
   protected JComboBox<String>  fontCombo;
   protected JLabel           preview;
   protected JTextField       languageText;

   private String[] buttonNames;
	
   public JFontChooser(KeyboardFonts keyboardFonts)
   {  
	  super(JOptionPane.getRootFrame()
              , LanguageText.getMessage("commonHelpSets", 36), true);

      buttonNames = LanguageText.getMessageList("commonHelpSets", 45);
	
      // Save the available language keyboards.
      saveKeyboards = null;
      if (keyboardFonts==null) keyboardFonts = new KeyboardFonts();
          keyboards = (KeyboardFonts)keyboardFonts.clone();
		
      // Get current font.
      Font active = keyboards.getActiveFont();

      // Find index to active font
      // Set icon into root frame
      // Get the ACORNS property change listener.
      AcornsProperties properties = AcornsProperties.getAcornsProperties();
      JOptionPane.getRootFrame().setIconImage
                    (properties.getIcon(AcornsProperties.ACORN ,20).getImage());
       
      // Create label for previewing the font
      Color grey = new Color(192,192,192);
      Color optionColor = new Color(220, 220, 220);

      JPanel previewPanel = new JPanel();
      previewPanel.setFont(active);
      previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.X_AXIS));
      previewPanel.setBackground(grey);
      preview
          = new JLabel("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
                      , JLabel.CENTER );
      previewPanel.add(Box.createHorizontalGlue());
      previewPanel.add(preview);
      previewPanel.add(Box.createHorizontalGlue());
      keyboards.setFont(preview);
    
      // Configure font size options
      String[] sizes = {"8", "10", "12", "14", "16", "18", "20", "24"};
      sizeCombo = new JComboBox<String>( sizes );
      sizeCombo.setSize(new Dimension(65,25));
      sizeCombo.setPreferredSize(sizeCombo.getSize());
      sizeCombo.setBackground(optionColor);
      sizeCombo.setEditable(false);
      sizeCombo.setSelectedItem("" + active.getSize());
      sizeCombo.addActionListener(
            new ActionListener()
            {  public void actionPerformed(ActionEvent e) { previewFont(true); }
            });

      // Configure font name options
      fontCombo = keyboards.createLanguageComboBox(null, true);
      fontCombo.setBackground(optionColor);
      fontCombo.setEditable(false);
      fontCombo.addActionListener(
         new ActionListener() 
         {  
        	 public void actionPerformed(ActionEvent e) 
        	 { 
        		 previewFont(true); 
        	 }
         });
 
      // Create language label and text field.
      JLabel languageLabel
              = new JLabel(LanguageText.getMessage("commonHelpSets", 37));
      languageText = new JTextField();
      Dimension size = new Dimension(125, 25);
      languageText.setMaximumSize(size);
      languageText.setPreferredSize(size);
      languageText.setSize(size);
      languageText.setText(keyboards.getLanguage());
      languageText.setToolTipText(LanguageText.getMessage("commonHelpSets",38));
      languageText.setFont(active);
      
      // Create panel for dropdowns for font, font size, and language
      JPanel selections = new JPanel();
      selections.setLayout(new BoxLayout(selections, BoxLayout.X_AXIS));
      selections.setBackground(grey);
      selections.add(Box.createHorizontalStrut(50));
      selections.add(fontCombo);
      selections.add(Box.createHorizontalStrut(10));
      selections.add(sizeCombo);
      selections.add(Box.createHorizontalStrut(10));
      selections.add(languageLabel);
      selections.add(languageText);
      selections.add(Box.createHorizontalStrut(50));
      
      // Create a Scrollable list of current selections
      String[] info = keyboards.getLanguageInfo();
      model = new DefaultListModel<String>();
      infoList = new JList<String>(model);
      infoList.setCellRenderer(new InfoListRenderer());
      for (int i=0; i<info.length; i++) { model.add(i, info[i]); }
      infoList.setSelectedIndex(keyboards.getActiveIndex());
      infoList.addListSelectionListener(
            new ListSelectionListener() 
            {  public void valueChanged(ListSelectionEvent e) 
				           {   
            					previewFont(false); 	
            				}
            });
		
      JScrollPane scroll = new JScrollPane(infoList);
      scroll.getVerticalScrollBar().setUnitIncrement(30);

      JPanel scrollPanel = new JPanel();
      scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.X_AXIS));
      scrollPanel.setBackground(grey);
      scrollPanel.add(Box.createHorizontalGlue());
      scrollPanel.add(scroll);
      scrollPanel.add(Box.createHorizontalGlue());

      // Create the button to remove keyboard languages
      JButton removeButton = new JButton(buttonNames[0]);
      removeButton.setBackground(optionColor);
      removeButton.addActionListener(
              new ActionListener()
              {  public void actionPerformed (ActionEvent e)
                 {
                     int selection = infoList.getSelectedIndex();
                     Frame root = JOptionPane.getRootFrame();
                     if (selection<0)
                          JOptionPane.showMessageDialog
                          (root, LanguageText.getMessage("commonHelpSets", 39));
                     else if (selection==0) 
                          JOptionPane.showMessageDialog
                          (root, LanguageText.getMessage("commonHelpSets", 40));
                     else 
                     {   keyboards.removeLanguage(selection);
                         updateList(false, false);
                     }                    
                 }
              });
           
      // Create the button to set the active language
      JButton setButton = new JButton(buttonNames[1]);
      setButton.setBackground(optionColor);
      setButton.addActionListener(
        new ActionListener()
              {  public void actionPerformed (ActionEvent e)
                 {
                     int selection = infoList.getSelectedIndex();
                     Frame root = JOptionPane.getRootFrame();
                     if (selection<0)
                          JOptionPane.showMessageDialog
                          (root, LanguageText.getMessage("commonHelpSets", 41));
                     else 
                     {   keyboards.setLanguage(selection);
                         updateList(false, false);
                     }                    
                 }
              });
  
		    
      // Create the button to add keyboard languages
      JButton addButton = new JButton(buttonNames[2]);
      addButton.setBackground(optionColor);
      addButton.addActionListener(
          new ActionListener()
          {   public void actionPerformed(ActionEvent e)
              {  
	               String name = (String)fontCombo.getSelectedItem();
	               String size = (String)sizeCombo.getSelectedItem();
	               String language = languageText.getText();
	               Frame root = JOptionPane.getRootFrame();
	               
	               if (name==null)
	                  JOptionPane.showMessageDialog
	                     (root, LanguageText.getMessage("commonHelpSets", 42));
	               else if (size==null)
	                   JOptionPane.showMessageDialog
	                      (root, LanguageText.getMessage("commonHelpSets", 43));
	               else if (language==null || (language.length()==0))
	                   JOptionPane.showMessageDialog
	                      (root, LanguageText.getMessage("commonHelpSets", 44));
	               else
	               {   boolean insert = keyboards.addLanguage
	                           (name, Integer.parseInt(size), language);
	                   updateList(insert, true);
	               }
              }
          });
      
      // Create button to exit the dialog.
      JButton exitButton = new JButton(buttonNames[3]);
      exitButton.setBackground(optionColor);
      exitButton.addActionListener(
          new ActionListener()
          {  public void actionPerformed(ActionEvent e)
             {  saveKeyboards = keyboards;
				            dispose();  
				         }
          });
             
      // Create panel of option buttons
      JPanel buttons = new JPanel();
      buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
      buttons.setBackground(grey);
      buttons.add(Box.createHorizontalGlue());
      buttons.add(removeButton);
      buttons.add(Box.createHorizontalStrut(10));
      buttons.add(addButton);
      buttons.add(Box.createHorizontalStrut(10));
      buttons.add(setButton);
      buttons.add(Box.createHorizontalStrut(10));
      buttons.add(exitButton);
      buttons.add(Box.createHorizontalStrut(10));
      
      // Now create the main panel
      Container pane = getContentPane();
      pane.setBackground(grey);
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(selections);
      pane.add(Box.createVerticalStrut(10));
      pane.add(scrollPanel);
      pane.add(Box.createVerticalStrut(10));
      pane.add(previewPanel);
      pane.add(Box.createVerticalStrut(10));
      pane.add(buttons);
      
      pack(); 
      Dimension screen = getToolkit().getScreenSize();
      Rectangle bounds = getBounds();
      setLocation((screen.width - bounds.width) / 2
                                 , (screen.height - bounds.height) / 2);
   }
	
  /** Preview a font and set the active language
   *  @param dropDowns true if we should get the font information from drop downs.
   */
   private void previewFont(boolean dropDowns)
   {
      if (dropDowns)
      {
          int size = Integer.parseInt((String)sizeCombo.getSelectedItem());
    	  String fontName = (String)fontCombo.getSelectedItem();
    	  Font font = new Font(fontName, Font.PLAIN, size); 
          preview.setFont(font);
          languageText.setFont(font);
      }
      else  // Get preview from list selection
      {  int index = infoList.getSelectedIndex();
         if (index>=0)
         {  
        	Font font = keyboards.getFont(index);
            preview.setFont(font); 
            languageText.setText(keyboards.getLanguage(index));
            languageText.setFont(font);
            sizeCombo.setSelectedItem("" + font.getSize());
            fontCombo.setSelectedItem(font.getFamily());
         }
      }
   }
 
  /** Update list of languages */
   private void updateList(boolean inserted, boolean insertOperation)
   {
      String[] info = keyboards.getLanguageInfo();
      int oldIndex = infoList.getSelectedIndex();

       model.clear();
       for (int i=0; i<info.length; i++) model.add(i, info[i]);
       if (inserted)  infoList.setSelectedIndex(info.length-1);
       else
       {   if (insertOperation)  infoList.setSelectedIndex(oldIndex);
           else infoList.setSelectedIndex(keyboards.getActiveIndex());
       }
       previewFont(false);
   }

  /** Start a font chooser dialog */	
   public static void startDialog(KeyboardFonts keyboards)
   {  
	   JFontChooser chooser = new JFontChooser(keyboards);
       chooser.setVisible(true);
   }
	
  /** Return the result of a font chooser dialog
   * @return updated available language keyboards or null.
   */
   public static KeyboardFonts getResult() { return saveKeyboards; }

   
   private static class InfoListRenderer
   					implements ListCellRenderer<String>
   {
	   CustomRenderer renderer = null;
	   
	   protected CustomRenderer getRenderer()
	   { 
	       if (renderer == null)
	       {	
	    	   renderer = new CustomRenderer();
	       }
	       return renderer;
	   }
	   
	   public Component getListCellRendererComponent(
			   JList<? extends String> list, String languageData, int index,
			    boolean isSelected, boolean cellHasFocus) 
	   {
		   final Component result = getRenderer().getListCellRendererComponent(
				   list, languageData, index, isSelected, cellHasFocus);
	    
		   CustomRenderer renderer = (CustomRenderer)result;
		   renderer.setText(languageData);
		   return result;
	   }
	   private class CustomRenderer extends JPanel
	   {
		   private static final long serialVersionUID = 1L;
		   JLabel labels[] = new JLabel[4];
		   int[] widths = {60, 250, 60, 250};
		   
		   public CustomRenderer()
		   {
			   setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			   Dimension size;
			   for (int i=0; i<labels.length; i++)
			   {
				   labels[i] = new JLabel();
				   size = new Dimension(widths[i], 20);
				   labels[i].setPreferredSize(size);
				   labels[i].setMinimumSize(size);
				   labels[i].setMaximumSize(size);
				   labels[i].setSize(size);
				   add(labels[i]);
			   }
		   }
		   
		   public Component getListCellRendererComponent
		      (JList<? extends String> list, String language, int index, boolean isSelected, boolean cellHasFocus)
		   {
			  return this; 
		   }
		      
		   public void setText(String font)
		   {
			   labels[0].setText(font.substring(0, 4));
			   
			   String[] languageFields 
		   		= LanguageFont.extractToString
		   				(font.substring(4));
			   
			   labels[1].setText(languageFields[0]);
			   labels[1].setFont(new Font(languageFields[0], Font.PLAIN, 12));
			   labels[2].setText(languageFields[1]);
			   labels[3].setText(languageFields[2]);
			   labels[3].setFont(new Font(languageFields[0], Font.PLAIN, 12));
		   }
	   }
   }

}     // End of jFontChooser