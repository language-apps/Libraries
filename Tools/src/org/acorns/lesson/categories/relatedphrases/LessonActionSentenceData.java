/**
 * LessonActionSentenceData.java
 * @author HarveyD
 * @version 6.00
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.categories.relatedphrases;

import org.acorns.data.*;

public class LessonActionSentenceData extends UndoRedoData
{

    private SentenceData sentence;
    private int layer;
    private int row;

   /** Constructor
    *
    * @param sentence The data associated with a particular sentence
    * @param layer The lesson layer
    * @param row Which sentence in the layer
    */
    public LessonActionSentenceData(SentenceData sentence, int layer, int row)
    {   this.layer = layer;
        this.row = row;
        if (sentence!=null)
             this.sentence = sentence.clone();
        else this.sentence = null;
    }

   /** Method to complete the UndoRedoData abstract class
    *  @return data object for undo redo operations
    */
    public SentenceData getData()	{ return sentence;	}
    public int getLayer() { return layer; }
    public int getRow()   { return row;   }

} // End LessonActionSentenceData
