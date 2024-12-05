/**
 * LessonActionData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.data;

/** Abstract class for objects pushed and popped using the ACORNS undo redo
 * stack.
 */
public abstract class UndoRedoData
{   /** Method to get the object that can be undone or redone */
    public abstract Object getData(); }