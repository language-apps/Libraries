/*
 * SoundUndoRedoData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.data;

/** UndoRedo object used by audio objects with annotations */
public class SoundUndoRedoData extends UndoRedoData
{
    Annotations sound;

    /** Constructor
     *
     * @param sound audio object with annotations
     */
    public SoundUndoRedoData(Annotations sound)  {  this.sound = sound; }

    /** Satisfy the abstract class requirements
     *
     * @return sound audio object with annotations
     */
    public Annotations getData() { return sound; }
}

