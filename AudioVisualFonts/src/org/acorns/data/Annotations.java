/**
 * ImageAnnotationData.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */
package org.acorns.data;

import java.awt.*;
import org.acorns.editor.*;

/** Interface defining methods used for picture and text annotations to
 *  an audio file. Currently implemented by text and picture annotation modules.
 *
 */
public interface Annotations 
{
    /** Delete an annotation
     *
     * @param point The range of time domain spots
     * @param all Delete all layers (true), otherwise false
     * @return true if successful
     */
    public boolean delete(Point point, boolean all);

    /** Shift annotations up or down
     *
     * @param point The time domain points to shift
     * @param all All layers (true), false otherwise
     * @param direction Direction to right (true), left (false)
     * @return true if successful
     */
    public boolean shift(Point point, boolean all, boolean direction);

    /** Return the undoRedoObject */
    public UndoRedoData undoRedoObject();

    /** Get the audio sound wave object */
    public SoundData getSound();

    /** Create a SoundEditor for this audio
     *
     * @param redoUndo The UndoRedo stack to pass to the SoundEditor
     */
    public void setSoundEditor(UndoRedo redoUndo);

    /** Get the SoundEditor object */
    public SoundEditor getSoundEditor();

    /** Get all of the AnnotationNodes */
    public AnnotationNode[] getAnnotationNodes();

    /** Get the count of annotations */
    public int getAnnotationCount();

    /** Create an identical copy of the annotation object */
    public Annotations clone();

    /** Get the current layer number. For some annotations, there is a different
     *   audio object per layer, for others there is one for all layers. In
     *   the later case, this method always return zero. In the former case
     *   the return is the same as the getAnnotationLevel() method
     */
    public int getSoundLayer();

    /** Get the current annotation layer */
    public int getAnnotationLevel();

    /** Set the current Annotation layer */
    public void setAnnotationLevel(int level);
}
