/**
 * PicturesSetUpContainer.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.categories.multiplepictures;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.*;

import org.acorns.data.*;
import org.acorns.widgets.*;
import org.acorns.visual.*;

// Class that will process the setup of a lesson.
public class PicturesSetUpPanel extends JPanel 
              implements MouseListener, Scrollable, MouseMotionListener
{
   private final static long serialVersionUID = 1;
   private final static int GAP = 5;


   private CategoryMultiplePictures  lesson;
   private PictureChoice      currentChoice;
   private GridLayout         layout;
   
   public PicturesSetUpPanel(CategoryMultiplePictures lesson)
   {  
      super(); 
      
      this.lesson = lesson;

      setOpaque(false);
      layout = new GridLayout(0,5,GAP,GAP);
      setLayout(layout);
      setComponents();
      
   }  // End PicturesSetUpLabel2()
   
   /** Method to reinsert all of the components. */
   public final void setComponents()
   {
      ChoiceButton button;
      lesson.removeListeners();
      removeAll();
      PictureChoice[] data = lesson.getPicturesData();      
      Dimension buttonSize;
      for(int i=0; i<data.length; i++)
      {
          if (data[i] == null) break;
         button = data[i].getButton();
         button.removeListeners();
         button.addMouseListener(this);
         button.addMouseMotionListener(this);
         buttonSize = button.getSize();
         buttonSize.height += 1;
         buttonSize.width  += 1;
         add(data[i].getButton());         
      }
      Dimension displaySize = lesson.getDisplaySize();
      Dimension pictureSize = lesson.getPictureSize();
      Object scrollObj = UIManager.get("ScrollBar.width");
      
      int scrollWidth = 20;
      if (scrollObj!=null) scrollWidth = Integer.parseInt(scrollObj.toString());

      displaySize.width  -= (scrollWidth );

      int pictureWidth = pictureSize.width + GAP;
      
      if (displaySize.width < pictureWidth)
               displaySize.width = pictureWidth;
      int picturesPerRow = displaySize.width / pictureWidth;
      int border = displaySize.width - ((displaySize.width / pictureWidth) * pictureWidth);
      border/=2;
      setBorder(BorderFactory.createEmptyBorder(0, border, 0, 0));
      layout.setColumns(picturesPerRow);
      repaint();
   }

   /** Method to draw the picture in the background
     *
     * @param g The graphics object associated with this component
     */
    public @Override void paintComponent(Graphics g)
    {
        BufferedImage image = null;
        ColorScheme colors = lesson.getColorScheme();
        PictureData picture = colors.getPicture();
        Dimension setupSize = lesson.getDisplaySize();
        if (picture!=null)
           image = picture.getImage
              (this, new Rectangle(0,0,setupSize.width,setupSize.height));

         if (image!=null)
            g.drawImage
             (image, 0, 0, image.getWidth(), image.getHeight(), this);
    }
   
   //------------------------------------------------------------
   // Method to handle dialogs for creating sound data.
   //------------------------------------------------------------
   public void picturesDialog(PictureChoice choice)
   {
      PicturesSoundData sounds = choice.getQuestions(lesson.getLayer());
      int selection = lesson.getSelectedIndex();
      LessonActionPicturesData oldData 
            = new LessonActionPicturesData(choice, false, selection);

      Class<?> className = lesson.getClass();
      int description = PicturesSoundData.STANDARD;
      try
      {
         Field descriptionField = className.getDeclaredField("description");
         if (descriptionField!=null)
            if (descriptionField.getBoolean(lesson))
               description = PicturesSoundData.DESCRIPTION;
      }
      catch (Exception e) {}

      Frame frame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this);
      int result = sounds.pictureDialog(lesson, frame, description);
  
      if(result != JOptionPane.CANCEL_OPTION)
      {  // Mark that the file needs a save operation.
         lesson.setDirty(true);
			
         // Enable the redo commands.
         lesson.pushUndo(oldData);
      }
      
   }  // End picturesDialog()
   
   
   //--------------------------------------------------------------
   // Methods to respond to mouse.
   //--------------------------------------------------------------   
   
   // Method to get the button object that was pressed.
   private ChoiceButton getChoiceButton(MouseEvent event)
   {
       String sourceName = event.getSource().getClass().getSimpleName();
       if (!sourceName.equals("ChoiceButton")) return null;
       
       return (ChoiceButton)event.getSource();       
   }
   
   public void mouseClicked(MouseEvent event)
   {
      ChoiceButton choice = getChoiceButton(event);
      if (choice==null) return;
      
      currentChoice = choice.getPictureChoice();
      PictureChoice pictureChoice = choice.getPictureChoice();
      if (lesson.isSelected(pictureChoice))
      {   lesson.getData();
          picturesDialog(currentChoice);
      }
      else lesson.select(pictureChoice);
     
    }  // End mouseClicked()

   private transient boolean drag = false;

   public void mouseEntered (MouseEvent event) {}
   public void mouseExited  (MouseEvent event) {}
   public void mousePressed (MouseEvent event) 
   {   ChoiceButton choice = getChoiceButton(event);
       PictureChoice pictureChoice = choice.getPictureChoice();
       if (choice!=null) 
           choice.selectBorders(true, lesson.isSelected(pictureChoice));           
   }
   public void mouseReleased(MouseEvent event) 
   { 	
       ChoiceButton choice = getChoiceButton(event);
       if (choice==null) return;
       
       PictureChoice pictureChoice = choice.getPictureChoice();
       if (drag)
       { 
          if (getMousePosition(true) == null)
          {
             if (lesson.isSelected(pictureChoice))
             {  lesson.removeSelectedPicture();  }
          }
          else
          {  
        	  lesson.reorderPictures();
          }
          drag = false;
       }
       choice.selectBorders(false, lesson.isSelected(pictureChoice));
   }

   public void mouseMoved(MouseEvent event) {}
   public void mouseDragged(MouseEvent event)
   { drag = true;}

   //--------------------------------------------------------------     
   // Scrollable Methods.
   //--------------------------------------------------------------     
   public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction)
   {  
       if(orientation == SwingConstants.HORIZONTAL)   return (getWidth() / 10);
       else return (getHeight() / 10); 
       
   }  // End getScrollableBlockIncrement()
   
   public Dimension getPreferredScrollableViewportSize()
   { return getSize(); }
   
   public boolean getScrollableTracksViewportHeight() { return false; }
   public boolean getScrollableTracksViewportWidth()  { return false; }
   public int getScrollableUnitIncrement(Rectangle r,int o,int d) {return 10;}

}  // End PicturesSetUpContainer
