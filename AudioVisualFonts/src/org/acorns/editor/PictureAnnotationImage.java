/**
 * PictureAnnotationImage.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.editor;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import org.acorns.visual.*;
import org.acorns.data.*;

public class PictureAnnotationImage 
{   public final Dimension ANNOTATION_SIZE = new Dimension(300,300);

/** Method to draw pictures annotated to a sound wave
 * 
 * @param graphics The graphics object to draw into
 * @param annotations The object with the annotatons
 * @param width The width of the panel
 * @param clip The visible area
 * @param panel The panel onto which to draw the picture
 * @param colors The background color object
 */
  public synchronized void drawAnnotation
    (Graphics graphics, Annotations annotations
       , int width, Rectangle clip, JPanel panel, ColorScheme colors)
  {
     Color color = colors.getColor(true);
     graphics.setColor(color);
     graphics.fillRect(clip.x,clip.y, clip.width, clip.height);

     // Handle the annotations
     SoundData sound = annotations.getSound();
     long frames = sound.getFrames();
     double samplesPerPoint = (double)frames / width;

     AnnotationNode[] nodes = annotations.getAnnotationNodes();
     if (nodes == null || nodes.length == 0 
                       || nodes[0]==null || frames==0) return;

     int startOffset, endOffset = (int)(nodes[0].getOffset() / samplesPerPoint);
     int newWidth, newHeight, x, y;
     double scaleX, scaleY, scale;
     Rectangle pictureSize;
     BufferedImage picture;
     PictureData pictureData;
     Dimension size;

     for (int i=1; i<nodes.length && nodes[i]!=null; i++)
     {
         startOffset = endOffset;
         endOffset = (int)(nodes[i].getOffset() / samplesPerPoint);
         pictureData = (PictureData)nodes[i].getObject();
         
         // If we find an object, it is a picture to display
         Color pictureBackground = new Color(180,180,180);
         if (pictureData!=null)
         {
                // Is the picture in the visible area?
             if (startOffset <= clip.x + clip.width && endOffset >= clip.x)
             {
                 // Now scale the picture to the available space
                 int pictureWidth = endOffset - startOffset;
                 pictureData.loadImages(true, ANNOTATION_SIZE);

                 size = pictureData.getSize();
                 if (size == null) return;

                 scaleX = 1.0 * pictureWidth / size.width;
                 scaleY = 1.0 * clip.height / size.height;
                 scale = scaleX;
                 if (scaleX > scaleY) scale = scaleY;
                 newWidth = (int)(size.width * scale);
                 newHeight = (int)(size.height * scale);
                 x = startOffset + (pictureWidth - newWidth)/2;
                 y = clip.y + (clip.height - newHeight) /2;

                 graphics.setColor(pictureBackground);
                 graphics.fillRect(startOffset, clip.y, pictureWidth, clip.height);

                 pictureSize = new Rectangle(0, 0, newWidth, newHeight);
                 picture = pictureData.getImage(panel, pictureSize);
                 graphics.drawImage(picture, x, y, newWidth, newHeight, null); 
                 
             }   // End if picture within bounds
                
         }   // End of if
         graphics.setColor(Color.WHITE);
         graphics.drawLine(endOffset, clip.y, endOffset, clip.y+clip.height);
         graphics.drawLine(startOffset, clip.y, startOffset, clip.y+clip.height);
     }       // End of for loop
  }        // End of drawAnnotation() method

}  // End of PictureAnnotationImage class
