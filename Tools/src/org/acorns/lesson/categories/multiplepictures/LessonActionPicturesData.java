/**
 * LessonActionPicturesData.java
 * @author HarveyD
 * @version 6.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.categories.multiplepictures;

import org.acorns.data.*;

/** The UndoRedoData extension for lessons with multiple pictures */
public class LessonActionPicturesData extends UndoRedoData
{

   private PictureChoice pictureChoice;
   private boolean deleted;
   private int selected;

   /** Constructor
    *  @param pictureChoice PictureChoice  data object for redos/undos.
    *  @param deleted flag to indicate if this is a deleted object
    *  @param selected indentify the Picture Choice object number
    */
    public LessonActionPicturesData
            (PictureChoice pictureChoice, boolean deleted, int selected)
    {   if (pictureChoice!=null)
             this.pictureChoice = pictureChoice.clone();
        else this.pictureChoice = null;
        this.deleted = deleted;
        this.selected = selected;
    }

   /** Method to complete the UndoRedoData abstract class
    *  @return data object for undo redo operations
    */
    public PictureChoice getData()	{ return pictureChoice;	}

    /** Determine  if this was a deleted picture */
    public boolean isDeleted() { return deleted; }
    public int getSelected()   { return selected; }

} // End LessonActionPicturesData