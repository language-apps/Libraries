/*
 * LessonPopupMenu.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson;

import javax.swing.*;
import java.awt.*;

/** Popup menu that appears when users click the center icon to
 *  see lesson specific execution options
 */
public class LessonPopupMenu extends JPopupMenu
{
   private JPanel  panel;   // Lesson play panel
   private boolean armed;
   
   private static final long serialVersionUID=1L;

   /** Create a popup menu with execution mode lesson options
    *
    * @param lesson The lesson object
    * @param panel The lesson play panel to attach the popup
    * @param items The list of JMenu items (one for each option)
    */
   public LessonPopupMenu(Lesson lesson, JPanel panel, JMenuItem[] items)
   {
      this.panel = panel;
      
      for (int i=0; i<items.length; i++)
      {   if (items[i]==null) addSeparator();
          else                add(items[i]);
      }
   }
  
   /** Cancel the popup menu */
   public void cancel()  
   {  firePopupMenuWillBecomeInvisible(); 
      setVisible(false);
      armed = false;
   }  
    
   /** Determine if the popup menu is armed. */
   public boolean isArmed() {return armed;}
   
   /** Method to fire this popup
    */
   public void fire()
   {  firePopupMenuWillBecomeVisible();
      armed = true;
      Dimension size = panel.getSize();
      show(panel, size.width/2,size.height-100);
   }
    
}  // End LessonPopUpMenu class
 
