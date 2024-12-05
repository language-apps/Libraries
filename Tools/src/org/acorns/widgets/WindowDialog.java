/**
 * WindowDialog.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2010, all rights reserved
 */

package org.acorns.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;

/** Create a JDialog panel that displays gloss/native text,
 *  descriptive information (if it exists), and audio.
 * It is used in a variety  of lessons.
 */
public class WindowDialog extends JDialog
{
	private static final long serialVersionUID = 1;
    private static final int LINE = 80;
    private static final int PANEL_WIDTH = 425;

    private SoundData sound;

    /** Create a Window Dialog object to display gloss/text/description/audio
     *
     * @param lesson The lesson object that applies to the call
     * @param frame The frame to which this dialog belongs
     * @param title The title of the dialog
     * @param soundData The audio object
     * @param point Location in frame to place the dialog window
     * @param fg A foreground color
     * @param bg A background color
     */
    public WindowDialog( Lesson lesson, Frame frame, String title
            , SoundData soundData, Point point, Color fg, Color bg)
    {
      super(frame, title, false);
      initialize(lesson, frame, title, soundData, point, fg, bg);

    }

    /** Create a dialog to display gloss/text/description/audio (no colors)
     *
     * @param lesson The lesson object that applies to the call
     * @param frame The frame to which the dialog belongs
     * @param title The title of the dialog
     * @param soundData The audio object
     * @param point The place in the frame to place the dialog window
     */
    public WindowDialog( Lesson lesson, Frame frame, String title
            , SoundData soundData, Point point)
    {
      super(frame, title, false);
      Color fg = Color.BLACK;
      Color bg = Color.WHITE; 
      initialize(lesson, frame, title, soundData, point, fg, bg);
    }

    private void initialize(Lesson lesson, Frame frame, String title
                                         , SoundData soundData, Point point
                                         , Color fg, Color bg)
    {
      sound = soundData;

      // Process the play pictures command.
      JPanel dialog = new JPanel();
      dialog.setBackground(bg);
      BoxLayout box = new BoxLayout(dialog, BoxLayout.X_AXIS);
      dialog.setLayout(box);

      JPanel panel = getTextPanel(soundData, fg, bg);
      if (panel!=null) dialog.add(panel);

      // Create Play button if there is a recorded sound
      if (soundData!=null && soundData.isRecorded())
      {
          ImageIcon icon = lesson.getIcon
                          (AcornsProperties.PLAY, 25);
          JButton button = new JButton(icon)
          {
        	 private static final long serialVersionUID = 1;
        	 
             public @Override Point getToolTipLocation(MouseEvent e)
             {  return new Point(20, -30); }
          };
          button.setOpaque(false);
          button.setMargin(new Insets(0,0,0,0));
          button.setToolTipText(LanguageText.getMessage(lesson, 1));
          button.addActionListener(
              new ActionListener()
              {
                 public void actionPerformed(ActionEvent event)
                 {   sound.stopSound();
                     sound.playBack(null, 0, -1);
                     return;
                 }
              } );
          dialog.add(button);
      }   // if sound ready to play.

      frame.setIconImage
          (lesson.getIcon(AcornsProperties.ACORN ,20).getImage());
      if (dialog.getComponentCount()==0) return;
      
      add(dialog);
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      pack();
      Dimension windowSize = getSize();
      windowSize.width += 20;

      if (windowSize.width<PANEL_WIDTH) windowSize.width = PANEL_WIDTH;
      windowSize.height += 20;
      setSize(windowSize);

      setVisible(true);
      if (point.x + PANEL_WIDTH > frame.getSize().width)
          point.x = frame.getSize().width - PANEL_WIDTH;
      
      Point frameLocation = frame.getLocation();
      point.x = point.x+frameLocation.x;
      point.y = point.y + frameLocation.y;
      setLocation(point);
    }

    /** Method to format the panel with the text */
    private JPanel getTextPanel(SoundData soundData, Color fg, Color bg)
    {  String[]  soundText = soundData.getSoundText();
       JPanel panel  = new JPanel();
       panel.setBackground(bg);
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

       boolean show = soundText[SoundData.GLOSS].length()!=0
                || soundText[SoundData.NATIVE].length()!=0
                || soundText[SoundData.DESC].length() !=0;
       if (!show) return null;
       JLabel label;
       label = new JLabel(soundText[SoundData.GLOSS]);
       label.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
       label.setForeground(fg);
       label.setFont(new Font(null, Font.PLAIN, 14));
       panel.add(label);
       panel.add(Box.createVerticalStrut(5));
       
       label = new JLabel(soundText[SoundData.NATIVE]);
       label.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
       label.setForeground(fg);
       String language = "English";
       if (soundText.length>SoundData.LANGUAGE)
              language = soundText[SoundData.LANGUAGE];
       Font font = KeyboardFonts.getLanguageFonts().getFont(language);
       label.setFont(font);
       panel.add(label);
       panel.add(Box.createVerticalStrut(10));
       

       String text ="";
       if (soundText[SoundData.DESC].length()!=0)
       {   String[] lines
                 = soundText[SoundData.DESC].replaceAll(" +", " ").trim().split("[\\r\\n]+");
           StringBuilder buf = new StringBuilder();
           buf.append("<html>");

           int index;
           String words;
           for (int i=0; i<lines.length; i++)
           {   words = lines[i];
	           while (words.length()>0)
	           {  buf.append("<br>");
	              if (words.length()>LINE)
	              {  index = words.substring(0,LINE).lastIndexOf(" ");
	                 if (index<0) index = LINE;
	              }
	              else index = words.length();
	
	              buf.append(words.substring(0, index));
	              words = words.substring(index).trim();
	           }
	           if (i<lines.length) buf.append("<br>"); 
           }
           text += buf.toString();
           label = new JLabel(text);
           label.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
           label.setForeground(fg);
           label.setFont(new Font(null, Font.PLAIN, 14));
           panel.add(Box.createVerticalStrut(10));
           panel.add(label);
       }
       return panel;
    }
}