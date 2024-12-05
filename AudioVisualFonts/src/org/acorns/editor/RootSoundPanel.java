/*
 * RootSoundPanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.editor;

import java.beans.*;
import javax.swing.*;
import org.acorns.data.*;

/** This class is the root panel for the SoundEditor panels in SoundMode
 *  and in Annotation node.
 *
 *  The lessons that access need this panel are those that display sound
 *  waves. They extend this class, fill in the blank methods, and add a
 *  SoundPanel object to it.

 */
public abstract class RootSoundPanel extends JPanel 
                                       implements PropertyChangeListener
{
   private static final long serialVersionUID = 1;

    
    /** Property Change Listener to draw vertical line during play back.
      *
      *  @param event property change "PlayBack" fired by the sound data object.
      */
     public void propertyChange(PropertyChangeEvent event)  {}

     /** Set the Annotation object in the panel
      *
      * @param data The AnnotationData object
      * @param panel The SoundPanel object (relevant when more than one panel)
      */
     public abstract void setAnnotationData(Annotations data, JPanel panel);

     /** Get the AnnotationData object
      *
      * @param panel The SoundPanel object (relevant when more than one panel)
      * @return The AnnotationData object
      */
     public abstract Annotations getAnnotationData(JPanel panel);

     /** Return the ACORNS UndoRedo stack <br>
      * (getUndoRedoStack()) is a Lesson class method */
     public abstract UndoRedo getUndoRedo();
  }

