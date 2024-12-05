/**
 * SetupPanel.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.help.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.*;

import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.language.*;

/** Create the components that will reside in the secondary lesson toolbar */
public class SetupPanel 
{
    /** The height of the secondary tool bar panel */
    public  static final int HEIGHT = 25;
    
    private static JPanel    controlPanel;
	private static JButton   rotateButton;
    private static JButton   backgroundButton, foregroundButton, pictureButton;
    private static JButton   leftButton, centerButton;
    private static JComboBox<String> scaleBox, fontBox, pictureScaleBox;
    private static JSlider   sliderLayer;
    private static JLabel    linkLabel, scaleLabel, fontLabel, linkToLabel;
    private static JButton   helpButton, anchorButton;

    private static ColorScheme colors;
    private static Method getPictureData;
    private static Method getSelectedPicture;
    private static Method copyPaste;
    private static Method getAnnotationData;
    
    private static Lesson lesson;

    /** Foreground and background color buttons */
    public static final int FGBG = 1;
    /** Scale picture size drop down */
    public static final int SCALE = 2;
    /** Rotate picture button */
    public static final int ROTATE = 4;
    /** Insert picture button */
    public static final int PICTURE = 8;
    /** Fill picture to button */
    public static final int FILL = 16;
    /** Font size drop down */
    public static final int FONT = 32;
    /** Alignment buttons */
    public static final int ALIGN = 64;
    
    /** Create the setup secondary tool bar panel for a lesson
     * 
     * @param lesson The lesson object
     * @param helpCat The id of the help page for the setup panel
     * @param options The component options
     *       FGBG, SCALE, ROTATE, PICTURE, FILL, FONT, ALIGN
     * @return The created panel
     */
    public static JPanel createSetupPanel
    (Lesson lesson, String helpCat, int options)
    {
    	return createSetupPanel(lesson, null, helpCat, options);
    }

    /** Create the setup secondary tool bar panel for a lesson
     * 
     * @param lesson The lesson object
     * @param auxiliary Additional panel that specific lesson might want to add
     * @param helpCat The id of the help page for the setup panel
     * @param options The component options
     *       FGBG, SCALE, ROTATE, PICTURE, FILL, FONT, ALIGN
     * @return The created panel
     */
    public static JPanel createSetupPanel
            (Lesson lesson, JPanel auxiliary, String helpCat, int options)
    {
       SetupPanel.lesson = lesson;

       // Complete the secondary toolbar panel.
       if (controlPanel!=null) 
       { controlPanel.removeAll(); }
       else
       {
          controlPanel = new JPanel();
          BoxLayout box = new BoxLayout(controlPanel, BoxLayout.X_AXIS);
          controlPanel.setLayout(box);
          controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
       }


      Dimension size = lesson.getDisplaySize();
      size.height    = HEIGHT;
      controlPanel.setPreferredSize(size);
      controlPanel.setBackground(new Color(210,210,210));

      /** See if we should include color buttons */
       Method getColorScheme;
       Class<?> lessonClass = lesson.getClass();
       try
       {
          getColorScheme = lessonClass.getMethod("getColorScheme");
          colors = (ColorScheme)getColorScheme.invoke(lesson, new Object[0]);
       }
       catch (Exception e)  { colors = null; }

       /** See if we should include a background picture */
       Class<?>[] params = new Class[1];
       params[0] = String.class;
       try
       { getSelectedPicture = lessonClass.getMethod
                  ("getSelectedPicture", params);
       }
       catch (Exception e)  { getSelectedPicture = null; }

       try
       { 
    	   Class<?>[] copyPasteParams = new Class[1];
    	   copyPasteParams[0] = JButton.class;
    	   copyPaste = lessonClass.getMethod
                  ("copyPaste", copyPasteParams);
       }
       catch (Exception e)  { copyPaste = null; }

       /** See if we should include a background picture */
       try
       { getPictureData = lessonClass.getMethod("getPictureData", params); }
       catch (Exception e)  { getPictureData = null; }

       /** See if we should include a background picture */
       try
       { getAnnotationData
                  = lessonClass.getMethod("getAnnotationData");
       }
       catch (Exception e)  { getAnnotationData = null; }

      // Add help button.
      JButton help
          = getHelpButton(LanguageText.getMessage("commonHelpSets",25),helpCat);
      if (help!=null)
      {
          controlPanel.add(Box.createRigidArea(new Dimension(10,0)));
          controlPanel.add(help);
      }
      controlPanel.add(Box.createHorizontalStrut(2));

      // Create label component for control panel.
      String[]    lessonInfo   = lesson.getLessonHeader();
      String      title        = lessonInfo[AcornsProperties.TITLE];
      JLabel      titleLabel   = new JLabel(title);
      titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      controlPanel.add(titleLabel);
      controlPanel.add(Box.createHorizontalStrut(2));

      JSlider slider = getLayerSlider();
      controlPanel.add(slider);
      controlPanel.add(Box.createHorizontalStrut(2));
      if ((options & PICTURE) != 0)
      {  controlPanel.add(getPictureButton());
         controlPanel.add(Box.createHorizontalStrut(2));
      }

      if ((options & FILL) != 0 )
      {
         controlPanel.add(getScaleBox());
         controlPanel.add(Box.createHorizontalStrut(2));
      }

      if ((options & ROTATE) != 0)
      {   controlPanel.add(getRotateButton());
          controlPanel.add(Box.createHorizontalStrut(2));
      }

      String[] labels = LanguageText.getMessageList("commonHelpSets", 8);
      if ((options & SCALE) != 0)
      {  if (scaleLabel==null) scaleLabel = new JLabel(labels[0]);
         controlPanel.add(scaleLabel);
         controlPanel.add(getPictureScaleBox());
         controlPanel.add(Box.createHorizontalStrut(2));
      }

      if ((options & FGBG) != 0)
      {  controlPanel.add(getForegroundButton());
         controlPanel.add(Box.createHorizontalStrut(3));
         controlPanel.add(getBackgroundButton());
         controlPanel.add(Box.createHorizontalStrut(3));
      }

      if ((options & FONT) != 0)
      {   if (fontLabel==null) { fontLabel = new JLabel(labels[1]); }
          controlPanel.add(fontLabel);
          controlPanel.add(getFontBox());
          controlPanel.add(Box.createHorizontalStrut(2));
      }

      if ((options & ALIGN) != 0)
      {
          controlPanel.add(getLeftButton());
          controlPanel.add(getCenterButton());
          controlPanel.add(Box.createHorizontalStrut(2));
          setJustifiedButtonBorders();
      }
      
      if (copyPaste != null)
      {
    	  String[] msgs = LanguageText.getMessageList("commonHelpSets", 101);

    	  JButton copyButton = getCopyPasteButton(AcornsProperties.COPY, msgs[0], msgs[2]);
    	  controlPanel.add(copyButton);
	      controlPanel.add(Box.createHorizontalStrut(2));

	      JButton pasteButton = getCopyPasteButton(AcornsProperties.PASTE, msgs[1], msgs[3]);
    	  controlPanel.add(pasteButton);
          controlPanel.add(Box.createHorizontalStrut(2));
      }
      
      if (auxiliary != null)
      {
    	  controlPanel.add(auxiliary);
    	  controlPanel.add(Box.createHorizontalStrut(2));
      }

      if (linkToLabel==null) linkToLabel = new JLabel(labels[2]);
      controlPanel.add(linkToLabel);
      controlPanel.add(Box.createHorizontalStrut(2));

      controlPanel.add(getAnchorButton());
      controlPanel.add(Box.createHorizontalStrut(2));

      controlPanel.add(getLinkLabel());
      controlPanel.add(Box.createHorizontalGlue());
      return controlPanel;

   }  // End getControlPanel()

    /**  Method to get the combo box for scaling pictures to buttons.
    *
    * @return JComboBox component
    */
   private static JComboBox<String> getScaleBox()
   {
      if (scaleBox == null)
      {
         String[] items = LanguageText.getMessageList("commonHelpSets", 9);

         scaleBox = new JComboBox<String>(items);
         scaleBox.setBackground(new Color(210,210,210));
         scaleBox.setMaximumSize(new Dimension(50, 20));
         scaleBox.setToolTipText(LanguageText.getMessage("commonHelpSets", 10));
         scaleBox.setEditable(false);
         scaleBox.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent event)
               {
                  PictureChoice choice;
                  try
                  { choice = (PictureChoice)getSelectedPicture.invoke
                        (lesson, new Object[]{""});
                  }
                  catch (Exception e) { choice = null; }
                  if (choice==null) return;

                  int index = scaleBox.getSelectedIndex();
                  choice.setType(index);
				              lesson.setDirty(true);
                  lesson.displayLesson();
               }     // End of actionPerformed.
            }        // End of anonymous ActionListener.
         );          // End of addActionListener.
      }              // End of if (scaleBox==null).

      int selection;
      PictureChoice choice;
      try
      { choice = (PictureChoice)getSelectedPicture.invoke
                                           (lesson, new Object[0]);
      }
      catch (Exception e) { choice = null; }
      if (choice==null) selection = 0;
      else             selection = choice.getType();

      setScaleBox(selection);
      return scaleBox;
   }                 // End of getScaleBox().

   /** Method to set a new combo box value after selecting a picture */
   public static void setScaleBox(int selection)
   {
      ActionListener[] listeners = scaleBox.getActionListeners();
      scaleBox.removeActionListener(listeners[0]);
      scaleBox.setSelectedIndex(selection);
      scaleBox.addActionListener(listeners[0]);
   }
   
   /** Button for copying or pasting a portion of a lesson
    * 
    * @param type Type of button (AcornsProperties.COPY or AcornsProperties.PASTE)
    * @param name Name of the button
    * @param tip Tool tip text
    * @return The created button
    */
   private static JButton getCopyPasteButton(int type, String name, String tip)
   {
	      // Create copy and paste buttons for control panel.
	      JButton copyPasteButton  = new JButton
	              (lesson.getIcon(type, Lesson.ICON));
	      
          copyPasteButton.setBorder(BorderFactory.createRaisedBevelBorder());
          copyPasteButton.setToolTipText(tip);
	      copyPasteButton.setName(name);
	      
	      copyPasteButton.addActionListener(
	         new ActionListener()
	         {
	            public void actionPerformed(ActionEvent event)
	            {
	                try
	                {
	                   Object object = event.getSource();
	                   if (object instanceof JButton)
	                	   copyPaste.invoke(lesson, (JButton)object);
	                }
	                catch (Exception e)  
	                {
	                	System.out.println(e);
	                }
	            }
	         });
	   	     return copyPasteButton;
   }

   /** Button to rotate pictures
    *
    * @return JButton to rotate pictures
    */
   private static JButton getRotateButton()
   {
      if (rotateButton != null) return rotateButton;

      rotateButton  = new JButton
           (lesson.getIcon(AcornsProperties.ROTATE, Lesson.ICON));
      rotateButton.setBorder(BorderFactory.createRaisedBevelBorder());
      rotateButton.setToolTipText(LanguageText.getMessage("commonHelpSets",11));
      rotateButton.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent event)
               {
                  PictureData pictureData;
                  try
                  {  pictureData =(PictureData)getPictureData.invoke(lesson,"");
                  }
                  catch (Exception e) { pictureData = null; }
                  if (pictureData==null) return;

                  int angle = pictureData.getAngle();
                  angle = (angle +90) % 360;
                  pictureData.setAngle(angle);
                  lesson.setDirty(true);
                  lesson.displayLesson();
               }  // End of actionPerformed.
            }     // End of anonymous ActionListener.
     );           // End of addActionListener.
     return rotateButton;
   }    // End of rotateButton.

   /** Method to get the widget to scale a picture */
   private static JComboBox<String> getPictureScaleBox()
   {
       PictureData pictureData = null;
       try
       {  pictureData = (PictureData)getPictureData.invoke(lesson, "");  }
       catch (Exception e) { pictureData = null; }

      if (pictureScaleBox == null)
      {  int scaleFactor = 100;
         if (pictureData!=null)  scaleFactor = pictureData.getScale();
         String[] items =
                    {  ""+scaleFactor + "%", "20%",  "33%",  "50%",  "66%"
                       , "75%", "100%", "150%", "200%", "300%", "400%", "500%"};

         pictureScaleBox = new JComboBox<String>(items);
         pictureScaleBox.setBackground(new Color(210,210,210));
         pictureScaleBox.setSelectedItem(0);
         pictureScaleBox.setEditable(true);
         pictureScaleBox.setSize(new Dimension(80, Lesson.ICON+5));
         pictureScaleBox.setPreferredSize(pictureScaleBox.getSize());
         pictureScaleBox.setMinimumSize(pictureScaleBox.getSize());
         pictureScaleBox.setMaximumSize(pictureScaleBox.getSize());
         pictureScaleBox.setToolTipText
                 (LanguageText.getMessage("commonHelpSets", 12));
         pictureScaleBox.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent event)
               {
                  String answer = (String)pictureScaleBox.getSelectedItem();
                  int    index  = answer.lastIndexOf("%");

                  if (index >=0)
                  {
                     answer = answer.substring(0,index);
                  }

                  PictureData picture;
                  try
                  {  picture = (PictureData)getPictureData.invoke(lesson, "");
                  }
                  catch (Exception e) { picture = null; }

                  if (picture==null)
                  {
                      Toolkit.getDefaultToolkit().beep();
                      return;
                  }
                  try
                  {
                     int factor = Integer.parseInt(answer);
                     if (factor<PictureData.MIN_SCALE
                             || factor>PictureData.MAX_SCALE)
                     {
                        throw new NumberFormatException();
                     }
                     picture.setScale(factor);
							              lesson.setDirty(true);
                     lesson.displayLesson();
                  }
                  catch (NumberFormatException exception)
                  {
                      setScaleBox(pictureScaleBox, picture.getScale() + "%");
 							              Toolkit.getDefaultToolkit().beep();
                  }
               }     // End of actionPerformed.
            }        // End of anonymous ActionListener.
         );          // End of addActionListener.
      }              // End of if (comboBox==null).

      if (pictureData!=null)
         setScaleBox(pictureScaleBox, pictureData.getScale() + "%");
      return pictureScaleBox;
   }

   /** Method to get background button
    *  @return background color choice button
    */
   private static JButton getBackgroundButton()
   {
       if (backgroundButton == null)
       {
           backgroundButton = new JButton
              (lesson.getIcon(AcornsProperties.BACKGROUND, Lesson.ICON));
           backgroundButton.setBorder(BorderFactory.createRaisedBevelBorder());
           backgroundButton.setToolTipText
                             (LanguageText.getMessage("commonHelpSets", 13));
           backgroundButton.addActionListener(
              new ActionListener()
              {
                  public void actionPerformed(ActionEvent event)
                  {
                     if (!colors.setColor(backgroundButton, true))
                          lesson.setText
                                (LanguageText.getMessage("commonHelpSets", 14));
                     else { lesson.setDirty(true);
                            lesson.displayLesson();
                          }
                  }  // End of actionPerformed
              }      // End of Action Listerner
           );        // End of addActionListener
       }             // End if button exists
       return backgroundButton;
   }                 // End getBackgroundButton

   /** Method to get foreground button
    *  @return background color choice button
    */
   private static JButton getForegroundButton()
   {
       if (foregroundButton == null)
       {
           foregroundButton = new JButton
              (lesson.getIcon(AcornsProperties.FOREGROUND, Lesson.ICON));

           foregroundButton.setBorder(BorderFactory.createRaisedBevelBorder());
           foregroundButton.setToolTipText(LanguageText.getMessage
                                                       ("commonHelpSets", 15));
           foregroundButton.addActionListener(
              new ActionListener()
              {
                  public void actionPerformed(ActionEvent event)
                  {
                     if (!colors.setColor(foregroundButton, false))
                          lesson.setText
                                (LanguageText.getMessage("commonHelpSets", 14));
                     else  {  lesson.setDirty(true);
                              lesson.displayLesson();
                           }
                  }  // End of actionPerformed
              }      // End of Action Listerner
           );        // End of addActionListener
       }             // End if button exists
       return foregroundButton;
   }                 // End getForegroundButton

   /** Method to get a button to load a background picture
    *
    * @return The button to press to import a background picture
    */
   private static JButton getPictureButton()
   {
       if (pictureButton==null)
       {
           pictureButton = new JButton
              (lesson.getIcon(AcornsProperties.IMAGE, Lesson.ICON));
           pictureButton.setBorder(BorderFactory.createRaisedBevelBorder());
           pictureButton.setToolTipText
                               (LanguageText.getMessage("commonHelpSets", 16));
           pictureButton.addActionListener(
              new ActionListener()
              {
                  public void actionPerformed(ActionEvent event)
                  {
                     try
                     {
                        URL url = lesson.getPicture();
                        if (url==null) throw new IOException();

                        PictureData picture = new PictureData(url, null);
                        colors.setPicture(picture);
                        lesson.setDirty(true);
                        lesson.displayLesson();
                     }
                     catch (Exception e)
                     {    lesson.setText
                              (LanguageText.getMessage("commonHelpSets", 17)); }
                  }  // End of actionPerformed
              }      // End of Action Listerner
           );        // End of addActionListener
       }
       return pictureButton;

   }

    /** Method to get the slider component.
     *
     * @return language level JSlider component
     */
    //------------------------------------------------------------
    private static JSlider getLayerSlider()
    {
      int startLayer = lesson.getLayer();
      if (sliderLayer == null)
      {
           // Create GUI component for determining layer.
          sliderLayer
               = new JSlider(JSlider.HORIZONTAL
                     , AcornsProperties.MIN_LAYERS, AcornsProperties.MAX_LAYERS
                     , startLayer);
          sliderLayer.setMajorTickSpacing(3);
          sliderLayer.setMinorTickSpacing(1);
          sliderLayer.setMaximumSize(new Dimension(200,50));
          sliderLayer.setPaintTicks(true);
          sliderLayer.setPaintLabels(true);
          sliderLayer.setToolTipText(
                                LanguageText.getMessage("commonHelpSets", 18));
          sliderLayer.addChangeListener(
                  new ChangeListener()
                  {
                     public void stateChanged(ChangeEvent event)
                     {  JSlider source = (JSlider)event.getSource();
                        if (!source.getValueIsAdjusting())
                           lesson.setLayer(sliderLayer.getValue());
                     }
                  } );
          sliderLayer.setBackground(new Color(210,210,210));
          sliderLayer.setSize(new Dimension(100, Lesson.ICON+5));
          sliderLayer.setPreferredSize(sliderLayer.getSize());
          sliderLayer.setMinimumSize(sliderLayer.getSize());
      }

      changeLayer(startLayer);
      return sliderLayer;
    }

    /** Change layer displayed in the slider */
    public static void changeLayer(int layer)
    {
    	if (sliderLayer==null) return;
    	
        ChangeListener[] listeners = sliderLayer.getChangeListeners();
        sliderLayer.removeChangeListener(listeners[0]);
        sliderLayer.setValue(layer);
        sliderLayer.addChangeListener(listeners[0]);
    }

   /** Method creating button link to next lesson.
    *
    * @return JButton object
    */
   private static JButton getAnchorButton()
   {  linkLabel = getLinkLabel();

      if (anchorButton == null)
      {
         anchorButton  = new JButton
             (lesson.getIcon(AcornsProperties.ANCHOR, Lesson.ICON));
         anchorButton.setBorder(BorderFactory.createRaisedBevelBorder());
         anchorButton.setToolTipText
                           (LanguageText.getMessage("commonHelpSets", 19));
         anchorButton.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent event)
               {
                   JLabel linkLabel = getLinkLabel();
                   String link = JOptionPane.showInputDialog
                           (LanguageText.getMessage("commonHelpSets", 20),
                            linkLabel.getText());
                   if (link==null) return;
                   linkLabel.setText(link.toLowerCase());
                   lesson.setLink(link);
               }  // End of actionPerformed
            }     // End of Action Listerner
        );        // End of addActionListener
      }
      return anchorButton;
   }

   /** Get Label to display the link to the next lesson
    *
    * @return The JLabel component for the next link
    */
   private static JLabel getLinkLabel()
   {   String link = lesson.getLink();

       if (linkLabel == null)
       {   linkLabel = new JLabel();
           linkLabel.setForeground(Color.GRAY);
           linkLabel.setSize(new Dimension(100, 20));
           linkLabel.setMinimumSize(linkLabel.getSize());
           linkLabel.setPreferredSize(linkLabel.getSize());
       }
       linkLabel.setText(link);
       return linkLabel;
   }

   /** Create button for user help
    *
    * @param toolTip The text to show if user mouses over the button
    * @param category The name of the help items for this type of lesson
    * @return The help button component
    */
   private static JButton getHelpButton(String toolTip, String category)
	   {
       HelpSet helpSet = lesson.getHelpSet();
       if (helpSet==null) return null;

       try
       {
           if (helpButton == null)
           {
               helpButton = new JButton
                       (LanguageText.getMessage("commonHelpSets", 21));
               helpButton.setBorder(BorderFactory.createEtchedBorder());
               helpButton.setToolTipText(toolTip);
           }

           // Replace the listener if necessary.
           ActionListener[] listeners = helpButton.getActionListeners();
           if (listeners.length>0)
               helpButton.removeActionListener(listeners[0]);

           CSH.setHelpIDString(helpButton, category);
               helpButton.addActionListener(
                  new CSH.DisplayHelpFromFocus(helpSet
                         , "javax.help.SecondaryWindow", null));
           return helpButton;

       }
       catch (Throwable t) {}
       return null;

    }  // End of getHelp Button.

    /** Get the button to left justify text */
   private static JButton getLeftButton()
   {
       if (leftButton==null)
       {
           leftButton = new JButton
              (lesson.getIcon(AcornsProperties.LEFT, Lesson.ICON));
           leftButton.setToolTipText
                             (LanguageText.getMessage("commonHelpSets", 22));
           leftButton.addActionListener(
              new ActionListener()
              {
                  public void actionPerformed(ActionEvent event)
                  {
                      setJustifyStatus(false);
                      setJustifiedButtonBorders();
                      lesson.setDirty(true);
                      lesson.displayLesson();
                  }  // End of actionPerformed
              }      // End of Action Listerner
           );        // End of addActionListener
       }
       return leftButton;
   }

   /** Method to return the center button */
   private static JButton getCenterButton()
   {
       if (centerButton==null)
       {
           centerButton = new JButton
              (lesson.getIcon(AcornsProperties.CENTER, Lesson.ICON));

           centerButton.setToolTipText
                             (LanguageText.getMessage("commonHelpSets", 23));
           centerButton.addActionListener(
              new ActionListener()
              {
                  public void actionPerformed(ActionEvent event)
                  {
                      setJustifyStatus(true);
                      setJustifiedButtonBorders();
                      lesson.setDirty(true);
                      lesson.displayLesson();
                  }  // End of actionPerformed
              }      // End of Action Listerner
           );        // End of addActionListener
       }
       return centerButton;
   }

   /** Method to get annotation data object */
   private static AnnotationData getAnnotationDataObject()
   {   try
       {  return (AnnotationData)getAnnotationData.invoke
                                                   (lesson, new Object[0]);
       } catch (Exception e) {}
       return null;
   }

    /** Method to set whether text should be centered
    *
    * @param justify true if to be centered (true if yes)
    */
   private static void setJustifyStatus(boolean justify)
   {   AnnotationData annotations = getAnnotationDataObject();
       if (annotations!=null)  annotations.setCentered(justify);
   }

   /** Method to set the borders correctly */
   private static void setJustifiedButtonBorders()
   {
      AnnotationData annotations = getAnnotationDataObject();
      if (annotations!=null && annotations.isCentered())
      {   getLeftButton().setBorder
                  (BorderFactory.createRaisedBevelBorder());
          getCenterButton().setBorder
                  (BorderFactory.createLoweredBevelBorder());
       }
       else
       {  getLeftButton().setBorder
                  (BorderFactory.createLoweredBevelBorder());
          getCenterButton().setBorder
                  (BorderFactory.createRaisedBevelBorder());
       }
   }

   /** Method to get the combo box for setting font sizes */
   private static JComboBox<String> getFontBox()
   {
      if (fontBox == null)
      {
         String[] items =
                { "" + colors.getSize(), "8", "10",  "12",  "14",  "16",  "18",
                  "20", "22", "24", "26", "28", "30", "36", "40"};
         fontBox = new JComboBox<String>(items);
         fontBox.setToolTipText(LanguageText.getMessage("commonHelpSets", 24));
         fontBox.setBackground(new Color(210,210,210));
         fontBox.setEditable(true);
         fontBox.setSize(new Dimension(45, Lesson.ICON));
         fontBox.setPreferredSize(fontBox.getSize());
         fontBox.setMinimumSize(fontBox.getSize());
         fontBox.setMaximumSize(fontBox.getSize());
         fontBox.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent event)
               {
                  String answer = (String)fontBox.getSelectedItem();
                  try
                  {
                     int factor = Integer.parseInt(answer);
                     if (factor<ColorScheme.MIN_FONT_SIZE
                             || factor > ColorScheme.MAX_FONT_SIZE)
                     {
                        throw new NumberFormatException();
                     }
                     colors.setSize(factor);
							              lesson.setDirty(true);
                     lesson.displayLesson();
                 }
                  catch (NumberFormatException exception)
                  {  setScaleBox(pictureScaleBox, "" + colors.getSize());
 						              Toolkit.getDefaultToolkit().beep();
                  }
               }     // End of actionPerformed.
            }        // End of anonymous ActionListener.
         );          // End of addActionListener.
      }  // End of if (comboBox==null).

      setScaleBox(fontBox, "" + colors.getSize());
      return fontBox;
   }   // End of getFontBox().

   private static void setScaleBox(JComboBox<String> box, String value)
   {
      ActionListener[] listeners = box.getActionListeners();
      box.removeActionListener(listeners[0]);
      box.setSelectedIndex(0);
      box.setSelectedItem(value);
      box.addActionListener(listeners[0]);
   }

}      // End of SetupPanel class
