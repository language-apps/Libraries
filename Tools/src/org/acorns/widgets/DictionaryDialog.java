/**
 *   @name AudioDialog.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2010, all rights reserved
 */

package org.acorns.widgets;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.visual.*;

public class DictionaryDialog extends JDialog
{  
   private static final long serialVersionUID = 1;
   private final static int PANEL_WIDTH = 425, SIZE=200;
   private final static int MIN_MARGIN = 80, MAX_MARGIN = 90;

   private SoundData sound;
   private final PictureData picture;
   private SoundData phraseSound;
   
   /** Method to display information about a sentence
   *
   * @param frame The parent frame
   * @param sentence A descriptive sentence
   * @param pictureData PictureData object containing a picture to display
   * @param phrase Sound object for the main phrase (or null if none)
   * @param soundData Sound object to display
   * @param point The point where to display the data
   * @param lesson The lesson in question
   * @param colors background and foreground colors
   */
public DictionaryDialog(Frame frame, String sentence, PictureData pictureData,
         SoundData phrase, SoundData soundData,
         Point point, Lesson lesson, ColorScheme colors)
{
	this(frame, sentence, pictureData,
	       phrase, soundData, soundData.getSoundText(),
	       point,  lesson, colors);
}   

     /** Method to display information about a sentence
      *
      * @param frame The parent frame
      * @param sentence A descriptive sentence
      * @param pictureData PictureData object containing a picture to display
      * @param phrase Sound object for the main phrase (or null if none)
      * @param soundData Sound object to display
      * @param sountText The gloss and native text to display
      * @param point The point where to display the data
      * @param lesson The lesson in question
      * @param colors background and foreground colors
      */
   public DictionaryDialog(Frame frame, String sentence, PictureData pictureData,
            SoundData phrase, SoundData soundData, String[] soundText,
            Point point, Lesson lesson, ColorScheme colors)
   {
       super(frame, lesson.getTitle(), false);

       sound = soundData;
       picture = pictureData;
       phraseSound = phrase;

       String language = "English";
       if (soundText.length>2) language = soundText[2];
       Font font = KeyboardFonts.getLanguageFonts().getFont(language);
       String face = font.getName();

       JPanel dialog = new JPanel();
       BoxLayout box = new BoxLayout(dialog, BoxLayout.X_AXIS);
       dialog.setLayout(box);
       JComponent component = null;
       
       if (picture!=null)
       {
          JPanel panel = new JPanel()
          {
        	 private final static long serialVersionUID = 1;
        	 
             public @Override void paintComponent(Graphics graphics)
             {
                super.paintComponents(graphics);
                Dimension size = getSize();

                if (picture !=null)
                {  BufferedImage image = picture.getImage
                            (this, new Rectangle(0, 0, size.width, size.height));
                   graphics.drawImage(image, 0, 0, size.width, size.height, null);
                }
                else
                {
                   Color color = new Color(204,204,204);
                   graphics.setColor(color);
                   graphics.fillRect(0, 0, size.width, size.height);
                }
            }
          };
          panel.setPreferredSize(new Dimension(SIZE, SIZE));
          dialog.add(panel);
       }
       dialog.add(Box.createHorizontalStrut(5));

       String text = "<html>&nbsp;" + sentence + "<br><br>"
                   + "&nbsp;" + soundText[0] + "<br><font face=\"" + face
                   + "\" >&nbsp;" + soundText[1] + "<br><br>";

       String[] description = soundText[SoundData.DESC].split("\\n");
       String line, output;
       int spot = -1;
       for (int i=0; i<description.length; i++)
       {   
           line = description[i];
           do
           {
               if (line.length()<MIN_MARGIN) output = line;
               else
               {
                   spot = line.indexOf(' ', MIN_MARGIN);
                   if (spot<0 || spot>MAX_MARGIN) spot = MIN_MARGIN;
                   else spot++; // Move past the space
                   output = line.substring(0, spot);
               }

               text += "&nbsp;" + output + "<br>";
               line = line.substring(output.length());
           }
               
           while (line.length()!=0);
       }
       text += "</html>";

       if (phrase==null || !phrase.isRecorded()) 
       {
          component = new JLabel(text);
          component.setOpaque(true);
       }
       else 
       {   
           component = new JButton(text);
           component.addMouseListener(
                   new MouseListener()
                   {
                       public void mouseExited(MouseEvent event) {}
                       public void mouseEntered(MouseEvent event) {}
                       public void mouseClicked(MouseEvent event) {}
                       public void mousePressed(MouseEvent event) {}
                       public void mouseReleased(MouseEvent event)
                       {
                           phraseSound.stopSound();
                           phraseSound.playBack(null, 0, -1);
                       }
                   });
       }
       if (colors!=null)
       {
    	   component.setOpaque(true);
    	   if (component instanceof JButton)
    		   ((JButton)component).setBorderPainted(false);
    	   component.setBackground(colors.getColor(true));
           component.setForeground(colors.getColor(false));
       }
       dialog.add(component);
       
       dialog.add(Box.createHorizontalStrut(5));

       // Create Play button if there is a recorded sound
       if (sound!=null && sound.isRecorded())
       {
           String toolTip = LanguageText.getMessage("commonHelpSets", 76);
           ImageIcon icon = lesson.getIcon
                           (AcornsProperties.PLAY, 25);
           JButton iconButton = new ToolTipButton(icon, toolTip);
           iconButton.setOpaque(false);
           iconButton.setMargin(new Insets(0,0,0,0));
           iconButton.addActionListener(
               new ActionListener()
               {
                  public void actionPerformed(ActionEvent event)
                  {   sound.stopSound();
                      sound.playBack(null, 0, -1);
                  }
               } );
           dialog.add(iconButton);
       }   // if sound ready to play.

       frame.setIconImage
           (lesson.getIcon(AcornsProperties.ACORN ,20).getImage());
       add(dialog);
       setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
       pack();
       Dimension windowSize = getSize();
       windowSize.width += 20;

       if (windowSize.width<PANEL_WIDTH) windowSize.width = PANEL_WIDTH;
       windowSize.height += 20;
       setSize(windowSize);

       if (point.x + PANEL_WIDTH > frame.getSize().width)
           point.x = frame.getSize().width - PANEL_WIDTH;
       setLocation(point);
       setVisible(true);
   }
   
   class ToolTipButton extends JButton
   {
	   private static final long serialVersionUID = 1;
	   
       public ToolTipButton(ImageIcon icon, String toolTip)
       {
           super(icon);
           setToolTipText(toolTip);
       }
       
       public @Override Point getToolTipLocation(MouseEvent e)
       {  return new Point(20, -30); }
   }
}      // End of DictionaryDialog class