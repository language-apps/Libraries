/*
 * UndoRedo.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007 - 2015, all rights reserved
 */

package org.acorns.data;

import org.acorns.lesson.*;

/** Definition of the ACORNS undo/redo stack. Lessons access the methods
 *  but do not create it.
 */
public class UndoRedo
{   /** The maximum size of the ACORNS undo and redo stacks */
    public static final int MAX_UNDO = 5;
    
    int redoTop, undoTop;
    UndoRedoData[] redoStack, undoStack;
    AcornsProperties properties;
    
    /** Creates a new instance of UndoRedo using the default maximum size */
    public UndoRedo()  {  initialize(MAX_UNDO); }
    /** Creates a new instance with a specified maximum size */
    public UndoRedo(int max)  {  initialize(max); }
    
    private void initialize(int max)
    {
       redoStack = new UndoRedoData[max];
       undoStack = new UndoRedoData[max];
       properties = AcornsProperties.getAcornsProperties();
       redoTop = undoTop = -1;
    }
    
    /** Reset the redo and undo stacks
    */
    public void resetRedoUndo()  
    { redoTop = undoTop = -1; 
      setFileProperties(false);
    }
    
    /** Determine whether the redo stack is empty
    *
    *  @return true if redo stack is empty
    */
   public boolean isRedoEmpty()   { return (redoTop==-1); }
	
   /** Determine whether the undo stack is empty
    *
    *  @return true if undo stack is empty
    */
   public boolean isUndoEmpty()   { return (undoTop==-1); }
	
   /** Process a redo operation
    *
    * @param current The object's cucrrent state
    * @return The state after the redo operation
    */
   public UndoRedoData redo(UndoRedoData current)
   {
      if (isRedoEmpty()) return null;

      // Push current data into undo stack
      if (undoTop==undoStack.length - 1) undoStack = shift(undoStack);
      else undoTop++;
      
	     UndoRedoData data = redoStack[redoTop--];
      if (current==null) current = data;
      undoStack[undoTop] = current;
      setFileProperties(true);
      return data;
   }
	
   /** Process an undo operation
    *
    * @param current The current objects state
    * @return The state after the undo operation
    */
   public UndoRedoData undo(UndoRedoData current)
   {
      if (isUndoEmpty()) return null;
	
      // Remove from undo stack and add to redo stack.
      UndoRedoData data = undoStack[undoTop--];
      if (redoTop==redoStack.length-1) redoStack = shift(redoStack);
      else redoTop++;

      if (current==null) current = data;
      redoStack[redoTop] = current;
	   setFileProperties(true);
      return data;
   }

   /** Replace top entry of stack
    *
    * @param current Current data object
    * @param undo true for undo stack, fals for redo stack
    */
   public void replaceUndoRedoTop(UndoRedoData current, boolean undo)
   {
      if (undo) undoStack[undoTop] = current;
      else      redoStack[redoTop] = current;
   }
	
   /** Push an undo command onto the undo stack 
    * @param data The object containing data to be pushed.
    */
   public void pushUndo(UndoRedoData data)
   {
      if (undoTop==undoStack.length-1) undoStack = shift(undoStack);
      else undoTop++;
      
	   undoStack[undoTop] = data;
	   redoTop = -1;
      setFileProperties(true);
   }
   
   // Method to shift entries in the stacks if necessary.
   private UndoRedoData[] shift(UndoRedoData[] stack)
   {   for (int s=1; s<stack.length; s++)  stack[s-1] = stack[s];
       return stack;
   }
   
   // Method to get ACORNS file properties.
   private void setFileProperties(boolean dirty)
   {
       if (properties!=null)
       {  properties.activateMenuItems();
          if (dirty) properties.setFileDirty();
       }
   }
         
}  // End of RedoUndo class.