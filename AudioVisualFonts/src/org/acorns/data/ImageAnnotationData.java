/**
 * ImageAnnotationData.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.data;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import org.acorns.editor.*;
import org.acorns.audio.*;
import org.acorns.properties.*;

/** Extends the SoundData class to add picture annotations to
 *  a portion of an audio recording
 */

public class ImageAnnotationData 
                implements Serializable, Cloneable, Annotations
{
    /** Java file's serial version number */
    private static final long serialVersionUID = 1;
    
    private AnnotationNodeArray[] data; // Table of annotation data
    private SoundData[] sounds;         // Table of attached sounds
    private int layer;                  // The current lesson layer

    private transient Object lesson;    // The acorns lesson object
    
    /** Standard constructor  */
    public ImageAnnotationData()
    {  
       data = new AnnotationNodeArray[AcornsProperties.MAX_LAYERS];
       sounds = new SoundData[AcornsProperties.MAX_LAYERS]; 
       layer = 0;
       
       for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)  
       {
           data[i] = new AnnotationNodeArray();
           sounds[i] = new SoundData();
       }
    }

    
    /** Method to insert a new annotation at the current level
     *  @param picture The picture annotation object associated with this
     *   part of a sound wave
     *  @param point  selection for this annotation
     *  @return true if successful, false otherwise
     */
    public boolean insertAnnotation(PictureData picture, Point point)
    {           
        if (point.y>getSound().getFrames()) return false;
        return data[layer].insert(picture, new Point(point));  
    } 
    
   /** Method to insert a new annotation at the current level
     *  @param picture Picture annotation associated with this part of a
    *          sound wave
     *  @param point  Selection for this annotation
     *  @param sampleRate sample rate of imported file
     *  @return true if successful, false otherwise
     */
    public boolean insertAnnotation
            (PictureData picture, Point point, int sampleRate)
    {           
        point.x = getAnnotationOffset(point.x, sampleRate);
        point.y = getAnnotationOffset(point.y, sampleRate);
        if (point.y>getSound().getFrames()) return false;
        return data[layer].insert(picture, new Point(point));  
    } 
    
    /** Method to scale the annotations to the current SoundData object
     * 
     * @param sampleRate the sample rate present in the imported file
     * @return the computed offset accounting for possible changes in the frame rate.
     */
    private int getAnnotationOffset(int offset, int sampleRate)
    {
       double ratio = getSound().getFrameRate()/sampleRate;
       return (int)(ratio * offset);
    }
    
    /** Method to shift annotations based for a copy operation
     *  @param point  selection for this annotation
     *  @param all unused, only one level can be shifted
     *  @param direction true if shift right, false otherwise
     */
    public boolean shift(Point point, boolean all, boolean direction)
    {   if (point.y>sounds[layer].getFrames()) return false;
        data[layer].shift(new Point(point), direction);
        return true;
    }
    
    /** Get an annotation picture object 
     * 
     * @param point The point in the sound wave where the picture is
     * @return PictureData object or null if it doesn't exist.
     */
    public PictureData getObject(Point point)
    {
        return (PictureData)data[layer].getObject(point);
    }
    
    /** Method to delete an annotation object
     * 
     * @param object Object to remove
     * @return true if successful, false otherwise
     */
    public synchronized boolean deleteObject(Object object)
    {  return data[layer].deleteObject(object); }
    
    /** Method to remove an annotation. 
     *  @param point  selection for this annotation
     *  @param all Only one level (false) or all levels (true) to be deleted
     *  @return true if deletion succeeded, false otherwise
     */
    public synchronized boolean delete(Point point, boolean all)
    {   return delete(point);   }
        
    /** Method to remove an annotation
     *  @param point  selection for this annotation
     *  @return true if deletion succeeded, false otherwise
     */
    public boolean delete(Point point)
    {   
        int frames = sounds[layer].getFrames();
        if (frames==0) return false;
        if (point.y < 0) point.y = frames;

        return data[layer].delete(point); 
    }
    
    /** Method to set the active annotation level
     *  @param layer desired annotation level
     */
    public void setAnnotationLevel(int layer) {  this.layer = layer; }
    
    /** Method to return the active annotation level
     *  @return active annotation level
     */
    public int getAnnotationLevel() { return layer; }
    
    /** Method to return the sound index for the current layer
     *  @return sound layer
     */
    public int getSoundLayer() 
    { return layer; // A sound wave for each layer 
    }
      
    /** Method to return the annotationData for the active level
     *  @return an array of annotations for this level, or null if none
     */
    public AnnotationNode[] getAnnotationNodes()  
    { return data[layer].getAnnotationNodes(); }
    
    /** Method to return the annotationData for the active level
     *  @param level the level of interest
     *  @return an array of annotations for this level, or null if none
     */
    public AnnotationNode[] getAnnotationNodes(int level)
    { return data[level].getAnnotationNodes(); }
    
    /** Method to return the annotationData for a particular level 
     *      scaled for export
     * 
     *  @param level The level identifying which group of nodes to return
     *  @param extension The type of sound file being created
     *  @return an array of annotations for this level, or null if none
     */
    public AnnotationNode[] getScaledAnnotationNodes
                                      (int level, String extension)  
    {   float sampleRate = getSound().getFrameRate();
        float newRate = SoundIO.getDesiredFrameRate(extension);
        float ratio = 1.0f;
        if (newRate>0) ratio = newRate / sampleRate;
        return data[level].getScaledAnnotationNodes(ratio);
    }

    /** Method to return the count of non null annotation nodes
     *  @param level the annotation level of interest
     *  @return count of non-null nodes
     */
    public int getAnnotationCount(int level)
    {   int totalCount = 0;
        
        AnnotationNode[] nodes = data[level].getAnnotationNodes();
        int size = data[level].getAnnotationSize();
        for (int n=0; n<size; n++) 
        {   if (nodes[n].getObject() != null) totalCount++;  }
        return totalCount;        
    }
    
    /** Method to get count of non null annotations at the current level
     * 
     * @return count of non-null annotations
     */
    public int getAnnotationCount() { return getAnnotationCount(layer); }
    
    /** Method to return the count of annotations at the active level
     *  @return number of annotations
     */
    public int getAnnotationSize()  
    { return data[layer].getAnnotationSize(); }
    
    /** Method to retturn the count of annotations at a particular level
     *  @return number of annotations at the designated level
     */
    public int getAnnotationSize(int level) 
    { return data[level].getAnnotationSize(); }
        
    /** Method to set all nodes visible */
    public void setAllVisible()
    {   AnnotationNode[] nodes = data[layer].getAnnotationNodes();
        
        int size = data[layer].getAnnotationSize();
        for (int n=0; n<size; n++) 
        { if (nodes[n]!=null) nodes[n].setVisible(true); }
    }
    
   // Editor object with time domain data.
   private transient SoundEditor soundEditor;
   
  /** Set Sound Editor object */
   public void setSoundEditor(UndoRedo redoUndo)
   {   soundEditor 
          = new SoundEditor(this, sounds[layer], redoUndo); 
   }
   
   /** Retrieve the soundEditor object */
   public SoundEditor getSoundEditor()
   {   return soundEditor;
   }
   
   /** Return the SoundData object for this level */
   public SoundData getSound()  { return sounds[layer]; }
   
   /** Return the SoundData for a particular layer. */
   public SoundData getSound(int level) { return sounds[level]; }

   /** Get the lesson associated with this object */
   public Object getLesson() { return lesson; }

   /** Set the lesson associated with this object */
   public void setLesson(Object lesson) { this.lesson = lesson; }
        
   /** Return the undo redo data for this object. */
   public UndoRedoData undoRedoObject()
   {   ImageAnnotationData dataClone = this.clone();
       dataClone.soundEditor = null;
       return new SoundUndoRedoData(this.clone());
   }
   
   /** Make an identical copy of this object. */
   public @Override ImageAnnotationData clone()
   {   ImageAnnotationData newData = null;

       try
       {   newData = (ImageAnnotationData)super.clone();
           newData.data = new AnnotationNodeArray[AcornsProperties.MAX_LAYERS];
           newData.sounds = new SoundData[AcornsProperties.MAX_LAYERS];
           newData.lesson = lesson;
           
           if (data !=null)
           {
               for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
               {
                   newData.data[i] = data[i].clone();
                   newData.sounds[i] = sounds[i].clone();
               }
           }
       }
       catch (Exception e)   
       { JOptionPane.showMessageDialog
                 (null, "Couldn't clone ImageAnnotationData"); }
       return newData;
   } 

}   // End of ImageAnntationData class
