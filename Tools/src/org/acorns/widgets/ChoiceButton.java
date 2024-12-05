/*
 * ChoiceButton.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.widgets;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.BorderFactory;

import org.acorns.data.*;

/** Display button used by lessons with pictures to which is attached audio
 */
public class ChoiceButton extends AbstractButton
{
   private  final static long serialVersionUID = 1;
   private final static int THICKNESS = 4;
   
   private Border  outerNoSelect, outerSelect;
   private Border  innerLow, innerRaised;
   private Border  compoundLowNoSelect, compoundLowSelect;
   private Border  compoundHighNoSelect, compoundHighSelect;

   private Dimension     buttonSize;
   private PictureChoice pictureChoice;

   boolean selected;

   /** Create a button of a desired size
    *
    * @param pictureChoice The PictureChoice object
    * @param size Desired size to display picture
    */
   public ChoiceButton(PictureChoice pictureChoice, Dimension size)
   {
      resizeButton(size);
      this.pictureChoice = pictureChoice;
      selectBorders(false, false);
      
   }  // End ChoiceButton

   /** Method to resize the display
    *
    * @param size Desired size
    */
   public final void resizeButton(Dimension size)
   {  
      buttonSize = new Dimension(size);
      setSize(new Dimension(buttonSize.width, buttonSize.height));
      setPreferredSize(getSize());
      setMinimumSize(getSize());
      setMaximumSize(getSize());
   }
   
   /** Get the PictureChoice object that goes with this button. */
   public PictureChoice getPictureChoice() { return pictureChoice; }
   /** Set the PictureChoice object tat goes with this button */
   public void setPictureChoice(PictureChoice choice) {pictureChoice = choice;}

   // Method to create borders.
   private void makeBorders()
   {    
       if (outerNoSelect==null)
       {
           outerNoSelect = BorderFactory.createLineBorder
                            (new Color(80,80,80), THICKNESS);
           innerLow  = BorderFactory.createLoweredBevelBorder();
           compoundLowNoSelect 
              = BorderFactory.createCompoundBorder(outerNoSelect, innerLow);
           
           outerSelect = BorderFactory.createLineBorder(Color.RED, THICKNESS);
           compoundLowSelect
              = BorderFactory.createCompoundBorder(outerSelect, innerLow);
           
           innerRaised = BorderFactory.createRaisedBevelBorder();
           compoundHighNoSelect 
              = BorderFactory.createCompoundBorder(outerNoSelect, innerRaised);
           
           compoundHighSelect
              = BorderFactory.createCompoundBorder(outerSelect, innerRaised);
       }
   }
   
   /** Set the border to indicate if this button is pressed or selected
    *  @param pressed true if pressed, false otherwise
    *  @param selected true if pressed, false otherwise
    */
   public final void selectBorders(boolean pressed, boolean selected)
   {   
       makeBorders();
       this.selected = selected;
       if (pressed && selected)   setBorder(compoundLowSelect);
       if (pressed && !selected)  setBorder(compoundLowNoSelect);
       if (!pressed && selected)  setBorder(compoundHighSelect);
       if (!pressed && !selected) setBorder(compoundHighNoSelect);
       repaint();
   }

   /** Overridden method used by the class */
   public @Override void paintComponent(Graphics page)
   {  
      super.paintComponent(page);

      // Draw the background
      Graphics2D graphics = (Graphics2D)page;
      graphics.setColor(new Color(180,180,180));
      graphics.fillRect(0, 0, buttonSize.width, buttonSize.height);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING
                                    , RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                                    , RenderingHints.VALUE_INTERPOLATION_BILINEAR);
       
      // Get the size of the picture to place in the button
      Dimension size = pictureChoice.getPictureData().getSize();
      if (size == null) return;
      Rectangle center = new Rectangle(0,0,buttonSize.width, buttonSize.height);
      double scaleX = 1.0 * buttonSize.width / size.width;
      double scaleY = 1.0 * buttonSize.height / size.height;
      double scale  = 1.0;
      Dimension newSize = new Dimension(buttonSize.width, buttonSize.height);
      
      // Determine the coordinates and the scaling
      switch(pictureChoice.getType())
      { 
         case PictureChoice.SCALE:
            scale = scaleX;
            if (scaleX > scaleY) scale = scaleY;
            newSize.width  = (int)(size.width * scale);
            newSize.height = (int)(size.height * scale); 
            center = centerImage(newSize);
            break;
            
         case PictureChoice.STRETCH:            
            break;
            
         case PictureChoice.TRUNCATE:
             scale = scaleX;
             if (scaleX < scaleY) scale = scaleY;
             newSize.width  = (int)(size.width * scale);
             newSize.height = (int)(size.height * scale); 
             center = centerImage(new Dimension((int)(scale*size.width), (int)(scale*size.height)));
             break;
            
         case PictureChoice.CHOP_HORIZONTAL:
             newSize.height = (int)(size.height * scaleY);
             newSize.width  = (int)(size.width * scaleY);
             center = centerImage(newSize);
            break;
            
         case PictureChoice.CHOP_VERTICAL:
             newSize.height = (int)(size.height * scaleX);
             newSize.width  = (int)(size.width * scaleX);
            center = centerImage(newSize);
      }  // End switch
      
      BufferedImage image = pictureChoice.getPictureData().getImage(this, center);
      graphics.drawImage(image, center.x, center.y, null);

   }  // End paintComponent()

   /** Remove extraneous listeners */
   public void removeListeners()
   {
      // Remove all ActionListeners
      ActionListener[] al = getActionListeners();
      
      for(int i=0; i<al.length; i++)
         removeActionListener(al[i]);
      
      // Remove all MouseListeners
      MouseListener[] ml = getMouseListeners();
      
      for(int i=0; i<ml.length; i++)
         removeMouseListener(ml[i]);

      MouseMotionListener[] mm = getMouseMotionListeners();

      for (int i=0; i<mm.length; i++)
          removeMouseMotionListener(mm[i]);

      validate();
   }  // End removeListeners()

   /** Method to determine if this button is selected
    *
    * @return true if yes, false otherwise
    */
   public boolean isChoiceSelected()   { return selected;  }
   
   // Method to compute the centered coordinates for an image
   private Rectangle centerImage(Dimension size)
   {  Rectangle position 
              = new Rectangle(0, 0, size.width, size.height);
      
      int width = size.width; 
      position.x = (buttonSize.width - width) / 2;

      int height = size.height;
      position.y = (buttonSize.height - height) / 2;
      return position;
   }

}  // End PictureChoice

