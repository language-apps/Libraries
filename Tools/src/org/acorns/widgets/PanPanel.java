/**
 * PanPanel.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */
package org.acorns.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;

import org.acorns.data.*;

/** Abstract panel class with a picture that users can pan by moving the mouse
   near one of the borders.<br>
 * Users of this class create a panel that extends PanPanel.
 */
public abstract class PanPanel extends JPanel implements MouseMotionListener
{
   private static final long serialVersionUID = 1;
   private static final int SCROLL_SPEED = 30, SCROLL_BORDER = 25;

   private Dimension pictureSize;
   private Rectangle viewport;
   private BufferedImage image;
   private PictureData pictureData;
   private int speed, border;

   /** Constructor to create a panning panel
    *
    * @param data The picture object
    * @param background The background color
    */
   public PanPanel(PictureData data, Color background)
   {
      pictureData = data;
      speed       = SCROLL_SPEED;
      border      = SCROLL_BORDER;

      setBackground(background);
      setBorder(BorderFactory.createRaisedBevelBorder());

      addMouseMotionListener(this);
   }  // End of constructor

   /** Method to position the image within the panel */
   private void positionPicture()
   {
      Dimension panelSize = getSize();
      pictureSize = pictureData.getSize();

      viewport = new Rectangle((panelSize.width-pictureSize.width)/2,
                               (panelSize.height-pictureSize.height)/2,
                                pictureSize.width, pictureSize.height);

   }  // End of positionPicture()

   /** Method to force the picture to redraw (possibly after a panel resize) */
   protected void resetPicture() { image = null; }

   /** Get the viewport for this panel
    *
    * @return The viewable rectangle
    */
   public Rectangle getViewableArea() { return viewport; }

   /** Set the border size in pixels
    *
    * @param scrollBorder The border size in pixels
    */
   public void setScrollBorder(int scrollBorder) { border = scrollBorder; }

   /** Set the scrolling speed
    *
    * @param scrollSpeed Scroll speed in pixels per event
    */
   public void setScrollSpeed(int scrollSpeed)   { speed  = scrollSpeed; }

   /** Overridden method to redraw the picture */
   public @Override void paintComponent(Graphics page)
   {
      super.paintComponent(page);

      Graphics2D graphics = (Graphics2D)page;
      BufferedImage newImage = image;

      if (image==null)
      {  positionPicture();
         newImage = image = pictureData.getImage
              (this, new Rectangle(0, 0, pictureSize.width,pictureSize.height));
      }
      else  if (pictureData.getNumberFrames()!= 1)
      {  newImage = pictureData.getImage(this, new Rectangle
                 (0, 0,image.getWidth(),image.getHeight()));
      }

      int imageWidth  = newImage.getWidth();
      int imageHeight = newImage.getHeight();
      System.gc();
      graphics.drawImage
              (newImage, viewport.x, viewport.y, imageWidth, imageHeight, this);

      paintMore(page);
   }

   /** Method that causes additional drawing onto the panel. For example
    * Picture and Sound paintCompnent() method lessons draws acorns
    * on the panel.
    *
    * @param page The graphics object
    */
   protected abstract void paintMore(Graphics page);

   /** Panning control event that responds to mouse moves */
   public void mouseMoved(MouseEvent event)
   {
      int xPoint = event.getX();
      int yPoint = event.getY();

      Dimension panelSize = getSize();
      if (panelSize == null) return;
      if (pictureSize==null) return;
      
      int distanceX = 0, distanceY = 0, distance;

      if (panelSize.width < pictureSize.width)
      {   distance = panelSize.width - xPoint;
          if (xPoint < border)
          {   if (viewport.x < 0)
              {  if (viewport.x + speed > 0) distanceX = -viewport.x;
                 else distanceX = speed;
              }
          }
          else if (distance>0 && distance < border)
          {  distance = viewport.x + pictureSize.width - panelSize.width;
             if (distance >0)
             {   if (distance > speed) distance = speed;
                 distanceX = -distance;
             }
          }
      }

      if (panelSize.height < pictureSize.height)
      {   distance = panelSize.height - yPoint;
          if (yPoint < border)
          {   if (viewport.y < 0)
              {  if (viewport.y + speed > 0) distanceY = -viewport.y;
                 else distanceY = speed;
              }
          }
          else if (distance>0 && distance < border)
          {  distance = viewport.y + pictureSize.height - panelSize.height;
             if (distance >0)
             {   if (distance > speed) distance = speed;
                 distanceY = -distance;
             }
          }
      }

      if (distanceX != 0 || distanceY != 0)
      {
          viewport.x += distanceX;
          viewport.y += distanceY;
          repaint();
      }
  }   // End of mouseMoved()

   /** Unused mouse motion event */
    public void mouseDragged(MouseEvent event) {}
}
